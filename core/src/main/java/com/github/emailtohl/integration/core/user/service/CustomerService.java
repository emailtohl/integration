package com.github.emailtohl.integration.core.user.service;

import static com.github.emailtohl.integration.core.user.entities.Authority.CUSTOMER_ENABLED;
import static com.github.emailtohl.integration.core.user.entities.Authority.CUSTOMER_LEVEL;
import static com.github.emailtohl.integration.core.user.entities.Authority.CUSTOMER_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.user.entities.Authority.CUSTOMER_ROLE;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.entities.Card;
import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * 客户的服务层
 * @author HeLei
 */
public interface CustomerService extends StandardService<Customer> {
	
	/**
	 * 客户登录
	 * @param cellPhoneOrEmail
	 * @param password
	 * @return
	 */
	@NotNull ExecResult login(String cellPhoneOrEmail, String password);
	
	/**
	 * 通过手机号码或者邮箱查找客户
	 * @param cellPhoneOrEmail
	 * @return
	 */
	Customer findByCellPhoneOrEmail(String cellPhoneOrEmail);
	
	/**
	 * 为客户授予角色
	 * @param id
	 * @param roleNames
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_ROLE + "')")
	Customer grandRoles(Long id, String... roleNames);
	
	/**
	 * 为客户提升等级的权限
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
	 * 更新客户的密码，一般是用户忘记密码后，通过邮箱确认后自行修改，所以不需要输入原密码
	 * @param cellPhoneOrEmail
	 * @param newPassword
	 * @param token 通过邮箱或短信或其他方式获取的令牌，该令牌证明确实是该账号拥有者在修改密码
	 * @return ExecResult
	 */
	@PreAuthorize("#cellPhoneOrEmail matches authentication.principal.username")
	@NotNull ExecResult updatePassword(@P("cellPhoneOrEmail") String cellPhoneOrEmail, String newPassword, String token);
	
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
	Customer changeCellPhone(Long id, @Pattern(regexp = ConstantPattern.CELL_PHONE) String newCellPhone);
	
	/**
	 * 跟换电子邮箱
	 * @param id
	 * @param newEmail
	 * @return
	 */
	Customer changeEmail(Long id, String newEmail);
	
	/**
	 * 是否锁定该客户的账号
	 * @param id
	 * @param enabled
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CUSTOMER_ENABLED + "')")
	Customer enabled(Long id, boolean enabled);
	
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
