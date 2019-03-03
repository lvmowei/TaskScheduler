package com.lede.tech.workflow.parser.element.annotation.condition;

import com.lede.tech.workflow.parser.element.annotation.AbstractAnnotationParser;
import com.lede.tech.workflow.parser.element.bean.ConditionBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class ConditionParser extends AbstractAnnotationParser
{
	public ConditionParser(Annotation target)
	{
		super(target);
	}

	public ConditionBean parse() throws Exception
	{
		ConditionBean bean = (ConditionBean) super.parse();
		return bean;
	}

	public boolean parse(Method method)
	{
		Class type = method.getReturnType();
		if (type != boolean.class)
		{
			throw new IllegalArgumentException("该注解[@condition]方法返回类型应为boolean");
		}
		return true;
	}
}
