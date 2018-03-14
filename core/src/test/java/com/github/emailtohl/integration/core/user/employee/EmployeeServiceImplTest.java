package com.github.emailtohl.integration.core.user.employee;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.encryption.myrsa.KeyGenerator;
import com.github.emailtohl.integration.common.encryption.myrsa.KeyPairs;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.Gender;
import com.google.gson.Gson;
/**
 * 业务类测试
 * @author HeLei
 */
public class EmployeeServiceImplTest extends CoreTestEnvironment {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = EmployeeServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	EmployeeService employeeService;
	@Inject
	Gson gson;
	@Inject
	CorePresetData cpd;
	@Inject
	CoreTestData td;
	@Value("${" + Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD +"}")
	String employeeDefaultPassword;
	Long id;

	@Before
	public void setUp() throws Exception {
		Employee e = new Employee();
		e.setName("haha");
		e.setNickname("haha");
		e.setPassword("112233");
		e.setEmail("haha@test.com");
		e.setCellPhone("17767853456");
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
		e.setDepartment(cpd.product);
		e = employeeService.create(e);
		id = e.getId();
	}

	@After
	public void tearDown() throws Exception {
		employeeService.delete(id);
	}

	@Test
	public void testExist() {
		assertFalse(employeeService.exist(null));
		assertTrue(employeeService.exist("haha@test.com"));
		assertTrue(employeeService.exist("17767853456"));
		assertFalse(employeeService.exist("12321312353"));
	}

	@Test
	public void testGet() {
		Employee e = employeeService.get(id);
		assertNotNull(e);
		System.out.println(gson.toJson(e));
		e = employeeService.getByEmpNum(e.getEmpNum());
		assertNotNull(e);
		e = employeeService.getByEmail(e.getEmail());
		assertNotNull(e);
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
		Employee newEntity = new Employee();
		newEntity.setName("lala");
		newEntity.setNickname("bar");
		newEntity.setPassword("556677");
		newEntity.setEmail("barbar@test.com");
		newEntity.setTelephone("998877");
		newEntity.setDescription("质量人员");
		newEntity.setGender(Gender.MALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-bar.jpg")) {
			newEntity.setBirthday(sdf.parse("1990-12-22"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			newEntity.setImage(new Image("icon-head-bar.jpg", "download/img/icon-head-bar.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		newEntity.setPost("系统分析师");
		newEntity.setSalary(10000.00);
		newEntity.setDepartment(cpd.qa);
		Employee e = employeeService.update(id, newEntity);
		assertEquals(newEntity.getDescription(), e.getDescription());
		assertEquals(newEntity.getDepartment(), e.getDepartment());
	}

	@Test
	public void testGrandRoles() {
		Employee e = employeeService.grandRoles(id, cpd.role_staff.getName(), cpd.role_guest.getName());
		assertFalse(e.getRoles().isEmpty());
	}
	
	@Test
	public void testResetPassword() {
		ExecResult r = employeeService.resetPassword(0L);
		assertFalse(r.ok);
		r = employeeService.resetPassword(id);
		assertTrue(r.ok);
		Employee e = employeeService.get(id);
		r = employeeService.login(e.getEmpNum(), "234572adfa");
		assertFalse(r.ok);
		r = employeeService.login(e.getEmpNum(), employeeDefaultPassword);
		assertTrue(r.ok);
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
	public void testSetPublicKey() {
		KeyGenerator kg = new KeyGenerator();
		KeyPairs k = kg.generateKeys(128);
		employeeService.setPublicKey(id, k.getPublicKey().toString());
		Employee e = employeeService.get(id);
		assertEquals(k.getPublicKey().toString(), e.getPublicKey());
	}
	
	/**
	 * Maven测试时若Search失败，很可能是因为找不到Lucene索引。
	 * 典型例子就是每个test的Profiles不一致，即创建索引在内存中（Profiles.DB_RAM_H2）而Search使用索引却在文件系统中找。
	 */
	@Test
	public void testSearch() {
		Paging<Employee> p = employeeService.search(null, pageable);
		System.out.println(gson.toJson(p));
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		p = employeeService.search("haha", pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		p = employeeService.search("haha@test.com", pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		p = employeeService.search("17767853456", pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		// test role name
		Employee emp = employeeService.grandRoles(id, cpd.role_staff.getName());
		System.out.println(emp.getRoles());
		p = employeeService.search(cpd.role_staff.getName(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		// test Integer
		p = employeeService.search(emp.getEmpNum().toString(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		// test Double
		p = employeeService.search(emp.getSalary().toString(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
		
		// test Enum
		p = employeeService.search(emp.getGender().name(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(e -> System.out.println(e));
	}
	
	@Test
	public void testLogin() {
		ExecResult r = employeeService.login(0, "123");
		assertFalse(r.ok);
		r = employeeService.login(td.bar.getEmpNum(), "123");
		assertFalse(r.ok);
		r = employeeService.login(td.bar.getEmpNum(), "123456");
		assertTrue(r.ok);
	}
	
	@Test
	public void testGetRef() {
		EmployeeRef ref = employeeService.getRef(id);
		assertNotNull(ref);
		System.out.println(gson.toJson(ref));
	}
	
	@Test
	public void testFindRefByEmpNum() {
		EmployeeRef ref = employeeService.findRefByEmpNum(td.bar.getEmpNum());
		assertNotNull(ref);
		ref = employeeService.findRefByEmpNum(td.bar.getEmpNum());
		assertNotNull(ref);
		System.out.println(gson.toJson(ref));
	}
	
	@Test
	public void testQueryRefPageable() {
		Paging<EmployeeRef> p = employeeService.queryRef(null, pageable);
		assertFalse(p.getContent().isEmpty());
		
		System.out.println(gson.toJson(p));
		
		EmployeeRef params = new EmployeeRef();
		params.setCellPhone(td.bar.getCellPhone());
		params.setEmail(td.bar.getEmail());
		p = employeeService.queryRef(params, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		params = new EmployeeRef();
		params.setEmail(td.bar.getEmail());
		p = employeeService.queryRef(params, pageable);
		assertFalse(p.getContent().isEmpty());
	}
	
	@Test
	public void testQueryRef() {
		List<EmployeeRef> ls = employeeService.queryRef(null);
		assertFalse(ls.isEmpty());
		
		System.out.println(gson.toJson(ls));
		
		EmployeeRef params = new EmployeeRef();
		params.setCellPhone(td.bar.getCellPhone());
		params.setEmail(td.bar.getEmail());
		ls = employeeService.queryRef(params);
		assertFalse(ls.isEmpty());
		System.out.println(gson.toJson(ls));
		
		params = new EmployeeRef();
		params.setEmpNum(td.bar.getEmpNum());
		ls = employeeService.queryRef(params);
		assertFalse(ls.isEmpty());
	}

}
