/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;

/**
 * @author mproch
 *
 */
public class SecondEventTest extends GenericTest {

	@Override
	protected void setUpResources(KnowledgeBuilder kbuilder) throws Exception {
		kbuilder.add(new ClassPathResource("secondEvent.drl"), ResourceType.DRL);
	}
	
	@Override
	public void setUpInternal() throws Exception {
		ksession.setGlobal("s", sentStuff);
	}

	public void testOne() throws Exception {
		insertAdvanceDays(new A("b"), 12);
		log.debug(sentStuff);
		insertAdvanceDays(new A("a"), 10);
		log.debug(sentStuff);
	}
	
}
