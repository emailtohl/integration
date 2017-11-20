package com.github.emailtohl.integration.common.jpa.testEntities;

import static com.github.emailtohl.integration.common.jpa.entity.BaseEntity.CREATE_DATE_PROPERTY_NAME;
import static com.github.emailtohl.integration.common.jpa.entity.BaseEntity.ID_PROPERTY_NAME;
import static com.github.emailtohl.integration.common.jpa.entity.BaseEntity.MODIFY_DATE_PROPERTY_NAME;
import static com.github.emailtohl.integration.common.jpa.entity.BaseEntity.VERSION_PROPERTY_NAME;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 实体超类的测试
 * @author HeLei
 */
public class BaseEntityTest {

	@Test
	public void testGetIgnoreProperties() {
		String[] result = BaseEntity.getIgnoreProperties("abc", "bcd");
		assertTrue(Arrays.equals(new String[] { ID_PROPERTY_NAME, CREATE_DATE_PROPERTY_NAME, MODIFY_DATE_PROPERTY_NAME,
				VERSION_PROPERTY_NAME, "abc", "bcd" }, result));

		result = BaseEntity.getIgnoreProperties();
		assertTrue(Arrays.equals(new String[] { ID_PROPERTY_NAME, CREATE_DATE_PROPERTY_NAME, MODIFY_DATE_PROPERTY_NAME,
				VERSION_PROPERTY_NAME }, result));
	}

}
