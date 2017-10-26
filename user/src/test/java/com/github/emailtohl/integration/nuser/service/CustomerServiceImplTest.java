package com.github.emailtohl.integration.nuser.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.UserTestData;
import com.github.emailtohl.integration.nuser.entities.Address;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Customer.Level;
import com.github.emailtohl.integration.nuser.entities.Image;
import com.github.emailtohl.integration.nuser.entities.User.Gender;
import com.github.emailtohl.integration.nuser.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.nuser.userTestConfig.ServiceConfiguration;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
@Rollback(false)
public class CustomerServiceImplTest {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = CustomerServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	CustomerService customerService;
	Long id;

	@Before
	public void setUp() throws Exception {
		Customer e = new Customer();
		e.setName("haha");
		e.setNickname("haha");
		e.setPassword("112233");
		e.setEmail("haha@test.com");
		e.setTelephone("112342513514");
		e.setDescription("测试人员");
		e.setGender(Gender.MALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-foo.jpg")) {
			e.setBirthday(sdf.parse("1990-12-13"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			e.setImage(new Image("icon-head-foo.jpg", "download/img/icon-head-foo.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		e.setCellPhone("18712309876");
		e.setIdentification("510111111111111111");
		e.setAddress(new Address("重庆", "400000", "回龙路"));
		e = customerService.create(e);
		id = e.getId();
	}

	@After
	public void tearDown() throws Exception {
		customerService.delete(id);
	}


	@Test
	public void testExist() {
		UserTestData td = new UserTestData();
		assertTrue(customerService.exist(null, td.baz.getEmail()));
		assertTrue(customerService.exist(null, td.baz.getCellPhone()));
		assertFalse(customerService.exist(null, td.foo.getEmail()));
	}

	@Test
	public void testGet() {
		Customer c = customerService.get(id);
		assertNotNull(c);
	}

	@Test
	public void testQueryCustomerPageable() {
		UserTestData td = new UserTestData();
		Page<Customer> p = customerService.query(null, pageable);
		assertFalse(p.getContent().isEmpty());
		Customer params = new Customer();
		params.setCellPhone(td.baz.getCellPhone());
		params.setEmail(td.baz.getEmail());
		p = customerService.query(params, pageable);
		assertFalse(p.getContent().isEmpty());
		
		params = new Customer();
		params.setEmail(td.bar.getEmail());
		p = customerService.query(params, pageable);
		assertTrue(p.getContent().isEmpty());
	}

	@Test
	public void testQueryCustomer() {
		UserTestData td = new UserTestData();
		Customer params = new Customer();
		List<Customer> p = customerService.query(params);
		assertFalse(p.isEmpty());
		params.setCellPhone(td.baz.getCellPhone());
		params.setEmail(td.baz.getEmail());
		p = customerService.query(params);
		assertFalse(p.isEmpty());
		
		params = new Customer();
		params.setEmail(td.bar.getEmail());
		p = customerService.query(params);
		assertTrue(p.isEmpty());
	}

	@Test
	public void testUpdate() {
		Customer newEntity = new Customer();
		newEntity.setDescription("update");
		newEntity.setCellPhone("new Phone");
		newEntity.setGender(Gender.FEMALE);
		Customer c = customerService.update(id, newEntity);
		c = customerService.get(id);
		assertEquals("update", c.getDescription());
		assertFalse("new Phone".equals(c.getCellPhone()));
		
	}

	@Test
	public void testFindByCellPhoneOrEmail() {
		UserTestData td = new UserTestData();
		Customer c = customerService.findByCellPhoneOrEmail(td.baz.getCellPhone());
		assertNotNull(c);
		c = customerService.findByCellPhoneOrEmail(td.baz.getEmail());
		assertNotNull(c);
	}

	@Test
	public void testGrandRoles() {
		UserTestData td = new UserTestData();
		Customer c = customerService.grandRoles(id, td.role_guest.getName());
		assertFalse(c.getRoles().isEmpty());
	}

	@Test
	public void testGrandLevel() {
		Customer c = customerService.grandLevel(id, Level.VIP);
		c = customerService.get(id);
		assertEquals(Level.VIP, c.getLevel());
	}

	@Test
	public void testUpdatePassword() {
		ExecResult r = customerService.updatePassword(0L, "new password");
		assertFalse(r.ok);
		r = customerService.updatePassword(id, "new password");
		assertTrue(r.ok);
	}

	@Test
	public void testResetPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testChangeCellPhone() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testChangeEmail() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testLock() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateCards() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddCard() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveCard() {
		fail("Not yet implemented");
	}

}
