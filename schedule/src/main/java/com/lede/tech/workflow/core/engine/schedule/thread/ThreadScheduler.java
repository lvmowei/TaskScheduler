package com.lede.tech.workflow.core.engine.schedule.thread;

import com.lede.tech.workflow.core.engine.schedule.Scheduler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadScheduler implements Runnable
{
	private Log log = LogFactory.getLog(getClass());

	private Scheduler scheduler;

	public ThreadScheduler(Scheduler scheduler)
	{
		this.scheduler = scheduler;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				scheduler.schedule();
			}
			catch (Exception e)
			{
				log.fatal("流程调度处理有异常！", e);
			}
		}

	}
}
