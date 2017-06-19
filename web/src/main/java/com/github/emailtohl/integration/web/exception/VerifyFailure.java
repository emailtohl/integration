package com.github.emailtohl.integration.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 注解了@ResponseStatus的异常被抛出后，Spring MVC会自动根据其状态自动调用HttpServletResponse.setStatus()方法
 * @author HeLei
 * @date 2017.02.04
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VerifyFailure extends RestException {
	private static final long serialVersionUID = 2104594800958968108L;

	public VerifyFailure() {
		super();
	}

	public VerifyFailure(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public VerifyFailure(String message, Throwable cause) {
		super(message, cause);
	}

	public VerifyFailure(String message) {
		super(message);
	}

	public VerifyFailure(Throwable cause) {
		super(cause);
	}
	
}
