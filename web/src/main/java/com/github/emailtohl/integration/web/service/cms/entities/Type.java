package com.github.emailtohl.integration.web.service.cms.entities;

import java.util.LinkedHashSet;
import java.util.Set;

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
import com.github.emailtohl.integration.core.common.SelfRef;

/**
 * 文章的分类
 * @author HeLei
 */
@Entity
@Table(name = "article_type")
public class Type extends BaseEntity implements SelfRef {
	private static final long serialVersionUID = -1103006931831197370L;
	/**
	 * 分类的名字
	 */
	@NotNull
	private String name;
	
	/**
	 * 分类的描述
	 */
	private String description;

	/**
	 * 上一级分类
	 */
	private Type parent;
	
	/**
	 * 分类下的文章
	 */
	private Set<Article> articles = new LinkedHashSet<Article>();
	
	private Integer articlesNum;
	
	public Type() {}
	
	public Type(String name, String description, Type parent) {
		super();
		this.name = name;
		this.description = description;
		this.parent = parent;
	}

	@Column(unique = true, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToOne
	@JoinColumn(name = "parent_type")
	public Type getParent() {
		return parent;
	}

	public void setParent(Type parent) {
		this.parent = parent;
	}

	@JsonBackReference
	// 使用LazyCollectionOption.EXTRA，集合在调用size(),isEmpty(),contains()等操作时不会加载实例
	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.EXTRA)
	/*
	 * 孤儿删除和级联删除的区别：
	 * orphanRemoval is an entirely ORM-specific thing. It marks "child" entity
	 * to be removed when it's no longer referenced from the "parent" entity,
	 * e.g. when you remove the child entity from the corresponding collection
	 * of the parent entity.
	 * 
	 * ON DELETE CASCADE is a database-specific thing, it deletes the "child"
	 * row in the database when the "parent" row is deleted.
	 */
	@OneToMany(mappedBy = "type", orphanRemoval = false)
	@OrderBy(BaseEntity.CREATE_DATE_PROPERTY_NAME)
	public Set<Article> getArticles() {
		return articles;
	}

	public void setArticles(Set<Article> articles) {
		this.articles = articles;
	}

	@Transient
	public Integer getArticlesNum() {
		return articlesNum;
	}
	public void setArticlesNum(Integer articlesNum) {
		this.articlesNum = articlesNum;
	}

	@Override
	public String toString() {
		return "Type [name=" + name + ", parent=" + parent + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Type other = (Type) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
	

}
