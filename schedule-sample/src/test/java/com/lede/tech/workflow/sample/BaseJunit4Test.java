package com.lede.tech.workflow.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration(value = "src/main/webapp")
@ContextConfiguration(locations =
{ "classpath:applicationContext.xml" })
public class BaseJunit4Test
{
	protected final Log LOG = LogFactory.getLog(getClass());
	@Test
	public void test()
	{
		
	}
}
