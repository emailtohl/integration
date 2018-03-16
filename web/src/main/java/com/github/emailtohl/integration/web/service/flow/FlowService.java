package com.github.emailtohl.integration.web.service.flow;

import java.time.LocalDate;
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
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.config.WebPresetData;

/**
 * 流程服务
 * 
 * @author HeLei
 */
@PreAuthorize("isAuthenticated()")
@Transactional
@Service
public class FlowService {
	protected static final Logger logger = LogManager.getLogger();
	public final static String PROCESS_DEFINITION_KEY = "apply";
	@Inject
	CorePresetData corePresetData;
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

	@Inject
	FlowRepository flowRepository;

	/**
	 * 保存请假实体并启动流程
	 * 表单需要参数：
	 * flowType：流程类型
	 * content： 申请内容
	 * 已登录，可以获取到用户id
	 * @return 启动的流程实例
	 */
	public FlowData startWorkflow(FlowData form) {
		String userId = getCurrentUserId();
		form.setApplicantId(Long.valueOf(userId));
		FlowType flowType = form.getFlowType();
		String content = form.getContent();
		if (flowType == null || !StringUtils.hasText(content)) {
			return null;
		}
		FlowData source = flowRepository.save(form);
		String businessKey = source.getId().toString();
		// 计算表单号
		LocalDate d = LocalDate.now();
		int year = d.getYear(), month = d.getMonthValue(), day = d.getDayOfMonth();
		StringBuilder flowNum = new StringBuilder();
		flowNum.append(flowType.name().toUpperCase()).append(year);
		if (month < 10) {
			flowNum.append(0).append(month);
		} else {
			flowNum.append(month);
		}
		if (day < 10) {
			flowNum.append(0).append(day);
		} else {
			flowNum.append(day);
		}
		flowNum.append(businessKey);
		source.setFlowNum(flowNum.toString());
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("flowType", flowType);
		variables.put("content", content);
		// 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
		identityService.setAuthenticatedUserId(userId);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, businessKey, variables);
		String processInstanceId = processInstance.getId();
		source.setProcessInstanceId(processInstanceId);
		source.setActivityId(processInstance.getActivityId());
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
		if (task != null) {
			source.setTaskId(task.getId());
			source.setTaskName(task.getName());
		}
		logger.debug("start process of {key={}, bkey={}, pid={}}",
				new Object[] { PROCESS_DEFINITION_KEY, businessKey, processInstanceId });
		return transientDetail(source);
	}

	/**
	 * 查询当前用户的任务
	 * 需要参数：
	 * 已登录，可以获取到用户id
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
			flowData.setTaskName(task.getName());
			flowData.setTaskAssignee(task.getAssignee());
			flowData.setActivityId(processInstance.getActivityId());
			results.add(flowData);
		}
		return results;
	}

	/**
	 * 签收任务
	 * 表单需要参数：
	 * taskId：任务id
	 * 已登录，可以获取到用户id
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
	 * 表单需要参数：
	 * id：流程单的id或者是流程实例id（processInstanceId）
	 * taskId： 任务id
	 * 已登录，可以获取到用户id
	 * assignee：任务签收人的id
	 * checkApproved：审核是否通过
	 * checkComment： 审核意见可选
	 * @return 执行是否成功
	 */
	public ExecResult check(FlowData form) {
		FlowData source = getFlowData(form);
		if (source == null) {
			return new ExecResult(false, "未找到此流程单", null);
		}
		String taskId = form.getTaskId();
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
		User user = identityService.createUserQuery().userId(currentUserId).singleResult();
		if (user == null) {
			return new ExecResult(false, currentUserId + " 用户已不再系统中", null);
		}
		Map<String, Object> variables = new HashMap<>();
		Boolean approved = form.getCheckApproved();
		if (approved == null) {
			return new ExecResult(false, "没有审核结果", null);
		}
		Date currentTime = new Date();
		switch (form.getActivityId()) {
		case "check":
			// 与流程相关的
			variables.put("checkApproved", approved);
			
			// 其他附带信息
			variables.put("checkerId", currentUserId);
			variables.put("checkerName", user.getFirstName());
			variables.put("checkComment", form.getCheckComment());
			variables.put("checkTime", currentTime);
			break;
		case "recheck":
			// 与流程相关的
			variables.put("recheckApproved", approved);
			
			// 其他附带信息
			variables.put("recheckerId", currentUserId);
			variables.put("recheckerName", user.getFirstName());
			variables.put("recheckComment", form.getCheckComment());
			variables.put("recheckTime", currentTime);
			if (approved) {
				source.setPass(approved);
				variables.put("pass", true);
			}
			break;
		default:
			return new ExecResult(false, "未处于审核任务下", null);
		}
		
		if (StringUtils.hasText(form.getCheckComment())) {
			// 将审批的评论添加进记录中
			taskService.addComment(taskId, task.getProcessInstanceId(), form.getCheckComment());
		}
		taskService.complete(taskId, variables);
		
		// 将产生的过程数据保存在业务关系中
		Check check = new Check();
		check.setActivityId(task.getTaskDefinitionKey());
		check.setTaskName(task.getName());
		check.setCheckApproved(approved);
		check.setCheckerId(Long.valueOf(currentUserId));
		check.setCheckerName(user.getFirstName());
		check.setCheckComment(form.getCheckComment());
		check.setCheckTime(currentTime);
		source.getChecks().add(check);
		
		return new ExecResult(true, "", null);
	}

	/**
	 * 重新申请
	 * 表单modifyApply必填：若为true则重新申请，若为false则结束流程
	 * content：若重新申请，则content需填写
	 * @return 执行是否成功
	 */
	public ExecResult reApply(FlowData form) {
		FlowData source = getFlowData(form);
		if (source == null) {
			return new ExecResult(false, "未查找到流程数据", null);
		}
		String userId = getCurrentUserId();
		if (!source.getApplicantId().equals(Long.valueOf(userId))) {
			return new ExecResult(false, "不是任务提交人", null);
		}
		String taskId = form.getTaskId();
		if (!StringUtils.hasText(taskId)) {
			return new ExecResult(false, "没有提交任务id", null);
		}
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(source.getProcessInstanceId()).singleResult();
		if (processInstance == null || !"modifyApply".equals(processInstance.getActivityId())) {
			return new ExecResult(false, "modifyApply才能修改申请内容", null);
		}
		Boolean reApply = form.getReApply();
		if (reApply == null) {
			return new ExecResult(false, "没有reApply字段", null);
		}
		Map<String, Object> variables = new HashMap<>();
		if (reApply) {
			if (!StringUtils.hasText(form.getContent())) {
				return new ExecResult(false, "更新内容不能为空", null);
			}
			source.setContent(form.getContent());
			variables.put("content", form.getContent());
			source.setReApply(reApply);
			variables.put("reApply", reApply);
		} else {
			source.setReApply(reApply);
			variables.put("reApply", reApply);
			variables.put("pass", false);
		}
		taskService.complete(taskId, variables);
		return new ExecResult(true, "", null);
	}

	/**
	 * 查询审批时的批注信息
	 * @param processInstanceId
	 * @return
	 */
	public List<CommentInfo> getCommentInfo(String processInstanceId) {
		// 查找任务名称
		final Map<String, String> taskNames = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(processInstanceId).list().stream()
				.collect(Collectors.toMap(historicTaskInstance -> historicTaskInstance.getId(),
						historicTaskInstance -> historicTaskInstance.getName()));
		return taskService.getProcessInstanceComments(processInstanceId).stream().map(comment -> {
			CommentInfo info = new CommentInfo();
			BeanUtils.copyProperties(comment, info);
			info.setTaskName(taskNames.get(comment.getTaskId()));
			User user = identityService.createUserQuery().userId(comment.getUserId()).singleResult();
			if (user != null) {
				info.setUsername(user.getFirstName());
			}
			return info;
		}).collect(Collectors.toList());
	}
	
	private ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues()
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
	 * 
	 * @param params
	 * @param pageable
	 * @return
	 */
	public Paging<FlowData> query(FlowData params, Pageable pageable) {
		Page<FlowData> p;
		if (params == null) {
			Example<FlowData> example = Example.of(params, matcher);
			p = flowRepository.findAll(example, pageable);
		} else {
			p = flowRepository.findAll(pageable);
		}
		List<FlowData> flowDatas = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		setTaskInfo(flowDatas);
		return new Paging<FlowData>(flowDatas, pageable, p.getTotalElements());
	}

	public List<FlowData> query(FlowData params) {
		List<FlowData> flowDatas;
		if (params == null) {
			flowDatas = flowRepository.findAll().stream().map(this::toTransient).collect(Collectors.toList());
		} else {
			Example<FlowData> example = Example.of(params, matcher);
			flowDatas = flowRepository.findAll(example).stream().map(this::toTransient).collect(Collectors.toList());
		}
		setTaskInfo(flowDatas);
		return flowDatas;
	}
	
	/**
	 * 通过流程数据id查询流程数据
	 * @param id
	 * @return
	 */
	public FlowData findByFlowDataId(Long id) {
		return transientDetail(flowRepository.findOne(id));
	}
	
	/**
	 * 通过流程实例id查询流程数据
	 * @param processInstanceId
	 * @return
	 */
	public FlowData findByProcessInstanceId(String processInstanceId) {
		return transientDetail(flowRepository.findByProcessInstanceId(processInstanceId));
	}
	
	/**
	 * 通过流程单号查询流程数据
	 * @param processInstanceId
	 * @return
	 */
	public FlowData findByFlowNum(String flowNum) {
		return transientDetail(flowRepository.findByFlowNum(flowNum));
	}

	/**
	 * 在上下文中获取当前用户id
	 * 
	 * @return 获取当前用户id
	 * @throws UsernameNotFoundException
	 *             若未查找到将会抛出异常
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
	 * 返回持久化状态的FlowData
	 * 
	 * @param entity
	 * @return 未查找到，则为null
	 */
	protected FlowData getFlowData(FlowData entity) {
		FlowData flowData = null;
		if (entity == null || entity.getId() == null) {
			return null;
		}
		flowData = flowRepository.findOne(entity.getId());
		if (flowData != null) {
			return flowData;
		}
		String processInstanceId = entity.getProcessInstanceId();
		if (!StringUtils.hasText(processInstanceId)) {
			return null;
		}
		flowData = flowRepository.findByProcessInstanceId(processInstanceId);
		return flowData;
	}
	
	/**
	 * 用于列表
	 * @param source
	 * @return
	 */
	protected FlowData toTransient(FlowData source) {
		if (source == null) {
			return null;
		}
		FlowData target = new FlowData();
		BeanUtils.copyProperties(source, target, "checks");
		return target;
	}
	
	/**
	 * 用于详情
	 * @param source
	 * @return
	 */
	protected FlowData transientDetail(FlowData source) {
		FlowData target = toTransient(source);
		if (target == null) {
			return null;
		}
		List<Check> checks = source.getChecks().stream().map(src -> {
			Check tar = new Check();
			tar.setActivityId(src.getActivityId());
			tar.setTaskName(src.getTaskName());
			tar.setCheckApproved(src.getCheckApproved());
			tar.setCheckComment(src.getCheckComment());
			tar.setCheckerId(src.getCheckerId());
			tar.setCheckerName(src.getCheckerName());
			tar.setCheckTime(src.getCheckTime());
			return tar;
		}).collect(Collectors.toList());
		target.getChecks().addAll(checks);
		if (StringUtils.hasText(source.getProcessInstanceId())) {
			Task task = taskService.createTaskQuery().processInstanceId(source.getProcessInstanceId()).singleResult();
			if (task != null) {
				target.setTaskId(task.getId());
				target.setTaskAssignee(task.getAssignee());
				target.setTaskName(task.getName());
				target.setActivityId(task.getTaskDefinitionKey());
			}
		}
		if (source.getApplicantId() != null) {
			User u = identityService.createUserQuery().userId(source.getApplicantId().toString()).singleResult();
			if (u != null) {
				target.setApplicantName(u.getFirstName());
			}
		}
		if (StringUtils.hasText(source.getTaskAssignee())) {
			User u = identityService.createUserQuery().userId(source.getTaskAssignee()).singleResult();
			if (u != null) {
				target.setTaskAssignee(u.getFirstName());
			}
		}
		return target;
	}
	
	/**
	 * 在FlowData列表中设置任务信息，包括任务名、任务id等
	 * @param flowDatas
	 */
	private void setTaskInfo(List<FlowData> flowDatas) {
		// 在查询过程中同时记录下所涉及的流程id，以便于查询当前活动名
		List<String> processInstanceIds = flowDatas.stream()
				.filter(flowData -> StringUtils.hasText(flowData.getProcessInstanceId()))
				.map(flowData -> flowData.getProcessInstanceId()).collect(Collectors.toList());
		if (!processInstanceIds.isEmpty()) {
			// 根据活动id集合查询对应的当前任务映射
			final Map<String, Task> processInstanceIdTotask = taskService.createTaskQuery()
					.processInstanceIdIn(processInstanceIds).list().stream()
					.collect(Collectors.toMap(task -> task.getProcessInstanceId(), task -> task));
			// 最后将活动名添加进结果中
			flowDatas.stream().filter(flowData -> StringUtils.hasText(flowData.getProcessInstanceId()))
					.forEach(flowData -> {
						Task task = processInstanceIdTotask.get(flowData.getProcessInstanceId());
						if (task != null) {
							flowData.setTaskId(task.getId());
							flowData.setActivityId(task.getTaskDefinitionKey());
							flowData.setTaskName(task.getName());
						}
					});
		}
	}
}
