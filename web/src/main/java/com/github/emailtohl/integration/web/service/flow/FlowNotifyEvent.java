package com.github.emailtohl.integration.web.service.flow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.github.emailtohl.integration.web.cluster.ClusterEvent;
/**
 * 用户通知事件
 * @author HeLei
 */
public class FlowNotifyEvent extends ClusterEvent {
	public static enum Type {
		Execution, Task
	}
	private static final long serialVersionUID = -482996726719979688L;
	private String id;
	private Type type;
	private String eventName;
	private String processInstanceId;
	private String processBusinessKey;
	private String activityId;
	private String activityName;
	// 通知的人
	private Set<String> toUserIds = new HashSet<>();
	// 通知的内容
	private Object content;
	
	public FlowNotifyEvent(Serializable source) {
		super(source);
	}

	public FlowNotifyEvent(Serializable source, String id, Type type, String eventName, String processInstanceId) {
		super(source);
		this.id = id;
		this.type = type;
		this.eventName = eventName;
		this.processInstanceId = processInstanceId;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessBusinessKey() {
		return processBusinessKey;
	}
	public void setProcessBusinessKey(String processBusinessKey) {
		this.processBusinessKey = processBusinessKey;
	}

	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public Set<String> getToUserIds() {
		return toUserIds;
	}
	public void setToUserIds(Set<String> toUserIds) {
		this.toUserIds = toUserIds;
	}

	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}

}
