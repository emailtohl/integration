package com.github.emailtohl.integration.common.jpa.envers;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.RevisionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.emailtohl.integration.common.commonTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.common.commonTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.common.jpa.testRepository.CleanAuditData;
import com.github.emailtohl.integration.common.testEntities.CommonTestData;
import com.github.emailtohl.integration.common.testEntities.Customer;
import com.github.emailtohl.integration.common.testEntities.Role;
import com.github.emailtohl.integration.common.testEntities.User;
/**
 * Hibernate envers组件使用测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
@Transactional
public class AbstractAuditedRepositoryTest {
	private static final Logger logger = LogManager.getLogger();
	@Inject
	EntityManagerFactory entityManagerFactory;
	@Inject
	ApplicationContext context;
	@Inject
	CleanAuditData cleanAuditTestData;
	CommonTestData td = new CommonTestData();
	
	@Transactional
	class AuditedRepositoryForTest extends AbstractAuditedRepository<User> {}
	AuditedRepositoryForTest audRepos;
	private Long id;
	private Sort sort = new Sort(Sort.Direction.DESC, "name");
	private Pageable pageable = new PageRequest(0, 20, sort);
	

	@Before
	public void setUp() throws Exception {
		audRepos = new AuditedRepositoryForTest();
		AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
		factory.autowireBeanProperties(audRepos, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		factory.initializeBean(audRepos, "auditedRepositoryForTest");
		
		Customer u = new Customer();
		u.setEmail("forAuditTest@test.com");
		u.setTitle("ceo");
		u.setName("forAuditTest");
		u.setUsername("forAuditTest");
		u.setPassword("123456");
		
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		
		em.persist(u);
		
		em.getTransaction().commit();
		
		id = u.getId();
		u.setName("forAuditTestForUpdate");
		u.setTitle("cto");
		
		em.getTransaction().begin();
		
		Role r = (Role) em.createQuery("select r from Role r where r.name = ?1")
		.setParameter(1, td.employee.getName()).getSingleResult();
		u.getRoles().clear();
		u.getRoles().add(r);
		
		Customer n = em.merge(u);
		logger.debug(n);
		
		em.getTransaction().commit();
		em.close();
		
	}

	@After
	public void tearDown() throws Exception {
		// 删除后还有一次审计记录
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		
		Customer uu = em.find(Customer.class, id);
		em.remove(uu);
		
		em.getTransaction().commit();
		em.close();
		
		cleanAuditTestData.cleanUserAudit(id);
	}

	@Test
	public void test() {
		Number origin = null;
		
		Map<String, Object> propertyNameValueMap = new HashMap<String, Object>();
		propertyNameValueMap.put("name", "forAuditTest");
		// test getEntityRevision
		Page<Tuple<User>> page = audRepos.getRevisionInfoPage(propertyNameValueMap, pageable);
		for (Tuple<User> tuple : page.getContent()) {
			logger.debug(tuple.getEntity());
			logger.debug(tuple.getDefaultRevisionEntity());
			logger.debug(tuple.getRevisionType());
			
			Number rev = tuple.getDefaultRevisionEntity().getId();
			// test getEntityAtRevision
			User ru = audRepos.getEntityAtRevision(id, rev);
			logger.debug(ru);
			if (tuple.getRevisionType() == RevisionType.ADD) {
				origin = rev;
			}
			
			Page<User> pu = audRepos.getEntitiesAtRevision(rev, propertyNameValueMap, pageable);
			assertFalse(pu.getContent().isEmpty());
			pu.getContent().forEach(u -> {
				logger.debug(u);
			});
		
		}
		assertFalse(page.getContent().isEmpty());
		
		if (origin != null) {
			// 由于实体基类BaseEntity的createDate为不可变，所以回滚时遭遇数据库约束异常，暂时不能使用此接口
//			audRepos.rollback(id, origin);
		}
		
		List<Tuple<User>> ls = audRepos.getAllRevisionInfo(propertyNameValueMap);
		assertFalse(ls.isEmpty());
		ls.forEach(t -> {
			System.out.println(t);
			if (t.getRevisionType() == RevisionType.ADD) {
				int rev = t.getDefaultRevisionEntity().getId();
				audRepos.getEntitiesAtRevision(rev, propertyNameValueMap).forEach(u -> System.out.println(u));
			}
		});
		
	}

}
