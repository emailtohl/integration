package com.github.emailtohl.integration.core.user;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static com.github.emailtohl.integration.core.user.Constant.ADMIN_NAME;
import static com.github.emailtohl.integration.core.user.Constant.ANONYMOUS_EMAIL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
import com.github.emailtohl.integration.core.user.entities.User;
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
	public void testQueryStringPageable() {
		Paging<User> p = userService.search(td.baz.getName(), pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		CoreTestData td = new CoreTestData();
		p = userService.search(td.role_manager.getName(), pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
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
	}

	@Test
	public void testGet() {
		Paging<User> p = userService.query(null, pageable);
		p.getContent().forEach(u -> {
			User uu = userService.get(u.getId());
			assertNotNull(uu);
		});
		System.out.println(gson.toJson(p));
	}
	
	@Test
	public void testFind() {
		User u = userService.find(ADMIN_NAME);
		assertNotNull(u);
		u = userService.find(ANONYMOUS_EMAIL);
		assertNotNull(u);
		u = userService.find(td.foo.getEmpNum().toString());
		assertNotNull(u);
		u = userService.find(td.baz.getCellPhone());
		assertNotNull(u);
		u = userService.find(td.baz.getEmail());
		assertNotNull(u);
	}
}
