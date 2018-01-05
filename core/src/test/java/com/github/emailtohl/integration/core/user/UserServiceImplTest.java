package com.github.emailtohl.integration.core.user;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static com.github.emailtohl.integration.core.config.Constant.ANONYMOUS_EMAIL;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.google.gson.Gson;

/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class UserServiceImplTest {
	@Inject
	UserService userService;
	@Inject
	Gson gson;
	CoreTestData td = new CoreTestData();
	Pageable pageable = new PageRequest(0, 20);

	@Test
	public void testSearchStringPageable() {
		Paging<User> p = userService.search(td.baz.getName(), pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		p = userService.search(td.foo.getEmail(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(u -> {
			if (u.getUserType() == UserType.Employee) {
				Employee e = (Employee) u;
				Paging<User> pp = userService.search(e.getEmpNum().toString(), pageable);
				// 这里为什么查不到，是因为索引的UserRef里面没有empNum字段
				assertTrue(pp.getContent().isEmpty());
			}
		});
		
		p = userService.search(td.role_manager.getName(), pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
	}
	
	@Test
	public void testSearchRefStringPageable() {
		Paging<UserRef> p = userService.searchRef(td.baz.getName(), pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		p = userService.searchRef(td.foo.getEmail(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(u -> {
			if (u.getUserType() == UserType.Employee) {
				EmployeeRef e = (EmployeeRef) u;
				Paging<UserRef> pp = userService.searchRef(e.getEmpNum().toString(), pageable);
				// 这里为什么查不到，是因为索引的UserRef里面没有empNum字段
				assertTrue(pp.getContent().isEmpty());
			}
		});
		System.out.println(gson.toJson(p));
		
		p = userService.searchRef(td.bar.getCellPhone(), pageable);
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testQueryUserPageable() {
		User params = new User();
		params.setName(td.bar.getName());
		params.setGender(td.bar.getGender());
		params.setCellPhone(td.bar.getCellPhone());
		Paging<User> p = userService.query(params, pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		Employee ep = new Employee();
		ep.setEmpNum(td.foo.getEmpNum());
		p = userService.query(params, pageable);
		assertFalse(p.getContent().isEmpty());
		
		p = userService.query(null, pageable);
		assertFalse(p.getContent().isEmpty());
	}
	
	@Test
	public void testQueryUserRefPageable() {
		UserRef params = new UserRef();
		params.setName(td.bar.getName());
		params.setCellPhone(td.bar.getCellPhone());
		Paging<UserRef> p = userService.queryRef(params, pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		EmployeeRef refParam = new EmployeeRef();
		refParam.setEmpNum(td.foo.getEmpNum());
		p = userService.queryRef(refParam, pageable);
		assertFalse(p.getContent().isEmpty());
		
		p = userService.queryRef(null, pageable);
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testGet() {
		Paging<User> p = userService.query(null, pageable);
		p.getContent().forEach(u -> {
			User uu = userService.get(u.getId());
			assertNotNull(uu);
		});
		System.out.println(gson.toJson(p));
		User uu = userService.get(1L);
		assertNull(uu);
	}
	
	@Test
	public void testGetRef() {
		Paging<User> p = userService.query(null, pageable);
		p.getContent().forEach(u -> {
			UserRef ref = userService.getRef(u.getId());
			assertNotNull(ref);
			System.out.println(gson.toJson(ref));
		});
		UserRef ref = userService.getRef(1L);
		assertNull(ref);
	}
	
	@Test
	public void testFind() {
		User u = userService.find("" + Employee.NO_BOT);
		assertNotNull(u);
		u = userService.find("" + Employee.NO1);
		assertNotNull(u);
		u = userService.find(ANONYMOUS_EMAIL);
		assertNotNull(u);
		u = userService.find(td.foo.getEmpNum().toString());
		assertNotNull(u);
		u = userService.find(td.baz.getCellPhone());
		assertNotNull(u);
		u = userService.find(td.baz.getEmail());
		assertNotNull(u);
		
		// 通过客户账号的邮箱以及手机号查询
		u = userService.find(td.baz.getCellPhone());
		assertNotNull(u);
		u = userService.find(td.baz.getEmail());
		assertNotNull(u);
	}
	
	@Test
	public void testFindAndRefreshLastLogin() {
		User u = userService.find(td.baz.getCellPhone());
		Date lastLogin = u.getLastLogin();
		u = userService.findAndRefreshLastLogin(td.baz.getCellPhone());
		assertNotEquals(lastLogin, u.getLastLogin());
	}
	
	@Test
	public void testFindRef() {
		UserRef u = userService.findRef("" + Employee.NO_BOT);
		assertNotNull(u);
		u = userService.findRef("" + Employee.NO1);
		assertNotNull(u);
		u = userService.findRef(ANONYMOUS_EMAIL);
		assertNotNull(u);
		u = userService.findRef(td.foo.getEmpNum().toString());
		assertNotNull(u);
		u = userService.findRef(td.baz.getCellPhone());
		assertNotNull(u);
		u = userService.findRef(td.baz.getEmail());
		assertNotNull(u);
		
		// 通过平台账号的邮箱以及手机号查询
		u = userService.findRef(td.bar.getCellPhone());
		assertNotNull(u);
		u = userService.findRef(td.bar.getEmail());
		assertNotNull(u);
	}
	
	@Test
	public void testFindRefByRoleName() {
		List<UserRef> ls = userService.findRefByRoleName(td.role_admin.getName());
		assertFalse(ls.isEmpty());
		ls = userService.findRefByRoleName(td.role_manager.getName());
		assertFalse(ls.isEmpty());
		ls = userService.findRefByRoleName(td.role_guest.getName());
		assertFalse(ls.isEmpty());
	}
}
