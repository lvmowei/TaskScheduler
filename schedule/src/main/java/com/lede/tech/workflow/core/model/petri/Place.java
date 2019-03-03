package com.lede.tech.workflow.core.model.petri;

import com.lede.tech.workflow.core.model.graph.Node;

/**库所节点
 * @author mwlv
 */
public class Place extends Node implements Cloneable
{
	public final static String PREFIX = "P";

	public final static Place START = new Place("PLACE_START");
	public final static Place END = new Place("PLACE_END");

	public Place(String name)
	{
		super(name);
	}

	public static String name(String from, String to)
	{
		return PREFIX + from + "-" + to;
	}

	public Object clone()
	{
		Place trans = new Place(getName());
		return trans;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
