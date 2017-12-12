package com.github.emailtohl.integration.core.coreTestConfig;

import java.security.SecureRandom;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.Constant;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;

/**
 * 初始化数据库
 * 
 * @author HeLei
 */
//@Component 如果是自动扫描则不容易理解，显示地写在配置文件中
class AppendTestData {
	@Inject
	EntityManagerFactory factory;
	
	final String customerDefaultPassword;
	final String employeeDefaultPassword;
	
	public AppendTestData(EntityManagerFactory factory, Environment env) {
		this.factory = factory;
		this.customerDefaultPassword = env.getProperty(Constant.PROP_CUSTOMER_DEFAULT_PASSWORD);
		this.employeeDefaultPassword = env.getProperty(Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD);
	}

	/**
	 * 数据库初始化的标识
	 */
	private static volatile boolean isInit = false;
	
	/**
	 * 数据库初始化只执行一次
	 */
	@PostConstruct
	public void init() {
		if (!isInit) {
			synchronized (AppendTestData.class) {
				if (!isInit) {
					CoreTestData td = new CoreTestData();
					EntityManager em = factory.createEntityManager();
					em.getTransaction().begin();

					foo(em, td);
					bar(em, td);
					baz(em, td);
					qux(em, td);
					
					em.getTransaction().commit();
					em.close();
					isInit = true;
				}
				
			}
		}
	}
	
	private void foo(EntityManager em, CoreTestData td) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
		Root<Employee> root = q.from(Employee.class);
		q = q.select(root).where(cb.equal(root.get("empNum"), td.foo.getEmpNum()));
		Employee e = null;
		try {
			e = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {}
		if (e != null) {
			e.setPassword(hashpw(employeeDefaultPassword));
			return;
		}
		Set<Role> roles = td.foo.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
		Department d = getDepartment(em, td.foo.getDepartment());
		td.foo.getRoles().clear();
		td.foo.getRoles().addAll(roles);
		roles.forEach(r -> r.getUsers().add(td.foo));
		td.foo.setDepartment(d);
		td.foo.setPassword(hashpw(employeeDefaultPassword));
		em.persist(td.foo);
		EmployeeRef fooRef = new EmployeeRef(td.foo);
		td.foo.setEmployeeRef(fooRef);
		em.persist(fooRef);
	}
	
	private void bar(EntityManager em, CoreTestData td) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
		Root<Employee> root = q.from(Employee.class);
		q = q.select(root).where(cb.equal(root.get("empNum"), td.bar.getEmpNum()));
		Employee e = null;
		try {
			e = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {}
		if (e != null) {
			e.setPassword(hashpw(employeeDefaultPassword));
			return;
		}
		Set<Role> roles = td.bar.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
		Department d = getDepartment(em, td.bar.getDepartment());
		td.bar.getRoles().clear();
		td.bar.getRoles().addAll(roles);
		roles.forEach(r -> r.getUsers().add(td.bar));
		td.bar.setDepartment(d);
		td.bar.setPassword(hashpw(employeeDefaultPassword));
		em.persist(td.bar);
		EmployeeRef barRef = new EmployeeRef(td.bar);
		td.bar.setEmployeeRef(barRef);
		em.persist(barRef);
	}
	
	private void baz(EntityManager em, CoreTestData td) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
		Root<Customer> root = q.from(Customer.class);
		q = q.select(root).where(cb.equal(root.get("email"), td.baz.getEmail()));
		Customer c = null;
		try {
			c = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {}
		if (c != null) {
			c.setPassword(hashpw(customerDefaultPassword));
			return;
		}
		Set<Role> roles = td.baz.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
		td.baz.getRoles().clear();
		td.baz.getRoles().addAll(roles);
		roles.forEach(r -> r.getUsers().add(td.baz));
		td.baz.setPassword(hashpw(customerDefaultPassword));
		em.persist(td.baz);
		CustomerRef bazRef = new CustomerRef(td.baz);
		td.baz.setCustomerRef(bazRef);
		em.persist(bazRef);
	}
	
	private void qux(EntityManager em, CoreTestData td) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
		Root<Customer> root = q.from(Customer.class);
		q = q.select(root).where(cb.equal(root.get("cellPhone"), td.qux.getCellPhone()));
		Customer c = null;
		try {
			c = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {}
		if (c != null) {
			c.setPassword(hashpw(customerDefaultPassword));
			return;
		}
		Set<Role> roles = td.qux.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
		td.qux.getRoles().clear();
		td.qux.getRoles().addAll(roles);
		roles.forEach(r -> r.getUsers().add(td.qux));
		td.qux.setPassword(hashpw(customerDefaultPassword));
		em.persist(td.qux);
		CustomerRef quxRef = new CustomerRef(td.qux);
		td.qux.setCustomerRef(quxRef);
		em.persist(quxRef);
	}

	private Role getRole(EntityManager em, Role trans) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = cb.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), trans.getName()));
		Role pers = null;
		try {
			pers = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return pers;
	}
	
	private Department getDepartment(EntityManager em, Department trans) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Department> q = cb.createQuery(Department.class);
		Root<Department> r = q.from(Department.class);
		q = q.select(r).where(cb.equal(r.get("name"), trans.getName()));
		Department pers = null;
		try {
			pers = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return pers;
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}
}
