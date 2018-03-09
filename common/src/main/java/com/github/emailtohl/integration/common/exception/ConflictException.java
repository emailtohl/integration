package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 冲突异常
 * @author HeLei
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RestException {
	private static final long serialVersionUID = 5963164432312278754L;

	public ConflictException() {
		super();
	}

	public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException(Throwable cause) {
		super(cause);
	}

}
