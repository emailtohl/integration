package com.github.emailtohl.integration.web.service.flow;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 流程数据的存档
 * @author HeLei
 */
interface FlowRepository extends JpaRepository<FlowData, Long> {
	
	FlowData findByProcessInstanceId(String processInstanceId);
	
}
