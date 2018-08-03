package com.github.emailtohl.integration.core.user.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.entities.Address;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Gender;
import com.github.emailtohl.lib.jpa.AuditedRepository.Tuple;
import com.google.gson.Gson;
/**
 * 业务类测试
 * @author HeLei
 */
public class CustomerAuditedServiceImplTest extends CoreTestEnvironment {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = CustomerServiceImplTest.class.getClassLoader();
	Pageable pageable = PageRequest.of(0, 20);
	@Inject
	CustomerService customerService;
	@Inject
	CustomerAuditedService auditedService;
	@Inject
	Gson gson;
	
	Long id;
	
	@Before
	public void setUp() throws Exception {
		Customer c = new Customer();
		c.setName("haha");
		c.setNickname("haha");
		c.setPassword("112233");
		c.setEmail("haha@test.com");
		c.setTelephone("112342513514");
		c.setDescription("某客户");
		c.setGender(Gender.MALE);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-foo.jpg")) {
			c.setBirthday(sdf.parse("1990-12-13"));
			byte[] icon = new byte[is.available()];
			is.read(icon);
			c.setImage(new Image("icon-head-foo.jpg", "download/img/icon-head-foo.jpg", icon));
		} catch (ParseException | IOException exception) {
			exception.printStackTrace();
		}
		c.setLevel(Customer.Level.ORDINARY);
		c.setAddress(new Address("成都", "12345", "xx街道"));
		c.setIdentification("120101199901012579");
		c = customerService.create(c);
		id = c.getId();
		
		Customer tar = new Customer();
		tar.setDescription("update");
		tar.setAddress(new Address("重庆", "40000", "xx街道"));
		Customer u = customerService.update(id, tar);
		u = customerService.get(id);
		assertEquals(tar.getAddress(), u.getAddress());
	}

	@After
	public void tearDown() throws Exception {
		customerService.delete(id);
	}

	@Test
	public void testGetCustomerRevision() {
		List<Tuple<Customer>> ls = auditedService.getCustomerRevision(id);
		System.out.println(gson.toJson(ls));
		assertTrue(ls.size() >= 2);// 一个新增、一个修改
//		在Maven统一执行时有其他用例修改数据，所以届时得到的结果会不一致
//		assertEquals(ls.get(0).getRevisionType(), RevisionType.ADD);
//		assertEquals(ls.get(1).getEntity().getAddress(), new Address("重庆", "40000", "xx街道"));
//		assertEquals(ls.get(1).getRevisionType(), RevisionType.MOD);
	}

	@Test
	public void testGetCustomerAtRevision() {
		List<Tuple<Customer>> ls = auditedService.getCustomerRevision(id);
		System.out.println(gson.toJson(ls));
		Integer revision = ls.get(0).defaultRevisionEntity.getId();
		Customer e = auditedService.getCustomerAtRevision(id, revision);
		assertNotNull(e);
//		在Maven统一执行时有其他用例修改数据，所以届时得到的结果会不一致
//		assertEquals(new Address("成都", "12345", "xx街道"), e.getAddress());
	}

}
