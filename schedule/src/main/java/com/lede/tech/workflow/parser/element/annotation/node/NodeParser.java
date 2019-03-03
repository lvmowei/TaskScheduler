package com.lede.tech.workflow.parser.element.annotation.node;

import com.lede.tech.workflow.parser.element.annotation.AbstractAnnotationParser;
import com.lede.tech.workflow.parser.element.bean.NodeBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class NodeParser extends AbstractAnnotationParser
{
	public NodeParser(Annotation target)
	{
		super(target);
	}

	public NodeBean parse() throws Exception
	{
		NodeBean bean = (NodeBean) super.parse();
		return bean;
	}

	public boolean parse(Method method)
	{
		Class type = method.getReturnType();
		if (type != String.class)
		{
			throw new IllegalArgumentException("该注解[@node]方法返回类型应为String");
		}
		return true;
	}
}
