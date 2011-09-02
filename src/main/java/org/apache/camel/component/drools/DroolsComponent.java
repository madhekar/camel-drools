/**
 * 
 */
package org.apache.camel.component.drools;

import java.util.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.drools.impl.SessionCache;
import org.apache.camel.component.drools.utils.ResourceWrapper;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.camel.impl.DefaultComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.builder.ResourceType;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author mproch
 * @author pjagielski
 * 
 */
@SuppressWarnings("unchecked")
public class DroolsComponent extends DefaultComponent<Exchange> {

    protected SessionCache cache;
    protected long loadSessionsPeriod = 10000;
    protected Log log = LogFactory.getLog(DroolsComponent.class);
    protected Timer timer;
    protected ResourceLoader resourceLoader = new DefaultResourceLoader();

    public DroolsComponent(CamelContext context) {
        super(context);
        cache = context.getRegistry()
                .lookup("sessionCache", SessionCache.class);
    }

    public void configure() {
        timer = new Timer("camel-drools:loadSessions", true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    cache.loadSessions();
                } catch (Exception e) {
                    log.info("Failed to load sessions", e);
                }
            }
        };
        timer.scheduleAtFixedRate(task, 10000, loadSessionsPeriod);

    }

    @Override
    protected Endpoint<Exchange> createEndpoint(String uri, String resource,
            Map parameters) throws Exception {

        Properties properties = new Properties();
        properties.putAll(parameters);

        List<ResourceWrapper> resources = new ArrayList<ResourceWrapper>();

        Boolean stateful = getAndRemoveParameter(parameters, "stateful", Boolean.class, Boolean.FALSE);
        String resourceFactoryRef = getAndRemoveParameter(parameters, "resourceFactory", String.class, null);
        if (resourceFactoryRef != null) {
            resources = (List<ResourceWrapper>) getCamelContext().getRegistry().lookup(resourceFactoryRef);
        } else {
            if (!resource.startsWith("/")) {
                resource = "/" + resource;
            }
            resources.add(new ResourceWrapper(Utils.makeResource(resource, new Properties()), ResourceType.DRL));
        }

        DroolsEndpoint endpoint = new DroolsEndpoint(uri, resources, properties, getCamelContext());
        endpoint.setStateful(stateful);
        return endpoint;
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        timer.cancel();
    }

}
