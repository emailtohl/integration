package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
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
public class RoleAuditedServiceImplTest {
	@Inject
	RoleAuditedService roleAuditedService;
	@Inject
	RoleService roleService;
	@Inject
	Gson gson;
	
	Long id;

	@Before
	public void setUp() throws Exception {
		CoreTestData td = new CoreTestData();
		Role r = new Role("test_role", "for test");
		r.getAuthorities().addAll(Arrays.asList(
			new Authority("not exist", "不存在的权限", null),
			td.auth_customer_lock, td.auth_customer_reset_password
		));
		
		r = roleService.create(r);
		id = r.getId();
		r = roleService.get(id);
		assertEquals(2, r.getAuthorities().size());
		r.getAuthorities().remove(td.auth_customer_lock);
		r.getAuthorities().add(td.auth_customer_level);
		r.getAuthorities().add(td.auth_employee_role);
		r.setDescription("for update");
		roleService.update(id, r);
	}

	@After
	public void tearDown() throws Exception {
		roleService.delete(id);
	}

	@Test
	public void test() {
		List<Tuple<Role>> ls = roleAuditedService.getRoleRevision(id);
		System.out.println(gson.toJson(ls));
		assertFalse(ls.isEmpty());
		ls.forEach(t -> {
			System.out.println(t);
			int revision = t.getDefaultRevisionEntity().getId();
			Role r = roleAuditedService.getRoleAtRevision(id, revision);
			assertNotNull(r);
			System.out.println(gson.toJson(r));
		});
	}


}
