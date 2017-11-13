package com.github.emailtohl.integration.core.user.service;

import static com.github.emailtohl.integration.core.user.entities.Authority.EMPLOYEE_ENABLED;
import static com.github.emailtohl.integration.core.user.entities.Authority.EMPLOYEE_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.user.entities.Authority.EMPLOYEE_ROLE;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.core.standard.ExecResult;
import com.github.emailtohl.integration.core.standard.StandardService;
import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 平台账号的服务层
 * @author HeLei
 */
@Validated
@PreAuthorize("isAuthenticated()")
public interface EmployeeService extends StandardService<Employee> {
	
	/**
	 * 平台账号登录
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
	 * 根据姓名查询平台账号
	 * @param name 真实姓名
	 * @return
	 */
	List<Employee> findByName(String name);
	
	/**
	 * 为平台账号授予角色
	 * @param id
	 * @param roleNames
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_ROLE + "')")
	Employee grandRoles(Long id, String... roleNames);
	
	/**
	 * 更新平台账号的密码
	 * @param empNum
	 * @param oldPassword
	 * @param newPassword
	 * @return ExecResult
	 */
	@PreAuthorize("#empNum.toString() matches authentication.principal.username")
	@NotNull ExecResult updatePassword(@P("empNum") Integer empNum, String oldPassword, String newPassword);
	
	/**
	 * 重置密码，用于忘记密码无法恢复时
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_RESET_PASSWORD + "')")
	@NotNull ExecResult resetPassword(Long id);
	
	/**
	 * 是否锁定该平台账号
	 * @param id
	 * @param enabled
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_ENABLED + "')")
	Employee enabled(Long id, boolean enabled);
	
	/**
	 * 账号过期的维护
	 */
	@Scheduled(fixedRate = 3600 * 24/*每天执行一次*/)
	void accountStatus();
}
