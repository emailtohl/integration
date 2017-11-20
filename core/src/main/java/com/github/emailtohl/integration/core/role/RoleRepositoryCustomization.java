package com.github.emailtohl.integration.core.role;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.CriterionQueryRepository;
import com.github.emailtohl.integration.core.role.Role;

/**
 * 访问角色的自定义接口
 * @author HeLei
 */
interface RoleRepositoryCustomization extends CriterionQueryRepository<Role> {
	/**
     * 根据角色名和权限名组合查询
     * @param roleName
     * @param authorityName
     * @param pageable
     * @return
     */
    Page<Role> query(String roleName, String authorityName, Pageable pageable);
    /**
     * 根据角色名和权限名组合查询
     * @param roleName
     * @param authorityName
     * @return
     */
    List<Role> getRoleList(String roleName, String authorityName);
    /**
     * 查找能被删除的角色，即未被用户使用的角色
     * @return
     */
    @Transactional
    Set<Long> roleIdsWhichCanNotBeDeleted();
    /**
     * 检查该roleName是否存在
     * @param roleName
     * @return
     */
    @Transactional
    boolean exist(String roleName);
}