package com.github.emailtohl.integration.core.user.customer;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.lib.jpa.AuditedRepository.RevTuple;

/**
 * 审计平台账号的历史记录
 * 
 * @author HeLei
 */
@Transactional
@Service
public class CustomerAuditedServiceImpl implements CustomerAuditedService {
	@Inject
	CustomerAudit customerAudit;

	@Override
	public List<RevTuple<Customer>> getCustomerRevision(Long id) {
		List<RevTuple<Customer>> ls = customerAudit.getRevisions(id);
		return ls.stream().map(rt -> {
			return new RevTuple<Customer>(toTransient(rt.entity), transientRevisionEntity(rt.defaultRevisionEntity),
					rt.revisionType);
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
		source.getRoles().forEach(
				role -> target.getRoles().add(new Role(role.getName(), role.getRoleType(), role.getDescription())));
		return target;
	}

	private DefaultRevisionEntity transientRevisionEntity(DefaultRevisionEntity re) {
		if (re == null) {
			return null;
		}
		DefaultRevisionEntity n = new DefaultRevisionEntity();
		n.setId(re.getId());
		n.setTimestamp(re.getTimestamp());
		return n;
	}
}
