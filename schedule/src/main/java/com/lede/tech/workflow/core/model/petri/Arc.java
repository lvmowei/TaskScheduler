package com.lede.tech.workflow.core.model.petri;

import com.lede.tech.workflow.core.model.graph.Edge;
import com.lede.tech.workflow.core.model.graph.Node;

/**有权重的边
 * @author mwlv
 */
public class Arc extends Edge
{
	private int weight;

	public Arc(String name, Node source, Node target)
	{
		super(name, source, target);
		weight = 1;
	}

	public Arc(Node source, Node target)
	{
		super(source, target);
		weight = 1;
	}

	public int getWeight()
	{
		return weight;
	}

	public void setWeight(int weight)
	{
		this.weight = weight;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
