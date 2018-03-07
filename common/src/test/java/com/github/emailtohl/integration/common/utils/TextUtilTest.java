package com.github.emailtohl.integration.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextUtilTest {
	private static final Logger logger = LogManager.getLogger();
	File file;
	String content = "Hello World\n你好，世界";
	String encoding;

	@Before
	public void setUp() throws Exception {
		file = new File(System.getProperty("java.io.tmpdir"), "TextUtilTest");
		if (!file.exists()) {
			file.createNewFile();
		}
//		String[] encodes = { "ISO-2022-CN", "BIG-5", "EUC-TW", "GB18030", "HZ-GB-2312", "ISO-8859-5", "KOI8-R", "WINDOWS-1251", "MACCYRILLIC", "IBM866", "IBM855", "ISO-8859-7", "WINDOWS-1253", "ISO-8859-8", "WINDOWS-1255", "ISO-2022-JP", "Shift_JIS", "EUC-JP", "ISO-2022-KR", "EUC-KR",
//				"UTF-8", "UTF-16BE", "UTF-16LE", "UTF-32BE", "UTF-32LE", "X-ISO-10646-UCS-4-3412", "X-ISO-10646-UCS-4-2143", "WINDOWS-1252" };
		String[] encodes = new String[Charset.availableCharsets().size()];
		encodes = Charset.availableCharsets().keySet().toArray(encodes);
		Random ran = new Random();
		encoding = encodes[ran.nextInt(encodes.length)];
//		encoding = "UTF-8";
//		encoding = "GBK";
//		encoding = "GB2312";
//		encoding = "GB18030";
		System.out.println("随机获取的编码是: " + encoding);
		FileUtils.write(file, content, encoding);
	}

	@After
	public void tearDown() throws Exception {
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void testDetect() throws IOException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			String encoding = TextUtil.detect(inputStream);
			logger.debug("Detect: " + encoding);
		}
	}

	@Test
	public void testReadFileToString() throws IOException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			String txt = TextUtil.readFileToString(inputStream, encoding);
			logger.debug("TextUtil: " + txt);
			txt = FileUtils.readFileToString(file, encoding);
			logger.debug("FileUtils: " + txt);
		}
	}

}
