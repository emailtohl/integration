package com.github.emailtohl.integration.core.user.org;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Department;

/**
 * 部门服务层实现
 * @author HeLei
 */
@Transactional
@Service
public class DepartmentServiceImpl extends StandardService<Department> implements DepartmentService {
	@Inject
	DepartmentRepository departmentRepository;
	@Inject
	CompanyRepository companyRepository;
	@Inject
	CompanyService companyService;
	
	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "department_cache";

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Department create(Department entity) {
		validate(entity);
		Department src = new Department();
		src.setName(entity.getName());
		src.setDescription(entity.getDescription());
		src.setResponsiblePerson(entity.getResponsiblePerson());
		Department p = entity.getParent();
		if (p != null && hasText(p.getName())) {
			Department pd = departmentRepository.findByName(p.getName());
			if (pd != null) {
				src.setParent(pd);
			}
		}
		Company c = entity.getCompany();
		if (c != null && hasText(c.getName())) {
			Company pc = companyRepository.findByName(c.getName());
			if (pc != null) {
				src.setCompany(pc);
			}
		}
		return transientDetail(departmentRepository.save(src));
	}
	
	private ExampleMatcher existMatcher = ExampleMatcher.matching().withMatcher("name", GenericPropertyMatchers.caseSensitive());

	@Override
	public boolean exist(Object name) {
		Department probe = new Department();
		probe.setName((String) name);
		Example<Department> example = Example.of(probe, existMatcher);
		return departmentRepository.exists(example);
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Department get(Long id) {
		return transientDetail(departmentRepository.findOne(id));
	}
	
	@Override
	public Department findByName(String name) {
		return transientDetail(departmentRepository.findByName(name));
	}

	private ExampleMatcher queryMatcher = ExampleMatcher.matching()
			.withIgnorePaths("employees", "parent")
			.withMatcher("name", GenericPropertyMatchers.caseSensitive())
			.withMatcher("description", GenericPropertyMatchers.caseSensitive())
			.withMatcher("responsiblePerson", GenericPropertyMatchers.caseSensitive())
			.withMatcher("company.name", GenericPropertyMatchers.caseSensitive());
	
	@Override
	public Paging<Department> query(Department params, Pageable pageable) {
		Page<Department> page;
		if (params == null) {
			page = departmentRepository.findAll(pageable);
		} else {
			Example<Department> example = Example.of(params, queryMatcher);
			page = departmentRepository.findAll(example, pageable);
		}
		List<Department> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<Department> query(Department params) {
		List<Department> ls;
		if (params == null) {
			ls = departmentRepository.findAll();
		} else {
			Example<Department> example = Example.of(params, queryMatcher);
			ls = departmentRepository.findAll(example);
		}
		return ls.stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Department update(Long id, Department newEntity) {
		validate(newEntity);
		Department src = departmentRepository.findOne(id);
		if (src == null) {
			return null;
		}
		if (newEntity.getName() != null) {
			src.setName(newEntity.getName());
		}
		if (newEntity.getDescription() != null) {
			src.setDescription(newEntity.getDescription());
		}
		if (newEntity.getResponsiblePerson() != null) {
			src.setResponsiblePerson(newEntity.getResponsiblePerson());
		}
		Department targetParent = null;
		if (newEntity.getParent() != null) {
			if (newEntity.getParent().getId() != null) {
				targetParent = departmentRepository.getOne(newEntity.getParent().getId());
			} else if (hasText(newEntity.getParent().getName())) {
				targetParent = departmentRepository.findByName(newEntity.getParent().getName());
			}
		}
		// 父级节点不能是本实例的下级节点，否则会出现循环引用
		if (targetParent != null) {
			if (src.isAncestorOf(targetParent)) {
				throw new NotAcceptableException("父级节点不能是本实例的下级节点，否则会出现循环引用");
			}
			src.setParent(targetParent);
		}
		Company c = newEntity.getCompany();
		if (c != null && hasText(c.getName())) {
			Company pc = companyRepository.findByName(c.getName());
			if (pc != null) {
				src.setCompany(pc);
			}
		}
		return transientDetail(src);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Department src = departmentRepository.findOne(id);
		if (src == null) {
			return;
		}
		Company c = src.getCompany();
		if (c != null) {
			c.getDepartments().remove(src);
		}
		src.setCompany(null);
		// 将本部门的下级部门连接到本部门的上级部门（本部门的上级部门可以是null）
		departmentRepository.findByParentId(id).forEach(d -> d.setParent(src.getParent()));
		src.getEmployees().forEach(e -> e.setDepartment(null));
		src.getEmployees().clear();
		departmentRepository.delete(src);
	}

	@Override
	protected Department toTransient(Department src) {
		if (src == null) {
			return null;
		}
		Department tar = new Department();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getCreateDate());
		return tar;
	}
	
	@Override
	protected Department transientDetail(Department src) {
		if (src == null) {
			return null;
		}
		Department tar = new Department();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getCreateDate());
		tar.setCompany(companyService.transientCompanyDetail(src.getCompany()));
		tar.setParent(transientDetail(src.getParent()));
		return tar;
	}

}
