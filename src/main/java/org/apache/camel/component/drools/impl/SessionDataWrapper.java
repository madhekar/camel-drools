/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import org.apache.commons.collections.keyvalue.MultiKey;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 11:09:57 AM
 */
public class SessionDataWrapper {

    public final MultiKey id;

    public final byte[] data;

    public final Date nextTimer;

    public SessionDataWrapper(byte[] data, Date nextTimer, MultiKey id) {
        this.data = data;
        this.nextTimer = nextTimer;
        this.id = id;
    }
}
