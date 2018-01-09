package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.role.Authority.CONTENT;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 文章类型接口
 * @author HeLei
 */
public interface TypeService {
	
	/**
	 * 创建一个文章类型
	 * @param entity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Type create(Type entity);

	/**
	 * 查询该文章类型的名字是否已存在
	 * @param name 字符串类型
	 * @return
	 */
	boolean exist(Object name);

	/**
	 * 获取文章类型详情
	 * @param id
	 * @return
	 */
	Type get(Long id);

	/**
	 * 分页查询文章类型
	 * @param params
	 * @param pageable
	 * @return
	 */
	Paging<Type> query(Type params, Pageable pageable);

	/**
	 * 查询文章类型列表
	 * @param params
	 * @return
	 */
	List<Type> query(Type params);

	/**
	 * 更新文章类型
	 * 若修改父级节点，则父级节点不能是本实例的下级节点，否则会出现循环引用
	 * @param id
	 * @param newEntity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Type update(Long id, Type newEntity);

	/**
	 * 删除文章类型
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	void delete(Long id);
}
