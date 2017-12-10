package com.github.emailtohl.integration.core.auth;

import java.io.Serializable;

/**
 * Stores additional details about the authentication request.
 * These might be an IP address, certificate serial number etc.
 * 
 * @author HeLei
 */
public class Details implements Serializable {
	private static final long serialVersionUID = -3337658966125412997L;
	private String remoteAddress;
	private String sessionId;
	private String certificateSerialNumber;

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getCertificateSerialNumber() {
		return certificateSerialNumber;
	}

	public void setCertificateSerialNumber(String certificateSerialNumber) {
		this.certificateSerialNumber = certificateSerialNumber;
	}
}
