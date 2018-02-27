package com.github.emailtohl.integration.web.service.mybatistest;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.github.emailtohl.integration.core.user.org.DepartmentService;
import com.github.emailtohl.integration.web.config.MybatisConfiguration;

@Configurable
@Import(MybatisConfiguration.class)
class Config {

	@Bean
	public MybatisTestService mybatisTestService(DepartmentService departmentService, DepartmentMapper departmentMapper,
			SqlSession sqlSession) {
		return new MybatisTestService(departmentService, departmentMapper, sqlSession);
	}
}
