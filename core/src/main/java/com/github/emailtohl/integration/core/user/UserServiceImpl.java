package com.github.emailtohl.integration.core.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.customer.CustomerRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeRepository;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 统一查询功能
 * 
 * @author HeLei
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
	@Inject
	CustomerRepository customerRepository;
	@Inject
	EmployeeRepository employeeRepository;
	@Inject
	UserRepository userRepository;

	public UserServiceImpl() {
	}

	public UserServiceImpl(CustomerRepository customerRepository, EmployeeRepository employeeRepository,
			UserRepository userRepository) {
		this.customerRepository = customerRepository;
		this.employeeRepository = employeeRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Paging<User> search(String fulltext, Pageable pageable) {
		Page<SearchResult<User>> p = userRepository.searchWithScore(fulltext, pageable);
		List<User> ls = new ArrayList<>();
		p.forEach(r -> ls.add(toTransient(r.getEntity())));
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public Paging<User> query(User params, Pageable pageable) {
		Page<User> p = userRepository.queryForPage(params, pageable);
		List<User> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public User get(Long id) {
		User source = userRepository.get(id);
		return transientDetail(source);
	}

	/**
	 * 注意返回的实例是瞬时态的实体对象，若出了事务层后再调用延迟加载字段会报异常
	 * 
	 * @param username 在Spring Security中用户唯一性的标识，本系统中可以是平台工号、客户手机或客户邮箱
	 * @return 若没查找到则返回null
	 */
	@Override
	public User find(String username) {
		if (username == null) {
			return null;
		}
		User u = null;
		Matcher m = Constant.PATTERN_CELL_PHONE.matcher(username);
		if (m.find()) {
			u = customerRepository.findByCellPhone(username);
		}
		if (u == null) {
			m = Constant.PATTERN_EMAIL.matcher(username);
			if (m.find()) {
				u = customerRepository.findByEmail(username);
			}
		}
		if (u == null) {
			m = Constant.PATTEN_EMP_NUM.matcher(username);
			if (m.find()) {
				Integer empNum = Integer.parseInt(username);
				u = employeeRepository.findByEmpNum(empNum);
			}
		}
		if (u != null) {
			u.authorityNames();// 加载角色与权限
		}
		return u;
	}

	/**
	 * 为查找登录而准备的接口，在返回用户时，同时更新最后登录时间
	 * @param username
	 * @return
	 */
	@Override
	public User findAndRefreshLastLogin(String username) {
		User u = find(username);
		if (u != null) {
			u.setLastLogin(new Date());
		}
		return u;
	}
	
	private User toTransient(User source) {
		if (source == null) {
			return null;
		}
		User target = new User();
		BeanUtils.copyProperties(source, target, "employeeRef", "customerRef", "password", "roles", "cards");
		return target;
	}

	private User transientDetail(User source) {
		if (source == null) {
			return null;
		}
		User target = new User();
		BeanUtils.copyProperties(source, target, "employeeRef", "customerRef", "password", "roles", "cards");
		return target;
	}

}
