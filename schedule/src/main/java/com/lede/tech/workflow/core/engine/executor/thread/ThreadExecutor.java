package com.lede.tech.workflow.core.engine.executor.thread;

import com.lede.tech.workflow.core.engine.executor.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadExecutor implements Runnable
{
	private Log log = LogFactory.getLog(getClass());

	private Executor executor;

	public ThreadExecutor(Executor executor)
	{
		this.executor = executor;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				executor.execute();
			}
			catch (Exception e)
			{
				log.fatal("流程运行器处理有异常！", e);
			}
		}
	}
}
