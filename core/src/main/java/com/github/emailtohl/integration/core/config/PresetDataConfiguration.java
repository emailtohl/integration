package com.github.emailtohl.integration.core.config;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;

/**
 * 预置数据的配置
 * 
 * @author HeLei
 */
@Configuration
@Import(JpaConfiguration.class)
class PresetDataConfiguration {
	/**
	 * 初始化数据库中的数据
	 */
	@Bean
	public CorePresetData corePresetData(LocalContainerEntityManagerFactoryBean entityManagerFactory, Environment env) {
		synchronized (getClass()) {
			EntityManagerFactory factory = entityManagerFactory.getObject();
			CorePresetData pd = new CorePresetData();
			EntityManager em = factory.createEntityManager();
			em.getTransaction().begin();
			authority(em, pd);
			role(em, pd);
			company(em, pd);
			department(em, pd);
			user(em, env, pd);
			em.getTransaction().commit();
			em.close();
			return pd;
		}
	}

	private void authority(EntityManager em, CorePresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<Authority> r = q.from(Authority.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L));
		boolean exist = em.createQuery(q).getSingleResult();
		if (exist) {
			CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
			Root<Authority> root = criteriaQuery.from(Authority.class);
			criteriaQuery = criteriaQuery.multiselect(root.get("name").alias("name"), root.get("id").alias("id"));
			Map<String, Long> nameIdMap = new HashMap<>();
			em.createQuery(criteriaQuery).getResultList()
					.forEach(tuple -> nameIdMap.put(tuple.get("name", String.class), tuple.get("id", Long.class)));
			pd.auth_role.setId(nameIdMap.get(pd.auth_role.getName()));
			pd.auth_org.setId(nameIdMap.get(pd.auth_org.getName()));
			pd.auth_query_all_user.setId(nameIdMap.get(pd.auth_query_all_user.getName()));
			pd.auth_employee.setId(nameIdMap.get(pd.auth_employee.getName()));
			pd.auth_employee_role.setId(nameIdMap.get(pd.auth_employee_role.getName()));
			pd.auth_employee_lock.setId(nameIdMap.get(pd.auth_employee_lock.getName()));
			pd.auth_employee_reset_password.setId(nameIdMap.get(pd.auth_employee_reset_password.getName()));
			pd.auth_employee_delete.setId(nameIdMap.get(pd.auth_employee_delete.getName()));
			pd.auth_customer.setId(nameIdMap.get(pd.auth_customer.getName()));
			pd.auth_customer_role.setId(nameIdMap.get(pd.auth_customer_role.getName()));
			pd.auth_customer_level.setId(nameIdMap.get(pd.auth_customer_level.getName()));
			pd.auth_customer_lock.setId(nameIdMap.get(pd.auth_customer_lock.getName()));
			pd.auth_customer_reset_password.setId(nameIdMap.get(pd.auth_customer_reset_password.getName()));
			pd.auth_customer_delete.setId(nameIdMap.get(pd.auth_customer_delete.getName()));
			pd.auth_audit_user.setId(nameIdMap.get(pd.auth_audit_user.getName()));
			pd.auth_audit_role.setId(nameIdMap.get(pd.auth_audit_role.getName()));
			pd.auth_resource.setId(nameIdMap.get(pd.auth_resource.getName()));
			pd.auth_content.setId(nameIdMap.get(pd.auth_content.getName()));
		} else {
			em.persist(pd.auth_role);
			em.persist(pd.auth_org);
			em.persist(pd.auth_query_all_user);
			em.persist(pd.auth_employee);
			em.persist(pd.auth_employee_role);
			em.persist(pd.auth_employee_lock);
			em.persist(pd.auth_employee_reset_password);
			em.persist(pd.auth_employee_delete);
			em.persist(pd.auth_customer);
			em.persist(pd.auth_customer_role);
			em.persist(pd.auth_customer_level);
			em.persist(pd.auth_customer_lock);
			em.persist(pd.auth_customer_reset_password);
			em.persist(pd.auth_customer_delete);
			em.persist(pd.auth_audit_user);
			em.persist(pd.auth_audit_role);
			em.persist(pd.auth_resource);
			em.persist(pd.auth_content);
		}
	}

	private void role(EntityManager em, CorePresetData pd) {
		Role admin = getRole(em, pd.role_admin.getName());
		if (admin == null) {
			Set<Authority> authorities = pd.role_admin.getAuthorities().stream().map(a -> getAuthority(em, a.getName()))
					.filter(a -> a != null).peek(a -> a.getRoles().add(pd.role_admin)).collect(Collectors.toSet());
			pd.role_admin.getAuthorities().clear();
			pd.role_admin.getAuthorities().addAll(authorities);
			em.persist(pd.role_admin);
		} else {
			pd.role_admin.setId(admin.getId());
		}

		Role manager = getRole(em, pd.role_manager.getName());
		if (manager == null) {
			Set<Authority> authorities = pd.role_manager.getAuthorities().stream()
					.map(a -> getAuthority(em, a.getName())).filter(a -> a != null)
					.peek(a -> a.getRoles().add(pd.role_manager)).collect(Collectors.toSet());
			pd.role_manager.getAuthorities().clear();
			pd.role_manager.getAuthorities().addAll(authorities);
			em.persist(pd.role_manager);
		} else {
			pd.role_manager.setId(manager.getId());
		}

		Role staff = getRole(em, pd.role_staff.getName());
		if (staff == null) {
			Set<Authority> authorities = pd.role_staff.getAuthorities().stream().map(a -> getAuthority(em, a.getName()))
					.filter(a -> a != null).peek(a -> a.getRoles().add(pd.role_staff)).collect(Collectors.toSet());
			pd.role_staff.getAuthorities().clear();
			pd.role_staff.getAuthorities().addAll(authorities);
			em.persist(pd.role_staff);
		} else {
			pd.role_staff.setId(staff.getId());
		}

		Role guest = getRole(em, pd.role_guest.getName());
		if (guest == null) {
			Set<Authority> authorities = pd.role_guest.getAuthorities().stream().map(a -> getAuthority(em, a.getName()))
					.filter(a -> a != null).peek(a -> a.getRoles().add(pd.role_guest)).collect(Collectors.toSet());
			pd.role_guest.getAuthorities().clear();
			pd.role_guest.getAuthorities().addAll(authorities);
			em.persist(pd.role_guest);
		} else {
			pd.role_guest.setId(guest.getId());
		}
	}

	private void company(EntityManager em, CorePresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Company> q = cb.createQuery(Company.class);
		Root<Company> r = q.from(Company.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.company.getName()));
		Company c = null;
		try {
			c = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {
		}
		if (c == null) {
			em.persist(pd.company);
		} else {
			pd.company.setId(c.getId());
		}
	}

	private void department(EntityManager em, CorePresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Department> q = cb.createQuery(Department.class);
		Root<Department> r = q.from(Department.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.qa.getName()));
		Department d = null;
		try {
			d = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {
		}
		if (d == null) {
			em.persist(pd.qa);
		} else {
			pd.qa.setId(d.getId());
		}

		q = cb.createQuery(Department.class);
		r = q.from(Department.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.product.getName()));
		d = null;
		try {
			d = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {
		}
		if (d == null) {
			em.persist(pd.product);
		} else {
			pd.product.setId(d.getId());
		}
	}

	private void user(EntityManager em, Environment env, CorePresetData pd) {
		String adminPassword = env.getProperty("admin.password");
		adminPassword = hashpw(adminPassword);
		String employeeDefaultPassword = env.getProperty(Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD);
		employeeDefaultPassword = hashpw(employeeDefaultPassword);
		String customerDefaultPassword = env.getProperty(Constant.PROP_CUSTOMER_DEFAULT_PASSWORD);
		customerDefaultPassword = hashpw(customerDefaultPassword);

		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<Employee> empQuery = cb.createQuery(Employee.class);
		Root<Employee> empRoot = empQuery.from(Employee.class);
		empQuery = empQuery.select(empRoot).where(cb.equal(empRoot.get("empNum"), pd.user_bot.getEmpNum()));
		Employee bot = null;
		try {
			bot = em.createQuery(empQuery).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (bot == null) {
			pd.user_bot.setPassword(employeeDefaultPassword);
			Set<Role> roles = pd.user_bot.getRoles().stream().map(r -> getRole(em, r.getName())).filter(r -> r != null)
					.peek(r -> r.getUsers().add(pd.user_bot)).collect(Collectors.toSet());
			pd.user_bot.getRoles().clear();
			pd.user_bot.getRoles().addAll(roles);
			em.persist(pd.user_bot);
			EmployeeRef ref = new EmployeeRef(pd.user_bot);
			pd.user_bot.setEmployeeRef(ref);
		} else {
			pd.user_bot.setId(bot.getId());
			EmployeeRef ref = new EmployeeRef(pd.user_bot);
			pd.user_bot.setEmployeeRef(ref);
			pd.user_bot.setPassword(bot.getPassword());
		}

		empQuery = cb.createQuery(Employee.class);
		empRoot = empQuery.from(Employee.class);
		empQuery = empQuery.select(empRoot).where(cb.equal(empRoot.get("empNum"), pd.user_admin.getEmpNum()));
		Employee admin = null;
		try {
			admin = em.createQuery(empQuery).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (admin == null) {
			pd.user_admin.setPassword(adminPassword);
			Set<Role> roles = pd.user_admin.getRoles().stream().map(r -> getRole(em, r.getName()))
					.filter(r -> r != null).peek(r -> r.getUsers().add(pd.user_admin)).collect(Collectors.toSet());
			pd.user_admin.getRoles().clear();
			pd.user_admin.getRoles().addAll(roles);
			em.persist(pd.user_admin);
			EmployeeRef ref = new EmployeeRef(pd.user_admin);
			pd.user_admin.setEmployeeRef(ref);
		} else {
			pd.user_admin.setId(admin.getId());
			EmployeeRef ref = new EmployeeRef(pd.user_admin);
			pd.user_admin.setEmployeeRef(ref);
			pd.user_admin.setPassword(admin.getPassword());
		}

		CriteriaQuery<Customer> custQuery = cb.createQuery(Customer.class);
		Root<Customer> custRoot = custQuery.from(Customer.class);
		custQuery = custQuery.select(custRoot).where(cb.equal(custRoot.get("email"), pd.user_anonymous.getEmail()));
		Customer anon = null;
		try {
			anon = em.createQuery(custQuery).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (anon == null) {
			pd.user_anonymous.setPassword(customerDefaultPassword);
			Set<Role> roles = pd.user_anonymous.getRoles().stream().map(r -> getRole(em, r.getName()))
					.filter(r -> r != null).peek(r -> r.getUsers().add(pd.user_anonymous)).collect(Collectors.toSet());
			pd.user_anonymous.getRoles().clear();
			pd.user_anonymous.getRoles().addAll(roles);
			em.persist(pd.user_anonymous);
			CustomerRef ref = new CustomerRef(pd.user_anonymous);
			pd.user_anonymous.setCustomerRef(ref);
		} else {
			pd.user_anonymous.setId(anon.getId());
			CustomerRef ref = new CustomerRef(pd.user_anonymous);
			pd.user_anonymous.setCustomerRef(ref);
			pd.user_anonymous.setPassword(anon.getPassword());
		}

		custQuery = cb.createQuery(Customer.class);
		custRoot = custQuery.from(Customer.class);
		custQuery = custQuery.select(custRoot).where(cb.equal(custRoot.get("email"), pd.user_emailtohl.getEmail()));
		Customer emailtohl = null;
		try {
			emailtohl = em.createQuery(custQuery).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (emailtohl == null) {
			pd.user_emailtohl.setPassword(customerDefaultPassword);
			Set<Role> roles = pd.user_emailtohl.getRoles().stream().map(r -> getRole(em, r.getName()))
					.filter(r -> r != null).peek(r -> r.getUsers().add(pd.user_emailtohl)).collect(Collectors.toSet());
			pd.user_emailtohl.getRoles().clear();
			pd.user_emailtohl.getRoles().addAll(roles);
			em.persist(pd.user_emailtohl);
			CustomerRef ref = new CustomerRef(pd.user_emailtohl);
			pd.user_emailtohl.setCustomerRef(ref);
		} else {
			pd.user_emailtohl.setId(emailtohl.getId());
			CustomerRef ref = new CustomerRef(pd.user_emailtohl);
			pd.user_emailtohl.setCustomerRef(ref);
			pd.user_emailtohl.setPassword(emailtohl.getPassword());
		}
	}

	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}

	private Role getRole(EntityManager em, String roleName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = cb.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), roleName));
		Role role = null;
		try {
			role = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {
		}
		return role;
	}

	private Authority getAuthority(EntityManager em, String authorityName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Authority> q = cb.createQuery(Authority.class);
		Root<Authority> r = q.from(Authority.class);
		q = q.select(r).where(cb.equal(r.get("name"), authorityName));
		Authority authority = null;
		try {
			authority = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {
		}
		return authority;
	}
}
