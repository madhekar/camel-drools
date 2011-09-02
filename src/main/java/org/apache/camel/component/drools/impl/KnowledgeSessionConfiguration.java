/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.camel.component.drools.utils.PartitionedTimerFactory;
import org.apache.camel.component.drools.utils.ResourceWrapper;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.conf.AssertBehaviorOption;
import org.drools.conf.EventProcessingOption;
import org.drools.definition.KnowledgePackage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 25, 2010
 * Time: 10:20:29 AM
 */
public class KnowledgeSessionConfiguration implements InitializingBean {

    private KnowledgeBase knowledgeBase;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(10);

    private SessionConfiguration config;

    private List<ResourceWrapper> resources = new ArrayList<ResourceWrapper>();

    private void prepareKnowledgeBase() {
        KnowledgeBaseConfiguration sconfig = KnowledgeBaseFactory
                .newKnowledgeBaseConfiguration();
        sconfig.setOption(EventProcessingOption.STREAM);
        sconfig.setOption(AssertBehaviorOption.EQUALITY);
        knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(sconfig);
        knowledgeBase.addKnowledgePackages(readKnowledgePackages());
    }

    public void afterPropertiesSet() {
        threadPool.setMaximumPoolSize(10);
        prepareKnowledgeBase();
        prepareSesionConfig();
    }

    private void prepareSesionConfig() {
        config = new SessionConfiguration();
        config.setTimerFactory(new PartitionedTimerFactory(threadPool));
    }


    private Collection<KnowledgePackage> readKnowledgePackages() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (ResourceWrapper r : resources) {
           kbuilder.add(r.resource, r.resourceType);
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }
        return kbuilder.getKnowledgePackages();
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public SessionConfiguration getConfig() {
        return config;
    }


    @Required
    public void setResources(List<ResourceWrapper> resources) {
        this.resources = resources;
    }


    public void setThreadPool(ScheduledThreadPoolExecutor ex) {
        threadPool = ex;
    }
}
