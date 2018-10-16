package com.github.emailtohl.integration.web.service.cms;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.github.emailtohl.lib.exception.NotAcceptableException;
import com.github.emailtohl.lib.jpa.EntityBase;
import com.github.emailtohl.lib.jpa.Paging;

/**
 * 文章服务实现
 * @author HeLei
 */
@Service
@Transactional
public class ArticleServiceImpl extends StandardService<Article> implements ArticleService {
	private static final Pattern IMG_PATTERN = Pattern.compile("<img\\b[^<>]*?\\bsrc[\\s\\t\\r\\n]*=[\\s\\t\\r\\n]*[\"\"']?[\\s\\t\\r\\n]*(?<imgUrl>[^\\s\\t\\r\\n\"\"'<>]*)[^<>]*?/?[\\s\\t\\r\\n]*>");
	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "article_cache";
	public static final String CACHE_ALL = "article_cache_all";
	
	ArticleRepository articleRepository;
	CommentRepository commentRepository;
	TypeRepository typeRepository;
	UserService userService;
	CustomerService customerService;
	EmployeeService employeeService;
	CorePresetData presetData;
	WebPresetData webPresetData;
	
	@Inject
	public ArticleServiceImpl(ArticleRepository articleRepository, CommentRepository commentRepository,
			TypeRepository typeRepository, UserService userService, CustomerService customerService,
			EmployeeService employeeService, CorePresetData presetData, WebPresetData webPresetData) {
		super();
		this.articleRepository = articleRepository;
		this.commentRepository = commentRepository;
		this.typeRepository = typeRepository;
		this.userService = userService;
		this.customerService = customerService;
		this.employeeService = employeeService;
		this.presetData = presetData;
		this.webPresetData = webPresetData;
	}

	@CacheEvict(value = CACHE_ALL)
	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Article create(Article entity) {
		// 实际上这里就可以校验body与title是必填项
		validate(entity);
		String body = entity.getBody();
		// 若没有设置封面，则将第一幅图作为封面
		if (!hasText(entity.getCover()) && hasText(body)) {
			Matcher m = IMG_PATTERN.matcher(entity.getBody());
			if (m.find()) {
				entity.setCover(m.group(1));
			}
		}
		// 若没有摘要，则选取前50字
		String summary = entity.getSummary();
		if (!hasText(summary) && hasText(body)) {
			int size = body.length();
			if (size > 50) {
				summary = body.substring(0, 50) + "……";
			} else {
				summary = body;
			}
			entity.setSummary(summary.replaceAll(IMG_PATTERN.pattern(), ""));
		}
		Long userId = CURRENT_USER_ID.get();
		UserRef userRef;
		if (userId == null) {
			userRef = presetData.user_anonymous.getCustomerRef();
		} else {
			userRef = userService.getRef(userId);
		}
		if (userRef == null) {
			userRef = presetData.user_anonymous.getCustomerRef();
		}
		entity.setAuthor(userRef);
		Type type = null;
		if (entity.getType() != null) {
			if (entity.getType().getId() != null) {
				type = typeRepository.findById(entity.getType().getId()).orElse(null);
			} else if (hasText(entity.getType().getName())) {
				type = typeRepository.getByName(entity.getType().getName());
			}
		}
		if (type == null) {
			type = typeRepository.findById(webPresetData.unclassified.getId()).orElse(null);
		}
		if (type != null) {
			entity.setType(type);
			type.getArticles().add(entity);
		}
		return transientDetail(articleRepository.save(entity));
	}

