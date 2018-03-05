package com.github.emailtohl.integration.web.service.flow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 整个流程涉及的数据，既作为接收流程相关的表单数据，也作为显示层的数据承载对象
 * 
 * @author HeLei
 */
@Entity
@Table(name = "flow_data")
public class FlowData extends BaseEntity {
	private static final long serialVersionUID = 6027660935630687413L;
	// 关联Activiti的流程id
	private String processInstanceId;
	// 流程类型
	private FlowType flowType;
	// 申请内容
	private String content;
	// 申请人id
	private Long applicantId;
	// 申请人姓名
	private String applicantName;
	// 是否放弃申请
	private Boolean reApply;
	// 最终结果
	private Boolean pass;
	// 历史的审核信息
	private List<Check> checks = new ArrayList<>();
	// 审核是否通过
	private Boolean checkApproved;
	// 审核意见
	private String checkComment;
	// 当前任务id
	private String taskId;
	// 当前任务是否被签收
	private String taskAssignee;
	// 当前所在的活动id
	private String activityId;

	@Column(name = "process_instance_id")
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "flow_type", nullable = false)
	public FlowType getFlowType() {
		return flowType;
	}
	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	@Column(name = "content", nullable = false)
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "applicant_id", nullable = false)
	public Long getApplicantId() {
		return applicantId;
	}
	public void setApplicantId(Long applicantId) {
		this.applicantId = applicantId;
	}

	@Column(name = "applicant_name")
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}

	@Column(name = "re_apply")
	public Boolean getReApply() {
		return reApply;
	}
	public void setReApply(Boolean reApply) {
		this.reApply = reApply;
	}

	@Column(name = "pass")
	public Boolean getPass() {
		return pass;
	}
	public void setPass(Boolean pass) {
		this.pass = pass;
	}
	
	@ElementCollection
	@CollectionTable(name = "flow_data_check", joinColumns = @JoinColumn(name = "flow_data_id"))
	public List<Check> getChecks() {
		return checks;
	}
	public void setChecks(List<Check> checks) {
		this.checks = checks;
	}
	
	@Transient
	public Boolean getCheckApproved() {
		return checkApproved;
	}
	public void setCheckApproved(Boolean checkApproved) {
		this.checkApproved = checkApproved;
	}
	
	@Transient
	public String getCheckComment() {
		return checkComment;
	}
	public void setCheckComment(String checkComment) {
		this.checkComment = checkComment;
	}
	
	@Transient
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Transient
	public String getTaskAssignee() {
		return taskAssignee;
	}
	public void setTaskAssignee(String taskAssignee) {
		this.taskAssignee = taskAssignee;
	}

	@Transient
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	
}
