package com.github.emailtohl.integration.core.auth;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
/**
 * 业务类测试
 * @author HeLei
 */
public class AuthenticationProviderImplTest extends CoreTestEnvironment {
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
