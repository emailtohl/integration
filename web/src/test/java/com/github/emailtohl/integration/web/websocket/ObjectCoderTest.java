package com.github.emailtohl.integration.web.websocket;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.web.WebTestData;

public class ObjectCoderTest {
	
	private class User extends com.github.emailtohl.integration.user.entities.User {
		private static final long serialVersionUID = 4534734369771438196L;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testObjectCoder() {
		ObjectCoder<User> oc = new ObjectCoder<User>(){};
		assertEquals(User.class, oc.clazz);
	}
	
	@Test
	public void test() throws EncodeException, IOException, DecodeException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectCoder<Customer> oc = new ObjectCoder<Customer>(){};
		WebTestData td = new WebTestData();
		oc.encode(td.emailtohl, out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Customer c = oc.decode(in);
		assertEquals(td.emailtohl.getUsername(), c.getUsername());
		assertEquals(td.emailtohl.getEmail(), c.getEmail());
		assertEquals(td.emailtohl.getRoles().iterator().next().getName(), c.getRoles().iterator().next().getName());
	}

}
