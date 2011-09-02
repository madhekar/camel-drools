package org.apache.camel.component.drools.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.camel.component.drools.impl.KnowledgeSessionConfiguration;
import org.apache.camel.component.drools.impl.SessionDataWrapper;
import org.apache.camel.component.drools.impl.SessionWithIdentifier;
import org.apache.camel.component.drools.persistence.DatabaseObjectWrapper;
import org.apache.camel.component.drools.persistence.DefaultMarshallingStrategyFactory;
import org.apache.camel.component.drools.persistence.PersistentSessionProvider;
import org.apache.camel.component.drools.persistence.TaskAssembler;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * author pjagielski
 */
public class MockSessionDAO implements SessionDAO {

    SessionWithIdentifier session;
    List<byte[]> sessionObjects = new ArrayList<byte[]>();
    final int sessionId = 1;
    boolean shouldCreate = true;
    
    public SessionWithIdentifier createOrLoadSession(MultiKey id, KnowledgeSessionConfiguration configuration) {
        if (session == null && shouldCreate) {
            DefaultMarshallingStrategyFactory factory = new DefaultMarshallingStrategyFactory();
            factory.setSessionDAO(this);
            factory.setAssembler(new TaskAssembler());
            StatefulKnowledgeSession ksession = PersistentSessionProvider.createNewSession(sessionId, this, configuration, factory);
            session = new SessionWithIdentifier(ksession, id);
            return session;
        }
        throw new RuntimeException("x");
    }
    
    public void clearSession() {
        this.session = null;
        this.shouldCreate = false;
    }
    
    public byte[] getSessionData(MultiKey id) {
        return new byte[0];
    }

    public Map<MultiKey,Boolean> loadSessions(long timeLimit) {
        return new HashMap<MultiKey,Boolean>();
    }

    public SessionWithIdentifier restoreSession(SessionDataWrapper data,
            KnowledgeSessionConfiguration configuration) {
        return session;
    }

    public DatabaseObjectWrapper loadObjectData(int dbId) {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] loadSessionData(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public int saveObjectData(DatabaseObjectWrapper obj, int sessionId) {
        return 0;
    }

    public void saveSessionData(int id, byte[] sessionData, Date nextTime) {
        // TODO Auto-generated method stub
        
    }

    public List<DatabaseObjectWrapper> loadObjectsForSession(MultiKey key) {
        return new ArrayList<DatabaseObjectWrapper>();
    }

    public void deleteObjectsData(int sessionId) {
        // TODO Auto-generated method stub
        
    }

    public int getSessionId(MultiKey key) {
        return sessionId;
    }

    public SessionWithIdentifier getSession() {
        return session;
    }

}

