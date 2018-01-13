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

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;

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
	public Comment create(Comment entity) {
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
		if (entity.getArticle() != null && entity.getArticle().getId() != null) {
			Article a = articleRepository.findOne(entity.getArticle().getId());
			if (a != null) {
				entity.setArticle(a);
			}
		}
		if (entity.getComment() != null && entity.getComment().getId() != null) {
			Comment targetComment = commentRepository.get(entity.getComment().getId());
			if (targetComment != null) {
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
		return transientDetail(commentRepository.get(id));
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
		Comment c = commentRepository.findOne(id);
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
		Comment c = commentRepository.findOne(id);
		if (c == null) {
			return;
		}
		if (c.getArticle() != null) {
			c.getArticle().getComments().remove(c);
		}
		commentRepository.findByCommentId(id).forEach(otherComment -> otherComment.setComment(null));
		commentRepository.delete(c);
	}

	@Override
	public Comment approve(long id, boolean approved) {
		Comment c = commentRepository.findOne(id);
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
		target.setCreateDate(entity.getCreateDate());
		target.setModifyDate(entity.getModifyDate());
		target.setContent(entity.getContent());
		target.setApproved(entity.getApproved());
		target.setApprover(transientEmployeeRef(entity.getApprover()));
		target.setReviewer(transientUserRef(entity.getReviewer()));
		return target;
	}

	@Override
	protected Comment transientDetail(Comment entity) {
		return toTransient(entity);
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

}
