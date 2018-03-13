package com.github.emailtohl.integration.web.service.systemInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.github.emailtohl.integration.web.cluster.ClusterEvent;

/**
 * 系统信息事件
 * @author HeLei
 */
public class SystemInfoEvent extends ClusterEvent {
	private static final long serialVersionUID = 946573335982913324L;
	private Map<String, Serializable> systemInfo = new HashMap<>();
	
	public SystemInfoEvent(Serializable source) {
		super(source);
	}

	public SystemInfoEvent(Serializable source, Map<String, Serializable> info) {
		super(source);
		this.systemInfo = info;
	}

	public Map<String, Serializable> getSystemInfo() {
		return systemInfo;
	}

	public void setSystemInfo(Map<String, Serializable> systemInfo) {
		this.systemInfo = systemInfo;
	}

}
