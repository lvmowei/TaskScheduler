package com.lede.tech.workflow.parser.element.bean;

public class NodeBean extends AbstractBean
{
	private Branch next;
	private Branch previous;
	private String preCondition;
	private String postCondition;
	private String delay;
	private Integer retryTimes;
	private Long retryDelay;
	private Long fixedDelay;

	public Branch getNext()
	{
		return next;
	}

	public void setNext(Branch next)
	{
		this.next = next;
	}

	public Branch getPrevious()
	{
		return previous;
	}

	public void setPrevious(Branch previous)
	{
		this.previous = previous;
	}

	public String getPreCondition()
	{
		return preCondition;
	}

	public void setPreCondition(String preCondition)
	{
		this.preCondition = preCondition;
	}

	public int getRetryTimes()
	{
		return retryTimes;
	}

	public long getRetryDelay()
	{
		return retryDelay;
	}

	public String getPostCondition()
	{
		return postCondition;
	}

	public void setPostCondition(String postCondition)
	{
		this.postCondition = postCondition;
	}

	public String getDelay()
	{
		return delay;
	}

	public void setDelay(String delay)
	{
		this.delay = delay;
	}

	public long getFixedDelay()
	{
		return fixedDelay;
	}

	public void setFixedDelay(Long fixedDelay)
	{
		this.fixedDelay = fixedDelay;
	}

	public void setRetryTimes(Integer retryTimes)
	{
		this.retryTimes = retryTimes;
	}

	public void setRetryDelay(Long retryDelay)
	{
		this.retryDelay = retryDelay;
	}

	@Override
	public String toString()
	{
		return "NodeBean{" + "name=" + getName() + ", next=" + next + ", previous=" + previous + ", preCondition='"
				+ preCondition + '\'' + ", postCondition='" + postCondition + '\'' + ", delay='" + delay + '\''
				+ ", retryTimes=" + retryTimes + ", retryDelay=" + retryDelay + ", fixedDelay=" + fixedDelay + '}';
	}
}
