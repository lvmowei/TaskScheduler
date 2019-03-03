package com.lede.tech.workflow.core.model.petri;

import com.lede.tech.workflow.core.model.graph.Edge;
import com.lede.tech.workflow.core.model.graph.Graph;
import com.lede.tech.workflow.core.model.graph.Node;
import com.lede.tech.workflow.core.model.petri.reachability.Mark;
import com.lede.tech.workflow.core.model.petri.reachability.Tree;
import com.lede.tech.workflow.util.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**由若干个库所Place和若干个迁移Transition组成的PetriNet网
 * @author mwlv
 */
public class PetriNet extends Graph
{
	private Status status;
	private Transition[] transitions;
	private Place[] places;

	public PetriNet(String name)
	{
		super(name);
		status = Status.INIT;
		transitions = new Transition[0];
		places = new Place[0];
	}

	public boolean isReady()
	{
		return status == Status.READY;
	}

	/**
	 * 初始化PetriNet
	 */
	public void setReady()
	{
		if (isReady())
		{
			return;
		}
		List<Transition> trans = new ArrayList<Transition>();
		List<Place> placs = new ArrayList<Place>();
		Node[] nodes = getNodes();
		for (Node node : nodes)
		{
			if (node == null)
			{
				continue;
			}
			else if (node instanceof Transition)
			{
				trans.add((Transition) node);
			}
			else if (node instanceof Place)
			{
				placs.add((Place) node);
			}
		}
		transitions = trans.toArray(new Transition[0]);
		places = placs.toArray(new Place[0]);
		Arrays.sort(transitions);
		Arrays.sort(places);
		status = Status.READY;
	}

	public Place getPlace(int i)
	{
		return places[i];
	}

	public Place getPlace(String name)
	{
		Node node = getNode(name);
		if (node != null && node instanceof Place)
		{
			return ((Place) node);
		}
		return null;
	}

	private int getPosition(Place place)
	{
		if (place == null)
		{
			return -1;
		}
		for (int i = 0; i < places.length; i++)
		{
			if (place.equals(places[i]))
			{
				return i;
			}
		}
		return -1;
	}

	public Transition getTransition(int i)
	{
		return transitions[i];
	}

	public Transition getTransition(String name)
	{
		Node node = getNode(name);
		if (node != null && node instanceof Transition)
		{
			return ((Transition) node);
		}
		return null;
	}

	private int getPosition(Transition transition)
	{
		if (transition == null)
		{
			return -1;
		}
		for (int i = 0; i < transitions.length; i++)
		{
			if (transition.equals(transitions[i]))
			{
				return i;
			}
		}
		return -1;
	}

	public Arc getArc(String name)
	{
		for (Edge edge : getEdges())
		{
			if (edge.getName().equals(name))
			{
				return (Arc) edge;
			}
		}
		return null;
	}

	private Arc getArc(String source, String target)
	{
		Edge edge = getEdge(source, target);
		if (edge != null && edge instanceof Arc)
		{
			return ((Arc) edge);
		}
		return null;
	}

	/////////////////////////////////////////////////////////////
	//////////////////////////////结构构造//////////////////////////
	/////////////////////////////////////////////////////////////

	protected Arc link(Node source, Node target)
	{
		return new Arc(source, target);
	}

	/**将trans节点开始的树中的节点和边添加到图中，递归
	 * @param trans
	 * @return
	 */
	public boolean addAncestor(Transition trans)
	{
		if (isReady())
		{
			log.error("流程模型[" + getName() + "]已经就绪，不能再添加前置结点");
			return false;
		}
		if (trans == null)
		{
			return false;
		}
		boolean flag = false;
		for (Edge edge : trans.getSource())
		{
			Place place = (Place) edge.getSource();
			flag = add(place.getName(), trans.getName(), null);
			//只有在Place到Transition的方向时才设置条件方法
			setDescriptor(place.getName(), trans.getName(), ((Arc) edge).getDescriptor());
			flag = addAncestor(place, trans);
		}
		return flag;
	}

	private boolean addAncestor(Place place, Transition descendant)
	{
		if (place == null)
		{
			return false;
		}
		boolean flag = false;
		for (Edge edge : place.getSource())
		{
			Transition trans = (Transition) edge.getSource();
			flag = add(null, trans.getName(), place.getName());
			if (trans.equals(descendant))
			{
				//防止循环引用
				continue;
			}
			flag = addAncestor(trans);
		}
		return flag;
	}

