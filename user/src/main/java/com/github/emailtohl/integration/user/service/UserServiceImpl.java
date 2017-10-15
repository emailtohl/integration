package com.github.emailtohl.integration.user.service;

import static com.github.emailtohl.integration.user.entities.Role.ADMIN;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.utils.BeanUtil;
import com.github.emailtohl.integration.user.dao.DepartmentRepository;
import com.github.emailtohl.integration.user.dao.RoleRepository;
import com.github.emailtohl.integration.user.dao.UserRepository;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.entities.Department;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.Role;
import com.github.emailtohl.integration.user.entities.User;
/**
 * 管理用户的相关服务，实现类中只提供功能
 * 安全，校验等功能在切面中完成
 * @author HeLei
 * @date 2017.02.04
 */
@Service
public class UserServiceImpl implements UserService, Serializable {
	private static final long serialVersionUID = 6570774356558489623L;
	private static final transient Logger logger = LogManager.getLogger();
	private static final transient SecureRandom RANDOM = new SecureRandom();
	private static final transient int HASHING_ROUNDS = 10;
	@Inject transient UserRepository userRepository;
	@Inject transient RoleRepository roleRepository;
	@Inject transient DepartmentRepository departmentRepository;
	private transient Role admin;
	
	@PostConstruct
	public void setRoles() {
		logger.debug("UserServiceImpl init");
		admin = roleRepository.findByName(ADMIN);
	}
	
	/**
	 * 判断该用户是否存在
	 * @param username
	 */
    @Override
    public boolean exist(String username) {
        return userRepository.exist(username);
    }
    
