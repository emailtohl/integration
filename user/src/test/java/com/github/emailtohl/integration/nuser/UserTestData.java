package com.github.emailtohl.integration.nuser;

import static com.github.emailtohl.integration.nuser.entities.Authority.APPLICATION_FORM_DELETE;
import static com.github.emailtohl.integration.nuser.entities.Authority.APPLICATION_FORM_READ_HISTORY;
import static com.github.emailtohl.integration.nuser.entities.Authority.APPLICATION_FORM_TRANSIT;
import static com.github.emailtohl.integration.nuser.entities.Authority.AUDIT_ROLE;
import static com.github.emailtohl.integration.nuser.entities.Authority.AUDIT_USER;
import static com.github.emailtohl.integration.nuser.entities.Authority.CONTENT_MANAGER;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_DELETE;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_LEVEL;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_LOCK;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_RESET_PASSWORD;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_ROLE;
import static com.github.emailtohl.integration.nuser.entities.Authority.EMPLOYEE;
import static com.github.emailtohl.integration.nuser.entities.Authority.EMPLOYEE_DELETE;
import static com.github.emailtohl.integration.nuser.entities.Authority.EMPLOYEE_LOCK;
import static com.github.emailtohl.integration.nuser.entities.Authority.EMPLOYEE_RESET_PASSWORD;
import static com.github.emailtohl.integration.nuser.entities.Authority.EMPLOYEE_ROLE;
import static com.github.emailtohl.integration.nuser.entities.Authority.FLOW;
import static com.github.emailtohl.integration.nuser.entities.Authority.FORUM_DELETE;
import static com.github.emailtohl.integration.nuser.entities.Authority.QUERY_ALL_USER;
import static com.github.emailtohl.integration.nuser.entities.Authority.RESOURCE_MANAGER;
import static com.github.emailtohl.integration.nuser.entities.Authority.ROLE;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.common.jpa.entity.Image;
import com.github.emailtohl.integration.nuser.entities.Address;
import com.github.emailtohl.integration.nuser.entities.Authority;
import com.github.emailtohl.integration.nuser.entities.Company;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Customer.Level;
import com.github.emailtohl.integration.nuser.entities.Department;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.Role;
import com.github.emailtohl.integration.nuser.entities.User.Gender;
/**
 * 用于测试的数据
 * @author HeLei
 * @date 2017.06.12
 */
public class UserTestData {
	public final static String TEST_PASSWORD = "123456";
	static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static final SecureRandom r = new SecureRandom();
	static final int HASHING_ROUNDS = 10;
	
	public final Authority
			role = new Authority(ROLE, "管理角色的权限", null),
			query_all_user = new Authority(QUERY_ALL_USER, "查询所有用户的权限", null),
			employee = new Authority(EMPLOYEE, "管理平台账号的权限", null),
			employee_role = new Authority(EMPLOYEE_ROLE, "为平台账号授予角色的权限", employee),
			employee_lock = new Authority(EMPLOYEE_LOCK, "为平台账号解锁的权限", employee),
			employee_reset_password = new Authority(EMPLOYEE_RESET_PASSWORD, "为平台账号重置密码的权限", employee),
			employee_delete = new Authority(EMPLOYEE_DELETE, "删除平台账号的权限", employee),
			customer = new Authority(CUSTOMER, "管理客户的权限", null),
			customer_role = new Authority(CUSTOMER_ROLE, "为客户授予角色的权限", customer),
			customer_level = new Authority(CUSTOMER_LEVEL, "为客户提升等级的权限", customer),
			customer_lock = new Authority(CUSTOMER_LOCK, "为客户解锁的权限", customer),
			customer_reset_password = new Authority(CUSTOMER_RESET_PASSWORD, "为客户重置密码的权限", customer),
			customer_delete = new Authority(CUSTOMER_DELETE, "删除客户的权限", customer),
			flow = new Authority(FLOW, "处理申请单的权限", null),
			application_form_transit = new Authority(APPLICATION_FORM_TRANSIT, "处理申请单的权限", flow),
			application_form_read_history = new Authority(APPLICATION_FORM_READ_HISTORY, "查看申请单历史记录的权限", flow),
			application_form_delete = new Authority(APPLICATION_FORM_DELETE, "删除申请单", flow),
			forum_delete = new Authority(FORUM_DELETE, "删除论坛帖子", null),
			audit_user = new Authority(AUDIT_USER, "审计修改用户信息", null),
			audit_role = new Authority(AUDIT_ROLE, "审计修改角色信息", null),
			resource_manager = new Authority(RESOURCE_MANAGER, "资源管理，文件上传，目录创建、改名以及删除", null),
			content_manager = new Authority(CONTENT_MANAGER, "内容管理", null);
	
	public final Role role_admin = new Role(Role.ADMIN, "超级管理员"), role_manager = new Role("manager", "经理"),
			role_staff = new Role("staff", "雇员"), role_guest = new Role("guest", "普通用户");
	
	public final Customer emailtohl = new Customer();
	public final Employee foo = new Employee();
	public final Employee bar = new Employee();
	public final Customer baz = new Customer();
	public final Customer qux = new Customer();
	
	public final Company company = new Company();
	public final Department product = new Department(), qa = new Department();

