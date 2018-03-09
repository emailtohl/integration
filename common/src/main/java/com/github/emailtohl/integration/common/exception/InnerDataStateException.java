package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 内部系统数据的状态异常
 * @author HeLei
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InnerDataStateException extends RestException {
	private static final long serialVersionUID = 3293571086104637644L;

	public InnerDataStateException() {
		super();
	}

	public InnerDataStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InnerDataStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InnerDataStateException(String message) {
		super(message);
	}

	public InnerDataStateException(Throwable cause) {
		super(cause);
	}

}
