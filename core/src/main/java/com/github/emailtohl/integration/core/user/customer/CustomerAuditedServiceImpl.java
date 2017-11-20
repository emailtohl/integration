package com.github.emailtohl.integration.core.user.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.user.entities.Customer;
/**
 * 审计平台账号的历史记录
 * @author HeLei
 */
@Transactional
@Service
public class CustomerAuditedServiceImpl implements CustomerAuditedService {
	@Inject CustomerAudit customerAudit;

	@Override
	public List<Tuple<Customer>> getCustomerRevision(Long id) {
		Map<String, Object> propertyNameValueMap = new HashMap<>();
		propertyNameValueMap.put("id", id);
		return customerAudit.getAllRevisionInfo(propertyNameValueMap);
	}

	@Override
	public Customer getCustomerAtRevision(Long id, Number revision) {
		return customerAudit.getEntityAtRevision(id, revision);
	}

}
