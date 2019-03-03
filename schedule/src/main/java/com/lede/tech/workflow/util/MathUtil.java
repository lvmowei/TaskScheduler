package com.lede.tech.workflow.util;

import java.math.BigDecimal;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;


/**
 * 数学计算的Util
 * 
 * 
 */
public class MathUtil
{
	private static String COLON = ":";
	private static String BACKSPACE = " ";

	private MathUtil()
	{
	}

	/**
	 * 计算排列组合的值
	 * 
	 * @param total
	 * @param select
	 * @return
	 */
	public static int combine(int total, int select)
	{
		if (select > total)
		{
			return 0;
		}
		else if (select == total)
		{
			return 1;
		}
		else if (total == 0)
		{
			return 1;
		}
		else
		{
			if (select > total / 2)
				select = total - select;

			long result = 1;
			for (int i = total; i > total - select; i--)
			{
				result *= i;
				if (result < 0)
					return -1;
			}
			for (int j = select; j > 0; j--)
			{
				result /= j;
			}
			if (result > Integer.MAX_VALUE)
				return -1;
			return (int) result;
		}
	}

	/**
	 * 计算两个数是否在允许的误差范围内相等
	 * @param a
	 * @param b 
	 * @param maxErrorValue 最大误差
	 * @return
	 */
	public static boolean isEqual(BigDecimal a, BigDecimal b, BigDecimal maxErrorValue)
	{
		if (a.subtract(b).abs().compareTo(maxErrorValue) > 0)
		{
			return false;
		}
		return true;
	}

	public static int[][] multiply(int[][] a, int[][] b)
	{
		if (a[0].length != b.length)
		{
			throw new IllegalArgumentException("矩阵列[" + a[0].length + "]行[" + b.length + "]数目不等，不能相乘");
		}
		int[][] res = new int[a.length][b[0].length];
		for (int i = 0; i < a.length; i++)
		{//rows of a
			for (int j = 0; j < b[0].length; j++)
			{//columns of b
				for (int k = 0; k < a[0].length; k++)
				{//columns of a = rows of b
					res[i][j] = res[i][j] + a[i][k] * b[k][j];
				}
			}
		}
		return res;
	}

	public static int[][] minus(int[][] a, int[][] b)
	{
		if (a.length != b.length && a[0].length != b[0].length)
		{
			return null;
		}
		int[][] res = new int[a.length][a[0].length];
		for (int i = 0; i < a.length; i++)
		{//rows of a
			for (int j = 0; j < a[0].length; j++)
			{//columns of b
				res[i][j] = a[i][j] - b[i][j];
			}
		}
		return res;
	}

	public static int[][] plus(int[][] a, int[][] b)
	{
		if (a.length != b.length && a[0].length != b[0].length)
		{
			return null;
		}
		int[][] res = new int[a.length][a[0].length];
		for (int i = 0; i < a.length; i++)
		{//rows of a
			for (int j = 0; j < a[0].length; j++)
			{//columns of b
				res[i][j] = a[i][j] + b[i][j];
			}
		}
		return res;
	}

	//计算一组数的大小比
	public static String caculateDXBi(String number)
	{
		int bigNumber = 0;
		int smallNumber = 0;
		String[] array = number.split(BACKSPACE);
		for (int i = 0; i < array.length; i++)
		{
			if (Integer.parseInt(array[i]) > 5)
			{
				bigNumber++;
			}
			else
			{
				smallNumber++;
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(bigNumber)).append(COLON).append(String.valueOf(smallNumber));
		return sb.toString();
	}

	/**
	 * 计算一组数的大小比    当number中大于avgNum为大   小于avgNum为小
	 * @param number
	 * @param avgNum
	 * @return
	 */
	public static String caculateDXBi(String number, int avgNum)
	{
		int bigNumber = 0;
		int smallNumber = 0;
		String[] array = number.split(BACKSPACE);
		for (int i = 0; i < array.length; i++)
		{
			if (Integer.parseInt(array[i]) > avgNum)
			{
				bigNumber++;
			}
			else
			{
				smallNumber++;
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(bigNumber)).append(COLON).append(String.valueOf(smallNumber));
		return sb.toString();
	}

	//计算一组数的奇偶比
	public static String caculateJOBi(String number)
	{
		int jiNumber = 0;
		int ouNumber = 0;
		String[] array = number.split(BACKSPACE);
		for (int i = 0; i < array.length; i++)
		{
			if (Integer.parseInt(array[i]) % 2 == 0)
			{
				ouNumber++;
			}
			else
			{
				jiNumber++;
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(jiNumber)).append(COLON).append(String.valueOf(ouNumber));
		return sb.toString();
	}

	public static int convertStringToInt(String number)
	{
		if (StringUtils.isBlank(number))
			return 0;
		else
		{
			//			if(number.)
			number = number.replace("+", "");
			return Integer.parseInt(number);
		}
	}

	public static void main(String[] args)
	{
		System.out.println(convertStringToInt("+23"));
	}

	public static String convertIntToString(int number)
	{
		if (number > 0)
			return "+" + number;
		return String.valueOf(number);
	}

	/**
	 * @Description 获取字符串转型后的和值
	 * @param strs
	 * @return int
	 */
	public static int getSumOfStr(String... strs) throws ClassCastException
	{
		int sum = 0;
		for (String str : strs)
			sum += Integer.valueOf(str);
		return sum;
	}

	// 存储结果的堆栈
	private Stack<Object> stack = new Stack<Object>();

	/**
	 * 获得指定数组从指定开始的指定数量的数据组合<br>
	 * 
	 * @param arr 指定的数组
	 * @param begin 开始位置
	 * @param num 获得的数量
	 */
	public void getSequence(Object[] arr, int begin, int num)
	{
		if (num == 0)
		{
			System.out.println(stack); // 找到一个结果
		}
		else
		{
			// 循环每个可用的元素
			for (int i = begin; i < arr.length; i++)
			{
				// 当前位置数据放入结果堆栈
				stack.push(arr[i]);
				// 将当前数据与起始位置数据交换
				swap(arr, begin, i);
				// 从下一个位置查找其余的组合
				getSequence(arr, begin + 1, num - 1);
				// 交换回来
				swap(arr, begin, i);
				// 去除当前数据
				stack.pop();
			}
		}
	}

	/**
	 * 交换2个数组的元素
	 * 
	 * @param arr 数组
	 * @param from 位置1
	 * @param to 位置2
	 */
	public static void swap(Object[] arr, int from, int to)
	{
		if (from == to)
		{
			return;
		}
		Object tmp = arr[from];
		arr[from] = arr[to];
		arr[to] = tmp;
	}

}
