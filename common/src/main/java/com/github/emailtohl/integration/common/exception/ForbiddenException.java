package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 禁止操作，不要重试。这不是认证和授权的问题，这是禁止操作。
 * 比如：删除一个执行中流程的任务是不允许的，无论用户或流程任务的状态。
 * @author HeLei
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RestException {
	private static final long serialVersionUID = 5891524623188327493L;

	public ForbiddenException() {
		super();
	}

	public ForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(message, cause);
	}

	public ForbiddenException(String message) {
		super(message);
	}

	public ForbiddenException(Throwable cause) {
		super(cause);
	}

}
