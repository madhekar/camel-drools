package org.apache.camel.component.drools.persistence;

import java.util.Date;

import org.apache.camel.component.drools.impl.EventWithTime;

public class DatabaseObjectWrapper implements EventWithTime {

    private long sessionId;
    private long objectId;
    private String type;
    private Date timestamp;

    public DatabaseObjectWrapper(String type, Long objectId, Long sessionId, Date timestamp) {
        this.sessionId = sessionId;
        this.type = type;
        this.objectId = objectId;
        this.timestamp = timestamp;
    }
    
    public long getSessionId() {
        return sessionId;
    }

    public String getType() {
        return type;
    }

    public long getObjectId() {
        return objectId;
    }

    public long getTimestamp() {
        return timestamp.getTime();
    }

    public Date getTimestampAsDate() {
        return timestamp;
    }
    
    public int compareTo(EventWithTime o) {
        return new Long(getTimestamp()).compareTo(o.getTimestamp());
    }
    
    @Override
    public String toString() {
        return "DatabaseObjectWrapper[type:" + type + ", timestamp: " + timestamp + ", objectId: " + objectId + ", sessionId: " + sessionId + "]";
    }

}
