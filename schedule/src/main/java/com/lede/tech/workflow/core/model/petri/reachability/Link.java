package com.lede.tech.workflow.core.model.petri.reachability;

public class Link
{
	private String name;
	private Tree tree;

	public Link(String name, Tree tree)
	{
		this.name = name;
		this.tree = tree;
	}

	public String getName()
	{
		return name;
	}

	public Tree getTree()
	{
		return tree;
	}

	public boolean equals(Object o)
	{
		return name.equals(((Link) o).name) && tree.equals(((Link) o).tree);
	}

	public int hashCode()
	{
		return (name + tree.hashCode()).hashCode();
	}
}
