package com.github.emailtohl.integration.nuser.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.nuser.dao.CustomerRepository;
import com.github.emailtohl.integration.nuser.dao.EmployeeRepository;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 封装查询用户逻辑
 * @author HeLei
 */
@Component
public class LoadUser {
	public static final Pattern PATTEN_EMAIL = Pattern.compile(Constant.PATTERN_EMAIL);
	public static final Pattern PATTERN_CELL_PHONE = Pattern.compile(Constant.PATTERN_CELL_PHONE);
	public static final Pattern PATTEN_EMP_NUM = Pattern.compile(Employee.PATTERN_EMP_NUM);
	@Inject
	CustomerRepository customerRepository;
	@Inject
	EmployeeRepository employeeRepository;
	
	/**
	 * 返回持久化状态的用户实例
	 * @param username 在Spring Security中用户唯一性的标识，本系统中可以是平台工号、客户手机或客户邮箱
	 * @return 若没查找到则返回null
	 */
	public User load(String username) {
		if (username == null) {
			return null;
		}
		User u = null; 
		Matcher m = PATTERN_CELL_PHONE.matcher(username);
		if (m.find()) {
			u = customerRepository.findByCellPhone(username);
		}
		if (u == null) {
			m = PATTEN_EMAIL.matcher(username);
			if (m.find()) {
				u = customerRepository.findByEmail(username);
			}
		}
		if (u == null) {
			m = PATTEN_EMP_NUM.matcher(username);
			if (m.find()) {
				Integer empNum = Integer.parseInt(username);
				u = employeeRepository.findByEmpNum(empNum);
			}
		}
		return u;
	}
}
