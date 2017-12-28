package com.github.emailtohl.integration.core.user;

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
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.customer.CustomerRefRepository;
import com.github.emailtohl.integration.core.user.customer.CustomerRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeRefRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 统一查询功能
 * 
 * @author HeLei
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
	CustomerRepository customerRepository;
	EmployeeRepository employeeRepository;
	CustomerRefRepository customerRefRepository;
	EmployeeRefRepository employeeRefRepository;
	UserRepository userRepository;
	UserRefRepository userRefRepository;

	@Inject
	public UserServiceImpl(CustomerRepository customerRepository, EmployeeRepository employeeRepository,
			CustomerRefRepository customerRefRepository, EmployeeRefRepository employeeRefRepository,
			UserRepository userRepository, UserRefRepository userRefRepository) {
		super();
		this.customerRepository = customerRepository;
		this.employeeRepository = employeeRepository;
		this.customerRefRepository = customerRefRepository;
		this.employeeRefRepository = employeeRefRepository;
		this.userRepository = userRepository;
		this.userRefRepository = userRefRepository;
	}

	@Override
	public Paging<User> search(String fulltext, Pageable pageable) {
		Page<User> p = userRepository.search(fulltext, pageable);
		List<User> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public Paging<UserRef> searchRef(String fulltext, Pageable pageable) {
		Page<UserRef> p = userRefRepository.search(fulltext, pageable);
		List<UserRef> ls = p.getContent().stream().map(this::transientRef).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public Paging<User> query(User params, Pageable pageable) {
		Page<User> p = userRepository.queryForPage(params, pageable);
		List<User> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public Paging<UserRef> queryRef(UserRef params, Pageable pageable) {
		Page<UserRef> p = userRefRepository.queryForPage(params, pageable);
		List<UserRef> ls = p.getContent().stream().map(this::transientRef).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}
	
	@Override
	public User get(Long id) {
		User source = userRepository.get(id);
		return transientDetail(source);
	}

	@Override
	public UserRef getRef(Long id) {
		UserRef ref = userRefRepository.findOne(id);
		return transientRef(ref);
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
			if (u == null) {
				u = employeeRepository.findByCellPhone(username);
			}
		}
		if (u == null) {
			m = Constant.PATTERN_EMAIL.matcher(username);
			if (m.find()) {
				u = customerRepository.findByEmail(username);
				if (u == null) {
					u = employeeRepository.findByEmail(username);
				}
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
	 * 
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

	@Override
	public UserRef findRef(String username) {
		if (username == null) {
			return null;
		}
		UserRef ref = null;
		Matcher m = Constant.PATTERN_CELL_PHONE.matcher(username);
		if (m.find()) {
			ref = customerRefRepository.findByCellPhone(username);
			if (ref == null) {
				ref = employeeRefRepository.findByCellPhone(username);
			}
		}
		if (ref == null) {
			m = Constant.PATTERN_EMAIL.matcher(username);
			if (m.find()) {
				ref = customerRefRepository.findByEmail(username);
				if (ref == null) {
					ref = employeeRefRepository.findByEmail(username);
				}
			}
		}
		if (ref == null) {
			m = Constant.PATTEN_EMP_NUM.matcher(username);
			if (m.find()) {
				Integer empNum = Integer.parseInt(username);
				ref = employeeRefRepository.findByEmpNum(empNum);
			}
		}
		return transientRef(ref);
	}

	@Override
	public List<UserRef> findRefByRoleName(String roleName) {
		return userRefRepository.findUserRefByRoleName(roleName).stream().map(this::transientRef)
				.collect(Collectors.toList());
	}
	
	private User toTransient(User source) {
		if (source == null) {
			return null;
		}
		User target;
		if (source.getUserType() == UserType.Employee) {
			Employee _target = new Employee();
			_target.setEmpNum(((Employee) source).getEmpNum());
			target = _target;
		} else if (source.getUserType() == UserType.Customer) {
			target = new Customer();
		} else {
			target = new User();
		}
		BeanUtils.copyProperties(source, target, "employeeRef", "customerRef", "password", "roles", "cards");
		return target;
	}

	private User transientDetail(User source) {
		if (source == null) {
			return null;
		}
		User target;
		if (source.getUserType() == UserType.Employee) {
			Employee _target = new Employee();
			_target.setEmpNum(((Employee) source).getEmpNum());
			target = _target;
		} else if (source.getUserType() == UserType.Customer) {
			target = new Customer();
		} else {
			target = new User();
		}
		BeanUtils.copyProperties(source, target, "employeeRef", "customerRef", "password", "roles", "cards");
		return target;
	}

	private UserRef transientRef(UserRef source) {
		if (source == null) {
			return null;
		}
		UserRef target;
		if (source.getUserType() == UserType.Employee) {
			EmployeeRef _target = new EmployeeRef();
			_target.setEmpNum(((EmployeeRef) source).getEmpNum());
			target = _target;
		} else if (source.getUserType() == UserType.Customer) {
			target = new CustomerRef();
		} else {
			target = new UserRef();
		}
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setCellPhone(source.getCellPhone());
		target.setName(source.getName());
		target.setNickname(source.getNickname());
		target.setIconSrc(source.getIconSrc());
		return target;
	}

}
