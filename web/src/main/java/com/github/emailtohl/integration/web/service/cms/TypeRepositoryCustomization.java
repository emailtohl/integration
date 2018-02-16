package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 自定义
 * @author HeLei
 */
interface TypeRepositoryCustomization {
	List<Type> getTypesWithArticleNum(Type type);
}
