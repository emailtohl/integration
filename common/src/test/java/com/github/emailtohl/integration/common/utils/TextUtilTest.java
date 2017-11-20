package com.github.emailtohl.integration.common.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
/**
 * 文本工具的测试
 * @author HeLei
 */
public class TextUtilTest {
	TextUtil util = new TextUtil();

	@Test
	public void testAvailableCharsets() {
		System.out.println(util.charsets);
	}
	
	@Test
	public void testWriteText() {
		File f = new File("text.txt");
		try {
			util.writeText(f, "text", "GB2312");
			assertTrue(f.exists());
		} finally {
			if (f.exists())
				f.delete();
		}
	}

	@Test
	public void testGetText() {
		String s = util.getText(new File("pom.xml"), "UTF-8");
		assertNotNull(s);
	}

}
