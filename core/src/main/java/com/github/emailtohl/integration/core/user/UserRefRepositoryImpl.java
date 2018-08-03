package com.github.emailtohl.integration.core.user;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.lib.jpa.SearchRepository;

/**
 * 自定义的用户引用数据访问层的实现
 * @author HeLei
 */
class UserRefRepositoryImpl extends SearchRepository<UserRef, Long> implements UserRefRepositoryCustomization {

	@Override
	public List<UserRef> findUserRefByRoleName(String roleName) {
		List<UserRef> ls = new ArrayList<>();
		ls.addAll(findEmployeeRefByRoleName(roleName));
		ls.addAll(findCustomerRefByRoleName(roleName));
		return ls;
	}

	@Override
	public List<EmployeeRef> findEmployeeRefByRoleName(String roleName) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<EmployeeRef> q = b.createQuery(EmployeeRef.class);
		Root<Employee> r = q.from(Employee.class);
		q = q.select(r.get("employeeRef")).where(b.equal(r.join("roles").get("name"), roleName));
		return entityManager.createQuery(q).getResultList();
	}

	@Override
	public List<CustomerRef> findCustomerRefByRoleName(String roleName) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<CustomerRef> q = b.createQuery(CustomerRef.class);
		Root<Customer> r = q.from(Customer.class);
		q = q.select(r.get("customerRef")).where(b.equal(r.join("roles").get("name"), roleName));
		return entityManager.createQuery(q).getResultList();
	}
	
}
