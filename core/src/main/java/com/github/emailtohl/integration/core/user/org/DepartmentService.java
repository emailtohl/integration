package com.github.emailtohl.integration.core.user.org;

import static com.github.emailtohl.integration.core.role.Authority.ORG;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.lib.exception.NotAcceptableException;
import com.github.emailtohl.lib.jpa.Paging;

/**
 * 部门信息服务层
 * @author HeLei
 */
public interface DepartmentService {
	/**
	 * 创建一个部门
	 * @param entity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + ORG + "')")
	Department create(Department entity);
	
	/**
	 * 根据部门名是否已存在
	 * @param name
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	boolean exist(Object name);
	
	/**
	 * 根据ID获取部门
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Department get(Long id);
	
	/**
	 * 根据部门名查询部门
	 * @param name
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Department findByName(String name);
	
	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的部门转瞬态时，同时改变分页对象
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<Department> query(Department params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<Department> query(Department params);

	/**
	 * 修改部门的基本属性
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该部门
	 * @throws NotAcceptableException 父级节点不能是本实例的下级节点，否则会出现循环引用
	 */
	@PreAuthorize("hasAuthority('" + ORG + "')")
	Department update(Long id, Department newEntity) throws NotAcceptableException;

	/**
	 * 根据ID删除部门
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + ORG + "')")
	void delete(Long id);
}
