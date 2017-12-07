package com.github.emailtohl.integration.core.user;

import java.util.regex.Pattern;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 模块相关常量
 * @author HeLei
 */
public interface Constant {
	String ADMIN_NAME = "admin";
	String ANONYMOUS_NAME = "anonymous";
	String DEFAULT_PASSWORD = "123456";
	
	Pattern PATTERN_EMAIL = Pattern.compile(ConstantPattern.EMAIL);
	Pattern PATTERN_CELL_PHONE = Pattern.compile(ConstantPattern.CELL_PHONE);
	Pattern PATTEN_EMP_NUM = Pattern.compile(Employee.PATTERN_EMP_NUM);
}
