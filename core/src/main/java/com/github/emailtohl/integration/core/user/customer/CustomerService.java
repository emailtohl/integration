package com.github.emailtohl.integration.core.user.customer;

import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_ENABLED;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_LEVEL;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_RESET_PASSWORD;
import static com.github.emailtohl.integration.core.role.Authority.CUSTOMER_ROLE;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.entities.Card;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;

/**
 * 客户的服务层
 * @author HeLei
 */
public interface CustomerService extends StandardService<Customer> {
	
	/**
	 * 全文查询
	 * @param query
	 * @param pageable
	 * @return
	 */
	Paging<Customer> search(String query, Pageable pageable);
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
	
	/**
	 * 获取客户引用
	 * @param id
	 * @return
	 */
	CustomerRef getRef(Long id);
	
	/**
	 * 通过手机或邮箱获取客户引用
	 * @param cellPhoneOrEmail
	 * @return
	 */
	CustomerRef findRefByCellPhoneOrEmail(String cellPhoneOrEmail);
	
	/**
	 * 查找客户引用
	 * @param params
	 * @param pageable
	 * @return
	 */
	Paging<CustomerRef> queryRef(CustomerRef params, Pageable pageable);
	
	/**
	 * 查找客户引用
	 * @param params
	 * @return
	 */
	List<CustomerRef> queryRef(CustomerRef params);
}
