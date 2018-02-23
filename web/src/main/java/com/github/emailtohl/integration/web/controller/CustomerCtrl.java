package com.github.emailtohl.integration.web.controller;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.web.service.mail.EmailService;

/**
 * 客户信息控制层
 * @author HeLei
 */
@RestController
@RequestMapping(value = "customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CustomerCtrl extends RestCtrl<Customer> {
	public static final Pattern PATTERN_EMAIL = Pattern.compile(ConstantPattern.EMAIL);
	public static final Pattern PATTERN_CELL_PHONE = Pattern.compile(ConstantPattern.CELL_PHONE);
	@Inject
	CustomerService customerService;
	@Inject
	EmailService emailService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public Customer create(@RequestBody @Valid Customer entity, Errors errors) {
		checkErrors(errors);
		return filter(customerService.create(entity));
	}
	
	@RequestMapping(value = "exist", method = RequestMethod.GET)
	public String exist(@RequestParam(name = "cellPhoneOrEmail") String cellPhoneOrEmail) {
		return String.format("{\"exist\":%b}", customerService.exist(cellPhoneOrEmail));
	}

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public Customer get(@PathVariable("id") Long id) {
		Customer c = customerService.get(id);
		mustExist(c);
		return filter(c);
	}

	@RequestMapping(value = "page", method = RequestMethod.GET)
	public Paging<Customer> query(Customer params,
			@PageableDefault(page = 0, size = 10, sort = { BaseEntity.ID_PROPERTY_NAME,
					BaseEntity.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		Paging<Customer> p = customerService.query(params, pageable);
		p.getContent().stream().peek(this::filter);
		return p;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<Customer> query(Customer params) {
		List<Customer> ls = customerService.query(params);
		ls.stream().peek(this::filter);
		return ls;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(@PathVariable("id") Long id, @RequestBody @Valid Customer newEntity, Errors errors) {
		checkErrors(errors);
		customerService.update(id, newEntity);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") Long id) {
		customerService.delete(id);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	public Paging<Customer> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
			@PageableDefault(page = 0, size = 10, sort = { BaseEntity.ID_PROPERTY_NAME,
					BaseEntity.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		Paging<Customer> p = customerService.search(query, pageable);
		p.getContent().stream().peek(this::filter);
		return p;
	}
	
	@RequestMapping(value = "cellPhoneOrEmail", method = RequestMethod.GET)
	public ResponseEntity<Customer> getByCellPhoneOrEmail(@RequestParam(name = "cellPhoneOrEmail") String cellPhoneOrEmail) {
		Customer c = customerService.getByUsername(cellPhoneOrEmail);
		if (c == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(filter(c), HttpStatus.OK);
	}

	@RequestMapping(value = "grandRoles", method = RequestMethod.POST)
	public void grandRoles(@RequestBody Form form) {
		customerService.grandRoles(form.id, form.roleNames);
	}

	/**
	 * 如果用手机号码，则将token以短信形式发送给用户；
	 * 若用邮箱，则将token以邮件发送给用户。
	 * @param cellPhoneOrEmail
	 */
	@RequestMapping(value = "token", method = RequestMethod.GET)
	public void getToken(@RequestParam("cellPhoneOrEmail") String cellPhoneOrEmail, @RequestParam("_csrf") String _csrf, HttpServletRequest request) {
		boolean b = customerService.exist(cellPhoneOrEmail);
		if (!b) {
			throw new NotFoundException("没有此用户：" + cellPhoneOrEmail);
		}
		String token = customerService.getToken(cellPhoneOrEmail);
		if (PATTERN_CELL_PHONE.matcher(cellPhoneOrEmail).find()) {
			System.out.println("手机短信发送给用户： " + token);
		} else if (PATTERN_EMAIL.matcher(cellPhoneOrEmail).find()) {
			String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/getUpdatePasswordPage";
			emailService.updatePassword(url, cellPhoneOrEmail, token, _csrf);
		}
	}
	
	@RequestMapping(value = "updatePassword", method = RequestMethod.POST)
	public ExecResult updatePassword(@RequestBody Form form) {
		return customerService.updatePassword(form.cellPhoneOrEmail, form.newPassword, form.token);
	}

	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	public ExecResult resetPassword(@RequestBody Form form) {
		return customerService.resetPassword(form.id);
	}
	
	@RequestMapping(value = "enabled", method = RequestMethod.POST)
	public Customer enabled(@RequestBody Form form) {
		return filter(customerService.enabled(form.id, form.enabled));
	}
	
	public void setCustomerService(CustomerService CustomerService) {
		this.customerService = CustomerService;
	}
	
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	private Customer filter(Customer c) {
		if (c.getImage() != null) {
			c.getImage().setBin(null);
		}
		return c;
	}
	
	/**
	 * jackson 解析内部类必须是static的，且根据getter、setter来识别属性
	 * @author HeLei
	 */
	static class Form implements Serializable {
		private static final long serialVersionUID = -4556588161350417003L;
		Long id;
		String[] roleNames;
		String cellPhoneOrEmail;
		String token;
		String newPassword;
		Boolean enabled;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String[] getRoleNames() {
			return roleNames;
		}
		public void setRoleNames(String[] roleNames) {
			this.roleNames = roleNames;
		}
		public String getCellPhoneOrEmail() {
			return cellPhoneOrEmail;
		}
		public void setCellPhoneOrEmail(String cellPhoneOrEmail) {
			this.cellPhoneOrEmail = cellPhoneOrEmail;
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public String getNewPassword() {
			return newPassword;
		}
		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}
		public Boolean getEnabled() {
			return enabled;
		}
		public void setEnabled(Boolean enabled) {
			this.enabled = enabled;
		}
	}

}

