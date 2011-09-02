package org.apache.camel.component.drools.persistence;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.drools.marshalling.ObjectMarshallingStrategy;
import org.springframework.beans.factory.annotation.Required;

public class DefaultMarshallingStrategyFactory implements MarshallingStrategyFactory {

    protected SessionDAO sessionDAO;
    protected DatabaseObjectWrapperAssembler assembler;
    
    public ObjectMarshallingStrategy createMarshallingStrategy(int sessionId) {
        return new DefaultPersistentObjectMarshallingStrategy(sessionId, sessionDAO, assembler);
    }

    @Required
    public void setSessionDAO(SessionDAO sessionDAO) {
        this.sessionDAO = sessionDAO;
    }

    @Required
    public void setAssembler(DatabaseObjectWrapperAssembler assembler) {
        this.assembler = assembler;
    }


}