	{
		role.getRoles().addAll(Arrays.asList(role_admin));
		query_all_user.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		employee.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		employee_role.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		employee_lock.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		employee_reset_password.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		employee_delete.getRoles().addAll(Arrays.asList(role_admin));
		customer.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff, role_guest));
		customer_role.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		customer_level.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		customer_lock.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		customer_reset_password.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		customer_delete.getRoles().addAll(Arrays.asList(role_admin));
		
		flow.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		application_form_transit.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		application_form_read_history.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		application_form_delete.getRoles().addAll(Arrays.asList(role_admin));
		forum_delete.getRoles().addAll(Arrays.asList(role_admin));
		audit_user.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		audit_role.getRoles().addAll(Arrays.asList(role_admin));
		resource_manager.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		content_manager.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		
		role_admin.getAuthorities().addAll(Arrays.asList(role, query_all_user, employee, employee_role, employee_lock, employee_reset_password, employee_delete, customer, customer_role, customer_level, customer_lock, customer_reset_password, customer_delete, flow, application_form_transit, application_form_read_history, application_form_delete, forum_delete, audit_user, audit_role, resource_manager, content_manager));
		role_manager.getAuthorities().addAll(Arrays.asList(query_all_user, employee, employee_role, customer_level, employee_lock, employee_reset_password, customer, customer_role, customer_lock, customer_reset_password, flow, application_form_transit, application_form_read_history, audit_user, resource_manager, content_manager));
		role_staff.getAuthorities().addAll(Arrays.asList(employee, customer, customer_lock, flow, application_form_transit, application_form_read_history, resource_manager, content_manager));
		role_guest.getAuthorities().addAll(Arrays.asList(customer, flow));
		
		role_admin.getUsers().add(emailtohl);
		role_manager.getUsers().add(foo);
		role_staff.getUsers().add(bar);
		role_guest.getUsers().addAll(Arrays.asList(emailtohl, baz, qux));
		
		String salt = BCrypt.gensalt(HASHING_ROUNDS, r);
		ClassLoader cl = UserTestData.class.getClassLoader();
		byte[] icon;

		/*
		 * 下面是创建一对多对一数据模型
		 */
		company.setName("XXX注册公司");
		company.setDescription("公司上面还有集团公司");

		product.setName("生产部");
		product.setDescription("研发生产部门");
		product.setCompany(company);
		qa.setName("质量部");
		qa.setDescription("质量与测试部门");
		qa.setCompany(company);

		company.setDepartments(new HashSet<Department>(Arrays.asList(product, qa)));
		
		emailtohl.setName("hl");
		emailtohl.setNickname("hl");
		emailtohl.setEmail("emailtohl@163.com");
		emailtohl.setCellPhone("17712356789");
		emailtohl.setTelephone("69922113");
		emailtohl.setAddress(new Address("重庆", "40000", "回龙路66号"));
		emailtohl.setAccountNonLocked(true);
		emailtohl.setPassword(BCrypt.hashpw(TEST_PASSWORD, salt));
		emailtohl.setDescription("developer");
		emailtohl.setGender(Gender.MALE);
		emailtohl.setLevel(Level.VIP);
		emailtohl.getRoles().addAll(Arrays.asList(role_admin, role_guest));
		// cl.getResourceAsStream方法返回的输入流已经是BufferedInputStream对象，无需再装饰
		try (InputStream is = cl.getResourceAsStream("img/icon-head-emailtohl.png")) {
			emailtohl.setBirthday(sdf.parse("1982-02-12"));
			icon = new byte[is.available()];
			is.read(icon);
			emailtohl.setImage(new Image("icon-head-emailtohl.png", "download/img/icon-head-emailtohl.png", icon));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		foo.setName("foo");
		foo.setNickname("foo");
		foo.setEmail("foo@test.com");
		foo.setCellPhone("18900987678");
		foo.setTelephone("40221199");
		foo.setAccountNonLocked(true);
		foo.setPassword(BCrypt.hashpw(TEST_PASSWORD, salt));
		foo.setDescription("业务管理人员");
		foo.setGender(Gender.MALE);
		foo.getRoles().add(role_manager);
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
		foo.setDepartment(product);
		
		bar.setName("bar");
		bar.setNickname("bar");
		bar.setEmail("bar@test.com");
		bar.setCellPhone("18255678769");
		bar.setTelephone("67891234");
		bar.setAccountNonLocked(true);
		bar.setPassword(BCrypt.hashpw(TEST_PASSWORD, salt));
		bar.setDescription("普通职员");
		bar.setGender(Gender.FEMALE);
		bar.getRoles().add(role_staff);
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
		bar.setDepartment(qa);
		
		baz.setName("baz");
		baz.setCellPhone("19012345678");
		baz.setNickname("baz");
		baz.setEmail("baz@test.com");
		baz.setCellPhone("18127834567");
		baz.setTelephone("7722134");
		baz.setAddress(new Address("成都", "", "新南路XX号"));
		baz.setAccountNonLocked(true);
		baz.setPassword(BCrypt.hashpw(TEST_PASSWORD, salt));
		baz.setDescription("普通客户");
		baz.setGender(Gender.FEMALE);
		baz.getRoles().add(role_guest);
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
		qux.setPassword(BCrypt.hashpw(TEST_PASSWORD, salt));
		qux.setDescription("高级客户");
		qux.setGender(Gender.FEMALE);
		qux.getRoles().add(role_guest);
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
		product.setEmployees(new HashSet<Employee>(Arrays.asList(foo)));
		qa.setEmployees(new HashSet<Employee>(Arrays.asList(bar)));
		
	}
}
