package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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
import com.google.gson.Gson;

/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class RoleServiceImplTest {
	@Inject
	RoleService roleService;
	@Inject
	Gson gson;

	@Test
	public void testCRUD() {
		CoreTestData td = new CoreTestData();
		Role r = new Role("test_role", RoleType.EMPLOYEE, "for test");
		r.getAuthorities().addAll(Arrays.asList(
			new Authority("not exist", "不存在的权限", null),
			td.auth_customer_lock, td.auth_customer_reset_password
		));
		
		r = roleService.create(r);
		Long id = r.getId();
		r = roleService.get(id);
		assertEquals(2, r.getAuthorities().size());
		
		r = roleService.get("test_role");
		assertNotNull(r);
		
		r.getAuthorities().remove(td.auth_customer_lock);
		r.getAuthorities().add(td.auth_customer_level);
		r.getAuthorities().add(td.auth_employee_role);
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
		CoreTestData td = new CoreTestData();
		boolean b = roleService.exist(td.role_guest.getName());
		assertTrue(b);
		b = roleService.exist("foo");
		assertFalse(b);
	}

	@Test
	public void testQueryRolePageable() {
		Pageable pageable = new PageRequest(0, 20);
		Paging<Role> p = roleService.query(null, pageable);
		assertFalse(p.getContent().isEmpty());
		CoreTestData td = new CoreTestData();
		p = roleService.query(td.role_manager, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
	}

	@Test
	public void testQueryRole() {
		List<Role> ls = roleService.query(null);
		assertFalse(ls.isEmpty());
		CoreTestData td = new CoreTestData();
		ls = roleService.query(td.role_staff);
		assertFalse(ls.isEmpty());
		System.out.println(gson.toJson(ls));
	}

	@Test
	public void testGetAuthorities() {
		List<Authority> ls = roleService.getAuthorities();
		assertFalse(ls.isEmpty());
		System.out.println(gson.toJson(ls));
	}

}
