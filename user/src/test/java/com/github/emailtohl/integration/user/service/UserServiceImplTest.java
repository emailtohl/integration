package com.github.emailtohl.integration.user.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.common.jpa.Pager;
import com.github.emailtohl.integration.user.UserTestData;
import com.github.emailtohl.integration.user.dao.CleanAuditData;
import com.github.emailtohl.integration.user.dao.RoleRepository;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.Role;
import com.github.emailtohl.integration.user.entities.Subsidiary;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.entities.User.Gender;
import com.github.emailtohl.integration.user.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.user.userTestConfig.ServiceConfiguration;
import com.google.gson.Gson;

import javassist.NotFoundException;
/**
 * 业务类测试
 * @author HeLei
 * @date 2017.02.04
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
public class UserServiceImplTest {
	static final Logger logger = LogManager.getLogger();
	@Inject @Named("userServiceImpl") UserService userService;
	@Inject RoleRepository roleRepository;
	@Inject CleanAuditData cleanAuditData;
	@Inject CacheManager cacheManager;
	Gson gson = new Gson();
	Employee emp;
	Customer cus;
	
	@Before
	public void setUp() {
		UserTestData td = new UserTestData();
		emp = new Employee();
		emp.setAddress("四川路");
		emp.setAge(20);
		emp.getRoles().addAll(Arrays.asList(td.employee, td.manager));
		td.employee.getUsers().add(emp);
		td.manager.getUsers().add(emp);
		emp.setBirthday(Date.from(Instant.now().minus(Duration.ofDays(10000))));
		emp.setDescription("test");
		emp.setEmail("testEmp@test.com");
		emp.setPassword("1234567890");
		emp.setName("name");
		emp.setTelephone("123456789");
		emp.setUsername("username");
		emp.setGender(Gender.MALE);
		Subsidiary ec = new Subsidiary();
		ec.setCity("成都");
		ec.setCountry("中国");
		ec.setLanguage("zh");
		ec.setProvince("四川");
		emp.setSubsidiary(ec);
//		Set<ConstraintViolation<User>> set = Validator.validate(emp);
//		logger.debug(set);
		
		cus = new Customer();
		cus.setAddress("四川路");
		cus.setAge(20);
		cus.getRoles().add(td.user);
		td.user.getUsers().add(cus);
		cus.setBirthday(Date.from(Instant.now().minus(Duration.ofDays(10000))));
		cus.setDescription("test");
		cus.setEmail("testCus@test.com");
		cus.setPassword("1234567890");
		cus.setName("name");
		cus.setTelephone("123456789");
		cus.setUsername("username");
		cus.setGender(Gender.FEMALE);
		Subsidiary cc = new Subsidiary();
		cc.setCity("成都");
		cc.setCountry("中国");
		cc.setLanguage("zh");
		cc.setProvince("四川");
		cus.setSubsidiary(cc);
		cus.setTitle("客户甲乙丙");
		cus.setAffiliation("某某科技公司");
		
//		set = Validator.validate(cus);
//		logger.debug(set);
	}
	
	@Test
	public void testExist() {
		
	}
	
	@Test
	public void testCRUD1() {
		Long id = userService.addEmployee(emp).getId();
		
		assertTrue(userService.exist(emp.getUsername()));
		assertFalse(userService.exist("~*&^$"));
		assertFalse(userService.exist(""));
		assertFalse(userService.exist(null));
		
		try {
			assertNotNull(id);
			// test query
			User qu = userService.getUser(id);
			assertTrue(qu instanceof Employee);
			assertEquals(emp.getEmail(), qu.getEmail());
			// test update
			Employee uu = new Employee();
			uu.setRoles(null);
			uu.setDescription("已修改");
			userService.mergeEmployee(emp.getEmail(), uu);
			qu = userService.getUser(id);
			assertEquals("已修改", qu.getDescription());
			// test enable
			userService.enableUser(id);
			qu = userService.getUser(id);
			assertTrue(qu.getEnabled());
			// test disable
			userService.disableUser(id);
			qu = userService.getUser(id);
			assertFalse(qu.getEnabled());
			// test grantRoles
			userService.grantRoles(id, Role.EMPLOYEE, Role.MANAGER);
			qu = userService.getUser(id);
			assertTrue(qu.getRoles().contains(roleRepository.findByName(Role.EMPLOYEE)));
			assertTrue(qu.getRoles().contains(roleRepository.findByName(Role.MANAGER)));

			userService.grantUserRole(id);
			qu = userService.getUser(id);
			assertTrue(qu.getRoles().contains(roleRepository.findByName(Role.USER)));
		} catch (Exception e) {
			e.printStackTrace();
			fail("不能出现异常");
		} finally {
			userService.deleteUser(id);
			cleanAuditData.cleanUserAudit(id);
		}

	}
	
	@Test
	public void testCRUD2() {
		Long id = userService.addCustomer(cus).getId();
		try {
			assertNotNull(id);
			// test query
			User qu = userService.getUser(id);
			assertTrue(qu instanceof Customer);
			assertEquals(cus.getEmail(), qu.getEmail());
			// test update
			Customer uu = new Customer();
			uu.setRoles(null);
			uu.setDescription("已修改");
			userService.mergeCustomer(cus.getEmail(), uu);
			qu = userService.getUser(id);
			assertEquals("已修改", qu.getDescription());
			// test enable
			userService.enableUser(id);
			qu = userService.getUser(id);
			assertTrue(qu.getEnabled());
			// test disable
			userService.disableUser(id);
			qu = userService.getUser(id);
			assertFalse(qu.getEnabled());

			userService.grantUserRole(id);
			assertTrue(qu.getRoles().contains(roleRepository.findByName(Role.USER)));
			// test grantRoles
			userService.grantRoles(id, Role.EMPLOYEE, Role.MANAGER);
			qu = userService.getUser(id);
			assertTrue(qu.getRoles().contains(roleRepository.findByName(Role.EMPLOYEE)));
			assertTrue(qu.getRoles().contains(roleRepository.findByName(Role.MANAGER)));
			assertFalse(qu.getRoles().contains(roleRepository.findByName(Role.USER)));
		} catch (Exception e) {
			e.printStackTrace();
			fail("不能出现异常");
		} finally {
			userService.deleteUser(id);
			cleanAuditData.cleanUserAudit(id);
		}
	}

	public void testUpdateIcon() throws NotFoundException {
		UserTestData td = new UserTestData();
		String iconSrc = "img/icon-head-foo.jpg";
		ClassLoader cl = UserTestData.class.getClassLoader();
		// cl.getResourceAsStream方法返回的输入流已经是BufferedInputStream对象，无需再装饰
		try (InputStream is = cl.getResourceAsStream(iconSrc)) {
			byte[] icon = new byte[is.available()];
			is.read(icon);
			long id = userService.getUserByEmail(td.foo.getEmail()).getId();
			userService.updateIconSrc(id, iconSrc);
			userService.updateIcon(id, icon);
		} catch (IOException | ResourceNotFoundException e) {
			e.printStackTrace();
			fail("不能出现异常");
		}
	}
	
	@Test
	public void testGetUserPager() {
		UserTestData td = new UserTestData();
		// 查询页从第0页开始
		User u = new User();
		u.setUsername(td.foo.getUsername());
		u.setRoles(td.foo.getRoles());
		u.setEmail(td.foo.getEmail());
		Pager<User> p = userService.getUserPager(u, new PageRequest(0, 20));
		assertTrue(p.getContent().size() > 0);
	}
	
	@Test
	public void testGetUserPage() {
		UserTestData td = new UserTestData();
		// 查询页从第0页开始
		User u = new User();
		u.setUsername(td.foo.getUsername());
		u.setRoles(td.foo.getRoles());
		u.setEmail(td.foo.getEmail());
		Page<User> p = userService.getUserPage(u, new PageRequest(0, 20));
		assertTrue(p.getContent().size() > 0);
	}
	
	@Test
	public void testPublicKey() throws NotFoundException {
		UserTestData td = new UserTestData();
		userService.setPublicKey(td.foo.getEmail(), "eyJtb2R1bGUiOiI5MTIxMzkwMjU2NDM1ODA1MjM4NDg4MDI5NDE3MjgxMzIxNjk4NTYxMDk2NTcwNTE5NDc2OTM4NDQ4NDA1NzgxMjAyMDM4NzM1NzQwNDg0OTczODQ5NzU2MTIzNjE3MjQ1MzI1MzMzMTEzNDMwMzAwMjc4NjIyNjc2NjkwMDEzMzkxOTgxMjAyMTk2NzY5Mjg2MDc3NzMwODkwOTkxODIyMDMzNTk4NjQ1NjkwMzU1NzYxNTU3NjUwNjkwMzI1MTE2NTUzODQ3OTI0NTc5OTk1MTQwNDM0NDkyOTk3NDg0MDg1NjM5ODI2NjU4NzY1NDM4NTE3ODk0Mzg5NTc4NDg1ODYxNDMxMjY3Mzg0OTM3MDE1MzgyMjg2MzAzODYxOTU5NzcyOTA1OTQwNDUzNjMxNjA2OSIsInB1YmxpY0tleSI6IjY1NTM3In0=");
		User u;
		try {
			u = userService.getUserByEmail(td.foo.getEmail());
			assertNotNull(u.getPublicKey());
			userService.clearPublicKey(td.foo.getEmail());
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
			fail("不能出现异常");
		}
	}
	
	@Test
	public void testCache() throws ResourceNotFoundException {
		UserTestData td = new UserTestData();
		String email = td.foo.getEmail();
		User u = userService.getUserByEmail(email);
		assertNotNull(u);
		// 打断点可以看见返回为null可以被缓存，且在第二次调用时直接返回了缓存结果
		u = userService.getUserByEmail(email);
		assertNotNull(u);
		
		Cache c = cacheManager.getCache(UserService.CACHE_NAME_USER);
		Object v = c.get(email).get();
		assertTrue(v instanceof Employee);
		Employee e = (Employee) v;
		assertEquals(email, e.getEmail());
		
		UserTestData td1 = new UserTestData();
		td1.foo.setDescription("update");
		
		userService.mergeEmployee(email, td1.foo);
		v = c.get(email).get();
		e = (Employee) v;
		assertEquals(td1.foo.getDescription(), e.getDescription());
		
		userService.mergeEmployee(email, td.foo);
		v = c.get(email).get();
		e = (Employee) v;
		assertEquals(td.foo.getDescription(), e.getDescription());
	}
}
