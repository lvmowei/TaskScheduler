package com.lede.tech.workflow;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author xliao
 * 
 */
@Component
public class ThreadPool
{
	private static final Log LOG = LogFactory.getLog(ThreadPool.class);
	//	private static ExecutorService exec = Executors.newCachedThreadPool();
	// modify by zja 修改线程池实现，减少频繁生成新线程
	//	private static ExecutorService exec = new ThreadPoolExecutor(10, 50, 5 * 60, TimeUnit.SECONDS,
	//			new ArrayBlockingQueue<Runnable>(100), new ThreadPoolExecutor.AbortPolicy());

	//	modify by 李家智，改成队列方式，保证被执行
	private final ThreadPoolExecutor exec;

	/*private final ThreadPoolExecutor execAddTask; //异步执行任务时负责调度的线程池

	private final ThreadPoolExecutor livePushPoolExecutor; //执行比分直播推送用的线程池

	private final ThreadPoolExecutor execSendCouponExecutor; //登录彩票app时派发红包线程池

	private final ThreadPoolExecutor selfCalcAwardExec; //自派奖线程池

	private final ThreadPoolExecutor execTicketOut;//出票分账
	private final ThreadPoolExecutor execAwardDistribute;//派奖分账

	private static ExecutorService execCloseEpay;*/
	
	/**
	 * @author bjlibo
	 * 新增加一个单独的线程池，用于异步统计投注比例
	 */
	/*private static ExecutorService execBidCount = new ThreadPoolExecutor(10, 10, 5 * 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());*/


	/*private static ExecutorService execReqZX4APISport;*/

	/**
	 * 监控高频销售线程池
	 */
	/*private static ExecutorService executorService = Executors.newFixedThreadPool(5);*/
	/**
	 * 新增一个单独的线程池，用于并发发起请求资讯获取竞彩相关wap投注页面的赛事分析数据
	 * 
	 */
	/*private static ExecutorService execReqZX4WapSport;*/

	//新增线程池执行大彩的派奖系流程
	/*private static ExecutorService execDigitalQuryyPrizeUsers;*/

	//新增线程池执行大彩的派奖系流程
	/*private static ExecutorService execDigitalDownloadPeriod;*/

	//新增线程池执行大彩的派奖系流程
	/*private static ExecutorService execDigitalAwardedStakeOrder;*/

	/**
	 * @author wangmeng
	 * 增加一个单独的线程池，执行投注成功后的拆单投注逻辑
	 */
	/*private static ExecutorService execSpecial;*/

	/**
	 * 单线程，任务之间有依赖关系的可以用这个。默认无界LinkedBlockingQueue
	 */
	/*private static ExecutorService execSingle;*/

	/**
	 * 新增线程池，用于支付成功通知里的已经关闭的订单发送推送消息，提醒用户用红包支付，池子10个活跃线程。
	 */
	/*private static ExecutorService execPayFailPush;*/

	//
	//	//用于计算过关统计的线程执行。
	//	private static ExecutorService execCalHit = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS,
	//			new LinkedBlockingQueue<Runnable>());
	//	/**
	//	 * 新增线程池，用于支付成功通知里的新启线程
	//	 */
	//	private static ExecutorService execPay = new ThreadPoolExecutor(30, 30, 5 * 60, TimeUnit.SECONDS,
	//			new LinkedBlockingQueue<Runnable>());
	//	
	//	/**
	//	 * 新增线程池，获取比分直播中赛事直播事件
	//	 */
	//	private static ExecutorService execGetMatchLiveData = new ThreadPoolExecutor(30, 30, 5 * 60, TimeUnit.SECONDS,
	//			new LinkedBlockingQueue<Runnable>());

	private static final ThreadPool instance = new ThreadPool();
	/*private static ScheduledExecutorService scheduledExecutorService;*/

	private ThreadPool()
	{
		exec = new ThreadPoolExecutor(50, 100, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);

		/*execAwardDistribute = new ThreadPoolExecutor(50, 100, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		execAwardDistribute.allowCoreThreadTimeOut(true);

		execTicketOut = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		execTicketOut.allowCoreThreadTimeOut(true);

		execAddTask = new ThreadPoolExecutor(5, 50, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		execAddTask.allowCoreThreadTimeOut(true);

		livePushPoolExecutor = new ThreadPoolExecutor(5, 15, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		livePushPoolExecutor.allowCoreThreadTimeOut(true);

		execSendCouponExecutor = new ThreadPoolExecutor(5, 16, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		execSendCouponExecutor.allowCoreThreadTimeOut(true);

		selfCalcAwardExec = new ThreadPoolExecutor(5, 20, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		execCloseEpay = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		execReqZX4APISport = new ThreadPoolExecutor(IniBean.getIniIntValue("MID_INFO_ZX_CORE_POOL_SIZE", 5),
				IniBean.getIniIntValue("MID_INFO_ZX_MAX_POOL_SIZE", 5),
				IniBean.getIniLongValue("MID_INFO_ZX_KEEP_ALIVE_TIME", 60), TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		execReqZX4WapSport = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		execDigitalQuryyPrizeUsers = new ThreadPoolExecutor(5, 30, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		execDigitalDownloadPeriod = new ThreadPoolExecutor(5, 20, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		execDigitalAwardedStakeOrder = new ThreadPoolExecutor(5, 20, 5 * 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		execSpecial = new ThreadPoolExecutor(5, 20, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		execSingle = Executors.newSingleThreadExecutor();

		execPayFailPush = new ThreadPoolExecutor(5, 10, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		scheduledExecutorService = Executors.newScheduledThreadPool(3);*/
	}

	public static ThreadPool getInstance()
	{
		return instance;
	}

