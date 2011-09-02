package org.apache.camel.component.drools.stateful.model;

import java.io.Serializable;

public class State implements Serializable {

    private static final long serialVersionUID = -2432512433530979423L;
    private String name;
    
    public State(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public long getId() {
        return name.hashCode();
    }

}