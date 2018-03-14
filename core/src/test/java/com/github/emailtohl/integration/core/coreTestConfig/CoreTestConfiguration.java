package com.github.emailtohl.integration.core.coreTestConfig;

import java.security.SecureRandom;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.config.CoreConfiguration;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 业务层的配置
 * 
 * @author HeLei
 */
@Configuration
@Import(CoreConfiguration.class)
public class CoreTestConfiguration implements ApplicationListener<ContextClosedEvent> {
	@Inject
	LocalContainerEntityManagerFactoryBean entityManagerFactory;
	/**
	 * 创造测试数据
	 * 
	 * @return
	 */
	@Bean
	public CoreTestData coreTestData(Environment env) {
		synchronized (getClass()) {
			EntityManagerFactory factory = entityManagerFactory.getObject();
			String customerDefaultPassword = env.getProperty(Constant.PROP_CUSTOMER_DEFAULT_PASSWORD);
			String employeeDefaultPassword = env.getProperty(Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD);
			customerDefaultPassword = hashpw(customerDefaultPassword);
			employeeDefaultPassword = hashpw(employeeDefaultPassword);
			
			CoreTestData td = new CoreTestData();
			EntityManager em = factory.createEntityManager();
			em.getTransaction().begin();
			
			foo(em, td, employeeDefaultPassword);
			bar(em, td, employeeDefaultPassword);
			baz(em, td, customerDefaultPassword);
			qux(em, td, customerDefaultPassword);
			
			em.getTransaction().commit();
			em.close();
			
			return td;
		}
	}

	private void foo(EntityManager em, CoreTestData td, String employeeDefaultPassword) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
		Root<Employee> root = q.from(Employee.class);
		q = q.select(root).where(cb.equal(root.get("empNum"), td.foo.getEmpNum()));
		Employee e = null;
		try {
			e = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (e == null) {
			td.foo.setPassword(employeeDefaultPassword);
			Set<Role> roles = td.foo.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
			Department d = getDepartment(em, td.foo.getDepartment());
			td.foo.getRoles().clear();
			td.foo.getRoles().addAll(roles);
			td.foo.setDepartment(d);
			em.persist(td.foo);
			EmployeeRef ref = new EmployeeRef(td.foo);
			td.foo.setEmployeeRef(ref);
			em.persist(ref);
		} else {
			td.foo.setId(e.getId());
			EmployeeRef ref = new EmployeeRef(td.foo);
			td.foo.setEmployeeRef(ref);
			td.foo.setPassword(e.getPassword());
		}
	}

	private void bar(EntityManager em, CoreTestData td, String employeeDefaultPassword) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
		Root<Employee> root = q.from(Employee.class);
		q = q.select(root).where(cb.equal(root.get("empNum"), td.bar.getEmpNum()));
		Employee e = null;
		try {
			e = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (e == null) {
			td.bar.setPassword(employeeDefaultPassword);
			Set<Role> roles = td.bar.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
			Department d = getDepartment(em, td.bar.getDepartment());
			td.bar.getRoles().clear();
			td.bar.getRoles().addAll(roles);
			td.bar.setDepartment(d);
			em.persist(td.bar);
			EmployeeRef ref = new EmployeeRef(td.bar);
			td.bar.setEmployeeRef(ref);
			em.persist(ref);
		} else {
			td.bar.setId(e.getId());
			EmployeeRef ref = new EmployeeRef(td.bar);
			td.bar.setEmployeeRef(ref);
			td.bar.setPassword(e.getPassword());
		}
	}

	private void baz(EntityManager em, CoreTestData td, String customerDefaultPassword) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
		Root<Customer> root = q.from(Customer.class);
		q = q.select(root).where(cb.equal(root.get("email"), td.baz.getEmail()));
		Customer c = null;
		try {
			c = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (c == null) {
			td.baz.setPassword(customerDefaultPassword);
			Set<Role> roles = td.baz.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
			td.baz.getRoles().clear();
			td.baz.getRoles().addAll(roles);
			em.persist(td.baz);
			CustomerRef ref = new CustomerRef(td.baz);
			td.baz.setCustomerRef(ref);
			em.persist(ref);
		} else {
			td.baz.setId(c.getId());
			CustomerRef ref = new CustomerRef(td.baz);
			td.baz.setCustomerRef(ref);
			td.baz.setPassword(c.getPassword());
		}
	}

	private void qux(EntityManager em, CoreTestData td, String customerDefaultPassword) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
		Root<Customer> root = q.from(Customer.class);
		q = q.select(root).where(cb.equal(root.get("cellPhone"), td.qux.getCellPhone()));
		Customer c = null;
		try {
			c = em.createQuery(q).getSingleResult();
		} catch (NoResultException ex) {
		}
		if (c == null) {
			td.qux.setPassword(customerDefaultPassword);
			Set<Role> roles = td.qux.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
			td.qux.getRoles().clear();
			td.qux.getRoles().addAll(roles);
			em.persist(td.qux);
			CustomerRef ref = new CustomerRef(td.qux);
			td.qux.setCustomerRef(ref);
			em.persist(ref);
		} else {
			td.qux.setId(c.getId());
			CustomerRef ref = new CustomerRef(td.qux);
			td.qux.setCustomerRef(ref);
			td.qux.setPassword(c.getPassword());
		}
	}

	private Role getRole(EntityManager em, Role trans) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = cb.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(r).where(cb.equal(r.get("name"), trans.getName()));
		Role pers = null;
		try {
			pers = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {
		}
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
		} catch (NoResultException e) {
		}
		return pers;
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		synchronized (getClass()) {
			CoreTestData td = new CoreTestData();
			EntityManagerFactory factory = entityManagerFactory.getObject();
			EntityManager em = factory.createEntityManager();
			em.getTransaction().begin();
			
			User foo = getUserByEmail(em, td.foo.getEmail());
			if (foo != null) {
				foo.getRoles().forEach(r -> r.getUsers().remove(foo));
				((Employee) foo).getDepartment().getEmployees().remove(foo);
				em.remove(foo);
			}
			User bar = getUserByEmail(em, td.bar.getEmail());
			if (bar != null) {
				bar.getRoles().forEach(r -> r.getUsers().remove(bar));
				((Employee) bar).getDepartment().getEmployees().remove(bar);
				em.remove(bar);
			}
			User baz = getUserByEmail(em, td.baz.getEmail());
			if (baz != null) {
				baz.getRoles().forEach(r -> r.getUsers().remove(baz));
				em.remove(baz);
			}
			User qux = getUserByEmail(em, td.qux.getEmail());
			if (qux != null) {
				qux.getRoles().forEach(r -> r.getUsers().remove(qux));
				em.remove(qux);
			}
			
			em.getTransaction().commit();
			em.close();
		}
	}
	
	private User getUserByEmail(EntityManager em, String email) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> q = cb.createQuery(User.class);
		Root<User> r = q.from(User.class);
		q = q.select(r).where(cb.equal(r.get("email"), email));
		User u = null;
		try {
			u = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return u;
	}
}
