package com.github.emailtohl.integration.core.standard;

/**
 * 对于执行结果，如果失败，往往需要附带失败信息
 * 
 * @author HeLei
 */
public class ExecResult {
	public final boolean ok;
	public final String cause;
	public final Object attribute;

	public ExecResult(boolean ok, String cause, Object attribute) {
		super();
		this.ok = ok;
		this.cause = cause;
		this.attribute = attribute;
	}

	@Override
	public String toString() {
		return "ExecResult [ok=" + ok + ", cause=" + cause + ", attribute=" + attribute + "]";
	}
	
}
