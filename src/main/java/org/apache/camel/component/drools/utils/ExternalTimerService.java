/**
 * 
 */
package org.apache.camel.component.drools.utils;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.SessionClock;
import org.drools.time.TimerService;
import org.drools.time.Trigger;
import org.drools.time.impl.JDKTimerService.JDKJobHandle;

/**
 * @author mproch
 * 
 */
public class ExternalTimerService implements TimerService, SessionClock {

	private static final long serialVersionUID = 7323073087297725364L;

	protected static Log log = LogFactory.getLog(ExternalTimerService.class);

    protected static Long MAX_DATE;
    
    static {
        try {
            MAX_DATE = new SimpleDateFormat("yyyy-MM-dd").parse("4000-01-01").getTime();
        } catch (Exception ex) {
            log.error("Unable to parse MAX_DATE!!!");
        }
    }
    
	PriorityQueue<JDKJobHandle> queue = new PriorityQueue<JDKJobHandle>(1, new Comparator<JDKJobHandle>() {
		public int compare(JDKJobHandle o1, JDKJobHandle o2) {
			return o1.getFuture().compareTo(o2.getFuture());
		}
	});

	protected ScheduledThreadPoolExecutor scheduler;

	public ExternalTimerService() {
		this(new ScheduledThreadPoolExecutor(1));
	}

	public ExternalTimerService(ScheduledThreadPoolExecutor scheduler) {
		this.scheduler = scheduler;
	}

	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	public long getTimeToNextJob() {
		JDKJobHandle h = queue.peek();
		//if nothing here - time is \infty
		return h == null ? MAX_DATE : h.getFuture().getDelay(TimeUnit.MILLISECONDS);
	}

	public boolean removeJob(JobHandle jobHandle) {
		JDKJobHandle handle = (JDKJobHandle) jobHandle;
		boolean ret = scheduler.remove((Runnable) handle.getFuture());
		return ret && queue.remove(handle);
	}

	public JobHandle scheduleJob(Job job, JobContext ctx, Trigger trigger) {
		Date date = trigger.nextFireTime();
		log.debug("scheduling: "+job+" date: "+date);
		JDKJobHandle jobHandle = null;
		if (date != null) {
			jobHandle = new JDKJobHandle();
			JDKCallableJob callableJob = new JDKCallableJob(job, ctx, trigger, jobHandle);
			schedule(date, callableJob);
		}
		return jobHandle;
	}

	protected void schedule(Date date, JDKCallableJob callableJob) {
		long delay = Math.max(0,
				date.getTime() - System.currentTimeMillis());
		log.debug("scheduling: delay "+delay);
		ScheduledFuture<Void> f = scheduler.schedule(callableJob, Math.max(0,
				date.getTime() - System.currentTimeMillis()),
				TimeUnit.MILLISECONDS);
		callableJob.handle.setFuture(f);
		queue.add(callableJob.handle);
	}

	public void shutdown() {
		for (JDKJobHandle v : queue) {
			scheduler.remove((Runnable) v.getFuture());
		}
	}

	//mostly copy&paste from JdkExecutorService
	public class JDKCallableJob implements Callable<Void> {
		private final Job job;
		private final Trigger trigger;
		private final JobContext ctx;
		private final JDKJobHandle handle;

		public JDKCallableJob(Job job, JobContext ctx, Trigger trigger, JDKJobHandle handle) {
			this.job = job;
			this.ctx = ctx;
			this.trigger = trigger;
			this.handle = handle;
		}

		public Void call() throws Exception {
			try {
				this.job.execute(this.ctx);
			} finally {
				queue.remove(handle);
			}
			// our triggers allow for flexible rescheduling
			Date date = this.trigger.nextFireTime();
			if (date != null) {
				schedule(date, this);
			}
			return null;
		}
	}

}
