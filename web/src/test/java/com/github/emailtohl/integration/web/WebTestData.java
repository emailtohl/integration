package com.github.emailtohl.integration.web;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.entities.Address;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Customer.Level;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.Gender;

public class WebTestData {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private final String DEFAULT_PASSWORD = "123456";
	public final CorePresetData pd;
	public final Employee foo = new Employee();
	public final Employee bar = new Employee();
	public final Customer baz = new Customer();
	public final Customer qux = new Customer();
	
	public WebTestData(CorePresetData pd) {
		this.pd = pd;
		set();
	}

	private void set() 
	{
		pd.role_manager.getUsers().add(foo);
		pd.role_staff.getUsers().add(bar);
		pd.role_guest.getUsers().addAll(Arrays.asList(baz, qux));
		
		ClassLoader cl = WebTestData.class.getClassLoader();
		byte[] icon;

		/*
		 * 下面是创建一对多对一数据模型
		 */
		pd.company.setName("XXX注册公司");
		pd.company.setDescription("公司上面还有集团公司");

		pd.product.setName("生产部");
		pd.product.setDescription("研发生产部门");
		pd.product.setCompany(pd.company);
		pd.qa.setName("质量部");
		pd.qa.setDescription("质量与测试部门");
		pd.qa.setCompany(pd.company);

		pd.company.setDepartments(new HashSet<Department>(Arrays.asList(pd.product, pd.qa)));
		
		foo.setName("foo");
		foo.setNickname("foo");
		foo.setEmail("foo@test.com");
		foo.setCellPhone("18900987678");
		foo.setTelephone("40221199");
		foo.setAccountNonLocked(true);
		foo.setPassword(hashpw(DEFAULT_PASSWORD));
		foo.setDescription("业务管理人员");
		foo.setGender(Gender.MALE);
		foo.getRoles().add(pd.role_manager);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-foo.jpg")) {
			foo.setBirthday(sdf.parse("1990-12-13"));
			icon = new byte[is.available()];
			is.read(icon);
			foo.setImage(new Image("icon-head-foo.jpg", "download/img/icon-head-foo.jpg", icon));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		foo.setEmpNum(Employee.NO1 + 1);
		foo.setPost("系统分析师");
		foo.setSalary(10000.00);
		foo.setDepartment(pd.product);
		
		bar.setName("bar");
		bar.setNickname("bar");
		bar.setEmail("bar@test.com");
		bar.setCellPhone("18255678769");
		bar.setTelephone("67891234");
		bar.setAccountNonLocked(true);
		bar.setPassword(hashpw(DEFAULT_PASSWORD));
		bar.setDescription("普通职员");
		bar.setGender(Gender.FEMALE);
		bar.getRoles().add(pd.role_staff);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-bar.jpg")) {
			bar.setBirthday(sdf.parse("1991-10-24"));
			icon = new byte[is.available()];
			is.read(icon);
			bar.setImage(new Image("icon-head-bar.jpg", "download/img/icon-head-bar.jpg", icon));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		bar.setEmpNum(Employee.NO1 + 2);
		bar.setPost("QA人员");
		bar.setSalary(6000.00);
		bar.setDepartment(pd.qa);
		
		baz.setName("baz");
		baz.setCellPhone("19012345678");
		baz.setNickname("baz");
		baz.setEmail("baz@test.com");
		baz.setCellPhone("18127834567");
		baz.setTelephone("7722134");
		baz.setAddress(new Address("成都", "", "新南路XX号"));
		baz.setAccountNonLocked(true);
		baz.setPassword(hashpw(DEFAULT_PASSWORD));
		baz.setDescription("普通客户");
		baz.setGender(Gender.FEMALE);
		baz.getRoles().add(pd.role_guest);
		baz.setLevel(Level.ORDINARY);
		baz.setIdentification("510104199901013338");
		try (InputStream is = cl.getResourceAsStream("img/icon-head-baz.jpg")) {
			baz.setBirthday(sdf.parse("1995-11-20"));
			icon = new byte[is.available()];
			is.read(icon);
			baz.setImage(new Image("icon-head-baz.jpg", "download/img/icon-head-baz.jpg", icon));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		qux.setName("qux");
		qux.setCellPhone("17809876543");
		qux.setNickname("qux");
		qux.setEmail("qux@test.com");
		qux.setTelephone("98241562");
		qux.setAddress(new Address("南京", "", "竹山路XX号"));
		qux.setAccountNonLocked(true);
		qux.setPassword(hashpw(DEFAULT_PASSWORD));
		qux.setDescription("高级客户");
		qux.setGender(Gender.FEMALE);
		qux.getRoles().add(pd.role_guest);
		qux.setLevel(Level.ORDINARY);
		qux.setIdentification("510104199901016176");
		try (InputStream is = cl.getResourceAsStream("img/icon-head-qux.jpg")) {
			qux.setBirthday(sdf.parse("1992-07-17"));
			icon = new byte[is.available()];
			is.read(icon);
			qux.setImage(new Image("icon-head-qux.jpg", "download/img/icon-head-qux.jpg", icon));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		pd.product.setEmployees(new HashSet<Employee>(Arrays.asList(foo)));
		pd.qa.setEmployees(new HashSet<Employee>(Arrays.asList(bar)));
		
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}
}
