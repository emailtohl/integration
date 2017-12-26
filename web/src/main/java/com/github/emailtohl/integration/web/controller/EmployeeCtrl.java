package com.github.emailtohl.integration.web.controller;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 平台账号的控制器
 * @author HeLei
 */
@RestController
@RequestMapping(value = "employee", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EmployeeCtrl extends RestCtrl<Employee> {
	@Inject
	EmployeeService employeeService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public Employee create(@RequestBody @Valid Employee entity, Errors errors) {
		checkErrors(errors);
		return filter(employeeService.create(entity));
	}
	
	@RequestMapping(value = "exist/{empNum}", method = RequestMethod.GET)
	public String exist(@PathVariable("empNum") Integer empNum) {
		return String.format("{\"exist\":%b}", employeeService.exist(empNum));
	}
	
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public Employee get(@PathVariable("id") Long id) {
		Employee e = employeeService.get(id);
		mustExist(e);
		return filter(e);
	}

	@RequestMapping(value = "page", method = RequestMethod.GET)
	public Paging<Employee> query(Employee params,
			@PageableDefault(page = 0, size = 10, sort = { BaseEntity.ID_PROPERTY_NAME,
					BaseEntity.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		Paging<Employee> p = employeeService.query(params, pageable);
		p.getContent().stream().peek(this::filter);
		return p;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<Employee> query(Employee params) {
		List<Employee> ls = employeeService.query(params);
		ls.stream().peek(this::filter);
		return ls;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(@PathVariable("id") Long id, @RequestBody @Valid Employee newEntity, Errors errors) {
		checkErrors(errors);
		employeeService.update(id, newEntity);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") Long id) {
		employeeService.delete(id);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	public Paging<Employee> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
			@PageableDefault(page = 0, size = 10, sort = { BaseEntity.ID_PROPERTY_NAME,
					BaseEntity.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		Paging<Employee> p = employeeService.search(query, pageable);
		p.getContent().stream().peek(this::filter);
		return p;
	}
	
	@RequestMapping(value = "empNum/{empNum}", method = RequestMethod.GET)
	public ResponseEntity<Employee> getByEmpNum(@PathVariable("empNum") Integer empNum) {
		Employee e = employeeService.getByEmpNum(empNum);
		if (e == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(filter(e), HttpStatus.OK);
	}

	@RequestMapping(value = "grandRoles", method = RequestMethod.POST)
	public void grandRoles(@RequestBody Form form) {
		employeeService.grandRoles(form.id, form.roleNames);
	}

	@RequestMapping(value = "updatePassword", method = RequestMethod.POST)
	public ExecResult updatePassword(@RequestBody Form form) {
		return employeeService.updatePassword(form.empNum, form.oldPassword, form.newPassword);
	}

	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	public ExecResult resetPassword(@RequestBody Form form) {
		return employeeService.resetPassword(form.id);
	}
	
	@RequestMapping(value = "enabled", method = RequestMethod.POST)
	public Employee enabled(@RequestBody Form form) {
		return filter(employeeService.enabled(form.id, form.enabled));
	}
	
	public void setEmployeeService(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	private Employee filter(Employee e) {
		if (e.getImage() != null) {
			e.getImage().setBin(null);
		}
		return e;
	}
	
	class Form {
		Long id;
		String[] roleNames;
		Integer empNum;
		String oldPassword;
		String newPassword;
		Boolean enabled;
	}

}


