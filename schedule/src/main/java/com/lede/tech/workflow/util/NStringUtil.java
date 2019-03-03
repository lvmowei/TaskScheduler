package com.lede.tech.workflow.util;

import org.apache.commons.lang.StringUtils;
/**
 * @Description:字符串工具类,主要功能是字符串空的判断，字符串大写等等
 * @author xlzhu
 * @date 2015年9月2日 下午3:11:07
 * @version 1.0
 */
public final class NStringUtil
{
	/**
	 * @Description: 检查多个参数是否合法
	 * 对于String，一定不能为空且不为空字符串，对于对象不能为空
	 * @return ：boolean
	 */
	public static boolean checkParams(final Object... objects)
	{
		if (objects == null)
			return false;
		for (int i = 0; i < objects.length; i++)
		{
			if (objects[i] == null)
				return false;
			if (objects[i] instanceof String)
			{
				String t = (String) objects[i];
				if (StringUtils.isBlank(t))
					return false;
			}
		}
		return true;
	}
	/**
	 * String[] 拼接，以 @separator 分隔,默认为逗号
	 * @param objs
	 * @param separator
	 * @return
	 */
	public static String combine(Object[] objs, String separator)
	{
		if (objs == null || objs.length == 0)
		{
			return null;
		}
		if (separator == null || separator.trim().length() == 0)
		{
			separator = ",";
		}
		StringBuffer sb = new StringBuffer();
		for (Object obj : objs)
		{
			if (obj != null)
			{
				sb.append(obj.toString()).append(separator);
			}
		}
		return sb.substring(0, sb.length() - 1).toString();
	}
	/**
	 * @Description: 检查多个参数是否合法
	 * 对于String，一定不能为空且不为空字符串，对于对象不能为空
	 * @return ：boolean
	 */
	public static boolean checkParams(final String... objects)
	{
		if (objects == null)
			return false;
		for (int i = 0; i < objects.length; i++)
		{
			if (objects[i] == null)
				return false;
			String t = (String) objects[i];
			if (StringUtils.isBlank(t))
				return false;
		}
		return true;
	}
	/**
	 * @Description
	 * @param info
	 * @param a
	 * @param b
	 * @param bind
	 * @return String
	 */
	public static String tranStart(String info, int a, int b, int bind)
	{
		if (StringUtils.isBlank(info))
			return StringUtils.EMPTY;
		if (bind == 1)
			return info;
		return CommonUtil.encryptString(info, a, b);
	}
	public static String[] getWinNumberSplit(final String sourceStr, final String splitTag)
	{
		String[] temp = sourceStr.trim().split(splitTag);
		if (temp == null || temp.length < 1)
		{
			return null;
		}
		return temp;
	}
	/**
	 * @Description Java  第一个字母大写
	 * @param fildeName
	 * @return String
	 */
	public static String UpperCase(String fildeName)
	{
		byte[] items = fildeName.getBytes();
		items[0] = (byte) ((char) items[0] - 'a' + 'A');
		return new String(items);
	}
}