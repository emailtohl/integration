package com.github.emailtohl.integration.user.service;

import static com.github.emailtohl.integration.user.entities.Authority.USER_CREATE_SPECIAL;
import static com.github.emailtohl.integration.user.entities.Authority.USER_DELETE;
import static com.github.emailtohl.integration.user.entities.Authority.USER_DISABLE;
import static com.github.emailtohl.integration.user.entities.Authority.USER_GRANT_ROLES;
import static com.github.emailtohl.integration.user.entities.Authority.USER_READ_ALL;
import static com.github.emailtohl.integration.user.entities.Authority.USER_READ_SELF;
import static com.github.emailtohl.integration.user.entities.Authority.USER_UPDATE_ALL;
import static com.github.emailtohl.integration.user.entities.Authority.USER_UPDATE_SELF;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.Role;
import com.github.emailtohl.integration.user.entities.User;
/**
 * 用户管理的服务
 * @author HeLei
 * @date 2017.02.04
 */
@Transactional
@Validated
public interface UserService {
	// 该缓存map的key是用户的email而非id，这是因为getUserByEmail方法在上下文访问较多，而通过id访问的场景主要在于用户管理里面，无需缓存
	String CACHE_NAME_USER = "userCache";
	String CACHE_NAME_USER_PAGE = "userPageCache";
	
	/**
	 * 创建雇员账号，但只授予Employee的角色
	 * 注意：对于Spring Security来说，新增用户时，必须同时为其添加相应的用户授权，否则即便激活了该用户，也不会让其登录
	 * @param u
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("hasAuthority('" + USER_CREATE_SPECIAL + "')")
	User addEmployee(@Valid Employee u);
	
	/**
	 * 注册普通账号，无需权限即可
	 * 注意：对于Spring Security来说，新增用户时，必须同时为其添加相应的用户授权，否则即便激活了该用户，也不会让其登录
	 * @param u
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	User addCustomer(@Valid Customer u);
	
	/**
	 * 启用用户，无需权限即可
	 * @param id
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	User enableUser(@Min(value = 1L) Long id);
	
	/**
	 * 禁用用户
	 * @param id
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("hasAuthority('" + USER_DISABLE + "')")
	User disableUser(@Min(value = 1L) Long id);
	
	/**
	 * 授予用户角色
	 * @param roleNames
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("hasAuthority('" + USER_GRANT_ROLES + "')")
	User grantRoles(long id, String... roleNames);
	
	/**
	 * 新建用户时，授予普通用户角色
	 * @param id
	 * @return 用于缓存
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	User grantUserRole(long id);
	
	/**
	 * 修改密码，限制只能本人才能修改
	 * 登录页面中通过邮箱方式修改密码在AuthenticationService接口中
	 * authentication是直接从SecurityContextHolder中获取的对象
	 * @param email
	 * @param newPassword
	 * @return 用于缓存
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("#email == authentication.principal.username")
	User changePassword(@P("email") String email, @NotNull @Pattern(regexp = "^[^\\s&\"<>]+$") String newPassword);
	
	/**
	 * 修改密码，用于用户忘记密码的场景，没有权限控制
	 * 由方法内部逻辑判断进行修改
	 * @param email
	 * @param newPassword
	 * @return 用于缓存
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	User changePasswordByEmail(String email, @NotNull @Pattern(regexp = "^[^\\s&\"<>]+$") String newPassword);
	
	/**
	 * 删除用户
	 * @param id
	 */
	@CacheEvict(value = { CACHE_NAME_USER, CACHE_NAME_USER_PAGE }, allEntries = true)
	@PreAuthorize("hasAuthority('" + USER_DELETE + "')")
	void deleteUser(@Min(value = 1L) Long id);
	
	/**
	 * 查询用户，通过认证的均可调用
	 * returnObject和principal是spring security内置对象
	 * @param id
	 * @return 若未查找到用户，返回null
	 */
	@PostAuthorize("hasAuthority('" + USER_READ_ALL + "') || (hasAuthority('" + USER_READ_SELF + "') && returnObject.username == principal.username)")
	User getUser(@Min(value = 1L) Long id);
	
