package com.github.emailtohl.integration.web.service.mybatistest;

import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.web.config.MybatisMapperInterface;

/**
 * Mybatis数据层接口
 * @author HeLei
 */
@MybatisMapperInterface
public interface DepartmentMapper {
    /**
     * 根据id查询部门
     * @param name
     * @return result
     */
	Department findByName(String name);
    
    /**
     * 插入部门
     * @param d 实体
     * @return result id
     */
    long insert(Department d);
    
    /**
     * 删除部门
     * @param id id
     * @return result
     */
//    int delete(long id);
}
