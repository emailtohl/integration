package com.github.emailtohl.integration.core.user.employee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Employee;
/**
 * 审计平台账号的历史记录
 * @author HeLei
 */
@Transactional
@Service
public class EmployeeAuditedServiceImpl implements EmployeeAuditedService {
	@Inject EmployeeAudit employeeAudit;

	@Override
	public List<Tuple<Employee>> getEmployeeRevision(Long id) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("id", id);
		List<Tuple<Employee>> ls = employeeAudit.getAllRevisionInfo(propertyNameValueMap);
		return ls.stream().map(t -> {
			Tuple<Employee> n = new Tuple<Employee>();
			n.setDefaultRevisionEntity(t.getDefaultRevisionEntity());
			n.setRevisionType(t.getRevisionType());
			n.setEntity(toTransient(t.getEntity()));
			return n;
		}).collect(Collectors.toList());
	}

	@Override
	public Employee getEmployeeAtRevision(Long id, Number revision) {
		return transientDetail(employeeAudit.getEntityAtRevision(id, revision));
	}
	
	private Employee toTransient(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "password", "roles");
		return target;
	}
	
	private Employee transientDetail(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "password", "roles");
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getDescription())));
		return target;
	}
}
