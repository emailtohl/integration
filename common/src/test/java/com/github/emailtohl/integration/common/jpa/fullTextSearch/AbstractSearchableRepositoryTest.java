package com.github.emailtohl.integration.common.jpa.fullTextSearch;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.emailtohl.integration.common.commonTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.common.commonTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.common.jpa.testEntities.Relation1;
import com.github.emailtohl.integration.common.testEntities.Article;
/**
 * JPA2的标准查询的测试类
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
@Transactional
public class AbstractSearchableRepositoryTest {
	private static final Logger logger = LogManager.getLogger();
	@Inject ApplicationContext context;
	class ArticleFullTextSearch extends AbstractSearchableRepository<Article> {}
	ArticleFullTextSearch articleFullTextSearch = new ArticleFullTextSearch();
	class TestFindByField extends AbstractSearchableRepository<Relation1>{}
	
	/**
	 * 需要在事务环境中使用，所以在具体类中进行测试
	 */
	@Test
	public void testInitialize() {
		AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
		factory.autowireBeanProperties(articleFullTextSearch, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		factory.initializeBean(articleFullTextSearch, "articleFullTextSearch");
	}

	@Test
	public void testAbstractSearchableRepository() {
		assertEquals(Article.class, articleFullTextSearch.getEntityClass());
		logger.debug(Arrays.toString(articleFullTextSearch.onFields));
		assertArrayEquals(new String[] {"author.description", "author.email", "author.name", "author.username", "body", "comments.content", "comments.critics", "keywords", "summary", "title"}, articleFullTextSearch.onFields);
	
		TestFindByField t = new TestFindByField();
		assertEquals(Relation1.class, t.getEntityClass());
		logger.debug(Arrays.toString(t.onFields));
		assertArrayEquals(new String[] {"relation1", "relation2.relation2"}, t.onFields);
	}
	
	@Test
	public void testIndexedEmbeddedCollection() {
		ArticleFullTextSearch articleFullTextSearch = new ArticleFullTextSearch();
		logger.debug(Arrays.toString(articleFullTextSearch.onFields));
	}
	
}
