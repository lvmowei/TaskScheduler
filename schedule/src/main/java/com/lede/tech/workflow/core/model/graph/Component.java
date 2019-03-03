package com.lede.tech.workflow.core.model.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.SecureRandom;
import java.util.Random;

public abstract class Component implements Comparable<Component>
{
	private String name;
	private String descriptor;

	protected Log log = LogFactory.getLog(getClass());

	public enum Type
	{
		GRAPH, NODE, EDGE, OTHER
	}

	public Component()
	{
		Random r = new SecureRandom();
		name = String.valueOf(r.nextInt());
		descriptor = null;
	}

	public Component(String name)
	{
		this.name = name;
		descriptor = null;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescriptor()
	{
		return descriptor;
	}

	public void setDescriptor(String descriptor)
	{
		this.descriptor = descriptor;
	}

	public abstract Type getType();

	@Override
	public boolean equals(Object node)
	{
		return name.equals(((Component) node).getName());
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	public int compareTo(Component c)
	{
		if (c == null)
		{
			return -1;
		}
		else
		{
			return getName().compareTo(c.getName());
		}
	}
}
