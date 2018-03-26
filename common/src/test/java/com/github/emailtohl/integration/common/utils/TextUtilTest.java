package com.github.emailtohl.integration.common.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextUtilTest {
	private static final Logger logger = LogManager.getLogger();
	String content = "Hello World\n你好，世界";
	String encoding;
	ByteArrayInputStream in;
	
	@Before
	public void setUp() throws Exception {
//		String[] encodes = { "ISO-2022-CN", "BIG-5", "EUC-TW", "GB18030", "HZ-GB-2312", "ISO-8859-5", "KOI8-R", "WINDOWS-1251", "MACCYRILLIC", "IBM866", "IBM855", "ISO-8859-7", "WINDOWS-1253", "ISO-8859-8", "WINDOWS-1255", "ISO-2022-JP", "Shift_JIS", "EUC-JP", "ISO-2022-KR", "EUC-KR",
//				"UTF-8", "UTF-16BE", "UTF-16LE", "UTF-32BE", "UTF-32LE", "X-ISO-10646-UCS-4-3412", "X-ISO-10646-UCS-4-2143", "WINDOWS-1252" };
		String[] encodes = new String[Charset.availableCharsets().size()];
		encodes = Charset.availableCharsets().keySet().toArray(encodes);
		Random ran = new Random();
		encoding = encodes[ran.nextInt(encodes.length)];
//		encoding = "UTF-8";
//		encoding = "GBK";
//		encoding = "GB2312";
//		encoding = "UTF-16";
//		encoding = "ISO8859-1";
//		encoding = "GB18030";
		System.out.println("实际编码是: " + encoding);
		in = new ByteArrayInputStream(content.getBytes(encoding));
	}

	@After
	public void tearDown() throws Exception {
		in.close();
	}

	@Test
	public void testDetect() throws IOException {
		String encoding = TextUtil.detect(in);
		logger.debug("返回的编码是: " + encoding);
	}

	@Test
	public void testReadFileToString() throws IOException {
		String txt = TextUtil.readFileToString(in);
		logger.debug("TextUtil: " + txt);
	}

}
