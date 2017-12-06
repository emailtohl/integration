package com.github.emailtohl.integration.core.user.org;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Department;

/**
 * 部门数据层
 * @author HeLei
 */
interface DepartmentRepository extends JpaRepository<Department, Long> {

	Department findByName(String name);
	
	List<Department> findByParentId(Long id);
}
