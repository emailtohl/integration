package com.github.emailtohl.integration.web.service.flow;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.config.WebPresetData;

@Service
@Transactional
public class ApplyServiceImpl extends StandardService<Apply> {
	@Inject
	WebPresetData webPresetData;
	@Inject
	UserService userService;
	@Inject
	RepositoryService repositoryService;
	@Inject
	RuntimeService runtimeService;
	@Inject
	TaskService taskService;
	@Inject
	HistoryService historyService;
	@Inject
	IdentityService identityService;
	@Inject
	FormService formService;
	
	@Inject
	ApplyRepository applyRepository;
	
	@Override
	public Apply create(Apply entity) {
		validate(entity);
		UserRef applicant = userService.getRef(CURRENT_USER_ID.get());
		if (applicant == null) {
			throw new UsernameNotFoundException("没有此账号");
		}
		entity.setApplicant(applicant);
		entity.setResult(null);
		Apply source = applyRepository.save(entity);
		String businessKey = source.getId().toString();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("apply", businessKey);
		source.setProcessInstanceId(processInstance.getId());
		return transientDetail(source);
	}

	private ExampleMatcher idMatcher = ExampleMatcher.matching().withMatcher("id",
			GenericPropertyMatchers.exact());
	
	@Override
	public boolean exist(Object id) {
		if (!(id instanceof Long)) {
			return false;
		}
		Apply a = new Apply();
		a.setId((Long) id);
		Example<Apply> example = Example.of(a, idMatcher);
		return applyRepository.exists(example);
	}

	@Override
	public Apply get(Long id) {
		return transientDetail(applyRepository.findOne(id));
	}

	private ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("reason",
			GenericPropertyMatchers.ignoreCase())
			.withMatcher("applicant.name", GenericPropertyMatchers.ignoreCase());
	
	
	@Override
	public Paging<Apply> query(Apply params, Pageable pageable) {
		Example<Apply> example = Example.of(params, matcher);
		Page<Apply> p = applyRepository.findAll(example, pageable);
		List<Apply> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<Apply>(ls, pageable, p.getTotalElements());
	}

	@Override
	public List<Apply> query(Apply params) {
		Example<Apply> example = Example.of(params, matcher);
		return applyRepository.findAll(example).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@Override
	public Apply update(Long id, Apply newEntity) {
		validate(newEntity);
		Apply source = applyRepository.findOne(id);
		if (source == null) {
			return null;
		}
		if (hasText(newEntity.getReason())) {
			source.setReason(newEntity.getReason());
		}
		if (hasText(newEntity.getResult())) {
			source.setResult(newEntity.getResult());
		}
		return transientDetail(source);
	}

	@Override
	public void delete(Long id) {
		
	}

	@Override
	protected Apply toTransient(Apply entity) {
		if (entity == null) {
			return null;
		}
		Apply target = new Apply();
		target.setId(entity.getId());
		target.setCreateDate(entity.getCreateDate());
		target.setModifyDate(entity.getModifyDate());
		target.setReason(entity.getReason());
		target.setApplicant(transientUserRef(entity.getApplicant()));
		target.setProcessInstanceId(entity.getProcessInstanceId());
		return target;
	}

	@Override
	protected Apply transientDetail(Apply entity) {
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
}
