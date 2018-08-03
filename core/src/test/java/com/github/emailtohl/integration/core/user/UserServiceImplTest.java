package com.github.emailtohl.integration.core.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.lib.jpa.Paging;
import com.google.gson.Gson;

/**
 * 用户服务类测试
 * @author HeLei
 */
public class UserServiceImplTest extends CoreTestEnvironment {
	@Inject
	UserService userService;
	@Inject
	Gson gson;
	@Inject
	CorePresetData cpd;
	@Inject
	CoreTestData td;
	Pageable pageable = PageRequest.of(0, 20);

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
		
		p = userService.search(cpd.role_manager.getName(), pageable);
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
		refParam.setName(td.foo.getName());
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
		u = userService.find(Customer.ANONYMOUS_EMAIL);
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
		u = userService.findRef(Customer.ANONYMOUS_EMAIL);
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
		List<UserRef> ls = userService.findRefByRoleName(cpd.role_admin.getName());
		assertFalse(ls.isEmpty());
		ls = userService.findRefByRoleName(cpd.role_manager.getName());
		assertFalse(ls.isEmpty());
		ls = userService.findRefByRoleName(cpd.role_guest.getName());
		assertFalse(ls.isEmpty());
	}
}
