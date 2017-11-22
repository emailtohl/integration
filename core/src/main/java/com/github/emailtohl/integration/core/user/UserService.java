package com.github.emailtohl.integration.core.user;

import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 统一查询功能
 * @author HeLei
 */
@Validated
public interface UserService {
	Pattern PATTERN_EMAIL = Pattern.compile(ConstantPattern.EMAIL);
	Pattern PATTERN_CELL_PHONE = Pattern.compile(ConstantPattern.CELL_PHONE);
	Pattern PATTEN_EMP_NUM = Pattern.compile(Employee.PATTERN_EMP_NUM);
	/**
	 * 全文搜索
	 * @param fulltext
	 * @param pageable
	 * @return
	 */
	Paging<User> search(String fulltext, Pageable pageable);
	
	/**
	 * 根据域条件查询
	 * @param params
	 * @param pageable
	 * @return
	 */
	Paging<User> query(User params, Pageable pageable);
	
	/**
	 * 查看该User
	 * @param id
	 * @return
	 */
	User get(Long id);
	
	/**
	 * 返回持久化状态的用户实例
	 * @param username 在Spring Security中用户唯一性的标识，本系统中可以是平台工号、客户手机或客户邮箱
	 * @return 若没查找到则返回null
	 */
	User find(String username);
	
}
