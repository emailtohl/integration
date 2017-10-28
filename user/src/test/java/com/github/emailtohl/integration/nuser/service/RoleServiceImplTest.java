package com.github.emailtohl.integration.nuser.service;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.nuser.UserTestData;
import com.github.emailtohl.integration.nuser.entities.Authority;
import com.github.emailtohl.integration.nuser.entities.Role;
import com.github.emailtohl.integration.nuser.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.nuser.userTestConfig.ServiceConfiguration;

/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
public class RoleServiceImplTest {
	@Inject
	RoleService roleService;

	@Test
	public void testCRUD() {
		UserTestData td = new UserTestData();
		Role r = new Role("test_role", "for test");
		r.getAuthorities().addAll(Arrays.asList(
			new Authority("not exist", "不存在的权限", null),
			td.customer_lock, td.customer_reset_password
		));
		
		r = roleService.create(r);
		Long id = r.getId();
		r = roleService.get(id);
		assertEquals(2, r.getAuthorities().size());
		r.getAuthorities().remove(td.customer_lock);
		r.getAuthorities().add(td.customer_level);
		r.getAuthorities().add(td.employee_role);
		r.setDescription("for update");
		roleService.update(id, r);
		r = roleService.get(id);
		assertEquals("for update", r.getDescription());
		assertEquals(3, r.getAuthorities().size());
		roleService.delete(id);
		r = roleService.get(id);
		assertNull(r);
	}

	@Test
	public void testExist() {
		UserTestData td = new UserTestData();
		boolean b = roleService.exist("name", td.role_guest.getName());
		assertTrue(b);
		b = roleService.exist("name", "foo");
		assertFalse(b);
	}

	@Test
	public void testQueryRolePageable() {
		Pageable pageable = new PageRequest(0, 20);
		Page<Role> p = roleService.query(null, pageable);
		assertFalse(p.getContent().isEmpty());
		UserTestData td = new UserTestData();
		p = roleService.query(td.role_manager, pageable);
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testQueryRole() {
		List<Role> ls = roleService.query(null);
		assertFalse(ls.isEmpty());
		UserTestData td = new UserTestData();
		ls = roleService.query(td.role_staff);
		assertFalse(ls.isEmpty());
	}

	@Test
	public void testGetAuthorities() {
		List<Authority> ls = roleService.getAuthorities();
		assertFalse(ls.isEmpty());
	}

}
