package com.lede.tech.workflow.parser.element.annotation.delay;

import com.lede.tech.workflow.parser.element.annotation.AbstractAnnotationParser;
import com.lede.tech.workflow.parser.element.bean.DelayBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class DelayParser extends AbstractAnnotationParser
{
	public DelayParser(Annotation target)
	{
		super(target);
	}

	public DelayBean parse() throws Exception
	{
		DelayBean bean = (DelayBean) super.parse();
		return bean;
	}

	public boolean parse(Method method)
	{
		Class type = method.getReturnType();
		if (type != long.class)
		{
			throw new IllegalArgumentException("该注解[@delay]方法返回类型应为long");
		}
		return true;
	}
}