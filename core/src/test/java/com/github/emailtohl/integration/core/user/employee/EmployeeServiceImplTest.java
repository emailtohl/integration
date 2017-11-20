package com.github.emailtohl.integration.core.user.employee;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User.Gender;
import com.google.gson.Gson;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
@Rollback(false)
public class EmployeeServiceImplTest {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = EmployeeServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	EmployeeService employeeService;
	@Inject
	Gson gson;
	Long id;

	@Before
	public void setUp() throws Exception {
		Employee e = new Employee();
		e.setName("haha");
		e.setNickname("haha");
		e.setPassword("112233");
		e.setEmail("haha@test.com");
		e.setTelephone("17812345678");
		e.setDescription("做单元测试");
		e.setGender(Gender.MALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-foo.jpg")) {
			e.setBirthday(sdf.parse("1990-12-13"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			e.setImage(new Image("icon-head-foo.jpg", "download/img/icon-head-foo.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		e.setPost("系统分析师");
		e.setSalary(10000.00);
		e.setDepartment(new CoreTestData().product);
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
		System.out.println(gson.toJson(e));
		List<Employee> ls = employeeService.findByName(e.getName());
		assertFalse(ls.isEmpty());
		System.out.println(ls);
	}

	@Test
	public void testQueryEmployeePageable() {
		Paging<Employee> p = employeeService.query(new Employee(), pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
	}

	@Test
	public void testQueryEmployee() {
		List<Employee> ls = employeeService.query(new Employee());
		assertFalse(ls.isEmpty());
		System.out.println(gson.toJson(ls));
	}

	@Test
	public void testUpdate() {
		Employee e = new Employee();
		e.setName("lala");
		e.setNickname("bar");
		e.setPassword("556677");
		e.setEmail("barbar@test.com");
		e.setTelephone("998877");
		e.setDescription("质量人员");
		e.setGender(Gender.MALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-bar.jpg")) {
			e.setBirthday(sdf.parse("1990-12-22"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			e.setImage(new Image("icon-head-bar.jpg", "download/img/icon-head-bar.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		e.setPost("系统分析师");
		e.setSalary(10000.00);
		e.setDepartment(new CoreTestData().qa);
		Employee u = employeeService.update(id, e);
		assertEquals(e.getDescription(), u.getDescription());
		assertEquals(e.getDepartment(), u.getDepartment());
	}

	@Test
	public void testGrandRoles() {
		CoreTestData td = new CoreTestData();
		Employee e = employeeService.grandRoles(id, td.role_staff.getName(), td.role_guest.getName());
		assertFalse(e.getRoles().isEmpty());
	}

	@Test
	public void testUpdatePassword() {
		Employee e = employeeService.get(id);
		ExecResult r = employeeService.updatePassword(e.getEmpNum(), "00000", "445566");
		assertFalse(r.ok);
		r = employeeService.updatePassword(e.getEmpNum(), "112233", "445566");
		assertTrue(r.ok);
		e = employeeService.get(id);
		r = employeeService.login(e.getEmpNum(), "445566");
		assertTrue(r.ok);
	}

	@Test
	public void testEnabled() {
		employeeService.enabled(id, false);
		Employee e = employeeService.get(id);
		assertFalse(e.getEnabled());
	}
	
	@Test
	public void testLogin() {
		ExecResult r = employeeService.login(0, "123");
		assertFalse(r.ok);
		CoreTestData td = new CoreTestData();
		r = employeeService.login(td.bar.getEmpNum(), "123");
		assertFalse(r.ok);
		r = employeeService.login(td.bar.getEmpNum(), "123456");
		assertTrue(r.ok);
	}

}
