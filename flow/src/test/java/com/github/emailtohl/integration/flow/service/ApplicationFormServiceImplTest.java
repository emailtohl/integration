package com.github.emailtohl.integration.flow.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.flow.FlowTestData;
import com.github.emailtohl.integration.flow.dao.ApplicationFormRepository;
import com.github.emailtohl.integration.flow.dao.ApplicationHandleHistoryRepository;
import com.github.emailtohl.integration.flow.entities.ApplicationForm;
import com.github.emailtohl.integration.flow.entities.ApplicationForm.Status;
import com.github.emailtohl.integration.flow.entities.ApplicationHandleHistory;
import com.github.emailtohl.integration.flow.flowTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.flow.flowTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.user.dao.UserRepository;
import com.github.emailtohl.integration.user.entities.User;
/**
 * 业务类测试
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.H2_RAM_DB)
public class ApplicationFormServiceImplTest {
	static final Logger logger = LogManager.getLogger();
	@Inject UserRepository userRepository;
	@Inject ApplicationFormRepository applicationFormRepository;
	@Inject ApplicationHandleHistoryRepository applicationHandleHistoryRepository;
	@Inject ApplicationFormService applicationFormService;
	private final String title = "test";
	private final String description = "test content";
	private Pageable pageable = new PageRequest(0, 20);
	private Long id;
	private FlowTestData td = new FlowTestData();
	// 申请人
	private User applicant = td.baz;
	// 审核人
	private User auditor = td.foo;
	
	@Before
	public void setUp() throws Exception {
		id = applicationFormService.application(applicant.getEmail(), title, description);
	}

	@After
	public void tearDown() throws Exception {
		if (id != null) {
			/*
			Page<ApplicationHandleHistory> page = applicationHandleHistoryRepository.findByApplicationFormId(id, pageable);
			for (Iterator<ApplicationHandleHistory> i = page.getContent().iterator(); i.hasNext();) {
				ApplicationHandleHistory h = i.next();
				logger.debug(h);
				applicationHandleHistoryRepository.delete(h.getId());
			}
			applicationFormRepository.delete(id);
			*/
			applicationFormService.delete(id);
		}
	}

	@Test
	public void testFindById() {
		if (id != null) {
			ApplicationForm e = applicationFormService.findById(id);
			assertNotNull(e);
		}
	}
	
	@Test
	public void testFindByNameLike() {
		Page<ApplicationForm> page = applicationFormService.findByNameLike(title.substring(0, title.length() - 1) + '%', pageable);
		assertTrue(page.getTotalElements() > 0);
	}

	@Test
	public void testFindByStatus() {
		Page<ApplicationForm> page = applicationFormService.findByStatus(Status.REQUEST, pageable);
		assertTrue(page.getTotalElements() > 0);
	}

	@Test
	public void testFindApplicationFormByEmail() {
		Page<ApplicationForm> page = applicationFormService.findApplicationFormByEmail(applicant.getEmail(), pageable);
		assertTrue(page.getTotalElements() > 0);
	}
	
	@Test
	public void testFindByNameLikeAndStatus() {
		Page<ApplicationForm> page = applicationFormService.findByNameAndStatus(title, Status.REQUEST, pageable);
		assertTrue(page.getTotalElements() > 0);
		
		page = applicationFormService.findByNameAndStatus(null, Status.REQUEST, pageable);
		assertTrue(page.getTotalElements() > 0);
		
		page = applicationFormService.findByNameAndStatus(title, null, pageable);
		assertTrue(page.getTotalElements() > 0);
		
		page = applicationFormService.findByNameAndStatus(null, null, pageable);
		assertTrue(page.getTotalElements() > 0);
	}

	@Test
	//ApplicationForm#getApplicationHandleHistory()使用懒加载，事务不能在service层关闭，所以在此添加上@Transactional
	@Transactional
	public void testTransit() {
		String cause = "缘由是：……";
		if (id != null) {
			applicationFormService.transit(auditor.getEmail(), id, Status.REJECT, cause);
			ApplicationForm af = applicationFormService.findById(id);
			assertEquals(Status.REJECT, af.getStatus());
			assertEquals(cause, af.getCause());
			ApplicationHandleHistory history = af.getApplicationHandleHistory().iterator().next();
			assertNotNull(history);
			
			Instant now = Instant.now();
			Date start = Date.from(now.minusSeconds(1000));
			Date end = Date.from(now.plusSeconds(100));
			Page<ApplicationHandleHistory> page = applicationFormService.historyFindByCreateDateBetween(start, end, pageable);
			assertTrue(page.getTotalElements() > 0);
			
			page = applicationFormService.historyFindByCreateDateGreaterThanEqual(start, pageable);
			assertTrue(page.getTotalElements() > 0);
			
			page = applicationFormService.historyFindByCreateDateLessThanEqual(end, pageable);
			assertTrue(page.getTotalElements() > 0);
			
			page = applicationFormService.historyFindByHandlerEmailLike(auditor.getEmail(), pageable);
			assertTrue(page.getTotalElements() > 0);
			
			page = applicationFormService.historyFindByStatus(Status.REJECT, pageable);
			assertTrue(page.getTotalElements() > 0);
			
			page = applicationFormService.history(applicant.getEmail(), null, title, Status.REJECT, start, end, pageable);
			assertTrue(page.getTotalElements() > 0);
			page = applicationFormService.history(null, auditor.getEmail(), title, Status.REJECT, start, end, pageable);
			assertTrue(page.getTotalElements() > 0);
			page = applicationFormService.history("", "", title, Status.REJECT, start, end, pageable);
			assertTrue(page.getTotalElements() > 0);
			
			List<ApplicationHandleHistory> ls = applicationFormService.findHistoryByApplicationFormId(id);
			assertFalse(ls.isEmpty());
			
		} else {
			fail("没有持久化");
		}
	}

}
