package com.lede.tech.workflow.core.engine;

import com.lede.tech.workflow.ThreadPool;
import com.lede.tech.workflow.core.engine.bean.InstanceBean;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import com.lede.tech.workflow.core.engine.bean.Task;
import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.core.engine.container.Container;
import com.lede.tech.workflow.core.engine.executor.Executor;
import com.lede.tech.workflow.core.engine.executor.thread.ThreadExecutor;
import com.lede.tech.workflow.core.engine.schedule.Scheduler;
import com.lede.tech.workflow.core.engine.schedule.thread.ThreadMonitor;
import com.lede.tech.workflow.core.engine.schedule.thread.ThreadScheduler;
import com.lede.tech.workflow.core.model.petri.Status;
import com.lede.tech.workflow.parser.AnnotationBuilder;
import com.lede.tech.workflow.util.LogPrefix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

//import com.lede.tech.constant.IniBean;

public class Engine
{
	private static Log log = LogFactory.getLog(Engine.class);

	private static Executor executor;
	private static Scheduler scheduler;
	private static Container container;

	private static boolean enable = false;

	public final static int WORKER_NUMBER = 30;

	static
	{
		init();
	}

	private static void init()
	{
		container = new Container();
		executor = new Executor(container);
		scheduler = new Scheduler(container);

		executor.setScheduler(scheduler);
		scheduler.setExecutor(executor);
	}

	public static void start()
	{
		if (enable)
		{
			return;
		}
		synchronized (executor)
		{
			if (enable)
			{
				return;
			}

			log.warn("===工作流引擎初始化开始===");
			long start = System.currentTimeMillis();
			log.warn("启动工作流调度器线程...");
			ThreadPool.getInstance().exec(new ThreadScheduler(scheduler));
			ThreadPool.getInstance().exec(new ThreadMonitor(scheduler));
			ThreadPool.getInstance().exec(new Runnable() {
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							executor.monitor();
							int m = /*IniBean.getIniIntValue("executorMonitorTimeout", "10")*/10 / 2;
							if (m <= 0)
							{
								m = 1;
							}
							m *= 60000;
							Thread.sleep(m);
						}
						catch (Exception e)
						{
							log.fatal("流程Executor监控超时时间有异常！", e);
						}
					}
				}
			});

			log.warn("启动工作流运行器线程...");
			for (int i = 0; i < WORKER_NUMBER; i++)
			{
				ThreadPool.getInstance().exec(new ThreadExecutor(executor));
			}

			long end = System.currentTimeMillis();
			log.warn("===工作流引擎初始化完成,使用" + (end - start) + "毫秒===");

			enable = true;
		}
	}

	public static String snaphot()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(container.snaphot()).append("\n").append(scheduler.snaphot()).append("\n").append(executor.snaphot())
				.append("\n");
		return sb.toString();
	}

	/**
	 * 带特殊标记的Snapshot
	 * @param specifiedMsg
	 * @return
	 */
	public static String snapshot(String specifiedMsg)
	{
		StringBuffer sb = new StringBuffer();
		String asterisk = "**********************";
		sb.append(asterisk).append("Specified Snapshot Start:").append(specifiedMsg).append(asterisk);
		sb.append(snaphot());
		sb.append(asterisk).append("Specified Snapshot End:").append(specifiedMsg).append(asterisk);
		return sb.toString();
	}

	/**
	 * 根据{@link ProcessTemplate}生成{@link TemplateBean}并装载到{@link Container}
	 * @param templateClass
	 * @return if throw exception return false ,else return true
	 */
	public static boolean initTemplate(Class<? extends ProcessTemplate> templateClass)
	{
		try
		{
			if (container.getTemplate(templateClass) != null)
			{
				return false;
			}
			log.warn("初始化流程模板[" + templateClass.getName() + "]...");
			AnnotationBuilder builder = new AnnotationBuilder();
			builder.parse(templateClass);
			TemplateBean bean = (TemplateBean) builder.build();
			container.addTemplate(bean);
			return true;
		}
		catch (Exception e)
		{
			log.fatal("初始化流程模板[" + templateClass.getName() + "]失败", e);
			return false;
		}
	}

	public static boolean initInstance(ProcessTemplate instance)
	{
		log.info("WorkFlow:Engine init instance:[" + instance.getInstanceId() + "]");
		try
		{
			TemplateBean templateBean = container.getTemplate(instance);
			if (templateBean == null)
			{
				log.fatal("初始化实例[" + instance.getInstanceId() + "]所属流程模板类未在引擎中找到");
				return false;
			}
			InstanceBean instanceBean = new InstanceBean(templateBean, instance);
			Status status = instanceBean.getStatus();
			//初始化，判断要执行的transitions
			String[] transitions = instance.initStatus();
			if (transitions == null || transitions.length <= 0)
			{
				log.error("初始化实例[" + instance.getInstanceId() + "]返回要执行的节点为空");
				return false;
			}
			//设置 token的状态
			if (LogPrefix.ifPrint())
			{
				log.info("Engine:initInstance:Begin to SetToken");
			}
			status.setToken(transitions);

			container.addInstance(instanceBean);//已经包括了移除相同的instanceId的操作了
			scheduler.remove(templateBean.getTemplateId(), instance.getInstanceId());
			//预锁定任务 从InstanceBean中解析出接下来要执行的Task
			List<Task> nextTasks = Executor.preFiring(instanceBean);
			for (Task next : nextTasks)
			{
				boolean ifOffer = scheduler.offer(next);
				if (!ifOffer)
				{
					log.warn("Executor:executor:流程实例[" + next.getInstance() + "] 任务[" + next.getTransition()
							+ "] 放入调度队列失败！");
				}
			}
			return true;
		}
		catch (Exception e)
		{
			log.fatal("初始化实例[" + instance.getInstanceId() + "]失败，请检查", e);
			return false;
		}
	}

	public static boolean removeFinishedInstance()
	{
		return container.removeFinishedInstance();
	}

	public static Executor getExecutor()
	{
		return executor;
	}

	public static Scheduler getScheduler()
	{
		return scheduler;
	}

	public static Container getContainer()
	{
		return container;
	}

	public static void main(String[] args)
	{
		//DCSpfPeriodTemplate bean = new DCSpfPeriodTemplate();
		//Engine.initTemplate(bean.getClass());
	}

}
