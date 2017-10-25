package com.github.emailtohl.integration.nuser.service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.dao.DepartmentRepository;
import com.github.emailtohl.integration.nuser.dao.EmployeeRepository;
import com.github.emailtohl.integration.nuser.dao.RoleRepository;
import com.github.emailtohl.integration.nuser.entities.Department;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.Role;

/**
 * 内部人员服务层的实现
 * 
 * @author HeLei
 */
@Transactional
@Service
public class EmployeeServiceImpl implements EmployeeService {
	private static final transient SecureRandom RANDOM = new SecureRandom();
	private static final transient int HASHING_ROUNDS = 10;
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
	public Employee create(Employee entity) {
		Employee e = new Employee();
		BeanUtils.copyProperties(entity, e, BaseEntity.getIgnoreProperties("roles", "accountNonLocked", "department"));
		// 关于工号
		synchronized (this) {
			Integer max = employeeRepository.getMaxEmpNo();
			if (max == null) {
				max = 0;
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
			pw = "123456";// 设置默认密码
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		e.setPassword(pw);
		e = employeeRepository.save(e);
		return filter(e);
	}

	@Override
	public boolean exist(String propertyName, Object matcherValue) {
		// 以工号为唯一识别，故始终为false
		return false;
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee get(Long id) {
		Employee e = employeeRepository.findOne(id);
		return filter(e);
	}

	@Override
	public Page<Employee> query(Employee params, Pageable pageable) {
		Page<Employee> p = employeeRepository.queryForPage(params, pageable);
		List<Employee> content = p.getContent().stream().map(this::filter).collect(Collectors.toList());
		return new PageImpl<>(content, pageable, p.getTotalElements());
	}

	@Override
	public List<Employee> query(Employee params) {
		return employeeRepository.queryForList(params).stream().map(this::filter).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee update(Long id, Employee newEntity) {
		Employee target = employeeRepository.findOne(id);
		if (target == null) {
			return null;
		}
		target.setBirthday(newEntity.getBirthday());
		target.setDescription(newEntity.getDescription());
		target.setEmail(newEntity.getEmail());
		target.setGender(newEntity.getGender());
		target.setImage(newEntity.getImage());
		target.setName(newEntity.getName());
		target.setPost(newEntity.getPassword());
		target.setPublicKey(newEntity.getPublicKey());
		target.setSalary(newEntity.getSalary());
		target.setTelephone(newEntity.getTelephone());
		target.setNickname(newEntity.getNickname());
		// 关于部门
		if (newEntity.getDepartment() != null && newEntity.getDepartment().getName() != null) {
			Department d = departmentRepository.findByName(newEntity.getDepartment().getName());
			if (d != null) {
				target.setDepartment(d);
			}
		}
		return filter(target);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Employee target = employeeRepository.getOne(id);
		// 解除双方关系
		for (Iterator<Role> i = target.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(target);
			i.remove();
		}
		employeeRepository.delete(id);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee grandRoles(Long id, String... roleNames) {
		Employee target = employeeRepository.findOne(id);
		if (target == null) {
			return null;
		}
		// 解除双方关系
		for (Iterator<Role> i = target.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(target);
			i.remove();
		}
		for (String name : roleNames) {
			Role r = roleRepository.findByName(name);
			if (r != null) {
				target.getRoles().add(r);
				r.getUsers().add(target);
			}
		}
		return filter(target);
	}

	@Override
	public ExecResult updatePassword(Long id, String oldPassword, String newPassword) {
		Employee target = employeeRepository.findOne(id);
		if (target == null) {
			return new ExecResult(false, "没有此用户");
		}
		if (!BCrypt.checkpw(oldPassword, target.getPassword())) {
			return new ExecResult(false, "原密码输入错误");
		}
		String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		target.setPassword(hashPw);
		return new ExecResult(true, "");
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee lock(Long id, boolean lock) {
		Employee target = employeeRepository.findOne(id);
		if (target == null) {
			return null;
		}
		target.setAccountNonLocked(lock);
		return filter(target);
	}
	
	private Employee filter(Employee source) {
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, Employee.getIgnoreProperties("password"));
		target.setId(source.getId());
		return target;
	}
	
}
