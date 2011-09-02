/**
 * 
 */
package org.apache.camel.component.drools;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.ClockType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.marshalling.Marshaller;
import org.drools.marshalling.MarshallerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.time.impl.PseudoClockScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mproch
 * 
 */
public class LongRunTest {

	KnowledgeBase knowledgeBase;
	SessionConfiguration config;
	StatefulKnowledgeSession ksession;
	PseudoClockScheduler scheduler;
	List<String> list;
	Marshaller marshaller;

	@Before
	public void set() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("longRun.drl"),
				ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		KnowledgeBaseConfiguration kconfig = KnowledgeBaseFactory
				.newKnowledgeBaseConfiguration();
		kconfig.setOption(EventProcessingOption.STREAM);
		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(kconfig);
		knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		config = new SessionConfiguration();
		config.setClockType(ClockType.PSEUDO_CLOCK);
		ksession = knowledgeBase.newStatefulKnowledgeSession(config,
				KnowledgeBaseFactory.newEnvironment());
		scheduler = ksession.getSessionClock();
		list = new ArrayList<String>();
		ksession.setGlobal("list", list);
		marshaller = MarshallerFactory.newMarshaller(knowledgeBase);
	}

	@Test
	public void test1() {
		ksession.insert(new A("a"));
		ksession.fireAllRules();
		scheduler.advanceTime(600, TimeUnit.SECONDS);
		assertEquals(1, ksession.getFactHandles().size());
		assertEquals(1, list.size());
	}

	@Test
	public void test2() {
		ksession.insert(new A("a"));
		ksession.fireAllRules();
		scheduler.advanceTime(200, TimeUnit.SECONDS);
		assertEquals(0, list.size());
		readWrite();
		scheduler.advanceTime(200, TimeUnit.SECONDS);
		assertEquals(1, ksession.getFactHandles().size());
		assertEquals(0, list.size());
		scheduler.advanceTime(100, TimeUnit.SECONDS);
		assertEquals(1, list.size());
	}
	
	@Test
	public void test2a() {
		scheduler.advanceTime(20, TimeUnit.SECONDS);
		readWrite();
		ksession.insert(new A("a"));
		ksession.fireAllRules();
		assertEquals(0, list.size());
		scheduler.advanceTime(20, TimeUnit.SECONDS);
		assertEquals(1, ksession.getFactHandles().size());
		assertEquals(0, list.size());
		scheduler.advanceTime(700, TimeUnit.SECONDS);
		assertEquals(1, list.size());
	}


	@Test
	public void test3() {
		ksession.insert(new A("a"));
		ksession.fireAllRules();
		scheduler.advanceTime(600, TimeUnit.SECONDS);
		assertEquals(1, list.size());
		readWrite();
		assertEquals(1, list.size());
		assertEquals(1, ksession.getFactHandles().size());
		scheduler.advanceTime(400, TimeUnit.SECONDS);
		assertEquals(1, list.size());
	}
	
	@Test
	public void testTwo() {
		ksession.insert(new A("a"));
		ksession.fireAllRules();
		scheduler.advanceTime(60, TimeUnit.SECONDS);
		assertEquals(0, list.size());
	}
	
   @Test
    public void testBnoA() {
        ksession.insert(new B("abc"));
        ksession.fireAllRules();
        scheduler.advanceTime(99, TimeUnit.SECONDS);
        assertEquals(0, list.size());
        scheduler.advanceTime(1000, TimeUnit.SECONDS);
        assertEquals(1, list.size());
    }

	@After
	public void after() {
	}

	private void readWrite() {
		try {
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			marshaller.marshall(o, ksession);
			ksession = marshaller.unmarshall(new ByteArrayInputStream(o
					.toByteArray()), config, KnowledgeBaseFactory
					.newEnvironment());
			ksession.setGlobal("list", list);
			ksession.fireAllRules();
			scheduler = ksession.getSessionClock();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
