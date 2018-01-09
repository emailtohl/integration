package com.github.emailtohl.integration.core.user.org;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Department;

/**
 * 部门数据层
 * @author HeLei
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
	
	/**
	 * 根据部门名查询部门
	 * @param name
	 * @return
	 */
	Department findByName(String name);
	
	/**
	 * 查询父id下的所有部门
	 * @param id
	 * @return
	 */
	List<Department> findByParentId(Long id);
}
