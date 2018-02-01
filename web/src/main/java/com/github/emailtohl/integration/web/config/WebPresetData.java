package com.github.emailtohl.integration.web.config;

import java.io.Serializable;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 本web模块的预置数据
 * 
 * @author HeLei
 */
public class WebPresetData implements Serializable {
	private static final long serialVersionUID = 8938868828524481149L;
	public final Group group_admin;
	public final Group group_manager;
	public final Group group_staff;
	public final Group group_guest;
	public final User user_admin;
	public final User user_bot;
	public final User user_anonymous;
	public final User user_emailtohl;

	public final Type unclassified = new Type("unclassified", "未分类", null);

	public WebPresetData(CorePresetData cpd, IdentityService identityService) {
		// 用户组（角色）
		group_admin = identityService.newGroup(cpd.role_admin.getName());
		group_admin.setName(cpd.role_admin.getDescription());
		group_admin.setType(cpd.role_admin.getRoleType() == null ? null : cpd.role_admin.getRoleType().name());

		group_manager = identityService.newGroup(cpd.role_manager.getName());
		group_manager.setName(cpd.role_manager.getDescription());
		group_manager.setType(cpd.role_manager.getRoleType() == null ? null : cpd.role_manager.getRoleType().name());

		group_staff = identityService.newGroup(cpd.role_staff.getName());
		group_staff.setName(cpd.role_staff.getDescription());
		group_staff.setType(cpd.role_staff.getRoleType() == null ? null : cpd.role_staff.getRoleType().name());

		group_guest = identityService.newGroup(cpd.role_guest.getName());
		group_guest.setName(cpd.role_guest.getDescription());
		group_guest.setType(cpd.role_guest.getRoleType() == null ? null : cpd.role_guest.getRoleType().name());

		// 用户
		user_admin = identityService.newUser(cpd.user_admin.getId().toString());
		user_admin.setEmail(cpd.user_admin.getEmail());
		user_admin.setFirstName(cpd.user_admin.getName());
		user_admin.setLastName(cpd.user_admin.getNickname());
		user_admin.setPassword(cpd.user_admin.getPassword());

		user_bot = identityService.newUser(cpd.user_bot.getId().toString());
		user_bot.setEmail(cpd.user_bot.getEmail());
		user_bot.setFirstName(cpd.user_bot.getName());
		user_bot.setLastName(cpd.user_bot.getNickname());
		user_bot.setPassword(cpd.user_bot.getPassword());

		user_anonymous = identityService.newUser(cpd.user_anonymous.getId().toString());
		user_anonymous.setEmail(cpd.user_anonymous.getEmail());
		user_anonymous.setFirstName(cpd.user_anonymous.getName());
		user_anonymous.setLastName(cpd.user_anonymous.getNickname());
		user_anonymous.setPassword(cpd.user_anonymous.getPassword());

		user_emailtohl = identityService.newUser(cpd.user_emailtohl.getId().toString());
		user_emailtohl.setEmail(cpd.user_emailtohl.getEmail());
		user_emailtohl.setFirstName(cpd.user_emailtohl.getName());
		user_emailtohl.setLastName(cpd.user_emailtohl.getNickname());
		user_emailtohl.setPassword(cpd.user_emailtohl.getPassword());
	}

}