	/**将trans节点开始的树中的节点和边添加到图中，递归
	 * @param trans
	 * @return
	 */
	public boolean addDescendant(Transition trans)
	{
		if (isReady())
		{
			log.error("流程模型[" + getName() + "]已经就绪，不能再添加后继结点");
			return false;
		}
		if (trans == null)
		{
			return false;
		}
		boolean flag = false;
		for (Edge edge : trans.getTarget())
		{
			Place place = (Place) edge.getTarget();
			flag = add(null, trans.getName(), place.getName());
			flag = addDescendant(place, trans);
		}
		return flag;
	}

	private boolean addDescendant(Place place, Transition ancestor)
	{
		if (place == null)
		{
			return false;
		}
		boolean flag = false;
		for (Edge edge : place.getTarget())
		{
			Transition trans = (Transition) edge.getTarget();
			flag = add(place.getName(), trans.getName(), null);
			//只有在Place到Transition的方向时才设置条件方法
			setDescriptor(place.getName(), trans.getName(), ((Arc) edge).getDescriptor());
			if (trans.equals(ancestor))
			{
				//防止循环引用
				continue;
			}
			flag = addDescendant(trans);
		}
		return flag;
	}

	/**连接起两个迁移节点，根据petrinet的规则两个迁移节点中间要加上一个库所节点
	 * @param fromTrans
	 * @param toTrans
	 * @return
	 */
	public boolean join(String fromTrans, String toTrans)
	{
		if (isReady())
		{
			log.error("流程模型[" + getName() + "]已经就绪，不能链接结点");
			return false;
		}
		Transition from = getTransition(fromTrans);
		Transition to = getTransition(toTrans);
		if (from != null && to != null && isAccessible(from, to))
		{
			return false;
		}
		else
		{
			//如果节点相同，则为自己链接自己
			String place = Place.name(fromTrans, toTrans);
			add(null, fromTrans, place);
			add(place, toTrans, null);
		}
		return true;
	}

	private boolean isAccessible(Transition from, Transition target)
	{
		if (from == null || from.getTarget() == null)
		{
			return false;
		}
		boolean flag = false;
		for (Edge edge : from.getTarget())
		{
			Place place = (Place) edge.getTarget();
			flag = isAccessible(place, target) || flag;
		}
		return flag;
	}

	private boolean isAccessible(Place from, Transition target)
	{
		if (from == null || from.getTarget() == null)
		{
			return false;
		}
		boolean flag = false;
		for (Edge edge : from.getTarget())
		{
			Transition trans = (Transition) edge.getTarget();
			if (trans.equals(target))
			{
				return true;
			}
			else
			{
				flag = isAccessible(trans, target) || flag;
			}
		}
		return flag;
	}

	/**将节点和边添加到图中
	 * @param placeFrom	迁移节点前的库所
	 * @param transition 要添加的迁移节点 like 更新赔率1
	 * @param placeTo 迁移节点后的库所 like P更新赔率1-更新赔率1
	 * @return
	 */
	private boolean add(String placeFrom, String transition, String placeTo)
	{
		if (transition == null || placeFrom == null && placeTo == null)
		{
			return false;
		}
		boolean flag = false;
		if (getNode(transition) == null)
		{
			flag = addNode(new Transition(transition));
		}
		if (placeFrom != null)
		{
			//拷贝是为了避免内部数据独立，避免引用到外部数据
			if (getNode(placeFrom) == null)
			{
				addNode(new Place(placeFrom));
			}
			flag = addEdge(placeFrom, transition) || flag;
		}
		if (placeTo != null)
		{
			if (getNode(placeFrom) == null)
			{
				addNode(new Place(placeTo));
			}
			flag = addEdge(transition, placeTo) || flag;
		}
		return flag;
	}

	private boolean setDescriptor(String source, String target, String condition)
	{
		if (source == null || target == null)
		{
			return false;
		}
		Node src = getNode(source);
		Node dst = getNode(target);
		if (src instanceof Place && dst instanceof Transition)
		{
			Arc arc = getArc(source, target);
			if (arc != null)
			{
				arc.setDescriptor(condition);
				return true;
			}
		}
		return false;
	}

	/////////////////////////////////////////////////////////////
	////////////////////////////流程处理////////////////////////////
	/////////////////////////////////////////////////////////////

