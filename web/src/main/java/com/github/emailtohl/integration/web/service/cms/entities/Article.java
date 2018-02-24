package com.github.emailtohl.integration.web.service.cms.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 文章实体
 * @author HeLei
 */
@org.hibernate.annotations.BatchSize(size = 10)// 因n+1查询问题，盲猜优化，一次性加载size个代理
@org.hibernate.envers.Audited
@org.hibernate.search.annotations.Indexed
@org.hibernate.search.annotations.Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Entity
@Table(name = "article")
public class Article extends BaseEntity implements Comparable<Article> {
	private static final long serialVersionUID = -950419219310175184L;
	@NotNull
	private String title;
	private String keywords;
	@NotNull
	private String body;
	private String summary;
	private String cover;
//	@NotNull
	private UserRef author;
	private Type type;
	private Boolean canApproved;
	private EmployeeRef approver;
	private Boolean canComment;
	private List<Comment> comments = new ArrayList<>();
	
	public Article() {}
	
	public Article(String title, String keywords, String body, String summary) {
		this.title = title;
		this.keywords = keywords;
		this.body = body;
		this.summary = summary;
	}

	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.YES)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.Field(boost = @org.hibernate.search.annotations.Boost(1.5F), store = org.hibernate.search.annotations.Store.YES)// 关键字加权因子
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	// 解决hibernate在postgresql环境下，@Lob转字符串的异常
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.NO)
//	@Lob
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	// 解决hibernate在postgresql环境下，@Lob转字符串的异常
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.YES, boost = @org.hibernate.search.annotations.Boost(1.2f))
//	@Lob
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	@org.hibernate.envers.NotAudited
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	
	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToOne(optional = false)
	@JoinColumn(name = "author_id")
	public UserRef getAuthor() {
		return author;
	}
	public void setAuthor(UserRef author) {
		this.author = author;
	}
	
	@org.hibernate.envers.NotAudited
	@ManyToOne(optional = false)
	@JoinColumn(name = "article_type_id")
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	@Column(name = "can_approved")
	public Boolean getCanApproved() {
		return canApproved;
	}
	public void setCanApproved(Boolean canApproved) {
		this.canApproved = canApproved;
	}

	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToOne
	@JoinColumn(name = "approver_id")
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

	@JsonBackReference
	// 使用LazyCollectionOption.EXTRA，集合在调用size(),isEmpty(),contains()等操作时不会加载实例
	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.EXTRA)
	@org.hibernate.search.annotations.IndexedEmbedded(depth = 2)
	@OrderBy(BaseEntity.CREATE_DATE_PROPERTY_NAME + " DESC")
	@OneToMany(mappedBy = "article")
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	@Transient
	public Integer getCommentNumbers() {
		return this.comments.size();
	}
	
	@Override
	public String toString() {
		return "Article [title=" + title + "]";
	}
	
	@Override
	public int compareTo(Article o) {
		int r = 0;
		if (getCreateDate() != null && o.getCreateDate() != null)
			// 时间越大越靠前（返回负数）
			r = o.getCreateDate().compareTo(getCreateDate());
		return r;
	}
	
}
