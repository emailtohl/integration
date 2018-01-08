package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.web.WebTestConfig;
import com.github.emailtohl.integration.web.WebTestData;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class ArticleServiceImplTest {
	@Inject
	ArticleService articleService;
	@Inject
	WebTestData td;
	
	Long typeId;
	Long articleId;

	@Before
	public void setUp() throws Exception {
		Type tp = new Type("test parent", "for test", null);
		
		Article a = new Article();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetLong() {
//		fail("Not yet implemented");
	}

}
