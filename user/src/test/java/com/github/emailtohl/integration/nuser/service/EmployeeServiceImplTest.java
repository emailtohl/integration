package com.github.emailtohl.integration.nuser.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.UserTestData;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.Image;
import com.github.emailtohl.integration.nuser.entities.User.Gender;
import com.github.emailtohl.integration.nuser.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.nuser.userTestConfig.ServiceConfiguration;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
//@Rollback(false)
public class EmployeeServiceImplTest {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = EmployeeServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	EmployeeService employeeService;
	Long id;

	@Before
	public void setUp() throws Exception {
		Employee e = new Employee();
		e.setName("haha");
		e.setNickname("haha");
		e.setPassword("112233");
		e.setEmail("haha@test.com");
		e.setTelephone("112342513514");
		e.setDescription("测试人员");
		e.setGender(Gender.MALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-foo.jpg")) {
			e.setBirthday(sdf.parse("1990-12-13"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			e.setImage(new Image("icon-head-foo.jpg", "download/img/icon-head-foo.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		e.setEmpNum(1);
		e.setPost("系统分析师");
		e.setSalary(10000.00);
		e.setDepartment(new UserTestData().product);
		e = employeeService.create(e);
		id = e.getId();
	}

	@After
	public void tearDown() throws Exception {
		employeeService.delete(id);
	}

	@Test
	public void testExist() {
		assertFalse(employeeService.exist(null, null));
	}

	@Test
	public void testGet() {
		Employee e = employeeService.get(id);
		assertNotNull(e);
	}

	@Test
	public void testQueryEmployeePageable() {
		Page<Employee> p = employeeService.query(new Employee(), pageable);
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testQueryEmployee() {
		List<Employee> ls = employeeService.query(new Employee());
		assertFalse(ls.isEmpty());
	}

	@Test
	public void testUpdate() {
		Employee src = employeeService.get(id);
		Employee tar = new Employee();
		BeanUtils.copyProperties(src, tar);
		tar.setDescription("update");
		tar.setDepartment(new UserTestData().qa);
		Employee u = employeeService.update(id, tar);
		assertEquals(tar.getDescription(), u.getDescription());
		assertEquals(tar.getDepartment(), u.getDepartment());
	}

	@Test
	public void testGrandRoles() {
		UserTestData td = new UserTestData();
		employeeService.grandRoles(id, td.role_staff.getName(), td.role_guest.getName());
	}

	@Test
	public void testUpdatePassword() {
		ExecResult r = employeeService.updatePassword(id, "112233", "445566");
		assertTrue(r.ok);
	}

	@Test
	public void testLock() {
		employeeService.lock(id, false);
	}

}
