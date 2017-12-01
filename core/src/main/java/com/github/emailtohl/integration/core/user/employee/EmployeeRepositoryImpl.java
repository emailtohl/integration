package com.github.emailtohl.integration.core.user.employee;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 数据访问层
 * @author HeLei
 */
class EmployeeRepositoryImpl extends AbstractSearchableRepository<Employee>
		implements EmployeeRepositoryCustomization {

	@Override
	public Integer getMaxEmpNo() {
		Integer result;
		// result = entityManager.createQuery("select max(e.empNum) from
		// Employee e", Integer.class).getSingleResult();

		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<Integer> q = b.createQuery(Integer.class);
		Root<Employee> r = q.from(Employee.class);
		result = entityManager.createQuery(q.select(b.max(r.get("empNum")))).getSingleResult();

		return result;
	}
}