package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.emailtohl.integration.common.exception.InvalidDataException;
import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.web.service.cms.CmsService;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.github.emailtohl.integration.web.service.cms.entities.WebPage;
import com.google.gson.Gson;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
/**
 * 内容管理的控制器
 * @author HeLei
 */
@Controller
public class CmsCtrl {
	private static final Logger logger = LogManager.getLogger();
	@Inject Configuration cfg;
	@Inject CmsService cmsService;
	Gson gson = new Gson();

	/**
	 * 获取某文章
	 * @param id
	 * @return
	 * @throws NotFoundException 
	 */
	@RequestMapping(value = "cms/article/{id}", method = GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String findArticle(@PathVariable long id) throws NotFoundException {
		Article article = cmsService.getArticle(id);
		String json = gson.toJson(article);// 因Article有时间类型，用配置了时间格式的Gson解析
		return json;
	}
	
	/**
	 * 全文搜索
	 * @param query
	 * @param pageable
	 * @return 只返回查找到的实体类E
	 */
	@RequestMapping(value = "cms/article/search", method = GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String search(@RequestParam(name="query", required = false, defaultValue = "") String query, 
			@PageableDefault(page = 0, size = 10, sort = {"title", "keywords"}, direction = Direction.DESC) Pageable pageable) {
		Paging<Article> page = cmsService.searchArticles(query, pageable);
		String json = gson.toJson(page);// 因Article有时间类型，用配置了时间格式的Gson解析
		return json;
	}
	
	/**
	 * 保存文章，从安全上下文中查找用户名
	 * @param form 前端提交的表单数据
	 * @param e 若校验失败后，存储的失败信息
	 */
	@RequestMapping(value = "cms/article", method = POST)
	@ResponseBody
	public void saveArticle(@RequestBody @Valid ArticleForm form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			throw new InvalidDataException(e.toString());
		}
		Article a = new Article(form.title, form.keywords, form.body, form.summary);
		cmsService.saveArticle(CtrlUtil.getCurrentUsername(), a, form.type);
	}
	
	/**
	 * 文章的表单信息
	 */
	class ArticleForm implements Serializable {
		private static final long serialVersionUID = 2897739809930401319L;
		@NotNull String title;
		String keywords;
		@NotNull String body;
		String summary;
		String type;
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
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		@Override
		public String toString() {
			return "ArticleForm [title=" + title + ", keywords=" + keywords + ", body=" + body + ", type=" + type + "]";
		}
	}
	
	/**
	 * 修改某文章
	 * @param id 文章id
	 * @param article 修改的信息
	 * @param article 存储校验失败的信息
	 */
	@RequestMapping(value = "cms/article/{id}", method = PUT)
	@ResponseBody
	public void updateArticle(@PathVariable long id, @RequestBody @Valid ArticleForm form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			throw new InvalidDataException(e.toString());
		}
		cmsService.updateArticle(id, form.title, form.keywords, form.body, form.summary, form.type);
	}
	
	/**
	 * 特殊情况下用于管理员删除文章
	 * @param id
	 */
	@RequestMapping(value = "cms/article/{id}", method = DELETE)
	@ResponseBody
	public void deleteArticle(@PathVariable long id) {
		cmsService.deleteArticle(id);
	}

	/**
	 * 让文章发表
	 * @param articleId
	 */
	@RequestMapping(value = "cms/approveArticle", method = POST)
	@ResponseBody
	public void approveArticle(@RequestParam long articleId) {
		cmsService.approveArticle(articleId);
	}
	
	/**
	 * 拒绝文章发布
	 * @param articleId
	 */
	@RequestMapping(value = "cms/rejectArticle", method = POST)
	@ResponseBody
	public void rejectArticle(@RequestParam long articleId) {
		cmsService.rejectArticle(articleId);
	}
	
	/**
	 * 开放评论
	 * @param articleId
	 */
	@RequestMapping(value = "cms/openComment", method = POST)
	@ResponseBody
	public void openComment(@RequestParam long articleId) {
		cmsService.openComment(articleId);
	}
	
	/**
	 * 关闭评论
	 * @param articleId
	 */
	@RequestMapping(value = "cms/closeComment", method = POST)
	@ResponseBody
	public void closeComment(@RequestParam long articleId) {
		cmsService.closeComment(articleId);
	}
	
	/**
	 * 查询评论列表
	 * @param articleTitle
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "cms/comments", method = GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String queryComments(@RequestParam(required = false, name = "query", defaultValue = "") String query, 
			@PageableDefault(page = 0, size = 10, sort = {BaseEntity.CREATE_DATE_PROPERTY_NAME, "article.title"}, direction = Direction.DESC) Pageable pageable) {
		Paging<Comment> page = cmsService.queryComments(query, pageable);
		String json = gson.toJson(page);// 因Article有时间类型，用配置了时间格式的Gson解析
		return json;
	}
	
	/**
	 * 获取某评论
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "cms/comment/{id}", method = GET)
	@ResponseBody
	public Comment findComment(@PathVariable long id) {
		return cmsService.findComment(id);
	}
	
	/**
	 * 保存评论，从安全上下文中查找用户名，如果没有认证则为匿名
	 * @param articleId 被评论文章的ID
	 * @param content 评论的内容
	 */
	@RequestMapping(value = "cms/comment", method = POST)
	public String saveComment(@RequestParam(required = true, name = "articleId") long articleId,
			@RequestParam(required = true, name = "content") String content) {
		cmsService.saveComment(CtrlUtil.getCurrentUsername(), articleId, content);
		return "redirect:/detail?id=" + articleId;
	}
	
