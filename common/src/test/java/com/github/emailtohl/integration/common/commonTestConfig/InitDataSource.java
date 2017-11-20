package com.github.emailtohl.integration.common.commonTestConfig;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.github.emailtohl.integration.common.testEntities.CommonTestData;

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
					CommonTestData td = new CommonTestData();
					EntityManager em = factory.createEntityManager();
					em.getTransaction().begin();

					em.persist(td.role_authority_allocation);
					em.persist(td.user_create_ordinary);
					em.persist(td.user_create_special);
					em.persist(td.user_enable);
					em.persist(td.user_disable);
					em.persist(td.user_grant_roles);
					em.persist(td.user_read_all);
					em.persist(td.user_read_self);
					em.persist(td.user_update_all);
					em.persist(td.user_update_self);
					em.persist(td.user_delete);
					em.persist(td.user_customer);
					em.persist(td.application_form_transit);
					em.persist(td.application_form_read_history);
					em.persist(td.application_form_delete);
					em.persist(td.forum_delete);
					em.persist(td.audit_user);
					em.persist(td.audit_role);
					em.persist(td.resource_manager);
					em.persist(td.content_manager);

					em.persist(td.admin);
					em.persist(td.manager);
					em.persist(td.employee);
					em.persist(td.user);

					em.persist(td.company);
					em.persist(td.product);
					em.persist(td.qa);
					em.persist(td.emailtohl);
					em.persist(td.foo);
					em.persist(td.bar);
					em.persist(td.baz);
					em.persist(td.qux);

					em.persist(td.parent);
					em.persist(td.subType);
					em.persist(td.article);
					em.persist(td.comment);

					em.getTransaction().commit();
					em.close();
					isInit = true;
				}
				
			}
		}
	}

}
