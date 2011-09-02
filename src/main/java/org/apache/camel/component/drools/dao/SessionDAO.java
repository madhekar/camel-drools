/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.camel.component.drools.impl.KnowledgeSessionConfiguration;
import org.apache.camel.component.drools.impl.SessionDataWrapper;
import org.apache.camel.component.drools.impl.SessionWithIdentifier;
import org.apache.camel.component.drools.persistence.DatabaseObjectWrapper;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 * @author mproch
 * @author pjagielski
 */

public interface SessionDAO {
    
    int getSessionId(MultiKey key);

    SessionWithIdentifier createOrLoadSession(MultiKey key, KnowledgeSessionConfiguration configuration);

    SessionWithIdentifier restoreSession(SessionDataWrapper data, KnowledgeSessionConfiguration configuration);

    Map<MultiKey,Boolean> loadSessions(long timeLimit);
    
    byte[] getSessionData(MultiKey key);
	
    void saveSessionData(int id, byte[] sessionData, Date nextTime);
    
    byte[] loadSessionData(int id);
    
    int saveObjectData(DatabaseObjectWrapper wrapper, int sessionId);
    
    DatabaseObjectWrapper loadObjectData(int dbId);
    
    List<DatabaseObjectWrapper> loadObjectsForSession(MultiKey key);

}
