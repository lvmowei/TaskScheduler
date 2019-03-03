package com.lede.tech.workflow.sample.template;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lede.tech.workflow.annotation.Condition;
import com.lede.tech.workflow.annotation.Delay;
import com.lede.tech.workflow.annotation.Node;
import com.lede.tech.workflow.annotation.Template;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;

@Template
@Component("sampleTemplate3")
@Scope("prototype")
public class SampleTemplate3 implements ProcessTemplate
{
	private long offset = 10;
	private long delay = 5 * 1000 + offset;
	private boolean cond = true;

	@Node(name = "n00", next = "[n01,n05]")
	public String node00()
	{
		System.out.println("执行节点n00执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n01", next = "n06", previous = "n03")
	public String node01()
	{
		System.out.println("执行节点n01执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n02", retryDelay = 100, retryTimes = 10, next = "n06")
	public String node02()
	{
		System.out.println("执行节点n02执行任务");
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
		return cond;
	}

	@Node(name = "n03")
	public String node03()
	{
		System.out.println("执行节点n03执行任务");
		return ProcessTemplate.SUCCESS;
	}
	
	@Node(name = "n04", next = "n06")
	public String node04()
	{
		System.out.println("执行节点n04执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n05", next = "n06")
	public String node05()
	{
		System.out.println("执行节点n05执行任务");
		return ProcessTemplate.SUCCESS;
	}

	@Node(name = "n06")
	public String node06()
	{
		System.out.println("执行节点n06执行任务");
		return ProcessTemplate.SUCCESS;
	}

	/*	@Node(name = "n11", previous = "[n02, n10, n03, [n07, n08], (n05, n06)]")
	public String node11()
	{
		return ProcessTemplate.SUCCESS;
	}
	
	
	
	@Node(name = "n07", fixedDelay = 2 * 1000)
	public String node07()
	{
		return ProcessTemplate.SUCCESS;
	}
	
	@Node(name = "n08")
	public String node08()
	{
		return ProcessTemplate.SUCCESS;
	}
	
	@Node(name = "n10")
	public String node10()
	{
		return ProcessTemplate.SUCCESS;
	}
	
	@Node(name = "n22", delay = "ri", next = "(c02:n22)", retryTimes = 1, retryDelay = 2 * 60 * 1000)
	public String transitionUMI()
	{
		System.out.println("执行节点 n22");
		return ProcessTemplate.SUCCESS;
	}
	
	
	@Delay(name = "ri")
	public long delay1()
	{
		return delay;
	}*/

	@Override
	public String[] initStatus()
	{
		return new String[]
		{ "n00" };
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
		return "sample3";
	}

}
