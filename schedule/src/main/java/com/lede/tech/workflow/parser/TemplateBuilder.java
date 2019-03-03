package com.lede.tech.workflow.parser;

import com.lede.tech.workflow.core.engine.bean.Descriptor;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.core.engine.bean.descriptor.ConditionDescriptor;
import com.lede.tech.workflow.core.engine.bean.descriptor.TransitionDescriptor;
import com.lede.tech.workflow.core.model.graph.Node;
import com.lede.tech.workflow.core.model.petri.Arc;
import com.lede.tech.workflow.core.model.petri.PetriNet;
import com.lede.tech.workflow.core.model.petri.Place;
import com.lede.tech.workflow.core.model.petri.Transition;
import com.lede.tech.workflow.parser.element.bean.*;
import com.lede.tech.workflow.util.LogPrefix;
import com.lede.tech.workflow.util.NStringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class TemplateBuilder
{
	private Log log = LogFactory.getLog(getClass());

	private int serial = 0;//模板内唯一节点自增编号

	private Class<? extends ProcessTemplate> template;

	/**
	 * 引擎模板中的node注解实例
	 */
	private Set<NodeBean> nodeList;
	/**
	 * 引擎模板中的condition注解实例
	 */
	private Set<ConditionBean> conditionList;
	/**
	 * 引擎模板中的delay注解实例
	 */
	private Set<DelayBean> delayList;

	private Descriptor descriptor;

	public TemplateBuilder(Class<? extends ProcessTemplate> template)
	{
		this.template = template;

		nodeList = new HashSet<NodeBean>();
		conditionList = new HashSet<ConditionBean>();
		delayList = new HashSet<DelayBean>();

		descriptor = new Descriptor();
	}

	/**根据Node注解名称获取其注解的方法
	 * @param name
	 * @return
	 */
	protected Method getActionMethod(String name)
	{
		if (StringUtils.isBlank(name))
		{
			return null;
		}
		for (NodeBean bean : nodeList)
		{
			if (bean.getName().equalsIgnoreCase(name))
			{
				return bean.getMethod();
			}
		}
		return null;
	}

	/**根据Condition注解名字获取其注解的方法
	 * @param name
	 * @return
	 */
	protected Method getConditionMethod(String name)
	{
		if (StringUtils.isBlank(name))
		{
			return null;
		}
		for (ConditionBean bean : conditionList)
		{
			if (bean.getName().equalsIgnoreCase(name))
			{
				return bean.getMethod();
			}
		}
		return null;
	}

	protected Method getDelayMethod(String name)
	{
		if (StringUtils.isBlank(name))
		{
			return null;
		}
		for (DelayBean bean : delayList)
		{
			if (bean.getName().equalsIgnoreCase(name))
			{
				return bean.getMethod();
			}
		}
		return null;
	}

	public boolean addBean(AbstractBean bean)
	{
		boolean flag = false;
		if (bean instanceof NodeBean)
		{
			flag = nodeList.add((NodeBean) bean);
			if (log.isDebugEnabled())
			{
				NodeBean node = (NodeBean) bean;
				if (node.getNext() != null)
				{
					log.debug("[" + node.getName() + "]下游 =" + Branch.print(node.getNext()));
				}
				if (node.getPrevious() != null)
				{
					log.debug("[" + node.getName() + "]上游 =" + Branch.print(node.getPrevious()));
				}
			}
		}
		else if (bean instanceof ConditionBean)
		{
			flag = conditionList.add((ConditionBean) bean);
		}
		else if (bean instanceof DelayBean)
		{
			flag = delayList.add((DelayBean) bean);
		}
		return flag;
	}

	/**
	 * @param type 0:Place 1:Transition
	 * @return
	 */
	private String getName(int type)
	{
		String str = "";
		if (type == 0)
		{
			str += Place.PREFIX;
		}
		else
		{
			str += Transition.PREFIX;
		}
		str += "R";
		if (serial > 999)
		{
			throw new IllegalArgumentException("节点编号超出最大限制999");
		}
		int num = serial++;
		for (int i = 100; i > 0; i /= 10)
		{
			str += num / i;
			num = num % i;
		}
		return str;
	}

	/**
	 * @param from
	 * @param to
	 * @param inverse true:to->from 向前连接
	 * @param condition
	 */
	private void link(Node from, Node to, boolean inverse, String condition)
	{
		Arc arc = null;
		if (!inverse)
		{
			arc = new Arc(from, to);
			from.addTarget(arc);
			to.addSource(arc);
		}
		else
		{
			arc = new Arc(to, from);
			to.addTarget(arc);
			from.addSource(arc);
		}
		if (condition != null)
		{
			ConditionDescriptor condDesc = getDescriptor(arc.getName(), condition);
			descriptor.add(condDesc);
			arc.setDescriptor(condition);
		}
	}

	/**递归构造从node节点之后或之前的相关联的图
	 * @param branch
	 * @param inverse true:processPrevious false:processNext
	 * @return
	 */
	private Node process(Branch branch, boolean inverse)
	{
		if (branch == null)
		{
			return null;
		}
		if (branch.getType() == Branch.Type.None)
		{
			Transition trans = new Transition(branch.getName());
			Method action = getActionMethod(trans.getName());
			if (action == null)
			{
				throw new IllegalArgumentException("不存在节点对应的动作[" + trans.getName() + "]");
			}
			return trans;
		}
		else if (StringUtils.isNotBlank(branch.getName()))
		{
			throw new IllegalArgumentException("空分支不应有ID[" + branch.getName() + "]");
		}

		Node res = null;
		if (branch.getType() == Branch.Type.Condition)
		{
			res = new Place(getName(0));
			for (Branch sub : branch.getSubBranch())
			{
				Node target = process(sub, inverse);
				if (target == null)
				{
					throw new IllegalArgumentException("存在空分支[" + branch.getType() + "]");
				}
				//				ConditionDescriptor condDesc = getDescriptor(sub.getCondition());
				//				descriptor.add(condDesc);
				//				if (condDesc == null && !inverse)
				//				{
				//					throw new IllegalArgumentException("不存在条件方法[" + sub.getCondition() + "]");
				//				}
				if (target instanceof Transition)
				{
					link(res, target, inverse, sub.getCondition());
				}
				else if (target instanceof Place)
				{
					Transition t = new Transition(getName(1));
					link(res, t, inverse, sub.getCondition());
					link(t, target, inverse, null);
				}
				else
				{
					throw new IllegalArgumentException("未知种类的节点类型");
				}
			}
		}
		else if (branch.getType() == Branch.Type.Synchronize)
		{
			res = new Transition(getName(1));
			for (Branch sub : branch.getSubBranch())
			{
				Node target = process(sub, inverse);
				if (target == null)
				{
					throw new IllegalArgumentException("存在空分支[" + branch.getType() + "]");
				}
				if (target instanceof Transition)
				{
					Place p = new Place(getName(0));
					link(res, p, inverse, null);
					link(p, target, inverse, null);
				}
				else if (target instanceof Place)
				{
					link(res, target, inverse, null);
				}
				else
				{
					throw new IllegalArgumentException("未知种类的节点类型");
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("分支存在非法类型[" + branch.getType() + "]");
		}
		return res;
	}

	private Transition processNext(NodeBean from)
	{
		Branch next = from.getNext();
		if (next == null)
		{
			return null;
		}
		Node node = process(next, false);
		if (node instanceof Place)
		{
			Transition res = new Transition(from.getName());
			link(res, (Place) node, false, null);
			return res;
		}
		else
		{
			node.setName(from.getName());
			return (Transition) node;
		}
	}

	private Transition processPrevious(NodeBean to)
	{
		Branch previous = to.getPrevious();
		if (previous == null)
		{
			return null;
		}
		Node node = process(previous, true);
		if (node instanceof Place)
		{
			Transition res = new Transition(to.getName());
			link((Place) node, res, false, null);
			return res;
		}
		else
		{
			node.setName(to.getName());
			return (Transition) node;
		}
	}

	/**组成图<p>
	 * 我们用注解配置的Node节点都是迁移节点<p>
	 * 迁移节点和库所节点都是相间隔的，中间用有向弧连接<p>
	 * @return
	 */
	public TemplateBean process()
	{
		PetriNet net = new PetriNet(template.getPackage().getName() + "." + template.getName());
		if (LogPrefix.ifPrint())
		{
			log.info("TemplateBuilder:process:template:" + template.getName() + " NodeList is :"
					+ NStringUtil.combine(nodeList.toArray(), null));
		}
		//先组装分裂，聚合节点
		//分裂节点，比如next = "[开奖0, 开奖1]"
		//聚合节点，比如previous = "[开奖0, 开奖1]"
		for (NodeBean nodeBean : nodeList)
		{
			Branch next = nodeBean.getNext();
			if (next != null && next.getType() != Branch.Type.None)
			{
				if (log.isDebugEnabled())
				{
					log.debug("处理分裂节点分支[" + nodeBean.getName() + "]下游");
				}
				net.addDescendant(processNext(nodeBean));
			}
			Branch previous = nodeBean.getPrevious();
			if (previous != null && previous.getType() != Branch.Type.None)
			{
				if (log.isDebugEnabled())
				{
					log.debug("处理聚合节点分支[" + nodeBean.getName() + "]上游");
				}
				net.addAncestor(processPrevious(nodeBean));
			}
		}
		//处理有普通上下游节点的节点
		//比如next = "开奖0" 或 previous = "开奖0"
		for (NodeBean nodeBean : nodeList)
		{
			Branch next = nodeBean.getNext();
			if (next != null && next.getType() == Branch.Type.None)
			{
				if (log.isDebugEnabled())
				{
					log.debug("处理普通节点分支[" + nodeBean.getName() + "]下游[" + next.getName() + "]");
				}
				if (!net.join(nodeBean.getName(), next.getName()))
				{
					throw new IllegalArgumentException(
							"结构分支定义有冲突[节点" + nodeBean.getName() + " 下至 " + next.getName() + "]");
				}
			}
			//处理上游
			Branch previous = nodeBean.getPrevious();
			if (previous != null && previous.getType() == Branch.Type.None)
			{
				if (log.isDebugEnabled())
				{
					log.debug("处理普通节点分支[" + nodeBean.getName() + "]上游[" + previous.getName() + "]");
				}
				if (!net.join(previous.getName(), nodeBean.getName()))
				{
					throw new IllegalArgumentException(
							"结构分支定义有冲突[节点" + nodeBean.getName() + " 上至" + previous.getName() + "]");
				}
			}
		}

		//再组装普通节点，加上条件延时等
		for (NodeBean nodeBean : nodeList)
		{
			if (log.isDebugEnabled())
			{
				log.debug("处理无分支普通节点[" + nodeBean.getName() + "]");
			}
			//给节点注入方法
			//条件节点的条件方法注入 是在 处理 Branch时进行的。没有放在下面进行，是因为有在next或者previous中由于需要自动生成的分裂，聚合边上需要有条件
			Transition trans = net.getTransition(nodeBean.getName());
			if (trans == null)
			{
				throw new IllegalArgumentException("存在未使用的节点[" + nodeBean.getName() + "]");
			}
			TransitionDescriptor transDesc = getDescriptor(nodeBean);
			trans.setDescriptor(transDesc.getName());
			descriptor.add(transDesc);
		}
		//给没有库所的动作节点加上库所
		for (NodeBean nodeBean : nodeList)
		{
			Transition t = net.getTransition(nodeBean.getName());
			if (t.getSource().length <= 0)
			{
				link(t, new Place(getName(0)), true, null);
				net.addAncestor(t);
			}
			if (t.getTarget().length <= 0)
			{
				link(t, new Place(getName(0)), false, null);
				net.addDescendant(t);
			}
		}

		//注册模板
		net.setReady();
		if (log.isDebugEnabled())
		{
			log.debug(net.analyze());
		}

		TemplateBean bean = new TemplateBean(template, net, descriptor);
		return bean;
	}

	private ConditionDescriptor getDescriptor(String edgeName, String condName)
	{
		ConditionDescriptor condDesc = new ConditionDescriptor(edgeName);
		Method condition = getConditionMethod(condName);
		condDesc.setCondition(condition);
		return condDesc;
	}

	private TransitionDescriptor getDescriptor(NodeBean nodeBean)
	{
		TransitionDescriptor transDesc = new TransitionDescriptor(nodeBean.getName());
		transDesc.setFixedDelay(nodeBean.getFixedDelay());
		transDesc.setRetryDelay(nodeBean.getRetryDelay());
		transDesc.setRetryTimes(nodeBean.getRetryTimes());
		Method action = getActionMethod(nodeBean.getName());
		if (action == null)
		{
			throw new IllegalArgumentException("节点对应的动作方法[" + nodeBean.getName() + "]不存在");
		}
		transDesc.setAction(action);
		Method pre = getConditionMethod(nodeBean.getPreCondition());
		if (pre != null)
		{
			transDesc.setPreCondition(pre);
		}
		else if (pre == null && nodeBean.getPreCondition() != null)
		{
			throw new IllegalArgumentException(
					"节点前置条件方法[" + nodeBean.getName() + "|" + nodeBean.getPreCondition() + "]不存在");
		}
		Method post = getConditionMethod(nodeBean.getPostCondition());
		if (post != null)
		{
			transDesc.setPostCondition(post);
		}
		else if (post == null && nodeBean.getPostCondition() != null)
		{
			throw new IllegalArgumentException(
					"节点后置条件方法[" + nodeBean.getName() + "|" + nodeBean.getPostCondition() + "]不存在");
		}
		Method delay = getDelayMethod(nodeBean.getDelay());
		if (delay != null)
		{
			transDesc.setDelay(delay);
		}
		else if (post == null && nodeBean.getDelay() != null)
		{
			throw new IllegalArgumentException("节点延迟方法[" + nodeBean.getName() + "|" + nodeBean.getDelay() + "]不存在");
		}
		return transDesc;
	}
}
