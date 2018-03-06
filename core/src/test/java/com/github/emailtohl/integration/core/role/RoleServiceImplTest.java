package com.github.emailtohl.integration.core.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
import com.google.gson.Gson;

/**
 * 业务类测试
 * @author HeLei
 */
public class RoleServiceImplTest extends CoreTestEnvironment {
	@Inject
	RoleService roleService;
	@Inject
	Gson gson;
	@Inject
	CorePresetData cpd;

	@Test
	public void testCRUD() {
		Role r = new Role("test_role", RoleType.EMPLOYEE, "for test");
		r.getAuthorities().addAll(Arrays.asList(
			new Authority("not exist", "不存在的权限", null),
			cpd.auth_customer_lock, cpd.auth_customer_reset_password
		));
		
		r = roleService.create(r);
		Long id = r.getId();
		r = roleService.get(id);
		assertEquals(2, r.getAuthorities().size());
		
		r = roleService.get("test_role");
		assertNotNull(r);
		
		r.getAuthorities().remove(cpd.auth_customer_lock);
		r.getAuthorities().add(cpd.auth_customer_level);
		r.getAuthorities().add(cpd.auth_employee_role);
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
		boolean b = roleService.exist(cpd.role_guest.getName());
		assertTrue(b);
		b = roleService.exist("foo");
		assertFalse(b);
	}

	@Test
	public void testQueryRolePageable() {
		Pageable pageable = new PageRequest(0, 20);
		Paging<Role> p = roleService.query(null, pageable);
		assertFalse(p.getContent().isEmpty());
		p = roleService.query(cpd.role_manager, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
	}

	@Test
	public void testQueryRole() {
		List<Role> ls = roleService.query(null);
		assertFalse(ls.isEmpty());
		ls = roleService.query(cpd.role_staff);
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
