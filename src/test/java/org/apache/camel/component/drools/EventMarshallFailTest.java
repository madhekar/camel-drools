/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.drools.marshalling.MarshallerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author mproch
 *
 */
@SuppressWarnings("serial")
public class EventMarshallFailTest extends TestCase  {

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

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MarshallerFactory.newMarshaller(knowledgeBase).marshall(out, ksession);
		
		ksession = MarshallerFactory.newMarshaller(knowledgeBase).unmarshall(new ByteArrayInputStream(out.toByteArray()));

		ksession.insert(new B());
		ksession.fireAllRules();
		assertEquals(3, ksession.getObjects().size());
	}
	
	public static class A implements Serializable {}
	public static class B implements Serializable {}
	public static class C implements Serializable {}
	
	public static void main(String[] args) throws Exception {
		EventMarshallFailTest f = new EventMarshallFailTest();
		f.setUp();
		System.out.println("setup");
		f.testMarshall();
		System.out.println("out");
		System.exit(0);
	}
	
	String str = 
		    "import org.apache.camel.component.drools.EventMarshallFailTest.*\n"+
		    "declare A\n" + 
		    " @role( event )\n" + 
		    " @expires( 10m )\n" + 
		    "end\n" + 
		    "declare B\n" + 
		    " @role( event )\n" + 
		    " @expires( 10m )\n" + 
		    "end\n" + 
		    "rule one\n" + 
			"when\n" + 
			"   $a : A()\n" + 
			"   B(this after $a)\n" + 
			"then\n" +
			"insert(new C());" + 
			"end\n"; 
}
