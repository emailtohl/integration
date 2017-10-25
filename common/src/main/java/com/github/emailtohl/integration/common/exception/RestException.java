package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 在web层将异常转成http状态码
 * @author HeLei
 * @date 2017.03.12
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RestException extends RuntimeException {
	private static final long serialVersionUID = 3662626896336571259L;

	public RestException() {
		super();
	}

	public RestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RestException(String message, Throwable cause) {
		super(message, cause);
	}

	public RestException(String message) {
		super(message);
	}

	public RestException(Throwable cause) {
		super(cause);
	}

}
