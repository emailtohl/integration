package com.github.emailtohl.integration.core.config;

import java.util.regex.Pattern;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 模块相关常量
 * @author HeLei
 */
public interface Constant {
	String PROP_CUSTOMER_DEFAULT_PASSWORD = "customer.default.password";
	String PROP_EMPLOYEE_DEFAULT_PASSWORD = "employee.default.password";
	
	Pattern PATTERN_EMAIL = Pattern.compile(ConstantPattern.EMAIL);
	Pattern PATTERN_CELL_PHONE = Pattern.compile(ConstantPattern.CELL_PHONE);
	Pattern PATTEN_EMP_NUM = Pattern.compile(Employee.PATTERN_EMP_NUM);
	
	String SESSION_ID_PROPERTY_NAME = "sessionId";
	String REMOTE_ADDRESS_PROPERTY_NAME = "remoteAddress";
	String USER_PRINCIPAL_PROPERTY_NAME = "userPrincipal";
	String USERNAME = "username";
	
	String TIME_STAMP = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	/**
	 * id参数要用@org.springframework.security.access.method.P("id")注释
	 */
	String SPEL_MATCH_ID = " (authentication.principal instanceof T(com.github.emailtohl.integration.core.auth.UserDetailsImpl) and #id EQ authentication.principal.id) ";
	/**
	 * empNum参数要用org.springframework.security.access.method.P("empNum")注释
	 */
	String SPEL_MATCH_EMP_NUM = " (authentication.principal instanceof T(com.github.emailtohl.integration.core.auth.UserDetailsImpl) and #empNum.toString() EQ authentication.principal.username) ";
	/**
	 * username参数要用org.springframework.security.access.method.P("username")注释
	 */
	String SPEL_MATCH_CELL_PHONE_OR_EMAIL = " (authentication.principal instanceof T(com.github.emailtohl.integration.core.auth.UserDetailsImpl) and #username EQ authentication.principal.cellPhone or #username EQ authentication.principal.email) ";
}
