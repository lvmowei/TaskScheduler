package com.lede.tech.workflow.sample.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController
{
	
	private final Log LOG = LogFactory.getLog(getClass());


	/***
	 * @param request
	 * @param response
	 */

	@RequestMapping(value = "/test.html")
	@ResponseBody
	public String test(HttpServletRequest request, HttpServletResponse response)
	{
		LOG.info("aaaaaaaaaaaaaaaaaaaaaaaaaa");
		return "test ok!";
	}

}
