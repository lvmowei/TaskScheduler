package com.lede.tech.workflow.factory;

import com.lede.tech.workflow.core.engine.Engine;
import com.lede.tech.workflow.core.engine.bean.InstanceBean;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class TemplateFactory implements ApplicationContextAware
{
	private static Log log = LogFactory.getLog(TemplateFactory.class);
	protected static ApplicationContext ctx;

	private static String getTemplateName(Class<? extends ProcessTemplate> clazz)
	{
		String className = clazz.getName();
		className = className.substring(className.lastIndexOf('.') + 1);
		className = className.substring(0, 1) + className.substring(1);
		return className;
	}

	/**从spring容器中获取对应template的实例
	 * @param clazz
	 * @return
	 */
	public static ProcessTemplate initInstance(Class<? extends ProcessTemplate> clazz)
	{
		ProcessTemplate newInstance = (ProcessTemplate) ctx.getBean(getTemplateName(clazz));
		return newInstance;
	}

	/**1.先去Engine的Container中去找，找不到返回null
	 * @param instanceId
	 * @return
	 */
	public static ProcessTemplate cloneInstance(String instanceId)
	{
		if (StringUtils.isBlank(instanceId))
		{
			return null;
		}
		InstanceBean instanceBean = Engine.getContainer().getInstance(instanceId);
		if (instanceBean == null)
		{
			log.info("获取流程实例副本[" + instanceId + "]时在引擎中未找到目标实例ID");
			return null;
		}
		ProcessTemplate instance = instanceBean.getInstance();
		String className = getTemplateName(instance.getClass());
		if (log.isDebugEnabled())
		{
			log.debug("获取流程实例副本" + className);
		}
		ProcessTemplate newInstance = (ProcessTemplate) ctx.getBean(className);
		/*if (instance instanceof MasterTemplate)
		{
			MasterTemplate temp = (MasterTemplate) newInstance;
			temp.setGame(((MasterTemplate) instance).getGame());
		}
		else if (instance instanceof SlaveTemplate)
		{
			SlaveTemplate temp = (SlaveTemplate) newInstance;
			temp.setController(((SlaveTemplate) instance).getController());
			temp.setTask(((SlaveTemplate) instance).getTask());
		}*/
		return newInstance;
	}

	/**
	 * @param instanceId jclq201402193306
	 * @return
	 */
	public static boolean reinitInstance(String instanceId)
	{
		ProcessTemplate instance = cloneInstance(instanceId);
		if (instance == null)
		{
			return false;
		}
		/*if (instance instanceof MasterTemplate)
		{
			((MasterTemplate) instance).init();
		}
		else if (instance instanceof SlaveTemplate)
		{
			Engine.initInstance(instance);
		}*/
		return true;
	}

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		ctx = arg0;
	}

	public ApplicationContext getContext()
	{
		return ctx;
	}
}
