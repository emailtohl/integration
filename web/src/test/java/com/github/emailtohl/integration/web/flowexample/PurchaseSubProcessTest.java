package com.github.emailtohl.integration.web.flowexample;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
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
import com.github.emailtohl.integration.web.config.WebPresetData;

/**
 * 购买办公用品流程测试
 *
 * @author henryyan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class PurchaseSubProcessTest /*extends SpringActivitiTestCase */{
	@Inject
	WebPresetData webPresetData;
	@Inject
	RepositoryService repositoryService;
	@Inject
	RuntimeService runtimeService;
	@Inject
	TaskService taskService;
	@Inject
	HistoryService historyService;
	@Inject
	IdentityService identityService;
	@Inject
	FormService formService;
	
	String processDefinitionKey = "purchase-subprocess";
	String deploymentId;
	
	@Before
	public void setUp() throws Exception {
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
		deploymentBuilder.addClasspathResource("flowexample/purchase-subprocess.bpmn");
		deploymentId = deploymentBuilder.deploy().getId();
	}
	
	@After
	public void tearDown() throws Exception {
		repositoryService.deleteDeployment(deploymentId, true);
	}
	
    /**
     * 全部通过
     */
    @Test
//    @Deployment(resources = {"flowexample/purchase-subprocess.bpmn"})
    public void testAllApproved() throws Exception {
    	String applyUserId = webPresetData.getEric().getId().toString();
    	String kermit = webPresetData.getKermit().getId().toString();
    	String deptLeader = webPresetData.getDeptLeader().getName();
    	String supportCrew = webPresetData.getSupportCrew().getName();
    	String treasurer = webPresetData.getTreasurer().getName();
    	LocalDate today = LocalDate.now();
    	
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("dueDate", today.toString());
        String listing = "1. MacBook Pro一台\n";
        listing += "2. 27寸显示器一台";
        properties.put("listing", listing);
        properties.put("amountMoney", "22000");

        identityService.setAuthenticatedUserId(applyUserId);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), properties);
        assertNotNull(processInstance);

        // 部门领导
        Task task = taskService.createTaskQuery().taskCandidateGroup(deptLeader).singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(task.getId(), properties);

        // 联系供货方
        task = taskService.createTaskQuery().taskCandidateGroup(supportCrew).singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("supplier", "苹果公司");
        properties.put("bankName", "中国工商银行");
        properties.put("bankAccount", "203840240274247293");
        properties.put("planDate", today.plusDays(10).toString());
        formService.submitTaskFormData(task.getId(), properties);

        // 验证是否启动子流程
        Execution subExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("treasurerAudit").singleResult();
        assertNotNull(subExecution);
        assertEquals(listing, runtimeService.getVariable(processInstance.getId(), "usage"));

        // 子流程--财务审批
        task = taskService.createTaskQuery().taskCandidateGroup(treasurer).singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("treasurerApproved", "true");
        formService.submitTaskFormData(task.getId(), properties);

        // 子流程--总经理审批
        task = taskService.createTaskQuery().taskCandidateGroup("generalManager").singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("generalManagerApproved", "true");
        formService.submitTaskFormData(task.getId(), properties);

        // 子流程--出纳付款
        task = taskService.createTaskQuery().taskCandidateGroup("cashier").singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("generalManagerApproved", "true");
        formService.submitTaskFormData(task.getId(), properties);

        // 收货确认
        task = taskService.createTaskQuery().taskAssignee(applyUserId).singleResult();
        assertEquals("收货确认", task.getName());
        taskService.complete(task.getId());

        long count = historyService.createHistoricProcessInstanceQuery().finished().count();
        assertEquals(1, count);
    }

    /**
     * 财务拒绝
     */
    @Test
//    @Deployment(resources = {"flowexample/purchase-subprocess.bpmn"})
    public void testRejectOnTreasurer() throws Exception {
    	String applyUserId = webPresetData.getEric().getId().toString();
    	String kermit = webPresetData.getKermit().getId().toString();
    	String deptLeader = webPresetData.getDeptLeader().getName();
    	String supportCrew = webPresetData.getSupportCrew().getName();
    	String treasurer = webPresetData.getTreasurer().getName();
    	LocalDate today = LocalDate.now();
    	
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("dueDate", today.toString());
        String listing = "1. MacBook Pro一台\n";
        listing += "2. 27寸显示器一台";
        properties.put("listing", listing);
        properties.put("amountMoney", "22000");

        identityService.setAuthenticatedUserId(applyUserId);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), properties);
        assertNotNull(processInstance);

        // 部门领导
        Task task = taskService.createTaskQuery().taskCandidateGroup(deptLeader).singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(task.getId(), properties);

        // 联系供货方
        task = taskService.createTaskQuery().taskCandidateGroup(supportCrew).singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("supplier", "苹果公司");
        properties.put("bankName", "中国工商银行");
        properties.put("bankAccount", "203840240274247293");
        properties.put("planDate", today.plusDays(10).toString());
        formService.submitTaskFormData(task.getId(), properties);

        // 验证是否启动子流程
        Execution subExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("treasurerAudit").singleResult();
        assertNotNull(subExecution);
        assertEquals(listing, runtimeService.getVariable(processInstance.getId(), "usage"));

        // 子流程--财务审批
        task = taskService.createTaskQuery().taskCandidateGroup(treasurer).singleResult();
        taskService.claim(task.getId(), kermit);
        properties = new HashMap<String, String>();
        properties.put("treasurerApproved", "false");
        formService.submitTaskFormData(task.getId(), properties);

        // 任务流转至调整申请
        task = taskService.createTaskQuery().taskAssignee(applyUserId).singleResult();
        assertNotNull(task);
    }

}
