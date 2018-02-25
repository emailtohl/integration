package com.github.emailtohl.integration.web.service.flow;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 申请单存放
 * @author HeLei
 */
interface ApplyRepository extends JpaRepository<Apply, Long> {
	
}
