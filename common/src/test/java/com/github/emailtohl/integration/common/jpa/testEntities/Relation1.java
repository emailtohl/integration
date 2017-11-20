package com.github.emailtohl.integration.common.jpa.testEntities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 相互引用关系
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@Entity
public class Relation1 implements Serializable {
	private static final long serialVersionUID = 9107784769781908565L;
	private short id;
	@org.hibernate.search.annotations.Field(name = "relation1")
	private String me = "Relation1";
	@org.hibernate.search.annotations.IndexedEmbedded
	private Relation2 relation2;
	
	@Id
	public short getId() {
		return id;
	}
	public void setId(short id) {
		this.id = id;
	}
	public String getMe() {
		return me;
	}
	public void setMe(String me) {
		this.me = me;
	}
	public Relation2 getRelation2() {
		return relation2;
	}
	public void setRelation2(Relation2 relation2) {
		this.relation2 = relation2;
	}

}
