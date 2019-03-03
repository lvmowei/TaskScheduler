package com.lede.tech.workflow.core.engine.bean;


import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.lede.tech.workflow.util.DateUtil;


public class Task implements Delayed
{
	private long trigger;
	private int retried;
	private String template;
	private String instance; //instanceId
	private String sequence;
	private String transition;

	public Task(String template, String instance, String sequence, String transition)
	{
		this.template = template;
		this.instance = instance;
		this.sequence = sequence;
		this.transition = transition;
		trigger = 0;
	}

	public String getTemplate()
	{
		return template;
	}

	public String getInstance()
	{
		return instance;
	}

	public String getTransition()
	{
		return transition;
	}

	public String getSequence()
	{
		return sequence;
	}

	public int getRetried()
	{
		return retried;
	}

	public void setRetried(int retried)
	{
		this.retried = retried;
	}

	@Override
	public int compareTo(Delayed o)
	{
		Task that = (Task) o;
		if (trigger < that.trigger)
		{
			return -1;
		}
		if (trigger > that.trigger)
		{
			return 1;
		}
		return 0;
	}

	public void setDelay(long delay)
	{
		trigger = System.nanoTime() + TimeUnit.NANOSECONDS.convert(delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public long getDelay(TimeUnit unit)
	{
		return unit.convert(trigger - System.nanoTime(), TimeUnit.NANOSECONDS);
	}

	@Override
	public boolean equals(Object t)
	{
		return toString().equals(((Task) t).toString());
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public String toString()
	{
		return "[" + getTemplate() + " - " + getInstance() + " - " + getTransition() + "]";
	}

	public String snapshot()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("任务[模板").append(template).append("\n  实例").append(instance).append("序列号")
				.append(sequence.substring(0, 17)).append("]操作[").append(transition).append("]").append("[剩余重试")
				.append(retried).append("次][执行时间点");
		long time = getDelay(TimeUnit.MILLISECONDS);
		if (time < 0)
		{
			sb.append("(已过期)");
		}
		else
		{
			sb.append("(未到期)");
		}
		sb.append(" ").append(
				DateUtil.formatDate(new Date(System.currentTimeMillis() + time), DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS))
				.append("]");
		return sb.toString();
	}
}
