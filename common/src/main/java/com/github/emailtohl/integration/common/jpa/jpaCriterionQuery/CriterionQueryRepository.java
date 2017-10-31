package com.github.emailtohl.integration.common.jpa.jpaCriterionQuery;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.AccessType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.DynamicQueryRepository;
/**
 * 标准查询接口
 * 让该接口继承DynamicQueryRepository，即获得动态查询的能力，也获得本接口提供的功能
 * @param <E> 实体类
 * @author HeLei
 */
public interface CriterionQueryRepository<E extends Serializable> extends DynamicQueryRepository<E> {
	/**
	 * 标准查询接口，根据传入的条件集合得到一个Page对象
	 * 注意，Pageable的查询是从第0页开始，条件集合之间是AND关系
	 * 
	 * @param criteria 一个条件集合
	 * @param pageable 分页对象
	 * @return
	 */
	Page<E> queryForPage(Collection<Criterion> criteria, Pageable pageable);
	
	/**
	 * 标准查询接口，根据传入的条件集合得到一个Page对象
	 * 注意，Pageable的查询是从第0页开始，条件集合之间是AND关系
	 * 
	 * @param criteria 一个条件集合
	 * @return
	 */
	List<E> queryForList(Collection<Criterion> criteria);
	
	/**
	 * 根据实体参数进行AND查询。
	 * 注意实体尽量避免基本类型，否则基本类型的默认值会当做参数进行查询
	 * @param params 实体参数
	 * @param pageable 分页对象
	 * @param type 访问实体的方式：1.JavaBean的Getter、Setter；2.直接访问Field域
	 * @return 分页对象
	 */
	Page<E> queryForPage(E params, Pageable pageable, AccessType type);
	
	/**
	 * 根据实体参数进行AND查询。
	 * 注意：
	 * 1.实体尽量避免基本类型，否则基本类型的默认值会当做参数进行查询；
	 * 2.通过JavaBean的Getter、Setter访问实体参数
	 * @param params 实体参数
	 * @param pageable 分页对象
	 * @return 分页对象
	 */
	Page<E> queryForPage(E params, Pageable pageable);
	
	/**
	 * 根据实体参数进行AND查询。
	 * 注意实体尽量避免基本类型，否则基本类型的默认值会当做参数进行查询
	 * @param params 实体参数
	 * @param pageable 分页对象
	 * @param type 访问实体的方式：1.JavaBean的Getter、Setter；2.直接访问Field域
	 * @return 结果列表
	 */
	List<E> queryForList(E params, AccessType type);
	
	/**
	 * 根据实体参数进行AND查询。
	 * 注意：
	 * 1.实体尽量避免基本类型，否则基本类型的默认值会当做参数进行查询；
	 * 2.通过JavaBean的Getter、Setter访问实体参数
	 * @param params 实体参数
	 * @param pageable 分页对象
	 * @param type 访问实体的方式：1.JavaBean的Getter、Setter；2.直接访问Field域
	 * @return 结果列表
	 */
	List<E> queryForList(E params);
	
}
