package com.github.emailtohl.integration.web.service.flow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.config.WebPresetData;

/**
 * 流程服务
 * @author HeLei
 */
@Transactional
@PreAuthorize("isAuthenticated()")
public class FlowService {
	protected static final Logger logger = LogManager.getLogger();
	public final static String PROCESS_DEFINITION_KEY = "apply";
	@Inject
	CorePresetData corePresetData;
	@Inject
	WebPresetData webPresetData;
	@Inject
	UserService userService;
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
	
	@Inject
	FlowRepository flowRepository;
	
	 /**
     * 保存请假实体并启动流程
     */
    public ProcessInstance startWorkflow(FlowData entity, String userId) {
    	flowRepository.save(entity);
        String businessKey = entity.getId().toString();

        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        identityService.setAuthenticatedUserId(userId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, businessKey);
        String processInstanceId = processInstance.getId();
        entity.setProcessInstanceId(processInstanceId);
        logger.debug("start process of {key={}, bkey={}, pid={}}", new Object[]{PROCESS_DEFINITION_KEY, businessKey, processInstanceId});
        return processInstance;
    }
    
	/**
	 * 查询当前用户的任务
	 * @return
	 */
	public List<FlowData> findTodoTasks() {
		String userId = getCurrentUserId();
		List<FlowData> results = new ArrayList<>();
		List<Task> tasks = new ArrayList<>();
		// 根据当前人的ID查询
		List<Task> todoList = taskService.createTaskQuery().processDefinitionKey(PROCESS_DEFINITION_KEY)
				.taskAssignee(userId).list();
		// 根据当前人未签收的任务
		List<Task> unsignedTasks = taskService.createTaskQuery().processDefinitionKey(PROCESS_DEFINITION_KEY)
				.taskCandidateUser(userId).list();
		// 合并
		tasks.addAll(todoList);
		tasks.addAll(unsignedTasks);
		// 根据流程的业务ID查询实体并关联
		for (Task task : tasks) {
			String processInstanceId = task.getProcessInstanceId();
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
					.processInstanceId(processInstanceId).singleResult();
			String businessKey = processInstance.getBusinessKey();
			FlowData flowData = flowRepository.findOne(Long.valueOf(businessKey));
			flowData.setTaskId(task.getId());
			flowData.setActivityId(processInstance.getActivityId());
			results.add(flowData);
		}
		return results;
	}

	/**
	 * 签收任务
	 * @param taskId
	 */
	public ExecResult claim(String taskId) {
		String userId = getCurrentUserId();
		try {
			taskService.claim(taskId, userId);
			return new ExecResult(true, "", null);
		} catch (ActivitiTaskAlreadyClaimedException e) {
			return new ExecResult(false, "Activiti task already claimed exception", null);
		}
	}
	
