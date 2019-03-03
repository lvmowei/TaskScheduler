package com.lede.tech.workflow.core.engine.bean.descriptor;

public abstract class AbstractDescriptor
{
	private String name;

	public AbstractDescriptor(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object node)
	{
		return name.equals(((AbstractDescriptor) node).getName());
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
}
