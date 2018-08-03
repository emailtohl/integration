package com.github.emailtohl.integration.core.user;

import java.util.List;

import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.lib.jpa.SearchInterface;

/**
 * 用户引用自定义数据层
 * @author HeLei
 */
interface UserRefRepositoryCustomization extends SearchInterface<UserRef, Long> {
	/**
	 * 查找某角色下的所有用户引用
	 * @param roleName
	 * @return
	 */
	List<UserRef> findUserRefByRoleName(String roleName);
	
	/**
	 * 查找某角色下的平台账号的引用
	 * @param roleName
	 * @return
	 */
	List<EmployeeRef> findEmployeeRefByRoleName(String roleName);
	
	/**
	 * 查找某角色下的客户账号引用
	 * @param roleName
	 * @return
	 */
	List<CustomerRef> findCustomerRefByRoleName(String roleName);
}
