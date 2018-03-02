package com.github.emailtohl.integration.web.service.flow;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 流程申请单
 * @author HeLei
 */
@Entity
@Table(name = "apply")
public class Apply extends BaseEntity {
	private static final long serialVersionUID = 6263882826119555593L;

	private FlowType flowType;
	private String reason;
	private String processInstanceId;
	private UserRef applicant;
	private String result;
	private String taskId;
	private String activityId;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public FlowType getFlowType() {
		return flowType;
	}
	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}
	
	@Basic(optional = false)
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
	@ManyToOne
	@JoinColumn(name = "applicant_id", nullable = false, updatable = false)
	public UserRef getApplicant() {
		return applicant;
	}
	public void setApplicant(UserRef applicant) {
		this.applicant = applicant;
	}
	
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	@Transient
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	
	@Override
	public String toString() {
		return "Apply [reason=" + reason + ", processInstanceId=" + processInstanceId + ", applicant=" + applicant
				+ ", result=" + result + ", taskId=" + taskId + ", activityId=" + activityId + "]";
	}
	
}
