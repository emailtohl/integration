package com.github.emailtohl.integration.core.config;

import static com.github.emailtohl.integration.core.config.Constant.ADMIN_NAME;
import static com.github.emailtohl.integration.core.config.Constant.ANONYMOUS_EMAIL;
import static com.github.emailtohl.integration.core.config.Constant.ANONYMOUS_NAME;
import static com.github.emailtohl.integration.core.role.Authority.AUDIT_ROLE;
import static com.github.emailtohl.integration.core.role.Authority.AUDIT_USER;
import static com.github.emailtohl.integration.core.role.Authority.CONTENT;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_DELETE;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_ENABLED;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_LEVEL;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_ROLE;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_DELETE;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_ENABLED;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_ROLE;
import static com.github.emailtohl.integration.core.role.Authority.ORG;
import static com.github.emailtohl.integration.core.role.Authority.QUERY_ALL_USER;
import static com.github.emailtohl.integration.core.role.Authority.RESOURCE;
import static com.github.emailtohl.integration.core.role.Authority.ROLE;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleType;
import com.github.emailtohl.integration.core.user.entities.Address;
import com.github.emailtohl.integration.core.user.entities.Classify;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Customer.Level;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.Gender;

/**
 * 预置数据
 * @author HeLei
 */
public class PresetData {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public final Authority
			auth_org = new Authority(ORG, "管理组织信息的权限", null),
			auth_role = new Authority(ROLE, "管理角色的权限", null),
			auth_query_all_user = new Authority(QUERY_ALL_USER, "查询所有用户的权限", null),
			auth_employee = new Authority(EMPLOYEE, "管理平台账号的权限", null),
			auth_employee_role = new Authority(EMPLOYEE_ROLE, "为平台账号授予角色的权限", auth_employee),
			auth_employee_lock = new Authority(EMPLOYEE_ENABLED, "为平台账号解锁的权限", auth_employee),
			auth_employee_reset_password = new Authority(EMPLOYEE_RESET_PASSWORD, "为平台账号重置密码的权限", auth_employee),
			auth_employee_delete = new Authority(EMPLOYEE_DELETE, "删除平台账号的权限", auth_employee),
			auth_customer = new Authority(CUSTOMER, "管理客户的权限", null),
			auth_customer_role = new Authority(CUSTOMER_ROLE, "为客户授予角色的权限", auth_customer),
			auth_customer_level = new Authority(CUSTOMER_LEVEL, "为客户提升等级的权限", auth_customer),
			auth_customer_lock = new Authority(CUSTOMER_ENABLED, "为客户解锁的权限", auth_customer),
			auth_customer_reset_password = new Authority(CUSTOMER_RESET_PASSWORD, "为客户重置密码的权限", auth_customer),
			auth_customer_delete = new Authority(CUSTOMER_DELETE, "删除客户的权限", auth_customer),
			auth_audit_user = new Authority(AUDIT_USER, "审计修改用户信息", null),
			auth_audit_role = new Authority(AUDIT_ROLE, "审计修改角色信息", null),
			auth_resource = new Authority(RESOURCE, "资源管理，文件上传，目录创建、改名以及删除", null),
			auth_content = new Authority(CONTENT, "内容管理", null);
	
	public final Role
			role_admin = new Role(Role.ADMIN, RoleType.EMPLOYEE, "超级管理员"),
			role_manager = new Role("manager", RoleType.EMPLOYEE, "经理"),
			role_staff = new Role("staff", RoleType.EMPLOYEE, "雇员"),
			role_guest = new Role("guest", RoleType.CUSTOMER, "普通用户");
	
	public final Employee user_admin = new Employee();
	public final Employee user_bot = new Employee();
	public final Customer user_emailtohl = new Customer(), user_anonymous = new Customer();
	
	public final Company company = new Company("XXX注册公司", "公司上面还有集团公司", null);
	public final Department product = new Department("生产部", "研发生产部门", null), qa = new Department("质量部", "质量与测试部门", null);

