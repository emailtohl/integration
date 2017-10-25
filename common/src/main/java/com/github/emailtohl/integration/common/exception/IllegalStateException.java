package com.github.emailtohl.integration.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 内部状态异常
 * @author HeLei
 * @date 2017.03.12
 */
@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class IllegalStateException extends RestException {
	private static final long serialVersionUID = -6841274142149965663L;

}
