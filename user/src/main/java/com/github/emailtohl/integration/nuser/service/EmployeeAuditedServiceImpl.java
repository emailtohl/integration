package com.github.emailtohl.integration.nuser.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.nuser.dao.EmployeeAudit;
import com.github.emailtohl.integration.nuser.entities.Employee;
/**
 * 查询被审计的实体的历史记录
 * @author HeLei
 * @date 2017.02.04
 */
@Transactional
@Service
public class EmployeeAuditedServiceImpl implements EmployeeAuditedService {
	@Inject EmployeeAudit employeeAudit;

	@Override
	public List<Tuple<Employee>> getEmployeeRevision(Long id) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("id", id);
		return employeeAudit.getAllRevisionInfo(propertyNameValueMap);
	}

	@Override
	public Employee getEmployeeAtRevision(Long id, Number revision) {
		return null;
	}

	@Override
	public void rollback(Long id, Number revision) {
		// TODO Auto-generated method stub
		
	}
}
