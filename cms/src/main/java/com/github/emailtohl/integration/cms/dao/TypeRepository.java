package com.github.emailtohl.integration.cms.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.cms.entities.Type;

/**
 * 文章类型的数据访问接口
 * @author HeLei
 * @date 2017.02.17
 */
public interface TypeRepository extends JpaRepository<Type, Long> {
	/**
	 * 根据类型名查询类型实体
	 * @param name
	 * @return
	 */
	Type findByName(String name);
	
	/**
	 * 分页查询文章类型
	 * @param name
	 * @param pageable
	 * @return
	 */
	Page<Type> findByNameLike(String name, Pageable pageable);
	
}
