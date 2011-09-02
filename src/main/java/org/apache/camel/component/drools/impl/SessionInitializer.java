/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;


/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 17, 2010
 * Time: 5:44:19 PM
 */
public interface SessionInitializer {

    void initSession(SessionWithIdentifier session, boolean recoveryMode);

    boolean shouldInitializeNewSession(SessionWithIdentifier session, Object initiator);
}
