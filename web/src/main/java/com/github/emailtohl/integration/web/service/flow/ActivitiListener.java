package com.github.emailtohl.integration.web.service.flow;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
/**
 * 监听Activiti的事件 作为Activiti初始化所需要的参数，不能依赖identityService
 * @author HeLei
 */
public class ActivitiListener implements ExecutionListener, TaskListener, Serializable {
	private static final long serialVersionUID = -6779971905269762380L;
	private static final Logger logger = LogManager.getLogger();
	final UserIdMapper userIdMapper;
	final ApplicationEventPublisher publisher;

	public ActivitiListener(UserIdMapper userIdMapper, ApplicationEventPublisher publisher) {
		super();
		this.userIdMapper = userIdMapper;
		this.publisher = publisher;
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		logger.debug(execution.getClass().getSimpleName() + ", " + execution.getEventName());
		FlowNotifyEvent event = new FlowNotifyEvent(getClass().getName(), execution.getId(),
				FlowNotifyEvent.Type.Execution, execution.getEventName(), execution.getProcessInstanceId());
		event.setProcessBusinessKey(execution.getProcessBusinessKey());
		String applyUserId = execution.getVariable("applyUserId", String.class);
		if (StringUtils.hasText(applyUserId)) {
			event.getToUserIds().add(applyUserId);
		}
		publisher.publishEvent(event);
	}

	@Override
	public void notify(DelegateTask delegateTask) {
		logger.debug(delegateTask.getClass().getSimpleName() + ", " + delegateTask.getEventName());
		FlowNotifyEvent event = new FlowNotifyEvent(getClass().getName(), delegateTask.getId(),
				FlowNotifyEvent.Type.Task, delegateTask.getEventName(), delegateTask.getProcessInstanceId());
		event.setActivityId(delegateTask.getTaskDefinitionKey());
		event.setActivityName(delegateTask.getName());
		switch (delegateTask.getEventName()) {
		case "create":// 创建时，通知候选人
			final Set<String> userIds = delegateTask.getCandidates().stream()
					.filter(identityLink -> StringUtils.hasText(identityLink.getUserId()))
					.map(identityLink -> identityLink.getUserId()).collect(Collectors.toSet());
			List<String> groupIds = delegateTask.getCandidates().stream()
					.filter(identityLink -> StringUtils.hasText(identityLink.getGroupId()))
					.map(identityLink -> identityLink.getGroupId()).collect(Collectors.toList());
			if (!groupIds.isEmpty()) {
				userIds.addAll(userIdMapper.findUserIdInGroupId(groupIds));
			}
			event.getToUserIds().addAll(userIds);
			event.setContent("你有“" + delegateTask.getName() + "”任务可以签收");
			break;
		case "assignment":// 分配后，告知申请人
			String applyUserId = delegateTask.getVariable("applyUserId", String.class);
			if (StringUtils.hasText(applyUserId)) {
				event.getToUserIds().add(applyUserId);
			}
			event.setContent("你申请的任务被“" + delegateTask.getAssignee() + "”签收");
			break;
		case "complete":// 完成后，告知申请人
			String _applyUserId = delegateTask.getVariable("applyUserId", String.class);
			if (StringUtils.hasText(_applyUserId)) {
				event.getToUserIds().add(_applyUserId);
			}
			event.setContent("你申请的任务被“" + delegateTask.getAssignee() + "”完成");
			break;
		case "all":
			break;
		default:
		}
		publisher.publishEvent(event);
	}

}
