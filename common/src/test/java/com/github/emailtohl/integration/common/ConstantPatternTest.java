package com.github.emailtohl.integration.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConstantPatternTest {

	@Test
	public void testLegalFilename() {
		assertTrue("abcd.txt".matches(ConstantPattern.LEGAL_FILENAME));
		assertTrue("  abcd.txt".matches(ConstantPattern.LEGAL_FILENAME));
		assertFalse("-abcd.txt".matches(ConstantPattern.LEGAL_FILENAME));
		assertFalse("   -abcd.txt".matches(ConstantPattern.LEGAL_FILENAME));
		assertFalse("ab\ncd.txt".matches(ConstantPattern.LEGAL_FILENAME));
		assertFalse("  ab\ncd.txt".matches(ConstantPattern.LEGAL_FILENAME));
	}

}
