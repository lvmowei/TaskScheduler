package com.lede.tech.workflow.core.model.graph;

import java.util.HashSet;

/**
 * 图中一个节点
 * @author mwlv
 */
public class Node extends Component
{
	/**
	 * 入该节点的边
	 */
	private HashSet<Edge> source;
	/**
	 * 出该节点的边
	 */
	private HashSet<Edge> target;

	public Node(String name)
	{
		super(name);
		source = new HashSet<Edge>();
		target = new HashSet<Edge>();
	}

	public Type getType()
	{
		return Type.NODE;
	}

	public boolean addSource(Edge edge)
	{
		return source.add(edge);
	}

	public boolean addTarget(Edge edge)
	{
		return target.add(edge);
	}

	public Edge[] getSource()
	{
		return source.toArray(new Edge[0]);
	}

	public Edge[] getTarget()
	{
		return target.toArray(new Edge[0]);
	}
}
