/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import java.io.Serializable;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;


import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.marshalling.MarshallerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import junit.framework.TestCase;

/**
 * @author mproch
 *
 */
@SuppressWarnings("serial")
public class MarshallFailTest extends TestCase  {

	KnowledgeBase  knowledgeBase;

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
	
	public void testMarshall() throws Exception {
		StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();
		ksession.insert(new A());
		MarshallerFactory.newMarshaller(knowledgeBase).marshall(new ByteArrayOutputStream(), ksession);
	}
	
	public static class A implements Serializable {}
	public static class B implements Serializable {}
	
	String str = 
		    "import org.apache.camel.component.drools.MarshallFailTest.*\n"+
			"rule one\n" + 
			"when\n" + 
			"   A()\n" + 
			"   not(B())\n" + 
			"then\n" + 
			"System.out.println(\"a\");\n" + 
			"end\n" + 
			"\n" + 
			"rule two\n" + 
			"when\n" + 
			"   B()\n" + 
			"then\n" + 
			"System.out.println(\"b\");\n" + 
			"end\n"; 
}
