package com.github.emailtohl.integration.nuser.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.envers.RevisionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.entity.Image;
import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.nuser.entities.Address;
import com.github.emailtohl.integration.nuser.entities.Customer;
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
public class CustomerAuditedServiceImplTest {
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ClassLoader cl = CustomerServiceImplTest.class.getClassLoader();
	Pageable pageable = new PageRequest(0, 20);
	@Inject
	CustomerService customerService;
	@Inject
	CustomerAuditedService auditedService;
	
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
		assertEquals(tar.getDescription(), u.getDescription());
		assertEquals(tar.getAddress(), u.getAddress());
	}

	@After
	public void tearDown() throws Exception {
		customerService.delete(id);
	}

	@Test
	public void testGetCustomerRevision() {
		List<Tuple<Customer>> ls = auditedService.getCustomerRevision(id);
		assertTrue(ls.size() == 2);// 一个新增、一个修改
		assertEquals(ls.get(0).getEntity().getDescription(), "某客户");
		assertEquals(ls.get(0).getRevisionType(), RevisionType.ADD);
		assertEquals(ls.get(1).getEntity().getDescription(), "update");
		assertEquals(ls.get(1).getEntity().getAddress(), new Address("重庆", "40000", "xx街道"));
		assertEquals(ls.get(1).getRevisionType(), RevisionType.MOD);
	}

	@Test
	public void testGetCustomerAtRevision() {
		List<Tuple<Customer>> ls = auditedService.getCustomerRevision(id);
		Integer revision = ls.get(0).getDefaultRevisionEntity().getId();
		Customer e = auditedService.getCustomerAtRevision(id, revision);
		assertEquals("某客户", e.getDescription());
		assertEquals(new Address("成都", "12345", "xx街道"), e.getAddress());
	}

}