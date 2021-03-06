package com.github.emailtohl.integration.web.flowexample;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.config.WebTestEnvironment;

/**
 * 动态表单请假流程单元测试
 * @author henryyan
 */
public class LeaveDynamicFormTest extends WebTestEnvironment {
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
	
	String currentUserId;
	String kermit;
	String deptLeader;
	String supportCrew;
	String treasurer;
	String generalManager;
	String cashier;
	String hr;
	
	String processDefinitionKey = "leave";
	String deploymentId;
	
	@Before
	public void setUp() throws Exception {
		currentUserId = webPresetData.getEric().getId().toString();
		kermit = webPresetData.getKermit().getId().toString();
		deptLeader = webPresetData.getDeptLeader().getName();
		supportCrew = webPresetData.getSupportCrew().getName();
		treasurer = webPresetData.getTreasurer().getName();
		generalManager = webPresetData.getGeneralManager().getName();
		cashier = webPresetData.getCashier().getName();
		hr = webPresetData.getHr().getName();
		
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
		deploymentBuilder.addClasspathResource("flowexample/leave.bpmn");
		deploymentId = deploymentBuilder.deploy().getId();
	}
	
	@After
	public void tearDown() throws Exception {
		repositoryService.deleteDeployment(deploymentId, true);
	}
	
    @Test
//    @Deployment(resources = "flowexample/leave.bpmn")
    public void testJavascriptFormType() throws Exception {

        // 验证是否部署成功
        long count = repositoryService.createProcessDefinitionQuery().count();
        assertTrue(count > 0);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();
        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
        List<FormProperty> formProperties = startFormData.getFormProperties();
        for (FormProperty formProperty : formProperties) {
            System.out.println(formProperty.getId() + "，value=" + formProperty.getValue());
        }
    }

    /**
     * 部门领导和人事全部审批通过
     */
    @Test
//    @Deployment(resources = "flowexample/leave.bpmn")
    public void allApproved() throws Exception {

        // 验证是否部署成功
        long count = repositoryService.createProcessDefinitionQuery().count();
        assertTrue(count > 0);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();

        identityService.setAuthenticatedUserId(currentUserId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> variables = new HashMap<String, String>();
        Calendar ca = Calendar.getInstance();
        String startDate = sdf.format(ca.getTime());
        ca.add(Calendar.DAY_OF_MONTH, 2); // 当前日期加2天
        String endDate = sdf.format(ca.getTime());

        // 启动流程
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        // 部门领导审批通过
        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup(deptLeader).singleResult();
        variables = new HashMap<String, String>();
        variables.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 人事审批通过
        Task hrTask = taskService.createTaskQuery().taskCandidateGroup(hr).singleResult();
        variables = new HashMap<String, String>();
        variables.put("hrApproved", "true");
        formService.submitTaskFormData(hrTask.getId(), variables);

        // 销假（根据申请人的用户ID读取）
        Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
        variables = new HashMap<String, String>();
        variables.put("reportBackDate", sdf.format(ca.getTime()));
        formService.submitTaskFormData(reportBackTask.getId(), variables);

        // 验证流程是否已经结束
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().finished().list();
        assertFalse(historicProcessInstances.isEmpty());

        // 读取历史变量
        Map<String, Object> historyVariables = packageVariables(processInstance);

        // 验证执行结果
//        assertEquals("ok", historyVariables.get("result"));
        System.out.println(historyVariables.get("result"));
    }

    /**
     * 领导驳回后申请人取消申请
     */
    @Test
//    @Deployment(resources = "flowexample/leave.bpmn")
    public void cancelApply() throws Exception {

        identityService.setAuthenticatedUserId(currentUserId);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> variables = new HashMap<String, String>();
        Calendar ca = Calendar.getInstance();
        String startDate = sdf.format(ca.getTime());
        ca.add(Calendar.DAY_OF_MONTH, 2);
        String endDate = sdf.format(ca.getTime());

        // 启动流程
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        // 部门领导审批通过
        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup(deptLeader).singleResult();
        variables = new HashMap<String, String>();
        variables.put("deptLeaderApproved", "false");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 调整申请
        Task modifyApply = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
        variables = new HashMap<String, String>();
        variables.put("reApply", "false");
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");
        formService.submitTaskFormData(modifyApply.getId(), variables);

        // 读取历史变量
        Map<String, Object> historyVariables = packageVariables(processInstance);

        // 验证执行结果
//        assertEquals("canceled", historyVariables.get("result"));
        System.out.println(historyVariables.get("result"));
    }

    /**
     * 读取历史变量并封装到Map中
     */
    private Map<String, Object> packageVariables(ProcessInstance processInstance) {
        Map<String, Object> historyVariables = new HashMap<String, Object>();
        List<HistoricDetail> list = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).list();
        for (HistoricDetail historicDetail : list) {
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
        }
        return historyVariables;
    }

}
