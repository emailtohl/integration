package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
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
	@Inject
	CorePresetData cpd;
	
	Long id;

	@Before
	public void setUp() throws Exception {
		Role r = new Role("test_role", RoleType.EMPLOYEE, "for test");
		r.getAuthorities().addAll(Arrays.asList(
			new Authority("not exist", "不存在的权限", null),
			cpd.auth_customer_lock, cpd.auth_customer_reset_password
		));
		
		r = roleService.create(r);
		id = r.getId();
		r = roleService.get(id);
		assertEquals(2, r.getAuthorities().size());
		r.getAuthorities().remove(cpd.auth_customer_lock);
		r.getAuthorities().add(cpd.auth_customer_level);
		r.getAuthorities().add(cpd.auth_employee_role);
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
