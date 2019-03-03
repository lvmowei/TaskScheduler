package com.lede.tech.workflow.parser.element.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lede.tech.workflow.parser.element.AbstractParser;

@SuppressWarnings(
{ "unchecked", "rawtypes" })
public abstract class AbstractAnnotationParser extends AbstractParser
{
	private Log log = LogFactory.getLog(getClass());

	private final static String PARSER = "Parser";
	private final static String PROPTERTY = ".property.";
	private final static String BEAN = "Bean";
	private final static String SET = "set";
	private final static String ANNOTATION = "annotation";

	/**
	 * @param target 将待解析的注解
	 */
	public AbstractAnnotationParser(Annotation target)
	{
		super(target);
	}

	private static String setterName(String name)
	{
		if (StringUtils.isBlank(name) || name.length() < 2)
		{
			return null;
		}
		return SET + name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	/**获取注解中每个字段的parser类
	 * @param rootPath com.lede.tech.workflow.parser.element.annotation.node
	 * @param property preCondition、next
	 * @return com.lede.tech.workflow.parser.element.annotation.node.property.PreConditionParser
	 */
	private static String parserName(String rootPath, String property)
	{
		if (StringUtils.isBlank(property) || property.length() < 2)
		{
			return null;
		}
		return rootPath + PROPTERTY + property.substring(0, 1).toUpperCase() + property.substring(1) + PARSER;
	}

	/**
	 * @param rootPath com.lede.tech.schedule.workflow.parser.element.annotation.node
	 * @return com.lede.tech.workflow.parser.element.bean.NodeBean
	 */
	private static String beanName(String rootPath)
	{
		String str = rootPath.substring(rootPath.lastIndexOf('.') + 1);
		//		return rootPath + "." + str.substring(0, 1).toUpperCase() + str.substring(1) + BEAN;
		return rootPath.substring(0, rootPath.indexOf(ANNOTATION)) + "bean." + str.substring(0, 1).toUpperCase()
				+ str.substring(1) + BEAN;
	}

	public Object parse() throws Exception
	{
		//通过传入的注解，新建一个该注解对应的bean的实例，将注解的属性，放进对应的bean的属性中
		Class<? extends Annotation> annClazz = (Class<? extends Annotation>) target.getClass();
		//如com.lede.tech.workflow.parser.element.annotation.node
		String rootPath = this.getClass().getPackage().getName();
		//要返回的对象，如com.lede.tech.workflow.parser.element.bean.NodeBean
		//注解Node -> NodeBean
		String beanName = beanName(rootPath);
		if (log.isDebugEnabled())
		{
			String name = (String) annClazz.getMethod("name").invoke(target);
			log.debug("处理节点[" + name + "]待解结果路径 ： " + beanName);
		}
		Class beanClazz = Class.forName(beanName);
		Object bean = beanClazz.newInstance();
		Method[] methods = annClazz.getDeclaredMethods();
		//根据注解中的属性名，找到对应名字的Parser类，实例化该类，并调用parse()方法返回解析后的对象
		for (Method method : methods)
		{
			//过滤掉所有注解父类的方法
			boolean skip = false;
			for (Method superMethod : Annotation.class.getMethods())
			{
				if (method.getName().equals(superMethod.getName()))
				{
					skip = true;
				}
			}
			if (skip)
			{
				continue;
			}

			//比如com.lede.tech.workflow.parser.element.annotation.node.property.PreConditionParser
			Class<AbstractParser> clazz = (Class<AbstractParser>) Class.forName(parserName(rootPath, method.getName()));
			Constructor<AbstractParser> con = clazz.getConstructor(method.getReturnType());
			Object obj = method.invoke(target);
			if (log.isDebugEnabled())
			{
				log.debug("处理方法 [" + method.getName() + "]值[" + obj.toString() + "]");
			}
			//得到节点的属性解析器实例，比如PreConditionParser的实例
			AbstractParser parser = con.newInstance(obj);
			Object result = parser.parse();
			//把解析后的对方注入到注解对应的同名bean中，通过bean中同名的set方法
			if (result != null)
			{
				//注意：比如NodeBean里所有field的类型都应该不是primitive
				Method beanSetter = beanClazz.getMethod(setterName(method.getName()), result.getClass());
				beanSetter.invoke(bean, result);
			}
		}
		return bean;
	}

	public abstract boolean parse(Method method);
}
