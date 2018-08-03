package com.github.emailtohl.integration.web.controller;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.web.service.mail.EmailService;
import com.github.emailtohl.lib.exception.NotFoundException;
import com.google.gson.Gson;

/**
 * 认证控制器，管理用户注册，更改密码，授权等功能
 * 
 * @author HeLei
 */
@CrossOrigin(maxAge = 3600) // 支持跨站（CORS）访问登录页面
@Controller
public class LoginCtrl {
	private static final Logger logger = LogManager.getLogger();
	@Inject
	CustomerService customerService;
	@Inject
	EmployeeService employeeService;
	@Inject
	EmailService emailService;
	@Inject
	SessionRegistry sessionRegistry;
	@Inject
	Gson gson;

	/**
	 * GET方法获取登录页面 POST方法配置在Spring security中对用户进行认证
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String login() {
		return "login";
	}

	/**
	 * GET方法获取注册页面
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "register", method = RequestMethod.GET)
	public String register() {
		return "register";
	}

	/**
	 * POST方法注册一个账号，如果成功，则返回到登录页面
	 * 
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "register", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8" })
	public String register(HttpServletRequest requet, @Valid CustomerRegisterForm form, Errors e) {
		// 第一步，判断提交表单是否有效
		if (e.hasErrors()) {
			StringBuilder s = new StringBuilder();
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
				s.append(oe.getDefaultMessage());
			}
			// throw new VerifyFailure(e.toString());
			return "redirect:register?error=" + encode(s.toString());
		}
		try {
			// 第二步，添加该用户，若报运行时异常，则抛出，告诉用户该账号不能注册
			Customer c = new Customer();
			String cellPhoneOrEmail = form.getCellPhoneOrEmail();
			Matcher m = Constant.PATTERN_EMAIL.matcher(cellPhoneOrEmail);
			if (m.matches()) {
				c.setEmail(cellPhoneOrEmail);
			} else {
				m = Constant.PATTERN_CELL_PHONE.matcher(cellPhoneOrEmail);
				if (m.matches()) {
					c.setCellPhone(cellPhoneOrEmail);
				} else {
					return "redirect:register?error=" + encode("既不是正确的手机号也不是正确的邮箱");
				}
			}
			c.setPassword(form.getPassword());
			c.setName(form.getName());
			c = customerService.create(c);

			// 第三步，通知用户，让其激活该账号
			
//			Long id = c.getId();
//			String url = requet.getScheme() + "://" + requet.getServerName() + ":" + requet.getServerPort()
//					+ requet.getContextPath() + "/enable?id=" + id;
//			emailService.enableUser(url, c.getEmail());

			return "login";
		} catch (RuntimeException e1) {
			return "redirect:register?error=" + encode("手机号或邮箱重复");
		}
	}

	/**
	 * 另立一个私有方法处理URLEncoder.encode的检查型异常
	 * 
	 * @param s
	 * @return
	 */
	private String encode(String s) {
		String res = null;
		try {
			res = URLEncoder.encode(s.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 通过手机短信或电子邮箱发送token，然后跳转到忘记密码的页面
	 * 
	 * @return
	 * @throws NotFoundException
	 */
	@RequestMapping(value = "forgetPassword", method = RequestMethod.POST)
	public String forgetPassword(HttpServletRequest requet,
			@RequestParam(value = "cellPhoneOrEmail", required = false, defaultValue = "") String cellPhoneOrEmail,
			String _csrf, Map<String, Object> model) {
		if (!customerService.exist(cellPhoneOrEmail)) {
			throw new NotFoundException("没有此用户：" + cellPhoneOrEmail);
		}
		String token = customerService.getToken(cellPhoneOrEmail);
		Matcher m = Constant.PATTERN_EMAIL.matcher(cellPhoneOrEmail);
		if (m.matches()) {
			try {
				emailService.sendMail(cellPhoneOrEmail, "token", token);
			} catch (MailException e) {
				logger.warn("邮件发送失败", e);
			}
		} else {
			m = Constant.PATTERN_CELL_PHONE.matcher(cellPhoneOrEmail);
			if (m.matches()) {
				// TODO 发送token到短信中
			} else {
				return "redirect:register?error=" + encode("既不是正确的手机号也不是正确的邮箱");
			}
		}
		model.put("cellPhoneOrEmail", cellPhoneOrEmail);
		model.put("_csrf", _csrf);
		return "redirect:updatePassword";
	}

	/**
	 * 通过手机短信或电子邮箱发送token，然后跳转到忘记密码的页面
	 * 
	 * @return
	 * @throws NotFoundException
	 */
	@RequestMapping(value = "updatePassword", method = RequestMethod.GET)
	public String getUpdatePasswordPage(
			@RequestParam(value = "cellPhoneOrEmail", required = false, defaultValue = "") String cellPhoneOrEmail,
			@RequestParam(value = "_csrf", required = false, defaultValue = "") String _csrf, Map<String, Object> model) {
		model.put("cellPhoneOrEmail", cellPhoneOrEmail);
		model.put("_csrf", _csrf);
		return "updatePassword";
	}
	
	/**
	 * 修改密码
	 * @param cellPhoneOrEmail
	 * @param password
	 * @param token
	 * @param _csrf
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "updatePassword", method = RequestMethod.POST)
	public String updatePassword(@RequestParam(value = "cellPhoneOrEmail") String cellPhoneOrEmail,
			@RequestParam(value = "password") String password, @RequestParam(value = "token") String token,
			@RequestParam(value = "_csrf", required = false, defaultValue = "") String _csrf,
			Map<String, Object> model) {
		ExecResult e = customerService.updatePassword(cellPhoneOrEmail, password, token);
		model.put("cellPhoneOrEmail", cellPhoneOrEmail);
		model.put("_csrf", _csrf);
		if (e.ok) {
			return "redirect:login";
		} else {
			return "redirect:login?error=" + encode("修改密码失败");
		}
	}

	/**
	 * 获取用户的认证信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "authentication", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> authentication() {
		Map<String, Object> map = new HashMap<String, Object>();
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) {
			return map;
		}
		Authentication authentication = context.getAuthentication();
		if (authentication == null) {
			return map;
		}
		map.put("username", authentication.getName());
		map.put("details", authentication.getDetails());
		map.put("principal", authentication.getPrincipal());

		return map;
	}

	/**
	 * 测试接口
	 * 
	 * @return
	 */
	@RequestMapping({ "secure" })
	public String securePage(HttpSession session, Map<String, Object> model) {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication authentication = context.getAuthentication();
			if (authentication != null) {
				model.put("credentials", authentication.getCredentials());
				model.put("details", authentication.getDetails());
				model.put("principal", authentication.getPrincipal());
				model.put("authorities", authentication.getAuthorities());
				model.put("allSessions", sessionRegistry.getAllSessions(authentication.getPrincipal(), true));
			}
		}
		model.put("sessionInformation", sessionRegistry.getSessionInformation(session.getId()));
		model.put("allPrincipals", sessionRegistry.getAllPrincipals());
		model.put("allPrincipalsJson", gson.toJson(sessionRegistry.getAllPrincipals()));
		return "secure";
	}

	/**
	 * 注册表单
	 * 
	 * @author HeLei
	 */
	static class CustomerRegisterForm implements Serializable {
		private static final long serialVersionUID = 5123644992104952829L;
		// 表单会以此名字提交
		public static final String CSRF_NAME = CsrfToken.class.getName();
		@NotNull
		String cellPhoneOrEmail;
		String name;
		@NotNull
		String password;
		// @NotNull
		// 这里的Field名字应该是CSRF_NAME
		String _csrf;

		public String getCellPhoneOrEmail() {
			return cellPhoneOrEmail;
		}

		public void setCellPhoneOrEmail(String cellPhoneOrEmail) {
			this.cellPhoneOrEmail = cellPhoneOrEmail;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String get_csrf() {
			return _csrf;
		}

		public void set_csrf(String _csrf) {
			this._csrf = _csrf;
		}

		public static String getCsrfName() {
			return CSRF_NAME;
		}

		@Override
		public String toString() {
			return "CustomerRegisterForm [cellPhoneOrEmail=" + cellPhoneOrEmail + ", name=" + name + ", password="
					+ password + ", _csrf=" + _csrf + "]";
		}
	}
}
