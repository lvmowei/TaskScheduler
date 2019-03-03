package com.lede.tech.workflow.core.engine.schedule;

import com.lede.tech.workflow.core.engine.bean.InstanceBean;
import com.lede.tech.workflow.core.engine.bean.Task;
import com.lede.tech.workflow.core.engine.container.Container;
import com.lede.tech.workflow.core.engine.executor.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler
{
	private Executor executor;
	private Container container;

	private DelayQueue<Task> queue;
	private DelayQueue<Task> failed;

	private ReentrantLock lock;

	private Log log = LogFactory.getLog(getClass());

	public Scheduler(Container container)
	{
		this.container = container;
		queue = new DelayQueue<Task>();
		failed = new DelayQueue<Task>();

		lock = new ReentrantLock();
	}

	public void setExecutor(Executor executor)
	{
		this.executor = executor;
	}

	public boolean remove(String template)
	{
		//加锁为了保证任务队列和失败队列数据集合的一致性
		lock.lock();
		try
		{
			List<Task> removeList = new ArrayList<Task>();
			//DelayQueue的迭代器是复制当前容器内的元素，因此不会抛出ConcurrentModificationException
			Iterator<Task> it = queue.iterator();
			while (it.hasNext())
			{
				Task task = it.next();
				if (task.getTemplate().equals(template))
				{
					removeList.add(task);
					log.error("调度队列中将要移除任务[" + template + "]" + task.toString());
				}
			}
			queue.removeAll(removeList);
			removeList.clear();
			it = failed.iterator();
			while (it.hasNext())
			{
				Task task = it.next();
				if (task.getTemplate().equals(template))
				{
					removeList.add(task);
					log.error("调度失败队列中将要移除任务[" + template + "]" + task.toString());
				}
			}
			failed.removeAll(removeList);
			return true;
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean remove(String template, String instance)
	{
		lock.lock();
		try
		{
			List<Task> removeList = new ArrayList<Task>();
			Iterator<Task> it = queue.iterator();
			while (it.hasNext())
			{
				Task task = it.next();
				if (task.getInstance().equals(instance) && task.getTemplate().equals(template))
				{
					removeList.add(task);
					log.error("调度队列中将要移除任务[" + template + "|" + instance + "]" + task.toString());
				}
			}
			queue.removeAll(removeList);
			removeList.clear();
			it = failed.iterator();
			while (it.hasNext())
			{
				Task task = it.next();
				if (task.getInstance().equals(instance) && task.getTemplate().equals(template))
				{
					removeList.add(task);
					log.error("调度失败队列中将要移除任务[" + template + "|" + instance + "]" + task.toString());
				}
			}
			failed.removeAll(removeList);
			return true;
		}
		finally
		{
			lock.unlock();
		}
	}

	public void schedule()
	{
		Task task = null;
		boolean offer = false;
		try
		{
			task = queue.poll(1L, TimeUnit.SECONDS);
			if (task == null)
			{
				return;
			}
			if (executor.remove(task))//移除执行队列里面相同Task
			{
				offer = executor.offer(task);
			}
			else
			{
				log.error("Schedule:Schedule:执行器中已经存在任务:[" + task.toString() + "] 不放入执行器！！");
			}
		}
		catch (InterruptedException e)
		{
			log.fatal("调度任务队列发生中断异常,继续下次执行", e);
		}
		catch (Exception e)
		{
			log.fatal("流程调度器处理任务发生异常", e);
		}
		if (task != null && !offer)
		{//任务处理器队列满了
			addFailed(task);
		}
	}

	public void failedProcess()
	{
		Task task = null;
		try
		{
			//停2秒是为了避免失败队列来回把已经过期的(优先级最高)的任务反复插进普通任务队列中，影响普通任务的调度
			Thread.sleep(2000);
			task = takeFailed();
		}
		catch (InterruptedException e)
		{
			log.fatal("调度失败任务队列发生中断异常,继续下次执行", e);
		}
		if (task != null)
		{
			boolean ok = queue.offer(task);
			if (ok)
			{
				log.fatal("调度失败任务重新加入调度队列" + task.toString());
			}
			//offer理论上不会返回false
		}
	}

	public boolean offer(Task task)
	{
		if (task == null || !isValid(task))
		{
			return false;
		}
		if (log.isDebugEnabled())
		{
			log.debug("任务[" + task.getInstance() + "|" + task.getTransition() + "]将在"
					+ task.getDelay(TimeUnit.MILLISECONDS) + "豪秒后执行...");
		}
		return queue.offer(task);
	}

	private boolean addFailed(Task task)
	{
		if (task == null || !isValid(task))
		{
			return false;
		}
		log.fatal("调度失败队列压入任务" + task.toString());
		return failed.offer(task);
	}

	private Task takeFailed() throws InterruptedException
	{
		Task task = failed.take();
		log.fatal("调度失败队列弹出任务" + task.toString());
		return task;
	}

	public DelayQueue<Task> getQueue()
	{
		return queue;
	}

	private boolean isValid(Task task)
	{
		InstanceBean bean = container.getInstance(task.getInstance());
		if (bean == null)
		{
			log.warn("Schedule:isValid:任务[" + task.getInstance() + "|" + task.getTransition() + "]所属实例已从容器移除，不继续此任务");
			return false;
		}
		if (bean.getSequence().compareTo(task.getSequence()) > 0)
		{
			log.warn("Schedule:isValid:任务[" + task.getInstance() + "|" + task.getTransition() + "]序列号["
					+ task.getSequence() + "]所属实例已重初始化，不继续此任务");
			return false;
		}
		return true;
	}

	public String snaphot()
	{
		StringBuffer sb = new StringBuffer();
		Iterator<Task> it = queue.iterator();
		int index = 0;
		sb.append("=========== Scheduler 任务队列开始 ===========\n");
		while (it.hasNext())
		{
			index++;
			Task task = it.next();
			sb.append("Schedule:").append(index).append(" ").append(task.snapshot()).append("\n");
		}
		sb.append("=========== Scheduler 任务队列完成 ===========\n");
		index = 0;
		it = failed.iterator();
		sb.append("=========== Scheduler 失败队列开始 ===========\n");
		while (it.hasNext())
		{
			index++;
			Task task = it.next();
			sb.append("Schedule:").append(index).append(" ").append(task.snapshot()).append("\n");
		}
		sb.append("=========== Scheduler 失败队列完成 ===========\n");
		return sb.toString();
	}
}