	/**
	 * 审核任务
	 * @param entity
	 * @return
	 */
	public ExecResult check(FlowData entity) {
		FlowData source = getFlowData(entity);
		if (source == null) {
			return new ExecResult(false, "未找到此流程单", null);
		}
		String taskId = entity.getTaskId();
		if (!StringUtils.hasText(taskId)) {
			return new ExecResult(false, "提交数据没有任务id", null);
		}
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return new ExecResult(false, "没有这个任务", null);
		}
		String currentUserId = getCurrentUserId();
		String assignee = task.getAssignee();
		if (!currentUserId.equals(assignee)) {
			return new ExecResult(false, "当前用户是： " + currentUserId + " 任务所有人是： " + assignee, null);
		}
		Map<String, Object> variables = new HashMap<>();
		Boolean approved = null;
		switch (entity.getActivityId()) {
		case "check":
			approved = entity.getCheckApproved();
			if (approved == null) {
				return new ExecResult(false, "初审没有审核结果", null);
			}
			source.setCheckApproved(approved);
			variables.put("checkApproved", approved);
			source.setCheckerId(Long.valueOf(currentUserId));
			variables.put("checkerId", source.getCheckerId());
			source.setCheckTime(new Date());
			variables.put("checkTime", source.getCheckTime());
			source.setCheckOpinions(entity.getCheckOpinions());
			variables.put("checkOpinions", entity.getCheckOpinions());
			taskService.complete(taskId, variables);
			taskService.complete(taskId, variables);
			break;
		case "recheck":
			approved = entity.getRecheckApproved();
			if (approved == null) {
				return new ExecResult(false, "复审没有审核结果", null);
			}
			source.setRecheckApproved(approved);
			variables.put("recheckApproved", approved);
			source.setRecheckerId(Long.valueOf(currentUserId));
			variables.put("recheckerId", source.getRecheckerId());
			source.setRecheckTime(new Date());
			variables.put("recheckTime", source.getRecheckTime());
			source.setRecheckOpinions(entity.getRecheckOpinions());
			variables.put("recheckOpinions", entity.getRecheckOpinions());
			break;
		default:
			return new ExecResult(false, "处于未知审核任务下", null);
		}
		taskService.complete(taskId, variables);
		return new ExecResult(true, "", null);
	}
	
	private ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnoreNullValues()
			.withMatcher("processInstanceId", GenericPropertyMatchers.exact())
			.withMatcher("flowType", GenericPropertyMatchers.exact())
			.withMatcher("content", GenericPropertyMatchers.contains())
			.withMatcher("applicantId", GenericPropertyMatchers.exact())
			.withMatcher("applicantName", GenericPropertyMatchers.exact())
			.withMatcher("checkerId", GenericPropertyMatchers.exact())
			.withMatcher("checkerNum", GenericPropertyMatchers.exact())
			.withMatcher("checkerName", GenericPropertyMatchers.exact())
			.withMatcher("checkApproved", GenericPropertyMatchers.exact())
			.withMatcher("checkOpinions", GenericPropertyMatchers.contains())
			.withMatcher("recheckerId", GenericPropertyMatchers.exact())
			.withMatcher("recheckerNum", GenericPropertyMatchers.exact())
			.withMatcher("recheckApproved", GenericPropertyMatchers.exact())
			.withMatcher("recheckOpinions", GenericPropertyMatchers.contains())
			.withMatcher("reApply", GenericPropertyMatchers.exact())
			.withMatcher("pass", GenericPropertyMatchers.exact());
	
	/**
	 * 查询流程数据
	 * @param params
	 * @param pageable
	 * @return
	 */
	public Paging<FlowData> query(FlowData params, Pageable pageable) {
		Example<FlowData> example = Example.of(params, matcher);
		Page<FlowData> p = flowRepository.findAll(example, pageable);
		List<FlowData> ls = p.getContent().stream().collect(Collectors.toList());
		return new Paging<FlowData>(ls, pageable, p.getTotalElements());
	}
	
	public List<FlowData> query(FlowData params) {
		Example<FlowData> example = Example.of(params, matcher);
		return flowRepository.findAll(example).stream().collect(Collectors.toList());
	}
    
	/**
	 * 在上下文中获取当前用户id
	 * @return
	 * @throws UsernameNotFoundException 若未查找到将会抛出异常
	 */
	public String getCurrentUserId() throws UsernameNotFoundException {
		String currentId = ThreadContext.get(Constant.USER_ID);
		if (currentId == null) {
			throw new UsernameNotFoundException("未查找到当前用户");
		}
		if (Long.valueOf(currentId).equals(corePresetData.user_anonymous.getId())) {
			throw new UsernameNotFoundException("没有登录");
		}
		return currentId;
	}
	
	/**
	 * 若未查找到登录用户则抛异常
	 * @return
	 * @throws UsernameNotFoundException
	 */
	public UserRef getCurrentUserRef() throws UsernameNotFoundException {
		UserRef userRef = userService.getRef(Long.valueOf(getCurrentUserId()));
		if (userRef == null) {
			throw new UsernameNotFoundException("没有此账号");
		}
		return userRef;
	}
	
	/**
	 * 返回持久化状态的FlowData
	 * @param entity
	 * @return
	 */
	protected FlowData getFlowData(FlowData entity) {
		Long id = entity.getId();
		if (id != null) {
			return flowRepository.findOne(id);
		}
		String processInstanceId = entity.getProcessInstanceId();
		if (StringUtils.hasText(processInstanceId)) {
			return flowRepository.findByProcessInstanceId(processInstanceId);
		}
		return null;
	}
}
