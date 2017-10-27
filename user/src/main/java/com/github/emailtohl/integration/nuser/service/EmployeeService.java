package com.github.emailtohl.integration.nuser.service;

import static com.github.emailtohl.integration.nuser.entities.Authority.*;

import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.common.standard.StandardService;
import com.github.emailtohl.integration.nuser.entities.Employee;

/**
 * 内部用户的服务层
 * @author HeLei
 */
@Validated
public interface EmployeeService extends StandardService<Employee> {
	
	/**
	 * 内部用户登录
	 * @param empNum
	 * @param password
	 * @return
	 */
	@NotNull ExecResult login(Integer empNum, String password);
	
	/**
	 * 通过工号获取
	 * @param empNum
	 * @return
	 */
	Employee getByEmpNum(Integer empNum);
	
	/**
	 * 为内部人员授予角色
	 * @param id
	 * @param roleNames
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_ROLE + "')")
	Employee grandRoles(Long id, String... roleNames);
	
	/**
	 * 更新内部人员的密码
	 * @param id
	 * @param oldPassword
	 * @param newPassword
	 * @return ExecResult
	 */
	@NotNull ExecResult updatePassword(Long id, String oldPassword, String newPassword);
	
	/**
	 * 重置密码，用于忘记密码无法恢复时
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_RESET_PASSWORD + "')")
	@NotNull ExecResult resetPassword(Long id);
	
	/**
	 * 是否锁定该内部人员的账号
	 * @param id
	 * @param lock
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_LOCK + "')")
	Employee lock(Long id, boolean lock);
}
