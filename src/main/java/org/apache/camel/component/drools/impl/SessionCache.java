/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * Wrapper for LRU map storing drools sessions with additional capabilities:
 * - sessions with sufficiently low time to next scheduled timer are not removed.
 * - if session with given id is not in the map, then  
 *
 *
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 12:04:11 PM
 */
public class SessionCache implements InitializingBean {

    protected int cacheSize = 500;

    protected long removeOldSessionLimit = 10000;

    protected LRUMap sessions;

    SessionDAO sessionDAO;

    protected long moveSessionsToMemoryInterval = 20000;

    SessionMaker sessionMaker;

    protected static Log log = LogFactory.getLog(SessionCache.class);

    @SuppressWarnings("serial")
    public void afterPropertiesSet() {
       sessions = new LRUMap(cacheSize) {
    	        @Override
    	        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
                    StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) entry
    	                    .getValue();
    	            if (Utils.getTimeToNextScheduled(ksession) < removeOldSessionLimit) {
    	                return false;
    	            }
    	            log.debug("removing: "+ksession.getId());
    	            ksession.dispose();
    	            return true;
    	        }
    	    };
    }
    
    public StatefulKnowledgeSession getSession(MultiKey key, Object trigger) {
        return getSession(key, false, trigger);
    }

    public StatefulKnowledgeSession getSession(MultiKey key, boolean dirty, Object trigger) {
        synchronized (this) {
            StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) sessions.get(key);
            if (ksession != null && dirty) {
                ksession.dispose();                
            }
            if (ksession == null || dirty) {
                log.debug("Making session, dirty: " + dirty);
                ksession = sessionMaker.makeSession(key, trigger).getSession();
                sessions.put(key, ksession);
            }
            return ksession;
        }
    }

    public synchronized void loadSessions() throws Exception {
        for (Map.Entry<MultiKey, Boolean> me : sessionDAO.loadSessions(moveSessionsToMemoryInterval).entrySet()) {
            StatefulKnowledgeSession ksession = getSession(me.getKey(), me.getValue(), null);
            int fired = ksession.fireAllRules();
            log.debug("sessionId: " + ksession.getId() + ", fired: " + fired
                    + ", next timer: " + new Date(Utils.getTimeToNextScheduled(ksession)));
        }
    }

    public void updateSession(StatefulKnowledgeSession ksession) {
        int i = 1;
        while (i > 0) {
            i = ksession.fireAllRules();
            log.debug("fired: " + i);
        }
    }

    @SuppressWarnings("unchecked")
    public void close() {
        log.debug("closing drools...");
        for (StatefulKnowledgeSession ksession : ((Collection<StatefulKnowledgeSession>) sessions.values())) {
            ksession.dispose();
        }
        log.debug("drools closed");
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setRemoveOldSessionLimit(long removeOldSessionLimit) {
        this.removeOldSessionLimit = removeOldSessionLimit;
    }

    public void setMoveSessionsToMemoryInterval(
            long moveSessionsToMemoryInterval) {
        this.moveSessionsToMemoryInterval = moveSessionsToMemoryInterval;
    }

    @Required
    public void setSessionMaker(SessionMaker sessionMaker) {
        this.sessionMaker = sessionMaker;
    }

    @Required
    public void setSessionDAO(SessionDAO sessionDAO) {
        this.sessionDAO = sessionDAO;
    }
}
