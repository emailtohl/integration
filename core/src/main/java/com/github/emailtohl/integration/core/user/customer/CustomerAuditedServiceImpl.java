package com.github.emailtohl.integration.core.user.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.role.Role;
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
		List<Tuple<Customer>> ls = customerAudit.getAllRevisionInfo(propertyNameValueMap);
		return ls.stream().map(t -> {
			Tuple<Customer> n = new Tuple<Customer>();
			n.setDefaultRevisionEntity(t.getDefaultRevisionEntity());
			n.setRevisionType(t.getRevisionType());
			n.setEntity(toTransient(t.getEntity()));
			return n;
		}).collect(Collectors.toList());
	}

	@Override
	public Customer getCustomerAtRevision(Long id, Number revision) {
		return transientDetail(customerAudit.getEntityAtRevision(id, revision));
	}

	private Customer toTransient(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "password", "roles", "cards");
		return target;
	}

	private Customer transientDetail(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "password", "roles", "cards");
		source.getCards().forEach(card -> target.getCards().add(card));
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getDescription())));
		return target;
	}
}
