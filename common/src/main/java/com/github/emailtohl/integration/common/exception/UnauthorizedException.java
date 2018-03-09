package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 操作失败。操作要求设置Authentication头部。
 * 如果请求中已经设置了头部，对应的凭证是无效的或者用户不允许执行这个操作。
 * @author HeLei
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RestException {
	private static final long serialVersionUID = -4387267066943470737L;

	public UnauthorizedException() {
		super();
	}

	public UnauthorizedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(Throwable cause) {
		super(cause);
	}
}
