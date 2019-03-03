package com.lede.tech.workflow;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * 
 */
@Component
public class ThreadPool
{
	private static final Log LOG = LogFactory.getLog(ThreadPool.class);

	private final ThreadPoolExecutor exec;

	private static final ThreadPool instance = new ThreadPool();

	private ThreadPool()
	{
		exec = new ThreadPoolExecutor(50, 100, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);

	}

	public static ThreadPool getInstance()
	{
		return instance;
	}

	public void exec(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(exec))
		{
			LOG.warn("executor exec is not shutdown or terminating or terminated!");
			return;
		}

		exec.execute(command);
	}

	public static boolean isExecutorAvaliable(ThreadPoolExecutor executor)
	{
		if (executor.isShutdown() || executor.isTerminating() || executor.isTerminated())
			return false;
		return true;
	}

	@PreDestroy
	public void shutdown()
	{
		exec.shutdown();
	}
}
