package com.github.emailtohl.integration.web.message.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * 用户通知事件
 * @author HeLei
 */
public class FlowNotifyEvent extends ClusterEvent {
	private static final long serialVersionUID = -482996726719979688L;
	private Map<String, Serializable> variables = new HashMap<>();
	
	public FlowNotifyEvent(Serializable source) {
		super(source);
	}

	public FlowNotifyEvent(Serializable source, Map<String, Serializable> args) {
		super(source);
		this.variables = args;
	}

	public Map<String, Serializable> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, Serializable> variables) {
		this.variables = variables;
	}

}
