package com.github.emailtohl.integration.core.user.service;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.entity.Image;
import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User.Gender;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
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
		e.setTelephone("13534567230");
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
		e.setPost("系统分析师");
		e.setSalary(10000.00);
		e.setDepartment(new CoreTestData().product);
		e = employeeService.create(e);
		id = e.getId();
		
		Employee tar = new Employee();
		tar.setDescription("update");
		tar.setDepartment(new CoreTestData().qa);
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
		assertTrue(ls.size() >= 2);// 一个新增、一个修改
//		在Maven统一执行时有其他用例修改数据，所以届时得到的结果会不一致
//		assertEquals(ls.get(0).getEntity().getDescription(), "系统分析人员");
//		assertEquals(ls.get(0).getRevisionType(), RevisionType.ADD);
//		assertEquals(ls.get(1).getRevisionType(), RevisionType.MOD);
		
	}

	@Test
	public void testGetEmployeeAtRevision() {
		List<Tuple<Employee>> ls = auditedService.getEmployeeRevision(id);
		Integer revision = ls.get(0).getDefaultRevisionEntity().getId();
		Employee e = auditedService.getEmployeeAtRevision(id, revision);
		assertNotNull(e);
//		在Maven统一执行时有其他用例修改数据，所以届时得到的结果会不一致
//		assertEquals(new UserTestData().qa, e.getDepartment());
	}

}
