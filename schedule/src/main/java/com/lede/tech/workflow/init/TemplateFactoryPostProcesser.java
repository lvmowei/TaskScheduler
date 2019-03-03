package com.lede.tech.workflow.init;

import com.lede.tech.workflow.annotation.Template;
import com.lede.tech.workflow.core.engine.Engine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Repository;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
@Repository
public class TemplateFactoryPostProcesser implements BeanPostProcessor
{
	public final Log log = LogFactory.getLog(getClass());

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		try
		{
			Class beanClass = bean.getClass();
			if (findAnnotatedClass(beanClass) != null)
			{
				if (Engine.initTemplate(beanClass))
				{
					log.warn("流程模板[" + beanClass.getName() + "]已载入");
				}
			}
		}
		catch (Exception e)
		{
			log.error("扫描流程模板发生异常", e);
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
	{
		return bean;
	}

	/**
	 * Find Class or Interface annotated by Secure
	 * @param clazz
	 * @return
	 */
	private Class findAnnotatedClass(Class clazz)
	{
		if (clazz.getAnnotation(Template.class) != null)
		{
			return clazz;
		}
		for (Class pi : clazz.getInterfaces())
		{
			if (pi.getAnnotation(Template.class) != null)
			{
				return pi;
			}
		}
		return null;
	}

}