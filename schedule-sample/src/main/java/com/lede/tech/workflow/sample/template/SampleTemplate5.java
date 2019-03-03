package com.lede.tech.workflow.sample.template;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lede.tech.workflow.annotation.Condition;
import com.lede.tech.workflow.annotation.Delay;
import com.lede.tech.workflow.annotation.Node;
import com.lede.tech.workflow.annotation.Template;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;

@Template
@Component("sampleTemplate5")
@Scope("prototype")
public class SampleTemplate5 implements ProcessTemplate
{

	@Node(name = "n00", next = "[n01,(c02:n02,c03:n03)]", preCondition = "c04", delay = "d1")
	public String node00()
	{
		System.out.println("执行节点n00执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n01", fixedDelay = 2000, retryTimes = 10, retryDelay = 1000)
	public String node01()
	{
		System.out.println("执行节点n01执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n02", postCondition = "c04")
	public String node02()
	{
		System.out.println("执行节点n02执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n03")
	public String node03()
	{
		System.out.println("执行节点n03执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n04", previous = "[n01,n03]")
	public String node04()
	{
		System.out.println("执行节点n04执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Condition(name = "c02")
	public boolean condition2()
	{
		return false;
	}

	@Condition(name = "c03")
	public boolean condition3()
	{
		return true;
	}

	@Condition(name = "c04")
	public boolean condition4()
	{
		return true;
	}

	@Delay(name = "d1")
	public long delay1()
	{
		return 1000;
	}

	@Override
	public String[] initStatus()
	{
		return new String[]
		{ "n00" };
	}

	@Override
	public boolean isFinished()
	{
		return false;
	}

	@Override
	public String getInstanceId()
	{
		return "sample";
	}

}
