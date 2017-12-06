package com.github.emailtohl.integration.core.user.org;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.entities.Department;

/**
 * 部门信息服务层
 * @author HeLei
 */
@Validated
@PreAuthorize("isAuthenticated()")
public interface DepartmentService extends StandardService<Department> {

}
