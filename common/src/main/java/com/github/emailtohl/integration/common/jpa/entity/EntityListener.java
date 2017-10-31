package com.github.emailtohl.integration.common.jpa.entity;

import java.util.Date;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 创建日期、修改日期处理
 * 
 * @author HeLei
 */
public class EntityListener {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * 保存前处理
	 * 
	 * @param entity 基类
	 */
	@PrePersist
	public void prePersist(BaseEntity entity) {
		entity.setCreateDate(new Date());
		entity.setModifyDate(new Date());
	}

	/**
	 * 更新前处理
	 * 
	 * @param entity 基类
	 */
	@PreUpdate
	public void preUpdate(BaseEntity entity) {
		entity.setModifyDate(new Date());
	}

	@PostLoad
	void readTrigger(BaseEntity entity) {
		LOG.debug("entity read.");
	}

	@PostPersist
	void afterInsertTrigger(BaseEntity entity) {
		LOG.debug("entity inserted into database.");
	}

	@PostUpdate
	void afterUpdateTrigger(BaseEntity entity) {
		LOG.debug("entity just updated in the database.");
	}

	@PreRemove
	void beforeDeleteTrigger(BaseEntity entity) {
		LOG.debug("entity about to be deleted.");
	}

	@PostRemove
	void afterDeleteTrigger(BaseEntity entity) {
		LOG.debug("entity about deleted from database.");
	}
}
