package com.github.emailtohl.integration.core.user.service;

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
import com.github.emailtohl.integration.core.user.UserTestData;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.core.user.service.UserService;
import com.github.emailtohl.integration.core.user.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.core.user.userTestConfig.ServiceConfiguration;
import com.google.gson.Gson;

/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
public class UserServiceImplTest {
	@Inject
	UserService userService;
	@Inject
	Gson gson;
	UserTestData td = new UserTestData();
	Pageable pageable = new PageRequest(0, 20);

	@Test
	public void testQueryStringPageable() {
		Paging<User> p = userService.search(td.baz.getName(), pageable);
		System.out.println(p.getTotalElements());
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
}
