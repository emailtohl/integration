package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.service.UserService;
import com.github.emailtohl.integration.web.filter.UserPasswordEncryptionFilter;
import com.google.gson.Gson;

/**
 * 密钥管理相关的控制器
 * @author HeLei
 * @date 2017.02.04
 */
@RestController
@RequestMapping("encryption")
public class EncryptionCtrl {
	private static final Logger logger = LogManager.getLogger();
	@Inject UserService userService;
	Gson gson = new Gson();
	Encipher encipher = new Encipher();
	
	@RequestMapping(value = "publicKey", method = POST)
	public void uploadPublicKey(String publicKey) {
		userService.setPublicKey(CtrlUtil.getCurrentUsername(), publicKey);
	}
	
	@RequestMapping(value = "publicKey", method = DELETE)
	public void deletePublicKey() {
		userService.clearPublicKey(CtrlUtil.getCurrentUsername());
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
		
		User u = null;
		try {
			String email = CtrlUtil.getCurrentUsername();
			u = userService.getUserByEmail(email);
		} catch (ResourceNotFoundException e) {
			return null;
		}
		if (u == null || u.getPublicKey() == null)
			return null;
		String ciphertext = encipher.encrypt(plaintext, u.getPublicKey());
		logger.debug(ciphertext);
		// 构造成json格式
		return "{\"ciphertext\":\"" + ciphertext + "\"}";
	}
}
