package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 请求无效
 * @author HeLei
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NotAcceptableException extends RestException {
	private static final long serialVersionUID = 3020254799096026364L;

	public NotAcceptableException() {
		super();
	}

	public NotAcceptableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NotAcceptableException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAcceptableException(String message) {
		super(message);
	}

	public NotAcceptableException(Throwable cause) {
		super(cause);
	}

}
