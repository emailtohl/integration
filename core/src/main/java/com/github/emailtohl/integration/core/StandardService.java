package com.github.emailtohl.integration.core;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;

/**
 * 抽象的服务，主要就是增删改查功能。
 * 
 * 标准化参数名、参数类型以及返回后，不仅利于维护，更利于在切面层进行扩展。
 * 
 * 默认ID是Long类型
 * 
 * @author HeLei
 */
@Transactional
public abstract class StandardService<E extends Serializable> {
	/**
	 * Service尽量做到无状态，但是有些Service需要获取到当前用户信息
	 * 这里使用线程本地存储域，在Service外部统一设置当前用户id或唯一识别用户名
	 * Service内部就能根据id查询到当前用户信息
	 */
	public static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<Long>();
	public static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<String>();
	
	protected static final Logger LOG = LogManager.getLogger();
	/**
	 * 由于接口+抽象类的加入使得@Valid注解不能使用，所以可进行手动校验
	 */
	protected static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
	protected Validator validator = FACTORY.getValidator();
	
	protected static final transient int HASHING_ROUNDS = 10;
	
	/**
	 * 创建一个实体
	 * @param entity
	 * @return
	 */
	public abstract E create(E entity);
	
	/**
	 * 根据实体自身唯一性的属性查找是否已存在
	 * @param matcherValue
	 * @return
	 */
	public abstract boolean exist(Object matcherValue);
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public abstract E get(Long id);

	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的实体转瞬态时，同时改变分页对象
	 */
	public abstract Paging<E> query(E params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	public abstract List<E> query(E params);

	/**
	 * 修改实体内容，并指明哪些属性忽略
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该实体
	 */
	public abstract E update(Long id, E newEntity);

	/**
	 * 根据ID删除实体
	 * @param id
	 */
	public abstract void delete(Long id);
	
	/**
	 * 屏蔽实体中的敏感信息，如密码；将持久化状态的实体转存到瞬时态的实体对象上以便于调用者序列化
	 * 本方法提取简略信息，不做关联查询，主要用于列表中
	 * @param entity 持久化状态的实体对象
	 * @return 瞬时态的实体对象
	 */
	protected abstract E toTransient(E entity);
	
	/**
	 * 屏蔽实体中的敏感信息，如密码；将持久化状态的实体转存到瞬时态的实体对象上以便于调用者序列化
	 * 本方法提取详细信息，做关联查询
	 * @param entity 持久化状态的实体对象
	 * @return 瞬时态的实体对象
	 */
	protected abstract E transientDetail(@Valid E entity);
	
	/**
	 * 手动校验对象是否符合约束条件
	 * @param entity
	 * @return
	 */
	public void validate(E entity) {
		Set<ConstraintViolation<E>> violations = validator.validate(entity);
		if (violations.size() > 0) {
			violations.forEach(v -> LOG.debug(v));
			throw new NotAcceptableException(new ConstraintViolationException(violations));
		}
	}
	
	/**
	 * 手动校验对象是否符合约束条件
	 * @param entity
	 * @return
	 */
	public <T> void validate(T obj, Class<T> clz) {
		Set<ConstraintViolation<T>> violations = validator.validate(obj);
		if (violations.size() > 0) {
			violations.forEach(v -> LOG.debug(v));
			throw new NotAcceptableException(new ConstraintViolationException(violations));
		}
	}
	
	public String hashpw(String password) {
		String salt = BCrypt.gensalt(HASHING_ROUNDS, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}
	
	public boolean checkpw(String plaintext, String hashed) {
		return BCrypt.checkpw(plaintext, hashed);
	}
	
	/**
	 * 判断字符串是否存在
	 * @param text
	 * @return
	 */
	public boolean hasText(String text) {
		return text != null && !text.isEmpty();
	}

}
