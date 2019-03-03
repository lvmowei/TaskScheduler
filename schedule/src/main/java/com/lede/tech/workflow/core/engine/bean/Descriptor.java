package com.lede.tech.workflow.core.engine.bean;

import com.lede.tech.workflow.core.engine.bean.descriptor.AbstractDescriptor;
import com.lede.tech.workflow.core.engine.bean.descriptor.ConditionDescriptor;
import com.lede.tech.workflow.core.engine.bean.descriptor.TransitionDescriptor;

import java.util.HashSet;
import java.util.Set;

public class Descriptor
{
	private Set<TransitionDescriptor> transDesc;
	private Set<ConditionDescriptor> condDesc;

	public Descriptor()
	{
		transDesc = new HashSet<TransitionDescriptor>();
		condDesc = new HashSet<ConditionDescriptor>();
	}

	public boolean add(AbstractDescriptor descriptor)
	{
		if (descriptor == null)
		{
			return false;
		}
		if (descriptor instanceof TransitionDescriptor)
		{
			return transDesc.add((TransitionDescriptor) descriptor);
		}
		if (descriptor instanceof ConditionDescriptor)
		{
			return condDesc.add((ConditionDescriptor) descriptor);
		}
		return false;
	}

	public TransitionDescriptor getTransition(String name)
	{
		if (name == null)
		{
			return null;
		}
		for (TransitionDescriptor trans : transDesc)
		{
			if (trans.getName().equals(name))
			{
				return trans;
			}
		}
		return null;
	}

	public ConditionDescriptor getCondition(String name)
	{
		if (name == null)
		{
			return null;
		}
		for (ConditionDescriptor cond : condDesc)
		{
			if (cond.getName().equals(name))
			{
				return cond;
			}
		}
		return null;
	}
}
