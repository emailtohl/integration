package com.github.emailtohl.integration.web.service.flow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 整个流程涉及的数据
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
	// 初审人id
	private Long checkerId;
	// 初审人Number
	private Integer checkerNum;
	// 初审人姓名
	private String checkerName;
	// 初审是否通过
	private Boolean checkApproved;
	// 初审意见
	private String checkOpinions;
	// 初审时间
	private Date checkTime;
	// 复审人id
	private Long recheckerId;
	// 复审人Number
	private Integer recheckerNum;
	// 复审人姓名
	private String recheckerName;
	// 复审是否通过
	private Boolean recheckApproved;
	// 复审意见
	private String recheckOpinions;
	// 复审时间
	private Date recheckTime;
	// 是否放弃申请
	private Boolean reApply;
	// 最终结果
	private Boolean pass;
	// 当前任务id
	private String taskId;
	// 当前所在的活动id
	private String activityId;
	
	@Column(name = "process_instance_id", nullable = false)
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
	
	@Column(nullable = false)
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
	
	@Column(name = "checker_id")
	public Long getCheckerId() {
		return checkerId;
	}
	public void setCheckerId(Long checkerId) {
		this.checkerId = checkerId;
	}
	
	@Column(name = "checker_num")
	public Integer getCheckerNum() {
		return checkerNum;
	}
	public void setCheckerNum(Integer checkerNum) {
		this.checkerNum = checkerNum;
	}
	
	@Column(name = "checker_name")
	public String getCheckerName() {
		return checkerName;
	}
	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}
	
	@Column(name = "checker_approved")
	public Boolean getCheckApproved() {
		return checkApproved;
	}
	public void setCheckApproved(Boolean checkApproved) {
		this.checkApproved = checkApproved;
	}
	
	@Column(name = "check_opinions")
	public String getCheckOpinions() {
		return checkOpinions;
	}
	public void setCheckOpinions(String checkOpinions) {
		this.checkOpinions = checkOpinions;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "check_time")
	public Date getCheckTime() {
		return checkTime;
	}
	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime;
	}
	
	@Column(name = "rechecker_id")
	public Long getRecheckerId() {
		return recheckerId;
	}
	public void setRecheckerId(Long recheckerId) {
		this.recheckerId = recheckerId;
	}
	
	@Column(name = "rechecker_num")
	public Integer getRecheckerNum() {
		return recheckerNum;
	}
	public void setRecheckerNum(Integer recheckerNum) {
		this.recheckerNum = recheckerNum;
	}
	
	@Column(name = "rechecker_name")
	public String getRecheckerName() {
		return recheckerName;
	}
	public void setRecheckerName(String recheckerName) {
		this.recheckerName = recheckerName;
	}
	
	@Column(name = "recheck_approved")
	public Boolean getRecheckApproved() {
		return recheckApproved;
	}
	public void setRecheckApproved(Boolean recheckApproved) {
		this.recheckApproved = recheckApproved;
	}
	
	@Column(name = "recheck_opinions")
	public String getRecheckOpinions() {
		return recheckOpinions;
	}
	public void setRecheckOpinions(String recheckOpinions) {
		this.recheckOpinions = recheckOpinions;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "recheck_time")
	public Date getRecheckTime() {
		return recheckTime;
	}
	public void setRecheckTime(Date recheckTime) {
		this.recheckTime = recheckTime;
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
	
	@Transient
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	@Transient
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	
	@Override
	public String toString() {
		return "FlowData [processInstanceId=" + processInstanceId + ", flowType=" + flowType + ", content=" + content
				+ ", applicantId=" + applicantId + ", applicantName=" + applicantName + ", checkerId=" + checkerId
				+ ", checkerNum=" + checkerNum + ", checkerName=" + checkerName + ", checkApproved=" + checkApproved
				+ ", checkOpinions=" + checkOpinions + ", checkTime=" + checkTime + ", recheckerId=" + recheckerId
				+ ", recheckerNum=" + recheckerNum + ", recheckerName=" + recheckerName + ", recheckApproved="
				+ recheckApproved + ", recheckOpinions=" + recheckOpinions + ", recheckTime=" + recheckTime
				+ ", reApply=" + reApply + ", pass=" + pass + ", taskId=" + taskId + ", activityId=" + activityId + "]";
	}
	
}
