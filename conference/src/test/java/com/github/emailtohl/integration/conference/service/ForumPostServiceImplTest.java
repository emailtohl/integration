package com.github.emailtohl.integration.conference.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.Pager;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchResult;
import com.github.emailtohl.integration.conference.ConferenceTestData;
import com.github.emailtohl.integration.conference.conferenceTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.conference.conferenceTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.conference.dao.ForumPostRepository;
import com.github.emailtohl.integration.conference.dto.ForumPostDto;
import com.github.emailtohl.integration.user.dao.UserRepository;
/**
 * 全文搜索测试
 * 先存储论坛帖子，然后再通过索引查询
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.H2_RAM_DB)
public class ForumPostServiceImplTest {
	static final Logger logger = LogManager.getLogger();
	final Pageable pageable = new PageRequest(0, 20);
	final String title_emailtohl = "emailtohl's post", title_foo = "foo's post", title_bar = "bar's post",
			Keywords_emailtohl = "first emailtohl", Keywords_foo = "first foo", Keywords_bar = "first bar",
			body_emailtohl = "hl's first post hello forum", body_foo = "foo's first post hello forum", body_bar = "bar's first post hello forum";
	@Inject ForumPostService forumPostService;
	@Inject ForumPostRepository forumPostRepository;
	@Inject UserRepository userRepository;
	ConferenceTestData td = new ConferenceTestData();
	
	private Set<Long> idSet = new HashSet<>();
	
	@Before
	public void setUp() throws Exception {
		idSet.add(forumPostService.save(td.emailtohl.getEmail(), title_emailtohl, Keywords_emailtohl, body_emailtohl));
		idSet.add(forumPostService.save(td.foo.getEmail(), title_foo, Keywords_foo, body_foo));
		idSet.add(forumPostService.save(td.bar.getEmail(), title_bar, Keywords_bar, body_bar));
	}

	@After
	public void tearDown() throws Exception {
		idSet.forEach(id -> forumPostService.delete(id));
		idSet.clear();
//		forumPostService.deleteByEmail(emailtohl.getEmail());
//		forumPostService.deleteByEmail(foo.getEmail());
//		forumPostService.deleteByEmail(bar.getEmail());
	}

	@Test
	public void testSearch() {
		Pager<SearchResult<ForumPostDto>> p = forumPostService.search(body_emailtohl, pageable);
		List<String> ls = Arrays.asList(title_emailtohl, title_foo, title_bar);
		for (SearchResult<ForumPostDto> s : p.getContent()) {
			logger.debug(s.getEntity().getTitle());
			logger.debug(s.getEntity().getKeywords());
			logger.debug(s.getEntity().getBody());
			logger.debug(s.getEntity().getUser().getEmail());
			logger.debug(s.getEntity().getUser().getName());
			logger.debug(s.getRelevance());
			assertTrue(ls.contains(s.getEntity().getTitle()));
		}
		
		p = forumPostService.search(Keywords_emailtohl, pageable);
		for (SearchResult<ForumPostDto> s : p.getContent()) {
			logger.debug(s.getEntity().getTitle());
			logger.debug(s.getEntity().getKeywords());
			logger.debug(s.getEntity().getBody());
			logger.debug(s.getEntity().getUser().getEmail());
			logger.debug(s.getEntity().getUser().getName());
			logger.debug(s.getRelevance());
			assertTrue(ls.contains(s.getEntity().getTitle()));
		}
	}
	
	@Test
	public void testFind() {
		List<ForumPostDto> ls = forumPostService.findAll(body_foo);
		assertFalse(ls.isEmpty());
		ls = forumPostService.findAll(Keywords_foo);
		assertFalse(ls.isEmpty());
		
		Pager<ForumPostDto> p = forumPostService.findAllAndPaging(body_bar, pageable);
		assertFalse(p.getContent().isEmpty());
		
		p = forumPostService.find(Keywords_foo, pageable);
		assertFalse(p.getContent().isEmpty());
		
	}

	@Test
	public void testGetPager() {
		Pager<ForumPostDto> p = forumPostService.getPager(pageable);
		assertTrue(p.getTotalPages() > 0);
	}

	@Test
	public void testFindForumPostByTitle() {
		List<ForumPostDto> ls = forumPostService.findForumPostByTitle(title_emailtohl);
		assertFalse(ls.isEmpty());
	}

	@Test
	public void testFindByUserEmail() {
		List<ForumPostDto> ls = forumPostService.findByUserEmail(td.emailtohl.getEmail());
		assertFalse(ls.isEmpty());
	}

}
