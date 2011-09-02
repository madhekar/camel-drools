/*
 * Copyright (c) 2009 TouK.pl
 */
package org.apache.camel.component.drools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.conf.AssertBehaviorOption;
import org.drools.conf.EventProcessingOption;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.marshalling.Marshaller;
import org.drools.marshalling.MarshallerFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.time.SessionClock;
import org.drools.time.impl.PseudoClockScheduler;

/**
 * @author mproch
 * 
 */
public abstract class GenericTest extends TestCase {

    protected static Log log = LogFactory.getLog(GenericTest.class);

	protected KnowledgeBase knowledgeBase;

	protected StatefulKnowledgeSessionImpl ksession;

	protected PseudoClockScheduler pseudoClock;

	protected final List<String> sentStuff = new ArrayList<String>();

	protected KnowledgeSessionConfiguration sessionConfig;
	
	public final void setUp() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		setUpResources(kbuilder);

		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		KnowledgeBaseConfiguration config = KnowledgeBaseFactory
				.newKnowledgeBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
        config.setOption(AssertBehaviorOption.EQUALITY);

		sessionConfig = KnowledgeBaseFactory
				.newKnowledgeSessionConfiguration();
		sessionConfig.setOption(ClockTypeOption.get("pseudo"));

		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(config);
		knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		ksession = (StatefulKnowledgeSessionImpl) knowledgeBase
				.newStatefulKnowledgeSession(sessionConfig,
						KnowledgeBaseFactory.newEnvironment());
		pseudoClock = ksession.getSessionClock();
		setUpInternal();
	}

	protected abstract void setUpResources(KnowledgeBuilder kbuilder)
			throws Exception;

	public void setUpInternal() throws Exception {

	}

	protected void insertAdvance(Object o, int amount, TimeUnit u) {
		ksession.insert(o);
		while (ksession.fireAllRules() > 0) {
		}
		pseudoClock.advanceTime(amount, u);
		while (ksession.fireAllRules() > 0) {
		}
	}

	protected void insertAdvanceDays(Object o, int amount) {
		insertAdvance(o, amount * 3600 * 24, TimeUnit.SECONDS);
	}

	protected void advanceDays(long days) {
		pseudoClock.advanceTime(3600 * 24 * days, TimeUnit.SECONDS);
	}

	protected Date advanceSeconds(long secs) {
		return new Date(getDate().getTime() + (1000 * secs));
	}

	protected Date getDate() {
		SessionClock cl = ksession.getSessionClock();
		return new Date(cl.getCurrentTime());
	}

	protected Date returnAdvanceDays(long days) {
		return advanceSeconds(days * 3600 * 24);
	}

	protected void assertContains(int count) {
		assertEquals("" + sentStuff, count, sentStuff.size());
	}

	protected void assertContains(String what) {
		assertContains(what, 1);
	}

	protected void assertContains(String what, int count) {
		int c = 0;
		for (String cont : sentStuff) {
			if (what.equals(cont)) {
				c++;
			}
		}
		if (count != c) {
			throw new AssertionFailedError(what + " found " + c + " instead of "
					+ count + ", there are: " + sentStuff);
		}
	}

	protected void assertNotContains(String what) {
		assertContains(what, 0);
	}
	
	protected void simulateRestart() throws Exception {
		long time = pseudoClock.getCurrentTime();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Marshaller marsh = MarshallerFactory.newMarshaller(knowledgeBase);
		marsh.marshall(out, ksession);
		ksession.dispose();
		ksession = (StatefulKnowledgeSessionImpl) knowledgeBase.newStatefulKnowledgeSession(sessionConfig, null);
		setUpInternal();
		pseudoClock = ksession.getSessionClock();
		pseudoClock.advanceTime(time, TimeUnit.MILLISECONDS);
		marsh.unmarshall(new ByteArrayInputStream(out.toByteArray()), ksession);
	}

}
