/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.component.drools.impl.KnowledgeSessionConfiguration;
import org.apache.camel.component.drools.impl.SessionDataWrapper;
import org.apache.camel.component.drools.impl.SessionWithIdentifier;
import org.apache.camel.component.drools.persistence.DatabaseObjectWrapper;
import org.apache.camel.component.drools.persistence.MarshallingStrategyFactory;
import org.apache.camel.component.drools.persistence.PersistentSessionProvider;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author mproch
 * @author pjagielski
 * 
 * Date: Feb 25, 2010
 * Time: 2:59:57 PM
 */

@SuppressWarnings("unchecked")
public class JdbcSessionDAO extends JdbcDaoSupport implements SessionDAO {

    String sequenceName;

    List<String> keyColumns;

	String sessionTable;

    String objectTable;
    
    String discriminatorValue;
    
    MarshallingStrategyFactory marshallingStrategyFactory;

    static final int NEW_SESSION_ID = -1;

    public SessionWithIdentifier createOrLoadSession(MultiKey id, KnowledgeSessionConfiguration configuration) {
        int dbId = getSessionId(id);
        StatefulKnowledgeSession session;
        if (isNewSession(dbId)) {
            session = newSession(id, configuration);
         } else {
            session = loadSession(dbId, configuration);
        }
        return new SessionWithIdentifier(session, id);
    }

    public SessionWithIdentifier restoreSession(SessionDataWrapper data, KnowledgeSessionConfiguration configuration) {
        int dbId = getSessionId(data.id);
        saveSessionData(dbId, data.data, data.nextTimer);
        return new SessionWithIdentifier(loadSession(dbId, configuration), data.id);
    }

    private StatefulKnowledgeSession newSession(MultiKey key, KnowledgeSessionConfiguration configuration) {
        int dbId = getNextSequenceValue();
        getJdbcTemplate().update("insert into "+sessionTable+" (id, dirty, startdate, discriminator) values (?, 0, sysdate, ?)", 
                new Object[]{dbId, discriminatorValue});
        setSessionKey(dbId, key);
        return PersistentSessionProvider.createNewSession(dbId, this, configuration, marshallingStrategyFactory);
    }

    private StatefulKnowledgeSession loadSession(int id, KnowledgeSessionConfiguration configuration) {
        return PersistentSessionProvider.loadSession(id, this, configuration, marshallingStrategyFactory);
    }

	private void setSessionKey(int dbId, MultiKey key) {
        Object[] k = new Object[key.size()+1];
		for (int i=0;i<key.size();k[i] = key.getKey(i++));
		k[key.size()] = dbId;
		getJdbcTemplate().update("update "+sessionTable+" set "+getKeyCondition(true)+" where id = ?", k);
	}
	
    public int getSessionId(MultiKey key) {
        return getJdbcTemplate().queryForInt("select nvl(max(id), -1) from "+sessionTable+" where "
            +getKeyCondition(false) + " and discriminator = ?", makeParamsForSessionId(key));
    }

    private Object[] makeParamsForSessionId(MultiKey key) {
        Object[] keys = key.getKeys();
        Object[] params = new Object[keys.length+1];
        for (int i = 0; i < keys.length; i++) {
            params[i] = keys[i];
        }
        params[keys.length] = discriminatorValue;
        return params;
    }
	
    public void saveSessionData(int id, byte[] sessionData, Date nextTime) {
         getJdbcTemplate().update("update "+sessionTable+" set dirty = ?, rulesbytearray = ?, next_timer = ?, lastmodificationdate = sysdate  where id = ?" ,
                 new Object[]{0, sessionData, nextTime, id});
    }

    public byte[] loadSessionData(int id) {
        return (byte[]) getJdbcTemplate().queryForObject("select rulesbytearray from "+sessionTable+" where id = ? ", new Object[]{id}, byte[].class);
    }

