package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.role.Authority.CONTENT;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
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
	 * 根据文章类型名字获取类型
	 * @param name
	 * @return
	 */
	Type getByName(String name);

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
	 * 从数据层开始就获取到每个Type下有多少文章数
	 * @param params
	 * @return
	 */
	List<Type> getTypesWithArticleNum(Type params);

	/**
	 * 更新文章类型
	 * 
	 * @param id
	 * @param newEntity
	 * @return
	 * @throws NotAcceptableException 父级节点不能是本实例的下级节点，否则会出现循环引用
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	Type update(Long id, Type newEntity) throws NotAcceptableException;

	/**
	 * 删除文章类型
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	void delete(Long id);
}
