package com.github.emailtohl.integration.core.user.org;

import static com.github.emailtohl.integration.core.role.Authority.ORG;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.user.entities.Company;

/**
 * 公司信息服务层
 * @author HeLei
 */
public interface CompanyService {
	
	/**
	 * 创建一个公司
	 * @param entity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + ORG + "')")
	Company create(Company entity);
	
	/**
	 * 根据公司名查找是否已存在
	 * @param matcherValue
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	boolean exist(Object matcherValue);
	
	/**
	 * 根据ID获取公司
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Company get(Long id);
	
	/**
	 * 根据公司名查询公司
	 * @param name
	 * @return
	 */
	Company findByName(String name);
	
	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的公司转瞬态时，同时改变分页对象
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<Company> query(Company params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<Company> query(Company params);

	/**
	 * 修改公司的基本属性
	 * 若修改父级节点，则父级节点不能是本实例的下级节点，否则会出现循环引用
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该公司
	 * @throws NotAcceptableException 父级节点不能是本实例的下级节点，否则会出现循环引用
	 */
	@PreAuthorize("hasAuthority('" + ORG + "')")
	Company update(Long id, Company newEntity) throws NotAcceptableException;

	/**
	 * 根据ID删除公司
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + ORG + "')")
	void delete(Long id);
	
	/**
	 * 获取公司详情
	 * @param src
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Company transientCompanyDetail(Company src);
	
}
