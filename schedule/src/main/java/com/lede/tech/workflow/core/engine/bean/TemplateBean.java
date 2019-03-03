package com.lede.tech.workflow.core.engine.bean;

import com.lede.tech.workflow.core.engine.bean.descriptor.ConditionDescriptor;
import com.lede.tech.workflow.core.engine.bean.descriptor.TransitionDescriptor;
import com.lede.tech.workflow.core.model.petri.Arc;
import com.lede.tech.workflow.core.model.petri.PetriNet;
import com.lede.tech.workflow.core.model.petri.Transition;

/**根据template生成的包含petrinet的bean
 * @author mwlv
 *
 */
public class TemplateBean
{
	private Class<? extends ProcessTemplate> template;
	private PetriNet process;
	private Descriptor descriptor;

	public TemplateBean(Class<? extends ProcessTemplate> template, PetriNet process, Descriptor descriptor)
	{
		this.template = template;
		this.process = process;
		this.descriptor = descriptor;
	}

	public TransitionDescriptor getTransDescriptor(String transName)
	{
		Transition trans = process.getTransition(transName);
		if (trans == null)
		{
			return null;
		}
		return descriptor.getTransition(transName);
	}

	public ConditionDescriptor getCondDescriptor(String edgeName)
	{
		Arc arc = process.getArc(edgeName);
		if (arc == null)
		{
			return null;
		}
		return descriptor.getCondition(edgeName);
	}

	public Class<? extends ProcessTemplate> getTemplate()
	{
		return template;
	}

	public void setTemplate(Class<? extends ProcessTemplate> template)
	{
		this.template = template;
	}

	public PetriNet getProcess()
	{
		return process;
	}

	public String getTemplateId()
	{
		return process.getName();
	}

	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		return getTemplateId().equals(((TemplateBean) o).getTemplateId());
	}

	public int hashCode()
	{
		return getTemplateId().hashCode();
	}
}
