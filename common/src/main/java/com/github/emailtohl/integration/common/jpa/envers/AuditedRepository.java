package com.github.emailtohl.integration.common.jpa.envers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 查询Hibernate envers对实体的审计记录
 * Hibernate将每次：增、改、删操作视作一次修订，所以一次修订信息包括两方面：1.修订的版本号；2.修订的类型（增？改？删？）
 * 这反应在Tuple中
 * 
 * @author HeLei
 *
 * @param <E> 实体类型
 */
public interface AuditedRepository<E extends Serializable> {
	
	/**
	 * 根据实体的属性查询所有的修订记录，例如查询User实体中name属性为"foo"相关的所有修订记录
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	List<Tuple<E>> getAllRevisionInfo(Map<String, Object> propertyNameValueMap);
	
	/**
	 * 根据实体的属性，分页查询所有修订记录，例如查询User实体中name属性为"foo"相关的所有修订记录
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @param pageable
	 * @return 分页的元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	Page<Tuple<E>> getRevisionInfoPage(Map<String, Object> propertyNameValueMap, Pageable pageable);
	
	/**
	 * 查询某个修订版下（“新增”时或“修改”时的）并且与条件相匹配的历史记录
	 * 例如批量修改一批用户的版本号（revision）是5，该接口将查询这次修改的所有账户（当然要与propertyNameValueMap匹配的）
	 * @param revision 版本号，通过AuditReader#getRevisions(Entity.class, ID)获得
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @return
	 */
	List<E> getEntitiesAtRevision(Number revision, Map<String, Object> propertyNameValueMap);
	
	/**
	 * 查询某个修订版下（“新增”时或“修改”时的）并且与条件相匹配的历史记录
	 * 例如批量修改一批用户的版本号（revision）是5，该接口将查询这次修改的所有账户（当然要与propertyNameValueMap匹配的）
	 * @param revision 版本号，通过AuditReader#getRevisions(Entity.class, ID)获得
	 * @param propertyNameValueMap 查询的谓词，使用AND关系过滤查询结果，可为空
	 * @param pageable
	 * @return
	 */
	Page<E> getEntitiesAtRevision(Number revision, Map<String, Object> propertyNameValueMap, Pageable pageable);
	
	/**
	 * 精确查询某个实体在某个修订版时的历史记录
	 * @param id 实体的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Entity.class, ID)获得
	 * @return
	 */
	E getEntityAtRevision(Long id, Number revision);
	
	/**
	 * 将实体回滚到某历史版本上
	 */
	void rollback(Long id, Number revision);
}
