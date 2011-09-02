/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.camel.component.drools.persistence.MarshallingStrategyFactory;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 12:33:57 PM
 */
public class SessionMakerImpl implements SessionMaker, InitializingBean {

    protected static Log log = LogFactory.getLog(SessionMakerImpl.class);

    private List<SessionSanitizerDataExtractor> sessionSanitizerExtractors = new ArrayList<SessionSanitizerDataExtractor>();

    private List<AbstractSessionSanitizer> sessionSanitizers = new ArrayList<AbstractSessionSanitizer>();

    private SessionInitializer initializer;

    private KnowledgeSessionConfiguration sessionConfiguration;

    private SessionDAO sessionDAO;
    
    private MarshallingStrategyFactory marshallingStrategyFactory;

    public void afterPropertiesSet() {
        sessionSanitizers.clear();
        for (SessionSanitizerDataExtractor extractor : sessionSanitizerExtractors) {
            sessionSanitizers.add(new AbstractSessionSanitizer(sessionConfiguration, extractor, initializer, marshallingStrategyFactory));
        }
     }

    public SessionWithIdentifier makeSession(MultiKey id, Object trigger) {
        SessionWithIdentifier session;
        try {
           session = createOrLoadSession(id, trigger);
        } catch (Exception e) {
           log.info("failed to load session: " + id);
    	   session = restoreSession(id, trigger != null);
        }
        initializer.initSession(session, false);
        return session;
    }

    private SessionWithIdentifier restoreSession(MultiKey id, boolean isTriggered) {
    	for (AbstractSessionSanitizer sanitizer : sessionSanitizers) {
    	    int dbId = sessionDAO.getSessionId(id);
            SessionDataWrapper restoredSession = sanitizer.migrateSession(id, isTriggered, dbId);
            if (restoredSession != null) {
                return sessionDAO.restoreSession(restoredSession, sessionConfiguration);
            }
        }
        throw new RuntimeException("failed to restore session");
    }

    private SessionWithIdentifier createOrLoadSession(MultiKey id, Object trigger) {
           SessionWithIdentifier sessionWithId = sessionDAO.createOrLoadSession(id, sessionConfiguration);
           if (trigger != null && initializer.shouldInitializeNewSession(sessionWithId, trigger)) {
              throw new RuntimeException("Initializer assessed session as invalid");
           }
           return sessionWithId;
    }

    /* settery */
    public void setSessionSanitizers(List<SessionSanitizerDataExtractor> sessionSanitizers) {
        this.sessionSanitizerExtractors = sessionSanitizers;
    }

    @Required
    public void setSessionConfiguration(KnowledgeSessionConfiguration sessionConfiguration) {
        this.sessionConfiguration = sessionConfiguration;
    }

    @Required
    public void setInitializer(SessionInitializer initializer) {
        this.initializer = initializer;
    }

    @Required
    public void setSessionDAO(SessionDAO sessionDAO) {
        this.sessionDAO = sessionDAO;
    }

    @Required
    public void setMarshallingStrategyFactory(
            MarshallingStrategyFactory marshallingStrategyFactory) {
        this.marshallingStrategyFactory = marshallingStrategyFactory;
    }
}
