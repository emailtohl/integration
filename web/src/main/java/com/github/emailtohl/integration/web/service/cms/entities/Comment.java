package com.github.emailtohl.integration.web.service.cms.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 评论嵌入类
 * @author HeLei
 */
@org.hibernate.envers.Audited
@org.hibernate.search.annotations.Indexed
@org.hibernate.annotations.BatchSize(size = 10)// 因n+1查询问题，盲猜优化，一次性加载size个代理
@Entity
@Table(name = "article_comment")
public class Comment extends BaseEntity implements Comparable<Comment> {
	private static final long serialVersionUID = 2074688008515735092L;
	
	@NotNull
	private String content;
	private UserRef critics;
	@NotNull
	private Article article;
	private Boolean isApproved;
	private EmployeeRef approver;
	
	@org.hibernate.search.annotations.Field
	@Lob
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToOne
	@JoinColumn(name = "critics_customer_id")
	public UserRef getCritics() {
		return critics;
	}
	public void setCritics(UserRef critics) {
		this.critics = critics;
	}

	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.ContainedIn
	@ManyToOne
	@JoinColumn(name = "article_id", nullable = false)
	public Article getArticle() {
		return article;
	}
	public void setArticle(Article article) {
		this.article = article;
	}

	@Column(name = "is_approved")
	public boolean isApproved() {
		return isApproved;
	}
	public void setApproved(boolean isApproved) {
		this.isApproved = isApproved;
	}

	public Boolean getIsApproved() {
		return isApproved;
	}
	public void setIsApproved(Boolean isApproved) {
		this.isApproved = isApproved;
	}

	public EmployeeRef getApprover() {
		return approver;
	}
	public void setApprover(EmployeeRef approver) {
		this.approver = approver;
	}

	@Override
	public int compareTo(Comment o) {
		return getCreateDate().compareTo(o.getCreateDate());
	}

	@Override
	public String toString() {
		return "Comment [content=" + content + ", critics=" + critics + "]";
	}
	
}
