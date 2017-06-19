package com.github.emailtohl.integration.web.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.github.emailtohl.integration.cms.entities.Article;
import com.github.emailtohl.integration.cms.entities.Comment;
import com.github.emailtohl.integration.cms.entities.Type;

/**
 * web页面所需要的数据结构
 * @author HeLei
 * @date 2017.02.18
 */
public class WebPage implements Serializable {
	private static final long serialVersionUID = 2672917432577749422L;
	
	/**
	 * 页面中展现最近的文章
	 */
	List<Article> recentArticles;
	
	/**
	 * 边栏中最新评论
	 */
	List<Comment> recentComments;
	
	/**
	 * 文章类型，从每个Type里面可以获取关联的Article
	 */
	Map<Type, List<Article>> categories;

	public List<Article> getRecentArticles() {
		return recentArticles;
	}

	public void setRecentArticles(List<Article> recentArticles) {
		this.recentArticles = recentArticles;
	}

	public List<Comment> getRecentComments() {
		return recentComments;
	}

	public void setRecentComments(List<Comment> recentComments) {
		this.recentComments = recentComments;
	}

	public Map<Type, List<Article>> getCategories() {
		return categories;
	}

	public void setCategories(Map<Type, List<Article>> categories) {
		this.categories = categories;
	}

	
}
