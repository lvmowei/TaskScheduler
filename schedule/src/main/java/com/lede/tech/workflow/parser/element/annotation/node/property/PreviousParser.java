package com.lede.tech.workflow.parser.element.annotation.node.property;

import com.lede.tech.workflow.parser.element.bean.Branch;

public class PreviousParser extends NextParser
{
	public PreviousParser(String target)
	{
		super(target);
	}

	//previous = "(n2, ([n2, (n99,[n98,n99]), n7], n4, n5))"
	//previous = "[n1, n2, n3, [n1, n2], (n5, n6)]"
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
				int pos = 1;
				if (expr.startsWith("("))
				{
					branch = new Branch(null, null, Branch.Type.Condition);
				}
				else if (expr.startsWith("["))
				{
					branch = new Branch(null, null, Branch.Type.Synchronize);
				}
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
}
