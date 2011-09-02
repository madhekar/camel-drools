package org.apache.camel.component.drools;

import java.util.List;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.drools.utils.ResourceWrapper;
import org.apache.camel.impl.DefaultEndpoint;
import org.drools.io.Resource;

/**
 * @author mproch
 *
 */
@SuppressWarnings("unchecked")
public class DroolsEndpoint extends DefaultEndpoint {

    private boolean stateful;
    private List<ResourceWrapper> resources;
    private Properties props;

    public DroolsEndpoint(String endpointUri, List<ResourceWrapper> resources, Properties props, CamelContext camelContext) {
        super(endpointUri, camelContext);
        this.resources = resources;
        this.props = props;
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new Exception("Cannot create consumer drools endpoints");
    }

    public Producer createProducer() throws Exception {
        if (stateful) {
            return new StatefulDroolsProducer(this, getCamelContext(), resources);
        } else {
            return new StatelessDroolsProducer(this, getCamelContext(), resources, props);
        }
    }

    public boolean isSingleton() {
        return true;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    public boolean isStateful() {
        return stateful;
    }

    
}
