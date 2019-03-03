package com.lede.tech.workflow.core.engine.container;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class TemplateScanner
{
	private static Set<Class> templates = new HashSet<Class>();

	public static boolean add(Class templateClass)
	{
		return templates.add(templateClass);
	}

	public static boolean contains(Class templateClass)
	{
		return templates.contains(templateClass);
	}

	public Iterator<Class> iterator()
	{
		return templates.iterator();
	}
}