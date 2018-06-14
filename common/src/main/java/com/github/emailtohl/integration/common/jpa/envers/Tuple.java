package com.github.emailtohl.integration.common.jpa.envers;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
/**
 * 对AuditQuery接口查出来的Object[]进行封装的对象
 * @author HeLei
 *
 * @param <E> 实体类型
 */
public class Tuple<E> {
	private E entity;
	private DefaultRevisionEntity defaultRevisionEntity;
	private RevisionType revisionType;
	
	public Tuple() {}
	
	public Tuple(E entity, DefaultRevisionEntity defaultRevisionEntity, RevisionType revisionType) {
		this.entity = entity;
		this.defaultRevisionEntity = defaultRevisionEntity;
		this.revisionType = revisionType;
	}

	public E getEntity() {
		return entity;
	}
	public void setEntity(E entity) {
		this.entity = entity;
	}
	public DefaultRevisionEntity getDefaultRevisionEntity() {
		return defaultRevisionEntity;
	}
	public void setDefaultRevisionEntity(DefaultRevisionEntity defaultRevisionEntity) {
		this.defaultRevisionEntity = defaultRevisionEntity;
	}
	public RevisionType getRevisionType() {
		return revisionType;
	}
	public void setRevisionType(RevisionType revisionType) {
		this.revisionType = revisionType;
	}

	@Override
	public String toString() {
		return "Tuple [defaultRevisionEntity=" + defaultRevisionEntity + ", revisionType=" + revisionType + "]";
	}

}