	/**
	 * 得到Input (Place OutPut) Matrix
	 * @return
	 */
	private int[][] getIncidenceMinusMatrix()
	{
		int[][] matrix = new int[transitions.length][places.length];
		for (int i = 0; i < transitions.length; i++)
		{
			for (int j = 0; j < places.length; j++)
			{
				Arc a = getArc(places[j].getName(), transitions[i].getName());
				if (a != null)
				{
					matrix[i][j] = a.getWeight();
				}
			}
		}
		return matrix;
	}

	/**
	 * 得到Output (Transition OutPut) Matrix
	 * @return
	 */
	private int[][] getIncidencePlusMatrix()
	{
		int[][] matrix = new int[transitions.length][places.length];
		for (int i = 0; i < transitions.length; i++)
		{
			for (int j = 0; j < places.length; j++)
			{
				Arc a = getArc(transitions[i].getName(), places[j].getName());
				if (a != null)
				{
					matrix[i][j] = a.getWeight();
				}
			}
		}
		return matrix;
	}

	/**
	 * 如果transTab中存在不应该进行的迁移，返回的数组中则存在负数
	 * @param transTab 要进行的迁移
	 * @param markTab 迁移前的状态
	 * @return 迁移后的状态
	 */
	private int[] firing(int[] transTab, int[] markTab)
	{
		if (transTab == null || markTab == null || transTab.length != transitions.length
				|| markTab.length != places.length)
		{
			return null;
		}
		int[][] transition = new int[1][transTab.length];
		int[][] marking = new int[1][markTab.length];
		transition[0] = transTab;
		marking[0] = markTab;
		int[][] matrixD = MathUtil.minus(getIncidencePlusMatrix(), getIncidenceMinusMatrix());
		int[][] res = MathUtil.plus(MathUtil.multiply(transition, matrixD), marking);
		for (int i = 0; i < res[0].length; i++)
		{
			//如果不能同时进行trans，则返回null
			if (res[0][i] < 0)
			{
				return null;
			}
		}
		return res[0];
	}

	/**
	 * 根据Tokens的集合返回对应的int[]表示其对应的状态,数组的长度是Places的长度
	 * @param tokens
	 * @return
	 */
	public int[] getMarking(Token[] tokens)
	{
		if (!isReady())
		{
			return null;
		}
		int[] marking = new int[places.length];
		for (int i = 0; i < marking.length; i++)
		{
			for (int j = 0; j < tokens.length; j++)
			{
				if (places[i].getName().equals(tokens[j].getPlace()))
				{
					marking[i]++;
				}
			}
		}
		return marking;
	}

	private int[] getTransition(String[] transition)
	{
		if (transition == null)
		{
			return null;
		}
		int[] trans = new int[transitions.length];
		for (int i = 0; i < transitions.length; i++)
		{
			for (String s : transition)
			{
				if (transitions[i].getName().equalsIgnoreCase(s))
				{
					trans[i] = 1;
				}
			}
		}
		return trans;
	}

	/**
	 * 根据令牌状态，判断指定的迁移会不会发生，如果会发生，则返回发生所需要的最小Token集合所表示的状态
	 * @param tokens
	 * @param transition
	 * @return
	 */
	public int[] isEnable(Token[] tokens, String... transition)
	{
		int[] mark = getMarking(tokens);//获取指定Tokens的分布
		int[] tran = getTransition(transition);//获取执行Transition的分布
		return isEnable(mark, tran);
	}

	/**
	 * 根据令牌状态，判断指定的迁移会不会发生，如果会发生，则返回发生后的状态，如果不会发生，则返回null
	 * @param tokens
	 * @param transition
	 * @return
	 */
	public int[] firing(Token[] tokens, String... transition)
	{
		int[] mark = getMarking(tokens);
		int[] tran = getTransition(transition);
		return firing(tran, mark);
	}

	private int[] isEnable(int[] mark, int[] trans)
	{
		int[] cond = preMarking(trans);
		for (int i = 0; i < mark.length; i++)
		{
			if (mark[i] < cond[i])
			{
				return null;
			}
		}
		return cond;
	}

