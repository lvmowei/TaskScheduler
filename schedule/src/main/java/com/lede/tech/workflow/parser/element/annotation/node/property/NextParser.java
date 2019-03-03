package com.lede.tech.workflow.parser.element.annotation.node.property;

import com.lede.tech.workflow.parser.element.AbstractParser;
import com.lede.tech.workflow.parser.element.annotation.AbstractNameParser;
import com.lede.tech.workflow.parser.element.bean.Branch;
import org.apache.commons.lang.StringUtils;

import java.util.Stack;
import java.util.regex.Pattern;

public class NextParser extends AbstractParser
{
	protected static Pattern patternName = Pattern.compile(AbstractNameParser.PATTERN_NAME);
	protected static Pattern patternElement = Pattern.compile("\\[(.*?,)*(.*?)\\]|\\((.*?,)*(.*?)\\)");

	public NextParser(String target)
	{
		super(target);
	}

	protected static int getClosurePos(char prefix, int from, String expr)
	{
		if (expr == null || expr.length() <= 1 || !(prefix == '(' || prefix == '['))
		{
			return -1;
		}
		char closure = prefix == '(' ? ')' : ']';
		int start = expr.indexOf(prefix, from);
		if (start < 0)
		{
			return -1;
		}
		Stack<Character> stack = new Stack<Character>();
		int pos = start + 1;
		stack.push(prefix);
		while (pos < expr.length())
		{
			if (expr.charAt(pos) == prefix)
			{
				stack.push(prefix);
			}
			else if (expr.charAt(pos) == closure)
			{
				stack.pop();
				if (stack.isEmpty())
				{
					return pos;
				}
			}
			pos++;
		}
		return -1;
	}

	//next = "(c1:n2, c2:(c3:[n2, (c99:n99,c97:[n98,n99]), n7], c4:n4, c5:n5))"
	//next = "[n1, n2, n3, [n1, n2], (c2:n5, c3:n6)]"
	public Branch parseBranch(String expr)
	{
		Branch branch = null;
		if (patternName.matcher(expr).matches())
		{
			branch = new Branch(expr, null);
		}
		else if (patternElement.matcher(expr).matches())
		{
			try
			{
				if (expr.startsWith("("))
				{
					int pos = 1;
					branch = new Branch(null, null, Branch.Type.Condition);
					while (pos < expr.length())
					{
						int colonPos = expr.indexOf(':', pos);
						String condition = expr.substring(pos, colonPos);
						Branch sub;
						char parenthese = expr.charAt(colonPos + 1);
						if (parenthese == '(' || parenthese == '[')
						{
							int parentheseClosure = getClosurePos(parenthese, colonPos, expr);
							String subExpr = expr.substring(colonPos + 1, parentheseClosure + 1);
							sub = parseBranch(subExpr);
							pos = parentheseClosure + 1;
						}
						else
						{
							int commaPos = expr.indexOf(',', colonPos);
							if (commaPos < 0)
							{//到尾部了
								commaPos = expr.length() - 1;
							}
							String node = expr.substring(colonPos + 1, commaPos);
							sub = parseBranch(node);
							pos = commaPos;
						}
						if (pos < expr.length() - 1 && expr.charAt(pos) != ',')
						{
							throw new Exception("不符合语法 - " + expr);
						}
						pos++;
						sub.setCondition(condition);
						branch.addSubBranch(sub);
					}
				}
				else if (expr.startsWith("["))
				{
					int pos = 1;
					branch = new Branch(null, null, Branch.Type.Synchronize);
					while (pos < expr.length())
					{
						Branch sub;
						char parenthese = expr.charAt(pos);
						if (parenthese == '(' || parenthese == '[')
						{
							int parentheseClosure = getClosurePos(parenthese, pos, expr);
							String subExpr = expr.substring(pos, parentheseClosure + 1);
							sub = parseBranch(subExpr);
							pos = parentheseClosure + 1;
						}
						else
						{
							int commaPos = expr.indexOf(',', pos);
							if (commaPos < 0)
							{//到尾部了
								commaPos = expr.length() - 1;
							}
							String node = expr.substring(pos, commaPos);
							sub = parseBranch(node);
							pos = commaPos;
						}
						if (pos < expr.length() - 1 && expr.charAt(pos) != ',')
						{
							throw new IllegalArgumentException("不符合语法[" + expr + "]");
						}
						pos++;
						branch.addSubBranch(sub);
					}
				}
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("表达式语法错误【" + expr + "】", e);
			}
		}
		else
		{
			throw new IllegalArgumentException("表达式有误【" + expr + "】");
		}
		return branch;
	}

	public Object parse()
	{
		String expr = ((String) target);
		if (StringUtils.isBlank((String) target))
		{
			return null;
		}
		expr = ((String) target).trim().replaceAll("\\s*", "");
		return parseBranch(expr);
	}

}
