package org.apache.camel.component.drools.persistence;

import org.drools.marshalling.ObjectMarshallingStrategy;

public interface MarshallingStrategyFactory {
    
    public ObjectMarshallingStrategy createMarshallingStrategy(int sessionId);

}