	/**
	 * 通过邮箱名查询用户，通过认证的均可调用
	 * @param email
	 * @return
	 * @throws NotFoundException 由于应用了缓存策略，返回结果不能为null，所以若未找到资源，则抛此异常
	 */
	@Cacheable(value = CACHE_NAME_USER, key = "#root.args[0]")
	@PostAuthorize("hasAuthority('" + USER_READ_ALL + "') || (hasAuthority('" + USER_READ_SELF + "') && #email == principal.username)")
	User getUserByEmail(@NotNull @P("email") String email) throws NotFoundException;
	
	/**
	 * 修改用户的头像地址
	 * @param iconSrc 修改用户头像的地址
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("isAuthenticated()")
	User updateIconSrc(long id, String iconSrc);
	
	/**
	 * 将用户的头像二进制文件存入数据库
	 * @param icon 二进制图片文件
	 * @return 用于缓存
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("isAuthenticated()")
	User updateIcon(long id, byte[] icon);
	
	/**
	 * 修改用户
	 * 这里的方法名使用的是merge，传入的User参数只存储需要更新的属性，不更新的属性值为null
	 * 
	 * 修改密码，启用/禁用账户，授权功能，不走此接口
	 * 
	 * @param u中的id不能为null， u中属性不为null的值为修改项
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("hasAuthority('" + USER_UPDATE_ALL + "') || (hasAuthority('" + USER_UPDATE_SELF + "') && #email == principal.username)")
	User mergeEmployee(@NotNull @P("email") String email, Employee emp);
	
	/**
	 * 修改用户
	 * 这里的方法名使用的是merge，传入的User参数只存储需要更新的属性，不更新的属性值为null
	 * 
	 * 修改密码，启用/禁用账户，授权功能，不走此接口
	 * 
	 * @param u中的id不能为null， u中属性不为null的值为修改项
	 * @return 用于缓存
	 */
	@CacheEvict(value = CACHE_NAME_USER_PAGE, allEntries = true)
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("hasAuthority('" + USER_UPDATE_ALL + "') || (hasAuthority('" + USER_UPDATE_SELF + "') && #email == principal.username)")
	User mergeCustomer(@NotNull @P("email") String email, Customer cus);
	
	/**
	 * 获取用户Page
	 * 
	 * 实现类中要对Page中返回的List中敏感信息进行过滤
	 * 
	 * @param u
	 * @param pageable
	 * @return
	 */
	@Cacheable(value = CACHE_NAME_USER_PAGE, key = "#root.args")
	@NotNull
	@PreAuthorize("isAuthenticated()")
	Paging<User> getUserPage(User u, Pageable pageable);
	
	/**
	 * 检查该邮箱是否注册
	 * @param email
	 * @return
	 */
	boolean isExist(@Pattern(regexp = Constant.PATTERN_EMAIL, flags = { Pattern.Flag.CASE_INSENSITIVE }) String email);
	
	/**
	 * 通过用户邮箱名和角色名组合查询Page
	 * @param email
	 * @param roles
	 * @param pageable
	 * @return
	 */
	@Cacheable(value = CACHE_NAME_USER_PAGE, key = "#root.args")
	@PreAuthorize("isAuthenticated()")
	Paging<User> getPageByRoles(String email, Set<String> roleNames, Pageable pageable);
	
	/**
	 * 获取用户角色
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<Role> getRoles();
	
	/**
	 * 上传用户的公钥
	 * @param publicKey
	 * @param module
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("isAuthenticated()")
	User setPublicKey(@NotNull String email, @NotNull String publicKey);
	
	/**
	 * 删除用户的公钥
	 * @param publicKey
	 * @param module
	 */
	@CachePut(value = CACHE_NAME_USER, key = "#result.email")
	@PreAuthorize("isAuthenticated()")
	User clearPublicKey(@NotNull String email);

	/**
	 * 判断该用户是否存在
	 * @param username
	 */
	boolean exist(String username);
	
}
