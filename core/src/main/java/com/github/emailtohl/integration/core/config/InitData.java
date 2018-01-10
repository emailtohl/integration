package com.github.emailtohl.integration.core.config;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.core.env.Environment;
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
	
	private PresetData pd;

	public InitData(EntityManagerFactory factory, Environment env) {
		this.factory = factory;
		this.env = env;
	}
	
	public PresetData init() {
		if (exec)
			return pd;
		synchronized (getClass()) {
			if (exec)
				return pd;
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
			return pd;
		}
	}
	
	private void authority(EntityManager em, PresetData pd) {
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
			em.createQuery(criteriaQuery).getResultList().forEach(tuple -> nameIdMap.put((String) tuple.get("name"), (Long) tuple.get("id")));
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
	
	private void role(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = cb.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.role_admin.getName()));
		Role admin = null;
		try {
			admin = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (admin == null) {
			em.persist(pd.role_admin);
		} else {
			pd.role_admin.setId(admin.getId());
		}
		
		q = cb.createQuery(Role.class);
		r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.role_manager.getName()));
		Role manager = null;
		try {
			manager = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (manager == null) {
			em.persist(pd.role_manager);
		} else {
			pd.role_manager.setId(manager.getId());
		}

		q = cb.createQuery(Role.class);
		r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.role_staff.getName()));
		Role staff = null;
		try {
			staff = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (staff == null) {
			em.persist(pd.role_staff);
		} else {
			pd.role_staff.setId(staff.getId());
		}
		
		q = cb.createQuery(Role.class);
		r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.role_guest.getName()));
		Role guest = null;
		try {
			guest = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (guest == null) {
			em.persist(pd.role_guest);
		} else {
			pd.role_guest.setId(guest.getId());
		}
	}
	
	private void company(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Company> q = cb.createQuery(Company.class);
		Root<Company> r = q.from(Company.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.company.getName()));
		Company c = null;
		try {
			c = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (c == null) {
			em.persist(pd.company);
		} else {
			pd.company.setId(c.getId());
		}
	}
	
	private void department(EntityManager em, PresetData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Department> q = cb.createQuery(Department.class);
		Root<Department> r = q.from(Department.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.qa.getName()));
		Department d = null;
		try {
			d = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
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
		} catch (NoResultException e) {}
		if (d == null) {
			em.persist(pd.product);
		} else {
			pd.product.setId(d.getId());
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
		
		CriteriaQuery<Employee> empQuery = cb.createQuery(Employee.class);
		Root<Employee> empRoot = empQuery.from(Employee.class);
		empQuery = empQuery.select(empRoot).where(cb.equal(empRoot.get("empNum"), pd.user_bot.getEmpNum()));
		Employee bot = null;
		try {
			bot = em.createQuery(empQuery).getSingleResult();
		} catch (NoResultException ex) {}
		if (bot == null) {
			pd.user_bot.setPassword(employeeDefaultPassword);
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
		} catch (NoResultException ex) {}
		if (admin == null) {
			pd.user_admin.setPassword(adminPassword);
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
		} catch (NoResultException ex) {}
		if (anon == null) {
			pd.user_anonymous.setPassword(customerDefaultPassword);
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
		} catch (NoResultException ex) {}
		if (emailtohl == null) {
			pd.user_emailtohl.setPassword(customerDefaultPassword);
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
}
