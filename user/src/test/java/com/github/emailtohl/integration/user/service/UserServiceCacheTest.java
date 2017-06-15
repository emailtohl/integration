package com.github.emailtohl.integration.user.service;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.user.UserTestData;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.userTestConfig.CacheConfiguration;
import com.github.emailtohl.integration.user.userTestConfig.DataSourceConfiguration;

/**
 * 业务类测试
 * @author HeLei
 * @date 2017.06.15
 */
@Transactional
@Rollback(false)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CacheConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.POSTGRESQL_DB)
public class UserServiceCacheTest {
	@Inject
	UserService userService;
	@Inject
	CacheManager cacheManager;
	
	@Test
	public void testCache() throws ResourceNotFoundException {
		UserTestData td = new UserTestData();
		String email = td.foo.getEmail();
		User u = userService.getUserByEmail(email);
		assertNotNull(u);
		// 打断点可以看见返回为null可以被缓存，且在第二次调用时直接返回了缓存结果
		u = userService.getUserByEmail(email);
		assertNotNull(u);
		
		Cache c = cacheManager.getCache(UserService.CACHE_NAME_USER);
		Object v = c.get(email).get();
		assertTrue(v instanceof Employee);
		Employee e = (Employee) v;
		assertEquals(email, e.getEmail());
		
		UserTestData td1 = new UserTestData();
		td1.foo.setDescription("update");
		
		userService.mergeEmployee(email, td1.foo);
		v = c.get(email).get();
		e = (Employee) v;
		assertEquals(td1.foo.getDescription(), e.getDescription());
		
		userService.mergeEmployee(email, td.foo);
		v = c.get(email).get();
		e = (Employee) v;
		assertEquals(td.foo.getDescription(), e.getDescription());
	}
}
