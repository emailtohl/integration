package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;

import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.web.WebTestConfig;
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
	TypeService typeService;
	@Inject
	Gson gson;
	
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
	
	@Test
	public void testUpdate() {
		Type sub = typeService.get(subId);
		Type newEntity = new Type("typename", "for update test", sub);
		Type after = typeService.update(id, newEntity);
		// 不能将子节点作为当前被修改节点的父节点
		assertNotEquals(sub, after.getParent());
		
		Type other = new Type("other", "for other test", null);
		other = typeService.create(other);
		if (other == null || other.getId() == null) {
			fail("未能创建新的Type，测试失败");
		}
		try {
			newEntity = new Type("again", "for update again test", other);
			after = typeService.update(id, newEntity);
			assertEquals(other, after.getParent());
		} finally {
			typeService.delete(other.getId());
		}
	}

}
