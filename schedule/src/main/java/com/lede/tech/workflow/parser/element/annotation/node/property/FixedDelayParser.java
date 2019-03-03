package com.lede.tech.workflow.parser.element.annotation.node.property;

import com.lede.tech.workflow.parser.element.AbstractParser;

public class FixedDelayParser extends AbstractParser
{
	public FixedDelayParser(long target)
	{
		super(target);
	}

	@Override
	public Object parse()
	{
		try
		{
			long delay = (Long) target;
			return delay;
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("解析错误[" + target + "]", e);
		}
	}
}
