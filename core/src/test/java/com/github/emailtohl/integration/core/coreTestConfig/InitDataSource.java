package com.github.emailtohl.integration.core.coreTestConfig;

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

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Department;

/**
 * 初始化数据库
 * 
 * @author HeLei
 */
//@Component 如果是自动扫描则不容易理解，显示地写在配置文件中
class InitDataSource {
	@Inject
	EntityManagerFactory factory;
	
	public InitDataSource() {}

	public InitDataSource(EntityManagerFactory factory) {
		this.factory = factory;
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
			synchronized (InitDataSource.class) {
				if (!isInit) {
					CoreTestData td = new CoreTestData();
					EntityManager em = factory.createEntityManager();
					em.getTransaction().begin();
					
					Set<Role> roles = td.foo.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
					Department d = getDepartment(em, td.foo.getDepartment());
					td.foo.getRoles().clear();
					td.foo.getRoles().addAll(roles);
					roles.forEach(r -> r.getUsers().add(td.foo));
					td.foo.setDepartment(d);
					
					roles = td.bar.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
					d = getDepartment(em, td.bar.getDepartment());
					td.bar.getRoles().clear();
					td.bar.getRoles().addAll(roles);
					roles.forEach(r -> r.getUsers().add(td.bar));
					td.bar.setDepartment(d);
					
					roles = td.baz.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
					td.baz.getRoles().clear();
					td.baz.getRoles().addAll(roles);
					roles.forEach(r -> r.getUsers().add(td.baz));
					
					roles = td.qux.getRoles().stream().map(r -> getRole(em, r)).collect(Collectors.toSet());
					td.qux.getRoles().clear();
					td.qux.getRoles().addAll(roles);
					roles.forEach(r -> r.getUsers().add(td.qux));
					
					em.persist(td.foo);
					em.persist(td.bar);
					em.persist(td.baz);
					em.persist(td.qux);

					em.getTransaction().commit();
					em.close();
					isInit = true;
				}
				
			}
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
}
