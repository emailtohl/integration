package com.github.emailtohl.integration.web.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.aop.support.AopUtils.isAopProxy;
import static org.springframework.aop.support.AopUtils.isCglibProxy;
import static org.springframework.aop.support.AopUtils.isJdkDynamicProxy;

import javax.inject.Inject;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.role.RoleType;
import com.github.emailtohl.integration.web.config.WebTestData;
import com.github.emailtohl.integration.web.config.WebTestEnvironment;

/**
 * 对切面的测试
 * @author HeLei
 */
public class RoleServiceProxyTest extends WebTestEnvironment {
	@Inject
	RoleService roleService;
	@Inject
	RoleServiceProxy roleServiceProxy;
	@Inject
	IdentityService identityService;
	@Inject
	WebTestData td;
	
	final String groupId = "test";
	Long roleId;
	
	@Before
	public void setUp() throws Exception {
		System.out.println(isAopProxy("isAopProxy: " + roleServiceProxy));
        System.out.println(isCglibProxy("isCglibProxy: " + roleServiceProxy));
        System.out.println(isJdkDynamicProxy("isJdkDynamicProxy: " + roleServiceProxy));
        
        Role r = new Role(groupId, RoleType.CUSTOMER, "for test");
        r = roleService.create(r);
        roleId = r.getId();
        
        Group g = identityService.createGroupQuery().groupId(groupId).singleResult();
        assertNotNull(g);
	}

	@After
	public void tearDown() throws Exception {
		roleService.delete(roleId);
		Group g = identityService.createGroupQuery().groupId(groupId).singleResult();
        assertNull(g);
	}

	@Test
	public void testUpdate() {
		Role newRole = new Role("update", RoleType.EMPLOYEE, "update for test");
		newRole = roleService.update(roleId, newRole);
		Group g = identityService.createGroupQuery().groupId(groupId).singleResult();
        assertEquals(RoleType.EMPLOYEE.name(), g.getType());
	}

}
