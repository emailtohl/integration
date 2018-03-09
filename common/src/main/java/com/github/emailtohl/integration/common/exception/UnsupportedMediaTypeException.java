package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 操作失败。请求体包含了不支持的媒体类型。
 * 当请求体的JSON中包含未知的属性或值时，也会返回这个响应，一般是因为无法处理的错误格式或类型。
 * @author HeLei
 */
@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedMediaTypeException extends RestException {
	private static final long serialVersionUID = 3668791782059893526L;

	public UnsupportedMediaTypeException() {
		super();
	}

	public UnsupportedMediaTypeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedMediaTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedMediaTypeException(String message) {
		super(message);
	}

	public UnsupportedMediaTypeException(Throwable cause) {
		super(cause);
	}
}
