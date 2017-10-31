package com.github.emailtohl.integration.nuser.service;

import static org.junit.Assert.*;

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
import com.github.emailtohl.integration.nuser.entities.User;
import com.github.emailtohl.integration.nuser.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.nuser.userTestConfig.ServiceConfiguration;

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
	UserTestData td = new UserTestData();
	Pageable pageable = new PageRequest(0, 20);

	@Test
	public void testQueryStringPageable() {
		Page<User> p = userService.search(td.baz.getName(), pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testQueryUserPageable() {
		User params = new User();
		params.setName(td.bar.getName());
		params.setGender(td.bar.getGender());
		params.setCellPhone(td.bar.getCellPhone());
		Page<User> p = userService.query(params, pageable);
		System.out.println(p.getTotalElements());
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testGet() {
		Page<User> p = userService.query(null, pageable);
		p.getContent().forEach(u -> {
			User uu = userService.get(u.getId());
			assertNotNull(uu);
		});
	}
}
