package com.lede.tech.workflow.parser.element;

public abstract class AbstractParser implements Parser
{
	protected Object target;

	public AbstractParser(Object target)
	{
		this.target = target;
	}
}
