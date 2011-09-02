package org.apache.camel.component.drools.stateful.model;

import java.io.Serializable;

/**
 * @author pjagielski
 */
public class Task implements Serializable {

    private static final long serialVersionUID = -2964477958089238715L;
    
    private String name;
    private boolean completed;

    public Task(String name) {
        this(name, false);
    }

    public Task(String name, boolean completed) {
        this.name = name;
        this.completed = completed;
    }
    
    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }
    
    public long getId() {
        return name.hashCode();
    }
    
    public long duration() {
        return 10000;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Task)) return false;
        Task t = (Task) obj;
        return t.getName().equals(name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return "Task[name = "+name+", completed = "+completed+"]";
    }
}
