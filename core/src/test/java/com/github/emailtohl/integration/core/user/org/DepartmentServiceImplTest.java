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
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.google.gson.Gson;

/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
@Rollback(false)
public class DepartmentServiceImplTest {
	@Inject
	DepartmentService departmentService;
	@Inject
	EmployeeService employeeService;
	@Inject
	Gson gson;
	Pageable pageable = new PageRequest(0, 20);
	Long pid, sid1, sid2, empid1, empid2, empid3;

	@Before
	public void setUp() throws Exception {
		Department d = new Department("parent", "一级部门", null),
				d1 = new Department("son1", "二级部门1", d),
				d2 = new Department("son2", "二级部门2", d);
		d.setResponsiblePerson("_foo");
		d1.setResponsiblePerson("_bar");
		d2.setResponsiblePerson("_baz");
		
		pid = departmentService.create(d).getId();
		sid1 = departmentService.create(d1).getId();
		sid2 = departmentService.create(d2).getId();
		
		Employee emp1 = new Employee(),
				emp2 = new Employee(),
				emp3 = new Employee();
		emp1.setName("_foo");
		emp2.setName("_bar");
		emp3.setName("_baz");
		
		emp1.setDepartment(d);
		emp2.setDepartment(d1);
		emp3.setDepartment(d2);
		
		empid1 = employeeService.create(emp1).getId();
		empid2 = employeeService.create(emp2).getId();
		empid3 = employeeService.create(emp3).getId();
	}

	@After
	public void tearDown() throws Exception {
		departmentService.delete(pid);
		departmentService.delete(sid1);
		departmentService.delete(sid2);
		
		employeeService.delete(empid1);
		employeeService.delete(empid2);
		employeeService.delete(empid3);
	}

	@Test
	public void testExist() {
		assertTrue(departmentService.exist("parent"));
		assertTrue(departmentService.exist("son1"));
		assertTrue(departmentService.exist("son2"));
		assertFalse(departmentService.exist("son3"));
	}

	@Test
	public void testGet() {
		Department pd = departmentService.get(pid);
		assertNotNull(pd);
		System.out.println(gson.toJson(pd));
		
		pd = departmentService.get(sid1);
		assertNotNull(pd);
		System.out.println(gson.toJson(pd));
		
		pd = departmentService.get(sid2);
		assertNotNull(pd);
		System.out.println(gson.toJson(pd));
	}

	@Test
	public void testQuery() {
		Department d = departmentService.get(pid);
		Paging<Department> page = departmentService.query(d, pageable);
		assertFalse(page.getContent().isEmpty());
		List<Department> ls = departmentService.query(d);
		assertFalse(ls.isEmpty());
		
		d = departmentService.get(sid1);
		page = departmentService.query(d, pageable);
		assertFalse(page.getContent().isEmpty());
		ls = departmentService.query(d);
		assertFalse(ls.isEmpty());
		
		d = departmentService.get(sid2);
		page = departmentService.query(d, pageable);
		assertFalse(page.getContent().isEmpty());
		ls = departmentService.query(d);
		assertFalse(ls.isEmpty());
		
		page = departmentService.query(null, pageable);
		assertFalse(page.getContent().isEmpty());
		ls = departmentService.query(d);
		assertFalse(ls.isEmpty());
		
		System.out.println(gson.toJson(page));
		System.out.println(gson.toJson(ls));
		
	}

	@Test
	public void testUpdate() {
		Department ud = new Department();
		ud.setName("update");
		ud.setDescription("update");
		Department son1 = departmentService.get(sid1);
		ud.setParent(son1);
		Department newDepart = departmentService.update(sid2, ud);
		assertEquals("update", newDepart.getName());
		assertEquals("update", newDepart.getDescription());
		assertEquals(son1, newDepart.getParent());
	}

}
