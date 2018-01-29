package com.github.emailtohl.integration.web.controller;

import java.io.Serializable;
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
	
	@RequestMapping(value = "exist", method = RequestMethod.GET)
	public String exist(@RequestParam(name = "cellPhoneOrEmail") String cellPhoneOrEmail) {
		return String.format("{\"exist\":%b}", employeeService.exist(cellPhoneOrEmail));
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
		p.getContent().forEach(this::filter);
		return p;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<Employee> query(Employee params) {
		List<Employee> ls = employeeService.query(params);
		ls.forEach(this::filter);
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
		p.getContent().forEach(this::filter);
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
	
	/**
	 * jackson 解析内部类必须是static的，且根据getter、setter来识别属性
	 * @author HeLei
	 */
	static class Form implements Serializable {
		private static final long serialVersionUID = 5619878770794183688L;
		Long id;
		String[] roleNames;
		Integer empNum;
		String oldPassword;
		String newPassword;
		Boolean enabled;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String[] getRoleNames() {
			return roleNames;
		}
		public void setRoleNames(String[] roleNames) {
			this.roleNames = roleNames;
		}
		public Integer getEmpNum() {
			return empNum;
		}
		public void setEmpNum(Integer empNum) {
			this.empNum = empNum;
		}
		public String getOldPassword() {
			return oldPassword;
		}
		public void setOldPassword(String oldPassword) {
			this.oldPassword = oldPassword;
		}
		public String getNewPassword() {
			return newPassword;
		}
		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}
		public Boolean getEnabled() {
			return enabled;
		}
		public void setEnabled(Boolean enabled) {
			this.enabled = enabled;
		}
	}

}


