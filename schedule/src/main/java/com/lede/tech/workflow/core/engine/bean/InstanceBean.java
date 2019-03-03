package com.lede.tech.workflow.core.engine.bean;

import com.lede.tech.workflow.core.model.petri.Status;

import java.text.SimpleDateFormat;

public class InstanceBean
{
	private ProcessTemplate instance;
	private TemplateBean template;
	private Status status;
	private String sequence;

	public InstanceBean(TemplateBean template, ProcessTemplate instance)
	{
		this.instance = instance;
		this.template = template;
		status = new Status(template);
		sequence = getTimeMillisSequence();
	}

	public TemplateBean getTemplate()
	{
		return template;
	}

	public ProcessTemplate getInstance()
	{
		return instance;
	}

	public String getInstanceId()
	{
		return instance.getInstanceId();
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		return sequence.equals(((InstanceBean) o).getSequence());
	}

	public int hashCode()
	{
		return sequence.hashCode();
	}

	public String getSequence()
	{
		return sequence;
	}

	private static String getTimeMillisSequence()
	{
		long nanoTime = System.nanoTime();
		String preFix = "";
		if (nanoTime < 0)
		{
			preFix = "A";
			nanoTime = nanoTime + Long.MAX_VALUE + 1;
		}
		else
		{
			preFix = "Z";
		}
		String nanoTimeStr = String.valueOf(nanoTime);
		int difBit = String.valueOf(Long.MAX_VALUE).length() - nanoTimeStr.length();
		for (int i = 0; i < difBit; i++)
		{
			preFix = preFix + "0";
		}
		nanoTimeStr = preFix + nanoTimeStr;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String timeMillisSequence = sdf.format(System.currentTimeMillis()) + "-" + nanoTimeStr;
		return timeMillisSequence;
	}

}