	@Override
	public User addEmployee(Employee u) {
		Employee e = new Employee();
		BeanUtils.copyProperties(u, e, BaseEntity.getIgnoreProperties("roles", "enabled", "department"));
		// 关于工号
		synchronized (this) {
			Integer max = userRepository.getMaxEmpNo();
			if (max == null) {
				max = 0;
			}
			e.setEmpNum(++max);
		}
		// 关于初始授权
		Role r = roleRepository.findByName(Role.EMPLOYEE);
		e.getRoles().add(r);
		r.getUsers().add(e);
		// 关于部门
		Department d = u.getDepartment();
		if (d != null && d.getName() != null) {
			d = departmentRepository.findByName(d.getName());
			e.setDepartment(d);
		}
		// 创建雇员时，可以直接激活可用
		e.setEnabled(true);
		String pw = u.getPassword();
		if (pw == null || pw.isEmpty()) {
			pw = "123456";// 设置默认密码
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		e.setPassword(pw);
		userRepository.save(e);
		return convert(e);
	}

	@Override
	public User addCustomer(Customer u) {	
		Customer e = new Customer();
		BeanUtils.copyProperties(u, e, BaseEntity.getIgnoreProperties("roles", "enabled", "password", "department"));
		Role r = roleRepository.findByName(Role.USER);
		e.getRoles().add(r);
		r.getUsers().add(e);
		// 用户注册时，还未激活
		e.setEnabled(false);
		String pw = u.getPassword();
		if (pw == null || pw.isEmpty()) {
			pw = "123456";// 设置默认密码
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		e.setPassword(pw);
		userRepository.save(e);
		return convert(e);
	}

	@Override
	public User enableUser(Long id) {
		User u = userRepository.findOne(id);
		u.setEnabled(true);
		return convert(u);
	}

	@Override
	public User disableUser(Long id) {
		User u = userRepository.findOne(id);
		u.setEnabled(false);
		return convert(u);
	}
	
	@Override
	public User grantRoles(long id, String... roleNames) {
		// 能进此接口的拥有USER_ROLE_AUTHORITY_ALLOCATION权限，现在认为含有该权限的人就拥有ADMIN角色
//		boolean isAdmin = SecurityContextUtil.hasAnyAuthority(USER_ROLE_AUTHORITY_ALLOCATION);
		boolean isAdmin = true;
		User u = userRepository.findOne(id);
		// 先删除原有的
		/* 虽然在User实体上已定义了级联关系，但从业务逻辑理解和保险的角度上来看，还是手工操作关系的删除
		u.getRoles().clear();
		*/
		for (Iterator<Role> i = u.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(u);
			i.remove();
		}
		// 再添加新增的
		for (String name : roleNames) {
			Role r = roleRepository.findByName(name);
			if (!isAdmin && r.equals(admin)) {
				throw new IllegalArgumentException("你没有权限分配ADMIN角色或改动ADMIN角色的账户");
			}
			if (r == null) {
				// 抛出异常后，事务会回滚
				throw new IllegalArgumentException("没有这个角色名： " + name);
			}
			u.getRoles().add(r);
			r.getUsers().add(u);
		}
		return convert(u);
	}
	
	@Override
	public User grantUserRole(long id) {
		User u = userRepository.findOne(id);
		Role r = roleRepository.findByName(Role.USER);
		u.getRoles().add(r);
		r.getUsers().add(u);
		return convert(u);
	}
	
	@Override
	public User changePassword(String email, String newPassword) {
		String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		User u = userRepository.findByEmail(email);
		u.setPassword(hashPw);
		return convert(u);
	}

	@Override
	public User changePasswordByEmail(String email, String newPassword) {
		return changePassword(email, newPassword);
	}

	@Override
	public void deleteUser(Long id) {
		User entity = userRepository.findOne(id);
		// 先删除外联关系
		entity.getRoles().clear();
		userRepository.delete(entity);
	}

	@Override
	public User getUser(Long id) {
		return convert(userRepository.findOne(id));
	}

	@Override
	public User getUserByEmail(String email) throws ResourceNotFoundException {
		User u = userRepository.findByEmail(email);
		if (u == null)
			throw new ResourceNotFoundException("未找到该用户：" + email);
		return convert(u);
	}
	
	@Override
	public User mergeEmployee(String email, Employee emp) {
		User u = userRepository.findByEmail(email);
		if (!(u instanceof Employee)) {
			throw new IllegalArgumentException("未找到该职员");
		}
		Employee entity = (Employee) u;
		Department d = emp.getDepartment();
		if (d != null && d.getName() != null) {
			entity.setDepartment(departmentRepository.findByName(d.getName()));
		}
		// 修改密码，启用/禁用账户，授权功能，不走此接口，所以在调用merge方法前，先将其设置为null
		emp.setRoles(null);
		emp.setPassword(null);
		emp.setEnabled(null);
		emp.setDepartment(null);
		BeanUtil.merge(entity, emp);
		userRepository.save(entity);
		return convert(entity);
	}
	
	public User mergeCustomer(String email, Customer cus) {
		User u = userRepository.findByEmail(email);
		if (!(u instanceof Customer)) {
			throw new IllegalArgumentException("未找到该客户");
		}
		Customer entity = (Customer) u;
		// 修改密码，启用/禁用账户，授权功能，不走此接口，所以在调用merge方法前，先将其设置为null
		cus.setRoles(null);
		cus.setPassword(null);
		cus.setEnabled(null);
		BeanUtil.merge(entity, cus);
		userRepository.save(entity);
		return convert(entity);
	}

	@Override
	public User updateIconSrc(long id, String iconSrc) {
		User u = userRepository.getOne(id);
		u.setIconSrc(iconSrc);
		return convert(u);
	}

	@Override
	public User updateIcon(long id, byte[] icon) {
		User u = userRepository.getOne(id);
		u.setIcon(icon);
		return convert(u);
	}

	@Override
	public Paging<User> getUserPage(User u, Pageable pageable) {
		String fuzzy = u.getEmail();
		if (fuzzy != null && !fuzzy.isEmpty()) {
			fuzzy = '%' + fuzzy + '%';
			u.setEmail(fuzzy);
		}
		Paging<User> pe = userRepository.dynamicQuery(u, pageable);
		List<User> ls = convert(pe.getContent());
		Paging<User> pd = new Paging<User>(ls, pe.getTotalElements(), pageable.getPageNumber(), pe.getPageSize());
		return pd;
	}

	@Override
	public boolean isExist(String email) {
		if (userRepository.findByEmail(email) == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Paging<User> getPageByRoles(String email, Set<String> roleNames, Pageable pageable) {
		String fuzzy = email;
		if (email != null && !email.isEmpty()) {
			fuzzy = '%' + email + '%';
		}
		return userRepository.getPageByCriteria(fuzzy, roleNames, pageable);
	}
	
	@Override
	public List<Role> getRoles() {
		return roleRepository.findAll();
	}
	
	@Override
	public User setPublicKey(String email, String publicKey) {
		User u = userRepository.findByEmail(email);
		if (u != null) {
			u.setPublicKey(publicKey);
		}
		return convert(u);
	}
	
	
	@Override
	public User clearPublicKey(String email) {
		User u = userRepository.findByEmail(email);
		if (u != null) {
			u.setPublicKey(null);
		}
		return convert(u);
	}
	
	/**
	 * JPA提供者能根据用户的类型确定到底是User、Employ还是Manager
	 * @param users
	 * @return
	 */
	private List<User> convert(List<? extends User> users) {
		List<User> ls = new ArrayList<User>();
		users.forEach(u -> {
			User target;
			if (u instanceof Employee) {
				target = new Employee();
			} else if (u instanceof Customer) {
				target = new Customer();
			} else {
				target = new User();
			}
			BeanUtils.copyProperties(u, target, "password", "authorities", BaseEntity.VERSION_PROPERTY_NAME);
			ls.add(target);
		});
		return ls;
	}

	/**
	 * JPA提供者能根据用户的类型确定到底是User、Employ还是Manager
	 * @param users
	 * @return
	 */
	private User convert(User user) {
		if (user == null)
			return null;
		User result;
		if (user instanceof Employee) {
			result = new Employee();
		} else if (user instanceof Customer) {
			result = new Customer();
		} else {
			result = new User();
		}
		BeanUtils.copyProperties(user, result, "password", "icon", BaseEntity.VERSION_PROPERTY_NAME);
		return result;
	}

}
