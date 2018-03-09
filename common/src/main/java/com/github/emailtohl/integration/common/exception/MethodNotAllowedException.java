package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 操作失败。使用的资源方法不允许调用。比如：想更新（PUT）已部署的资源会返回405结果。
 * @author HeLei
 */
@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotAllowedException extends RestException {
	private static final long serialVersionUID = 4217917360359052547L;

	public MethodNotAllowedException() {
		super();
	}

	public MethodNotAllowedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MethodNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodNotAllowedException(String message) {
		super(message);
	}

	public MethodNotAllowedException(Throwable cause) {
		super(cause);
	}
}
