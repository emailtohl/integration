package com.github.emailtohl.integration.common.testEntities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 评论嵌入类
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.annotations.BatchSize(size = 10)// 因n+1查询问题，盲猜优化，一次性加载size个代理
@Entity
@Table(name = "t_article_comment")
public class Comment extends BaseEntity implements Comparable<Comment> {
	private static final long serialVersionUID = 2074688008515735092L;
	
	@NotNull
	private String content;
	private String reviewer;
	private String icon = "";
	@NotNull
	private Article article;
	private boolean isApproved = true;
	
	@org.hibernate.search.annotations.Field
	@Lob
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@org.hibernate.search.annotations.Field
	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

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

	@Override
	public int compareTo(Comment o) {
		return getCreateDate().compareTo(o.getCreateDate());
	}

	@Override
	public String toString() {
		return "Comment [content=" + content + ", reviewer=" + reviewer + "]";
	}
	
}
