package com.github.emailtohl.integration.web.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 对Web层测试的配置
 * 
 * @author HeLei
 */
@Configurable
@Import(PresetDataConfiguration.class)
@ComponentScan({ "com.github.emailtohl.integration.web.aop", "com.github.emailtohl.integration.web.service" })
@EnableAspectJAutoProxy
public class WebTestConfig {
	@Inject
	CustomerService customerService;
	@Inject
	EmployeeService employeeService;
	
	@Bean
	public ServletContext servletContext() {
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getContextPath()).thenReturn("/web");
		return servletContext;
	}

	@Bean
	public WebTestData webTestData(CorePresetData pd) {
		synchronized (getClass()) {
			WebTestData td = new WebTestData(pd);
			Customer c = customerService.getByUsername(td.baz.getEmail());
			if (c == null) {
				c = customerService.create(td.baz);
				td.baz.setId(c.getId());
				c = customerService.grandRoles(c.getId(),
						td.baz.roleNames().toArray(new String[td.baz.roleNames().size()]));
				td.baz.getRoles().clear();
				td.baz.getRoles().addAll(c.getRoles());
			} else {
				td.baz.setId(c.getId());
			}

			c = customerService.getByUsername(td.qux.getEmail());
			if (c == null) {
				c = customerService.create(td.qux);
				td.qux.setId(c.getId());
				c = customerService.grandRoles(c.getId(),
						td.qux.roleNames().toArray(new String[td.qux.roleNames().size()]));
				td.qux.getRoles().clear();
				td.qux.getRoles().addAll(c.getRoles());
			} else {
				td.qux.setId(c.getId());
			}

			Employee e = employeeService.getByEmail(td.foo.getEmail());
			if (e == null) {
				e = employeeService.create(td.foo);
				td.foo.setId(e.getId());
				e = employeeService.grandRoles(e.getId(),
						td.foo.roleNames().toArray(new String[td.foo.roleNames().size()]));
				td.foo.getRoles().clear();
				td.foo.getRoles().addAll(e.getRoles());
			} else {
				td.foo.setId(e.getId());
			}

			e = employeeService.getByEmail(td.bar.getEmail());
			if (e == null) {
				e = employeeService.create(td.bar);
				td.bar.setId(e.getId());
				e = employeeService.grandRoles(e.getId(),
						td.bar.roleNames().toArray(new String[td.bar.roleNames().size()]));
				td.bar.getRoles().clear();
				td.bar.getRoles().addAll(e.getRoles());
			} else {
				td.bar.setId(e.getId());
			}

			return td;
		}
	}

}
