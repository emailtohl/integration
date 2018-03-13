package com.github.emailtohl.integration.web.service.flow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.apache.logging.log4j.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.config.WebTestData;
import com.github.emailtohl.integration.web.config.WebTestEnvironment;
import com.google.gson.Gson;
/**
 * 测试流程
 * @author HeLei
 */
public class FlowServiceTest extends WebTestEnvironment {
	@Inject
	WebPresetData webPresetData;
	@Inject
	WebTestData webTestData;
	@Inject
	Gson gson;
	@Inject
	RuntimeService runtimeService;
	@Inject
	IdentityService identityService;
	@Inject
	TaskService taskService;
	@Inject
	HistoryService historyService;
	@Inject
	FlowService flowService;
	
	String applyUserId;
	String processInstanceId;
	
	@Before
	public void setUp() throws Exception {
		applyUserId = webPresetData.getEric().getId().toString();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		changeUser(applyUserId);
		FlowData form = new FlowData();
		form.setFlowType(FlowType.WORK);
		form.setContent("提交一个申请，内容是：*****");
		FlowData flowData = flowService.startWorkflow(form);
		assertNotNull(flowData);
		System.out.println(flowData.getActivityId());
		System.out.println(flowData.getProcessInstanceId());
		System.out.println(flowData.getFlowNum());
		processInstanceId = flowData.getProcessInstanceId();
		System.out.println(flowData.getActivityId());
		
		ExecResult execResult = null;
		
		// 初审通过
		changeUser(webTestData.bar.getId().toString());
		List<FlowData> ls = flowService.findTodoTasks();
		assertFalse(ls.isEmpty());
		for (FlowData fd : ls) {
			execResult = flowService.claim(fd.getTaskId());
			assertTrue(execResult.ok);
			fd.setCheckApproved(true);
			fd.setCheckComment("同意");
			execResult = flowService.check(fd);
			assertTrue(execResult.ok);
		}
		
		// 复审不通过
		changeUser(webTestData.foo.getId().toString());
		ls = flowService.findTodoTasks();
		assertFalse(ls.isEmpty());
		for (FlowData fd : ls) {
			execResult = flowService.claim(fd.getTaskId());
			assertTrue(execResult.ok);
			System.out.println(runtimeService.getVariable(fd.getProcessInstanceId(), "content", String.class));
			fd.setCheckApproved(false);
			fd.setCheckComment("不同意，原因是……");
			execResult = flowService.check(fd);
			assertTrue(execResult.ok);
		}
		
		// 调整申请
		changeUser(applyUserId);
		ls = flowService.findTodoTasks();
		assertFalse(ls.isEmpty());
		for (FlowData fd : ls) {
			// 查看流程中的评论
			List<CommentInfo> commentInfos = flowService.getCommentInfo(fd.getProcessInstanceId());
			System.out.println(gson.toJson(commentInfos));
			fd.setReApply(true);
			fd.setContent("调整申请内容为：……");
			execResult = flowService.reApply(fd);
			assertTrue(execResult.ok);
		}

		// 再次初审通过
		changeUser(webTestData.bar.getId().toString());
		ls = flowService.findTodoTasks();
		assertFalse(ls.isEmpty());
		for (FlowData fd : ls) {
			execResult = flowService.claim(fd.getTaskId());
			assertTrue(execResult.ok);
			System.out.println(runtimeService.getVariable(fd.getProcessInstanceId(), "content", String.class));
			fd.setCheckApproved(true);
			fd.setCheckComment("同意");
			execResult = flowService.check(fd);
			assertTrue(execResult.ok);
		}
		
		// 再次复审
		changeUser(webTestData.foo.getId().toString());
		ls = flowService.findTodoTasks();
		assertFalse(ls.isEmpty());
		for (FlowData fd : ls) {
			execResult = flowService.claim(fd.getTaskId());
			assertTrue(execResult.ok);
			fd.setCheckApproved(true);
//			fd.setContent("同意");
			execResult = flowService.check(fd);
			assertTrue(execResult.ok);
		}
		historyService.createHistoricProcessInstanceQuery().finished().list().forEach(h -> {
			System.out.println(h);
		});
		historyService.createHistoricActivityInstanceQuery().finished().list().forEach(h ->  {
			System.out.println(h);
		});
		historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list().forEach(h -> {
			System.out.println(gson.toJson(h));
		});
		Map<String, Object> historyVariables = new HashMap<String, Object>();
		historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).list().forEach(historicDetail -> {
            if (historicDetail instanceof HistoricFormProperty) {
                // 表单中的字段
                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
                System.out.println("form field: taskId=" + field.getTaskId() + ", " + field.getPropertyId() + " = " + field.getPropertyValue());
            } else if (historicDetail instanceof HistoricVariableUpdate) {
                HistoricVariableUpdate variable = (HistoricVariableUpdate) historicDetail;
                historyVariables.put(variable.getVariableName(), variable.getValue());
                System.out.println("variable: " + variable.getVariableName() + " = " + variable.getValue());
            }
		});
		System.out.println(gson.toJson(historyVariables));
        
        
		form = new FlowData();
		form.setApplicantId(Long.valueOf(applyUserId));
		form.setFlowType(FlowType.WORK);
		List<FlowData> flowDatas = flowService.query(form);
		assertFalse(flowDatas.isEmpty());
		System.out.println(gson.toJson(flowDatas));
		Paging<FlowData> page = flowService.query(form, new PageRequest(0, 10));
		assertFalse(page.getContent().isEmpty());
		page.getContent().forEach(c -> {
			FlowData fd = flowService.findByFlowDataId(c.getId());
			assertNotNull(fd);
			fd = flowService.findByProcessInstanceId(c.getProcessInstanceId());
			assertNotNull(fd);
			System.out.println(gson.toJson(fd));
		});
	}
	
	void changeUser(String userId) {
		ThreadContext.put(Constant.USER_ID, userId);
		identityService.setAuthenticatedUserId(userId);
	}
}
