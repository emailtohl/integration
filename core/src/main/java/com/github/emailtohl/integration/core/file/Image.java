package com.github.emailtohl.integration.core.file;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.validation.constraints.Size;

/**
 * 图片嵌入类，一般图片存储的名字会用Uuid等方式，所以需保存一个原始名
 * @author HeLei
 */
@Embeddable
public class Image {
	private String src;
	@Size(max = 1048576)
	private byte[] bin;
	private String filename;
	
	public Image() {
	}
	
	public Image(String filename, String src) {
		this(filename, src, null);
	}
	
	public Image(String filename, String src, byte[] bin) {
		super();
		this.src = src;
		this.filename = filename;
		this.bin = bin;
	}

	@Column(name = "image_src")
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@org.hibernate.envers.NotAudited
	@Column(name = "image_bin")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	public byte[] getBin() {
		return bin;
	}

	public void setBin(byte[] bin) {
		this.bin = bin;
	}

	@Column(name = "image_filename")
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
