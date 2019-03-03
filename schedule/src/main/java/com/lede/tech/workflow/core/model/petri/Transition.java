package com.lede.tech.workflow.core.model.petri;

import com.lede.tech.workflow.core.model.graph.Node;

/**迁移节点
 * @author mwlv
 */
public class Transition extends Node implements Cloneable
{
	public final static String PREFIX = "T";

	public Transition(String name)
	{
		super(name);
	}

	public Object clone()
	{
		Transition trans = new Transition(getName());
		return trans;
	}

	@Override
	public String toString()
	{
		return getName();
	}

}
