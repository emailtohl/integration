package com.github.emailtohl.integration.web.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.web.exception.BadRequestException;

/**
 * 获取Spring Security上下文的当前用户
 * @author HeLei
 * @date 2017.06.19
 */
public final class CtrlUtil {
	private CtrlUtil() {}
	/**
	 * 获取当前用户的登录名字，如未登录导致没有获取到，则抛出异常
	 * @return
	 */
	public static String getCurrentUsername() {
		String username = null;
		SecurityContext ctx = SecurityContextHolder.getContext();
		if (ctx != null) {
			Authentication a = ctx.getAuthentication();
			if (a != null) {
				username = a.getName();
			}
		}
		if (!StringUtils.hasText(username)) {
			throw new UsernameNotFoundException("没有此用户:" + username);
		}
		return username;
	}
	
	/**
	 * 将file输出到文件中
	 * 注意：
	 * （1） Since: Servlet 3.0
	 * （2）要使用本功能，需在Servet上要注解：@MultipartConfig
	 * 
	 * @param request HttpServletRequest
	 * @param uploadPath 上传到服务器中的目录
	 * @return 上传成功的文件名
	 */
	public static String multipartOnload(HttpServletRequest request, String uploadPath) {
		if (request == null) {
			throw new BadRequestException("传入参数是null");
		}
		StringBuilder msg = new StringBuilder();
		Collection<Part> fileParts = null;
		Map<String, String[]> map = request.getParameterMap();
		try {
			fileParts = request.getParts();
		} catch (IOException | ServletException e) {
			e.printStackTrace();
		}
		if (fileParts != null) {
			Iterator<Part> iterable = fileParts.iterator();
			while (iterable.hasNext()) {
				Part filePart = iterable.next();// 每个filePart表示一个文件，前端可能同时上传多个文件
				try {
					String filename = filePart.getSubmittedFileName();// 获取提交文件原始的名字
					if (filename != null && !map.containsKey(filename)) {
						filePart.write(request.getServletContext().getRealPath(uploadPath + filename));
						msg.append(',').append(filename);
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (filePart != null) {
						try {
							filePart.delete();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
		if (msg.length() == 0)
			return "未上传成功的文件";
		else {
			msg.deleteCharAt(0);
			msg.insert(0, "上传成功：");
			return msg.toString();
		}
	}
}
