package com.lede.tech.workflow.core.model.petri.reachability;

public class Mark
{
	private int[] mark;

	public Mark(int[] mark)
	{
		this.mark = mark;
	}

	public int[] mark()
	{
		int[] sim = new int[mark.length];
		for (int i = 0; i < sim.length; i++)
		{
			if (mark[i] < 0)
			{
				sim[i] = 10000;
			}
			else
			{
				sim[i] = mark[i];
			}
		}
		return sim;
	}

	public int[] getMark()
	{
		return mark;
	}

	public static Mark compare(int[] res, Mark m)
	{
		int[] next = new int[m.mark.length];
		boolean greater = true;
		for (int i = 0; i < m.mark.length; i++)
		{
			if (m.mark[i] < 0)
			{
				next[i] = -1;
			}
			else
			{
				next[i] = res[i];
			}
			if (next[i] >= 0 && m.mark[i] >= 0 && next[i] < m.mark[i])
			{
				greater = false;
			}
		}
		if (greater)
		{
			for (int i = 0; i < m.mark.length; i++)
			{
				if (next[i] >= 0 && m.mark[i] >= 0 && next[i] > m.mark[i])
				{
					next[i] = -1;
				}
			}
		}
		return new Mark(next);
	}

	public boolean equals(Object o)
	{
		Mark m = (Mark) o;
		if (m == null || mark.length != m.mark.length)
		{
			return false;
		}
		for (int i = 0; i < mark.length; i++)
		{
			if (mark[i] != m.mark[i])
			{
				return false;
			}
		}
		return true;
	}

	public int hashCode()
	{
		String str = "";
		for (int i = 0; i < mark.length; i++)
		{
			str += mark[i];
		}
		return str.hashCode();
	}

	public String toString()
	{
		String str = "[";
		for (int i = 0; i < mark.length; i++)
		{
			if (i > 0)
			{
				str += ", ";
			}
			str += mark[i];
		}
		return str + "]";
	}
}
