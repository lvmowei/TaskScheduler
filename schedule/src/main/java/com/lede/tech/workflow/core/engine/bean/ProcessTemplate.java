package com.lede.tech.workflow.core.engine.bean;

public interface ProcessTemplate
{
	static String SUCCESS = "SUCCESS";
	static String ERROR = "ERROR";
	static String DONE = "DONE";

	/**
	 * 根据持久化的数据，返回当前需要进行的操作
	 * @return 返回节点Node的名称集合
	 */
	String[] initStatus();

	/**
	 * 流程引擎删除实例的唯一条件
	 * @return
	 */
	boolean isFinished();

	/**
	 * 既要区分流程，也要区分流程实例，需要全局唯一
	 * @return
	 */
	String getInstanceId();
}
