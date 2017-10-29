package com.github.emailtohl.integration.nuser.service;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.common.utils.BeanUtil;
import com.github.emailtohl.integration.nuser.dao.RoleAudit;
import com.github.emailtohl.integration.nuser.dao.UserAudit;
import com.github.emailtohl.integration.nuser.entities.Role;
import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 查询被审计的实体的历史记录
 * @author HeLei
 * @date 2017.02.04
 */
@Transactional
@Service
public class AuditedServiceImpl implements AuditedService {
	@Inject UserAudit userAudit;
	@Inject RoleAudit roleAudit;
	@Override
	public Page<User> getUserRevision(Long id, Pageable pageable) {
		return null;
	}
	@Override
	public Page<User> getUsersAtRevision(int revision, Long id, Pageable pageable) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("id", id);
		Page<Tuple<User>> p = userAudit.getEntityRevision(propertyNameValueMap, pageable);
		List<User> ls = p.getContent().stream().map(t -> convert(t.getEntity())).collect(toList());
		return new PageImpl<>(ls, pageable, p.getNumberOfElements());
	}
	@Override
	public User getUserAtRevision(Long id, int revision) {
		return convert(userAudit.getEntityAtRevision(id, revision));
	}
	@Override
	public Page<Role> getRoleRevision(String name, Pageable pageable) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("name", name);
		Page<Tuple<Role>> p = roleAudit.getEntityRevision(propertyNameValueMap, pageable);
		List<Role> ls = p.getContent().stream().map(t -> t.getEntity()).collect(toList());
		return new PageImpl<>(ls, pageable, p.getNumberOfElements());
	}
	@Override
	public Page<Role> getRolesAtRevision(int revision, String name, Pageable pageable) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("name", name);
		return roleAudit.getEntitiesAtRevision(revision, propertyNameValueMap, pageable);
	}
	@Override
	public Role getRoleAtRevision(long roleId, int revision) {
		return roleAudit.getEntityAtRevision(roleId, revision);
	}

	private User convert(User src) {
		User target = BeanUtil.deepCopy(src);
		target.setPassword(null);
		return target;
	}
	
}