	/*public static ExecutorService getDigitalQuryyPrizeUsersExec()
	{
		return execDigitalQuryyPrizeUsers;
	}

	public static ExecutorService getDigitalDownloadPeriodExec()
	{
		return execDigitalDownloadPeriod;
	}

	public static ExecutorService getDigitalAwardedStakeOrder()
	{
		return execDigitalAwardedStakeOrder;
	}*/

	public void exec(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(exec))
		{
			LOG.warn("executor exec is not shutdown or terminating or terminated!");
			return;
		}

		exec.execute(command);
	}

	/*public void execAwardDistribute(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(execAwardDistribute))
		{
			LOG.warn("executor execAwardDistribute is not shutdown or terminating or terminated!");
			return;
		}

		execAwardDistribute.execute(command);
	}

	public void execTicketOut(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(execTicketOut))
		{
			LOG.warn("executor execTicketOut is not shutdown or terminating or terminated!");
			return;
		}

		execTicketOut.execute(command);
	}

	public void execAddTask(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(execAddTask))
		{
			LOG.warn("executor execAddTask is not shutdown or terminating or terminated!");
			return;
		}

		execAddTask.execute(command);
	}

	public void execLivePushTask(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(livePushPoolExecutor))
		{
			LOG.warn("executor livePushPoolExecutor is not shutdown or terminating or terminated!");
			return;
		}

		livePushPoolExecutor.execute(command);
	}

	public void execSendCouponExecutor(Runnable command)
	{
		if (!ThreadPool.isExecutorAvaliable(execSendCouponExecutor))
		{
			LOG.warn("Executor execSendCouponExecutor is not shutdown or terminating or terminated!");
			return;
		}

		execSendCouponExecutor.execute(command);
	}

	public void execPayFailPush(Runnable command)
	{
		if (!ThreadPool.isExecutorServiceAvaliable(execPayFailPush))
		{
			LOG.warn("ExecutorService execPayFailPush is not shutdown or terminated!");
			return;
		}

		execPayFailPush.execute(command);
	}

	public ThreadPoolExecutor getSelfAwardExecutor()
	{
		return selfCalcAwardExec;
	}

	public int getExecPoolSize()
	{
		return exec.getPoolSize();
	}

	public void execCloseEpay(Runnable command)
	{
		if (!ThreadPool.isExecutorServiceAvaliable(execCloseEpay))
		{
			LOG.warn("ExecutorService exec is not shutdown or terminated!");
			return;
		}

		execCloseEpay.execute(command);
	}
	
	public void execBidCount(Runnable command)
	{
		execBidCount.execute(command);
	}

	public <T> Future<T> submitReqZX4APISport(Callable<T> command)
	{
		if (!ThreadPool.isExecutorServiceAvaliable(execReqZX4APISport))
		{
			LOG.warn("ExecutorService execReqZX4APISport is not shutdown or terminated!");
			return null;
		}

		return execReqZX4APISport.submit(command);
	}

	public <T> Future<T> submitReqZX4WapSport(Callable<T> command)
	{
		if (!ThreadPool.isExecutorServiceAvaliable(execReqZX4WapSport))
		{
			LOG.warn("ExecutorService execReqZX4WapSport is not shutdown or terminated!");
			return null;
		}

		return execReqZX4WapSport.submit(command);
	}

	public void execSpecial(Runnable command)
	{
		if (!ThreadPool.isExecutorServiceAvaliable(execSpecial))
		{
			LOG.warn("ExecutorService execSpecial is not shutdown or terminated!");
			return;
		}

		execSpecial.execute(command);
	}

	public void execSingle(Runnable command)
	{
		if (CommonConstant.SWITCH_ON
				.equals(IniBean.getIniValue("hit_ifUseSingleThreadExecutor", CommonConstant.SWITCH_ON)))
		{
			if (!ThreadPool.isExecutorServiceAvaliable(execSingle))
			{
				LOG.warn("ExecutorService execSingle is not shutdown or terminated!");
				return;
			}

			execSingle.execute(command);
		}
		else
		{
			this.exec(command);
		}
	}

	public void executeSchedual(Runnable runnable, int delay, TimeUnit timeUnit)
	{
		if (!ThreadPool.isExecutorServiceAvaliable(scheduledExecutorService))
		{
			LOG.warn("ExecutorService scheduledExecutorService is shutdown or terminated!");
			return;
		}
		scheduledExecutorService.schedule(runnable, delay, timeUnit);
	}*/

	public static boolean isExecutorAvaliable(ThreadPoolExecutor executor)
	{
		if (executor.isShutdown() || executor.isTerminating() || executor.isTerminated())
			return false;
		return true;
	}

	/*public static boolean isExecutorServiceAvaliable(ExecutorService executor)
	{
		if (executor.isShutdown() || executor.isTerminated())
			return false;
		return true;
	}*/

	@PreDestroy
	public void shutdown()
	{
		exec.shutdown();
		/*execAddTask.shutdown();
		livePushPoolExecutor.shutdown();
		execSendCouponExecutor.shutdown();
		selfCalcAwardExec.shutdown();
		execCloseEpay.shutdown();
		execReqZX4APISport.shutdown();
		execReqZX4WapSport.shutdown();
		execDigitalQuryyPrizeUsers.shutdown();
		execDigitalDownloadPeriod.shutdown();
		execDigitalAwardedStakeOrder.shutdown();
		execSpecial.shutdown();
		execSingle.shutdown();
		execPayFailPush.shutdown();
		execTicketOut.shutdown();
		scheduledExecutorService.shutdown();
		execAwardDistribute.shutdown();*/
	}

	/*public void execMonitorHF(Runnable command)
	{
		executorService.execute(command);
	}*/
}
