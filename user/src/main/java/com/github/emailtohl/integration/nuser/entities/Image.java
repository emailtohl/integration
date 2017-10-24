package com.github.emailtohl.integration.nuser.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.validation.constraints.Size;

/**
 * 图片嵌入类，一般图片存储的名字会用Uuid等方式，所以需保存一个原始名
 * @author HeLei
 * @date 2017.10.17
 */
@Embeddable
public class Image {
	private String url;
	@Size(max = 1048576)
	private byte[] bin;
	private String filename;
	
	public Image() {
	}
	
	public Image(String url, String filename) {
		this(url, filename, null);
	}
	
	public Image(String url, String filename, byte[] bin) {
		super();
		this.url = url;
		this.filename = filename;
		this.bin = bin;
	}

	@Column(name = "image_url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@org.hibernate.envers.NotAudited
	@Lob
	@Basic(fetch = FetchType.LAZY)
	public byte[] getBin() {
		return bin;
	}

	public void setBin(byte[] bin) {
		this.bin = bin;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
