package com.github.emailtohl.integration.core.role;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.lib.jpa.QueryInterface;

/**
 * 访问角色的自定义接口
 * @author HeLei
 */
interface RoleRepositoryCustomization extends QueryInterface<Role, Long> {
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
     * 检查该roleName是否存在
     * @param roleName
     * @return
     */
    @Transactional
    boolean exist(String roleName);
    
    /**
     * 通过id查找角色的名字
     * @param id 角色id
     * @return 角色名
     */
    String getRoleName(Long id);
}
