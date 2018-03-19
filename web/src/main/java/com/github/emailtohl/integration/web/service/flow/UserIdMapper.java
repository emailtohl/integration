package com.github.emailtohl.integration.web.service.flow;

import java.util.List;

import com.github.emailtohl.integration.web.config.MybatisMapperInterface;
/**
 * 快速查询用户Id
 * @author HeLei
 */
@MybatisMapperInterface
public interface UserIdMapper {
	/**
	 * 根据组中所有的用户id
	 * @param groupIds
	 * @return
	 */
	public List<String> findUserIdInGroupId(List<String> groupIds);
	/**
	 * 根据用户id查FirstName
	 * @param userId
	 * @return
	 */
	public String getFirstNameByUserId(String userId);
	
}
