/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua.test;

import org.junit.Test;

/**
 * Contains unit tests for Java reflection.
 */
public class JavaReflectionTest extends AbstractLuaTest {
	// -- Test cases
	/**
	 * Tests Java reflection from Lua.
	 */
	@Test
	public void testReflection() throws Exception {
		runTest("com/naef/jnlua/test/Reflection.lua", "Reflection");
	}
}
