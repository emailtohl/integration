package com.github.emailtohl.integration.web.service.flow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Embeddable
public class Audit {
	private Long auditorId;
	private Long auditorName;
	private Boolean approved;
	private String remark;
	private Date time;
	
	public Audit() {}
	
	public Audit(Long auditorId, Long auditorName, Boolean approved, String remark, Date time) {
		this.auditorId = auditorId;
		this.auditorName = auditorName;
		this.approved = approved;
		this.remark = remark;
		this.time = time;
	}

	@Column(name = "auditor_id", nullable = false)
	public Long getAuditorId() {
		return auditorId;
	}
	public void setAuditorId(Long auditorId) {
		this.auditorId = auditorId;
	}
	
	@Column(name = "auditor_name", nullable = false)
	public Long getAuditorName() {
		return auditorName;
	}
	public void setAuditorName(Long auditorName) {
		this.auditorName = auditorName;
	}
	public Boolean getApproved() {
		return approved;
	}
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
	
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	

}
