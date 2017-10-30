package com.github.emailtohl.integration.common.jpa.envers;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
/**
 * 查询Hibernate envers对实体的审计记录，可保持数据多版本
 * 
 * Envers组件的JAR包只要在类路径上即可使用，对要审计的实体添加@org.hibernate.envers.Audited注解
 * 
 * 注意：调用者需根据业务情况明确事务边界，添加上@javax.transaction.Transactional
 * 
 * @param <E> 实体类型
 * @author HeLei
 * @date 2017.02.04
 */
public abstract class AbstractAuditedRepository<E extends Serializable> implements AuditedRepository<E> {
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();
	
	@PersistenceUnit
	protected EntityManagerFactory entityManagerFactory;
	@PersistenceContext
	protected EntityManager entityManager;
	protected Class<E> entityClass;
	
	/**
	 * 查询某实体所有的历史记录，并根据谓词进行筛选
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @param pageable
	 * @return 分页的元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Tuple<E>> getEntityRevision(Map<String, Object> propertyNameValueMap) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(entityClass, false, false);
		if (propertyNameValueMap != null) {
			for (Entry<String, Object> e : propertyNameValueMap.entrySet()) {
				Object v = e.getValue();
				if (v != null) {
					if (v instanceof String && !((String)v).isEmpty()) {
						query.add(AuditEntity.property(e.getKey()).like(((String)v).trim(), MatchMode.START));
					} else {
						query.add(AuditEntity.property(e.getKey()).eq(v));
					}
				}
			}
		}
		List<Object[]> result = query.getResultList();
		List<Tuple<E>> ls = new ArrayList<Tuple<E>>();
		for (Object[] o : result) {
			Tuple<E> tuple = new Tuple<E>();
			tuple.setEntity((E) o[0]); // 在版本时的详情
			tuple.setDefaultRevisionEntity((DefaultRevisionEntity) o[1]); // 版本详情：(id = 201, revisionDate = 2017-2-10 21:17:40)
			tuple.setRevisionType((RevisionType) o[2]); // 增(ADD)、改(MOD)、删(DEL)
			ls.add(tuple);
		}
		return ls;
	}
	
	/**
	 * 查询某实体所有的历史记录，并根据谓词进行筛选
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @param pageable
	 * @return 分页的元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Page<Tuple<E>> getEntityRevision(Map<String, Object> propertyNameValueMap, Pageable pageable) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(entityClass, false, false);
		if (propertyNameValueMap != null) {
			for (Entry<String, Object> e : propertyNameValueMap.entrySet()) {
				Object v = e.getValue();
				if (v != null) {
					if (v instanceof String && !((String)v).isEmpty()) {
						query.add(AuditEntity.property(e.getKey()).like(((String)v).trim(), MatchMode.START));
					} else {
						query.add(AuditEntity.property(e.getKey()).eq(v));
					}
				}
			}
		}
		Sort sort = pageable.getSort();
		if (sort != null) {
			Iterator<Order> i = sort.iterator();
			while (i.hasNext()) {
				Order o = i.next();
				if (o.isAscending()) {
					query.addOrder(AuditEntity.property(o.getProperty()).asc());
				} else {
					query.addOrder(AuditEntity.property(o.getProperty()).desc());
				}
			}
		}
		query.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
		List<Object[]> result = query.getResultList();
		List<Tuple<E>> ls = new ArrayList<Tuple<E>>();
		for (Object[] o : result) {
			Tuple<E> tuple = new Tuple<E>();
			tuple.setEntity((E) o[0]); // 在版本时的详情
			tuple.setDefaultRevisionEntity((DefaultRevisionEntity) o[1]); // 版本详情：(id = 201, revisionDate = 2017-2-10 21:17:40)
			tuple.setRevisionType((RevisionType) o[2]); // 增(ADD)、改(MOD)、删(DEL)
			ls.add(tuple);
		}
		/*
		 * 由于AuditQuery没有提供获取总条数的接口，所以此处对total进行猜测：
		 * 如果列表数目等于页码尺寸，那就认为还有下一页，否则到本页为止
		 */
		int total = pageable.getOffset();
		if (result.size() == pageable.getPageSize()) {
			total += 2 * pageable.getPageSize();
		} else {
			total += result.size();
		}
		return new PageImpl<Tuple<E>>(ls, pageable, total);
	}

	/**
	 * 查询某个修订版下，该实体类的所有的历史记录，但不包括删除时的
	 * 例如创建一批用户，这是“增加”类型的修订版，该版本就关联着这一批用户实体
	 * @param revision 版本号，通过AuditReader#getRevisions(Entity.class, ID)获得
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @param pageable
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<E> getEntitiesAtRevision(Number revision, Map<String, Object> propertyNameValueMap) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery query = auditReader.createQuery().forEntitiesAtRevision(entityClass, revision);
		for (Entry<String, Object> e : propertyNameValueMap.entrySet()) {
			Object v = e.getValue();
			if (v != null) {
				if (v instanceof String && !((String)v).isEmpty()) {
					query.add(AuditEntity.property(e.getKey()).like(((String)v).trim(), MatchMode.START));
				} else {
					query.add(AuditEntity.property(e.getKey()).eq(v));
				}
			}
		}
		return query.getResultList();
	}
	
	/**
	 * 查询某个修订版下，该实体类的所有的历史记录，但不包括删除时的
	 * 例如创建一批用户，这是“增加”类型的修订版，该版本就关联着这一批用户实体
	 * @param revision 版本号，通过AuditReader#getRevisions(Entity.class, ID)获得
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @param pageable
	 * @return 
	 */
	@Override
	public Page<E> getEntitiesAtRevision(Number revision, Map<String, Object> propertyNameValueMap, Pageable pageable) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery query = auditReader.createQuery().forEntitiesAtRevision(entityClass, revision);
		for (Entry<String, Object> e : propertyNameValueMap.entrySet()) {
			Object v = e.getValue();
			if (v != null) {
				if (v instanceof String && !((String)v).isEmpty()) {
					query.add(AuditEntity.property(e.getKey()).like(((String)v).trim(), MatchMode.START));
				} else {
					query.add(AuditEntity.property(e.getKey()).eq(v));
				}
			}
		}
		Sort sort = pageable.getSort();
		if (sort != null) {
			Iterator<Order> i = sort.iterator();
			while (i.hasNext()) {
				Order o = i.next();
				if (o.isAscending()) {
					query.addOrder(AuditEntity.property(o.getProperty()).asc());
				} else {
					query.addOrder(AuditEntity.property(o.getProperty()).desc());
				}
			}
		}
		query.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
		@SuppressWarnings("unchecked")
		List<E> result = query.getResultList();
		/*
		 * 由于AuditQuery没有提供获取总条数的接口，所以此处对total进行猜测：
		 * 如果列表数目等于页码尺寸，那就认为还有下一页，否则到本页为止
		 */
		int total = pageable.getOffset();
		if (result.size() == pageable.getPageSize()) {
			total += 2 * pageable.getPageSize();
		} else {
			total += result.size();
		}
		return new PageImpl<E>(result, pageable, total);
	}

	/**
	 * 查询某个实体在某个修订版时的历史记录
	 * @param id 实体的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Entity.class, ID)获得
	 * @return
	 */
	@Override
	public E getEntityAtRevision(Long id, Number revision) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		return auditReader.find(entityClass, id, revision);
	}
	
	/**
	 * 回滚到某历史版本上
	 */
	@Override
	public void rollback(Long id, Number revision) {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();
			AuditReader auditReader = AuditReaderFactory.get(entityManager);
			E bygone = auditReader.find(entityClass, id, revision);
			em.unwrap(Session.class).replicate(bygone, ReplicationMode.OVERWRITE);
			em.getTransaction().commit();
		} finally {
			if (em.isOpen())
				em.close();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected AbstractAuditedRepository() {
		Type genericSuperclass = getClass().getGenericSuperclass();
		while (!(genericSuperclass instanceof ParameterizedType)) {
			if (!(genericSuperclass instanceof Class))
				throw new IllegalStateException("Unable to determine type "
						+ "arguments because generic superclass neither " + "parameterized type nor class.");
			if (genericSuperclass == AbstractSearchableRepository.class)
				throw new IllegalStateException("Unable to determine type "
						+ "arguments because no parameterized generic superclass " + "found.");
			genericSuperclass = ((Class) genericSuperclass).getGenericSuperclass();
		}
		ParameterizedType type = (ParameterizedType) genericSuperclass;
		Type[] arguments = type.getActualTypeArguments();
		entityClass = (Class<E>) arguments[0];
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

}
