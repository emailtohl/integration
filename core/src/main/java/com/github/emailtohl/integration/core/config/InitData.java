package com.github.emailtohl.integration.core.config;

import java.security.SecureRandom;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.Constant;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;

/**
 * 将预置数据插入数据库
 * @author HeLei
 */
class InitData {
	/**
	 * 执行标识
	 */
	private static volatile boolean exec = false;
	
	private EntityManagerFactory factory;
	
	private Environment env;

	public InitData(EntityManagerFactory factory, Environment env) {
		this.factory = factory;
		this.env = env;
	}
	
	public void init() {
		if (exec)
			return;
		synchronized (getClass()) {
			if (exec)
				return;
			PresetData pd = new PresetData();
			EntityManager em = factory.createEntityManager();
			em.getTransaction().begin();
			authority(em, pd);
			role(em, pd);
			company(em, pd);
			department(em, pd);
			user(em, pd);
			em.getTransaction().commit();
			em.close();
			exec = true;
		}
	}
	
	private void authority(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<Authority> r = q.from(Authority.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L));
		boolean exist = em.createQuery(q).getSingleResult();
		if (exist) {
			return;
		}
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
	
	private void role(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.role_admin.getName()));
		boolean exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.role_admin);
		}
		
		q = cb.createQuery(boolean.class);
		r = q.from(Role.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.role_manager.getName()));
		exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.role_manager);
		}
		
		q = cb.createQuery(boolean.class);
		r = q.from(Role.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.role_staff.getName()));
		exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.role_staff);
		}
		
		q = cb.createQuery(boolean.class);
		r = q.from(Role.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.role_guest.getName()));
		exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.role_guest);
		}
	}
	
	private void company(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<Company> r = q.from(Company.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.company.getName()));
		boolean exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.company);
		}
	}
	
	private void department(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<Department> r = q.from(Department.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.qa.getName()));
		boolean exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.qa);
		}
		
		q = cb.createQuery(boolean.class);
		r = q.from(Department.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.product.getName()));
		exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.product);
		}
	}
	
	private void user(EntityManager em, PresetData pd) {
		String adminPassword = env.getProperty("admin.password");
		adminPassword = hashpw(adminPassword);
		String employeeDefaultPassword = env.getProperty(Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD);
		employeeDefaultPassword = hashpw(employeeDefaultPassword);
		String customerDefaultPassword = env.getProperty(Constant.PROP_CUSTOMER_DEFAULT_PASSWORD);
		customerDefaultPassword = hashpw(customerDefaultPassword);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<Employee> botq = cb.createQuery(Employee.class);
		Root<Employee> botr = botq.from(Employee.class);
		botq = botq.select(botr).where(cb.equal(botr.get("empNum"), pd.user_bot.getEmpNum()));
		Employee bot = null;
		try {
			bot = em.createQuery(botq).getSingleResult();
		} catch (NoResultException ex) {}
		if (bot == null) {
			pd.user_bot.setPassword(employeeDefaultPassword);
			em.persist(pd.user_bot);
			EmployeeRef ref = new EmployeeRef(pd.user_bot);
			pd.user_bot.setEmployeeRef(ref);
		} else {
			bot.setPassword(employeeDefaultPassword);
		}
		
		CriteriaQuery<Employee> aq = cb.createQuery(Employee.class);
		Root<Employee> ar = aq.from(Employee.class);
		aq = aq.select(ar).where(cb.equal(ar.get("empNum"), pd.user_admin.getEmpNum()));
		Employee u = null;
		try {
			u = em.createQuery(aq).getSingleResult();
		} catch (NoResultException ex) {}
		if (u == null) {
			pd.user_admin.setPassword(adminPassword);
			em.persist(pd.user_admin);
			EmployeeRef ref = new EmployeeRef(pd.user_admin);
			pd.user_admin.setEmployeeRef(ref);
		} else {
			u.setPassword(adminPassword);
		}
		
		CriteriaQuery<Customer> anonq = cb.createQuery(Customer.class);
		Root<Customer> anonr = anonq.from(Customer.class);
		anonq = anonq.select(anonr).where(cb.equal(anonr.get("email"), pd.user_anonymous.getEmail()));
		Customer anon = null;
		try {
			anon = em.createQuery(anonq).getSingleResult();
		} catch (NoResultException ex) {}
		if (anon == null) {
			pd.user_anonymous.setPassword(customerDefaultPassword);
			em.persist(pd.user_anonymous);
			CustomerRef ref = new CustomerRef(pd.user_anonymous);
			pd.user_anonymous.setCustomerRef(ref);
		} else {
			anon.setPassword(customerDefaultPassword);
		}
		
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<Customer> r = q.from(Customer.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("email"), pd.user_emailtohl.getEmail()));
		Boolean exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			pd.user_emailtohl.setPassword(customerDefaultPassword);
			em.persist(pd.user_emailtohl);
			CustomerRef ref = new CustomerRef(pd.user_emailtohl);
			pd.user_emailtohl.setCustomerRef(ref);
		}
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}
}
