package com.github.emailtohl.integration.nuser.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.dao.DepartmentRepository;
import com.github.emailtohl.integration.nuser.dao.EmployeeRepository;
import com.github.emailtohl.integration.nuser.dao.RoleRepository;
import com.github.emailtohl.integration.nuser.entities.Department;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.Role;

/**
 * 平台账号服务层的实现
 * 
 * @author HeLei
 */
@Transactional
@Service
public class EmployeeServiceImpl implements EmployeeService {
	private static final transient SecureRandom RANDOM = new SecureRandom();
	private static final transient int HASHING_ROUNDS = 10;
	@Value("${employee.default.password}")
	private String employeeDefaultPassword = "123456";
	@Inject
	EmployeeRepository employeeRepository;
	@Inject
	RoleRepository roleRepository;
	@Inject
	DepartmentRepository departmentRepository;

	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "employee_cache";

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Employee create(@Valid Employee entity) {
		Employee e = new Employee();
		BeanUtils.copyProperties(entity, e, BaseEntity.getIgnoreProperties("roles", "accountNonLocked", "department"));
		// 关于工号
		synchronized (this) {
			Integer max = employeeRepository.getMaxEmpNo();
			if (max == null) {
				max = Employee.NO1;
			}
			e.setEmpNum(++max);
		}
		// 关于部门
		if (entity.getDepartment() != null && entity.getDepartment().getName() != null) {
			Department d = departmentRepository.findByName(entity.getDepartment().getName());
			if (d != null) {
				e.setDepartment(d);
			}
		}
		// 创建雇员时，可以直接激活可用
		e.setAccountNonLocked(true);
		String pw = e.getPassword();
		if (pw == null || pw.isEmpty()) {
			pw = employeeDefaultPassword;// 设置默认密码
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		e.setPassword(pw);
		e = employeeRepository.save(e);
		return transientDetail(e);
	}

	@Override
	public boolean exist(String propertyName, Object matcherValue) {
		// 以工号为唯一识别，故始终为false
		return false;
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee get(Long id) {
		Employee source = employeeRepository.get(id);
		return transientDetail(source);
	}

	@Override
	public Paging<Employee> query(Employee params, Pageable pageable) {
		Page<Employee> p = employeeRepository.queryForPage(params, pageable);
		List<Employee> content = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(content, pageable, p.getTotalElements());
	}

	@Override
	public List<Employee> query(Employee params) {
		return employeeRepository.queryForList(params).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee update(Long id, Employee newEntity) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return null;
		}
		if (newEntity.getBirthday() != null)
			source.setBirthday(newEntity.getBirthday());
		if (newEntity.getDescription() != null)
			source.setDescription(newEntity.getDescription());
		if (newEntity.getEmail() != null)
			source.setEmail(newEntity.getEmail());
		if (newEntity.getGender() != null)
			source.setGender(newEntity.getGender());
		if (newEntity.getImage() != null)
			source.setImage(newEntity.getImage());
		if (newEntity.getName() != null)
			source.setName(newEntity.getName());
		if (newEntity.getPost() != null)
			source.setPost(newEntity.getPost());
		if (newEntity.getPublicKey() != null)
			source.setPublicKey(newEntity.getPublicKey());
		if (newEntity.getSalary() != null)
			source.setSalary(newEntity.getSalary());
		if (newEntity.getTelephone() != null)
			source.setTelephone(newEntity.getTelephone());
		if (newEntity.getNickname() != null)
			source.setNickname(newEntity.getNickname());
		// 关于部门
		if (newEntity.getDepartment() != null && newEntity.getDepartment().getName() != null) {
			Department d = departmentRepository.findByName(newEntity.getDepartment().getName());
			if (d != null) {
				source.setDepartment(d);
			}
		}
		return transientDetail(source);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Employee source = employeeRepository.getOne(id);
		// 解除双方关系
		for (Iterator<Role> i = source.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(source);
			i.remove();
		}
		employeeRepository.delete(id);
	}

	@Override
	public ExecResult login(Integer empNum, String password) {
		Employee source = employeeRepository.findByEmpNum(empNum);
		if (source == null) {
			return new ExecResult(false, LoginResult.notFound.name(), null);
		}
		if (!BCrypt.checkpw(password, source.getPassword())) {
			return new ExecResult(false, LoginResult.badCredentials.name(), null);
		}
		source.setLastLoginTime(new Date());
		return new ExecResult(true, LoginResult.success.name(), transientDetail(source));
	}

	@Override
	public Employee getByEmpNum(Integer empNum) {
		Employee source = employeeRepository.findByEmpNum(empNum);
		return transientDetail(source);
	}

	@Override
	public List<Employee> findByName(String name) {
		return employeeRepository.findByNameLike(name).stream().map(this::toTransient).collect(Collectors.toList());
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee grandRoles(Long id, String... roleNames) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return null;
		}
		// 解除双方关系
		for (Iterator<Role> i = source.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(source);
			i.remove();
		}
		for (String name : roleNames) {
			Role r = roleRepository.findByName(name);
			if (r != null) {
				source.getRoles().add(r);
				r.getUsers().add(source);
			}
		}
		return transientDetail(source);
	}

	@Override
	public ExecResult updatePassword(Long id, String oldPassword, String newPassword) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return new ExecResult(false, "没有此用户", null);
		}
		if (!BCrypt.checkpw(oldPassword, source.getPassword())) {
			return new ExecResult(false, "原密码输入错误", null);
		}
		String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		source.setPassword(hashPw);
		return new ExecResult(true, "", null);
	}

	@Override
	public ExecResult resetPassword(Long id) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return new ExecResult(false, "没有此用户", null);
		}
		String hashPw = BCrypt.hashpw(employeeDefaultPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		source.setPassword(hashPw);
		return new ExecResult(true, "", null);
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee lock(Long id, boolean lock) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setAccountNonLocked(!lock);
		return transientDetail(source);
	}
	
	private Employee toTransient(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "password", "roles");
		return target;
	}
	
	private Employee transientDetail(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "password", "roles");
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getDescription())));
		return target;
	}
	
}
