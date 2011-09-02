package org.apache.camel.component.drools;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.drools.utils.ResourceWrapper;
import org.apache.camel.impl.DefaultProducer;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author mproch
 *
 */
@SuppressWarnings("unchecked")
public class StatelessDroolsProducer extends DefaultProducer {

	KnowledgeBase knowledgeBase;
	
	KnowledgeSessionConfiguration configuration;

    CamelContext ctx;
	
	public StatelessDroolsProducer(Endpoint endpoint, CamelContext ctx, List<ResourceWrapper> resources, Properties props) {
		super(endpoint);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (ResourceWrapper rw : resources) {
            kbuilder.add(rw.resource, rw.resourceType);
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }
        KnowledgeBaseConfiguration config = KnowledgeBaseFactory
                  .newKnowledgeBaseConfiguration();
        this.knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(config);
        knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		this.configuration = new SessionConfiguration(props);
		this.ctx = ctx;
	}

	public void process(Exchange exchange) throws Exception {
        Object in = exchange.getIn().getBody();
        StatefulKnowledgeSession session = knowledgeBase.newStatefulKnowledgeSession(
                configuration, KnowledgeBaseFactory.newEnvironment());
        session.setGlobal("helper", new CamelDroolsHelper(ctx, exchange));
        if (in instanceof Collection) {
            for (Object o : (Collection) in) {
                session.insert(o);
            }
        } else {
            session.insert(in);
        }
        session.fireAllRules();
	}

}
