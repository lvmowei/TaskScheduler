package com.lede.tech.workflow.core.model.graph;

/**
 * 连接两个Node节点的一条有向边
 * @author mwlv
 */
public class Edge extends Component
{
	private final static String PREFFIX_EDGE = "E";

	/**
	 * 源节点
	 */
	private Node source;
	/**
	 * 目标节点
	 */
	private Node target;

	private Edge(String name)
	{
		super(name);
	}

	public Edge(String name, Node source, Node target)
	{
		this(name);
		this.source = source;
		this.target = target;
	}

	public Edge(Node source, Node target)
	{
		this(name(source.getName(), target.getName()));
		this.source = source;
		this.target = target;
	}

	public Type getType()
	{
		return Type.EDGE;
	}

	protected static String name(String source, String target)
	{
		return PREFFIX_EDGE + "[" + source + "_" + target + "]";
	}

	public Node getTarget()
	{
		return target;
	}

	public Node getSource()
	{
		return source;
	}

	public void setSource(Node source)
	{
		this.source = source;
	}

	public void setTarget(Node target)
	{
		this.target = target;
	}
}
