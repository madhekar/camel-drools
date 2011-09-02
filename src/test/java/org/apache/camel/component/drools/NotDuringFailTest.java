/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import java.io.Serializable;
import java.io.StringReader;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author mproch
 * 
 */
@SuppressWarnings("serial")
public class NotDuringFailTest extends TestCase {

	int counter = 0;

	KnowledgeBase  knowledgeBase;

	public void addItem() {
		counter++;
	}

	public void setUp() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newReaderResource(new StringReader(str)),
				ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		KnowledgeBaseConfiguration config = KnowledgeBaseFactory
				.newKnowledgeBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		knowledgeBase = KnowledgeBaseFactory
				.newKnowledgeBase(config);
		knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
	}

	public void testDuringRule() throws Exception {

		StatefulKnowledgeSession ksession = knowledgeBase
				.newStatefulKnowledgeSession();
		ksession.setGlobal("s", this);
		ksession.insert(new A());
		assertEquals(0, counter);
	}

	public class A implements Serializable {
		public long getDuration() {
			return 3000;
		}
	}

	public class B implements Serializable {
	}

	String str = "import org.apache.camel.component.drools.NotDuringFailTest;\n"
		+ "import org.apache.camel.component.drools.NotDuringFailTest.*;\n"
		+ "global NotDuringFailTest s;\n" 
		+ "declare A\n"
		+ "	@role(event)\n" 
		+ "	@duration(duration)\n" 
		+ "end\n"
		+ "declare B\n" 
		+ "	@role(event)\n" 
		+ "	@expires(10m)\n" 
		+ "end\n"
		+ "rule test\n" 
		+ "when\n" 
		+ "	$a : A()\n"
		+ "	not B(this during $a)\n" 
		+ "then\n" 
		+ "	s.addItem();\n" 
		+ "end";
}
