package com.github.emailtohl.integration.web.service.cms;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.FormService;
import org.activiti.engine.TaskService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.github.emailtohl.lib.exception.NotAcceptableException;
import com.github.emailtohl.lib.jpa.Paging;

/**
 * 文章评论服务实现
 * 
 * @author HeLei
 */
@Service
@Transactional
public class CommentServiceImpl extends StandardService<Comment> implements CommentService {
	CommentRepository commentRepository;
	ArticleRepository articleRepository;
	UserService userService;
	EmployeeService employeeService;
	FormService formService;
	TaskService taskService;
	CorePresetData presetData;
	
	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "article_comment_cache";
	
	@Inject
	public CommentServiceImpl(CommentRepository commentRepository, ArticleRepository articleRepository,
			UserService userService, EmployeeService employeeService, FormService formService, TaskService taskService,
			CorePresetData presetData) {
		super();
		this.commentRepository = commentRepository;
		this.articleRepository = articleRepository;
		this.userService = userService;
		this.employeeService = employeeService;
		this.formService = formService;
		this.taskService = taskService;
		this.presetData = presetData;
	}

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Comment create(Comment entity) throws NotAcceptableException {
		validate(entity);
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
		entity.setReviewer(userRef);
		// 评论总是有关联的，要么是与文章，要么是与评论
		if (entity.getArticle() != null && entity.getArticle().getId() != null) {
			Article a = articleRepository.find(entity.getArticle().getId());
			if (a != null) {
				if (a.getCanComment() != null && !a.getCanComment()) {
					throw new NotAcceptableException(a.getTitle() + "关闭了评论");
				}
				entity.setArticle(a);
			}
		}
		if (entity.getComment() != null && entity.getComment().getId() != null) {
			Comment targetComment = commentRepository.find(entity.getComment().getId());
			if (targetComment != null) {
				if (targetComment.getCanComment() != null && !targetComment.getCanComment()) {
					throw new NotAcceptableException("评论被关闭");
				}
				entity.setComment(targetComment);
			}
		}
		return transientDetail(commentRepository.save(entity));
	}

