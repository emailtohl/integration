package com.github.emailtohl.integration.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.emailtohl.integration.common.exception.RestException;

/**
 * 处理异常
 * 
 * @author HeLei
 */
@ControllerAdvice
public class ErrorHandler {

	/**
	 * 处理异常
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(RestException.class)
	public ResponseEntity<String> handle(RestException ex) {
		String msg = getMessage(ex);
		HttpStatus httpStatus = getHttpStatus(ex);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain;charset=UTF-8");
		return new ResponseEntity<>(msg, headers, httpStatus);
	}

	/**
	 * 获取注解中的状态
	 * @param ex
	 * @return
	 */
	public HttpStatus getHttpStatus(Exception ex) {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		// 获取状态码
		ResponseStatus anno = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
		if (anno != null) {
			httpStatus = (HttpStatus) AnnotationUtils.getValue(anno);
		}
		return httpStatus;
	}

	/**
	 * 获取异常中的信息
	 * @param t
	 * @return
	 */
	public String getMessage(Throwable t) {
		// 获取消息
		List<String> causeChain = new ArrayList<String>();
		Throwable sup = t, sub = sup.getCause();
		causeChain.add(sup.getMessage());
		while (sub != null) {
			sup = sub;
			causeChain.add(sup.getMessage());
			sub = sup.getCause();
		}
		boolean first = true;
		StringBuilder builder = new StringBuilder();
		for (String str : causeChain) {
			if (first) {
				builder.append(str);
				first = false;
			} else {
				builder.append(" -> ").append(str);
			}
		}
		String message = builder.toString();
		if (message.isEmpty()) {
			message = "内部错误";
		}
		return message;
	}
}
