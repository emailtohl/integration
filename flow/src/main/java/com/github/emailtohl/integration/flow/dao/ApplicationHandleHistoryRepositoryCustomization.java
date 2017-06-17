package com.github.emailtohl.integration.flow.dao;

import java.util.List;

import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.CriterionQueryRepository;
import com.github.emailtohl.integration.flow.entities.ApplicationHandleHistory;
/**
 * 为了灵活的支持多条件查询，创建自定义接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface ApplicationHandleHistoryRepositoryCustomization
		extends CriterionQueryRepository<ApplicationHandleHistory> {

	/**
	 * 当出现异常时，重新开启一个事务直接对底层数据进行访问
	 * @param id
	 * @return
	 */
	List<ApplicationHandleHistory> findByApplicationFormIdWhenException(long id);

}
