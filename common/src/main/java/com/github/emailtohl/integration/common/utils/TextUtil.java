package com.github.emailtohl.integration.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 以指定编码格式读写文件的工具
 * 注：Apache的commons-io组件中的FileUtils能提供更强大的文件操作功能
 * 
 * @author HeLei
 */
public class TextUtil {
	private static final Logger logger = LogManager.getLogger();
	final Set<String> charsets = Charset.availableCharsets().keySet();

	/**
	 * 返回可用的字符集
	 * @return
	 */
	public Set<String> availableCharsets() {
		return charsets;
	}
	
	/**
	 * 以指定编码格式将文本写入文件中
	 * 注意，本方法不确保线程安全，调用者自行控制
	 * @param file 要写入的文件的绝对路径
	 * @param textContext 文本的内容
	 * @param charset 编码格式
	 */
	public void writeText(File file, String textContext, String charset) {
		Charset cset = getCharset(charset);
		ByteBuffer buffer = cset.encode(textContext);
		byte[] bytes = buffer.array();
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(bytes);
		} catch (IOException e) {
			logger.error("写入：" + file.getName() + "发生错误", e);
			if (file.exists())
				file.delete();
			throw new RuntimeException("写入：" + file.getName() + "发生错误");
		}
	}
	
	/**
	 * 以指定编码格式读取文件的内容
	 * @param absolutePath 要读取的文件的绝对路径
	 * @param charset 编码格式
	 * @return 文本的内容
	 */
	public String getText(File file, String charset) {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file));
				ByteArrayOutputStream memory = new ByteArrayOutputStream()) {
			byte[] bytes = new byte[1024];
			int i;
			while (true) {
				i = is.read(bytes);
				if (i == -1) {
					break;
				} else {
					memory.write(bytes, 0, i);
				}
			}
			ByteBuffer bbuf = ByteBuffer.wrap(memory.toByteArray());
			Charset cset = getCharset(charset);
            CharBuffer cbuf = cset.decode(bbuf);
            return cbuf.toString();
		} catch (IOException e) {
			logger.error("读取" + file.getName() + "失败", e);
			throw new RuntimeException("读取" + file.getName() + "失败");
		}
	}
	
	private Charset getCharset(String charset) {
		Charset cset;
		try {
			cset = Charset.forName(charset);
		} catch (IllegalArgumentException e) {
			cset = Charset.defaultCharset();
			logger.info("使用默认编码：" + cset.displayName() + "读取文件");
		}
		return cset;
	}
}
