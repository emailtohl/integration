package com.github.emailtohl.integration.core.config;

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
	String ANONYMOUS_EMAIL = "anonymous@anonymous.com";
	
	String PROP_CUSTOMER_DEFAULT_PASSWORD = "customer.default.password";
	String PROP_EMPLOYEE_DEFAULT_PASSWORD = "employee.default.password";
	
	Pattern PATTERN_EMAIL = Pattern.compile(ConstantPattern.EMAIL);
	Pattern PATTERN_CELL_PHONE = Pattern.compile(ConstantPattern.CELL_PHONE);
	Pattern PATTEN_EMP_NUM = Pattern.compile(Employee.PATTERN_EMP_NUM);
	
	String SESSION_ID_PROPERTY_NAME = "sessionId";
	String REMOTE_ADDRESS_PROPERTY_NAME = "remoteAddress";
	String USER_PRINCIPAL_PROPERTY_NAME = "userPrincipal";
}
