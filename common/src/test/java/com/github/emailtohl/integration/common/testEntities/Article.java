package com.github.emailtohl.integration.common.testEntities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 文章实体
 * @author HeLei
 */
@org.hibernate.annotations.BatchSize(size = 10)// 因n+1查询问题，盲猜优化，一次性加载size个代理
@org.hibernate.envers.Audited
@org.hibernate.search.annotations.Indexed
@org.hibernate.search.annotations.Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Entity
@Table(name = "t_article")
public class Article extends BaseEntity implements Comparable<Article> {
	private static final long serialVersionUID = -5430993268188939177L;
	@NotNull
	private String title;
	private String keywords;
	@NotNull
	private String body;
	private String summary;
	private String cover;
	@NotNull
	private User author;
	private Type type;
	private boolean isApproved = true;
	private boolean isComment = true;
	private List<Comment> comments = new ArrayList<>();
	
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
	
	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.NO)
	@Lob
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.YES, boost = @org.hibernate.search.annotations.Boost(1.2f))
	@Lob
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
	@org.hibernate.search.annotations.IndexedEmbedded
	@ManyToOne(optional = false)
	@JoinColumn(name = "author_id")
	public User getAuthor() {
		return author;
	}
	public void setAuthor(User author) {
		this.author = author;
	}
	
	@org.hibernate.envers.NotAudited
	@ManyToOne
	@JoinColumn(name = "article_type_id", nullable = true)
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	@org.hibernate.envers.NotAudited
	@Column(name = "is_approved")
	public boolean isApproved() {
		return isApproved;
	}
	public void setApproved(boolean isApproved) {
		this.isApproved = isApproved;
	}
	
	@org.hibernate.envers.NotAudited
	@Column(name = "is_comment")
	public boolean isComment() {
		return isComment;
	}
	public void setComment(boolean isComment) {
		this.isComment = isComment;
	}
	
	// 使用LazyCollectionOption.EXTRA，集合在调用size(),isEmpty(),contains()等操作时不会加载实例
	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.EXTRA)
	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.IndexedEmbedded
	@OrderBy(BaseEntity.CREATE_DATE_PROPERTY_NAME + " DESC")
	@OneToMany(mappedBy = "article")
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
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
