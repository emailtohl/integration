package com.github.emailtohl.integration.web.service.flow;

import javax.persistence.Entity;
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

	@NotNull
	private String reason;
	private String processInstanceId;
	private UserRef applicant;
	private String result;
	private String taskId;
	
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
	@JoinColumn(name = "applicant_id")
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
	
	@Override
	public String toString() {
		return "Apply [reason=" + reason + ", processInstanceId=" + processInstanceId + ", applicant=" + applicant
				+ ", result=" + result + ", taskId=" + taskId + "]";
	}
	
}
