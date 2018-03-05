package com.github.emailtohl.integration.web.service.flow;

import org.activiti.engine.impl.persistence.entity.CommentEntity;

/**
 * 查询运行实例中对批注信息
 * @author HeLei
 */
public class CommentInfo extends CommentEntity {
	private static final long serialVersionUID = -1293757322668393869L;
	private String username;
	private String activitId;
	private String taskName;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getActivitId() {
		return activitId;
	}
	public void setActivitId(String activitId) {
		this.activitId = activitId;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	@Override
	public String toString() {
		return "CommentInfo [username=" + username + ", activitId=" + activitId + ", taskName=" + taskName + "]";
	}
}
