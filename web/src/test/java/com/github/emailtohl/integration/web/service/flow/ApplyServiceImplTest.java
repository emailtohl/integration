package com.github.emailtohl.integration.web.service.flow;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.config.WebTestConfig;
import com.github.emailtohl.integration.web.config.WebTestData;
/**
 * 测试流程
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class ApplyServiceImplTest {
	@Inject
	WebPresetData webPresetData;
	@Inject
	WebTestData webTestData;
	@Inject
	RuntimeService runtimeService;
	@Inject
	IdentityService identityService;
	@Inject
	ApplyServiceImpl applyService;
	
	String applyUserId;
	String processInstanceId;

	@Before
	public void setUp() throws Exception {
		applyUserId = webPresetData.getEric().getId().toString();
	}

	@After
	public void tearDown() throws Exception {
		runtimeService.deleteProcessInstance(processInstanceId, "清理测试数据");
	}

	@Test
	public void testFlow() {
		changeUser(applyUserId);
		Apply apply = new Apply();
		apply.setReason("提交一个申请，内容是：*****");
		apply = applyService.create(apply);
		apply.setReason("调整申请内容为：……");
		apply = applyService.update(apply.getId(), apply);
		processInstanceId = apply.getProcessInstanceId();
		changeUser(webTestData.bar.getId().toString());
		List<Apply> applys = applyService.findTodoTasks();
		assertFalse(applys.isEmpty());
		for (Apply a : applys) {
			ExecResult e = applyService.claim(a.getTaskId());
			assertTrue(e.ok);
			e = applyService.approve(a.getTaskId(), true);
			assertTrue(e.ok);
		}
		applys = applyService.findTodoTasks();
		assertTrue(applys.isEmpty());
		changeUser(webTestData.foo.getId().toString());
		applys = applyService.findTodoTasks();
		assertFalse(applys.isEmpty());
		for (Apply a : applys) {
			ExecResult e = applyService.claim(a.getTaskId());
			assertTrue(e.ok);
			e = applyService.approve(a.getTaskId(), true);
			assertTrue(e.ok);
		}
	}

	void changeUser(String userId) {
		identityService.setAuthenticatedUserId(userId);
		ApplyServiceImpl.CURRENT_USER_ID.set(Long.valueOf(userId));
	}
}
