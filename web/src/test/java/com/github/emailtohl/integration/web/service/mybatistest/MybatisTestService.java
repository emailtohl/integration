package com.github.emailtohl.integration.web.service.mybatistest;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.org.DepartmentService;

@Service
@Transactional
public class MybatisTestService {
	DepartmentService departmentService;
	DepartmentMapper departmentMapper;
	SqlSession sqlSession;
	
	@Inject
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
		// 先用Mybatis插入一个实例
		long id = departmentMapper.insert(d);
		// 再用JPA重复插入该实例
		departmentService.create(d);
		id = d.getId();
		return id;
	}

}
