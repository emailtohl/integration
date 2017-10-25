package com.github.emailtohl.integration.common.standard;

import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

/**
 * 抽象的服务接口，主要就是增删改查功能
 * 
 * 默认ID是Long类型
 * 
 * @author HeLei
 */
@Transactional
@Validated
public interface StandardService<E extends Serializable> {
	
	/**
	 * 创建一个实体
	 * @param entity
	 * @return
	 */
	E create(@Valid E entity);
	
	/**
	 * 根据JavaBean属性名以及匹配的值来查找是否已存在
	 * @param propertyName
	 * @param matcherValue
	 * @return
	 */
	boolean exist(String propertyName, Object matcherValue);
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	E get(Long id);

	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return
	 */
	Page<E> query(E params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	List<E> query(E params);

	/**
	 * 修改实体内容，并指明哪些属性忽略
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该实体
	 */
	E update(Long id, E newEntity);

	/**
	 * 根据ID删除实体
	 * @param id
	 */
	void delete(Long id);
}
