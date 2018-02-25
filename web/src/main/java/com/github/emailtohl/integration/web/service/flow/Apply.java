package com.github.emailtohl.integration.web.service.flow;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 流程申请的实体
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
	
	@Override
	public String toString() {
		return "Apply [reason=" + reason + ", processInstanceId=" + processInstanceId + ", applicant=" + applicant
				+ ", result=" + result + "]";
	}
}
