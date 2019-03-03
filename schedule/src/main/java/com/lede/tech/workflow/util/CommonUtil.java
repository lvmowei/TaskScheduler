package com.lede.tech.workflow.util;

import org.apache.commons.lang.StringUtils;

public class CommonUtil
{
	/**
	 * 加密字符串，使用*字符覆盖源字符串中的指定部分直至结尾
	 * @param str 源字符串
	 * @param start 开始位置（包括在覆盖部分内）
	 * @return
	 */
	public static String encryptString(String str, int start)
	{
		if (StringUtils.isBlank(str))
		{
			return "";
		}
		else
		{
			return encryptString(str, start, str.length());
		}
	}
	
	/**
	 * 加密字符串，使用*字符覆盖源字符串中的指定部分
	 * 
	 * @param str
	 *            源字符串
	 * @param start
	 *            开始位置（包括在覆盖部分内），且总是start与end中较小的
	 * @param end
	 *            终止位置（不包括在覆盖部分内），且总是start与end中较大的
	 * @return
	 */
	public static String encryptString(String str, int start, int end)
	{
		int min = min(start, end);
		int max = max(start, end);
		min = max(min, 0);
		max = min(max, str.length());
		if (StringUtils.isNotBlank(str))
		{
			return StringUtils.overlay(str, StringUtils.repeat("*", max - min), min, max);
		}
		else
		{
			return "";
		}
	}
	
	public static int min(int a, int b)
	{
		return a < b ? a : b;
	}
	public static int max(int a, int b)
	{
		return a > b ? a : b;
	}
}
