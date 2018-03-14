package com.github.emailtohl.integration.common;

/**
 * 常量定义
 * @author HeLei
 */
public interface Constant {
	/**
	 * 一般情况下的时间格式
	 */
	String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 在postgresql、oracle中字符串若当成Lob对待的话，会出现解析错误，一般解决方法有：
	 * 定义实体时，取消@Lob标注，按String对待；
	 * 定义实体时，保留@Lob标注，增加 @Type(type = "org.hibernate.type.TextType")标注；
	 * 不修改实体，重载PostgresDialect类remapSqlTypeDescriptor()方法，将CLOB当longvarchar处理。
	 * 
	 * 在JPA实体中，标注了@Lob的的字符串属性，统一Hibernate解析标注@org.hibernate.annotations.Type
	 */
	String LOB_TEXT = "org.hibernate.type.TextType";
}
