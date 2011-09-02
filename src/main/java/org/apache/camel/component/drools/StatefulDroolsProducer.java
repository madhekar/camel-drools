/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.drools.impl.DefaultSessionInitializer;
import org.apache.camel.component.drools.impl.KnowledgeSessionConfiguration;
import org.apache.camel.component.drools.impl.SessionCache;
import org.apache.camel.component.drools.impl.SessionMakerImpl;
import org.apache.camel.component.drools.utils.ResourceWrapper;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

/**
 * @author mproch
 *
 * Main task of this class is to provide:
 * - ability to insert object with given identifier
 * - mechanism for polling for sessions which need to be loaded into cache
 * Creating objects/keys from camel messages is delegated to subclasses.
 * Session management is delegated to SessionCache
 */
@SuppressWarnings("unchecked")
public class StatefulDroolsProducer extends DefaultProducer {

    protected CamelContext ctx;
    protected SessionCache cache;

    protected static Log log = LogFactory.getLog(StatefulDroolsProducer.class);

    public StatefulDroolsProducer(Endpoint endpoint, CamelContext ctx, List<ResourceWrapper> resources) {
        super(endpoint);
        init(ctx, resources);
    }

    public void process(Exchange exchange) throws Exception {
        MultiKey key = getKey(exchange);
        Object ev = getEvent(exchange);
        process(key, ev, exchange);
    }
    
    protected void process(MultiKey key, Object ev, Exchange exchange) throws Exception {
    	StatefulKnowledgeSession ksession = cache.getSession(key, ev);
        ksession.setGlobal("helper", new CamelDroolsHelper(ctx, exchange));

        //ksession NIE jest threadsafe!
        synchronized (ksession) {
            FactHandle fact = ksession.getFactHandle(ev);
            if (fact != null) {
                ksession.update(fact, ev);
                log.debug("updated: " + ev + " to next job: "+ Utils.getDateOfNextScheduled(ksession));
            } else {
                ksession.insert(ev);
                log.debug("inserted: " + ev + " to next job: "+ Utils.getDateOfNextScheduled(ksession));
            }
            cache.updateSession(ksession);
        }
    }


    protected Object getEvent(Exchange exchange) {
        return exchange.getIn().getBody();
    }

    protected MultiKey getKey(Exchange exchange) {
        MultiKey key = exchange.getIn().getHeader("drools.key", MultiKey.class);
        if (key == null) {
            throw new IllegalArgumentException("drools.key property not provided");
        }
        return key;
    }

    public void init(CamelContext camelContext, List<ResourceWrapper> resources) {
        log.info("Initializing StatefulDroolsProducer");
        ctx = camelContext;
        cache = (SessionCache) ctx.getRegistry().lookup("sessionCache");
        KnowledgeSessionConfiguration conf = 
            ctx.getRegistry().lookup("sessionConfiguration", KnowledgeSessionConfiguration.class);;
        conf.setResources(resources);
        conf.afterPropertiesSet();   
        DefaultSessionInitializer initializer = ctx.getRegistry().lookup("sessionInitializer", DefaultSessionInitializer.class);
        initializer.setCamelContext(ctx);
        SessionMakerImpl maker = ctx.getRegistry().lookup("sessionMaker", SessionMakerImpl.class);
        maker.afterPropertiesSet();
    }

    public void setSessionCache(SessionCache cache) {
        this.cache = cache;
    }

}
