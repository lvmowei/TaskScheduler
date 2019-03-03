package com.lede.tech.workflow.parser.element.annotation;

import com.lede.tech.workflow.parser.element.AbstractParser;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class AbstractNameParser extends AbstractParser
{
	public final static String PATTERN_NAME = "[a-zA-Z0-9\u4E00-\u9FA5]+";
	private static Pattern patternName = Pattern.compile(PATTERN_NAME);

	public AbstractNameParser(String target)
	{
		super(target);
	}

	@Override
	public Object parse()
	{
		String expr = (String) target;
		if (StringUtils.isBlank(expr))
		{
			throw new IllegalArgumentException("节点名称不能为空");
		}
		if (patternName.matcher(expr).matches())
		{
			return expr;
		}
		throw new IllegalArgumentException("节点名称格式错误");
	}
}
