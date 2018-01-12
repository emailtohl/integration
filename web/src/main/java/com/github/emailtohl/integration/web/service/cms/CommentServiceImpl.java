package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.FormService;
import org.activiti.engine.TaskService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;

/**
 * 文章评论服务实现
 * 
 * @author HeLei
 */
@Service
@Transactional
public class CommentServiceImpl extends StandardService<Comment> {
	CommentRepository commentRepository;
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
	public CommentServiceImpl(CommentRepository commentRepository, UserService userService,
			EmployeeService employeeService, FormService formService, TaskService taskService,
			CorePresetData presetData) {
		super();
		this.commentRepository = commentRepository;
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
	public Paging<Comment> query(Comment params, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Comment> query(Comment params) {
		// TODO Auto-generated method stub
		return null;
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Comment update(Long id, Comment newEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Comment toTransient(Comment entity) {
		if (entity == null) {
			return null;
		}
		return null;
	}

	@Override
	protected Comment transientDetail(Comment entity) {
		if (entity == null) {
			return null;
		}
		return null;
	}

}
