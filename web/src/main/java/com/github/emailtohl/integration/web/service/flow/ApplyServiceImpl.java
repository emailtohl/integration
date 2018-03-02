package com.github.emailtohl.integration.web.service.flow;

import java.util.ArrayList;
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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.config.WebPresetData;

/**
 * 流程服务
 * @author HeLei
 */
@Service
@Transactional
public class ApplyServiceImpl extends StandardService<Apply> {
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
	ApplyRepository applyRepository;
	
	@Override
	public Apply create(Apply entity) {
		validate(entity);
		UserRef applicant = getCurrentUserRef();
		entity.setApplicant(applicant);
		entity.setResult(null);
		Apply source = applyRepository.save(entity);
		// 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
		// 实际上过滤器处已经在上下文上设置了用户id，这里只是为了表达得更清晰
		identityService.setAuthenticatedUserId(applicant.getId().toString());
		String businessKey = source.getId().toString();
		Map<String, Object> args = new HashMap<>();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("apply", businessKey, args);
		source.setProcessInstanceId(processInstance.getId());
		source.setActivityId(processInstance.getActivityId());
		return transientDetail(source);
	}

	private ExampleMatcher idMatcher = ExampleMatcher.matching().withMatcher("id",
			GenericPropertyMatchers.exact());
	
	@Override
	public boolean exist(Object id) {
		if (!(id instanceof Long)) {
			return false;
		}
		Apply a = new Apply();
		a.setId((Long) id);
		Example<Apply> example = Example.of(a, idMatcher);
		return applyRepository.exists(example);
	}

	@Override
	public Apply get(Long id) {
		return transientDetail(applyRepository.findOne(id));
	}

	private ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("reason",
			GenericPropertyMatchers.ignoreCase())
			.withMatcher("applicant.name", GenericPropertyMatchers.ignoreCase());
	
	@Override
	public Paging<Apply> query(Apply params, Pageable pageable) {
		Example<Apply> example = Example.of(params, matcher);
		Page<Apply> p = applyRepository.findAll(example, pageable);
		List<Apply> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<Apply>(ls, pageable, p.getTotalElements());
	}

	@Override
	public List<Apply> query(Apply params) {
		Example<Apply> example = Example.of(params, matcher);
		return applyRepository.findAll(example).stream().map(this::toTransient).collect(Collectors.toList());
	}

	/**
	 * 修改申请内容
	 */
	@Override
	public Apply update(Long id, Apply newEntity) {
		validate(newEntity);
		Apply source = applyRepository.findOne(id);
		if (source == null) {
			return null;
		}
		if (!source.getApplicant().getId().equals(CURRENT_USER_ID.get())) {
			throw new AuthorizationServiceException("当前用户不是流程申请人");
		}
		String processInstanceId = source.getProcessInstanceId();
		if (!"praeiudicium".equals(getActivityId(processInstanceId))) {
			return transientDetail(source);
		}
		if (hasText(newEntity.getReason())) {
			source.setReason(newEntity.getReason());
		}
		return transientDetail(source);
	}

	@Override
	public void delete(Long id) {
		Apply source = applyRepository.findOne(id);
		if (source == null) {
			return;
		}
		if (!source.getApplicant().getId().equals(CURRENT_USER_ID.get())) {
			throw new AuthorizationServiceException("当前用户不是流程申请人");
		}
		String processInstanceId = source.getProcessInstanceId();
		if (!"praeiudicium".equals(getActivityId(processInstanceId))) {
			return;
		}
		applyRepository.delete(source);
		runtimeService.deleteProcessInstance(processInstanceId, "删除原因");
	}
	
	@Override
	protected Apply toTransient(Apply entity) {
		if (entity == null) {
			return null;
		}
		Apply target = new Apply();
		target.setId(entity.getId());
		target.setCreateDate(entity.getCreateDate());
		target.setModifyDate(entity.getModifyDate());
		target.setReason(entity.getReason());
		target.setApplicant(transientUserRef(entity.getApplicant()));
		target.setProcessInstanceId(entity.getProcessInstanceId());
		target.setTaskId(entity.getTaskId());
		return target;
	}

	@Override
	protected Apply transientDetail(Apply entity) {
		return toTransient(entity);
	}
	
	protected UserRef transientUserRef(UserRef source) {
		if (source == null) {
			return null;
		}
		UserRef target = new UserRef();
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setCellPhone(source.getCellPhone());
		target.setName(source.getName());
		target.setNickname(source.getNickname());
		target.setIconSrc(source.getIconSrc());
		return target;
	}

	/**
	 * 查询当前用户的任务
	 * @return
	 */
	public List<Apply> findTodoTasks() {
		String userId = CURRENT_USER_ID.get().toString();
		List<Apply> results = new ArrayList<>();
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
			Apply apply = applyRepository.findOne(Long.valueOf(businessKey));
			apply.setTaskId(task.getId());
			apply.setActivityId(processInstance.getActivityId());
			results.add(apply);
		}
		return results;
	}

	/**
	 * 签收任务
	 * @param taskId
	 */
	public ExecResult claim(String taskId) {
		if (CURRENT_USER_ID.get().equals(corePresetData.user_anonymous.getId())) {
			throw new UsernameNotFoundException("没有此账号");
		}
		String userId = CURRENT_USER_ID.get().toString();
		try {
			taskService.claim(taskId, userId);
			return new ExecResult(true, "", null);
		} catch (ActivitiTaskAlreadyClaimedException e) {
			return new ExecResult(false, "Activiti task already claimed exception", null);
		}
	}
	
	public ExecResult approve(String taskId, boolean approved) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return new ExecResult(false, "not found task", null);
		}
		Map<String, Object> args = new HashMap<>();
		if ("praeiudicium".equals(task.getTaskDefinitionKey())) {
			args.put("praeiudiciumApproved", approved);
		} else if ("recheck".equals(task.getTaskDefinitionKey())) {
			args.put("recheckApproved", approved);
		} else {
			return new ExecResult(false, "state is wrong", null);
		}
		taskService.complete(taskId, args);
		return new ExecResult(true, "", null);
	}
	
	public String getActivityId(String processInstanceId) {
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		if (processInstance == null) {
			return null;
		}
		return processInstance.getActivityId();
	}
	
	/**
	 * 若未查找到登录用户则抛异常
	 * @return
	 * @throws UsernameNotFoundException
	 */
	public UserRef getCurrentUserRef() throws UsernameNotFoundException {
		Long currentId = CURRENT_USER_ID.get();
		if (currentId == null || currentId.equals(corePresetData.user_anonymous.getId())) {
			throw new UsernameNotFoundException("没有登录");
		}
		UserRef userRef = userService.getRef(currentId);
		if (userRef == null) {
			throw new UsernameNotFoundException("没有此账号");
		}
		return userRef;
	}
	
}
