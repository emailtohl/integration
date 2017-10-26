package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 主要是前端表单数据存在问题，例如数据不完整或数据之间矛盾等
 * @author HeLei
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDataException extends RestException {
	private static final long serialVersionUID = -600268390844936571L;

	public InvalidDataException() {
		super();
	}

	public InvalidDataException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataException(String message) {
		super(message);
	}

	public InvalidDataException(Throwable cause) {
		super(cause);
	}

}
