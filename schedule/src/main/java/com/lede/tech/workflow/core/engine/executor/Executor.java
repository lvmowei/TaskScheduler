package com.lede.tech.workflow.core.engine.executor;

import com.lede.tech.workflow.core.engine.Engine;
import com.lede.tech.workflow.core.engine.bean.InstanceBean;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import com.lede.tech.workflow.core.engine.bean.Task;
import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.core.engine.bean.descriptor.ConditionDescriptor;
import com.lede.tech.workflow.core.engine.bean.descriptor.TransitionDescriptor;
import com.lede.tech.workflow.core.engine.container.Container;
import com.lede.tech.workflow.core.engine.schedule.Scheduler;
import com.lede.tech.workflow.core.model.petri.Status;
import com.lede.tech.workflow.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
//import com.lede.tech.constant.CommonConstant;
//import com.lede.tech.constant.IniBean;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class Executor
{
	private static Log log = LogFactory.getLog(Executor.class);

	private static final long DEFAULT_RETRY_DELAY = 400;//毫秒

	private Scheduler scheduler;
	private final Container container;

	private final BlockingQueue<Task> queue;

	//下面这么多变量就为了解决一个问题：显示正在运行的任务
	private final ReentrantLock lock;
	private final ThreadLocal<Integer> indexer;
	private final Task[] current = new Task[Engine.WORKER_NUMBER];

	public Executor(Container container)
	{
		this.container = container;
		queue = new LinkedBlockingQueue<Task>();
		lock = new ReentrantLock();
		indexer = new ThreadLocal<Integer>();
	}

	public void setScheduler(Scheduler scheduler)
	{
		this.scheduler = scheduler;
	}

	public boolean offer(Task task)
	{
		return queue.offer(task);
	}

	private void push(Task task)
	{
		//占个坑
		lock.lock();
		try
		{
			int index = -1;
			for (int i = 0; i < current.length; i++)
			{
				if (current[i] == null)
				{
					index = i;
				}
			}
			if (index < 0)
			{
				log.error("流程实例[" + task.getInstance() + "]执行任务前[" + task.getTransition() + "]蹲坑失败，强蹲！");
				index = 0;
			}
			current[index] = task;
			indexer.set(index);
		}
		finally
		{
			lock.unlock();
		}
	}

	private void pop()
	{
		lock.lock();
		try
		{
			//释放坑
			int index = indexer.get();
			current[index] = null;
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean monitor()
	{
		//执行调用monitor的时间 须 = 监控时间须  * 2 才能达到监控目的
		StringBuffer sb = null;
		long timeout = /*IniBean.getIniIntValue("executorMonitorTimeout", "10")*/ 10 * 60;
		int c = 0;
		for (final Task t : current)
		{
			if (t != null)
			{
				long expired = t.getDelay(TimeUnit.SECONDS);
				if (expired < 0 && ((-expired) > timeout))
				{
					c++;
					if (sb == null)
					{
						sb = new StringBuffer();
					}
					sb.append("实例[").append(t.getInstance()).append("]任务[").append(t.getTransition()).append("]预期开始时间[")
							.append(DateUtil.formatDate(new Date(System.currentTimeMillis() + expired * 1000),
									DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS))
							.append("]");
				}
			}
		}
		if (sb != null)
		{
			sb.insert(0, "工作流如下" + c + "项任务执行时间超过报警值[" + timeout / 60 + "分钟]");
			log.fatal(sb.toString());
		}
		return true;
	}

	public boolean execute()
	{
		Task task = null;
		try
		{
			task = queue.take();
			push(task);
			//开始流程
			InstanceBean instanceBean = container.getInstance(task.getInstance());
			if (instanceBean == null)
			{
				log.fatal("流程实例[" + task.getInstance() + "]在引擎中未找到，不执行任务[" + task.getTransition() + "]");
				return false;
			}
			if (instanceBean.getSequence().compareTo(task.getSequence()) > 0)
			{
				log.warn("Executor:executor:任务[" + task.getInstance() + "|" + task.getTransition() + "]序列号["
						+ task.getSequence() + "]所属实例已重初始化，不继续此任务");
				return false;
			}

			TemplateBean templateBean = container.getTemplate(instanceBean.getInstance());
			if (templateBean == null)
			{
				log.fatal("流程实例[" + task.getInstance() + "]所属流程模板在引擎中未找到，不执行任务[" + task.getTransition() + "]");
				return false;
			}

			TransitionDescriptor transDescriptor = templateBean.getTransDescriptor(task.getTransition());
			ProcessTemplate instance = instanceBean.getInstance();
			Class clazz = instance.getClass();
			//Transition 前置条件方法
			Method preCond = null;
			if (transDescriptor.getPreCondition() != null)
			{
				preCond = clazz.getMethod(transDescriptor.getPreCondition().getName(),
						transDescriptor.getPreCondition().getParameterTypes());
			}
			//Transition 后置方法
			Method postCond = null;
			if (transDescriptor.getPostCondition() != null)
			{
				postCond = clazz.getMethod(transDescriptor.getPostCondition().getName(),
						transDescriptor.getPostCondition().getParameterTypes());
			}
			//Transition 方法
			Method action = clazz.getMethod(transDescriptor.getAction().getName(),
					transDescriptor.getAction().getParameterTypes());
			boolean success = true;
			try
			{
				boolean pre = preCond == null ? true : (Boolean) preCond.invoke(instance);
				if (pre)
				{
					String res = (String) action.invoke(instance);

					if (res != ProcessTemplate.SUCCESS)
					{
						success = false;
					}
					else
					{
						boolean post = postCond == null ? true : (Boolean) postCond.invoke(instance);
						if (!post)
						{
							success = false;
						}
					}
				}
				else
				{
					success = false;
				}
			}
			catch (Exception e)
			{
				success = false;
				log.fatal(
						"Executor:executor:流程实例[" + task.getInstance() + "]运行任务[" + task.getTransition() + "]时任务逻辑发生异常",
						e);
			}
			//if (CommonConstant.SWITCH_ON.equals(IniBean.getIniValue(LogPrefix.LOG_INI_KEY, CommonConstant.SWITCH_ON)))
			//{
			log.info("Executor:Task:[" + task.getInstance() + "] 运行任务[" + task.getTransition() + "] result:" + success);
			//}
			if (!success)
			{
				boolean retry = retry(task, transDescriptor.getRetryDelay(), transDescriptor.getRetryTimes());
				if (retry)
				{//如果需要重试，则返回，表示任务执行未成功
					return !retry;
				}
				else
				{//不需要重试，表示当成任务执行成功处理
					success = true;
				}
			}

			Status cloneStatus = null;
			//去除对当前transition的token锁定，移动transition，生成新的token
			if (success)
			{
				//clone移动后的token状态以便后续操作失败方便回滚
				cloneStatus = instanceBean.getStatus().firing(task.getTransition());
				if (cloneStatus == null)
				{
					log.fatal("Executor:executor:流程实例[" + task.getInstance() + "]执行任务[" + task.getTransition()
							+ "]后生成新的流程状态失败！执行逻辑存在问题！需要手动重新初始化！");
				}
			}
			/*
			 * 这里其实需要保证已经经过锁定的任务(nextTasks)要么全部都成功的执行scheduler.offer(next)操作，要么全部不执行。因为前面的
			 * status.firing操作已经生成了新的token了，如果有scheduler.offer(next)发生异常，则status.firing会重新执行，但是
			 * 之前一次成功执行的preFiring已经将token全部锁定了，失败后重新执行preFiring就不会再生成新的任务，会导致tokens状态一直为Waiting
			 * 而不被执行下一次的任务。也就是说，需要保证preFiring操作和scheduler.offer(next)操作是源子的,如果有失败，则需回滚对status
			 * 的操作
			 */
			try
			{
				//锁定下次需要执行的任务，扔给调度器
				List<Task> nextTasks = preFiring(instanceBean);
				for (Task next : nextTasks)
				{
					boolean ifOffer = scheduler.offer(next);
					if (!ifOffer)
					{
						log.warn("Executor:executor:流程实例[" + next.getInstance() + "] 任务[" + next.getTransition()
								+ "] 放入调度队列失败！");
					}
				}
			}
			catch (Exception e)
			{
				//回滚状态。如果在并发时，有可能出现，已经成功执行的其他线程对instance.status的操作被回滚，不过暂时问题不大，最多再重新执行一次
				if (success && cloneStatus != null)
				{
					instanceBean.setStatus(cloneStatus);
				}
				throw e;
			}
			return true;
		}
		catch (Exception e)
		{
			String msg = "工作流运行器执行任务发生异常！";
			if (task != null)
			{
				if (task.getDelay(TimeUnit.MILLISECONDS) <= 0)
				{
					task.setDelay(DEFAULT_RETRY_DELAY);
				}
				scheduler.offer(task);
				msg += "[" + task.snapshot() + "]重新放入执行队列等待下次执行！";
			}
			log.fatal(msg, e);
			return false;
		}
		finally
		{
			pop();
		}
	}

	private boolean retry(Task task, long retryDelay, long retryTimes)
	{
		int retried = task.getRetried() - 1;
		if (retried >= 0)
		{
			task.setRetried(retried);
			if (retryDelay <= 0)
			{
				//设置一个最小延迟时间，否则连续出错的情况下，失败任务由于已经超为负，优先级最高，会循环占用队列资源
				retryDelay = DEFAULT_RETRY_DELAY;
			}
			task.setDelay(retryDelay);
			//重新发配给调度器，根据重试间隔，重新来过
			//if (CommonConstant.SWITCH_ON.equals(IniBean.getIniValue(LogPrefix.LOG_INI_KEY, CommonConstant.SWITCH_ON)))
			//{
			log.info("Executor:retry:Task[" + task.getInstance() + "]执行[" + task.getTransition() + "]失败！重新放入调度器");
			//}
			boolean ifOffer = scheduler.offer(task);
			if (!ifOffer)
			{
				log.warn("Executor:retry:Task[" + task.getInstance() + "] 任务[" + task.getTransition() + "]重新放入调度队列失败！");
			}
			return true;
		}
		else
		{
			log.error("流程实例[" + task.getInstance() + "]重试任务[" + task.getTransition() + "]" + retryTimes
					+ "次后仍然失败，为了不影响流程，按照执行成功处理！");
			return false;
		}
	}

	public static List<Task> preFiring(InstanceBean instanceBean) throws Exception
	{
		List<Task> lst = new ArrayList<Task>();
		try
		{
			Status status = instanceBean.getStatus();
			TemplateBean templateBean = instanceBean.getTemplate();
			ProcessTemplate instance = instanceBean.getInstance();

			//判断下一次可能发生的 transition,生成流程中其他可以执行的任务
			String[] edgeNames = status.nextCheck();
			List<String> trueEdges = new ArrayList<String>();
			for (String edgeName : edgeNames)
			{
				ConditionDescriptor condDescriptor = templateBean.getCondDescriptor(edgeName);
				if (condDescriptor == null)
				{
					trueEdges.add(edgeName);
				}
				else
				{
					Method cond = condDescriptor.getCondition();
					boolean edge = cond == null ? true : (Boolean) cond.invoke(instance);
					if (edge)
					{
						trueEdges.add(edgeName);
					}
				}
			}
			//得到下一次一定发生的transition
			String[] nextTrans = status.next(trueEdges);
			for (String next : nextTrans)
			{
				//如果锁定成功，则生成新任务
				if (status.hold(next))
				{
					Task nextTask = new Task(templateBean.getTemplateId(), instanceBean.getInstanceId(),
							instanceBean.getSequence(), next);
					TransitionDescriptor nextTransDesc = templateBean.getTransDescriptor(next);
					nextTask.setRetried(nextTransDesc.getRetryTimes());
					long delay = 0;
					Method delayMethod = nextTransDesc.getDelay();
					if (delayMethod != null)
					{
						delay = (Long) delayMethod.invoke(instance);
					}
					else
					{
						delay = nextTransDesc.getFixedDelay();
					}
					nextTask.setDelay(delay);
					//扔给调度器
					lst.add(nextTask);
				}
				else
				{
					log.error("流程实例[" + instance.getInstanceId() + "]预执行任务[" + next + "]可能由于并发原因,锁定失败,跳过!");
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("流程实例[" + instanceBean.getInstance().getInstanceId() + "]锁定预执行任务发生异常!", e);
			throw e;
		}
		return lst;
	}

	public String snaphot()
	{
		StringBuffer sb = new StringBuffer();
		Iterator<Task> it = queue.iterator();
		int index = 0;
		sb.append("=========== Executor 任务队列开始 ===========\n");
		while (it.hasNext())
		{
			Task task = it.next();
			sb.append("Executor:").append(++index).append(" ").append(task.snapshot()).append("\n");
		}
		sb.append("=========== Executor 任务队列完成 ===========\n");
		sb.append("=========== Executor 当前队列开始 ===========\n");
		index = 0;
		lock.lock();
		try
		{
			for (int i = 0; i < current.length; i++)
			{
				if (current[i] != null)
				{
					index++;
					sb.append("Executor:").append(index).append(" ").append(current[i].snapshot()).append("\n");
				}
			}
		}
		finally
		{
			lock.unlock();
		}
		sb.append("=========== Executor 当前队列完成 ===========\n");
		return sb.toString();
	}

	/**
	 * 移除相同的Task
	 * @param task
	 * @return
	 */
	public boolean remove(Task task)
	{
		try
		{
			List<Task> removeList = new ArrayList<Task>();
			Iterator<Task> it = queue.iterator();
			while (it.hasNext())
			{
				Task t = it.next();
				if (t.getInstance().equals(task.getInstance()) && task.getTemplate().equals(task.getTemplate()))
				{
					if (task.getSequence().compareTo(t.getSequence()) < 0)
					{
						log.error("Executor:Remove:执行队列中已经有任务:[" + t.toString() + "] Sequence is " + task.getSequence()
								+ "  当前任务是:[" + task.toString() + "] Sequence is " + task.getSequence());
						return false;
					}
					removeList.add(task);
					log.error("Executor:Remove:执行队列中将要移除任务[" + task.getTemplate() + "|" + task.getInstance() + "]"
							+ task.toString());
				}
			}
			if (removeList.size() > 0)
			{
				queue.removeAll(removeList);
				removeList.clear();
				return true;
			}

		}
		catch (Exception e)
		{
			log.fatal("Executor:Remove:执行队列移除任务[" + task.getTemplate() + "|" + task.getInstance() + "]"
					+ task.toString() + " 失败！");
		}
		return true;

	}
}
