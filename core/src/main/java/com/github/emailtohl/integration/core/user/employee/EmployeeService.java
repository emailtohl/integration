package com.github.emailtohl.integration.core.user.employee;

import static com.github.emailtohl.integration.core.config.Constant.*;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_DELETE;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_ENABLED;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.role.Authority.EMPLOYEE_ROLE;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;

/**
 * 平台账号的服务层
 * @author HeLei
 */
public interface EmployeeService {
	/**
	 * 创建一个平台账号
	 * @param entity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "')")
	Employee create(Employee entity);
	
	/**
	 * 查询该手机号码或邮箱是否已使用
	 * @param cellPhoneOrEmail
	 * @return
	 */
	boolean exist(Object cellPhoneOrEmail);
	
	/**
	 * 根据ID获取平台账号
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "') or" + SPEL_MATCH_ID)
	Employee get(@P("id") Long id);

	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的平台账号转瞬态时，同时改变分页对象
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "')")
	Paging<Employee> query(Employee params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "')")
	List<Employee> query(Employee params);

	/**
	 * 修改平台账号基本内容，不包括密码、角色、状态等属性
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该平台账号
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "') or" + SPEL_MATCH_ID)
	Employee update(@P("id") Long id, Employee newEntity) throws NotAcceptableException;

	/**
	 * 根据ID删除平台账号
	 * @param id
	 * @exception NotAcceptableException 不能删除内置账号
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_DELETE + "')")
	void delete(Long id) throws NotAcceptableException;
	
	/**
	 * 全文查询
	 * @param query
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "')")
	Paging<Employee> search(String query, Pageable pageable);
	
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
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "') or" + SPEL_MATCH_EMP_NUM)
	Employee getByEmpNum(@P("empNum") Integer empNum);
	
	/**
	 * 根据姓名查询平台账号
	 * @param name 真实姓名
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE + "')")
	List<Employee> findByName(String name);
	
	/**
	 * 为平台账号授予角色
	 * @param id
	 * @param roleNames
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
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
	@PreAuthorize(SPEL_MATCH_EMP_NUM)
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
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize("hasAuthority('" + EMPLOYEE_ENABLED + "')")
	Employee enabled(Long id, boolean enabled);
	
	/**
	 * 为平台账号设置公钥
	 * @param id
	 * @param publicKey
	 * @return
	 */
	Employee setPublicKey(Long id, String publicKey);

	/**
	 * 获取平台账号引用
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	EmployeeRef getRef(Long id);
	
	/**
	 * 通过工号获取平台账号引用
	 * @param empNum
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	EmployeeRef findRefByEmpNum(Integer empNum);
	
	/**
	 * 查找平台账号引用
	 * @param params
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<EmployeeRef> queryRef(EmployeeRef params, Pageable pageable);
	
	/**
	 * 查找平台账号引用
	 * @param params
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<EmployeeRef> queryRef(EmployeeRef params);
	
	/**
	 * 账号过期的维护
	 */
	@Scheduled(fixedRate = 3600 * 24/*每天执行一次*/)
	void accountStatus();
}
