package com.github.emailtohl.integration.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 非法请求
 * @author HeLei
 * @date 2017.03.12
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RestException {
	private static final long serialVersionUID = 4939288709671898521L;

	public BadRequestException() {
		super();
	}

	public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(Throwable cause) {
		super(cause);
	}

}
