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

}
