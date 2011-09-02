/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.utils;

import org.apache.commons.io.IOUtils;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.common.InternalAgenda;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.impl.ReaderResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.Activation;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 11:38:54 AM
 *
 */
public class Utils {

    public static String parseResource(String resourcePath, Properties properties) {
        try {
            String r = IOUtils.toString(Utils.class.getResourceAsStream(resourcePath));
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                r = r.replace("${" + e.getKey() + "}", e.getValue().toString());
            }
            return r;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static org.drools.io.Resource makeResource(String resource) {
        return makeResource(resource, null);
    }

    public static org.drools.io.Resource makeResource(String resource, Properties properties) {
        return new ReaderResource(new StringReader(Utils.parseResource(resource, properties)));
    }

    public static StatefulKnowledgeSessionImpl convertToStatefulKnowledgeSessionImpl(StatefulKnowledgeSession ksession) {
    	if (ksession instanceof StatefulKnowledgeSessionImpl) {
    		return (StatefulKnowledgeSessionImpl) ksession;
    	}
    	if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
    	return (StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext())
                .getStatefulKnowledgesession();
    	}
    	return null;
    }

    public static Date getDateOfNextScheduled(StatefulKnowledgeSession ksession) {
        return new Date(new Date().getTime() + getTimeToNextScheduled(ksession));
    }

    public static long getTimeToNextScheduled(StatefulKnowledgeSession ksession) {
        StatefulKnowledgeSessionImpl impl = convertToStatefulKnowledgeSessionImpl(ksession);
        return impl == null ? -1 : impl.session.getTimerService().getTimeToNextJob();
    }

     public static String printObjects(StatefulKnowledgeSession session) {
        StringBuffer b = new StringBuffer();
        for (Object o : session.getObjects()) {
            b.append(o);
        }
        if (!session.getObjects().isEmpty()) {
            b.append("\n");
        }
        StatefulKnowledgeSessionImpl sess = convertToStatefulKnowledgeSessionImpl(session);
        if (sess != null) {
            b.append("activations: \n");
        	for (Activation a : ((InternalAgenda) sess.session.getAgenda()).getActivations()) {
        		b.append(a.getRule().getName()+"\n");
        	}
        }
        return b.toString();
    }

}
