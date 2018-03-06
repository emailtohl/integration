package com.github.emailtohl.integration.core.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.emailtohl.integration.common.utils.ZtreeNode;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestEnvironment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 文件服务测试
 * @author HeLei
 */
public class FileServiceImplTest extends CoreTestEnvironment {
	@Inject
	FileService fileService;
	Gson gson = new GsonBuilder()/*.setPrettyPrinting()*/.create();
	String path;
	
	@Before
	public void setUp() throws Exception {
		ExecResult r = fileService.createDir("\\test1\\test2/test3/test4");
		assertTrue(r.ok);
		path = (String) r.attribute;
		assertTrue(fileService.exist(path));
		File srcDir = new File("src" + File.separator + "main" + File.separator + "resources");
		File destDir = fileService.getFile(path);
		FileUtils.copyDirectory(srcDir, destDir);
	}

	@After
	public void tearDown() throws Exception {
		File dir = new File(path);
		String parent = dir.getParent();
		parent = new File(parent).getParent();
		parent = new File(parent).getParent();
		ExecResult r = fileService.delete(parent);
		assertTrue(r.ok);
	}

	@Test
	public void testFindFile() throws IOException {
		List<ZtreeNode> nodes = fileService.findFile(null);
		assertTrue(nodes.stream().anyMatch(fileNode -> "test1".equals(fileNode.getName())));
		System.out.println(gson.toJson(nodes));
		
		fileService.reIndex();
		nodes = fileService.findFile("Console appender");
		System.out.println(gson.toJson(nodes));
		for (ZtreeNode n : nodes) {
			if ("test1".equals(n.getName())) {
				assertTrue(n.isOpen());
				for (ZtreeNode n1 : n.getChildren()) {
					if ("test2".equals(n1.getName())) {
						assertTrue(n1.isOpen());
						for (ZtreeNode n2 : n1.getChildren()) {
							if ("test3".equals(n2.getName())) {
								assertTrue(n2.isOpen());
								for (ZtreeNode n3 : n2.getChildren()) {
									if ("test4".equals(n3.getName())) {
										assertTrue(n3.isOpen());
										for (ZtreeNode n4 : n3.getChildren()) {
											if ("log4j2.xml".equals(n4.getName())) {
												assertTrue(n4.isSelected());
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Test(expected = ConstraintViolationException.class)
	public void testCreateDirConstraintViolationException() {
		fileService.createDir("-abc");
	}
	
	@Test
	public void testReName() {
		ExecResult r = fileService.reName("", null);
		assertFalse(r.ok);
		assertEquals("name is empty", r.cause); 
		
		r = fileService.reName("dfjoaj/eorg", null);
		assertFalse(r.ok);
		assertEquals("name is empty", r.cause); 
		
		r = fileService.reName(path, "dfjoaj/eorg");
		assertFalse(r.ok);
		assertEquals("not in the same directory", r.cause); 
		
		String destName = path;
		r = fileService.reName(path, destName);
		assertFalse(r.ok);
		assertEquals("destName already exists", r.cause); 
		
		destName = path.substring(0, path.lastIndexOf(File.separator)) + File.separator + "test5";
		r = fileService.reName(path, destName);
		assertTrue(r.ok);
	}

	@Test
	public void testSave() {
		File log4jconfig = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "log4j2.xml");
		try (InputStream in = new BufferedInputStream(new FileInputStream(log4jconfig))) {
			ExecResult r = fileService.save(path + File.separator + "log4jcopy.xml", in);
			assertTrue(r.ok);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testAutoSaveFile() {
		ClassLoader cl = CoreTestData.class.getClassLoader();
		File f = null;
		try (InputStream is = cl.getResourceAsStream("img/icon-head-emailtohl.png")) {
			ExecResult r = fileService.autoSaveFile(is, "png");
			assertTrue(r.ok);
			String filename = (String) r.attribute;
			System.out.println(filename);
			f = fileService.getFile(filename);
			assertTrue(f.exists());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			if (f != null && f.exists()) {
				f.delete();
			}
		}
	}

	@Test
	public void testLoadAndWriteText() {
		String filename = path + File.separator + "log4j2.xml";
		ExecResult r = fileService.loadText(filename, null);
		assertTrue(r.ok);
		String txt = (String) r.attribute;
		txt = txt + "\n  update foo";
		r = fileService.writeText(filename, txt, null);
		assertTrue(r.ok);
		List<ZtreeNode> fns = fileService.findFile("foo");
		System.out.println(gson.toJson(fns));
	}
}
