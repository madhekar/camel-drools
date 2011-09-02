/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import java.util.List;

import org.apache.camel.component.drools.persistence.DatabaseObjectWrapper;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 17, 2010
 * Time: 5:27:44 PM
 */
public interface SessionSanitizerDataExtractor {

    List<DatabaseObjectWrapper> getEvents(MultiKey key);

    Object toObject(DatabaseObjectWrapper wrapper);

    boolean alwaysWithLastEvent();

}
