package org.apache.camel.component.drools.persistence;

public interface DatabaseObjectWrapperAssembler {
    
    DatabaseObjectWrapper fromObject(Object object, long sessionId);
    
    Object toObject(DatabaseObjectWrapper wrapper);

}
