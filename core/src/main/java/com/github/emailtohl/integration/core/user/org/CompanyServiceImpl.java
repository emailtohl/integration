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

/**
 * 公司服务层实现
 * @author HeLei
 */
@Transactional
@Service
public class CompanyServiceImpl extends StandardService<Company> implements CompanyService {
	@Inject
	CompanyRepository companyRepository;
	
	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "company_cache";

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Company create(Company entity) {
		validate(entity);
		Company src = new Company();
		src.setName(entity.getName());
		src.setDescription(entity.getDescription());
		src.setResponsiblePerson(entity.getResponsiblePerson());
		Company p = entity.getParent();
		if (p != null && hasText(p.getName())) {
			Company pc = companyRepository.findByName(p.getName());
			if (pc != null) {
				src.setParent(pc);
			}
		}
		return transientDetail(companyRepository.save(src));
	}
	
	private ExampleMatcher existMatcher = ExampleMatcher.matching().withMatcher("name", GenericPropertyMatchers.caseSensitive());

	@Override
	public boolean exist(Object matcherValue) {
		Company probe = new Company();
		probe.setName((String) matcherValue);
		Example<Company> example = Example.of(probe, existMatcher);
		return companyRepository.exists(example);
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Company get(Long id) {
		return transientDetail(companyRepository.findOne(id));
	}

	@Override
	public Company findByName(String name) {
		return transientDetail(companyRepository.findByName(name));
	}
	
	private ExampleMatcher queryMatcher = ExampleMatcher.matching()
			.withIgnorePaths("parent", "departments")
			.withMatcher("name", GenericPropertyMatchers.caseSensitive())
			.withMatcher("description", GenericPropertyMatchers.caseSensitive())
			.withMatcher("responsiblePerson", GenericPropertyMatchers.caseSensitive());
	
	@Override
	public Paging<Company> query(Company params, Pageable pageable) {
		Page<Company> page;
		if (params == null) {
			page = companyRepository.findAll(pageable);
		} else {
			Example<Company> example = Example.of(params, queryMatcher);
			page = companyRepository.findAll(example, pageable);
		}
		List<Company> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<Company> query(Company params) {
		List<Company> ls;
		if (params == null) {
			ls = companyRepository.findAll();
		} else {
			Example<Company> example = Example.of(params, queryMatcher);
			ls = companyRepository.findAll(example);
		}
		return ls.stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Company update(Long id, Company newEntity) {
		validate(newEntity);
		Company src = companyRepository.findOne(id);
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
		
		Company targetParent = null;
		if (newEntity.getParent() != null) {
			if (newEntity.getParent().getId() != null) {
				targetParent = companyRepository.findOne(newEntity.getParent().getId());
			} else if (hasText(newEntity.getParent().getName())) {
				targetParent = companyRepository.findByName(newEntity.getParent().getName());
			}
		}
		// 父级节点不能是本实例的下级节点，否则会出现循环引用
		if (targetParent != null) {
			if (src.isAncestorOf(targetParent)) {
				throw new NotAcceptableException("父级节点不能是本实例的下级节点，否则会出现循环引用");
			}
			src.setParent(targetParent);
		}
		return transientDetail(src);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Company src = companyRepository.findOne(id);
		if (src == null) {
			return;
		}
		// 将本公司的下级公司连接到本公司的上级公司（本公司的上级公司可以是null）
		companyRepository.findByParentId(id).forEach(sub -> sub.setParent(src.getParent()));
		src.getDepartments().forEach(d -> d.setCompany(null));
		src.getDepartments().clear();
		companyRepository.delete(src);
	}

	@Override
	public Company transientCompanyDetail(Company src) {
		return transientDetail(src);
	}
	
	@Override
	protected Company toTransient(Company src) {
		if (src == null) {
			return null;
		}
		Company tar = new Company();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getCreateDate());
		return tar;
	}

	@Override
	protected Company transientDetail(Company src) {
		if (src == null) {
			return null;
		}
		Company tar = new Company();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getModifyDate());
		tar.setParent(transientDetail(src.getParent()));
		return tar;
	}
	
}
