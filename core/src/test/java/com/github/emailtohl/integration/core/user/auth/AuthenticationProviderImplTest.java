package com.github.emailtohl.integration.core.user.auth;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class AuthenticationProviderImplTest {
	@Inject
	@Named("authenticationProvider")
	AuthenticationProvider authenticationProvider;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSupports() {
		assertTrue(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
	}

}
