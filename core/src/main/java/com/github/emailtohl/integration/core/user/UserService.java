package com.github.emailtohl.integration.core.user;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 统一查询功能
 * @author HeLei
 */
@Validated
public interface UserService {
	/**
	 * 全文搜索用户信息
	 * @param fulltext
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<User> search(String fulltext, Pageable pageable);
	
	/**
	 * 全文搜索用户引用信息
	 * @param fulltext
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<UserRef> searchRef(String fulltext, Pageable pageable);
	
	/**
	 * 根据域条件查询
	 * @param params
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<User> query(User params, Pageable pageable);
	
	/**
	 * 根据域条件查询用户引用
	 * @param params
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<UserRef> queryRef(UserRef params, Pageable pageable);
	
	/**
	 * 查看该User
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	User get(Long id);
	
	/**
	 * 查看该User的引用
	 * @param id
	 * @return
	 */
//	@PreAuthorize("isAuthenticated()")
	UserRef getRef(Long id);
	
	/**
	 * 注意返回的实例是瞬时态的实体对象，若出了事务层后再调用延迟加载字段会报异常
	 * 
	 * @param username 在Spring Security中用户唯一性的标识，本系统中可以是平台工号、客户手机或客户邮箱
	 * @return 若没查找到则返回null
	 */
	User find(String username);
	
	/**
	 * 为查找登录而准备的接口，在返回用户时，同时更新最后登录时间
	 * @param username
	 * @return
	 */
	User findAndRefreshLastLogin(String username);
	
	/**
	 * 查找用户的引用
	 * @param username
	 * @return
	 */
	UserRef findRef(String username);
	
	/**
	 * 查找某角色下的所有用户引用
	 * @param roleName
	 * @return
	 */
	List<UserRef> findRefByRoleName(String roleName);
}
