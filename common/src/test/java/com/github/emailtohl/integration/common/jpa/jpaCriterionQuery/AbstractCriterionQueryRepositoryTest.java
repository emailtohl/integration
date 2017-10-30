package com.github.emailtohl.integration.common.jpa.jpaCriterionQuery;

import static org.junit.Assert.assertFalse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.AccessType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.emailtohl.integration.common.commonTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.common.commonTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.common.jpa.testRepository.CustomerRepository;
import com.github.emailtohl.integration.common.jpa.testRepository.UserRepository;
import com.github.emailtohl.integration.common.testEntities.CommonTestData;
import com.github.emailtohl.integration.common.testEntities.Customer;
import com.github.emailtohl.integration.common.testEntities.Employee;
/**
 * JPA2的标准查询的测试类
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
@Transactional
public class AbstractCriterionQueryRepositoryTest {
	private static final Logger logger = LogManager.getLogger();
	@Inject ApplicationContext context;
	@Inject UserRepository userRepository;
	@Inject CustomerRepository customerRepository;
	class EmployeeRepository extends AbstractCriterionQueryRepository<Employee> {}
	EmployeeRepository employeeRepository;
	CommonTestData td;
	
	@Before
	public void setUp() {
		td = new CommonTestData();
		employeeRepository = new EmployeeRepository();
		AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
		factory.autowireBeanProperties(employeeRepository, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		factory.initializeBean(employeeRepository, "employeeForAbstractCriterionQueryRepositoryTest");
	}
	
	@Test
	public void testUserQuery() {
		Sort sort = new Sort(Sort.Direction.DESC, "createDate")
				 .and(new Sort(Sort.Direction.ASC, "id"));
		Pageable p = new PageRequest(0, 20, sort);
		Set<Criterion> set = new HashSet<Criterion>();
		Criterion c1, c2, c3, c4, c5, c6, c7;
		c1 = new Criterion("email", Criterion.Operator.EQ, td.emailtohl.getEmail());
		c2 = new Criterion("age", Criterion.Operator.GTE, 10);
		c3 = new Criterion("age", Criterion.Operator.LTE, 150);
		c4 = new Criterion("subsidiary.mobile", Criterion.Operator.NOT_NULL, null);
		c5 = new Criterion("affiliation", Criterion.Operator.NULL, null);
		
		Set<Integer> in = new HashSet<>(), notIn = new HashSet<>();
		for (int i = 0; i < 150; i++) {
			in.add(i);
		}
		for (int i = -150; i < 0; i++) {
			notIn.add(i);
		}
		c6 = new Criterion("age", Criterion.Operator.IN, in);
		c7 = new Criterion("age", Criterion.Operator.NOT_IN, notIn);
		set.add(c1);
		set.add(c2);
		set.add(c3);
		set.add(c4);
		set.add(c5);
		set.add(c6);
		set.add(c7);
		Page<Customer> page = customerRepository.queryForPage(set, p);
		logger.debug(page.getContent());
		logger.debug(page.getNumber());
		logger.debug(page.getNumberOfElements());
		logger.debug(page.getSize());
		logger.debug(page.getTotalElements());
		logger.debug(page.getSort());
		assertFalse(page.getContent().isEmpty());
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalArgumentException() {
		Sort sort = new Sort(Sort.Direction.DESC, "createDate")
				 .and(new Sort(Sort.Direction.ASC, "id"));
		Pageable p = new PageRequest(0, 20, sort);
		Set<Criterion> set = new HashSet<Criterion>();
		Criterion c8 = new Criterion("department.name", Criterion.Operator.LIKE, td.foo.getDepartment().getName());
		set.clear();
		set.add(c8);
		customerRepository.queryForPage(set, p);
	}
	
	@Test
	public void testEmployeeQuery() {
		Sort sort = new Sort(Sort.Direction.DESC, "createDate")
				 .and(new Sort(Sort.Direction.ASC, "id"));
		Pageable p = new PageRequest(0, 20, sort);
		Set<Criterion> set = new HashSet<>();
		Criterion c1, c2, c3, c4, c5, c6;
		c1 = new Criterion("email", Criterion.Operator.EQ, td.foo.getEmail());
		c2 = new Criterion("department.name", Criterion.Operator.LIKE, td.foo.getDepartment().getName());
		c3 = new Criterion("department.company.name", Criterion.Operator.LIKE, td.foo.getDepartment().getCompany().getName());
		c4 = new Criterion("salary", Criterion.Operator.GT, td.foo.getSalary() - 1);
		c5 = new Criterion("salary", Criterion.Operator.LT, td.foo.getSalary() + 1);
		c6 = new Criterion("username", Criterion.Operator.NOT_LIKE, td.bar.getUsername());
		set.add(c1);
		set.add(c2);
		set.add(c3);
		set.add(c4);
		set.add(c5);
		set.add(c6);
		Page<Employee> page = employeeRepository.queryForPage(set, p);
		logger.debug(page.getContent());
		logger.debug(page.getNumber());
		logger.debug(page.getNumberOfElements());
		logger.debug(page.getSize());
		logger.debug(page.getTotalElements());
		logger.debug(page.getSort());
		assertFalse(page.getContent().isEmpty());
	}

	@Test
	public void testQueryList() {
		Set<Criterion> set = new HashSet<>();
		Criterion c1, c2, c3, c4, c5, c6;
		c1 = new Criterion("email", Criterion.Operator.EQ, td.foo.getEmail());
		c2 = new Criterion("department.name", Criterion.Operator.LIKE, td.foo.getDepartment().getName());
		c3 = new Criterion("department.company.name", Criterion.Operator.LIKE, td.foo.getDepartment().getCompany().getName());
		c4 = new Criterion("salary", Criterion.Operator.GT, td.foo.getSalary() - 1);
		c5 = new Criterion("salary", Criterion.Operator.LT, td.foo.getSalary() + 1);
		c6 = new Criterion("username", Criterion.Operator.NOT_LIKE, td.bar.getUsername());
		set.add(c1);
		set.add(c2);
		set.add(c3);
		set.add(c4);
		set.add(c5);
		set.add(c6);
		List<Employee> ls = employeeRepository.queryForList(set);
		assertFalse(ls.isEmpty());
	}
	
	@Test
	public void testToPredicate() {
		Employee params = new CommonTestData().foo;
		params.setAge(null);
		params.setBirthday(null);
		params.setCreateDate(null);
		params.setModifyDate(null);
		params.setPassword(null);
		
		CriteriaBuilder cb = employeeRepository.getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
		Root<Employee> r = q.from(Employee.class);
		
		Set<Predicate> predicates = employeeRepository.toPredicate(params, AccessType.PROPERTY, r, cb);
		assertFalse(predicates.isEmpty());
		
		LocalDate start = LocalDate.ofYearDay(1980, 1);
		LocalDate end = LocalDate.now();
		
		predicates.add(cb.between(r.get("birthday"), LocalDateToDate(start), LocalDateToDate(end)));
		
		Predicate[] restrictions = new Predicate[predicates.size()];
		
		q = q.select(r).where(predicates.toArray(restrictions));
		
		List<Employee> result = employeeRepository.getEntityManager().createQuery(q).getResultList();
		assertFalse(result.isEmpty());
		
		for (Employee e : result) {
			System.out.println(e);
		}
		
		//-----
		
		q = cb.createQuery(Employee.class);
		r = q.from(Employee.class);
		predicates = employeeRepository.toPredicate(params, AccessType.FIELD, r, cb);
		assertFalse(predicates.isEmpty());
		predicates.add(cb.between(r.get("birthday"), LocalDateToDate(start), LocalDateToDate(end)));
		restrictions = new Predicate[predicates.size()];
		
		q = q.select(r).where(predicates.toArray(restrictions));
		
		result = employeeRepository.getEntityManager().createQuery(q).getResultList();
		assertFalse(result.isEmpty());
		
		for (Employee e : result) {
			System.out.println(e);
		}
	}
	
	@Test
	public void testQueryForPage() {
		CommonTestData td = new CommonTestData();
		Employee params = td.foo;
		params.setAge(null);
		params.setBirthday(null);
		params.setCreateDate(null);
		params.setModifyDate(null);
		params.setPassword(null);
		Page<Employee> p = employeeRepository.queryForPage(params, new PageRequest(0, 20));
		assertFalse(p.getContent().isEmpty());
		System.out.println(p.getContent());
		
		params = null;
		p = employeeRepository.queryForPage(params, new PageRequest(0, 20));
		assertFalse(p.getContent().isEmpty());
		System.out.println(p.getContent());
		
		params = new Employee();
		params.setName(td.bar.getName().toUpperCase());
		p = employeeRepository.queryForPage(params, new PageRequest(0, 20));
		assertFalse(p.getContent().isEmpty());
		System.out.println(p.getContent());
	}
	
	
	public Date LocalDateToDate(LocalDate d) {
	    ZoneId zone = ZoneId.systemDefault();
	    Instant instant = d.atStartOfDay().atZone(zone).toInstant();
	    return Date.from(instant);
	}

}
