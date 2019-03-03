package com.lede.tech.workflow.sample.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lede.tech.workflow.annotation.Delay;
import com.lede.tech.workflow.annotation.Node;
import com.lede.tech.workflow.annotation.Template;
import com.lede.tech.workflow.core.engine.Engine;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;

//@Template
@Component("sampleTemplate")
@Scope("prototype")
public class SampleTemplate implements ProcessTemplate
{
	private final Log LOG = LogFactory.getLog(getClass());

	public synchronized void init()
	{
		
	}
	
	@Override
	public String[] initStatus()
	{
		return new String[]
		{ "任务1" };
	}

	@Override
	public boolean isFinished()
	{
		return false;
	}

	@Override
	public String getInstanceId()
	{
		return getClass().getName();
	}
	
	@Node(name = "任务1", next = "任务1", retryTimes = 1, retryDelay = 5 * 1000, delay = "任务1延迟")
	public String transitionQM0()
	{
		LOG.info("任务1 start");
		return ProcessTemplate.SUCCESS;
	}
	
	@Delay(name = "任务1延迟")
	public long delayQM0()
	{
		return 5 * 1000;
	}

	/*protected String[] gameIds;
	protected String[] platformIds;
	//连续报警计数器
	protected int WARN_COUNTS = 1;
	//报警序列
	protected int[] WARN_SEQ =
	{ 1, 3, 6 };

	protected Log log = LogFactory.getLog(getClass());

	public synchronized void init()
	{
		if (log.isDebugEnabled())
		{
			log.debug("流程引擎中场期任务[" + games + "]开始初始化");
		}
		gameIds = getGames(games);
		if (gameIds == null || gameIds.length <= 0)
		{
			throw new BusinessException("流程引擎初始化时缺少games参数");
		}
		platformIds = getPriorityPlatform(gameIds[0]);
		if (platformIds == null || platformIds.length <= 0)
		{
			throw new BusinessException("流程引擎初始化时缺少platform参数");
		}
		Engine.initInstance(this);

		List<Object> tasks = getUnfinishedWorks();

		Engine.removeFinishedInstance();
		for (Object task : tasks)
		{
			if (log.isDebugEnabled())
			{
				log.debug("初始化场次[" + task + "]任务");
			}
			updateTask(task);
		}
	}

	*//**
	 * @param task 竞彩中为String类型 like 201402193306
	 * 单场中为LotteryPeriod类型
	 * @return
	 *//*
	public boolean updateTask(Object task)
	{
		boolean ok = TemplateFactory.reinitInstance(getInstanceId(task));
		if (!ok)
		{
			SlaveTemplate instance = (SlaveTemplate) TemplateFactory.initInstance(getInstanceClass());
			instance.setController(this);
			instance.setTask(task);
			Engine.initInstance(instance);
			return true;
		}
		return ok;
	}

	public synchronized boolean monitor()
	{
		boolean needInit = false;
		String[] current = getPriorityPlatform(gameIds[0]);
		//该彩种的合作方数量有变化需要重新初始化
		log.info("MasterTemplate:monitor:game is " + gameIds[0]);
		if (platformIds.length != current.length)
		{
			needInit = true;
			log.error("流程实例[" + getInstanceId() + "]权重平台数量发生变化，需要重试初始化该实例");
		}
		//比较权重排序是否有变化，有变化需要重新初始化
		else
		{
			for (int i = 0; i < platformIds.length; i++)
			{
				if (!platformIds[i].equals(current[i]))
				{
					needInit = true;
					break;
				}
			}
			if (needInit)
			{
				log.error("流程实例[" + getInstanceId() + "]平台权重值发生变化，需要重试初始化该实例");
			}
		}

		if (current == null || current.length <= 0)
		{
			throw new BusinessException("流程监控[" + games + "]失败，缺少平台ID");
		}
		if (needInit)
		{
			log.info("MasterTemplate:monitor:平台权重发生变化，需要重新进行初始化实例:" + getInstanceId() + "  调之前:"
					+ printPlatform(platformIds) + "  调之后:" + printPlatform(current));
			if (CommonConstant.SWITCH_ON.equals(IniBean.getIniValue(LogPrefix.LOG_INI_KEY, CommonConstant.SWITCH_ON)))
			{
				Engine.snapshot("MasterTemplate Monitor Before init");
			}
			init();
			if (CommonConstant.SWITCH_ON.equals(IniBean.getIniValue(LogPrefix.LOG_INI_KEY, CommonConstant.SWITCH_ON)))
			{
				Engine.snapshot("MasterTemplate Monitor After init");
			}
		}
		return needInit;
	}

	*//**获取指定彩种的权重，按照权重大小顺序排序，大->小，包括为0的
	 * @param gameId
	 * @return
	 *//*
	protected String[] getPriorityPlatform(String gameId)
	{
		//return ContestMatchBean.getPriorityPlatform(gameId);
		List<String> platformList = new ArrayList<String>();
		Game game = Games.getGame(gameId);
		if (game.getPlatformBetType() == CommonConstant.PLATFORM_TYPE_WEIGHT)
		{
			List<LotteryWeight> weightList = LotteryWeightCache.getLotteryWeightByGameId(gameId);
			//倒序排，权重最大，放最前面
			Collections.sort(weightList, new Comparator<LotteryWeight>() {
				@Override
				public int compare(LotteryWeight w1, LotteryWeight w2)
				{
					if (w1.getWeight() == w2.getWeight())
					{
						return w1.getGameId().compareTo(w2.getGameId());
					}
					return w1.getWeight() < w2.getWeight() ? 1 : -1;
				}
			});
			for (LotteryWeight weight : weightList)
			{
				Lottery lottery = LotteryCache.getLotteryByGameIdAndPlatformId(gameId, weight.getPlatformId());
				if (lottery == null)
				{
					log.fatal("更新期次任务执行时发现Lottery不存在" + gameId + "|" + weight.getPlatformId());
					return new String[0];
				}
				//modified by mwlv 工作流可用的才返回
				if (weight.getScheduleUsable() == LotteryWeight.SCHEDULE_USABLE)
					platformList.add(weight.getPlatformId());
			}
		}
		else
		{
			List<String> lst = LotterySettingCache.getPlatformsFromLotterySettingByGameId(gameId);
			if (!(lst == null || lst.isEmpty()))
			{
				platformList.add(lst.get(0));
			}
		}
		return platformList.toArray(new String[0]);
	}

	public abstract Class<? extends ProcessTemplate> getInstanceClass();

	public abstract String getInstanceId(Object task);

	public abstract List<Object> getUnfinishedWorks();

	public abstract String[] getGames(String games);

	public String[] getGames()
	{
		return gameIds;
	}

	public String[] getPlatforms()
	{
		return platformIds;
	}

	@Override
	public String getInstanceId()
	{
		return games;
	}
	*/

	/*
	@Override
	public boolean isFinished()
	{
		return false;
	}

	public String getDefaultPlatformId()
	{
		return null;
	}

	private String printPlatform(String[] platformIds)
	{
		StringBuffer sb = new StringBuffer();
		for (String platformId : platformIds)
		{
			sb.append(platformId).append(":");
		}
		return sb.toString();
	}*/
}