	/**
	 * @param targetTrans 迁移节点的中文名字
	 * @return 返回要进行指定迁移所需要的令牌的集合
	 */
	public Token[] preMarking(String[] targetTrans)
	{
		int[] transTab = new int[transitions.length];
		for (String trans : targetTrans)
		{
			int pos = getPosition(getTransition(trans));
			if (pos >= 0)
			{
				transTab[pos] = 1; //需要执行的迁移节点在数组中的相应位置置1
			}
		}
		List<Token> lst = new ArrayList<Token>();
		int[] mark = preMarking(transTab);
		for (int i = 0; i < mark.length; i++)
		{
			for (int k = 0; k < mark[i]; k++) //由于目前所有边的权值都是1，所以一个库所节点的权值==其出度(出边条数)
			{
				String place = places[i].getName();
				lst.add(new Token(place));
			}
		}
		return lst.toArray(new Token[0]);
	}

	/**
	 * 返回进行指定操作所需要的状态
	 * 
	 * @param transTab
	 * @return
	 */
	private int[] preMarking(int[] transTab)
	{
		if (!isReady())
		{
			return null;
		}
		if (transTab == null || transTab.length != transitions.length)
		{
			return null;
		}
		int[] marking = new int[places.length];
		for (int i = 0; i < transTab.length; i++)
		{
			if (transTab[i] <= 0)
			{
				continue;
			}
			Edge[] edges = transitions[i].getSource();
			for (Edge edge : edges)
			{
				Arc arc = (Arc) edge;
				marking[getPosition((Place) arc.getSource())] += arc.getWeight(); //计算每个需要执行的迁移节点的前驱库所节点的权值（其实就是入边的权值,目前边的权值都是1）
			}
		}
		return marking;
	}

	/**
	 * 得到所有可能的下一次Transition，只计算边的权重，不判断边的条件
	 * 
	 * @param marking
	 * @return
	 */
	public int[] preFiring(int[] marking)
	{
		if (!isReady())
		{
			return null;
		}
		int[] transition = new int[transitions.length];
		for (int i = 0; i < places.length; i++)
		{
			if (marking[i] <= 0)
			{
				continue;
			}
			Edge[] edges = places[i].getTarget();
			Arrays.sort(edges);
			for (Edge edge : edges)
			{
				Arc arc = (Arc) edge;
				if (arc == null)
				{
					continue;
				}
				if (marking[i] < arc.getWeight())
				{
					continue;
				}
				Transition tran = (Transition) edge.getTarget();
				transition[getPosition(tran)] = 1;
			}
		}
		for (int i = 0; i < transition.length; i++)
		{
			if (transition[i] <= 0)
			{
				continue;
			}
			Edge[] edges = transitions[i].getSource();
			for (Edge edge : edges)
			{
				Arc arc = (Arc) edge;
				Place source = (Place) arc.getSource();
				if (marking[getPosition(source)] < arc.getWeight())
				{
					transition[i] = 0;
					break;
				}
			}
		}
		return transition;
	}

	/**
	 * 根据当前的情况生成下次能进行的transition所需的所有条件判断
	 * 
	 * @param marking
	 * @return
	 */
	public Arc[] preArc(int[] marking)
	{
		if (!isReady())
		{
			return null;
		}
		List<Arc> lst = new ArrayList<Arc>();
		for (int i = 0; i < places.length; i++)
		{
			if (marking[i] <= 0)
			{
				continue;
			}
			Edge[] edges = places[i].getTarget();
			Arrays.sort(edges);
			for (Edge edge : edges)
			{
				Arc arc = (Arc) edge;
				if (arc == null)
				{
					continue;
				}
				if (marking[i] < arc.getWeight())
				{
					continue;
				}
				if (!lst.contains(arc))
				{
					lst.add(arc);
				}
			}
		}
		return lst.toArray(new Arc[0]);
	}

	/**
	 * 根据参数中为真的条件边，很给定的状态，进行计算，返回下一次进行的transition
	 * 
	 * @param mark
	 * @param arcs 为真的条件
	 * @return
	 */
	public int[] preFiring(int[] mark, List<String> arcs)
	{
		if (!isReady())
		{
			return null;
		}
		int[] marking = Arrays.copyOf(mark, mark.length);
		int[] transition = preFiring(marking);
		for (int i = 0; i < transition.length; i++)
		{
			if (transition[i] <= 0)
			{
				continue;
			}
			boolean firing = true;
			Edge[] edges = transitions[i].getSource();
			for (Edge edge : edges)
			{
				Arc arc = (Arc) edge;
				int pos = getPosition((Place) arc.getSource());
				if (!arcs.contains(arc.getName()) || marking[pos] < arc.getWeight())
				{
					firing = false;
					break;
				}
			}
			if (firing)
			{
				for (Edge edge : edges)
				{
					Arc arc = (Arc) edge;
					int pos = getPosition((Place) arc.getSource());
					marking[pos] -= arc.getWeight();
				}
			}
			else
			{
				transition[i] = 0;
			}
		}
		return transition;
	}

