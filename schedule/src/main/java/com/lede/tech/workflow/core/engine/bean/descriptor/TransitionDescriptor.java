package com.lede.tech.workflow.core.engine.bean.descriptor;

import java.lang.reflect.Method;

public class TransitionDescriptor extends AbstractDescriptor
{
	private Method action;
	private Method preCondition;
	private Method postCondition;
	private Method delay;

	private long fixedDelay;
	private long retryDelay;
	private int retryTimes;

	public TransitionDescriptor(String name)
	{
		super(name);
	}

	public Method getAction()
	{
		return action;
	}

	public void setAction(Method action)
	{
		this.action = action;
	}

	public Method getPreCondition()
	{
		return preCondition;
	}

	public void setPreCondition(Method preCondition)
	{
		this.preCondition = preCondition;
	}

	public Method getPostCondition()
	{
		return postCondition;
	}

	public void setPostCondition(Method postCondition)
	{
		this.postCondition = postCondition;
	}

	public Method getDelay()
	{
		return delay;
	}

	public void setDelay(Method delay)
	{
		this.delay = delay;
	}

	public long getRetryDelay()
	{
		return retryDelay;
	}

	public void setRetryDelay(long retryDelay)
	{
		this.retryDelay = retryDelay;
	}

	public int getRetryTimes()
	{
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes)
	{
		this.retryTimes = retryTimes;
	}

	public long getFixedDelay()
	{
		return fixedDelay;
	}

	public void setFixedDelay(long fixedDelay)
	{
		this.fixedDelay = fixedDelay;
	}
}
