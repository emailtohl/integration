package com.github.emailtohl.integration.common.standard;

/**
 * 对于执行结果，如果失败，往往需要附带失败信息
 * 
 * @author HeLei
 */
public class ExecResult {
	public final boolean ok;
	public final String cause;

	public ExecResult(boolean ok, String cause) {
		super();
		this.ok = ok;
		this.cause = cause;
	}
}
