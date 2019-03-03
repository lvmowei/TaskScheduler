package com.lede.tech.workflow.sample.template;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lede.tech.workflow.annotation.Condition;
import com.lede.tech.workflow.annotation.Delay;
import com.lede.tech.workflow.annotation.Node;
import com.lede.tech.workflow.annotation.Template;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;

@Template
@Component("sampleTemplate4")
@Scope("prototype")
public class SampleTemplate4 implements ProcessTemplate
{
	@Node(name = "n00", next = "n01", preCondition = "c01", delay = "d01", retryDelay = 1000, retryTimes = 10)
	public String node00()
	{
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n01", next = "n04")
	public String node01()
	{
		return ProcessTemplate.SUCCESS;
	}

	@Condition(name = "c01")
	public boolean condition1()
	{
		return true;
	}

	@Delay(name = "d01")
	public long delay01()
	{
		return 1000;
	}

	@Override
	public String[] initStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFinished()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getInstanceId()
	{
		// TODO Auto-generated method stub
		return null;
	}
}