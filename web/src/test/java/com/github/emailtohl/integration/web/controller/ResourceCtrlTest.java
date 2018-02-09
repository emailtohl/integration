package com.github.emailtohl.integration.web.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.emailtohl.integration.common.utils.BeanUtil;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.file.FileService;
/**
 * 资源控制器测试
 * @author HeLei
 */
public class ResourceCtrlTest {
	ResourceCtrl ctrl = new ResourceCtrl();
	File resources;
	
	@Before
	public void setUp() throws Exception {
		FileService fileService = mock(FileService.class);
		String usrHome = System.getProperty("user.home");
		resources = new File(usrHome, "test");
		Field fileServiceField = ResourceCtrl.class.getDeclaredField("fileService");
		Field resourcesField = ResourceCtrl.class.getDeclaredField("resources");
		BeanUtil.injectField(fileServiceField, ctrl, fileService);
		BeanUtil.injectField(resourcesField, ctrl, resources);
		ctrl.init();
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteQuietly(resources);
	}

	@Test
	public void testGetFilePath() {
		String pathname = "\\abc/bcd\\def/";
		String actual = ctrl.getFilePath(pathname);
		String expected = String.join(File.separator, "userSpace", "abc", "bcd", "def");
		assertEquals(expected, actual);
		System.out.println(actual);
	}
	
	@Test
	public void exec() throws Exception {
		String pathname = "\\abc/bcd\\def/";
		System.out.println(ctrl.availableCharsets());
		ctrl.createDir(pathname);
		ctrl.delete(pathname);
		ctrl.exist(pathname);
		ctrl.reName(pathname, pathname);
		ctrl.loadText(pathname, "UTF-8");
		Part file = mock(Part.class);
		ctrl.uploadFile(pathname, file);
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter("uploadPath", pathname);
		req.getParts().add(file);
		ctrl.multipartOnload(req);
	}

}