    public Map<MultiKey,Boolean> loadSessions(long timeLimit) {
        final Map<MultiKey, Boolean> ret = new HashMap<MultiKey, Boolean>();
        getJdbcTemplate().query("select "+getKeyQuery()+", id, dirty from "+sessionTable+" where next_timer < ? and discriminator = ?",
                new Object[]{new Date(new Date().getTime()+timeLimit), discriminatorValue}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                String[] key = new String[keyColumns.size()];
                for (int i=0; i<keyColumns.size(); i++) {
                    key[i] = rs.getString(keyColumns.get(i));
                }
                ret.put(new MultiKey(key), 1 == rs.getInt("dirty"));
            }
        });
        return ret;
    }

    public byte[] getSessionData(MultiKey id) {
        int dbId = getSessionId(id);
        if (isNewSession(dbId)) {
            return null;
        }
		return loadSessionData(dbId);
	}
    
    public int saveObjectData(DatabaseObjectWrapper wrapper, int sessionId) {
        List existing = getJdbcTemplate().queryForList("select id from "+objectTable+" where session_id = ? and object_id = ? and type = ?", 
            new Object[]{sessionId, wrapper.getObjectId(), wrapper.getType()}); 
        if (existing.size() > 0) {
            // existing object - just ignore
            return ((BigDecimal)((Map) existing.get(0)).get("ID")).intValue();
        }
        int dbId = getNextSequenceValue();
        getJdbcTemplate().update("insert into "+objectTable+" (id, session_id, object_id, type, timestamp) values (?, ?, ?, ?, ?)", 
                new Object[]{dbId, sessionId, wrapper.getObjectId(), wrapper.getType(), wrapper.getTimestampAsDate()});
        return dbId;
    }
    
    public DatabaseObjectWrapper loadObjectData(int dbId) {
        return (DatabaseObjectWrapper) getJdbcTemplate().queryForObject("select * from "+objectTable+" where id = ? ", new Object[]{dbId},
                new DatabaseObjectWrapperRowMapper());
    }

    public List<DatabaseObjectWrapper> loadObjectsForSession(MultiKey key) {
        int sessionId = getSessionId(key);
        return (List<DatabaseObjectWrapper>) getJdbcTemplate().query("select * from "+objectTable+" where session_id = ? ", new Object[]{sessionId}, 
                new DatabaseObjectWrapperRowMapper());
    }
    
    protected class DatabaseObjectWrapperRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int index) throws SQLException {
            return new DatabaseObjectWrapper(rs.getString("TYPE"), rs.getLong("OBJECT_ID"), rs.getLong("SESSION_ID"), rs.getTimestamp("TIMESTAMP"));
        }
    }

    boolean isNewSession(int id) {
        return id == NEW_SESSION_ID;
    }
    
    protected int getNextSequenceValue() {
        return getJdbcTemplate().queryForInt("select "+sequenceName+".nextval from dual");
    }

    String getKeyQuery() {
		StringBuffer b = new StringBuffer();
		for (String key : keyColumns) {
			b.append(b.length() == 0 ? " " : ", ");
            b.append(key);
		}
		return b.toString();
	}

	String getKeyCondition(boolean forUpdate) {
		StringBuffer b = new StringBuffer();
		for (String key : keyColumns) {
			b.append((b.length() == 0 ? " " : forUpdate ? ", " : " and ")+key+" = ? ");
		}
		return b.toString();
	}

    @Required
	public void setKeyColumns(List<String> keyColumns) {
		this.keyColumns = keyColumns;
	}

    @Required
	public void setSessionTable(String table) {
		this.sessionTable = table;
	}

    @Required
    public void setObjectTable(String objectTable) {
        this.objectTable = objectTable;
    }

    @Required
    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }
    
    @Required
    public void setDiscriminatorValue(String discriminatorValue) {
        this.discriminatorValue = discriminatorValue;
    }

    @Required
    public void setMarshallingStrategyFactory(
            MarshallingStrategyFactory marshallingStrategyFactory) {
        this.marshallingStrategyFactory = marshallingStrategyFactory;
    }


}
