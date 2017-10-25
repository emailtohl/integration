package com.github.emailtohl.integration.nuser.userTestConfig;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.github.emailtohl.integration.nuser.UserTestData;

/**
 * 初始化数据库
 * 
 * @author HeLei
 * @date 2017.06.13
 */
//@Component 如果是自动扫描则不容易理解，显示地写在配置文件中
class InitDataSource {
	@Inject
	EntityManagerFactory factory;
	
	public InitDataSource() {}

	public InitDataSource(EntityManagerFactory factory) {
		super();
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
					UserTestData td = new UserTestData();
					EntityManager em = factory.createEntityManager();
					em.getTransaction().begin();

					em.persist(td.role);
					em.persist(td.query_all_user);
					em.persist(td.employee);
					em.persist(td.employee_role);
					em.persist(td.employee_lock);
					em.persist(td.employee_delete);
					em.persist(td.customer);
					em.persist(td.customer_role);
					em.persist(td.customer_lock);
					em.persist(td.customer_delete);
					em.persist(td.flow);
					em.persist(td.application_form_transit);
					em.persist(td.application_form_read_history);
					em.persist(td.application_form_delete);
					em.persist(td.forum_delete);
					em.persist(td.audit_user);
					em.persist(td.audit_role);
					em.persist(td.resource_manager);
					em.persist(td.content_manager);

					em.persist(td.role_admin);
					em.persist(td.role_manager);
					em.persist(td.employee);
					em.persist(td.role_guest);

					em.persist(td.company);
					em.persist(td.product);
					em.persist(td.qa);
					em.persist(td.emailtohl);
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

}
