package com.github.emailtohl.integration.conference.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.user.entities.User;
/**
 * 论坛帖子对象
 * @author HeLei
 * @date 2017.02.04
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.search.annotations.Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Entity
@Table(name = "t_Post")
public class ForumPost extends BaseEntity {
	private static final long serialVersionUID = 5500398003740322853L;
	private User user;
	private String title;
	private String body;
	private String keywords;

	@org.hibernate.search.annotations.IndexedEmbedded
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id")
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.YES)
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.NO)
	@Lob
	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@org.hibernate.search.annotations.Field(boost = @org.hibernate.search.annotations.Boost(1.5F), store = org.hibernate.search.annotations.Store.YES)// 关键字加权因子
	public String getKeywords() {
		return this.keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

}
