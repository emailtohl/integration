package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.web.filter.UserPasswordEncryptionFilter;
import com.google.gson.Gson;

/**
 * 密钥管理相关的控制器
 * @author HeLei
 */
@RestController
@RequestMapping("encryption")
public class EncryptionCtrl {
	private static final Logger logger = LogManager.getLogger();
	@Inject
	CustomerService customerService;
	@Inject
	EmployeeService employeeService;
	@Inject
	UserService userService;
	@Inject
	Gson gson;
	Encipher encipher = new Encipher();
	
	@RequestMapping(value = "publicKey", method = POST)
	public void uploadPublicKey(String publicKey) {
		String sid = ThreadContext.get(Constant.USER_ID);
		if (StringUtils.hasText(sid)) {
			Customer c = customerService.setPublicKey(Long.valueOf(sid), publicKey);
			if (c == null) { // 在Customer中没有找到
				employeeService.setPublicKey(Long.valueOf(sid), publicKey);
			}
		}
	}
	
	@RequestMapping(value = "publicKey", method = DELETE)
	public void deletePublicKey() {
		String sid = ThreadContext.get(Constant.USER_ID);
		if (StringUtils.hasText(sid)) {
			Customer c = customerService.setPublicKey(Long.valueOf(sid), null);
			if (c == null) { // 在Customer中没有找到
				employeeService.setPublicKey(Long.valueOf(sid), null);
			}
		}
	}
	
	@RequestMapping(value = "serverPublicKey", method = GET)
	public String serverPublicKey(HttpSession session) {
		String serverPublicKey = (String) session.getAttribute(UserPasswordEncryptionFilter.PUBLIC_KEY_PROPERTY_NAME);
		serverPublicKey = serverPublicKey == null ? "" : serverPublicKey;
		return "{\"serverPublicKey\":\"" + serverPublicKey + "\"}";
	}
	
	@RequestMapping(value = "secret", method = POST)
	public void secret(HttpSession session, String ciphertext) {
		String privateKey = (String) session.getAttribute(UserPasswordEncryptionFilter.PRIVATE_KEY_PROPERTY_NAME);
		if (StringUtils.hasText(privateKey)) {
			String recover = encipher.decrypt(ciphertext, privateKey);
			logger.info("\n" + recover);
		}
	}
	
	@RequestMapping(value = "testMessage", method = GET)
	public String testMessage() {
		String plaintext = "滚滚长江东逝水，浪花淘尽英雄。\r\n" + 
				"是非成败转头空。\r\n" + 
				"青山依旧在，几度夕阳红。\r\n" + 
				"白发渔樵江渚上，惯看秋月春风。\r\n" + 
				"一壶浊酒喜相逢。\r\n" + 
				"古今多少事，都付笑谈中。";
		
		String sid = ThreadContext.get(Constant.USER_ID);
		if (!StringUtils.hasText(sid)) {
			return null;
		}
		User u = userService.get(Long.valueOf(sid));
		if (u == null || u.getPublicKey() == null)
			return null;
		String ciphertext = "";
		try {
			ciphertext = encipher.encrypt(plaintext, u.getPublicKey());
		} catch (IllegalArgumentException e) {}
		logger.debug(ciphertext);
		// 构造成json格式
		return "{\"ciphertext\":\"" + ciphertext + "\"}";
	}
}