package com.github.emailtohl.integration.web.service.flow;

import java.util.List;

import com.github.emailtohl.integration.web.config.MybatisMapperInterface;
/**
 * 快速查询用户Id
 * @author HeLei
 */
@MybatisMapperInterface
public interface UserIdMapper {
	
	public List<String> findUserIdInGroupId(List<String> groupIds);
	
}
