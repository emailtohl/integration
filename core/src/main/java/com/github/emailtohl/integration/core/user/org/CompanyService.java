package com.github.emailtohl.integration.core.user.org;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.entities.Company;

/**
 * 公司信息服务层
 * @author HeLei
 */
@Validated
@PreAuthorize("isAuthenticated()")
public interface CompanyService extends StandardService<Company> {
	default Company transientDetail(Company src) {
		if (src == null) {
			return null;
		}
		Company tar = new Company();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getModifyDate());
		tar.setParent(transientDetail(src.getParent()));
		return tar;
	}
}
