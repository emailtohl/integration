package com.github.emailtohl.integration.nuser.service;

import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_LEVEL;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_LOCK;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_RESET_PASSWORD;
import static com.github.emailtohl.integration.nuser.entities.Authority.CUSTOMER_ROLE;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.common.standard.StandardService;
import com.github.emailtohl.integration.nuser.entities.Card;
import com.github.emailtohl.integration.nuser.entities.Customer;

/**
 * 外部用户的服务层
 * @author HeLei
 */
@Validated
public interface CustomerService extends StandardService<Customer> {
	
	/**
	 * 外部用户登录
	 * @param cellPhoneOrEmail
	 * @param password
	 * @return
	 */
	@NotNull ExecResult login(String cellPhoneOrEmail, String password);
	
	/**
	 * 通过手机号码或者邮箱查找外部用户
	 * @param cellPhoneOrEmail
	 * @return
	 */
	Customer findByCellPhoneOrEmail(String cellPhoneOrEmail);
	
	/**
	 * 为外部人员授予角色
	 * @param id
	 * @param roleNames
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_ROLE + "')")
	Customer grandRoles(Long id, String... roleNames);
	
	/**
	 * 为外部人员提升等级的权限
	 * @param id
	 * @param roleNames
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_LEVEL + "')")
	Customer grandLevel(Long id, Customer.Level level);
	
	/**
	 * 用户忘记密码时，通过邮箱或短信获取token令牌，以证明是被修改密码的账号拥有者
	 * @param cellPhoneOrEmail
	 * @return
	 */
	String getToken(String cellPhoneOrEmail);
	
	/**
	 * 更新外部人员的密码，一般是用户忘记密码后，通过邮箱确认后自行修改，所以不需要输入原密码
	 * @param cellPhoneOrEmail
	 * @param newPassword
	 * @param token 通过邮箱或短信或其他方式获取的令牌，该令牌证明确实是该账号拥有者在修改密码
	 * @return ExecResult
	 */
	@NotNull ExecResult updatePassword(String cellPhoneOrEmail, String newPassword, String token);
	
	/**
	 * 重置密码，用于忘记密码无法恢复时
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_RESET_PASSWORD + "')")
	@NotNull ExecResult resetPassword(Long id);
	
	/**
	 * 跟换手机号码
	 * @param id
	 * @param newCellPhone
	 * @return
	 */
	Customer changeCellPhone(Long id, String newCellPhone);
	
	/**
	 * 跟换电子邮箱
	 * @param id
	 * @param newEmail
	 * @return
	 */
	Customer changeEmail(Long id, String newEmail);
	
	/**
	 * 是否锁定该外部人员的账号
	 * @param id
	 * @param lock
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_LOCK + "')")
	Customer lock(Long id, boolean lock);
	
	/**
	 * 批量更新用户所属的卡
	 * @param id
	 * @param cards
	 * @return
	 */
	Customer updateCards(Long id, Set<Card> cards);
	
	/**
	 * 添加卡
	 * @param id
	 * @param card
	 * @return
	 */
	Customer addCard(Long id, @Valid Card card);
	
	/**
	 * 移除卡
	 * @param id
	 * @param card
	 * @return
	 */
	Customer removeCard(Long id, @Valid Card card);
}