package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.role.Authority.CONTENT;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.web.service.cms.entities.Article;

/**
 * 文章服务
 * @author HeLei
 */
public interface ArticleService {
	
	/**
	 * 创建文章
	 * @param entity
	 * @return
	 */
	Article create(Article entity);

	/**
	 * 总是返回true，因为文章名不是唯一性的
	 * @param matcherValue
	 * @return
	 */
	boolean exist(Object matcherValue);

	/**
	 * 获取一份文章
	 * @param id
	 * @return
	 */
	Article get(Long id);

	/**
	 * 全文搜索文章
	 * @param query
	 * @param pageable
	 * @return
	 */
	Paging<Article> search(String query, Pageable pageable);
	
	/**
	 * 查询文章
	 * @param params
	 * @param pageable
	 * @return
	 */
	Paging<Article> query(Article params, Pageable pageable);

	/**
	 * 查询文章
	 * @param params
	 * @return
	 */
	List<Article> query(Article params);

	/**
	 * 更新文章
	 * @param id
	 * @param newEntity
	 * @return
	 */
	Article update(Long id, Article newEntity);

	/**
	 * 删除文章
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	void delete(Long id);
}
