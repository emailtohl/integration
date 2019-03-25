package com.github.emailtohl.integration.web.service.mybatistest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.web.config.WebTestEnvironment;

/**
 * MyBatis测试
 * @author HeLei
 */
public class MybatisTest extends WebTestEnvironment {
	@Inject
	CorePresetData cpd;
	@Inject
	MybatisTestService mybatisTestService;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String name = cpd.product.getName();
		Department d = mybatisTestService.findByName(name);
		assertNotNull(d);
		assertEquals(cpd.product.getId(), d.getId());
	}

	@Test(expected = Exception.class)
	public void testTransactionCallback() {
		Department d = new Department();
		d.setName("test");
		d.setCreationTime(new Date());
		d.setModifyTime(new Date());
		d.setResponsiblePerson("test person");
		Long id = mybatisTestService.insert(d);
		fail("不会到达此处");
		System.out.println(id);
	}
}
