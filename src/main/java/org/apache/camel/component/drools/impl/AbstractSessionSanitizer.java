/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.drools.persistence.DatabaseObjectWrapper;
import org.apache.camel.component.drools.persistence.MarshallingStrategyFactory;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.ClockType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.marshalling.MarshallerFactory;
import org.drools.marshalling.ObjectMarshallingStrategy;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.time.impl.PseudoClockScheduler;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 17, 2010
 * Time: 5:12:45 PM

 */
public class AbstractSessionSanitizer {

    private KnowledgeBase knowledgeBase;

    private SessionSanitizerDataExtractor extractor;

    protected static Log log = LogFactory.getLog(AbstractSessionSanitizer.class);

    private SessionInitializer initializer;

    private MarshallingStrategyFactory marshallingStrategyFactory;

    public AbstractSessionSanitizer(KnowledgeSessionConfiguration knowledgeConfig, SessionSanitizerDataExtractor extractor, SessionInitializer initializer, 
            MarshallingStrategyFactory marshallingStrategyFactory) {
        this.knowledgeBase = knowledgeConfig.getKnowledgeBase();
        this.extractor = extractor;
        this.initializer = initializer;
        this.marshallingStrategyFactory = marshallingStrategyFactory;
   }

    private StatefulKnowledgeSession initSession(MultiKey id, int dbId) {
      SessionConfiguration config = new SessionConfiguration();
	  config.setClockType(ClockType.PSEUDO_CLOCK);
	  StatefulKnowledgeSession ret = knowledgeBase.newStatefulKnowledgeSession(config, KnowledgeBaseFactory.newEnvironment());
	  ((StatefulKnowledgeSessionImpl) ret).session.setId(dbId);
      initializer.initSession(new SessionWithIdentifier(ret, id), true);
      return ret;
    }

    public SessionDataWrapper migrateSession(MultiKey id, boolean withoutLast, int dbId) {
        StatefulKnowledgeSession ksession = null;
        try {
            log.debug("migrating session: "+id);
            ksession = initSession(id, dbId);
            insertEvents(id, ksession, withoutLast);
            //musimy jeszcze dociagnac do chwili obecnej - nawet troche pozniej - zeby aktualna regula sie odpalila
            Calendar cal = Calendar.getInstance();
            advanceTime(ksession, cal.getTimeInMillis());
            ksession.fireAllRules();
            log.debug("next scheduled: "+Utils.getDateOfNextScheduled(ksession));
            return prepareSessionDataWrapper(id, ksession);
        } catch (Exception e) {
            log.info("migrating session failed! ", e);
            if (ksession != null) {
                ksession.dispose();
            }
            return null;
        }
    }


    private SessionDataWrapper prepareSessionDataWrapper(MultiKey key, StatefulKnowledgeSession ksession) throws Exception {
       ByteArrayOutputStream out = new ByteArrayOutputStream();
	   MarshallerFactory.newMarshaller(knowledgeBase, new ObjectMarshallingStrategy[]{
	           marshallingStrategyFactory.createMarshallingStrategy(ksession.getId())})
	       .marshall(out, ksession);
       return new SessionDataWrapper(out.toByteArray(), Utils.getDateOfNextScheduled(ksession), key);
    }

    private void insertEvents(MultiKey id, StatefulKnowledgeSession ksession, boolean withoutLast) {
       List<DatabaseObjectWrapper> stuff = extractor.getEvents(id);
       Collections.sort(stuff);
       if (withoutLast && !extractor.alwaysWithLastEvent()) {
           stuff.remove(stuff.size()-1);
       }
       for (DatabaseObjectWrapper it : stuff) {
			advanceTime(ksession, it.getTimestamp());
			ksession.insert(extractor.toObject(it));
			ksession.fireAllRules();
		}
    }

    private void advanceTime(StatefulKnowledgeSession ksession, long targetTimeInMilis) {
        PseudoClockScheduler clock = (PseudoClockScheduler) ksession.getSessionClock();
        long delay = targetTimeInMilis - clock.getCurrentTime();
        log.debug("sessionClock: " + new Date(clock.getCurrentTime()) + ", advancing: " + delay);
        if (delay > 0) {
            clock.advanceTime(delay, TimeUnit.MILLISECONDS);
        }
    }

}
