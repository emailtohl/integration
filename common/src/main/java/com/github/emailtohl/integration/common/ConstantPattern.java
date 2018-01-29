package com.github.emailtohl.integration.common;

/**
 * 正则表达式常量定义
 * @author HeLei
 */
public interface ConstantPattern {
	/**
	 * 匹配中文字符的正则表达式
	 */
	String ZH = "[\\u4e00-\\u9fa5]";
	
	/**
	 * 匹配邮箱的正则匹配式
	 */
	String EMAIL = "^[a-zA-Z0-9`!#$%^&*'{}?/+=|_~-]+(\\.[a-zA-Z0-9`!#$%^&*'{}?/+=" +
			"|_~-]+)*@([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)+(\\.[a-zA-Z0-9]" +
			"([a-zA-Z0-9-]*[a-zA-Z0-9])?)*$";
	
	/**
	 * 匹配YYYY-MM-DD的正则表达式
	 */
	String DATE = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)";

	/**
	 * 匹配URL的正则匹配式
	 */
	String URL = "[a-zA-z]+://[^\\s]*";
	
	/**
	 * 匹配手机号码的正则表达式，默认首位是1的11位数字是手机号码
	 */
	String CELL_PHONE = "^1[0-9]{10}$";
	
	/**
	 * 匹配腾讯QQ号的正则匹配式
	 */
	String QQ = "[1-9][0-9]{4,}";
	
	/**
	 * 匹配邮政编码的正则匹配式
	 */
	String ZIPCODE = "[1-9]\\d{5}(?!\\d)";
	
	/**
	 * 匹配身份证号的正则匹配式
	 */
	String IDENTIFICATION = "^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$";
	
	/**
	 * 匹配整数的正则匹配式
	 */
	String INTEGER = "^-?[1-9]\\d*$";
	
	/**
	 * 匹配浮点的正则匹配式
	 */
	String FLOAT = "^-?[1-9]\\d*\\.\\d*|-0\\.\\d*[1-9]\\d*$";
	
	/**
	 * 匹配Windows和Unix风格的路径分隔符的正则匹配式
	 * “\”这里有四根，因为它在Java字符串和正则表达式中都需要转义
	 */
	String SEPARATOR = "[\\\\/]";
	
	/**
	 * 文件路径名中如果包含特殊字符，会造成不可预期的行为，还有可能产生系统漏洞，下面的字符如果用在文件路径名中会造成问题：
	 * “-”作为首字符：“-”经常作为命令行操作选项开关标识。
	 * 控制字符：比如换行，回车，ESC，如果在shell脚本中涉及到这些字符，行为会发生异常。
	 * 空格：需要使用双引号包含整个路径名，否则在shell脚本中，行为会发生异常。
	 * 非法的字符编码：字符编码后会使得验证文件路径名很困难。
	 * 命名空间分隔符：包含命名空间分隔符在文件路径名中会造成行为异常，甚至安全漏洞。
	 * 命令行特殊意义符号：如“%”，“>”,”@”,”*”等等。
	 */
	String ILLEGAL_FILENAME = "[^A-Za-z0-9%&+,.:=_]";
}
