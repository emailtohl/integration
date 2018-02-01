package com.github.emailtohl.integration.web.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.config.WebPresetData;

@RestController
public class PresetDataCtrl {
	@Inject
	CorePresetData corePresetData;
	@Inject
	WebPresetData webPresetData;
	
	Map<String, Serializable> presetData;
	
	@PostConstruct
	public void init() {
		presetData = new HashMap<>();
		
		presetData.put("user_bot_id", corePresetData.user_bot.getId());
		presetData.put("user_bot_emp_num", corePresetData.user_bot.getEmpNum());
		presetData.put("user_bot_email", corePresetData.user_bot.getEmail());
		presetData.put("user_bot_cell_phone", corePresetData.user_bot.getCellPhone());
		presetData.put("user_bot_image_src", corePresetData.user_bot.getImage().getSrc());
		
		presetData.put("user_admin_id", corePresetData.user_admin.getId());
		presetData.put("user_admin_emp_num", corePresetData.user_admin.getEmpNum());
		presetData.put("user_admin_email", corePresetData.user_admin.getEmail());
		presetData.put("user_admin_cell_phone", corePresetData.user_admin.getCellPhone());
		presetData.put("user_admin_image_src", corePresetData.user_admin.getImage().getSrc());
		
		presetData.put("user_anonymous_id", corePresetData.user_anonymous.getId());
		presetData.put("user_anonymous_email", corePresetData.user_anonymous.getEmail());
		presetData.put("user_anonymous_cell_phone", corePresetData.user_anonymous.getCellPhone());
		presetData.put("user_anonymous_image_src", corePresetData.user_anonymous.getImage().getSrc());
	
		presetData.put("role_admin_id", corePresetData.role_admin.getId());
		presetData.put("role_admin_name", corePresetData.role_admin.getName());
		
		presetData.put("role_manager_id", corePresetData.role_manager.getId());
		presetData.put("role_manager_name", corePresetData.role_manager.getName());
		
		presetData.put("role_staff_id", corePresetData.role_staff.getId());
		presetData.put("role_staff_name", corePresetData.role_staff.getName());
		
		presetData.put("role_guest_id", corePresetData.role_guest.getId());
		presetData.put("role_guest_name", corePresetData.role_guest.getName());
	}
	
	@RequestMapping(value = "presetData", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Map<String, Serializable> get() {
		return presetData;
	}
}
