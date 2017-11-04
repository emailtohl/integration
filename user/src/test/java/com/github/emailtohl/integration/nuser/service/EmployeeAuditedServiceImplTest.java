package com.github.emailtohl.integration.nuser.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.envers.RevisionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.entity.Image;
import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.nuser.UserTestData;
import com.github.emailtohl.integration.nuser.entities.Employee;
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
public class EmployeeAuditedServiceImplTest {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = EmployeeServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	EmployeeService employeeService;
	@Inject
	EmployeeAuditedService auditedService;
	
	Long id;
	
	@Before
	public void setUp() throws Exception {
		Employee e = new Employee();
		e.setName("haha");
		e.setNickname("haha");
		e.setPassword("112233");
		e.setEmail("haha@test.com");
		e.setTelephone("112342513514");
		e.setDescription("系统分析人员");
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
		
		Employee tar = new Employee();
		tar.setDescription("update");
		tar.setDepartment(new UserTestData().qa);
		Employee u = employeeService.update(id, tar);
		assertEquals(tar.getDescription(), u.getDescription());
		assertEquals(tar.getDepartment(), u.getDepartment());
	}

	@After
	public void tearDown() throws Exception {
		employeeService.delete(id);
	}

	@Test
	public void testGetEmployeeRevision() {
		List<Tuple<Employee>> ls = auditedService.getEmployeeRevision(id);
		assertTrue(ls.size() == 2);// 一个新增、一个修改
		assertEquals(ls.get(0).getEntity().getDescription(), "系统分析人员");
		assertEquals(ls.get(0).getRevisionType(), RevisionType.ADD);
		assertEquals(ls.get(1).getRevisionType(), RevisionType.MOD);
		
	}

	@Test
	public void testGetEmployeeAtRevision() {
		List<Tuple<Employee>> ls = auditedService.getEmployeeRevision(id);
		Integer revision = ls.get(0).getDefaultRevisionEntity().getId();
		Employee e = auditedService.getEmployeeAtRevision(id, revision);
		assertEquals("系统分析人员", e.getDescription());
	}

}
