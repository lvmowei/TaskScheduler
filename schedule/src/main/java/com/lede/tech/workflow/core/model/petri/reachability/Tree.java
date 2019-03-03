package com.lede.tech.workflow.core.model.petri.reachability;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Tree
{
	private Tree root;
	private Mark mark;
	private List<Link> links;

	public Tree(Tree root, Mark mark)
	{
		this.root = root;
		this.mark = mark;
		links = new ArrayList<Link>();
	}

	public Mark getMark()
	{
		return mark;
	}

	public Tree get(Mark mark)
	{
		Vector<Tree> vector = new Vector<Tree>();
		if (root == null)
		{
			vector.add(this);
		}
		else
		{
			vector.add(root);
		}
		while (!vector.isEmpty())
		{
			Tree tree = vector.remove(0);
			if (tree.mark == null)
			{
				continue;
			}
			if (tree.mark.equals(mark))
			{
				return tree;
			}
			if (tree.links == null || tree.links.isEmpty())
			{
				continue;
			}

			for (Link link : tree.links)
			{
				Tree t = link.getTree();
				if (t != null && !vector.contains(t) && !t.equals(tree))
				{
					vector.add(t);
				}
			}
		}
		return null;
	}

	public boolean add(Mark current, String name, Mark next)
	{
		Tree source = get(current);
		if (source == null)
		{
			return false;
		}
		Tree target = get(next);
		if (target == null)
		{
			if (source.root == null)
			{
				target = new Tree(source, next);
			}
			else
			{
				target = new Tree(source.root, next);
			}
		}

		Link l = new Link(name, target);
		if (!source.links.contains(l))
		{
			source.links.add(l);
			return true;
		}
		return false;
	}

	public boolean equals(Object o)
	{
		return mark.equals(((Tree) o).mark);
	}

	public int hashCode()
	{
		return mark.hashCode();
	}

	public String view(String prefix)
	{
		String pref = prefix + mark.toString();
		List<Link> loop = new ArrayList<Link>();
		StringBuilder sb = new StringBuilder();
		for (Link l : links)
		{
			if (!l.getTree().mark.equals(mark))
			{
				loop.add(l);
			}
			else
			{
				sb.append(pref + l.getName() + mark.toString()).append("\n");
			}
		}
		//System.out.println();
		if (loop == null || loop.isEmpty())
		{
			return sb.toString() + pref;
		}
		int i = 0;
		for (Link l : loop)
		{
			sb.append(l.getTree().view(pref + l.getName()));
			if (i < loop.size() - 1)
			{
				sb.append("\n");
			}
			i++;
		}
		return sb.toString();
	}
}
