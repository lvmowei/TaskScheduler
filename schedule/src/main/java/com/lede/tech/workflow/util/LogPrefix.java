package com.lede.tech.workflow.util;

//import com.lede.tech.constant.CommonConstant;
//import com.lede.tech.constant.IniBean;

public final class LogPrefix
{
	/**
	 * Ini log 开关配置 Key
	 */
	public static final String LOG_INI_KEY = "workflow_log";
	public static final String WORKFLOW = "Workflow:";

	public static boolean ifPrint()
	{
		//if (CommonConstant.SWITCH_ON.equals(IniBean.getIniValue(LogPrefix.LOG_INI_KEY, CommonConstant.SWITCH_ON)))
		//{
		return true;
		//}
		//return false;
	}
}
