package com.github.emailtohl.integration.core.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
/**
 * 审计角色的历史记录
 * @author HeLei
 */
@Transactional
@Service
public class RoleAuditedServiceImpl implements RoleAuditedService {
	@Inject RoleAudit roleAudit;
	
	@Override
	public List<Tuple<Role>> getRoleRevision(Long id) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("id", id);
		List<Tuple<Role>> ls = roleAudit.getAllRevisionInfo(propertyNameValueMap);
		return ls.stream().map(t -> {
			Tuple<Role> n = new Tuple<Role>();
			n.setDefaultRevisionEntity(transientRevisionEntity(t.getDefaultRevisionEntity()));
			n.setRevisionType(t.getRevisionType());
			n.setEntity(toTransient(t.getEntity()));
			return n;
		}).collect(Collectors.toList());
	}

	@Override
	public Role getRoleAtRevision(Long id, Number revision) {
		return toTransient(roleAudit.getEntityAtRevision(id, revision));
	}

	private Role toTransient(Role source) {
		if (source == null) {
			return source;
		}
		Role target = new Role();
		BeanUtils.copyProperties(source, target, "users", "authorities");
		source.getAuthorities().forEach(a -> target.getAuthorities().add(new Authority(a.getName(), a.getDescription(), null)));
		return target;
	}
	
	private DefaultRevisionEntity transientRevisionEntity(DefaultRevisionEntity re) {
		if (re == null) {
			return null;
		}
		DefaultRevisionEntity n = new DefaultRevisionEntity();
		n.setId(re.getId());
		n.setTimestamp(re.getTimestamp());
		return n;
	}
}
