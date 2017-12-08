package com.github.emailtohl.integration.core;

import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.Paging;

/**
 * 抽象的服务，主要就是增删改查功能
 * 
 * 默认ID是Long类型
 * 
 * @author HeLei
 */
@Transactional
public abstract class StandardService<E extends Serializable> {
	
	/**
	 * 创建一个实体
	 * @param entity
	 * @return
	 */
	public abstract E create(E entity);
	
	/**
	 * 根据实体自身唯一性的属性查找是否已存在
	 * @param matcherValue
	 * @return
	 */
	public abstract boolean exist(Object matcherValue);
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public abstract E get(Long id);

	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的实体转瞬态时，同时改变分页对象
	 */
	public abstract Paging<E> query(E params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	public abstract List<E> query(E params);

	/**
	 * 修改实体内容，并指明哪些属性忽略
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该实体
	 */
	public abstract E update(Long id, E newEntity);

	/**
	 * 根据ID删除实体
	 * @param id
	 */
	public abstract void delete(Long id);
	
	/**
	 * 屏蔽实体中的敏感信息，如密码；将持久化状态的实体转存到瞬时态的实体对象上以便于调用者序列化
	 * 本方法提取简略信息，不做关联查询，主要用于列表中
	 * @param entity 持久化状态的实体对象
	 * @return 瞬时态的实体对象
	 */
	protected abstract E toTransient(E entity);
	
	/**
	 * 屏蔽实体中的敏感信息，如密码；将持久化状态的实体转存到瞬时态的实体对象上以便于调用者序列化
	 * 本方法提取详细信息，做关联查询
	 * @param entity 持久化状态的实体对象
	 * @return 瞬时态的实体对象
	 */
	protected abstract E transientDetail(E entity);
}
