package com.github.emailtohl.integration.common.jpa.jpaCriterionQuery;

import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

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
	
	@Test(expected = IllegalArgumentException.class)
	public void testUserSearch() {
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
		Page<Customer> page = customerRepository.search(set, p);
		logger.debug(page.getContent());
		logger.debug(page.getNumber());
		logger.debug(page.getNumberOfElements());
		logger.debug(page.getSize());
		logger.debug(page.getTotalElements());
		logger.debug(page.getSort());
		assertFalse(page.getContent().isEmpty());
		
		
		Criterion c8 = new Criterion("department.name", Criterion.Operator.LIKE, td.foo.getDepartment().getName());
		set.clear();
		set.add(c8);
		page = customerRepository.search(set, p);
	}
	
	@Test
	public void testEmployeeSearch() {
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
		Page<Employee> page = employeeRepository.search(set, p);
		logger.debug(page.getContent());
		logger.debug(page.getNumber());
		logger.debug(page.getNumberOfElements());
		logger.debug(page.getSize());
		logger.debug(page.getTotalElements());
		logger.debug(page.getSort());
		assertFalse(page.getContent().isEmpty());
	}

}
