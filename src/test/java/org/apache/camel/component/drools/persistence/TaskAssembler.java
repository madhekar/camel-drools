package org.apache.camel.component.drools.persistence;

import java.util.Date;

import org.apache.camel.component.drools.stateful.model.Task;

public class TaskAssembler implements DatabaseObjectWrapperAssembler {

    public DatabaseObjectWrapper fromObject(Object object, long sessionId) {
        if (object instanceof Task) {
            Task task = (Task) object;
            return new DatabaseObjectWrapper("Task", task.getId(), sessionId, new Date());
        }
        return new DatabaseObjectWrapper("other", 1L, sessionId, new Date());
    }

    public Object toObject(DatabaseObjectWrapper wrapper) {
        if (wrapper.getObjectId() == 1) {
            return new Task("Task-A");
        }
        return new Task("Task-B");
    }

}
