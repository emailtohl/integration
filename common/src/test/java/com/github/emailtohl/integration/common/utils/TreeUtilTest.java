package com.github.emailtohl.integration.common.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.emailtohl.integration.common.utils.Node;
import com.github.emailtohl.integration.common.utils.TreeUtil;
import com.github.emailtohl.integration.common.utils.ZtreeNode;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 树型结构测试
 * @author HeLei
 */
public class TreeUtilTest {
	Gson gson = new GsonBuilder().setPrettyPrinting().addSerializationExclusionStrategy(new ExclusionStrategy() {
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			if (f.getName().equals("key")) {
				return true;
			}
			return false;
		}
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

	}).create();
	
	@Test
	public void testGetZtreeNode() {
		Department _super = new Department("super", null);
		Department sub1 = new Department("sub1", _super);
		Department sub2 = new Department("sub2", _super);
		List<Node> ls = Arrays.asList(_super, sub1, sub2);
		List<ZtreeNode> nodes = TreeUtil.getZtreeNode(ls);
		TreeUtil.sort(nodes, new Comparator<ZtreeNode>() {
			@Override
			public int compare(ZtreeNode o1, ZtreeNode o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		System.out.println(gson.toJson(nodes));
		assertEquals(1, nodes.size());
		for (ZtreeNode n : nodes) {
			assertEquals(2, n.children.size());
		}
	}

	@Test
	public void testGetZtreeNodeByFilesystem() throws IOException {
		String usrHome = System.getProperty("user.home");
		File test_root = new File(usrHome, "test_root");
		File sub1 = new File(test_root, "sub1"),
			 sub2 = new File(test_root, "sub2"),
			 sub3 = new File(test_root, "sub3");
		File sub1_1 = new File(sub1, "sub1_1"),
			 sub1_2 = new File(sub1, "sub1_2");
		File sub2_1 = new File(sub2, "sub2_1"),
			 sub2_2 = new File(sub2, "sub2_2");
		sub1.mkdirs();
		sub2.mkdirs();
		sub3.createNewFile();
		sub1_1.createNewFile();
		sub1_2.createNewFile();
		sub2_1.createNewFile();
		sub2_2.createNewFile();
		
		List<ZtreeNode> ls = TreeUtil.getZtreeNodeByFilesystem(Arrays.asList(test_root));
		// test sort
		TreeUtil.sort(ls, new Comparator<ZtreeNode>() {
			@Override
			public int compare(ZtreeNode o1, ZtreeNode o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		// test setOpen
		List<String> dirs = Arrays.asList("test_root", "sub2", "sub2_2");
		TreeUtil.setOpen(ls, dirs);
		
		System.out.println(gson.toJson(ls));
		
		// test forEach
		TreeUtil.forEach(ls, node -> System.out.println(node));
		
		assertEquals(ls.size(), 1);
		for (ZtreeNode n : ls) {
			assertEquals(3, n.children.size());
		}
		
		FileUtils.deleteDirectory(test_root);
	}

}
