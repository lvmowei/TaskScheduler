package com.lede.tech.workflow.parser.element.annotation.node.property;

import com.lede.tech.workflow.parser.element.AbstractParser;

public class RetryTimesParser extends AbstractParser
{
	public RetryTimesParser(int target)
	{
		super(target);
	}

	@Override
	public Object parse()
	{
		try
		{
			int delay = (Integer) target;
			return delay;
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("解析错误[" + target + "]", e);
		}
	}
}