	/**
	 * 修改评论
	 * @param id 评论的id
	 * @param commentContent 评论的内容
	 */
	@RequestMapping(value = "cms/comment/{id}", method = PUT)
	public String updateComment(@PathVariable("id") long id, 
			@RequestParam(required = true, name = "commentContent") String commentContent) {
		Article a = cmsService.updateComment(CtrlUtil.getCurrentUsername(), id, commentContent);
		return "redirect:/detail?id=" + a.getId();
	}
	
	/**
	 * 删除评论
	 * @param id 评论id
	 */
	@RequestMapping(value = "cms/comment/{id}", method = DELETE)
	@ResponseBody
	public void deleteComment(@PathVariable long id) {
		cmsService.deleteComment(id);
	}
	
	/**
	 * 允许评论发表
	 * @param commentId
	 */
	@RequestMapping(value = "cms/approvedComment", method = POST)
	@ResponseBody
	public void approvedComment(@RequestParam long commentId) {
		cmsService.approvedComment(commentId);
	}
	
	/**
	 * 拒绝评论发表
	 * @param commentId
	 */
	@RequestMapping(value = "cms/rejectComment", method = POST)
	@ResponseBody
	public void rejectComment(@RequestParam long commentId) {
		cmsService.rejectComment(commentId);
	}
	
	/**
	 * 获取所有的分类
	 * @return
	 */
	@RequestMapping(value = "cms/typePage", method = GET)
	@ResponseBody
	public Paging<Type> getTypePage(@RequestParam(name="name", required = false, defaultValue = "") String name, 
			@PageableDefault(page = 0, size = 10, sort = {BaseEntity.CREATE_DATE_PROPERTY_NAME}, direction = Direction.DESC) Pageable pageable) {
		return cmsService.getTypePage(name, pageable);
	}
	
	/**
	 * 获取所有的分类
	 * @return
	 */
	@RequestMapping(value = "cms/types", method = GET)
	@ResponseBody
	public List<Type> getTypes() {
		return cmsService.getTypes();
	}
	
	/**
	 * 根据id查找文章类型
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "cms/type/{id}", method = GET)
	@ResponseBody
	public Type findTypeById(@PathVariable long id) {
		return cmsService.findTypeById(id);
	}
	
	/**
	 * 通过名字查询文章类型
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "cms/type", method = GET)
	@ResponseBody
	public Type findTypeByName(@RequestParam("name") String name) {
		return cmsService.findTypeByName(name);
	}

	/**
	 * 保存一个文章类型
	 * @param name
	 * @param description
	 * @param parent
	 * @return
	 */
	@RequestMapping(value = "cms/type", method = POST)
	@ResponseBody
	public long saveType(@RequestBody TypeForm form) {
		return cmsService.saveType(form.name, form.description, form.parent);
	}
	
	/**
	 * 更新一个文章类型
	 * @param name
	 * @param description
	 * @param parent 类型的父类型，如果为null则为顶级类型
	 */
	@RequestMapping(value = "cms/type/{id}", method = PUT)
	@ResponseBody
	public void updateType(@PathVariable("id") long id, @RequestBody TypeForm form) {
		cmsService.updateType(id, form.name, form.description, form.parent);
	}
	
	class TypeForm {
		long id;
		String name;
		String description;
		String parent;
	}
	
	/**
	 * 删除一个文章类型
	 * @param id
	 */
	@RequestMapping(value = "cms/type/{id}", method = DELETE)
	@ResponseBody
	public void deleteType(@PathVariable("id") long id) {
		cmsService.deleteType(id);
	}
	
	/**
	 * 最近文章列表
	 * @return
	 */
	@RequestMapping(value = "public/recentArticles", method = GET)
	public List<Article> recentArticles() {
		return cmsService.recentArticles();
	}
	
	/**
	 * 最近评论列表
	 * @return
	 */
	@RequestMapping(value = "public/recentComments", method = GET)
	public List<Comment> recentComments() {
		return cmsService.recentComments();
	}
	
	/**
	 * 根据文章类型进行分类
	 * @return
	 */
	@RequestMapping(value = "public/classify", method = GET)
	public Map<Type, List<Article>> classify() {
		return cmsService.classify();
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
	@RequestMapping(value = "detail", method = GET)
	public void getDetail(@RequestParam long id, HttpServletRequest request, HttpServletResponse response) throws TemplateException, IOException, NotFoundException {
		Article a = cmsService.getArticle(id);
		Map<String, Object> model = new HashMap<>();
		model.put("article", a);
		List<Article> ls = cmsService.recentArticles();
		model.put("recentArticles", ls);
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		Template t = cfg.getTemplate("detail.html");
		t.process(model, out);
		out.close();
	}
}
