package com.github.emailtohl.integration.web.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
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

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.web.service.mail.EmailService;
import com.github.emailtohl.lib.ConstantPattern;
import com.github.emailtohl.lib.exception.NotFoundException;
import com.github.emailtohl.lib.jpa.EntityBase;
import com.github.emailtohl.lib.jpa.Paging;

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
	
	IDCardValidator IDCardValidator = new IDCardValidator();
	
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
	
	@RequestMapping(value = "identificationValidate", method = RequestMethod.GET)
	public String IDCardValidate(@RequestParam(name = "identification") String identification) {
		boolean vaild = IDCardValidator.IDCardValidate(identification);
		boolean exist = customerService.identificationExist(identification);
		return String.format("{\"exist\":%b,\"vaild\":%b}", exist, vaild);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public Customer get(@PathVariable("id") Long id) {
		Customer c = customerService.get(id);
		mustExist(c);
		return filter(c);
	}

	@RequestMapping(value = "page", method = RequestMethod.GET)
	public Paging<Customer> query(Customer params,
			@PageableDefault(page = 0, size = 10, sort = { EntityBase.ID_PROPERTY_NAME,
					EntityBase.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
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
			@PageableDefault(page = 0, size = 10, sort = { EntityBase.ID_PROPERTY_NAME,
					EntityBase.MODIFY_DATE_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
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

	@RequestMapping(value = "grandLevel", method = RequestMethod.POST)
	public void grandLevel(Long id, Form form) {
		
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
		Customer.Level level;
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
		public Customer.Level getLevel() {
			return level;
		}
		public void setLevel(Customer.Level level) {
			this.level = level;
		}
	}

}


/**
 * 一个检查类
 * @author HeLei
 *
 */
class IDCardValidator {
	/**
	 * 功能：身份证的有效验证
	 * 
	 * @param IDStr
	 *            身份证号
	 * @return true 有效：false 无效
	 * @throws ParseException
	 */
	public boolean IDCardValidate(String IDStr) {
		String[] ValCodeArr = { "1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2" };
		String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2" };
		String Ai = "";
		// ================ 号码的长度18位 ================
		if (IDStr.length() != 18) {
			return false;
		}
		// ================ 数字 除最后以为都为数字 ================
		if (IDStr.length() == 18) {
			Ai = IDStr.substring(0, 17);
		}
		if (isNumeric(Ai) == false) {
			// errorInfo = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
			return false;
		}
		// ================ 出生年月是否有效 ================
		String strYear = Ai.substring(6, 10);// 年份
		String strMonth = Ai.substring(10, 12);// 月份
		String strDay = Ai.substring(12, 14);// 日
		if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) {
			// errorInfo = "身份证生日无效。";
			return false;
		}
		GregorianCalendar gc = new GregorianCalendar();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		try {
			if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
					|| (gc.getTime().getTime() - s.parse(strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
				// errorInfo = "身份证生日不在有效范围。";
				return false;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
			// errorInfo = "身份证月份无效";
			return false;
		}
		if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
			// errorInfo = "身份证日期无效";
			return false;
		}
		// ================ 地区码时候有效 ================
		Map<String, String> h = GetAreaCode();
		if (h.get(Ai.substring(0, 2)) == null) {
			// errorInfo = "身份证地区编码错误。";
			return false;
		}
		// ================ 判断最后一位的值 ================
		int TotalmulAiWi = 0;
		for (int i = 0; i < 17; i++) {
			TotalmulAiWi = TotalmulAiWi + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
		}
		int modValue = TotalmulAiWi % 11;
		String strVerifyCode = ValCodeArr[modValue];
		Ai = Ai + strVerifyCode;

		if (IDStr.length() == 18) {
			if (Ai.equals(IDStr) == false) {
				// errorInfo = "身份证无效，不是合法的身份证号码";
				return false;
			}
		} else {
			return true;
		}
		return true;
	}

	/**
	 * 功能：设置地区编码
	 * 
	 * @return Hashtable 对象
	 */
	private Map<String, String> GetAreaCode() {
		Map<String, String> hashtable = new HashMap<>();
		hashtable.put("11", "北京");
		hashtable.put("12", "天津");
		hashtable.put("13", "河北");
		hashtable.put("14", "山西");
		hashtable.put("15", "内蒙古");
		hashtable.put("21", "辽宁");
		hashtable.put("22", "吉林");
		hashtable.put("23", "黑龙江");
		hashtable.put("31", "上海");
		hashtable.put("32", "江苏");
		hashtable.put("33", "浙江");
		hashtable.put("34", "安徽");
		hashtable.put("35", "福建");
		hashtable.put("36", "江西");
		hashtable.put("37", "山东");
		hashtable.put("41", "河南");
		hashtable.put("42", "湖北");
		hashtable.put("43", "湖南");
		hashtable.put("44", "广东");
		hashtable.put("45", "广西");
		hashtable.put("46", "海南");
		hashtable.put("50", "重庆");
		hashtable.put("51", "四川");
		hashtable.put("52", "贵州");
		hashtable.put("53", "云南");
		hashtable.put("54", "西藏");
		hashtable.put("61", "陕西");
		hashtable.put("62", "甘肃");
		hashtable.put("63", "青海");
		hashtable.put("64", "宁夏");
		hashtable.put("65", "新疆");
		// hashtable.put("71", "台湾");
		// hashtable.put("81", "香港");
		// hashtable.put("82", "澳门");
		// hashtable.put("91", "国外");
		return hashtable;
	}

	/**
	 * 功能：判断字符串是否为数字
	 * 
	 * @param str
	 * @return
	 */
	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (isNum.matches()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 功能：判断字符串是否为日期格式
	 * 
	 * @param str
	 * @return
	 */
	public boolean isDate(String strDate) {
		Pattern pattern = Pattern.compile(
				"^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
		Matcher m = pattern.matcher(strDate);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

}
