package com.github.emailtohl.integration.nuser.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.nuser.dao.CustomerRepository;
import com.github.emailtohl.integration.nuser.dao.EmployeeRepository;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 本类实现了UserDetailsService
 * @author HeLei
 * @date 2017.06.15
 */
@Transactional
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	public static final Pattern PATTEN_EMAIL = Pattern.compile(Constant.PATTERN_EMAIL);
	public static final Pattern PATTERN_CELL_PHONE = Pattern.compile(Constant.PATTERN_CELL_PHONE);
	public static final Pattern PATTEN_EMP_NUM = Pattern.compile(Employee.PATTERN_EMP_NUM);
	@Inject
	CustomerRepository customerRepository;
	@Inject
	EmployeeRepository employeeRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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
		if (u == null) {
			throw new UsernameNotFoundException("没有此用户：" + username);
		}
		UserDetails d = new UserDetailsImpl(u);
		return d;
	}

}
