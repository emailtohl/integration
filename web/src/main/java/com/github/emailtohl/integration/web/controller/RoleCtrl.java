package com.github.emailtohl.integration.web.controller;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.lib.jpa.EntityBase;
import com.github.emailtohl.lib.jpa.Paging;
/**
 * 角色管理的控制器
 * @author HeLei
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RoleCtrl extends RestCtrl<Role> {
	@Inject
	RoleService roleService;
	
	@RequestMapping(value = "role", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public Role create(@RequestBody @Valid Role entity, Errors errors) {
		checkErrors(errors);
		return roleService.create(entity);
	}

	@RequestMapping(value = "role/{id}", method = RequestMethod.GET)
	public Role get(@PathVariable("id") Long id) {
		Role e = roleService.get(id);
		mustExist(e);
		return e;
	}

	@RequestMapping(value = "role/page", method = RequestMethod.GET)
	public Paging<Role> query(Role params,
			@PageableDefault(page = 0, size = 10, sort = { EntityBase.ID_PROPERTY_NAME,
					EntityBase.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		return roleService.query(params, pageable);
	}

	@RequestMapping(value = "role", method = RequestMethod.GET)
	public List<Role> query(Role params) {
		return roleService.query(params);
	}

	@RequestMapping(value = "role/{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(@PathVariable("id") Long id, @RequestBody @Valid Role newEntity, Errors errors) {
		checkErrors(errors);
		roleService.update(id, newEntity);
	}

	@RequestMapping(value = "role/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") Long id) {
		roleService.delete(id);
	}
	
    /**
     * 查询该角色名是否已用
     * @param roleName
     * @return
     */
    @RequestMapping(value = "role/exist", method = RequestMethod.GET)
    public String exist(@RequestParam(required = false, name = "roleName", defaultValue = "") String roleName) {
        if (!StringUtils.hasText(roleName)) {
            return String.format("{\"exist\":%b}", false);
        }
        return String.format("{\"exist\":%b}", roleService.exist(roleName));
    }

	/**
	 * 获取所有权限
	 * @return
	 */
	@RequestMapping(value = "authority", method = RequestMethod.GET)
	public List<Authority> getAuthorities() {
		return roleService.getAuthorities();
	}
    
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

}
