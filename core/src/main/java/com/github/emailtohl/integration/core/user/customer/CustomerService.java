package com.github.emailtohl.integration.core.user.customer;

import static com.github.emailtohl.integration.core.config.Constant.SPEL_MATCH_CELL_PHONE_OR_EMAIL;
import static com.github.emailtohl.integration.core.config.Constant.SPEL_MATCH_ID;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_DELETE;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_ENABLED;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_LEVEL;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_ROLE;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.user.entities.Card;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;

/**
 * 客户的服务层
 * @author HeLei
 */
public interface CustomerService {
	/**
	 * 创建一个用户
	 * @param entity
	 * @return
	 */
	Customer create(Customer entity);
	
	/**
	 * 根据手机号或邮箱查找是否已存在
	 * @param matcherValue
	 * @return
	 */
	boolean exist(Object matcherValue);
	
	/**
	 * 根据ID获取用户
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER + "') or" + SPEL_MATCH_ID)
	Customer get(@P("id") Long id);

	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的用户转瞬态时，同时改变分页对象
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER + "')")
	Paging<Customer> query(Customer params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER + "')")
	List<Customer> query(Customer params);

	/**
	 * 修改用户的基本内容，不包括密码、角色、状态等属性
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该用户
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER + "') or" + SPEL_MATCH_ID)
	Customer update(@P("id") Long id, Customer newEntity);

	/**
	 * 根据ID删除用户
	 * @param id
	 * @exception NotAcceptableException 不能删除内置账号
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_DELETE + "')")
	void delete(Long id);
	/**
	 * 全文查询
	 * @param query
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER + "')")
	Paging<Customer> search(String query, Pageable pageable);
	/**
	 * 客户登录
	 * @param username
	 * @param password
	 * @return
	 */
	@NotNull ExecResult login(String username, String password);
	
	/**
	 * 通过手机号码或者邮箱查找客户
	 * @param username 邮箱或手机号
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER + "') or" + SPEL_MATCH_CELL_PHONE_OR_EMAIL)
	Customer findByUsername(@P("username") String username);
	
	/**
	 * 为客户授予角色
	 * @param id
	 * @param roleNames
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_ROLE + "')")
	Customer grandRoles(Long id, String... roleNames);
	
	/**
	 * 为客户提升等级的权限
	 * @param id
	 * @param roleNames
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_LEVEL + "')")
	Customer grandLevel(Long id, Customer.Level level);
	
	/**
	 * 用户忘记密码时，通过邮箱或短信获取token令牌，以证明是被修改密码的账号拥有者
	 * @param username 邮箱或手机号
	 * @return
	 */
	String getToken(String username);
	
	/**
	 * 更新客户的密码，一般是用户忘记密码后，通过邮箱确认后自行修改，所以不需要输入原密码
	 * @param username 邮箱或手机号
	 * @param newPassword
	 * @param token 通过邮箱或短信或其他方式获取的令牌，该令牌证明确实是该账号拥有者在修改密码
	 * @return ExecResult
	 */
	@PreAuthorize(SPEL_MATCH_CELL_PHONE_OR_EMAIL)
	@NotNull ExecResult updatePassword(@P("username") String username, String newPassword, String token);
	
	/**
	 * 重置密码，用于忘记密码无法恢复时
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_RESET_PASSWORD + "') or" + SPEL_MATCH_ID)
	@NotNull ExecResult resetPassword(@P("id") Long id);
	
	/**
	 * 跟换手机号码
	 * @param id
	 * @param newCellPhone
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	Customer changeCellPhone(Long id, @Pattern(regexp = ConstantPattern.CELL_PHONE) String newCellPhone);
	
	/**
	 * 跟换电子邮箱
	 * @param id
	 * @param newEmail
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	Customer changeEmail(Long id, String newEmail);
	
	/**
	 * 是否锁定该客户的账号
	 * @param id
	 * @param enabled
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_ENABLED + "')")
	Customer enabled(Long id, boolean enabled);
	
	/**
	 * 添加卡
	 * @param id
	 * @param card
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize(SPEL_MATCH_ID)
	Customer addCard(@P("id") Long id, Card card);
	
	/**
	 * 批量更新用户所属的卡
	 * @param id
	 * @param cards
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize(SPEL_MATCH_ID)
	Customer updateCards(@P("id") Long id, Set<Card> cards);
	
	/**
	 * 移除卡
	 * @param id
	 * @param card
	 * @return
	 * @exception NotAcceptableException 不能修改内置账号
	 */
	@PreAuthorize(SPEL_MATCH_ID)
	Customer removeCard(@P("id") Long id, Card card);
	
	/**
	 * 获取客户引用
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	CustomerRef getRef(Long id);
	
	/**
	 * 通过手机或邮箱获取客户引用
	 * @param username 邮箱或手机号
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	CustomerRef findRefByUsername(String username);
	
	/**
	 * 查找客户引用
	 * @param params
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<CustomerRef> queryRef(CustomerRef params, Pageable pageable);
	
	/**
	 * 查找客户引用
	 * @param params
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<CustomerRef> queryRef(CustomerRef params);
}
