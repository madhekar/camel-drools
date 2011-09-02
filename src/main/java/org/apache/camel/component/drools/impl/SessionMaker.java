/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import org.apache.commons.collections.keyvalue.MultiKey;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 12:25:54 PM

 */
public interface SessionMaker {

    SessionWithIdentifier makeSession(MultiKey id, Object trigger);

}
