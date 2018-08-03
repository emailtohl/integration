package com.github.emailtohl.integration.web.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleAuditedService;
import com.github.emailtohl.integration.core.user.customer.CustomerAuditedService;
import com.github.emailtohl.integration.core.user.employee.EmployeeAuditedService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.lib.jpa.AuditedRepository.Tuple;
import com.google.gson.Gson;
/**
 * 查阅Hibernate Envers产生的审计记录
 * 
 * @author HeLei
 */
@RestController
@RequestMapping(value = "audit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AuditCtrl {
	@Inject
	CustomerAuditedService customerAuditedService;
	@Inject
	EmployeeAuditedService employeeAuditedService;
	@Inject
	RoleAuditedService roleAuditedService;
	@Inject
	Gson gson;
	
	/**
	 * 查询角色所有的历史记录
	 * @param id 角色
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	@RequestMapping(value = "role/{id}", method = RequestMethod.GET)
	public List<Tuple<Role>> getRoleRevision(@PathVariable("id") Long id) {
		List<Tuple<Role>> ls =  roleAuditedService.getRoleRevision(id);
		return ls;
	}
	
	/**
	 * 查询角色在某个修订版时的历史记录
	 * @param id 角色id
	 * @param revision 版本号，通过AuditReader#getRevisions(Employee.class, ID)获得
	 * @return
	 */
	@RequestMapping(value = "role/{id}/revision/{revision}", method = RequestMethod.GET)
	public Role getRoleAtRevision(@PathVariable("id") Long id, @PathVariable("revision") Integer revision) {
		return roleAuditedService.getRoleAtRevision(id, revision);
	}
	
	/**
	 * 查询客户所有的历史记录
	 * @param id 平台账号id
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	@RequestMapping(value = "customer/{id}", method = RequestMethod.GET)
	public List<Tuple<Customer>> getCustomerRevision(@PathVariable("id") Long id) {
		return customerAuditedService.getCustomerRevision(id);
	}
	
	/**
	 * 查询客户在某个修订版时的历史记录
	 * @param id 客户的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Customer.class, ID)获得
	 * @return
	 */
	@RequestMapping(value = "customer/{id}/revision/{revision}", method = RequestMethod.GET)
	public Customer getCustomerAtRevision(@PathVariable("id") Long id, @PathVariable("revision") Integer revision) {
		return customerAuditedService.getCustomerAtRevision(id, revision);
	}
	
	/**
	 * 查询平台账号所有的历史记录
	 * @param id 平台账号id
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	@RequestMapping(value = "employee/{id}", method = RequestMethod.GET)
	public List<Tuple<Employee>> getEmployeeRevision(@PathVariable("id") Long id) {
		return employeeAuditedService.getEmployeeRevision(id);
	}
	
	/**
	 * 查询平台账号在某个修订版时的历史记录
	 * @param id 平台账号的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Employee.class, ID)获得
	 * @return
	 */
	@RequestMapping(value = "employee/{id}/revision/{revision}", method = RequestMethod.GET)
	public Employee getEmployeeAtRevision(@PathVariable("id") Long id, @PathVariable("revision") Integer revision) {
		return employeeAuditedService.getEmployeeAtRevision(id, revision);
	}

	public AuditCtrl() {}

	public AuditCtrl(CustomerAuditedService customerAuditedService, EmployeeAuditedService employeeAuditedService,
			RoleAuditedService roleAuditedService) {
		this.customerAuditedService = customerAuditedService;
		this.employeeAuditedService = employeeAuditedService;
		this.roleAuditedService = roleAuditedService;
	}
	
}
