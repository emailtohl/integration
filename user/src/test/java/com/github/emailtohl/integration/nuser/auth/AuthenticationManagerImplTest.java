package com.github.emailtohl.integration.nuser.auth;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.nuser.UserTestData;
import com.github.emailtohl.integration.nuser.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.nuser.userTestConfig.ServiceConfiguration;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
public class AuthenticationManagerImplTest {
	@Inject
	@Named("authenticationProviderImpl")
	AuthenticationManager authenticationManager;
	UserTestData td = new UserTestData();
	Encipher encipher = new Encipher();
	String publicKey, privateKey;
	
	@Before
	public void setUp() throws Exception {
		String[] pair = encipher.getKeyPairs(256);
		publicKey = pair[0];
		privateKey = pair[1];
		((AuthenticationProviderImpl) authenticationManager).setPrivateKey(privateKey);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAuthenticateAuthentication() {
		String crypted = encipher.encrypt(UserTestData.TEST_PASSWORD, publicKey);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(td.bar.getEmpNum().toString(), crypted);
		authenticationManager.authenticate(token);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUsernameNotFoundException() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("abc", UserTestData.TEST_PASSWORD);
		authenticationManager.authenticate(token);
	}
	

}
