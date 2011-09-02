/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.utils;

import org.drools.builder.ResourceType;
import org.drools.io.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 11:51:44 AM
  */
public class ResourceWrapper {
    public final Resource resource;
    public final ResourceType resourceType;

    public ResourceWrapper(Resource resource, ResourceType resourceType) {
       this.resource = resource;
       this.resourceType = resourceType;
    }
}
