package com.github.emailtohl.integration.common;
/**
 * 常量定义
 * @author HeLei
 */
public interface Constant {
	
	/**
	 * 匹配中文字符的正则表达式
	 */
	String PATTERN_ZN = "[\\u4e00-\\u9fa5]";
	
	/**
	 * 匹配邮箱的正则匹配式
	 */
	String PATTERN_EMAIL = "^[a-zA-Z0-9`!#$%^&*'{}?/+=|_~-]+(\\.[a-zA-Z0-9`!#$%^&*'{}?/+=" +
			"|_~-]+)*@([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)+(\\.[a-zA-Z0-9]" +
			"([a-zA-Z0-9-]*[a-zA-Z0-9])?)*$";
	/**
	 * 匹配YYYY-MM-DD的正则表达式
	 */
	String PATTERN_DATE = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)";

	/**
	 * 匹配URL的正则匹配式
	 */
	String PATTERN_URL = "[a-zA-z]+://[^\\s]*";
	
	/**
	 * 匹配手机号码的正则表达式，默认首位是1的11位数字是手机号码
	 */
	String PATTERN_CELL_PHONE = "^1\\d{10}$";
	
	/**
	 * 匹配腾讯QQ号的正则匹配式
	 */
	String PATTERN_QQ = "[1-9][0-9]{4,}";
	
	/**
	 * 匹配邮政编码的正则匹配式
	 */
	String PATTERN_ZIPCODE = "[1-9]\\d{5}(?!\\d)";
	
	/**
	 * 匹配身份证号的正则匹配式
	 */
	String PATTERN_IDENTIFICATION = "^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$";
	
	/**
	 * 匹配整数的正则匹配式
	 */
	String PATTERN_INTEGER = "^-?[1-9]\\d*$";
	
	/**
	 * 匹配浮点的正则匹配式
	 */
	String PATTERN_FLOAT = "^-?[1-9]\\d*\\.\\d*|-0\\.\\d*[1-9]\\d*$";
	
	
	/**
	 * 匹配Windows和Unix风格的路径分隔符的正则匹配式
	 */
	String PATTERN_SEPARATOR = "[\\\\/]";
	
	/**
	 * 一般情况下的时间格式
	 */
	String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
