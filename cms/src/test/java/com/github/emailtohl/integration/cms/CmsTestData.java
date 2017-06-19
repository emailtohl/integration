package com.github.emailtohl.integration.cms;

import static com.github.emailtohl.integration.user.entities.Authority.APPLICATION_FORM_DELETE;
import static com.github.emailtohl.integration.user.entities.Authority.APPLICATION_FORM_READ_HISTORY;
import static com.github.emailtohl.integration.user.entities.Authority.APPLICATION_FORM_TRANSIT;
import static com.github.emailtohl.integration.user.entities.Authority.AUDIT_ROLE;
import static com.github.emailtohl.integration.user.entities.Authority.AUDIT_USER;
import static com.github.emailtohl.integration.user.entities.Authority.CONTENT_MANAGER;
import static com.github.emailtohl.integration.user.entities.Authority.FORUM_DELETE;
import static com.github.emailtohl.integration.user.entities.Authority.RESOURCE_MANAGER;
import static com.github.emailtohl.integration.user.entities.Authority.USER_CREATE_ORDINARY;
import static com.github.emailtohl.integration.user.entities.Authority.USER_CREATE_SPECIAL;
import static com.github.emailtohl.integration.user.entities.Authority.USER_CUSTOMER;
import static com.github.emailtohl.integration.user.entities.Authority.USER_DELETE;
import static com.github.emailtohl.integration.user.entities.Authority.USER_DISABLE;
import static com.github.emailtohl.integration.user.entities.Authority.USER_ENABLE;
import static com.github.emailtohl.integration.user.entities.Authority.USER_GRANT_ROLES;
import static com.github.emailtohl.integration.user.entities.Authority.USER_READ_ALL;
import static com.github.emailtohl.integration.user.entities.Authority.USER_READ_SELF;
import static com.github.emailtohl.integration.user.entities.Authority.USER_ROLE_AUTHORITY_ALLOCATION;
import static com.github.emailtohl.integration.user.entities.Authority.USER_UPDATE_ALL;
import static com.github.emailtohl.integration.user.entities.Authority.USER_UPDATE_SELF;
import static com.github.emailtohl.integration.user.entities.Role.ADMIN;
import static com.github.emailtohl.integration.user.entities.Role.EMPLOYEE;
import static com.github.emailtohl.integration.user.entities.Role.MANAGER;
import static com.github.emailtohl.integration.user.entities.Role.USER;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.cms.entities.Article;
import com.github.emailtohl.integration.cms.entities.Comment;
import com.github.emailtohl.integration.cms.entities.Type;
import com.github.emailtohl.integration.user.entities.Authority;
import com.github.emailtohl.integration.user.entities.Company;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.entities.Department;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.Role;
import com.github.emailtohl.integration.user.entities.Subsidiary;
import com.github.emailtohl.integration.user.entities.User.Gender;
/**
 * 用于测试的数据
 * @author HeLei
 * @date 2017.06.12
 */
public class CmsTestData {
	static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static final SecureRandom r = new SecureRandom();
	static final int HASHING_ROUNDS = 10;
	
	public final Authority
			user_role_authority_allocation = new Authority(USER_ROLE_AUTHORITY_ALLOCATION, "对角色进行权限配置的权限"),
			user_create_ordinary = new Authority(USER_CREATE_ORDINARY, "创建普通账号，用于用户自行注册时"),
			user_create_special = new Authority(USER_CREATE_SPECIAL, "创建有一定权限的账号，用于管理员创建时"),
			user_enable = new Authority(USER_ENABLE, "激活账号"),
			user_disable = new Authority(USER_DISABLE, "禁用账号"),
			user_grant_roles = new Authority(USER_GRANT_ROLES, "授予角色"),
			user_read_all = new Authority(USER_READ_ALL, "读取所有用户的权限"),
			user_read_self = new Authority(USER_READ_SELF, "读取自己账号信息"),
			user_update_all = new Authority(USER_UPDATE_ALL, "修改所有用户的权限，用于管理员"),
			user_update_self = new Authority(USER_UPDATE_SELF, "修改自己账号的权限，用于普通用户"),
			user_delete = new Authority(USER_DELETE, "删除用户的权限"),
			user_customer = new Authority(USER_CUSTOMER, "客户管理的权限"),
			application_form_transit = new Authority(APPLICATION_FORM_TRANSIT, "处理申请单的权限"),
			application_form_read_history = new Authority(APPLICATION_FORM_READ_HISTORY, "查看申请单历史记录的权限"),
			application_form_delete = new Authority(APPLICATION_FORM_DELETE, "删除申请单"),
			forum_delete = new Authority(FORUM_DELETE, "删除论坛帖子"),
			audit_user = new Authority(AUDIT_USER, "审计修改用户信息"),
			audit_role = new Authority(AUDIT_ROLE, "审计修改角色信息"),
			resource_manager = new Authority(RESOURCE_MANAGER, "资源管理，文件上传，目录创建、改名以及删除"),
			content_manager = new Authority(CONTENT_MANAGER, "内容管理");
	
