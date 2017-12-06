package com.github.emailtohl.integration.core.user.org;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.user.entities.Company;
import com.github.emailtohl.integration.core.user.entities.Department;

/**
 * 部门服务层实现
 * @author HeLei
 */
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
	@Inject
	DepartmentRepository departmentRepository;
	@Inject
	CompanyRepository companyRepository;
	@Inject
	CompanyService companyService;

	@Override
	public Department create(Department entity) {
		Department src = new Department();
		src.setName(entity.getName());
		src.setDescription(entity.getDescription());
		src.setResponsiblePerson(entity.getResponsiblePerson());
		Department p = entity.getParent();
		if (p != null && StringUtils.hasText(p.getName())) {
			Department pd = departmentRepository.findByName(p.getName());
			if (pd != null) {
				src.setParent(pd);
			}
		}
		Company c = entity.getCompany();
		if (c != null && StringUtils.hasText(c.getName())) {
			Company pc = companyRepository.findByName(c.getName());
			if (pc != null) {
				src.setCompany(pc);
			}
		}
		return transientDetail(departmentRepository.save(src));
	}
	
	private ExampleMatcher existMatcher = ExampleMatcher.matching().withMatcher("name", GenericPropertyMatchers.caseSensitive());

	@Override
	public boolean exist(Object matcherValue) {
		Department probe = new Department();
		probe.setName((String) matcherValue);
		Example<Department> example = Example.of(probe, existMatcher);
		return departmentRepository.exists(example);
	}

	@Override
	public Department get(Long id) {
		return transientDetail(departmentRepository.findOne(id));
	}

	private ExampleMatcher queryMatcher = ExampleMatcher.matching()
			.withIgnorePaths("parent", "company")
			.withMatcher("name", GenericPropertyMatchers.caseSensitive())
			.withMatcher("description", GenericPropertyMatchers.caseSensitive())
			.withMatcher("responsiblePerson", GenericPropertyMatchers.caseSensitive());
	
	@Override
	public Paging<Department> query(Department params, Pageable pageable) {
		Example<Department> example = Example.of(params, queryMatcher);
		Page<Department> page = departmentRepository.findAll(example, pageable);
		List<Department> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<Department> query(Department params) {
		Example<Department> example = Example.of(params, queryMatcher);
		return departmentRepository.findAll(example).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@Override
	public Department update(Long id, Department newEntity) {
		Department src = departmentRepository.findOne(id);
		if (src == null) {
			return null;
		}
		src.setName(newEntity.getName());
		src.setDescription(newEntity.getDescription());
		src.setResponsiblePerson(newEntity.getResponsiblePerson());
		Department p = newEntity.getParent();
		if (p != null && StringUtils.hasText(p.getName())) {
			Department pd = departmentRepository.findByName(p.getName());
			if (pd != null) {
				src.setParent(pd);
			}
		}
		Company c = newEntity.getCompany();
		if (c != null && StringUtils.hasText(c.getName())) {
			Company pc = companyRepository.findByName(c.getName());
			if (pc != null) {
				src.setCompany(pc);
			}
		}
		return transientDetail(src);
	}

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
		Department parent = src.getParent();
		// 将本部门的下级部门连接到本部门的上级部门（本部门的上级部门可以是null）
		departmentRepository.findByParentId(src.getId()).forEach(d -> d.setParent(parent));
		departmentRepository.delete(src);
	}

	private Department toTransient(Department src) {
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
	
	private Department transientDetail(Department src) {
		if (src == null) {
			return null;
		}
		Department tar = new Department();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getCreateDate());
		tar.setCompany(companyService.transientDetail(src.getCompany()));
		tar.setParent(transientDetail(src.getParent()));
		return tar;
	}
}
