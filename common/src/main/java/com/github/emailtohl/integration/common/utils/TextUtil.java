package com.github.emailtohl.integration.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * 文本工具
 * 
 * @author HeLei
 */
public final class TextUtil {
	private static final Logger logger = LogManager.getLogger();
	
    /**
     * 检测内容编码格式
     * jchardet是mozilla自动字符集探测算法代码
     * @return
     * @throws Exception
     */
	public static String detect(InputStream inputStream) throws IOException {
		class nsICharsetDetectionObserverImpl implements nsICharsetDetectionObserver {
			boolean found = false;
			String charset = "";
			@Override
			public void Notify(String a) {
				found = true;
				charset = a;
			}
		}
		nsICharsetDetectionObserverImpl observer = new nsICharsetDetectionObserverImpl();
		int lang = nsPSMDetector.ALL;
		nsDetector det = new nsDetector(lang);
		det.Init(observer);

		byte[] buf = new byte[1024];
		int len;
		boolean done = false;
		boolean isAscii = true;

		while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
			if (isAscii)
				isAscii = det.isAscii(buf, len);
			if (!isAscii && !done)
				done = det.DoIt(buf, len, false);
		}
		det.DataEnd();

		if (isAscii) {
			observer.charset = "ASCII";
			observer.found = true;
		}

		if (!observer.found) {
			String prob[] = det.getProbableCharsets();
			observer.charset = prob[0];
		}
		return observer.charset;
	}
	/**
	 * 读取内容
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(InputStream inputStream, String charset) throws IOException {
		Charset cset;
		try {
			cset = Charset.forName(charset);
		} catch (IllegalArgumentException e) {
			logger.warn("解析编码失败，使用系统默认编码：" + Charset.defaultCharset().name());
			cset = Charset.defaultCharset();
		}
		try (ByteArrayOutputStream memory = new ByteArrayOutputStream()) {
			byte[] bytes = new byte[512];
			int i;
			while (true) {
				i = inputStream.read(bytes);
				if (i == -1) {
					break;
				} else {
					memory.write(bytes, 0, i);
				}
			}
			ByteBuffer bbuf = ByteBuffer.wrap(memory.toByteArray());
			CharBuffer cbuf = cset.decode(bbuf);
			return cbuf.toString();
		}
	}
}
