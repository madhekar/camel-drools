/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 25, 2010
 * Time: 8:40:28 AM
 */
public class SessionWithIdentifier {

    private final StatefulKnowledgeSession session;

    private final MultiKey id;

    public SessionWithIdentifier(StatefulKnowledgeSession session, MultiKey id) {
        this.session = session;
        this.id = id;
    }

    public MultiKey getBusinessId() {
        return id;
    }

    public StatefulKnowledgeSession getSession() {
        return session;
    }

    public int getSessionId() {
        return session.getId();
    }

}
