/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 18, 2010
 * Time: 7:42:49 AM
 */
public interface EventWithTime extends Comparable<EventWithTime> {
    long getTimestamp();
}
