package com.lede.tech.workflow.core.model.graph;

import java.util.Arrays;
import java.util.HashSet;

/**
 * 由若干节点Node和若干边Edge组成的图
 * @author mwlv
 */
public class Graph extends Component
{
	private HashSet<Node> nodes;
	private HashSet<Edge> edges;

	public Graph(String name)
	{
		super(name);
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();
	}

	public Graph(String name, Node[] nodes, Edge[] edges)
	{
		this(name);
		this.nodes.addAll(Arrays.asList(nodes));
		this.edges.addAll(Arrays.asList(edges));
	}

	public Node getNode(String name)
	{
		if (name == null)
		{
			return null;
		}
		for (Node node : nodes)
		{
			if (node.getName().equals(name))
			{
				return node;
			}
		}
		return null;
	}

	public Edge getEdge(String source, String target)
	{
		Node src = getNode(source);
		Node dst = getNode(target);
		if (src == null || dst == null)
		{
			return null;
		}
		for (Edge edge : src.getTarget())
		{
			if (edge != null && edge.getTarget() != null && edge.getTarget().equals(dst))
			{
				return edge;
			}
		}
		return null;
	}

	public Node[] getNodes()
	{
		return nodes.toArray(new Node[0]);
	}

	public Edge[] getEdges()
	{
		return edges.toArray(new Edge[0]);
	}

	protected Edge link(Node source, Node target)
	{
		return new Edge(source, target);
	}

	/**向图中添加source到target的一条边
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean addEdge(String source, String target)
	{
		if (source == null || target == null)
		{
			return false;
		}
		Node src = getNode(source);
		Node dst = getNode(target);
		if (src == null || dst == null)
		{
			return false;
		}
		Edge edge = getEdge(source, target);
		if (edge == null)
		{
			edge = link(src, dst);
			addEdge(edge);
		}
		else
		{
			edge.setSource(src);
			edge.setTarget(dst);
		}
		src.addTarget(edge);
		dst.addSource(edge);
		return true;
	}

	protected boolean addEdge(Edge edge)
	{
		return edges.add(edge);
	}

	/**向图中添加节点
	 * @param node
	 * @return
	 */
	protected boolean addNode(Node node)
	{
		return nodes.add(node);
	}

	public Type getType()
	{
		return Type.GRAPH;
	}

	public enum Status
	{
		INIT, READY
	}
}
