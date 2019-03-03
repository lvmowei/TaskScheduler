package com.lede.tech.workflow.core.model.petri;

import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.util.LogPrefix;
import com.lede.tech.workflow.util.NStringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Status implements Cloneable
{
	private PetriNet net;
	private Vector<Token> tokens;
	private ReentrantLock lock;
	private static Log log = LogFactory.getLog(Status.class);

	public Status(TemplateBean template)
	{
		net = template.getProcess();
		tokens = new Vector<Token>();
		lock = new ReentrantLock();
	}

	private Status(PetriNet net, Vector<Token> tokens, ReentrantLock lock)
	{
		this.net = net;
		this.tokens = tokens;
		this.lock = lock;
	}

	public boolean setToken(String[] trans)
	{
		lock.lock();
		try
		{
			Token[] ts = net.preMarking(trans);
			if (LogPrefix.ifPrint())
			{
				log.info("Status:setToken by trans :" + NStringUtil.combine(trans, null) + "  return tokens is :"
						+ NStringUtil.combine(ts, null));
			}
			if (ts.length <= 0)
			{
				return false;
			}
			for (Token token : ts)
			{
				tokens.add(token);
			}
			return true;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * 得到被transition任务锁定的，token状态是status的所有token集合
	 * @param transition 如果为空，则只检查token的状态
	 * @param status
	 * @return
	 */
	private Token[] getToken(String transition, Token.Status status)
	{
		//加锁，防止读取出来的状态不一致
		lock.lock();
		try
		{
			List<Token> lst = new ArrayList<Token>();
			for (Token token : tokens)
			{
				if ((token.getStatus() == Token.Status.Free && token.getTransition() == null)
						|| (token.getStatus() == Token.Status.Wait && token.getTransition() != null))
				{
					if (token.getStatus() == status)
					{
						if ((token.getTransition() == null && transition == null) || (token.getTransition() != null
								&& transition != null && token.getTransition().equals(transition)))
						{
							lst.add(token);
						}
					}
				}
				else
				{
					throw new IllegalArgumentException("Token状态[" + token.getStatus() + "|" + token.getTransition()
							+ "]和目标动作" + transition + "不一致");
				}
			}
			return lst.toArray(new Token[0]);
		}
		finally
		{
			lock.unlock();
		}
	}

	private boolean destroyToken(Token[] ts)
	{
		for (Token t : ts)
		{
			tokens.remove(t);
		}
		return true;
	}

	private boolean createToken(int[] mark)
	{
		List<Token> lst = new ArrayList<Token>();
		for (int i = 0; i < mark.length; i++)
		{
			if (mark[i] > 0)
			{
				Token t = new Token(net.getPlace(i).getName());
				lst.add(t);
			}
		}
		tokens.addAll(lst);
		return true;
	}

	/**
	 * 从set中选出满足mark的最小的Token集合
	 * 
	 * @param mark
	 * @param set
	 * @return
	 */
	private Token[] getToken(int[] mark, Token[] set)
	{
		List<Token> lst = new ArrayList<Token>();
		for (int i = 0; i < mark.length; i++)
		{
			if (mark[i] > 0)
			{
				int num = mark[i];
				for (Token token : set)
				{
					if (token.getPlace().equalsIgnoreCase(net.getPlace(i).getName()))
					{
						lst.add(token);
						num--;
					}
					if (num <= 0)
					{
						break;
					}
				}
				if (num > 0)
				{
					return new Token[0];
				}
			}
		}
		return lst.toArray(new Token[0]);
	}

	/**
	 * @return 下一次所有可能发生的transition所需要的边名
	 */
	public String[] nextCheck()
	{
		Token[] tokens = getToken(null, Token.Status.Free);
		int[] mark = net.getMarking(tokens);//获取Token分布
		Arc[] arcs = net.preArc(mark);
		String[] res = new String[arcs.length];
		for (int i = 0; i < res.length; i++)
		{
			res[i] = arcs[i].getName();
		}
		return res;
	}

	/**
	 * @param arcs 经判断后为真的边
	 * @return 根据为真的边，生成下一次一定会发生的transition的名字
	 */
	public String[] next(List<String> arcs)
	{
		Token[] tokens = getToken(null, Token.Status.Free);
		int[] mark = net.getMarking(tokens);
		int[] trans = net.preFiring(mark, arcs);
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < trans.length; i++)
		{
			if (trans[i] <= 0)
			{
				continue;
			}
			String t = net.getTransition(i).getName();
			if (!lst.contains(t))
			{
				lst.add(t);
			}
		}
		String[] res = lst.toArray(new String[0]);
		Arrays.sort(res);
		return res;
	}

	/**
	 * transition
	 * 
	 * @param transition
	 * @return
	 */
	public Status firing(String transition)
	{
		lock.lock();
		try
		{
			//获取被transition锁定的、状态为Token.Status.Wait的所有Token
			Token[] set = getToken(transition, Token.Status.Wait);
			int[] min = net.isEnable(set, transition);
			if (min == null)
			{
				log.error("Status:Firing:" + transition + ":获取执行所需要的最小Token集合返回空！" + NStringUtil.combine(set, null));
				log.error("Status:Firing:all tokens is " + getTokensStr());
				return null;
			}
			Token[] move = getToken(min, set);
			if (move.length <= 0)
			{
				log.error("Status:Firing:" + transition + ":获取执行所需要的最小Token集合返回空！ Set is "
						+ NStringUtil.combine(set, null));
				log.error("Status:Firing:all tokens is " + getTokensStr());
				return null;
			}
			int[] next = net.firing(move, transition);
			if (next == null)
			{
				log.error("Status:Firing:" + transition + ":获取下一次执行Token失败！Move Token is "
						+ NStringUtil.combine(move, null));
				log.error("Status:Firing:all tokens is " + getTokensStr());
				return null;
			}
			createToken(next);
			destroyToken(move);
			return (Status) clone();
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean hold(String transition)
	{
		lock.lock();
		try
		{
			Token[] allFree = getToken(null, Token.Status.Free);
			int[] min = net.isEnable(allFree, transition);
			if (min == null)
			{
				log.error("Status:hold:" + transition + ":获取执行Transition所需要的最小Token集合返回空！锁定失败！Token is :"
						+ NStringUtil.combine(allFree, null));
				log.error("Status:Firing:all tokens is " + getTokensStr());
				return false;
			}
			Token[] minFree = getToken(min, allFree);
			if (minFree.length <= 0)
			{
				log.error("Status:hold:" + transition + ":从最小Token Set中选出满足mark的最小的Token集合返回空！锁定失败！");
				log.error("Status:Firing:all tokens is " + getTokensStr());
				return false;
			}
			for (Token token : minFree)
			{
				token.setStatus(Token.Status.Wait);
				token.setTransition(transition);
			}
			return true;
		}
		finally
		{
			lock.unlock();
		}
	}

	public Object clone()
	{
		Vector<Token> clonetokens = new Vector<Token>();
		for (Token token : tokens)
		{
			clonetokens.add((Token) token.clone());
		}
		return new Status(net, clonetokens, lock);
	}

	public String getTokensStr()
	{
		lock.lock();
		try
		{
			if (tokens != null && tokens.size() > 0)
			{
				return NStringUtil.combine(tokens.toArray(), null);
			}
		}
		finally
		{
			lock.unlock();
		}
		return null;

	}
}
