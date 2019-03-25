package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.lib.exception.ConflictException;
import com.github.emailtohl.lib.exception.InvalidDataException;
import com.github.emailtohl.lib.exception.NotFoundException;
import com.github.emailtohl.lib.jpa.EntityBase;
import com.github.emailtohl.lib.jpa.Paging;

/**
 * 基本增删改查。基类注解仅做参考，为保证映射有效，导出类同样需添加MVC注解。
 * 
 * @author HeLei
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public abstract class RestCtrl<T> {
	protected static final Logger logger = LogManager.getLogger();
	protected Class<?> entityClass;
	@Inject
	protected UserService userService;
	
	@SuppressWarnings("unchecked")
	public RestCtrl() {
		Class<?> clz = this.getClass();
		while (clz != RestCtrl.class) {
			Type genericSuperclass = clz.getGenericSuperclass();
			if (genericSuperclass instanceof ParameterizedType) {
				ParameterizedType type = (ParameterizedType) genericSuperclass;
				Type[] arguments = type.getActualTypeArguments();
				if (arguments == null) {
					continue;
				}
				if (arguments[0] instanceof Class) {// 若继承层次上分开声明参数类型时arguments.length就为1
					entityClass = (Class<? extends Serializable>) arguments[0];
					break;
				}
			}
			clz = clz.getSuperclass();
		}
		if (entityClass == null) {
			logger.debug("初始化： " + this.getClass() + " 时，entityClass == null");
			throw new IllegalStateException("初始化： " + this.getClass() + " 时，entityClass == null");
		}
		logger.debug(entityClass);
	}

	/**
	 * 转换前端日期格式
	 * @param request
	 * @param binder
	 * @throws Exception
	 */
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		binder.registerCustomEditor(Date.class,
				new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
	}
	
	/**
	 * 校验
	 * @param errors
	 */
	protected void checkErrors(Errors errors) {
		if (errors.hasErrors()) {
			StringBuilder msg = new StringBuilder();
			for (ObjectError oe : errors.getAllErrors()) {
				logger.info(oe);
				String cause = oe.toString().split(";")[0];
				msg.append(cause).append("\t").append(oe.getDefaultMessage()).append("\n");
			}
			throw new InvalidDataException(msg.toString());
		}
	}
	
	/**
	 * 校验
	 * @param o
	 */
	protected void mustExist(Object o) {
		if (o == null) {
			throw new NotFoundException(entityClass.getName());
		}
	}
	
	/**
	 * 判断字符串是否存在
	 * @param text
	 * @return
	 */
	public boolean hasText(String text) {
		return text != null && !text.isEmpty();
	}
	
	/**
	 * 获取上下文地址
	 * @param request
	 * @return
	 */
	public String getContextPath(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
	}
	
	/**
	 * 成功则在header上返回新资源的地址；失败则返回失败的信息。
	 * @param entity
	 * @param e
	 * @return
	 */
	@RequestMapping(method = POST)
	@ResponseStatus(HttpStatus.CREATED)
	abstract public T create(@RequestBody @Valid T entity, Errors errors) throws InvalidDataException, ConflictException;

	@RequestMapping(value = "{id}", method = GET)
	abstract public T get(@PathVariable("id") Long id) throws NotFoundException;

	@RequestMapping(value = "page", method = GET)
	abstract public Paging<T> query(T params,
			@PageableDefault(page = 0, size = 10, sort = { EntityBase.ID_PROPERTY_NAME,
					EntityBase.MODIFY_TIME_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable);

	@RequestMapping(method = GET)
	abstract public List<T> query(T params);
	
	@RequestMapping(value = "{id}", method = PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	abstract public void update(@PathVariable("id") Long id, @RequestBody @Valid T newEntity, Errors errors) throws InvalidDataException;

	@RequestMapping(value = "{id}", method = DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	abstract public void delete(@PathVariable("id") Long id);

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
