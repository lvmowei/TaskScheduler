package com.lede.tech.workflow.parser.element.bean;

import java.util.ArrayList;
import java.util.List;

public class Branch
{
	private String name;
	private String condition;
	private Type type;
	/**
	 * 父节点引用
	 */
	private Branch ancestor;
	private List<Branch> subBranch;

	public enum Type
	{
		/** 普通分支，like "n1" */
		None,
		/** 条件分支，like "(c99:n99,c97:n98)" */
		Condition,
		/** 同步分支，like "[n1, n2, n3]" */
		Synchronize,
	}

	public Branch(String name, String condition)
	{
		this.name = name;
		this.condition = condition;
		subBranch = new ArrayList<Branch>();
		type = Type.None;
	}

	public Branch(String name, String preCondition, Type type)
	{
		this(name, preCondition);
		this.type = type;
	}

	public boolean hasBranch()
	{
		return !(subBranch == null || subBranch.isEmpty());
	}

	public int getSubBranchSize()
	{
		return subBranch.size();
	}

	public Branch getSubBranch(int index)
	{
		if (index >= getSubBranchSize())
		{
			return null;
		}
		return subBranch.get(index);
	}

	public void addSubBranch(Branch branch)
	{
		subBranch.add(branch);
		branch.setAncestor(this);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<Branch> getSubBranch()
	{
		return subBranch;
	}

	public void setSubBranch(List<Branch> subBranch)
	{
		this.subBranch = subBranch;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public String getCondition()
	{
		return condition;
	}

	public void setCondition(String condition)
	{
		this.condition = condition;
	}

	public Branch getAncestor()
	{
		return ancestor;
	}

	public void setAncestor(Branch ancestor)
	{
		this.ancestor = ancestor;
	}

	public static String print(Branch branch)
	{
		if (branch == null)
		{
			return "空";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(branch.getCondition()).append(":").append(branch.getName()).append(":").append(branch.getType());
		if (!branch.hasBranch())
		{
			return sb.toString();
		}
		for (Branch b : branch.getSubBranch())
		{
			sb.append("【").append(print(b)).append("】");
		}
		return sb.toString();
	}
}
