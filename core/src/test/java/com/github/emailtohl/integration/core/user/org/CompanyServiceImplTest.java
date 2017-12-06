package com.github.emailtohl.integration.core.user.org;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;

import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.google.gson.Gson;

/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
@Rollback(false)
public class CompanyServiceImplTest {
	@Inject
	CompanyService companyService;
	@Inject
	DepartmentService departmentService;
	@Inject
	Gson gson;
	
	Pageable pageable = new PageRequest(0, 20);
	Long pid, sid1, sid2, did, did1, did2;

	@Before
	public void setUp() throws Exception {
		Company c = new Company("parent", "集团公司", null),
				c1 = new Company("son1", "分子公司1", c),
				c2 = new Company("son2", "分子公司2", c);
		
		pid = companyService.create(c).getId();
		sid1 = companyService.create(c1).getId();
		sid2 = companyService.create(c2).getId();
		
		Department d = new Department("parent", "父级部门", null),
				d1 = new Department("son1", "二级部门1", d),
				d2 = new Department("son2", "二级部门2", d);
		
		d.setCompany(c);
		d1.setCompany(c1);
		d2.setCompany(c2);
		
		did = departmentService.create(d).getId();
		did1 = departmentService.create(d1).getId();
		did2 = departmentService.create(d2).getId();
	}

	@After
	public void tearDown() throws Exception {
		companyService.delete(pid);
		companyService.delete(sid1);
		companyService.delete(sid2);
		
		departmentService.delete(did);
		departmentService.delete(did1);
		departmentService.delete(did2);
	}

	@Test
	public void testExist() {
		assertTrue(companyService.exist("parent"));
		assertTrue(companyService.exist("son1"));
		assertTrue(companyService.exist("son2"));
		assertFalse(companyService.exist("son3"));
	}

	@Test
	public void testGet() {
		Company pc = companyService.get(pid);
		assertNotNull(pc);
		System.out.println(gson.toJson(pc));
		
		pc = companyService.get(sid1);
		assertNotNull(pc);
		System.out.println(gson.toJson(pc));
		
		pc = companyService.get(sid2);
		assertNotNull(pc);
		System.out.println(gson.toJson(pc));
	}

	@Test
	public void testQuery() {
		Company c = companyService.get(pid);
		Paging<Company> page = companyService.query(c, pageable);
		assertFalse(page.getContent().isEmpty());
		List<Company> ls = companyService.query(c);
		assertFalse(ls.isEmpty());
		
		c = companyService.get(sid1);
		page = companyService.query(c, pageable);
		assertFalse(page.getContent().isEmpty());
		ls = companyService.query(c);
		assertFalse(ls.isEmpty());
		
		c = companyService.get(sid2);
		page = companyService.query(c, pageable);
		assertFalse(page.getContent().isEmpty());
		ls = companyService.query(c);
		assertFalse(ls.isEmpty());
		
		page = companyService.query(null, pageable);
		assertFalse(page.getContent().isEmpty());
		ls = companyService.query(c);
		assertFalse(ls.isEmpty());
		
		System.out.println(gson.toJson(page));
		System.out.println(gson.toJson(ls));
		
	}

	@Test
	public void testUpdate() {
		Company ud = new Company();
		ud.setName("update");
		ud.setDescription("update");
		Company son1 = companyService.get(sid1);
		ud.setParent(son1);
		Company newComp = companyService.update(sid2, ud);
		assertEquals("update", newComp.getName());
		assertEquals("update", newComp.getDescription());
		assertEquals(son1, newComp.getParent());
	}

}
