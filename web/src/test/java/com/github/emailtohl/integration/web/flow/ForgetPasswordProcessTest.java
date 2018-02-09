package com.github.emailtohl.integration.web.flow;

import static com.github.emailtohl.integration.core.Profiles.DB_CONFIG;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.web.WebTestConfig;
import com.github.emailtohl.integration.web.WebTestData;
/**
 * 忘记密码流程的测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_CONFIG, ENV_NO_SERVLET })
public class ForgetPasswordProcessTest {
	@Inject
	WebTestData webTestData;
	@Inject
	IdentityService identityService;
	@Inject
	RuntimeService runtimeService;
	@Inject
	TaskService taskService;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String userId = webTestData.baz.getId().toString();
		identityService.setAuthenticatedUserId(userId);
		Map<String, Object> variables = new HashMap<>();
		variables.put("emailOrCellPhone", "email");
		variables.put("to", webTestData.baz.getEmail());
		variables.put("name", webTestData.baz.getName());
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("forgetPassword", variables);
		assertNotNull(processInstance);
		
		Task task = taskService.createTaskQuery().taskAssignee(userId).singleResult();
		taskService.claim(task.getId(), userId);
		System.out.println(task.getProcessVariables());
		taskService.complete(task.getId());
	}

}
