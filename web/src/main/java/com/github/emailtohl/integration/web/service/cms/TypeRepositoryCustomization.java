package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 自定义
 * 
 * @author HeLei
 */
interface TypeRepositoryCustomization {
	/**
	 * 查询文章类型时，就获取到起类型有多少关联的文章数
	 * @param params
	 * @return
	 */
	List<Type> getTypesWithArticleNum(Type params);
}
