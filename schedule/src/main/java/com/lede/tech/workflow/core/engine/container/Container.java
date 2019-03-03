package com.lede.tech.workflow.core.engine.container;

import com.lede.tech.workflow.core.engine.bean.InstanceBean;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.util.DateUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings(
{ "rawtypes" })
public class Container
{
	private Log log = LogFactory.getLog(getClass());

	private Set<TemplateBean> templateSet;
	private Set<InstanceBean> instanceSet;

	private Lock readLock;
	private Lock writeLock;

	private ReentrantReadWriteLock lock;

	public Container()
	{
		templateSet = new HashSet<TemplateBean>();
		instanceSet = new HashSet<InstanceBean>();

		lock = new ReentrantReadWriteLock();

		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}

	public boolean addInstance(InstanceBean bean)
	{
		writeLock.lock();
		try
		{
			if (!templateSet.contains(getTemplate(bean.getInstance())))
			{
				log.error("流程实例[" + bean.getInstance().getInstanceId() + "]所属流程模板在引擎中未注册，添加实例失败");
				return false;
			}
			if (instanceSet.contains(bean))
			{
				log.error("流程实例[" + bean.getInstance().getInstanceId() + "]在系统中已存在，不做重复添加");
				return false;
			}
			List<InstanceBean> removeList = new ArrayList<InstanceBean>();
			Iterator<InstanceBean> it = instanceSet.iterator();
			//移除历史的同名流程实例
			while (it.hasNext())
			{
				InstanceBean b = it.next();
				if (b.getInstanceId().equals(bean.getInstanceId()) && b.getSequence().compareTo(bean.getSequence()) < 0)
				{
					removeList.add(b);
					log.error("Container:addInstance:调度队列中将要移除任务[" + getTemplate(bean.getInstance()) + "|"
							+ bean.getInstance().getInstanceId() + "]" + b.toString());
				}
			}
			instanceSet.removeAll(removeList);
			return instanceSet.add(bean);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public InstanceBean getInstance(String instanceId)
	{
		readLock.lock();
		try
		{
			if (StringUtils.isBlank(instanceId))
			{
				return null;
			}
			Iterator<InstanceBean> it = instanceSet.iterator();
			while (it.hasNext())
			{
				InstanceBean bean = it.next();
				if (bean.getInstance().getInstanceId().equalsIgnoreCase(instanceId))
				{
					return bean;
				}
			}
			return null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public boolean removeFinishedInstance()
	{
		writeLock.lock();
		try
		{
			Iterator<InstanceBean> it = instanceSet.iterator();
			while (it.hasNext())
			{
				InstanceBean bean = it.next();
				if (bean.getInstance().isFinished())
				{
					it.remove();
				}
			}
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public boolean removeInstance(String instanceId)
	{
		writeLock.lock();
		try
		{
			if (StringUtils.isBlank(instanceId))
			{
				return false;
			}
			List<InstanceBean> removeList = new ArrayList<InstanceBean>();
			Iterator<InstanceBean> it = instanceSet.iterator();
			while (it.hasNext())
			{
				InstanceBean bean = it.next();
				if (bean.getInstance().getInstanceId().equalsIgnoreCase(instanceId))
				{
					removeList.add(bean);
				}
			}
			return instanceSet.remove(removeList);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public boolean addTemplate(TemplateBean bean)
	{
		writeLock.lock();
		try
		{
			return templateSet.add(bean);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public TemplateBean getTemplate(String id)
	{
		readLock.lock();
		try
		{
			Iterator<TemplateBean> it = templateSet.iterator();
			while (it.hasNext())
			{
				TemplateBean bean = it.next();
				if (bean.getTemplateId().equals(id))
				{
					return bean;
				}
			}
			return null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public TemplateBean getTemplate(Class templateClass)
	{
		readLock.lock();
		try
		{
			Iterator<TemplateBean> it = templateSet.iterator();
			while (it.hasNext())
			{
				TemplateBean bean = it.next();
				if (bean.getTemplate().equals(templateClass))
				{
					return bean;
				}
			}
			return null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public TemplateBean getTemplate(ProcessTemplate instance)
	{
		Class clazz = instance.getClass();
		return getTemplate(clazz);
	}

	public String snaphot()
	{
		Date now = new Date(System.currentTimeMillis());
		readLock.lock();
		try
		{
			StringBuffer sb = new StringBuffer();
			sb.append("============ Container 模板 ============\n");
			Iterator<TemplateBean> itTempldate = templateSet.iterator();
			while (itTempldate.hasNext())
			{
				TemplateBean bean = itTempldate.next();
				sb.append("模板[").append(bean.getTemplate().getName()).append("]\n");
			}
			sb.append("============ Container 实例 ============\n");
			Iterator<InstanceBean> itInstance = instanceSet.iterator();
			while (itInstance.hasNext())
			{
				InstanceBean bean = itInstance.next();
				sb.append(DateUtil.formatDate(now, DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS)).append("实例[")
						.append(bean.getInstance().getInstanceId()).append("]序列号[").append(bean.getSequence())
						.append("]\n");
			}
			return sb.toString();
		}
		finally
		{
			readLock.unlock();
		}
	}
}
