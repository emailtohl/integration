package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 请求时的状态异常
 * @author HeLei
 */
@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class RequestStateException extends RestException {
	private static final long serialVersionUID = -6841274142149965663L;

	public RequestStateException() {
		super();
	}

	public RequestStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RequestStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestStateException(String message) {
		super(message);
	}

	public RequestStateException(Throwable cause) {
		super(cause);
	}

}
