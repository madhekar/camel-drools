/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.camel.component.drools.persistence.DatabaseObjectWrapper;
import org.apache.camel.component.drools.persistence.DatabaseObjectWrapperAssembler;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.drools.marshalling.impl.PersisterEnums;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author mproch
 * @author pjagielski
 * 
 */
@SuppressWarnings("unused")
public class SessionSanitizer implements SessionSanitizerDataExtractor {

    SessionDAO sessionDAO;
    DatabaseObjectWrapperAssembler assembler;

    public boolean alwaysWithLastEvent() {
        return true;  
    }

    public List<DatabaseObjectWrapper> getEvents(MultiKey key) {
        return sessionDAO.loadObjectsForSession(key);
    }

    public Object toObject(DatabaseObjectWrapper wrapper) {
        return assembler.toObject(wrapper);
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
