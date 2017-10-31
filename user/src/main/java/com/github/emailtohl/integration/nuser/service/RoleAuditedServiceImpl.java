package com.github.emailtohl.integration.nuser.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.nuser.dao.RoleAudit;
import com.github.emailtohl.integration.nuser.entities.Role;
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
		return roleAudit.getAllRevisionInfo(propertyNameValueMap);
	}

	@Override
	public Role getRoleAtRevision(Long id, Number revision) {
		return roleAudit.getEntityAtRevision(id, revision);
	}

	@Override
	public void rollback(Long id, Number revision) {
		roleAudit.rollback(id, revision);
	}

}
