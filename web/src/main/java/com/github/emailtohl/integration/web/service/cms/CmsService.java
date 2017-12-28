package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.role.Authority.CONTENT;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * cms的服务层接口
 * @author HeLei
 */
@Validated
public interface CmsService {
	String CACHE_NAME_ARTICLE = "articleCache";
	String CACHE_NAME_ARTICLE_LIST = "articleListCache";
	String CACHE_NAME_CLASSIFY = "classifyCache";
	
	/**
	 * 获取某文章
	 * @param id
	 * @return
	 * @throws NotFoundException 由于缓存机制，返回一定不能为null，所以使用NotFoundException表示未找到文章
	 */
	@Cacheable(value = CACHE_NAME_ARTICLE, key = "#root.args[0]")
	Article getArticle(long id) throws NotFoundException;
	
	/**
	 * 全文搜索
	 * @param query
	 * @param pageable
	 * @return 只返回查找到的实体类E
	 */
	Paging<Article> searchArticles(String query, Pageable pageable);
	
	/**
	 * 保存文章
	 * @param username 系统中用户唯一标识：邮箱或手机号或者平台账号的工号
	 * @param article
	 * @param typeName 可以为null
	 * @return
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("isAuthenticated()")
	Article saveArticle(@NotNull String username, @NotNull @Valid Article article, String typeName);
	
	/**
	 * 修改某文章
	 * @param id
	 * @param article
	 * @return
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("isAuthenticated()")
	Article updateArticle(long id, @NotNull @Valid Article article);
	
	/**
	 * 修改某文章
	 * @param id
	 * @param title
	 * @param keywords
	 * @param body
	 * @param type
	 * @return
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("isAuthenticated()")
	Article updateArticle(long id, String title, String keywords, String body, String summary, String type);
	
	/**
	 * 特殊情况下用于管理员删除文章
	 * @param id
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE, CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	void deleteArticle(long id);

	/**
	 * 让文章发表
	 * @param articleId
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article approveArticle(long articleId);
	
	/**
	 * 拒绝文章发布
	 * @param articleId
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article rejectArticle(long articleId);
	
	/**
	 * 开放评论
	 * @param articleId
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article openComment(long articleId);
	
	/**
	 * 关闭评论
	 * @param articleId
	 */
	@CacheEvict(value = { CACHE_NAME_ARTICLE_LIST, CACHE_NAME_CLASSIFY }, allEntries = true)
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article closeComment(long articleId);
	
	/**
	 * 获取某评论
	 * @param id
	 * @return
	 */
	Comment findComment(long id);
	
	/**
	 * 根据文章标题查询评论
	 * @param articleTitle
	 * @param pageable
	 * @return
	 */
	Paging<Comment> queryComments(String articleTitle, Pageable pageable);
	
	/**
	 * 保存评论
	 * @param username 系统中用户唯一标识：邮箱或手机号或者平台账号的工号
	 * @param articleId
	 * @param content
	 * @return article 用于清除缓存
	 */
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("isAuthenticated() && #username == principal.username")
	Article saveComment(@NotNull @P("username") String username, @Min(1) long articleId, @NotNull String content);

	/**
	 * 修改评论
	 * @param email 系统中用户唯一标识：邮箱或手机号或者平台账号的工号
	 * @param id 评论的id
	 * @param commentContent 评论的内容
	 * @return article 用于清除缓存
	 */
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("isAuthenticated() && #username == principal.username")
	Article updateComment(@NotNull @P("username") String username, @Min(1) long id, @NotNull String commentContent);
	
	/**
	 * 删除评论
	 * @param id 评论id
	 * return article 用于清除缓存
	 */
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article deleteComment(@Min(1) long id);
	
	/**
	 * 允许评论发表
	 * @param commentId
	 * @return article 用于清除缓存
	 */
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article approvedComment(long commentId);
	
	/**
	 * 拒绝评论发表
	 * @param commentId
	 * @return article 用于清除缓存
	 */
	@CachePut(value = CACHE_NAME_ARTICLE, key = "#result.id")
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Article rejectComment(long commentId);
	
	/**
	 * 查询一篇文章的评论数
	 * @param articleId
	 * @return
	 */
	long commentCount(Long articleId);
	
	/**
	 * 获取所有的文章分类
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<Type> getTypes();
	
	/**
	 * 分页查询文章类型
	 * @param typeName
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<Type> getTypePage(String typeName, Pageable pageable);
	
	/**
	 * 根据id查找文章类型
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Type findTypeById(long id);
	
	/**
	 * 通过名字查询文章类型
	 * @param name
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Type findTypeByName(@NotNull String name);

	/**
	 * 保存一个文章类型
	 * @param name
	 * @param description
	 * @param parent
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	long saveType(@NotNull String name, String description, String parent);
	
	/**
	 * 更新一个文章类型
	 * @param name
	 * @param description
	 * @param parent 类型的父类型，如果为null则为顶级类型
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	void updateType(@Min(1) long id, @NotNull String name, String description, String parent);
	
	/**
	 * 删除一个文章类型
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	void deleteType(@Min(1) long id);
	
	/**
	 * 最近文章列表
	 * @return
	 */
	@Cacheable(value = CACHE_NAME_ARTICLE_LIST)
	List<Article> recentArticles();
	
	/**
	 * 打开文章
	 * @param id
	 * @return
	 */
	@Cacheable(value = CACHE_NAME_ARTICLE, key = "#root.args[0]")
	Article readArticle(long id);
	
	/**
	 * 最近评论列表
	 * @return
	 */
	List<Comment> recentComments();
	
	/**
	 * 根据文章类型进行分类
	 * @return
	 */
	@Cacheable(value = CACHE_NAME_CLASSIFY)
	Map<Type, List<Article>> classify();
	
}