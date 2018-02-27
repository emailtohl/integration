package com.github.emailtohl.integration.web.service.mybatistest;

import javax.transaction.Transactional;

import org.apache.ibatis.session.SqlSession;

import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.org.DepartmentService;

@Transactional
public class MybatisTestService {
	DepartmentService departmentService;
	DepartmentMapper departmentMapper;
	SqlSession sqlSession;
	
	public MybatisTestService(DepartmentService departmentService, DepartmentMapper departmentMapper,
			SqlSession sqlSession) {
		super();
		this.departmentService = departmentService;
		this.departmentMapper = departmentMapper;
		this.sqlSession = sqlSession;
	}

	public Department findByName(String name) {
		return departmentMapper.findByName(name);
	}
	
	/**
	 * 测试事务，重复提交有异常发生
	 * @param d
	 */
	public Long insert(Department d) {
		long id = departmentMapper.insert(d);
		departmentService.create(d);
		id = d.getId();
		return id;
	}

}