	@Override
	public boolean exist(Object matcherValue) {
		return true;
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article get(Long id) {
		Article a = articleRepository.find(id);
		return transientDetail(a);
	}

	@Override
	public Paging<Article> search(String query, Pageable pageable) {
		Page<Article> page;
		if (hasText(query)) {
			page = articleRepository.search(query, pageable);
		} else {
			page = articleRepository.findAll(pageable);
		}
		List<Article> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}
	
	@Override
	public Paging<Article> query(Article params, Pageable pageable) {
		Page<Article> page = articleRepository.queryForPage(params, pageable);
		List<Article> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<Article> query(Article params) {
		return articleRepository.queryForList(params).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@Override
	public Map<Long, Long> getCommentNumbers(Collection<Long> articleIds) {
		return articleRepository.getCommentNumbers(articleIds);
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article update(Long id, Article newEntity) {
		Article a = articleRepository.find(id);
		if (a == null) {
			return null;
		}
		if (hasText(newEntity.getTitle())) {
			a.setTitle(newEntity.getTitle());
		}
		if (hasText(newEntity.getSummary())) {
			a.setSummary(newEntity.getSummary());
		}
		String body = newEntity.getBody();
		if (hasText(body)) {
			a.setBody(body);
		}
		if (hasText(newEntity.getKeywords())) {
			a.setKeywords(newEntity.getKeywords());
		}
		if (hasText(newEntity.getCover())) {
			a.setCover(newEntity.getCover());
		}
		// 若没有设置封面，则将第一幅图作为封面
		body = a.getBody();
		if (!hasText(a.getCover()) && hasText(body)) {
			Matcher m = IMG_PATTERN.matcher(body);
			if (m.find()) {
				a.setCover(m.group(1));
			}
		}
		// 若没有摘要，则选取前50字
		String summary = a.getSummary();
		if (!hasText(summary) && hasText(body)) {
			int size = body.length();
			if (size > 50) {
				summary = body.substring(0, 50) + "……";
			} else {
				summary = body;
			}
			a.setSummary(summary.replaceAll(IMG_PATTERN.pattern(), ""));
		}
		Type type = null;
		if (newEntity.getType() != null) {
			if (newEntity.getType().getId() != null) {
				type = typeRepository.findById(newEntity.getType().getId()).orElse(null);
			} else if (hasText(newEntity.getType().getName())) {
				type = typeRepository.getByName(newEntity.getType().getName());
			}
		}
		if (type != null) {
			a.setType(type);
			type.getArticles().add(newEntity);
		}
		return transientDetail(a);
	}

	@CacheEvict(value = { CACHE_NAME, CACHE_ALL }, allEntries = true)
	@Override
	public void delete(Long id) {
		Article a = articleRepository.find(id);
		if (a == null) {
			return;
		}
		a.getType().getArticles().remove(a); // 文章一定有个类型，所以连续调用
		a.getComments().forEach(c -> c.setArticle(null));
		articleRepository.delete(a);
	}

	@CacheEvict(value = CACHE_ALL)
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article approve(long id, boolean canApproved) {
		Article a = articleRepository.find(id);
		if (a == null) {
			return null;
		}
		a.setCanApproved(canApproved);
		Long userId = CURRENT_USER_ID.get();
		EmployeeRef empRef;
		if (userId == null) {
			empRef = presetData.user_bot.getEmployeeRef();
		} else {
			empRef = employeeService.getRef(userId);
		}
		if (empRef == null) {
			empRef = presetData.user_bot.getEmployeeRef();
		}
		a.setApprover(empRef);
		return transientDetail(a);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article canComment(Long id, boolean canComment) {
		Article a = articleRepository.find(id);
		if (a == null) {
			return null;
		}
		a.setCanComment(canComment);
		return transientDetail(a);
	}
	
	@Cacheable(value = CACHE_ALL)
	@Override
	public Map<Type, List<Article>> articleClassify() {
		// 本系统中article.getType()是一定存在的
		return articleRepository.findAll().stream()
				.limit(100).filter(a -> a.getCanApproved() == null || a.getCanApproved())
				.map(this::toTransient).peek(this::filterCommentOfArticle)
				.collect(Collectors.groupingBy(article -> article.getType()));
	}

	private Pageable zeroToHundred  = PageRequest.of(0, 100, Sort.Direction.DESC, EntityBase.MODIFY_DATE_PROPERTY_NAME);
	@Override
	public List<Article> recentArticles() {
		return articleRepository.findAll(zeroToHundred).getContent()
				.stream()/* .limit(100) */
				.filter(a -> a.getCanApproved() == null || a.getCanApproved()).map(this::toTransient)
				.peek(this::filterCommentOfArticle).collect(Collectors.toList());
	}
	
	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article readArticle(Long id) throws NotAcceptableException {
		Article a = articleRepository.find(id);
		if (a == null) {
			return null;
		}
		if (a.getCanApproved() != null && !a.getCanApproved()) {
			throw new NotAcceptableException("该文章还未审核通过！");
		}
		filterCommentOfArticle(a);
		return transientDetail(a);
	}
	
	@Override
	protected Article toTransient(Article source) {
		if (source == null) {
			return null;
		}
		Article target = new Article();
		BeanUtils.copyProperties(source, target, "author", "approver", "type", "comments", "commentNumbers");
		// BeanUtils不处理is开头的访问器
		target.setCanApproved(source.getCanApproved());
		// 只获取作者必要信息
		target.setAuthor(transientUserRef(source.getAuthor()));
		target.setApprover(transientEmployeeRef(source.getApprover()));
		if (source.getType() != null) {
			Type st = source.getType();
			Type t = new Type();
			t.setId(st.getId());
			t.setCreateDate(st.getCreateDate());
			t.setModifyDate(st.getModifyDate());
			t.setName(st.getName());
			t.setDescription(st.getDescription());
			target.setType(t);
		}
		return target;
	}

	/**
	 * 对于文章来说，只需要展示用户名字，头像等基本信息即可
	 * 注意：本方法将所有评论载入，而不管该评论是否允许开放
	 */
	@Override
	protected Article transientDetail(Article source) {
		if (source == null) {
			return null;
		}
		Article target = new Article();
		BeanUtils.copyProperties(source, target, "author", "approver", "type", "comments", "commentNumbers");
		target.setAuthor(transientUserRef(source.getAuthor()));
		target.setApprover(transientEmployeeRef(source.getApprover()));
		target.setType(getType(source.getType()));
		target.setComments(transientComments(source.getComments()));
		return target;
	}

	protected UserRef transientUserRef(UserRef source) {
		if (source == null) {
			return null;
		}
		UserRef target = new UserRef();
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setCellPhone(source.getCellPhone());
		target.setName(source.getName());
		target.setNickname(source.getNickname());
		target.setIconSrc(source.getIconSrc());
		return target;
	}
	
	protected EmployeeRef transientEmployeeRef(EmployeeRef source) {
		if (source == null) {
			return null;
		}
		EmployeeRef target = new EmployeeRef();
		target.setEmpNum(source.getEmpNum());
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setCellPhone(source.getCellPhone());
		target.setName(source.getName());
		target.setNickname(source.getNickname());
		target.setIconSrc(source.getIconSrc());
		return target;
	}
	
	/**
	 * 过滤文章类型
	 * 
	 * @param source
	 * @return
	 */
	protected Type getType(Type source) {
		if (source == null)
			return null;
		Type target = new Type();
		target.setId(source.getId());
		target.setCreateDate(source.getCreateDate());
		target.setModifyDate(source.getModifyDate());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setParent(getType(source.getParent()));
		// 只要文章长度，不加载具体的文章
		int size = source.getArticles().size();
		for (int i = 0; i < size; i++) {
			target.getArticles().add(new Article());
		}
		return target;
	}
	
	protected List<Comment> transientComments(List<Comment> comments) {
		if (comments == null) {
			return null;
		}
		return comments.stream().filter(c -> c.getApproved() == null || c.getApproved()).map(source -> {
			Comment target = new Comment();
			target.setId(source.getId());
			target.setCreateDate(source.getCreateDate());
			target.setModifyDate(source.getModifyDate());
			target.setContent(source.getContent());
			target.setReviewer(transientUserRef(source.getReviewer()));
			target.setApprover(transientEmployeeRef(source.getApprover()));
			target.setApproved(source.getApproved());
			return target;
		}).collect(Collectors.toList());
	}
	
	/**
	 * 过滤文章下的评论，主要用于前端
	 * 
	 * @param article
	 */
	private void filterCommentOfArticle(Article article) {
		if (article.getCanApproved() != null && !article.getCanApproved()) {
			article.getComments().clear();
		} else {
			article.getComments().removeIf(comment -> !comment.getApproved());
		}
	}

}
