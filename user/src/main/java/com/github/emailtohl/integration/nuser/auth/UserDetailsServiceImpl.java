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
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Employee;
/**
 * 本类实现了UserDetailsService
 * @author HeLei
 * @date 2017.06.15
 */
@Transactional
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private static Pattern pattern_email = Pattern.compile(Constant.PATTERN_EMAIL);
	private static Pattern pattern_emNum = Pattern.compile(Employee.PATTERN_EMP_NUM);
	@Inject EmployeeRepository employeeRepository;
	@Inject CustomerRepository customerRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Matcher m = pattern_emNum.matcher(username);
		if (m.find()) {
			Integer empNum = Integer.parseInt(username);
			Employee e = employeeRepository.findByEmpNum(empNum);
			if (e == null) {
				throw new UsernameNotFoundException("查不到此平台账号");
			}
			return new UserDetailsImpl(e);
		}
		Customer c;
		m = pattern_email.matcher(username);
		if (m.find()) {
			c = customerRepository.findByEmail(username);
		} else {
			c = customerRepository.findByCellPhone(username);
		}
		if (c == null) {
			throw new UsernameNotFoundException("查不到此账号");
		}
		return new UserDetailsImpl(c);
	}

}
