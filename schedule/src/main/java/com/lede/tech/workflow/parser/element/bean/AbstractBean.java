package com.lede.tech.workflow.parser.element.bean;

import java.lang.reflect.Method;

public class AbstractBean
{
	private String name;
	/**
	 * 注解所加在的方法
	 */
	private Method method;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Method getMethod()
	{
		return method;
	}

	public void setMethod(Method method)
	{
		this.method = method;
	}

	@Override
	public boolean equals(Object node)
	{
		return name.equals(((AbstractBean) node).getName());
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
}