	public final Role admin = new Role(ADMIN, "管理员"), manager = new Role(MANAGER, "经理"),
			employee = new Role(EMPLOYEE, "雇员"), user = new Role(USER, "普通用户");
	
	public final Customer emailtohl = new Customer();
	public final Employee foo = new Employee();
	public final Employee bar = new Employee();
	public final Customer baz = new Customer();
	public final Customer qux = new Customer();
	
	public final Company company = new Company();
	public final Department product = new Department(), qa = new Department();

	public final Type subType = new Type();
	public final Type parent = new Type();
	public final Article article = new Article();
	public final Comment comment = new Comment();
	
	{
		user_role_authority_allocation.getRoles().add(admin);
		user_create_ordinary.getRoles().addAll(Arrays.asList(admin, manager, employee, user));
		user_create_special.getRoles().addAll(Arrays.asList(admin, manager));
		user_enable.getRoles().addAll(Arrays.asList(admin, manager, employee, user));
		user_disable.getRoles().addAll(Arrays.asList(admin, manager));
		user_grant_roles.getRoles().addAll(Arrays.asList(admin, manager));
		user_read_all.getRoles().addAll(Arrays.asList(admin, manager, employee));
		user_read_self.getRoles().addAll(Arrays.asList(admin, manager, employee, user));
		user_update_all.getRoles().addAll(Arrays.asList(admin));
		user_update_self.getRoles().addAll(Arrays.asList(admin, manager, employee, user));
		user_delete.getRoles().add(admin);
		user_customer.getRoles().addAll(Arrays.asList(admin, manager, employee));
		application_form_transit.getRoles().addAll(Arrays.asList(admin, manager, employee));
		application_form_read_history.getRoles().addAll(Arrays.asList(admin, manager, employee));
		application_form_delete.getRoles().addAll(Arrays.asList(admin));
		forum_delete.getRoles().addAll(Arrays.asList(admin));
		audit_user.getRoles().addAll(Arrays.asList(admin, manager));
		audit_role.getRoles().addAll(Arrays.asList(admin));
		resource_manager.getRoles().addAll(Arrays.asList(admin, manager, employee));
		content_manager.getRoles().addAll(Arrays.asList(admin, manager, employee));
		
		admin.getAuthorities().addAll(Arrays.asList(user_role_authority_allocation, user_create_ordinary, user_create_special, user_enable, user_disable, user_grant_roles, user_read_all, user_read_self, user_update_all, user_update_self, user_delete, user_customer, application_form_transit, application_form_read_history, application_form_delete, forum_delete, audit_user, audit_role, resource_manager, content_manager));
		manager.getAuthorities().addAll(Arrays.asList(user_create_ordinary, user_create_special, user_enable, user_disable, user_grant_roles, user_read_all, user_read_self, user_update_self, user_customer, application_form_transit, application_form_read_history, audit_user, resource_manager, content_manager));
		employee.getAuthorities().addAll(Arrays.asList(user_create_ordinary, user_enable, user_read_all, user_read_self, user_update_self, user_customer, application_form_transit, application_form_read_history, resource_manager, content_manager));
		user.getAuthorities().addAll(Arrays.asList(user_create_ordinary, user_enable, user_read_self, user_update_self));
		
		admin.getUsers().add(emailtohl);
		manager.getUsers().add(foo);
		employee.getUsers().add(bar);
		user.getUsers().addAll(Arrays.asList(emailtohl, baz, qux));
		
		String salt = BCrypt.gensalt(HASHING_ROUNDS, r);
		ClassLoader cl = CmsTestData.class.getClassLoader();
		byte[] icon;

		// 附属属性
		Subsidiary s;
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
		emailtohl.setUsername("emailtohl@163.com");
		emailtohl.setEmail("emailtohl@163.com");
		emailtohl.setTelephone("69922113");
		emailtohl.setAddress("回龙路66号");
		emailtohl.setEnabled(true);
		emailtohl.setPassword(BCrypt.hashpw("123456", salt));
		emailtohl.setDescription("developer");
		s = new Subsidiary();
		s.setCity("重庆");
		s.setCountry("中国");
		s.setProvince("重庆");
		s.setLanguage("zh");
		s.setMobile("187******82");
		emailtohl.setSubsidiary(s);
		emailtohl.setGender(Gender.MALE);
		emailtohl.getRoles().addAll(Arrays.asList(admin, user));
		// cl.getResourceAsStream方法返回的输入流已经是BufferedInputStream对象，无需再装饰
		try (InputStream is = cl.getResourceAsStream("img/icon-head-emailtohl.png")) {
			emailtohl.setBirthday(sdf.parse("1982-02-12"));
			icon = new byte[is.available()];
			is.read(icon);
			emailtohl.setIcon(icon);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		emailtohl.setIconSrc("download/img/icon-head-emailtohl.png");

		foo.setName("foo");
		foo.setUsername("foo@test.com");
		foo.setEmail("foo@test.com");
		foo.setTelephone("40221199");
		foo.setAddress("北大街XX号");
		foo.setEnabled(true);
		foo.setPassword(BCrypt.hashpw("123456", salt));
		foo.setDescription("业务管理人员");
		s = new Subsidiary();
		s.setCity("西安");
		s.setCountry("中国");
		s.setProvince("陕西");
		s.setLanguage("zh");
		s.setMobile("139******11");
		foo.setSubsidiary(s);
		foo.setGender(Gender.MALE);
		foo.getRoles().add(manager);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-foo.jpg")) {
			foo.setBirthday(sdf.parse("1990-12-13"));
			icon = new byte[is.available()];
			is.read(icon);
			foo.setIcon(icon);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		foo.setEmpNum(1);
		foo.setPost("系统分析师");
		foo.setSalary(10000.00);
		foo.setDepartment(product);
		foo.setIconSrc("download/img/icon-head-foo.jpg");
		
		bar.setName("bar");
		bar.setUsername("bar@test.com");
		bar.setEmail("bar@test.com");
		bar.setTelephone("67891234");
		bar.setAddress("XX路25号");
		bar.setEnabled(true);
		bar.setPassword(BCrypt.hashpw("123456", salt));
		bar.setDescription("普通职员");
		s = new Subsidiary();
		s.setCity("昆明");
		s.setCountry("中国");
		s.setProvince("云南");
		s.setLanguage("zh");
		s.setMobile("130******77");
		bar.setSubsidiary(s);
		bar.setGender(Gender.FEMALE);
		bar.getRoles().add(employee);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-bar.jpg")) {
			bar.setBirthday(sdf.parse("1991-10-24"));
			icon = new byte[is.available()];
			is.read(icon);
			bar.setIcon(icon);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		bar.setEmpNum(2);
		bar.setPost("QA人员");
		bar.setSalary(6000.00);
		bar.setDepartment(qa);
		bar.setIconSrc("download/img/icon-head-bar.jpg");
		
		baz.setName("baz");
		baz.setTitle("客户经理");
		baz.setAffiliation("客户咨询公司");
		baz.setUsername("baz@test.com");
		baz.setEmail("baz@test.com");
		baz.setTelephone("7722134");
		baz.setAddress("新南路XX号");
		baz.setEnabled(true);
		baz.setPassword(BCrypt.hashpw("123456", salt));
		baz.setDescription("普通客户");
		s = new Subsidiary();
		s.setCity("成都");
		s.setCountry("中国");
		s.setProvince("四川");
		s.setLanguage("zh");
		s.setMobile("136******87");
		baz.setSubsidiary(s);
		baz.setGender(Gender.FEMALE);
		baz.getRoles().add(user);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-baz.jpg")) {
			baz.setBirthday(sdf.parse("1995-11-20"));
			icon = new byte[is.available()];
			is.read(icon);
			baz.setIcon(icon);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		baz.setIconSrc("download/img/icon-head-baz.jpg");
		
		qux.setName("qux");
		qux.setTitle("销售经理");
		qux.setAffiliation("客户咨询公司");
		qux.setUsername("qux@test.com");
		qux.setEmail("qux@test.com");
		qux.setTelephone("98241562");
		qux.setAddress("竹山路XX号");
		qux.setEnabled(true);
		qux.setPassword(BCrypt.hashpw("123456", salt));
		qux.setDescription("高级客户");
		s = new Subsidiary();
		s.setCity("南京");
		s.setCountry("中国");
		s.setProvince("江苏");
		s.setLanguage("zh");
		s.setMobile("177******05");
		qux.setSubsidiary(s);
		qux.setGender(Gender.FEMALE);
		qux.getRoles().add(user);
		try (InputStream is = cl.getResourceAsStream("img/icon-head-qux.jpg")) {
			qux.setBirthday(sdf.parse("1992-07-17"));
			icon = new byte[is.available()];
			is.read(icon);
			qux.setIcon(icon);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		qux.setIconSrc("download/img/icon-head-qux.jpg");
		
		product.setEmployees(new HashSet<Employee>(Arrays.asList(foo)));
		qa.setEmployees(new HashSet<Employee>(Arrays.asList(bar)));
		
		
		parent.setName("未分类");
		subType.setName("子类");
		subType.setParent(parent);
		article.setTitle("世界，您好！");
		article.setBody("这是第一篇文章。编辑或删除它，然后开始写作吧！");
		article.setKeywords("世界 您好 文章");
		article.setSummary("第一篇文章");
		article.setType(subType);
		subType.getArticles().add(article);
		article.setAuthor(foo);
		
		comment.setArticle(article);
		comment.setCritics(bar.getName());
		comment.setContent("嗨，这是一条评论。评论者头像来自Gravatar。");
		comment.setIcon(bar.getIconSrc());
		article.getComments().add(comment);
	}
}
