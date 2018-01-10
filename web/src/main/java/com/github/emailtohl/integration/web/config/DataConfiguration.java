package com.github.emailtohl.integration.web.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 预置数据的配置
 * 
 * @author HeLei
 */
@Configuration
@Import(ActivitiConfiguration.class)
public class DataConfiguration {
	
	@Bean
	public WebPreData WebPreData(EntityManagerFactory jpaEntityManagerFactory) {
		EntityManager em = jpaEntityManagerFactory.createEntityManager();
		WebPreData pd = new WebPreData();
		em.getTransaction().begin();
		
		type(em, pd);
		
		em.getTransaction().commit();
		em.close();
		return pd;
	}
	
	private void type(EntityManager em, WebPreData pd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Type> q = cb.createQuery(Type.class);
		Root<Type> r = q.from(Type.class);
		q = q.select(r).where(cb.equal(r.get("name"), pd.unclassified.getName()));
		Type unclassified = null;
		try {
			unclassified = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (unclassified == null) {
			em.persist(pd.unclassified);
		} else {
			pd.unclassified.setId(unclassified.getId());
		}
	}
}