	@Override
	public boolean exist(Object matcherValue) {
		return true;
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Comment get(Long id) {
		return transientDetail(commentRepository.find(id));
	}

	@Override
	public Paging<Comment> search(String query, Pageable pageable) {
		Page<Comment> page;
		if (!hasText(query)) {
			page = commentRepository.findAll(pageable);
		} else {
			page = commentRepository.search(query, pageable);
		}
		List<Comment> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}
	
	private final ExampleMatcher commentMatcher = ExampleMatcher
			.matching()
			.withIgnoreNullValues()
			.withIgnorePaths("isApproved", "approver")
			.withMatcher("id", GenericPropertyMatchers.exact())
			.withMatcher("content", GenericPropertyMatchers.ignoreCase())
			.withMatcher("reviewer.id", GenericPropertyMatchers.exact())
			.withMatcher("reviewer.name", GenericPropertyMatchers.ignoreCase())
			.withMatcher("reviewer.nickname", GenericPropertyMatchers.exact())
			.withMatcher("reviewer.email", GenericPropertyMatchers.exact())
			.withMatcher("reviewer.cellPhone", GenericPropertyMatchers.exact())
			.withMatcher("approver.id", GenericPropertyMatchers.exact())
			.withMatcher("approver.name", GenericPropertyMatchers.ignoreCase())
			.withMatcher("approver.nickname", GenericPropertyMatchers.exact())
			.withMatcher("approver.email", GenericPropertyMatchers.exact())
			.withMatcher("approver.cellPhone", GenericPropertyMatchers.exact())
			;
	@Override
	public Paging<Comment> query(Comment params, Pageable pageable) {
		Page<Comment> page;
		if (params == null) {
			page = commentRepository.findAll(pageable);
		} else {
			Example<Comment> example = Example.of(params, commentMatcher);
			page = commentRepository.findAll(example, pageable);
		}
		List<Comment> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<Comment> query(Comment params) {
		List<Comment> ls;
		if (params == null) {
			ls = commentRepository.findAll();
		} else {
			Example<Comment> example = Example.of(params, commentMatcher);
			ls = commentRepository.findAll(example);
		}
		return ls.stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Comment update(Long id, Comment newEntity) {
		Comment c = commentRepository.find(id);
		if (c == null) {
			return null;
		}
		if (hasText(newEntity.getContent())) {
			c.setContent(newEntity.getContent());
		}
		return transientDetail(c);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Comment c = commentRepository.find(id);
		if (c == null) {
			return;
		}
		if (c.getArticle() != null) {
			c.getArticle().getComments().remove(c);
		}
		commentRepository.findByCommentId(id).forEach(otherComment -> otherComment.setComment(null));
		commentRepository.delete(c);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Comment approve(long id, boolean approved) {
		Comment c = commentRepository.find(id);
		if (c == null) {
			return null;
		}
		c.setApproved(approved);
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
		c.setApprover(empRef);
		return transientDetail(c);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Comment canComment(Long id, boolean canComment) {
		Comment c = commentRepository.find(id);
		if (c == null) {
			return null;
		}
		c.setCanComment(canComment);
		return transientDetail(c);
	}
	
	@Override
	public List<Comment> recentComments() {
		return commentRepository.findAll().stream().limit(20).filter(pc -> pc.getApproved() == null || pc.getApproved())
				.collect(Collectors.toList());
	}
	
	@Override
	protected Comment toTransient(Comment entity) {
		if (entity == null) {
			return null;
		}
		Comment target = new Comment();
		target.setId(entity.getId());
		target.setCreateTime(entity.getCreateTime());
		target.setModifyTime(entity.getModifyTime());
		target.setContent(entity.getContent());
		target.setApproved(entity.getApproved());
		target.setApprover(transientEmployeeRef(entity.getApprover()));
		target.setReviewer(transientUserRef(entity.getReviewer()));
		target.setArticle(keepArticleTitle(entity.getArticle()));
		target.setComment(keepCommentContent(entity.getComment()));
		return target;
	}

	@Override
	protected Comment transientDetail(Comment entity) {
		if (entity == null) {
			return null;
		}
		Comment target = new Comment();
		target.setId(entity.getId());
		target.setCreateTime(entity.getCreateTime());
		target.setModifyTime(entity.getModifyTime());
		target.setContent(entity.getContent());
		target.setApproved(entity.getApproved());
		target.setApprover(transientEmployeeRef(entity.getApprover()));
		target.setReviewer(transientUserRef(entity.getReviewer()));
		target.setArticle(transientArticle(entity.getArticle()));
		target.setComment(keepCommentContent(entity.getComment()));
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

	protected Article transientArticle(Article source) {
		if (source == null) {
			return null;
		}
		Article target = new Article(source.getTitle(), source.getKeywords(), source.getBody(), source.getSummary());
		target.setId(source.getId());
		target.setCreateTime(source.getCreateTime());
		target.setModifyTime(source.getModifyTime());
		target.setAuthor(transientUserRef(source.getAuthor()));
		target.setApprover(transientEmployeeRef(source.getApprover()));
		if (source.getType() != null) {
			Type st = source.getType();
			Type t = new Type();
			t.setId(st.getId());
			t.setCreateTime(st.getCreateTime());
			t.setModifyTime(st.getModifyTime());
			t.setName(st.getName());
			t.setDescription(st.getDescription());
			target.setType(t);
		}
		return target;
	}
	
	/**
	 * 只保留文章的标题，用于列表
	 * @param source
	 * @return
	 */
	protected Article keepArticleTitle(Article source) {
		if (source == null) {
			return null;
		}
		Article target = new Article(source.getTitle(), source.getKeywords(), null, null);
		target.setId(source.getId());
		target.setCreateTime(source.getCreateTime());
		target.setModifyTime(source.getModifyTime());
		return target;
	}
	
	/**
	 * 只保留评论的内容
	 * @param source
	 * @return
	 */
	protected Comment keepCommentContent(Comment source) {
		if (source == null) {
			return null;
		}
		Comment target = new Comment();
		target.setId(source.getId());
		target.setCreateTime(source.getCreateTime());
		target.setModifyTime(source.getModifyTime());
		target.setContent(source.getContent());
		target.setReviewer(transientUserRef(source.getReviewer()));
		return target;
	}
}
