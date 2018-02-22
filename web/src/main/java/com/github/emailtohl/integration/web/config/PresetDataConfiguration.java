package com.github.emailtohl.integration.web.config;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.org.DepartmentService;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

import freemarker.template.TemplateExceptionHandler;

/**
 * 预置数据的配置
 * 
 * @author HeLei
 */
@Configuration
@Import(ActivitiConfiguration.class)
@PropertySource({ "classpath:web.properties" })
class PresetDataConfiguration {

	@Bean(name = "templatesPath")
	public File templatesPath(Environment env, @Named("resources") File resourcesPath) throws IOException {
		String path = env.getProperty("templatesPath");
		File templatesPath;
		if (StringUtils.hasText(path)) {
			templatesPath = new File(path);
		} else {
			templatesPath = new File(resourcesPath, "templates");
		}
		if (!templatesPath.exists()) {
			templatesPath.mkdir();
			Resource r = new ClassPathResource("templates");
			FileUtils.copyDirectory(r.getFile(), templatesPath);
		}
		return templatesPath;
	}
	
	/**
	 * freemarker的配置
	 * @return
	 * @throws IOException
	 */
	@Bean
	public freemarker.template.Configuration freeMarkerConfiguration(@Named("templatesPath") File templatesPath) throws IOException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
		cfg.setDirectoryForTemplateLoading(templatesPath);
		cfg.setDefaultEncoding("UTF-8");
		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		cfg.setLogTemplateExceptions(false);
		return cfg;
	}
	
	@Bean
	public WebPresetData webPresetData(EntityManagerFactory jpaEntityManagerFactory, CorePresetData cpd,
			IdentityService identityService, EmployeeService employeeService, RoleService roleService,
			DepartmentService departmentService) {
		synchronized (getClass()) {
			EntityManager em = jpaEntityManagerFactory.createEntityManager();
			WebPresetData wpd = new WebPresetData();
			em.getTransaction().begin();

			// 先将core中的预置的账号信息补加到Activiti身份系统中
			appendIdentityData(cpd, identityService);
			
			// 再创建业务上需要的组织关系
			departments(em, wpd);
			role(em, wpd, identityService);
			user(em, identityService, wpd);
			
			// 其他内置数据
			type(em, wpd);

			em.getTransaction().commit();
			em.close();
			return wpd;
		}
	}
	
	/**
	 * 将预置的账号信息补加到Activiti身份系统中
	 * @param cpd
	 * @param identityService
	 */
	private void appendIdentityData(CorePresetData cpd, IdentityService identityService) {
		// 用户组（角色）
		Group g = identityService.createGroupQuery().groupId(cpd.role_admin.getName()).singleResult();
		if (g == null) {
			g = identityService.newGroup(cpd.role_admin.getName());
			g.setName(cpd.role_admin.getDescription());
			g.setType(cpd.role_admin.getRoleType().name());
			identityService.saveGroup(g);
		}
		
		g = identityService.createGroupQuery().groupId(cpd.role_manager.getName()).singleResult();
		if (g == null) {
			g = identityService.newGroup(cpd.role_manager.getName());
			g.setName(cpd.role_manager.getDescription());
			g.setType(cpd.role_manager.getRoleType().name());
			identityService.saveGroup(g);
		}
		
		g = identityService.createGroupQuery().groupId(cpd.role_staff.getName()).singleResult();
		if (g == null) {
			g = identityService.newGroup(cpd.role_staff.getName());
			g.setName(cpd.role_staff.getDescription());
			g.setType(cpd.role_staff.getRoleType().name());
			identityService.saveGroup(g);
		}
		
		g = identityService.createGroupQuery().groupId(cpd.role_guest.getName()).singleResult();
		if (g == null) {
			g = identityService.newGroup(cpd.role_guest.getName());
			g.setName(cpd.role_guest.getDescription());
			g.setType(cpd.role_guest.getRoleType().name());
			identityService.saveGroup(g);
		}
		
		// 用户
		User u = identityService.createUserQuery().userId(cpd.user_admin.getId().toString()).singleResult();
		if (u == null) {
			u = identityService.newUser(cpd.user_admin.getId().toString());
			u.setEmail(cpd.user_admin.getEmail());
			u.setFirstName(cpd.user_admin.getName());
			u.setLastName(cpd.user_admin.getNickname());
			u.setPassword(cpd.user_admin.getPassword());
			identityService.saveUser(u);
			cpd.user_admin.roleNames().forEach(groupId -> identityService.createMembership(cpd.user_admin.getId().toString(), groupId));
		}
		
		u = identityService.createUserQuery().userId(cpd.user_bot.getId().toString()).singleResult();
		if (u == null) {
			u = identityService.newUser(cpd.user_bot.getId().toString());
			u.setEmail(cpd.user_bot.getEmail());
			u.setFirstName(cpd.user_bot.getName());
			u.setLastName(cpd.user_bot.getNickname());
			u.setPassword(cpd.user_bot.getPassword());
			identityService.saveUser(u);
			cpd.user_bot.roleNames().forEach(groupId -> identityService.createMembership(cpd.user_bot.getId().toString(), groupId));
		}
		
		u = identityService.createUserQuery().userId(cpd.user_anonymous.getId().toString()).singleResult();
		if (u == null) {
			u = identityService.newUser(cpd.user_anonymous.getId().toString());
			u.setEmail(cpd.user_anonymous.getEmail());
			u.setFirstName(cpd.user_anonymous.getName());
			u.setLastName(cpd.user_anonymous.getNickname());
			u.setPassword(cpd.user_anonymous.getPassword());
			identityService.saveUser(u);
			cpd.user_anonymous.roleNames().forEach(groupId -> identityService.createMembership(cpd.user_anonymous.getId().toString(), groupId));
		}
		
		u = identityService.createUserQuery().userId(cpd.user_emailtohl.getId().toString()).singleResult();
		if (u == null) {
			u = identityService.newUser(cpd.user_emailtohl.getId().toString());
			u.setEmail(cpd.user_emailtohl.getEmail());
			u.setFirstName(cpd.user_emailtohl.getName());
			u.setLastName(cpd.user_emailtohl.getNickname());
			u.setPassword(cpd.user_emailtohl.getPassword());
			identityService.saveUser(u);
			cpd.user_emailtohl.roleNames().forEach(groupId -> identityService.createMembership(cpd.user_emailtohl.getId().toString(), groupId));
		}
		
	}
	
	private void type(EntityManager em, WebPresetData wpd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Type> q = cb.createQuery(Type.class);
		Root<Type> r = q.from(Type.class);
		q = q.select(r).where(cb.equal(r.get("name"), wpd.unclassified.getName()));
		Type unclassified = null;
		try {
			unclassified = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (unclassified == null) {
			em.persist(wpd.unclassified);
		} else {
			wpd.unclassified.setId(unclassified.getId());
		}
	}
	
	private void departments(EntityManager em, WebPresetData wpd) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = b.createQuery(Boolean.class);
		Root<Department> r = q.from(Department.class);
		Expression<Boolean> restriction = b.<String>in(r.<String>get("name"))
				.value(wpd.getMarket().getName())
				.value(wpd.getFinancial().getName())
				.value(wpd.getBusiness().getName())
				.value(wpd.getHumanResource().getName())
				.value(wpd.getBack().getName());
		q = q.select(b.greaterThan(b.count(r), 0L)).where(restriction);
		// 如果创建过，则不再创建
		if (em.createQuery(q).getSingleResult()) {
			wpd.setMarket(getDepartment(em, wpd.getMarket().getName()));
			wpd.setFinancial(getDepartment(em, wpd.getFinancial().getName()));
			wpd.setBusiness(getDepartment(em, wpd.getBusiness().getName()));
			wpd.setHumanResource(getDepartment(em, wpd.getHumanResource().getName()));
			wpd.setBack(getDepartment(em, wpd.getBack().getName()));
		} else {
			em.persist(wpd.getMarket());
			em.persist(wpd.getFinancial());
			em.persist(wpd.getBusiness());
			em.persist(wpd.getHumanResource());
			em.persist(wpd.getBack());
		}
	}
	
	private Department getDepartment(EntityManager em, String name) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Department> q = b.createQuery(Department.class);
		Root<Department> r = q.from(Department.class);
		q = q.select(r).where(b.equal(r.get("name"), name));
		Department department = null;
		try {
			department = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return department;
	}
	
	private void role(EntityManager em, WebPresetData wpd, IdentityService identityService) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = b.createQuery(Boolean.class);
		Root<Role> r = q.from(Role.class);
		Expression<Boolean> restriction = b.<String>in(r.<String>get("name"))
				.value(wpd.getGeneralManager().getName())
				.value(wpd.getDeptLeader().getName())
				.value(wpd.getHr().getName())
				.value(wpd.getTreasurer().getName())
				.value(wpd.getCashier().getName())
				.value(wpd.getSupportCrew().getName());
		q = q.select(b.greaterThan(b.count(r), 0L)).where(restriction);
		// 如果创建过，则不再创建
		if (em.createQuery(q).getSingleResult()) {
			wpd.setGeneralManager(getRole(em, wpd.getGeneralManager().getName()));
			wpd.setDeptLeader(getRole(em, wpd.getDeptLeader().getName()));
			wpd.setHr(getRole(em, wpd.getHr().getName()));
			wpd.setTreasurer(getRole(em, wpd.getTreasurer().getName()));
			wpd.setCashier(getRole(em, wpd.getCashier().getName()));
			wpd.setSupportCrew(getRole(em, wpd.getSupportCrew().getName()));
		} else {
			em.persist(wpd.getGeneralManager());
			em.persist(wpd.getDeptLeader());
			em.persist(wpd.getHr());
			em.persist(wpd.getTreasurer());
			em.persist(wpd.getCashier());
			em.persist(wpd.getSupportCrew());
			Group g;
			g = identityService.newGroup(wpd.getGeneralManager().getName());
			g.setName(wpd.getGeneralManager().getDescription());
			g.setType(wpd.getGeneralManager().getRoleType().name());
			identityService.saveGroup(g);
			
			g = identityService.newGroup(wpd.getDeptLeader().getName());
			g.setName(wpd.getDeptLeader().getDescription());
			g.setType(wpd.getDeptLeader().getRoleType().name());
			identityService.saveGroup(g);
			
			g = identityService.newGroup(wpd.getHr().getName());
			g.setName(wpd.getHr().getDescription());
			g.setType(wpd.getHr().getRoleType().name());
			identityService.saveGroup(g);
			
			g = identityService.newGroup(wpd.getTreasurer().getName());
			g.setName(wpd.getTreasurer().getDescription());
			g.setType(wpd.getTreasurer().getRoleType().name());
			identityService.saveGroup(g);
			
			g = identityService.newGroup(wpd.getCashier().getName());
			g.setName(wpd.getCashier().getDescription());
			g.setType(wpd.getCashier().getRoleType().name());
			identityService.saveGroup(g);
			
			g = identityService.newGroup(wpd.getSupportCrew().getName());
			g.setName(wpd.getSupportCrew().getDescription());
			g.setType(wpd.getSupportCrew().getRoleType().name());
			identityService.saveGroup(g);
		}
	}
	
	private Role getRole(EntityManager em, String name) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = b.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(r).where(b.equal(r.get("name"), name));
		Role role = null;
		try {
			role = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return role;
	}
	
	private void user(EntityManager em, IdentityService identityService, WebPresetData wpd) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = b.createQuery(Boolean.class);
		Root<Employee> r = q.from(Employee.class);
		Expression<Boolean> restriction = b.<String>in(r.<String>get("email"))
				.value(wpd.getBill().getEmail())
				.value(wpd.getJenny().getEmail())
				.value(wpd.getEric().getEmail())
				.value(wpd.getTom().getEmail())
				.value(wpd.getKermit().getEmail())
				.value(wpd.getAmy().getEmail())
				.value(wpd.getAndy().getEmail())
				.value(wpd.getTony().getEmail())
				.value(wpd.getLily().getEmail())
				.value(wpd.getThomas().getEmail());
		q = q.select(b.greaterThan(b.count(r), 0L)).where(restriction);
		// 如果创建过，则不再创建
		if (em.createQuery(q).getSingleResult()) {
			wpd.setBill(getUser(em, wpd.getBill().getEmail()));
			wpd.setJenny(getUser(em, wpd.getJenny().getEmail()));
			wpd.setEric(getUser(em, wpd.getEric().getEmail()));
			wpd.setTom(getUser(em, wpd.getTom().getEmail()));
			wpd.setKermit(getUser(em, wpd.getKermit().getEmail()));
			wpd.setAmy(getUser(em, wpd.getAmy().getEmail()));
			wpd.setAndy(getUser(em, wpd.getAndy().getEmail()));
			wpd.setTony(getUser(em, wpd.getTony().getEmail()));
			wpd.setLily(getUser(em, wpd.getLily().getEmail()));
			wpd.setThomas(getUser(em, wpd.getThomas().getEmail()));
		} else {
			createUser(em, identityService, wpd.getBill());
			createUser(em, identityService, wpd.getJenny());
			createUser(em, identityService, wpd.getEric());
			createUser(em, identityService, wpd.getTom());
			createUser(em, identityService, wpd.getKermit());
			createUser(em, identityService, wpd.getAmy());
			createUser(em, identityService, wpd.getAndy());
			createUser(em, identityService, wpd.getTony());
			createUser(em, identityService, wpd.getLily());
			createUser(em, identityService, wpd.getThomas());
		}
	}
	
	private Employee getUser(EntityManager em, String email) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Employee> q = b.createQuery(Employee.class);
		Root<Employee> r = q.from(Employee.class);
		q = q.select(r).where(b.equal(r.get("email"), email));
		Employee employee = null;
		try {
			employee = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (employee != null) {
			// 加载关系
			employee.getRoles().size();
			employee.getDepartment();
		}
		return employee;
	}
	
	private void createUser(EntityManager em, IdentityService identityService, Employee employee) {
		if (employee == null) {
			return;
		}
		// 将Department替换为持久化状态的对象
		if (employee.getDepartment() != null) {
			String name = employee.getDepartment().getName();
			Department department = getDepartment(em, name);
			employee.setDepartment(department);
			department.getEmployees().add(employee);
		}
		// 将角色替换为持久化状态的对象
		Set<String> roleNames = employee.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = b.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(r).where(r.get("name").in(roleNames));
		List<Role> roles = em.createQuery(q).getResultList();
		employee.getRoles().clear();
		employee.getRoles().addAll(roles);
		roles.forEach(role -> role.getUsers().add(employee));
		
		String salt = BCrypt.gensalt(10, new SecureRandom());
		employee.setPassword(BCrypt.hashpw(employee.getPassword(), salt));
		employee.setEmpNum(getMaxEmpNo(em) + 1);
		
		// 处理一对一关联关系
		em.persist(employee);
		EmployeeRef ref = new EmployeeRef(employee);
		employee.setEmployeeRef(ref);
		em.persist(ref);
		
		User user = identityService.newUser(employee.getId().toString());
		user.setFirstName(employee.getName());
		user.setLastName(employee.getNickname());
		user.setEmail(employee.getEmail());
		user.setPassword(employee.getPassword());
		identityService.saveUser(user);
		
		// 若在identityService中查不到组名，则说明与Role中的数据不一致，应该抛出异常
		employee.getRoles().forEach(role -> identityService.createMembership(employee.getId().toString(), role.getName()));
	}
	
	private Integer getMaxEmpNo(EntityManager em) {
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Integer> q = b.createQuery(Integer.class);
		Root<Employee> r = q.from(Employee.class);
		return em.createQuery(q.select(b.max(r.get("empNum")))).getSingleResult();
	}
}
