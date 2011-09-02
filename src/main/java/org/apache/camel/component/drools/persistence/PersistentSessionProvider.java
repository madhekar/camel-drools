/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.camel.component.drools.impl.KnowledgeSessionConfiguration;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.command.CommandService;
import org.drools.command.Context;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.UpdateCommand;
import org.drools.impl.EnvironmentFactory;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.marshalling.Marshaller;
import org.drools.marshalling.MarshallerFactory;
import org.drools.marshalling.ObjectMarshallingStrategy;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 25, 2010
 * Time: 2:22:47 PM
 */
public class PersistentSessionProvider implements CommandService {

    private StatefulKnowledgeSession ksession;
    private KnowledgeCommandContext kContext;
    private Marshaller marshaller;
    private SessionDAO sessionDao;
    int id;
    
    protected static Log log = LogFactory.getLog(PersistentSessionProvider.class);

    public static StatefulKnowledgeSession createNewSession(int id, SessionDAO sessionDao, KnowledgeSessionConfiguration configuration,
            MarshallingStrategyFactory marshallingStrategyFactory) {
        log.debug("creating session " + id);
        PersistentSessionProvider ret = new PersistentSessionProvider(sessionDao, configuration, marshallingStrategyFactory, id);
        ret.ksession = configuration.getKnowledgeBase().newStatefulKnowledgeSession(configuration.getConfig(), EnvironmentFactory.newEnvironment());
        ret.marshallSession();
        return createSession(ret);
    }

    public static StatefulKnowledgeSession loadSession(int id, SessionDAO sessionDao, KnowledgeSessionConfiguration configuration, 
            MarshallingStrategyFactory marshallingStrategyFactory) {
        log.debug("loading session " + id);
        PersistentSessionProvider ret = new PersistentSessionProvider(sessionDao, configuration, marshallingStrategyFactory, id);
        ret.ksession = ret.unMarshallSession(id, configuration);
        ret.setId(id);
        return createSession(ret);
    }

    private static StatefulKnowledgeSession createSession(PersistentSessionProvider provider) {
       provider.kContext = new KnowledgeCommandContext(new ContextImpl("ksession", null), null, null, provider.ksession, null );
       return new CommandBasedStatefulKnowledgeSession(provider);
    }

    private PersistentSessionProvider(SessionDAO sessionDao, KnowledgeSessionConfiguration configuration, MarshallingStrategyFactory marshallingStrategyFactory, int id) {
        this.marshaller = MarshallerFactory.newMarshaller(configuration.getKnowledgeBase(), 
                new ObjectMarshallingStrategy[]{marshallingStrategyFactory.createMarshallingStrategy(id)});
        this.sessionDao = sessionDao;
        this.id = id;

    }

    public <T> T execute(GenericCommand<T> command) {
        T res =  command.execute(getContext());
        if (command instanceof InsertObjectCommand || 
            command instanceof UpdateCommand ||
            command instanceof FireAllRulesCommand) 
        {
            log.debug("command: " + command +", marshalling session: " + id);
            marshallSession();            
        }
        return res;
    }

    public Context getContext() {
        return kContext;  
    }

    private void setId(int id) {
        //co za rzez ;)
        ((StatefulKnowledgeSessionImpl) ksession).session.setId(id);
    }

    private void marshallSession() {
        try {
            setId(id);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //FIXME: transakcyjnosc?
            marshaller.marshall(out, ksession);
            sessionDao.saveSessionData(ksession.getId(), out.toByteArray(), Utils.getDateOfNextScheduled(ksession));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StatefulKnowledgeSession unMarshallSession(int id, KnowledgeSessionConfiguration configuration) {
        try {
            return marshaller.unmarshall(new ByteArrayInputStream(sessionDao.loadSessionData(id)), configuration.getConfig(), EnvironmentFactory.newEnvironment());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
