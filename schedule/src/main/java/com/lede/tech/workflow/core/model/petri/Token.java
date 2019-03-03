package com.lede.tech.workflow.core.model.petri;

import com.lede.tech.workflow.core.model.graph.Component;

public class Token extends Component implements Cloneable
{
	private String transition;
	private String place;
	private Status status;

	public enum Status
	{
		Wait, Free, Busy
	}

	public Token(String place)
	{
		super();
		this.place = place;
		transition = null;
		status = Status.Free;
	}

	private Token(String transition, String place, Status status)
	{
		super();
		this.transition = transition;
		this.place = place;
		this.status = status;
	}

	@Override
	public Type getType()
	{
		return Component.Type.OTHER;
	}

	public String getPlace()
	{
		return place;
	}

	public void setPlace(String place)
	{
		this.place = place;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getTransition()
	{
		return transition;
	}

	public void setTransition(String transition)
	{
		this.transition = transition;
	}

	public Object clone()
	{
		return new Token(transition, place, status);
	}

	@Override
	public String toString()
	{
		return "Token{" + "transition='" + transition + '\'' + ", place='" + place + '\'' + ", status=" + status + '}';
	}
}
