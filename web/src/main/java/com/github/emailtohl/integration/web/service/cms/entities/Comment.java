package com.github.emailtohl.integration.web.service.cms.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.lib.jpa.EntityBase;

/**
 * 评论实体类，评论可以是针对于文章的，也可以是针对于评论的
 * @author HeLei
 */
@org.hibernate.envers.Audited
@org.hibernate.search.annotations.Indexed
@org.hibernate.annotations.BatchSize(size = 10)// 因n+1查询问题，盲猜优化，一次性加载size个代理
@Entity
@Table(name = "article_comment")
public class Comment extends EntityBase implements Comparable<Comment> {
	private static final long serialVersionUID = 2074688008515735092L;
	
	@NotNull
	private String content;
	private UserRef reviewer;
	/**
	 * 针对于文章
	 */
	private Article article;
	/**
	 * 针对于评论
	 */
	private Comment comment;
	private Boolean approved;
	private EmployeeRef approver;
	private Boolean canComment;
	
	@org.hibernate.search.annotations.Field
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	@Lob
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToOne
	@JoinColumn(name = "reviewer_user_id")
	public UserRef getReviewer() {
		return reviewer;
	}
	public void setReviewer(UserRef reviewer) {
		this.reviewer = reviewer;
	}

	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.ContainedIn
	@ManyToOne
	@JoinColumn(name = "article_id")
	public Article getArticle() {
		return article;
	}
	public void setArticle(Article article) {
		this.article = article;
	}

	@org.hibernate.envers.NotAudited
	@ManyToOne
	@JoinColumn(name = "target_comment_id")
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public Boolean getApproved() {
		return approved;
	}
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToOne
	@JoinColumn(name = "approver_employee_id")
	public EmployeeRef getApprover() {
		return approver;
	}
	public void setApprover(EmployeeRef approver) {
		this.approver = approver;
	}

	@Column(name = "can_comment")
	public Boolean getCanComment() {
		return canComment;
	}
	public void setCanComment(Boolean canComment) {
		this.canComment = canComment;
	}

	@Override
	public int compareTo(Comment o) {
		return getCreateDate().compareTo(o.getCreateDate());
	}

	@Override
	public String toString() {
		return "Comment [content=" + content + ", reviewer=" + reviewer + "]";
	}
	
}
