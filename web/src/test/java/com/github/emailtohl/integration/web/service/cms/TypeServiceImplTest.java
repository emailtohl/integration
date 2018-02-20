package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.web.WebTestConfig;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.google.gson.Gson;

/**
 * 测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class TypeServiceImplTest {
	@Inject
	EntityManagerFactory factory;
	@Inject
	TypeService typeService;
	@Inject
	Gson gson;
	
	@Inject
	CorePresetData cpd;
	
	Long id, superId, subId;

	@Before
	public void setUp() throws Exception {
		Type sup = new Type("super", "for test super", null);
		Type type = new Type("typename", "for test", sup);
		Type sub = new Type("sub item", "for test sub", type);
		
		sup = typeService.create(sup);
		type = typeService.create(type);
		sub = typeService.create(sub);
		
		superId = sup.getId();
		id = type.getId();
		subId = sub.getId();
	}

	@After
	public void tearDown() throws Exception {
		typeService.delete(id);
		Type sup = typeService.get(superId);
		Type sub = typeService.get(subId);
		// 本节点被删除后，下级节点的父节点是本节点的上级节点
		assertEquals(sup, sub.getParent());
		
		typeService.delete(superId);
		typeService.delete(subId);
	}

	@Test
	public void testQuery() {
		// exist
		assertTrue(typeService.exist("typename"));
		
		// query
		Type params = new Type("typename", "for test", null);
		List<Type> ls = typeService.query(params);
		gson.toJson(ls);
		assertFalse(ls.isEmpty());
		
		Paging<Type> p = typeService.query(params, new PageRequest(0, 20));
		gson.toJson(p);
		assertFalse(p.getContent().isEmpty());
	}
	
	@Test(expected = NotAcceptableException.class)
	public void testUpdateNotAcceptableException() {
		Type sub = typeService.get(subId);
		// 将目标的父节点修改为其原先的子节点，这样会得到报错
		Type newEntity = new Type("typename", "for update test", sub);
		Type after = typeService.update(id, newEntity);
		fail("不会到达此处" + after.toString());
	}
	
	@Test
	public void testUpdate() {
		Type other = new Type("other", "for other test", null);
		other = typeService.create(other);
		Type newEntity = new Type("again", "for update again test", other);
		try {
			Type after = typeService.update(id, newEntity);
			assertEquals(other, after.getParent());
			// 为了让 tearDown保持正确，再修改回来
			Type targetParent = typeService.get(superId);
			newEntity.setParent(targetParent);
			after = typeService.update(id, newEntity);
		} finally {
			typeService.delete(other.getId());
		}
	}

	@Test
	public void testGetTypesWithArticleNum() {
		Article a1 = new Article("title1", "keywords1", "body1", "summary1");
		Article a2 = new Article("title2", "keywords2", "body2", "summary2");
		Article a3 = new Article("title3", "keywords3", "body3", "summary3");
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Type type = em.find(Type.class, id);
		Type sub = em.find(Type.class, subId);
		
		a1.setType(type);
		a2.setType(sub);
		a3.setType(type);
		
		CustomerRef anon = em.find(CustomerRef.class, cpd.user_anonymous.getId());
		a1.setAuthor(anon);
		a2.setAuthor(anon);
		a3.setAuthor(anon);
		
		em.persist(a1);
		em.persist(a2);
		em.persist(a3);
		
		em.getTransaction().commit();
		
		List<Type> ls = typeService.getTypesWithArticleNum(null);
		assertFalse(ls.isEmpty());
		
		Map<Long, Type> map = new HashMap<>();
		for (Type t : ls) {
			map.put(t.getId(), t);
		}
		
		assertEquals(2, map.get(id).getArticlesNum().intValue());
		assertEquals(1, map.get(subId).getArticlesNum().intValue());
		assertEquals(0, map.get(superId).getArticlesNum().intValue());
		
		ls = typeService.getTypesWithArticleNum(type);
		assertFalse(ls.isEmpty());
		
		em.getTransaction().begin();
		em.merge(a1);
		em.merge(a2);
		em.merge(a3);
		
		em.remove(a1);
		em.remove(a2);
		em.remove(a3);
		em.getTransaction().commit();
		em.close();
	}
}
