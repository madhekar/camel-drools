/*
 * Copyright (c) 2010 TouK.pl
 */

package org.apache.camel.component.drools.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.drools.time.TimerService;
import org.drools.time.TimerServiceFactory;

/**
 * Created by IntelliJ IDEA.
 * User: mproch
 * Date: Feb 11, 2010
 * Time: 11:49:55 AM
 */
public class PartitionedTimerFactory implements TimerServiceFactory {

    ScheduledThreadPoolExecutor threadPool;

    public PartitionedTimerFactory(ScheduledThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    private static final long serialVersionUID = 2911623583987844463L;

    public TimerService createInstance() {
        return new ExternalTimerService(threadPool);
    }
}
