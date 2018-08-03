package com.github.emailtohl.integration.web.service.cms;

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

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.github.emailtohl.lib.exception.NotAcceptableException;
import com.github.emailtohl.lib.jpa.Paging;

/**
 * 文章类型服务实现
 * 
 * @author HeLei
 */
@Service
@Transactional
public class TypeServiceImpl extends StandardService<Type> implements TypeService {
	TypeRepository typeRepository;
	WebPresetData webPresetData;

	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "article_type_cache";
	
	@Inject
	public TypeServiceImpl(TypeRepository typeRepository, WebPresetData webPresetData) {
		super();
		this.typeRepository = typeRepository;
		this.webPresetData = webPresetData;
	}

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Type create(Type entity) {
		validate(entity);
		Type parent = entity.getParent();
		if (parent != null) {
			Type p = null;
			if (parent.getId() != null) {
				p = typeRepository.getOne(parent.getId());
			} else if (hasText(parent.getName())) {
				p = typeRepository.getByName(parent.getName());
			}
			if (p != null) {
				entity.setParent(p);
			}
		}
		return transientDetail(typeRepository.save(entity));
	}

	private final ExampleMatcher existMatcher = ExampleMatcher.matching().withMatcher("name", GenericPropertyMatchers.exact());
	@Override
	public boolean exist(Object matcherValue) {
		String typeName = (String) matcherValue;
		Type params = new Type();
		params.setName(typeName);
		Example<Type> example = Example.of(params, existMatcher);
		return typeRepository.exists(example);
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Type get(Long id) {
		return transientDetail(typeRepository.findById(id).orElse(null));
	}

	@Override
	public Type getByName(String name) {
		return transientDetail(typeRepository.getByName(name));
	}
	
	private final ExampleMatcher typeMatcher = ExampleMatcher.matching()
			.withIgnoreNullValues()
			.withIgnorePaths("articles", "parent")
			.withMatcher("id", GenericPropertyMatchers.exact())
			.withMatcher("name", GenericPropertyMatchers.caseSensitive())
			.withMatcher("description", GenericPropertyMatchers.ignoreCase());
	@Override
	public Paging<Type> query(Type params, Pageable pageable) {
		Page<Type> page;
		if (params == null) {
			page = typeRepository.findAll(pageable);
		} else {
			Example<Type> example = Example.of(params, typeMatcher);
			page = typeRepository.findAll(example, pageable);
		}
		List<Type> ls = page.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<Type> query(Type params) {
		List<Type> ls;
		if (params == null) {
			ls = typeRepository.findAll();
		} else {
			Example<Type> example = Example.of(params, typeMatcher);
			ls = typeRepository.findAll(example);
		}
		return ls.stream().map(this::toTransient).collect(Collectors.toList());
	}

	@Override
	public List<Type> getTypesWithArticleNum(Type params) {
		return typeRepository.getTypesWithArticleNum(params).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Type update(Long id, Type newEntity) {
		Type source = typeRepository.findById(id).orElse(null);
		if (source == null) {
			return null;
		}
		isIllegal(source);
		if (newEntity.getDescription() != null) {
			source.setDescription(newEntity.getDescription());
		}
		Type targetParent = null;
		if (newEntity.getParent() != null) {
			// 先将本type所有子类型的parent更改为本type的parent
			if (newEntity.getParent().getId() != null) {
				targetParent = typeRepository.findById(newEntity.getParent().getId()).orElse(null);
			} else if (hasText(newEntity.getParent().getName())) {
				targetParent = typeRepository.getByName(newEntity.getParent().getName());
			}
		}
		// 父级节点不能是本实例的下级节点，否则会出现循环引用
		if (targetParent != null) {
			if (source.isAncestorOf(targetParent)) {
				throw new NotAcceptableException("父级节点不能是本实例的下级节点，否则会出现循环引用");
			}
			source.setParent(targetParent);
		}
		return transientDetail(source);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Type source = typeRepository.findById(id).orElse(null);
		if (source == null) {
			return;
		}
		isIllegal(source);
		Type unclassified = typeRepository.findById(webPresetData.unclassified.getId()).orElse(null);
		source.getArticles().forEach(a -> {
			a.setType(unclassified);
		});
		// 将原先以本type为parent的下级type改为连接到本type的上级（本type的上级type可以是null）
		typeRepository.findByParentId(id).forEach(sub -> sub.setParent(source.getParent()));
		typeRepository.delete(source);
	}

	@Override
	protected Type toTransient(Type entity) {
		if (entity == null) {
			return null;
		}
		Type tar = new Type(entity.getName(), entity.getDescription(), null);
		tar.setId(entity.getId());
		tar.setArticlesNum(entity.getArticlesNum());
		tar.setCreateDate(entity.getCreateDate());
		tar.setModifyDate(entity.getModifyDate());
		if (entity.getParent() != null) {
			Type parent = new Type(entity.getParent().getName(), entity.getParent().getDescription(), null);
			parent.setId(entity.getParent().getId());
			tar.setParent(parent);
		}
		return tar;
	}

	@Override
	protected Type transientDetail(Type entity) {
		if (entity == null) {
			return null;
		}
		Type tar = new Type(entity.getName(), entity.getDescription(), transientDetail(entity.getParent()));
		tar.setId(entity.getId());
		tar.setArticlesNum(tar.getArticles().size());
		tar.setCreateDate(entity.getCreateDate());
		tar.setModifyDate(entity.getModifyDate());
		if (entity.getParent() != null) {
			Type parent = new Type(entity.getParent().getName(), entity.getParent().getDescription(), null);
			parent.setId(entity.getParent().getId());
			tar.setParent(parent);
		}
		return tar;
	}
	/**
	 * 若是系统内置数据，则触发异常
	 * @param t
	 */
	private void isIllegal(Type t) {
		if (webPresetData.unclassified.equals(t)) {
			throw new NotAcceptableException("不能修改系统内置类型");
		}
	}

}
