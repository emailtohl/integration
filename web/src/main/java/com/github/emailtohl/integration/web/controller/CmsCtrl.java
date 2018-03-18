package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.exception.InvalidDataException;
import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.web.service.cms.ArticleService;
import com.github.emailtohl.integration.web.service.cms.CommentService;
import com.github.emailtohl.integration.web.service.cms.TypeService;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.github.emailtohl.integration.web.service.cms.entities.WebPage;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
/**
 * 内容管理的控制器
 * @author HeLei
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CmsCtrl {
	private static final Logger logger = LogManager.getLogger();
	
	Configuration cfg;
	TypeService typeService;
	ArticleService articleService;
	CommentService commentService;
	
	@Inject
	public CmsCtrl(Configuration cfg, TypeService typeService, ArticleService articleService,
			CommentService commentService) {
		super();
		this.cfg = cfg;
		this.typeService = typeService;
		this.articleService = articleService;
		this.commentService = commentService;
	}
	
	/**
	 * 获取某文章
	 * @param id
	 * @return
	 * @throws NotFoundException 
	 */
	@RequestMapping(value = "cms/article/{id}", method = GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Article findArticle(@PathVariable long id) throws NotFoundException {
		return articleService.get(id);
	}

	/**
	 * 全文搜索
	 * @param query
	 * @param pageable
	 * @return 只返回查找到的实体类E
	 */
	@RequestMapping(value = "cms/article/search", method = GET)
	public Paging<Article> search(@RequestParam(name="query", required = false, defaultValue = "") String query, 
			@PageableDefault(page = 0, size = 10, sort = {"title", "keywords"}, direction = Direction.DESC) Pageable pageable) {
		return articleService.search(query, pageable);
	}
	
	/**
	 * 查询文章关联的评论数量
	 * @param articleIds
	 * @return
	 */
	@RequestMapping(value = "cms/article/commentNumbers")
	public Map<Long, Long> getCommentNumbers(Long[] articleIds) {
		return articleService.getCommentNumbers(Arrays.asList(articleIds));
	}
	/**
	 * 保存文章，从安全上下文中查找用户名
	 * @param form 前端提交的表单数据
	 * @param e 若校验失败后，存储的失败信息
	 */
	@RequestMapping(value = "cms/article", method = POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public Article saveArticle(@RequestBody @Valid ArticleForm form, Errors e) {
		checkErrors(e);
		Article a = new Article(form.title, form.keywords, form.body, form.summary);
		if (form.getTypeId() != null) {
			Type t = new Type();
			t.setId(form.getTypeId());
			a.setType(t);
		}
		return articleService.create(a);
	}
	
	/**
	 * 修改某文章
	 * @param id 文章id
	 * @param article 修改的信息
	 * @param article 存储校验失败的信息
	 */
	@RequestMapping(value = "cms/article/{id}", method = PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateArticle(@PathVariable long id, @RequestBody @Valid ArticleForm form, Errors e) {
		checkErrors(e);
		Article a = new Article(form.title, form.keywords, form.body, form.summary);
		if (form.getTypeId() != null) {
			Type t = new Type();
			t.setId(form.getTypeId());
			a.setType(t);
		}
		articleService.update(id, a);
	}

	/**
	 * 特殊情况下用于管理员删除文章
	 * @param id
	 */
	@RequestMapping(value = "cms/article/{id}", method = DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteArticle(@PathVariable long id) {
		articleService.delete(id);
	}
	
	/**
	 * 让文章发表
	 * @param articleId
	 */
	@RequestMapping(value = "cms/approveArticle", method = POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approveArticle(@RequestParam long articleId) {
		articleService.approve(articleId, true);
	}
	
	/**
	 * 拒绝文章发布
	 * @param articleId
	 */
	@RequestMapping(value = "cms/rejectArticle", method = POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void rejectArticle(@RequestParam long articleId) {
		articleService.approve(articleId, false);
	}
	
	/**
	 * 开放文章的评论
	 * @param articleId
	 */
	@RequestMapping(value = "cms/openComment", method = POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void openComment(@RequestParam long articleId) {
		articleService.canComment(articleId, true);
	}
	
	/**
	 * 关闭文章评论
	 * @param articleId
	 */
	@RequestMapping(value = "cms/closeComment", method = POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void closeComment(@RequestParam long articleId) {
		articleService.canComment(articleId, false);
	}
	
	/**
	 * 查询评论列表
	 * @param articleTitle
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "cms/comments", method = GET)
	public Paging<Comment> queryComments(@RequestParam(required = false, name = "query", defaultValue = "") String query, 
			@PageableDefault(page = 0, size = 10, sort = {BaseEntity.MODIFY_DATE_PROPERTY_NAME, "article.title"}, direction = Direction.DESC) Pageable pageable) {
		return commentService.search(query, pageable);
	}
	
	/**
	 * 获取某评论
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "cms/comment/{id}", method = GET)
	public Comment findComment(@PathVariable long id) {
		return commentService.get(id);
	}
	
	/**
	 * 保存评论，从安全上下文中查找用户名，如果没有认证则为匿名
	 * @param articleId 被评论文章的ID
	 * @param content 评论的内容
	 */
	@RequestMapping(value = "cms/comment", method = POST)
	@ResponseStatus(HttpStatus.CREATED)
	public Comment saveComment(@RequestBody @Valid CommentForm form, Errors e) {
		checkErrors(e);
		Comment c = new Comment();
		c.setContent(form.getContent());
		if (form.getArticleId() != null) {
			Article a = new Article();
			a.setId(form.getArticleId());
			c.setArticle(a);
		}
		if (form.getCommentId() != null) {
			Comment _c = new Comment();
			_c.setId(form.getCommentId());
		}
		return commentService.create(c);
	}
	
	/**
	 * 修改评论
	 * @param id 评论的id
	 * @param commentContent 评论的内容
	 */
	@RequestMapping(value = "cms/comment/{id}", method = PUT)
	public Comment updateComment(@PathVariable("id") long id, @RequestBody @Valid CommentForm form, Errors e) {
		checkErrors(e);
		Comment c = new Comment();
		c.setContent(form.getContent());
		if (form.getArticleId() != null) {
			Article a = new Article();
			a.setId(form.getArticleId());
			c.setArticle(a);
		}
		if (form.getCommentId() != null) {
			Comment _c = new Comment();
			_c.setId(form.getCommentId());
		}
		return commentService.update(id, c);
	}
	
	/**
	 * 删除评论
	 * @param id 评论id
	 */
	@RequestMapping(value = "cms/comment/{id}", method = DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteComment(@PathVariable long id) {
		commentService.delete(id);
	}
	
	/**
	 * 允许评论发表
	 * @param commentId
	 */
	@RequestMapping(value = "cms/approvedComment", method = POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approvedComment(@RequestParam long commentId) {
		commentService.approve(commentId, true);
	}
	
	/**
	 * 拒绝评论发表
	 * @param commentId
	 */
	@RequestMapping(value = "cms/rejectComment", method = POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void rejectComment(@RequestParam long commentId) {
		commentService.approve(commentId, false);
	}
	
	/**
	 * 文章类型是否存在
	 * @param category
	 * @return
	 */
	@RequestMapping(value = "cms/category/exist", method = RequestMethod.GET)
	public String exist(@RequestParam(name = "category") String category) {
		return String.format("{\"exist\":%b}", typeService.exist(category));
	}
	
	/**
	 * 获取所有的分类
	 * @return
	 */
	@RequestMapping(value = "cms/typePage", method = GET)
	public Paging<Type> getTypePage(TypeForm form, @PageableDefault(page = 0, size = 10, sort = {
			BaseEntity.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		Type t = new Type(form.getName(), form.getDescription(), null);
		return typeService.query(t, pageable);
	}
	
	/**
	 * 获取所有的分类
	 * @return
	 */
	@RequestMapping(value = "cms/types", method = GET)
	public List<Type> getTypes(TypeForm form) {
		Type t = new Type(form.name, form.description, null);
		return typeService.getTypesWithArticleNum(t);
	}
	
	/**
	 * 根据id查找文章类型
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "cms/type/{id}", method = GET)
	public Type findTypeById(@PathVariable long id) {
		return typeService.get(id);
	}
	
	/**
	 * 通过名字查询文章类型
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "cms/type", method = GET)
	public Type findTypeByName(@RequestParam("name") String name) {
		return typeService.getByName(name);
	}

	/**
	 * 保存一个文章类型
	 * @param name
	 * @param description
	 * @param parent
	 * @return
	 */
	@RequestMapping(value = "cms/type", method = POST)
	@ResponseStatus(HttpStatus.CREATED)
	public Type saveType(@RequestBody @Valid TypeForm form, Errors e) {
		checkErrors(e);
		Type t = new Type(form.name, form.description, null);
		if (form.getParentId() != null) {
			Type p = new Type();
			p.setId(form.getParentId());
			t.setParent(p);
		}
		return typeService.create(t);
	}
	
	/**
	 * 更新一个文章类型
	 * @param name
	 * @param description
	 * @param parent 类型的父类型，如果为null则为顶级类型
	 */
	@RequestMapping(value = "cms/type/{id}", method = PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateType(@PathVariable("id") long id, @RequestBody @Valid TypeForm form, Errors e) {
		checkErrors(e);
		Type t = new Type(form.name, form.description, null);
		if (form.getParentId() != null) {
			Type p = new Type();
			p.setId(form.getParentId());
			t.setParent(p);
		}
		typeService.update(id, t);
	}

	
	/**
	 * 删除一个文章类型
	 * @param id
	 */
	@RequestMapping(value = "cms/type/{id}", method = DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteType(@PathVariable("id") long id) {
		typeService.delete(id);
	}
	
	/**
	 * 最近文章列表
	 * @return
	 */
	@RequestMapping(value = "public/recentArticles", method = GET)
	public List<Article> recentArticles() {
		return articleService.recentArticles();
	}
	
	/**
	 * 最近评论列表
	 * @return
	 */
	@RequestMapping(value = "public/recentComments", method = GET)
	public List<Comment> recentComments() {
		return commentService.recentComments();
	}
	
	/**
	 * 根据文章类型进行分类
	 * @return
	 */
	@RequestMapping(value = "public/classify", method = GET)
	public Map<Type, List<Article>> classify() {
		return articleService.articleClassify();
	}
	
	/**
	 * 获取web页面所需要的数据
	 * @param query 搜索页面的参数，可以为null
	 * @return
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	@RequestMapping(value = "article", method = GET)
	public void getWebPage(HttpServletRequest request, HttpServletResponse response) throws TemplateException, IOException {
		WebPage wp = new WebPage();
		wp.setRecentArticles(recentArticles());
		wp.setRecentComments(recentComments());
		wp.setCategories(classify());
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		Template t = cfg.getTemplate("article.html");
		t.process(wp, out);
		out.close();
	}
	
	/**
	 * 获取文章详情
	 * @param request
	 * @param response
	 * @throws TemplateException
	 * @throws IOException
	 * @throws NotFoundException 
	 */
	@RequestMapping(value = "public/detail", method = GET)
	public void getDetail(@RequestParam long id, HttpServletRequest request, HttpServletResponse response) throws TemplateException, IOException, NotFoundException {
		Article a = articleService.get(id);
		Map<String, Object> model = new HashMap<>();
		model.put("article", a);
		List<Article> ls = articleService.recentArticles();
		model.put("recentArticles", ls);
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		Template t = cfg.getTemplate("detail.html");
		t.process(model, out);
		out.close();
	}
	
	
	/**
	 * 校验
	 * @param errors
	 */
	protected void checkErrors(Errors errors) {
		if (errors.hasErrors()) {
			StringBuilder msg = new StringBuilder();
			for (ObjectError oe : errors.getAllErrors()) {
				logger.info(oe);
				String cause = oe.toString().split(";")[0];
				msg.append(cause).append("\t").append(oe.getDefaultMessage()).append("\n");
			}
			throw new InvalidDataException(msg.toString());
		}
	}
	
	/**
	 * 文章的表单信息
	 */
	static class ArticleForm implements Serializable {
		private static final long serialVersionUID = 3889315211250556531L;
		@NotNull String title;
		String keywords;
		@NotNull String body;
		String summary;
		Long typeId;
		Boolean approve;
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getKeywords() {
			return keywords;
		}
		public void setKeywords(String keywords) {
			this.keywords = keywords;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
		public Long getTypeId() {
			return typeId;
		}
		public void setTypeId(Long typeId) {
			this.typeId = typeId;
		}
		public String getSummary() {
			return summary;
		}
		public void setSummary(String summary) {
			this.summary = summary;
		}
		public Boolean getApprove() {
			return approve;
		}
		public void setApprove(Boolean approve) {
			this.approve = approve;
		}
	}
	
	static class TypeForm implements Serializable {
		private static final long serialVersionUID = -5081395410863857040L;
		Long id;
		@NotNull String name;
		String description;
		Long parentId;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
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
		public Long getParentId() {
			return parentId;
		}
		public void setParentId(Long parentId) {
			this.parentId = parentId;
		}
	}
	
	static class CommentForm implements Serializable {
		private static final long serialVersionUID = -4025111492861614468L;
		@NotNull String content;
		/**
		 * 针对于文章
		 */
		Long articleId;
		/**
		 * 针对于评论
		 */
		Long commentId;
		Boolean approved;
		Boolean canComment;
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public Long getArticleId() {
			return articleId;
		}
		public void setArticleId(Long articleId) {
			this.articleId = articleId;
		}
		public Long getCommentId() {
			return commentId;
		}
		public void setCommentId(Long commentId) {
			this.commentId = commentId;
		}
		public Boolean getApproved() {
			return approved;
		}
		public void setApproved(Boolean approved) {
			this.approved = approved;
		}
		public Boolean getCanComment() {
			return canComment;
		}
		public void setCanComment(Boolean canComment) {
			this.canComment = canComment;
		}
		
	}
}
