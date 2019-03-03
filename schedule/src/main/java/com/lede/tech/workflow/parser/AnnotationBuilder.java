package com.lede.tech.workflow.parser;

import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.parser.element.annotation.AbstractAnnotationParser;
import com.lede.tech.workflow.parser.element.bean.AbstractBean;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationBuilder implements Builder
{
	/**
	 * 流程模板对应的bean的builder，用来生成templateBean
	 */
	private TemplateBuilder templateBean;
	/**
	 * 一个流程模板中的注解生成的对应的bean的list
	 */
	private List<AbstractBean> beanList;

	private static String annotationPackage = "com.lede.tech.workflow.annotation";
	private static String parserPath = "com.lede.tech.workflow.parser.element.annotation";

	/**获取方法上的工作流注解
	 * @param method
	 * @return
	 */
	private static Annotation getTemplateAnnotation(Method method)
	{
		Annotation[] annos = method.getAnnotations();
		for (Annotation anno : annos)
		{
			Package pkg = anno.annotationType().getPackage();
			if (pkg != null && pkg.getName().toLowerCase().equals(annotationPackage))
			{
				return anno;
			}
		}
		return null;
	}

	/**获取该注解对应的解析类
	 * @param anno
	 * @return
	 */
	private static String getParserName(Annotation anno)
	{
		if (anno != null)
		{
			String str = anno.annotationType().getName();
			String prefix = str.substring(str.lastIndexOf(".") + 1);
			return parserPath + "." + prefix.substring(0, 1).toLowerCase() + prefix.substring(1) + "." + prefix
					+ "Parser";
		}
		return null;
	}

	@Override
	//解析一个流程模板（接口或者抽象类）
	public void parse(Class<? extends ProcessTemplate> clazz)
	{
		templateBean = new TemplateBuilder(clazz);
		beanList = new ArrayList<AbstractBean>();

		//解析模板类中带有模板注解的方法，返回注解对应的AbstractBean，比如NodeBean, ConditionBean, DelayBean
		ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method)
			{
				try
				{
					AbstractAnnotationParser parser = null;
					AbstractBean bean = null;

					Annotation templateAnno = getTemplateAnnotation(method);
					if (templateAnno != null)
					{
						Class<?> parserClass = Class.forName(getParserName(templateAnno));
						Constructor<?> conn = parserClass.getConstructor(Annotation.class);
						parser = (AbstractAnnotationParser) conn.newInstance(templateAnno);
						bean = (AbstractBean) parser.parse();
					}
					//检查方法返回值是否合法
					if (parser != null)
					{
						parser.parse(method);
					}
					//注入接口方法
					if (bean != null)
					{
						bean.setMethod(method);
						beanList.add(bean);
					}
				}
				catch (Throwable e)
				{
					throw new RuntimeException(e);
				}
			}
		});

		for (AbstractBean bean : beanList)
		{
			if (!templateBean.addBean(bean))
			{
				throw new IllegalArgumentException("存在重复的名字定义[" + bean.getName() + "]");
			}
		}
	}

	@Override
	public Object build()
	{
		TemplateBean bean = templateBean.process();
		return bean;
	}
}
