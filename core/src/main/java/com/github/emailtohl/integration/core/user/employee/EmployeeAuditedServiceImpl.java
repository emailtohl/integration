package com.github.emailtohl.integration.core.user.employee;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.lib.jpa.AuditedRepository.Snapshoot;

/**
 * 审计平台账号的历史记录
 * 
 * @author HeLei
 */
@Transactional
@Service
public class EmployeeAuditedServiceImpl implements EmployeeAuditedService {
	@Inject
	EmployeeAudit employeeAudit;

	@Override
	public List<Snapshoot<Employee>> getEmployeeRevision(Long id) {
		List<Snapshoot<Employee>> ls = employeeAudit.getRevisions(id);
		return ls.stream().map(ss -> {
			return new Snapshoot<Employee>(toTransient(ss.entity), transientRevisionEntity(ss.defaultRevisionEntity),
					ss.revisionType);
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
		BeanUtils.copyProperties(source, target, "department", "password", "roles");
		if (source.getDepartment() != null) {// 懒加载
			Department d = new Department();
			d.setName(source.getDepartment().getName());
			target.setDepartment(d);
		}
		return target;
	}

	private Employee transientDetail(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "department", "password", "roles");
		if (source.getDepartment() != null) {// 懒加载
			Department d = new Department();
			d.setName(source.getDepartment().getName());
			target.setDepartment(d);
		}
		source.getRoles().forEach(
				role -> target.getRoles().add(new Role(role.getName(), role.getRoleType(), role.getDescription())));
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
