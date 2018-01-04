package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.web.WebTestData;
import com.github.emailtohl.integration.web.config.ServiceConfiguration;
import com.github.emailtohl.integration.web.service.cms.entities.Article;

/**
 * 测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class ArticleServiceImplTest {
	@Inject
	ArticleService articleService;
	
	WebTestData td = new WebTestData();
	
	Long id;

	@Before
	public void setUp() throws Exception {
		Article a = new Article();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetLong() {
		fail("Not yet implemented");
	}

}
