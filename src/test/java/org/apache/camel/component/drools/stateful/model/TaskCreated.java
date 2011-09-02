package org.apache.camel.component.drools.stateful.model;

import java.io.Serializable;

public class TaskCreated implements Serializable {

    private static final long serialVersionUID = 4574334200165750518L;
    
    protected String name;
    
    public TaskCreated(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
