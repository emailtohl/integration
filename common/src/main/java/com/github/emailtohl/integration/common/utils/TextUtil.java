package com.github.emailtohl.integration.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * 读取文本文件，自动检测文本编码
 * @author HeLei
 */
public final class TextUtil {
    /**
     * 检测内容编码格式
     * jchardet是mozilla自动字符集探测算法代码
     * @return
     * @throws Exception
     */
	public static String detect(InputStream inputStream) throws IOException {
		class nsICharsetDetectionObserverImpl implements nsICharsetDetectionObserver {
			boolean found = false;
			String result;
			@Override
			public void Notify(String charset) {
				found = true;
				result = charset;
			}
		}
		// Initalize the nsDetector() ;
		nsDetector det = new nsDetector(nsPSMDetector.ALL);
		// Set an observer...
		// The Notify() will be called when a matching charset is found.
		nsICharsetDetectionObserverImpl observer = new nsICharsetDetectionObserverImpl();
		det.Init(observer);
		byte[] bytes = new byte[1024];
		int len;
		boolean isAscii = true;
		while ((len = inputStream.read(bytes, 0, bytes.length)) != -1) {
			// Check if the stream is only ascii.
			if (isAscii)
				isAscii = det.isAscii(bytes, len);
			// DoIt if non-ascii and not done yet.
			if (!isAscii) {
				if (det.DoIt(bytes, len, false))
					break;
			}
		}
		det.DataEnd();
		String[] prob;
		if (isAscii) {
			observer.found = true;
			prob = new String[] { "ASCII" };
		} else if (observer.found) {
			prob = new String[] { observer.result };
		} else {
			prob = det.getProbableCharsets();
		}
		return prob[0];
	}
	/**
	 * 读取内容
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(InputStream inputStream) throws IOException {
		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		try {
			out = new ByteArrayOutputStream();
			byte[] bytes = new byte[512];
			int i;
			while (true) {
				i = inputStream.read(bytes);
				if (i == -1) {
					break;
				} else {
					out.write(bytes, 0, i);
				}
			}
			in = new ByteArrayInputStream(bytes);
			String encoding = detect(in);
			Charset charset = Charset.forName(encoding);
			ByteBuffer bbuf = ByteBuffer.wrap(out.toByteArray());
			CharBuffer cbuf = charset.decode(bbuf);
			return cbuf.toString();
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
		}
	}
}
