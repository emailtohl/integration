package com.github.emailtohl.integration.common.jpa.testRepository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.common.testEntities.User;
/**
 * 对于增删改查来说，最好使用JpaRepository下的方法，这是由spring data提供，比较方便可靠
 * UserRepositoryCustomization 下的方法主要是使用动态查询
 * @author HeLei
 * @date 2017.02.04
 */
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustomization {
	User findByEmail(String email);

	List<User> findByBirthdayBetween(Date start, Date end);
}
