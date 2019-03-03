package com.lede.tech.workflow.sample.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lede.tech.workflow.sample.Sample;

@Controller
public class SampleController
{
	
	private final Log LOG = LogFactory.getLog(getClass());

	@Autowired
	Sample sample;

	/***
	 * @param request
	 * @param response
	 */

	@RequestMapping(value = "/printPetriNet.html")
	@ResponseBody
	public String printPetriNet(HttpServletRequest request, HttpServletResponse response)
	{
		return sample.getPetriNetDot();
	}

}
