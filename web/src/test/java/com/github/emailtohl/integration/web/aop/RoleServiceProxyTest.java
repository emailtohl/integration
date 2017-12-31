package com.github.emailtohl.integration.web.aop;

import static org.junit.Assert.*;
import static org.springframework.aop.support.AopUtils.isAopProxy;
import static org.springframework.aop.support.AopUtils.isCglibProxy;
import static org.springframework.aop.support.AopUtils.isJdkDynamicProxy;

import javax.inject.Inject;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.role.RoleType;
import com.github.emailtohl.integration.web.WebTestData;

/**
 * 对切面的测试
 * 
 * @author HeLei
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class RoleServiceProxyTest {
	@Inject
	RoleService roleService;
	@Inject
	RoleServiceProxy roleServiceProxy;
	@Inject
	IdentityService identityService;
	@Inject
	WebTestData td;
	String roleId;
	
	@Before
	public void setUp() throws Exception {
		System.out.println(isAopProxy("isAopProxy: " + roleServiceProxy));
        System.out.println(isCglibProxy("isCglibProxy: " + roleServiceProxy));
        System.out.println(isJdkDynamicProxy("isJdkDynamicProxy: " + roleServiceProxy));
        
        Role r = new Role("test", RoleType.CUSTOMER, "for test");
        r = roleService.create(r);
        roleId = r.getId().toString();
        
        Group g = identityService.createGroupQuery().groupId(roleId).singleResult();
        assertNotNull(g);
	}

	@After
	public void tearDown() throws Exception {
		roleService.delete(Long.valueOf(roleId));
		Group g = identityService.createGroupQuery().groupId(roleId).singleResult();
        assertNull(g);
	}

	@Test
	public void testUpdate() {
		Role newRole = new Role("update", RoleType.EMPLOYEE, "update for test");
		newRole = roleService.update(Long.valueOf(roleId), newRole);
		Group g = identityService.createGroupQuery().groupType(RoleType.CUSTOMER.name()).singleResult();
        assertNull(g);
        g = identityService.createGroupQuery().groupType(RoleType.EMPLOYEE.name()).singleResult();
        assertNotNull(g);
	}

}
