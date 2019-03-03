package com.lede.tech.workflow.parser;

import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;

public interface Builder
{
	void parse(Class<? extends ProcessTemplate> template) throws Exception;

	Object build() throws Exception;
}
