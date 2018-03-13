package com.github.emailtohl.integration.web.cluster;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;
/**
 * 集群相关的事件
 * @author HeLei
 */
public class ClusterEvent extends ApplicationEvent implements Serializable {
	private static final long serialVersionUID = 754117002611345928L;
	private final Serializable serializableSource;
	private boolean rebroadcasted;

	public ClusterEvent(Serializable source) {
		super(source);
		this.serializableSource = source;
	}

	public final boolean isRebroadcasted() {
		return this.rebroadcasted;
	}

	public final void setRebroadcasted() {
		this.rebroadcasted = true;
	}

	@Override
	public Serializable getSource() {
		return this.serializableSource;
	}

}
