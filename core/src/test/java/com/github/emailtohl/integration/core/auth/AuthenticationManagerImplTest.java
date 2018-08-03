package com.github.emailtohl.integration.core.auth;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
import com.github.emailtohl.lib.encryption.myrsa.Encipher;
import com.github.emailtohl.lib.exception.InvalidDataException;
/**
 * 业务类测试
 * @author HeLei
 */
public class AuthenticationManagerImplTest extends CoreTestEnvironment {
	@Inject
	AuthenticationManager authenticationManager;// AuthenticationProviderImpl的实例
	@Inject
	CorePresetData cpd;
	@Inject
	CoreTestData td;
	String password = "123456";
	Encipher encipher = new Encipher();
	String publicKey, privateKey;
	
	@Before
	public void setUp() throws Exception {
		String[] pair = encipher.getKeyPairs(256);
		publicKey = pair[0];
		privateKey = pair[1];
		((AuthenticationManagerImpl) authenticationManager).setPrivateKey(privateKey);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAuthenticateAuthentication() {
		String crypted = encipher.encrypt(password, publicKey);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(td.bar.getEmpNum().toString(), crypted);
		authenticationManager.authenticate(token);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUsernameNotFoundException() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("abc", password);
		authenticationManager.authenticate(token);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUsernameNotFoundExceptionBot() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(cpd.user_bot.getEmpNum(), password);
		authenticationManager.authenticate(token);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUsernameNotFoundExceptionAnonymous() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(cpd.user_anonymous.getEmail(), password);
		authenticationManager.authenticate(token);
	}
	
	@Test(expected = InvalidDataException.class)
	public void testInvalidDataException() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(td.bar.getEmpNum().toString(), "123");
		authenticationManager.authenticate(token);
	}
	
	@Test(expected = BadCredentialsException.class)
	public void testBadCredentialsException() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(td.bar.getEmpNum().toString(), encipher.encrypt("123", publicKey));
		authenticationManager.authenticate(token);
	}

}
