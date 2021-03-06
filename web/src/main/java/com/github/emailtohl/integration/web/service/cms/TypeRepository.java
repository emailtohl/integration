package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 文章类型的数据访问接口
 * @author HeLei
 */
interface TypeRepository extends JpaRepository<Type, Long>, TypeRepositoryCustomization {
	/**
	 * 根据类型名查询类型实体
	 * @param name
	 * @return
	 */
	Type getByName(String name);
	
	/**
	 * 分页查询文章类型
	 * @param name
	 * @param pageable
	 * @return
	 */
	Page<Type> findByNameLike(String name, Pageable pageable);
	
	/**
	 * 查询父id下的所有类型
	 * @param id
	 * @return
	 */
	List<Type> findByParentId(Long id);
	
}
