package com.github.emailtohl.integration.common.ztree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
/**
 * ztree数据模型的测试
 * @author HeLei
 */
public class ZtreeNodeTest {
	File test_root = new File("test_root");
	File sub1 = new File(test_root, "sub1"),
		 sub2 = new File(test_root, "sub2"),
		 sub3 = new File(test_root, "sub3");
	File sub1_1 = new File(sub1, "sub1_1"),
		 sub1_2 = new File(sub1, "sub1_2");
	File sub2_1 = new File(sub2, "sub2_1"),
		 sub2_2 = new File(sub2, "sub2_2");
	
	@Before
	public void setUp() throws Exception {
		sub1.mkdirs();
		sub2.mkdirs();
		sub3.createNewFile();
		sub1_1.createNewFile();
		sub1_2.createNewFile();
		sub2_1.createNewFile();
		sub2_2.createNewFile();
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(new File(test_root.getAbsolutePath()));
	}

	@Test
	public void testNewInstance() {
		FileNode n = FileNode.newInstance(test_root);
		Gson gson = new Gson();
		String json = gson.toJson(n);
		System.out.println(json);
		FileNode nn = gson.fromJson(json, FileNode.class);
		System.out.println(nn);
		assertEquals(nn, n);
		long rootid = nn.getId();
		long pid = nn.getChildren().iterator().next().getPid();
		assertEquals(rootid, pid);
	}

	@Test
	public void testSetOpen() {
		FileNode n = FileNode.newInstance(test_root);
		String path = sub2_2.getPath();
		n.setOpen(path);
		assertTrue(n.isOpen());
		for (ZtreeNode<File> sub : n.getChildren()) {
			if ("sub2".equals(sub.getName())) {
				assertTrue(sub.isOpen());
			} else {
				assertFalse(sub.isOpen());
			}
		}
	}
	
	@Test
	public void testGetZtreeNodes() {
		Department _super = new Department("super", null);
		Department sub1 = new Department("sub1", _super);
		Department sub2 = new Department("sub2", _super);
		List<Node> c = Arrays.asList(_super, sub1, sub2);
		Set<ZtreeNode<Node>> ztree = ZtreeNode.getZtreeNodes(c);
		assertEquals(1, ztree.size());
		System.out.println(ztree);
	}
}
