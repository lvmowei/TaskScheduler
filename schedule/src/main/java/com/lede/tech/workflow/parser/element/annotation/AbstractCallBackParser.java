package com.lede.tech.workflow.parser.element.annotation;

import com.lede.tech.workflow.parser.element.AbstractParser;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class AbstractCallBackParser extends AbstractParser
{
	private static Pattern patternName = Pattern.compile(AbstractNameParser.PATTERN_NAME);

	public AbstractCallBackParser(String target)
	{
		super(target);
	}

	@Override
	public Object parse()
	{
		String expr = (String) target;
		if (StringUtils.isBlank(expr))
		{
			return null;
		}
		if (patternName.matcher(expr).matches())
		{
			return expr;
		}
		throw new IllegalArgumentException("节点名称格式错误[" + expr + "]");
	}
}