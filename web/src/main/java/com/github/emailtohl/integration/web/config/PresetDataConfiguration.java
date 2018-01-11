package com.github.emailtohl.integration.web.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 预置数据的配置
 * 
 * @author HeLei
 */
@Configuration
@Import(ActivitiConfiguration.class)
public class PresetDataConfiguration {

	@Bean
	public WebPresetData webPresetData(EntityManagerFactory jpaEntityManagerFactory, CorePresetData cpd, IdentityService identityService) {
		synchronized (getClass()) {
			EntityManager em = jpaEntityManagerFactory.createEntityManager();
			WebPresetData wpd = new WebPresetData(cpd, identityService);
			em.getTransaction().begin();
			
			appendIdentityData(wpd, cpd, identityService);
			type(em, wpd);
			
			em.getTransaction().commit();
			em.close();
			return wpd;
		}
	}
	
	/**
	 * 将预置的账号信息补加到Activiti身份系统中
	 * @param cpd
	 * @param identityService
	 */
	private void appendIdentityData(WebPresetData wpd, CorePresetData cpd, IdentityService identityService) {
		// 用户组（角色）
		Group g = identityService.createGroupQuery().groupId(wpd.group_admin.getId()).singleResult();
		if (g == null) {
			identityService.saveGroup(wpd.group_admin);
		}
		
		g = identityService.createGroupQuery().groupId(wpd.group_manager.getId()).singleResult();
		if (g == null) {
			identityService.saveGroup(wpd.group_manager);
		}
		
		g = identityService.createGroupQuery().groupId(wpd.group_staff.getId()).singleResult();
		if (g == null) {
			identityService.saveGroup(wpd.group_staff);
		}
		
		g = identityService.createGroupQuery().groupId(wpd.group_guest.getId()).singleResult();
		if (g == null) {
			identityService.saveGroup(wpd.group_guest);
		}
		
		// 用户
		User u = identityService.createUserQuery().userId(wpd.user_admin.getId().toString()).singleResult();
		if (u == null) {
			identityService.saveUser(wpd.user_admin);
			cpd.user_admin.roleNames().forEach(groupId -> identityService.createMembership(wpd.user_admin.getId(), groupId));
		}
		
		u = identityService.createUserQuery().userId(wpd.user_bot.getId().toString()).singleResult();
		if (u == null) {
			identityService.saveUser(wpd.user_bot);
			cpd.user_bot.roleNames().forEach(groupId -> identityService.createMembership(wpd.user_bot.getId(), groupId));
		}
		
		u = identityService.createUserQuery().userId(wpd.user_anonymous.getId().toString()).singleResult();
		if (u == null) {
			identityService.saveUser(wpd.user_anonymous);
			cpd.user_anonymous.roleNames().forEach(groupId -> identityService.createMembership(wpd.user_anonymous.getId(), groupId));
		}
		
		u = identityService.createUserQuery().userId(wpd.user_emailtohl.getId().toString()).singleResult();
		if (u == null) {
			identityService.saveUser(wpd.user_emailtohl);
			cpd.user_emailtohl.roleNames().forEach(groupId -> identityService.createMembership(wpd.user_emailtohl.getId(), groupId));
		}
		
	}
	
	private void type(EntityManager em, WebPresetData wpd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Type> q = cb.createQuery(Type.class);
		Root<Type> r = q.from(Type.class);
		q = q.select(r).where(cb.equal(r.get("name"), wpd.unclassified.getName()));
		Type unclassified = null;
		try {
			unclassified = em.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		if (unclassified == null) {
			em.persist(wpd.unclassified);
		} else {
			wpd.unclassified.setId(unclassified.getId());
		}
	}
}
