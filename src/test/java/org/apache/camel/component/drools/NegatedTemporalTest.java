/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import junit.framework.TestCase;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.time.impl.PseudoClockScheduler;

import java.io.Serializable;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

/**
 * @author mproch
 * 
 */
@SuppressWarnings("serial")
public class NegatedTemporalTest extends TestCase {

	int counter = 0;

	StatefulKnowledgeSession  ksession;

	PseudoClockScheduler clock;
	
	public void addItem() {
		counter++;
	}

	public void makeRules(String source) throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newReaderResource(new StringReader(source)),
				ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		KnowledgeBaseConfiguration config = KnowledgeBaseFactory
				.newKnowledgeBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		KnowledgeBase knowledgeBase = KnowledgeBaseFactory
				.newKnowledgeBase(config);
		knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		KnowledgeSessionConfiguration sessionConfig = KnowledgeBaseFactory
		.newKnowledgeSessionConfiguration();
		sessionConfig.setOption(ClockTypeOption.get("pseudo"));

		ksession = knowledgeBase
				.newStatefulKnowledgeSession(sessionConfig, null);
		ksession.setGlobal("s", this);
		clock = ksession.getSessionClock();
	}

	public void testDuringRule() throws Exception {
		makeRules(during);
		ksession.insert(new A());
		ksession.fireAllRules();
		assertEquals(0, counter);
		clock.advanceTime(65, TimeUnit.SECONDS);
		assertEquals(1, counter);
	}
	
	public void testAfterRule() throws Exception {
		makeRules(after);
		ksession.insert(new A());
		ksession.fireAllRules();
		assertEquals(0, counter);
		clock.advanceTime(55, TimeUnit.SECONDS);
		assertEquals(1, counter);
	}

	public class A implements Serializable {
		public long getDuration() {
			return 60L *1000;
		}
	}

	public class B implements Serializable {
        public long getDuration() {
			return 0;
		}
	}

	String common = "import org.apache.camel.component.drools.NegatedTemporalTest;\n"
	    + "import org.apache.camel.component.drools.NegatedTemporalTest.*;\n"
		+ "global NegatedTemporalTest s;\n" 
		+ "declare A\n"
		+ "	@role(event)\n" 
		+ "	@duration(duration)\n" 
		+ "end\n"
		+ "declare B\n" 
		+ "	@role(event)\n" 
		+ "	@expires(10m)\n"
        + "	@duration(duration)\n"
		+ "end\n"
		+ "rule test\n" 
		+ "when\n" 
		+ "	$a : A()\n"
		+ "	not B(this #pattern $a)\n" 
		+ "then\n" 
		+ "	s.addItem();\n" 
		+ "end";
	
	String after = common.replace("#pattern", "after [-*,-10s]");
	String during = common.replace("#pattern", "during");
	
}
