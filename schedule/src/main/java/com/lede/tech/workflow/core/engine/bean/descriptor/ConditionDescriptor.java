package com.lede.tech.workflow.core.engine.bean.descriptor;

import java.lang.reflect.Method;

public class ConditionDescriptor extends AbstractDescriptor
{
	private Method condition;

	public ConditionDescriptor(String name)
	{
		super(name);
	}

	public Method getCondition()
	{
		return condition;
	}

	public void setCondition(Method condition)
	{
		this.condition = condition;
	}
}