	/////////////////////////////////////////////////////////////
	////////////////////////////流程分析////////////////////////////
	/////////////////////////////////////////////////////////////

	public Tree reachability(int[] marking)
	{
		if (!isReady())
		{
			return null;
		}
		if (marking == null || marking.length != places.length)
		{
			throw new IllegalArgumentException("参数有误，暂不能进行可达性分析");
		}
		Mark start = new Mark(marking);
		Tree root = new Tree(null, start);

		List<Mark> markSet = new ArrayList<Mark>();
		List<Mark> current = new ArrayList<Mark>();
		markSet.add(start);
		current.add(start);

		while (!current.isEmpty())
		{
			Mark from = current.remove(0);
			Tree sub = root.get(from);
			if (sub == null)
			{
				throw new IllegalArgumentException("节点在树中未找到[" + from.toString() + "]");
			}
			else if (!sub.getMark().equals(from))
			{
				throw new IllegalArgumentException(
						"树冲突(目标" + from.toString() + ")(结果" + sub.getMark().toString() + ")");
			}
			int[] trans = preFiring(from.mark());
			for (int i = 0; i < trans.length; i++)
			{

				if (trans[i] > 0)
				{
					int[] transTab = new int[transitions.length];
					transTab[i] = trans[i];
					Mark next = Mark.compare(firing(transTab, from.mark()), from);
					int index = markSet.indexOf(next);
					if (index < 0)
					{
						markSet.add(next);
						current.add(next);
					}
					sub.add(from, transitions[i].getName(), next);
				}
			}
		}
		return root;
	}

	private String getName(String name)
	{
		if (name == null || name.length() <= 0)
		{
			return null;
		}
		String res = "";
		if (name.length() > 6)
		{
			res += name.charAt(0);
			res += "..";
			res += name.substring(name.length() - 3);
		}
		else
		{
			res = name;
		}
		return res;
	}

	public String analyze()
	{
		if (!isReady())
		{
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("\n======================== Input (Place OutPut) ========================\n");
		for (int i = 0; i < places.length; i++)
		{
			sb.append("\t");
			sb.append(getName(places[i].getName()));
		}
		sb.append("\n");
		int[][] matrixMinus = getIncidenceMinusMatrix();
		for (int i = 0; i < transitions.length; i++)
		{
			for (int j = 0; j < places.length; j++)
			{
				if (j == 0)
				{
					sb.append(getName(transitions[i].getName()));
				}
				sb.append("\t");
				sb.append(matrixMinus[i][j]);
			}
			sb.append("\n");
		}
		sb.append("=======================================================");
		sb.append("\n======================== Output (Transition OutPut) =======================\n");
		for (int i = 0; i < places.length; i++)
		{
			sb.append("\t");
			sb.append(getName(places[i].getName()));
		}
		sb.append("\n");
		int[][] matrixPlus = getIncidencePlusMatrix();
		for (int i = 0; i < transitions.length; i++)
		{
			for (int j = 0; j < places.length; j++)
			{
				if (j == 0)
				{
					sb.append(getName(transitions[i].getName()));
				}
				sb.append("\t");
				sb.append(matrixPlus[i][j]);
			}
			sb.append("\n");
		}
		sb.append("=======================================================");
		sb.append("\n======================== Arcs =======================\n");
		for (Edge edge : getEdges())
		{
			Arc a = (Arc) edge;
			sb.append(edge.getName()).append("\t\t[Weight = ").append(a.getWeight()).append("][Descriptor = ")
					.append(a.getDescriptor()).append("]\n");
		}
		sb.append("=======================================================");
		sb.append("\n====================== Transition =====================\n");
		for (Transition t : transitions)
		{
			sb.append(t.getName()).append("\t\t[Descriptor = ").append(t.getDescriptor()).append("]\n");
		}
		sb.append("=======================================================\n");
		return sb.toString();
	}

	public Transition[] getTransitions()
	{
		return transitions;
	}

	public void setTransitions(Transition[] transitions)
	{
		this.transitions = transitions;
	}

	public Place[] getPlaces()
	{
		return places;
	}

	public void setPlaces(Place[] places)
	{
		this.places = places;
	}

}
