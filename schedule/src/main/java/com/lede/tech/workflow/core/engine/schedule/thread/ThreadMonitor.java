package com.lede.tech.workflow.core.engine.schedule.thread;

import com.lede.tech.workflow.core.engine.schedule.Scheduler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadMonitor implements Runnable
{
	private Log log = LogFactory.getLog(getClass());

	private Scheduler scheduler;

	public ThreadMonitor(Scheduler scheduler)
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
				scheduler.failedProcess();
			}
			catch (Exception e)
			{
				log.fatal("流程调度失败队列处理有异常！", e);
			}
		}
	}
}
