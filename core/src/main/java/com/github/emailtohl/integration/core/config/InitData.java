package com.github.emailtohl.integration.core.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.User;

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

	public InitData(EntityManagerFactory factory) {
		this.factory = factory;
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
		em.persist(pd.role);
		em.persist(pd.query_all_user);
		em.persist(pd.employee);
		em.persist(pd.employee_role);
		em.persist(pd.employee_lock);
		em.persist(pd.employee_reset_password);
		em.persist(pd.employee_delete);
		em.persist(pd.customer);
		em.persist(pd.customer_role);
		em.persist(pd.customer_level);
		em.persist(pd.customer_lock);
		em.persist(pd.customer_reset_password);
		em.persist(pd.customer_delete);
		em.persist(pd.audit_user);
		em.persist(pd.audit_role);
		em.persist(pd.resource_manager);
		em.persist(pd.content_manager);
		em.persist(pd.flow);
		em.persist(pd.application_form_transit);
		em.persist(pd.application_form_read_history);
		em.persist(pd.application_form_delete);
		em.persist(pd.forum_delete);
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
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> q = cb.createQuery(boolean.class);
		Root<User> r = q.from(User.class);
		q = q.select(cb.greaterThan(cb.count(r), 0L)).where(cb.equal(r.get("name"), pd.user_admin.getName()));
		boolean exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.user_admin);
		}
		
		q = cb.createQuery(boolean.class);
		Root<Customer> r1 = q.from(Customer.class);
		q = q.select(cb.greaterThan(cb.count(r1), 0L)).where(cb.equal(r1.get("email"), pd.emailtohl.getEmail()));
		exist = em.createQuery(q).getSingleResult();
		if (!exist) {
			em.persist(pd.emailtohl);
		}
	}
	
}
