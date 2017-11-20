package com.github.emailtohl.integration.common.jpa.testRepository;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

/**
 * spring data的审计实现类
 * @author HeLei
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public String getCurrentAuditor() {
		return "tester";
	}

}
