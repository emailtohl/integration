package com.github.emailtohl.integration.core.user.customer;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.entities.Address;
import com.github.emailtohl.integration.core.user.entities.Card;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Customer.Level;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Gender;
import com.google.gson.Gson;
/**
 * 业务类测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
@Rollback(false)
public class CustomerServiceImplTest {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = CustomerServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	CustomerService customerService;
	@Value("${" + Constant.PROP_CUSTOMER_DEFAULT_PASSWORD + "}")
	String customerDefaultPassword;
	@Inject
	Gson gson;
	Long id;
	String password = "112233";

	@Before
	public void setUp() throws Exception {
		Customer e = new Customer();
		e.setName("haha");
		e.setNickname("haha");
		e.setPassword(password);
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
		e.setIdentification("510104199901019991");
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
		CoreTestData td = new CoreTestData();
		assertTrue(customerService.exist(td.baz.getEmail()));
		assertTrue(customerService.exist(td.baz.getCellPhone()));
		assertFalse(customerService.exist(td.foo.getEmail()));
	}

	@Test
	public void testGet() {
		Customer c = customerService.get(id);
		assertNotNull(c);
		System.out.println(gson.toJson(c));
	}

	@Test
	public void testQueryCustomerPageable() {
		CoreTestData td = new CoreTestData();
		Paging<Customer> p = customerService.query(null, pageable);
		assertFalse(p.getContent().isEmpty());
		
		System.out.println(gson.toJson(p));
		
		Customer params = new Customer();
		params.setCellPhone(td.baz.getCellPhone());
		params.setEmail(td.baz.getEmail());
		p = customerService.query(params, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		params = new Customer();
		params.setEmail(td.baz.getEmail());
		p = customerService.query(params, pageable);
		assertFalse(p.getContent().isEmpty());
	}

	@Test
	public void testQueryCustomer() {
		CoreTestData td = new CoreTestData();
		Customer params = new Customer();
		List<Customer> p = customerService.query(params);
		assertFalse(p.isEmpty());
		params.setCellPhone(td.baz.getCellPhone());
		params.setEmail(td.baz.getEmail());
		p = customerService.query(params);
		assertFalse(p.isEmpty());
		
		params = new Customer();
		params.setEmail(td.baz.getEmail());
		p = customerService.query(params);
		assertFalse(p.isEmpty());
		
		System.out.println(gson.toJson(p));
	}

	@Test
	public void testUpdate() {
		Customer e = new Customer();
		e.setName("bzzz");
		e.setNickname("bbzzz");
		e.setPassword("212233");
		e.setEmail("_haha@test.com");
		e.setTelephone("212342513514");
		e.setDescription("_测试人员");
		e.setGender(Gender.FEMALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-baz.jpg")) {
			e.setBirthday(sdf.parse("1990-12-22"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			e.setImage(new Image("icon-head-baz.jpg", "download/img/icon-head-baz.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		e.setCellPhone("13345678906");
		e.setIdentification("510104199901017718");
		e.setAddress(new Address("重庆", "400000", "回龙路"));
		Customer c = customerService.update(id, e);
		c = customerService.get(id);
		assertEquals(e.getDescription(), c.getDescription());
		assertFalse(e.getCellPhone().equals(c.getCellPhone()));
		
		System.out.println(gson.toJson(c));
	}

	@Test
	public void testFindByCellPhoneOrEmail() {
		CoreTestData td = new CoreTestData();
		Customer c = customerService.findByCellPhoneOrEmail(td.baz.getCellPhone());
		assertNotNull(c);
		c = customerService.findByCellPhoneOrEmail(td.baz.getEmail());
		assertNotNull(c);
		
		System.out.println(gson.toJson(c));
	}

	@Test
	public void testGrandRoles() {
		CoreTestData td = new CoreTestData();
		Customer c = customerService.grandRoles(id, td.role_guest.getName());
		c = customerService.get(id);
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
		Customer c = customerService.get(id);
		String token = customerService.getToken(c.getCellPhone());
		Thread.yield();
		ExecResult r = customerService.updatePassword(c.getCellPhone(), "new password", token);
		assertTrue(r.ok);
		r = customerService.login(c.getEmail(), "new password");
		assertTrue(r.ok);
		r = customerService.updatePassword(c.getEmail(), "new password again", token);
		assertTrue(r.ok);
		r = customerService.login(c.getCellPhone(), "new password again");
		assertTrue(r.ok);
		
		r = customerService.updatePassword(c.getCellPhone(), "again again", null);
		assertFalse(r.ok);
		r = customerService.updatePassword(null, "again again", token);
		assertFalse(r.ok);
		r = customerService.updatePassword("test@aaa.com", "again again", token);
		assertFalse(r.ok);
		r = customerService.updatePassword(c.getCellPhone(), "again again", UUID.randomUUID().toString());
		assertFalse(r.ok);
	}

	@Test
	public void testResetPassword() {
		ExecResult r = customerService.resetPassword(0L);
		assertFalse(r.ok);
		r = customerService.resetPassword(id);
		assertTrue(r.ok);
		Customer c = customerService.get(id);
		r = customerService.login(c.getEmail(), password);
		assertFalse(r.ok);
		r = customerService.login(c.getEmail(), customerDefaultPassword);
		assertTrue(r.ok);
	}

	@Test
	public void testChangeCellPhone() {
		String newPhone = "19089023456";
		customerService.changeCellPhone(id, newPhone);
		customerService.findByCellPhoneOrEmail(newPhone);
		ExecResult r = customerService.login(newPhone, password);
		assertTrue(r.ok);
	}
	
	@Test
	public void testChangeEmail() {
		String newEmail = "newEmail@test.com";
		customerService.changeEmail(id, newEmail);
		Customer c = customerService.findByCellPhoneOrEmail(newEmail);
		assertNotNull(c);
	}
	
	@Test
	public void testEnabled() {
		customerService.enabled(id, true);
		Customer c = customerService.get(id);
		assertTrue(c.getEnabled());
	}

	@Test
	public void testUpdateCards() {
		Set<Card> cards = new HashSet<Card>(Arrays.asList(new Card(Card.Type.BankAccount, "123"), new Card(Card.Type.BankAccount, "456")));
		customerService.updateCards(id, cards);
		Customer c = customerService.get(id);
		assertEquals(2, c.getCards().size());
	}

	@Test
	public void testAddAndRemoveCard() {
		Card card = new Card(Card.Type.BankAccount, "123");
		customerService.addCard(id, card);
		Customer c = customerService.get(id);
		assertEquals(1, c.getCards().size());
		customerService.removeCard(id, card);
		c = customerService.get(id);
		assertTrue(c.getCards().isEmpty());
	}
	
	/**
	 * Maven测试时若Search失败，很可能是因为找不到Lucene索引。
	 * 典型例子就是每个test的Profiles不一致，即创建索引在内存中（Profiles.DB_RAM_H2）而Search使用索引却在文件系统中找。
	 */
	@Test
	public void testSearch() {
		Paging<Customer> p = customerService.search(null, pageable);
		System.out.println(gson.toJson(p));
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(c -> System.out.println(c));
		
		p = customerService.search("haha", pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(c -> System.out.println(c));
		
		p = customerService.search("haha@test.com", pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(c -> System.out.println(c));
		
		p = customerService.search("112342513514", pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(c -> System.out.println(c));
		
		CoreTestData td = new CoreTestData();
		Customer cust = customerService.grandRoles(id, td.role_guest.getName());
		System.out.println(cust.getRoles());
		p = customerService.search(td.role_guest.getName(), pageable);
		assertFalse(p.getContent().isEmpty());
		p.getContent().forEach(c -> System.out.println(c));
		
	}
	
	@Test
	public void testLogin() {
		ExecResult r = customerService.login("lalala", "123");
		assertFalse(r.ok);
		CoreTestData td = new CoreTestData();
		r = customerService.login(td.baz.getCellPhone(), "123");
		assertFalse(r.ok);
		r = customerService.login(td.baz.getCellPhone(), "123456");
		assertTrue(r.ok);
	}
	
	@Test
	public void testGetRef() {
		CustomerRef ref = customerService.getRef(id);
		assertNotNull(ref);
		System.out.println(gson.toJson(ref));
	}
	
	@Test
	public void testFindRefByCellPhoneOrEmail() {
		CoreTestData td = new CoreTestData();
		CustomerRef ref = customerService.findRefByCellPhoneOrEmail(td.baz.getCellPhone());
		assertNotNull(ref);
		ref = customerService.findRefByCellPhoneOrEmail(td.baz.getEmail());
		assertNotNull(ref);
		System.out.println(gson.toJson(ref));
	}
	
	@Test
	public void testQueryRefPageable() {
		CoreTestData td = new CoreTestData();
		Paging<CustomerRef> p = customerService.queryRef(null, pageable);
		assertFalse(p.getContent().isEmpty());
		
		System.out.println(gson.toJson(p));
		
		CustomerRef params = new CustomerRef();
		params.setCellPhone(td.baz.getCellPhone());
		params.setEmail(td.baz.getEmail());
		p = customerService.queryRef(params, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		params = new CustomerRef();
		params.setEmail(td.baz.getEmail());
		p = customerService.queryRef(params, pageable);
		assertFalse(p.getContent().isEmpty());
	}
	
	@Test
	public void testQueryRef() {
		CoreTestData td = new CoreTestData();
		List<CustomerRef> ls = customerService.queryRef(null);
		assertFalse(ls.isEmpty());
		
		System.out.println(gson.toJson(ls));
		
		CustomerRef params = new CustomerRef();
		params.setCellPhone(td.baz.getCellPhone());
		params.setEmail(td.baz.getEmail());
		ls = customerService.queryRef(params);
		assertFalse(ls.isEmpty());
		System.out.println(gson.toJson(ls));
		
		params = new CustomerRef();
		params.setEmail(td.baz.getEmail());
		ls = customerService.queryRef(params);
		assertFalse(ls.isEmpty());
	}
	
	@Test(expected = NotAcceptableException.class)
	public void testDelete() {
		Customer c = customerService.findByCellPhoneOrEmail(Constant.ANONYMOUS_EMAIL);
		customerService.delete(c.getId());
	}
}
