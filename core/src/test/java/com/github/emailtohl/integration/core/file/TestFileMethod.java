package com.github.emailtohl.integration.core.file;

import static com.github.emailtohl.integration.common.ConstantPattern.SEPARATOR;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * 测试File类的一些特性
 * @author HeLei
 */
public class TestFileMethod {

	@Test
	public void test() throws IOException {
		File f = new File(".");
		System.out.println(f.getParent());
		System.out.println(f.getPath());
		// 注意绝对路径最后有一个“.”
		System.out.println(f.getAbsolutePath());
		assertTrue(f.getAbsolutePath().endsWith("."));
		String path = f.getCanonicalPath();
		System.out.println(path);
		Pattern root_pattern = Pattern.compile("integration" + SEPARATOR);
		System.out.println(root_pattern);
		Matcher m = root_pattern.matcher(path);
		if (m.find()) {
			int i = m.start();
			String relativelyPath = path.substring(i);
			System.out.println(relativelyPath);
		}
	
	}

}
