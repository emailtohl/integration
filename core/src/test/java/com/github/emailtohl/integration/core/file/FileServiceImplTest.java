package com.github.emailtohl.integration.core.file;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.common.ztree.FileNode;
import com.github.emailtohl.integration.common.ztree.ZtreeNode;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class FileServiceImplTest {
	@Inject
	FileService fileService;
	
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
		Set<FileNode> nodes = fileService.findFile(null);
		assertTrue(nodes.stream().anyMatch(fileNode -> "test1".equals(fileNode.getName())));
		System.out.println(nodes);
		
		fileService.reIndex();
		nodes = fileService.findFile("org.springframework");
		System.out.println(nodes);
		for (FileNode n : nodes) {
			if ("test1".equals(n.getName())) {
				assertTrue(n.isOpen());
				for (ZtreeNode<File> n1 : n.getChildren()) {
					if ("test2".equals(n1.getName())) {
						assertTrue(n1.isOpen());
						for (ZtreeNode<File> n2 : n1.getChildren()) {
							if ("test3".equals(n2.getName())) {
								assertTrue(n2.isOpen());
								for (ZtreeNode<File> n3 : n2.getChildren()) {
									if ("test4".equals(n3.getName())) {
										assertTrue(n3.isOpen());
										for (ZtreeNode<File> n4 : n3.getChildren()) {
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

	@Test
	public void testCreateDir() {
		ExecResult r = fileService.createDir("");
		assertFalse(r.ok);
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
	public void testLoadAndWriteText() {
		String filename = path + File.separator + "log4j2.xml";
		ExecResult r = fileService.loadText(filename, null);
		assertTrue(r.ok);
		String txt = (String) r.attribute;
		txt = txt + "\n  update foo";
		r = fileService.writeText(filename, txt, null);
		assertTrue(r.ok);
		Set<FileNode> fns = fileService.findFile("foo");
		System.out.println(fns);
	}
}
