package com.lede.tech.workflow.sample;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;


public class TestService extends BaseJunit4Test
{

	@Rollback(true)
	@Test
	public void test()
	{
		try
		{
		}
		catch (Exception e)
		{
			LOG.error(e);
		}
	}
}
