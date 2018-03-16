package com.github.emailtohl.integration.web.service.flow;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 保存每一次的审核信息
 * 
 * @author HeLei
 */
@Embeddable
@AttributeOverrides({ @AttributeOverride(name = "activityId", column = @Column(name = "activity_id")),
		@AttributeOverride(name = "checkerId", column = @Column(name = "checker_id")),
		@AttributeOverride(name = "checkerNum", column = @Column(name = "checker_num")),
		@AttributeOverride(name = "checkerName", column = @Column(name = "checker_name")),
		@AttributeOverride(name = "checkApproved", column = @Column(name = "check_approved")),
		@AttributeOverride(name = "checkComment", column = @Column(name = "check_comment")),
		@AttributeOverride(name = "checkTime", column = @Column(name = "check_time")) })
public class Check implements Serializable {
	private static final long serialVersionUID = 7833201762513830536L;
	// 所处节点
	private String activityId;
	// 所审核的任务名
	private String taskName;
	// 审核人id
	private Long checkerId;
	// 审核人姓名
	private String checkerName;
	// 审核是否通过
	private Boolean checkApproved;
	// 审核意见
	private String checkComment;
	// 审核时间
	private Date checkTime;
	
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Long getCheckerId() {
		return checkerId;
	}
	public void setCheckerId(Long checkerId) {
		this.checkerId = checkerId;
	}

	public String getCheckerName() {
		return checkerName;
	}
	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public Boolean getCheckApproved() {
		return checkApproved;
	}
	public void setCheckApproved(Boolean checkApproved) {
		this.checkApproved = checkApproved;
	}

	public String getCheckComment() {
		return checkComment;
	}
	public void setCheckComment(String checkComment) {
		this.checkComment = checkComment;
	}

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public Date getCheckTime() {
		return checkTime;
	}
	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime;
	}
	
	@Override
	public String toString() {
		return "Check [activityId=" + activityId + ", taskName=" + taskName + ", checkerId=" + checkerId
				+ ", checkerName=" + checkerName + ", checkApproved=" + checkApproved + ", checkComment=" + checkComment
				+ ", checkTime=" + checkTime + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checkTime == null) ? 0 : checkTime.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Check other = (Check) obj;
		if (checkTime == null) {
			if (other.checkTime != null)
				return false;
		} else if (!checkTime.equals(other.checkTime))
			return false;
		return true;
	}

}
