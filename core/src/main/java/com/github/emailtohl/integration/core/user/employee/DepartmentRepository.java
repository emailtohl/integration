package com.github.emailtohl.integration.core.user.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Department;
/**
 * 部门数据仓库
 * @author HeLei
 */
interface DepartmentRepository extends JpaRepository<Department, Long> {
	Department findByName(String name);
}