	{
		auth_role.getRoles().addAll(Arrays.asList(role_admin));
		auth_org.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_query_all_user.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_employee.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		auth_employee_role.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_employee_lock.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_employee_reset_password.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_employee_delete.getRoles().addAll(Arrays.asList(role_admin));
		auth_customer.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		auth_customer_role.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_customer_level.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_customer_lock.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff));
		auth_customer_reset_password.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_customer_delete.getRoles().addAll(Arrays.asList(role_admin));
		
		auth_audit_user.getRoles().addAll(Arrays.asList(role_admin, role_manager));
		auth_audit_role.getRoles().addAll(Arrays.asList(role_admin));
		auth_resource.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff, role_guest));
		auth_content.getRoles().addAll(Arrays.asList(role_admin, role_manager, role_staff, role_guest));
		
		role_admin.getAuthorities().addAll(Arrays.asList(auth_role, auth_org, auth_query_all_user, auth_employee, auth_employee_role, auth_employee_lock, auth_employee_reset_password, auth_employee_delete, auth_customer, auth_customer_role, auth_customer_level, auth_customer_lock, auth_customer_reset_password, auth_customer_delete, auth_audit_user, auth_audit_role, auth_resource, auth_content));
		role_manager.getAuthorities().addAll(Arrays.asList(auth_org, auth_query_all_user, auth_employee, auth_employee_role, auth_customer_level, auth_employee_lock, auth_employee_reset_password, auth_customer, auth_customer_role, auth_customer_lock, auth_customer_reset_password, auth_audit_user, auth_resource, auth_content));
		role_staff.getAuthorities().addAll(Arrays.asList(auth_employee, auth_customer, auth_customer_lock, auth_resource, auth_content));
		role_guest.getAuthorities().addAll(Arrays.asList(auth_resource, auth_content));
		
		role_admin.getUsers().add(user_admin);
		role_guest.getUsers().addAll(Arrays.asList(user_emailtohl));
		
		ClassLoader cl = PresetData.class.getClassLoader();
		byte[] icon;

		// 下面是创建一对多对一数据模型
		product.setCompany(company);
		qa.setCompany(company);
		company.getDepartments().addAll(Arrays.asList(product, qa));

		user_bot.setEmpNum(Employee.NO_BOT);
		user_bot.setName("bot");
		user_bot.setNickname("bot");
		user_bot.setAccountNonLocked(true);
		user_bot.setDescription("自动审批人");
		user_bot.setGender(Gender.UNSPECIFIED);
		user_bot.getRoles().addAll(Arrays.asList(role_manager));
		try (InputStream is = cl.getResourceAsStream("img/icon-head-bot.jpg")) {
			icon = new byte[is.available()];
			is.read(icon);
			user_bot.setImage(new Image("icon-head-bot.jpg", "download/img/icon-head-bot.jpg", icon));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		user_admin.setEmpNum(Employee.NO1);
		user_admin.setName(ADMIN_NAME);
		user_admin.setNickname(ADMIN_NAME);
		user_admin.setDescription("系统管理员");
		user_admin.setGender(Gender.UNSPECIFIED);
		user_admin.getRoles().add(role_admin);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-admin.png")) {
			icon = new byte[is.available()];
			is.read(icon);
			user_admin.setImage(new Image("icon-head-admin.png", "download/img/icon-head-admin.png", icon));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		user_anonymous.setName(ANONYMOUS_NAME);
		user_anonymous.setNickname(ANONYMOUS_NAME);
		user_anonymous.setEmail(ANONYMOUS_EMAIL);
		user_anonymous.setDescription("系统中的匿名用户; anonymous in system");
		user_anonymous.setGender(Gender.UNSPECIFIED);
		user_anonymous.getRoles().addAll(Arrays.asList(role_guest));
		try (InputStream is = cl.getResourceAsStream("img/icon-head-anonymous.jpg")) {
			icon = new byte[is.available()];
			is.read(icon);
			user_anonymous.setImage(new Image("icon-head-anonymous.jpg", "download/icon-head-anonymous.jpg", icon));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		user_emailtohl.setName("hl");
		user_emailtohl.setNickname("hl");
		user_emailtohl.setEmail("emailtohl@163.com");
		user_emailtohl.setCellPhone("18700667788");
		user_emailtohl.setTelephone("69922113");
		user_emailtohl.setAddress(new Address("重庆", "40000", "回龙路66号"));
		user_emailtohl.setAccountNonLocked(true);
		user_emailtohl.setDescription("developer");
		user_emailtohl.setGender(Gender.MALE);
		user_emailtohl.setLevel(Level.VIP);
		user_emailtohl.setClassify(Classify.COOPERATE);
		user_emailtohl.getRoles().addAll(Arrays.asList(role_admin, role_guest));
		try (InputStream is = cl.getResourceAsStream("img/icon-head-emailtohl.png")) {
			user_emailtohl.setBirthday(sdf.parse("1982-02-12"));
			icon = new byte[is.available()];
			is.read(icon);
			user_emailtohl.setImage(new Image("icon-head-emailtohl.png", "download/img/icon-head-emailtohl.png", icon));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

	}
	
}
