package com.github.emailtohl.integration.core.auth;

import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.exception.InnerDataStateException;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 工具类，在安全上下文中获取全局唯一识别的用户名
 * @author HeLei
 */
public final class UniqueUsername {
	public static final String EMP_NUM_PREFIX = "emp_num:";
	public static final String CELL_PHONE_PREFIX = "cell_phone:";
	public static final String EMAIL_PREFIX = "email:";
	public static final String ID_PREFIX = "id:";
	
	public static String get(User u) {
		StringBuilder uniqueName = new StringBuilder();
		if (u instanceof Employee) {
			uniqueName.append(EMP_NUM_PREFIX).append(((Employee) u).getEmpNum().toString());
		} else if (u instanceof Customer) {
			if (StringUtils.hasText(u.getCellPhone())) {
				uniqueName.append(CELL_PHONE_PREFIX).append(u.getCellPhone());
			}
			if (StringUtils.hasText(u.getEmail())) {
				if (uniqueName.length() > 0) {
					uniqueName.append(';');
				}
				uniqueName.append(EMAIL_PREFIX).append(u.getEmail());
			}
		} else if (u.getId() != null){
			uniqueName.append(ID_PREFIX).append(u.getId().toString());
		}
		if (uniqueName.length() == 0) {
			throw new InnerDataStateException("未获取到用户唯一标识");
		}
		return uniqueName.toString();
	}
}
