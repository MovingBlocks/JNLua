/*
 * Copyright (C) 2008,2012 Andre Naef
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.terasology.jnlua.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.terasology.jnlua.JavaModule;

/**
 * Contains unit tests for the Java module.
 */
public class JavaModuleTest extends AbstractLuaTest {
	// ---- Test cases
	/**
	 * Tests the toTable method.
	 */
	@Test
	public void testToTable() {
		// Map
		Map<Object, Object> map = new HashMap<Object, Object>();
		luaState.pushJavaObject(JavaModule.getInstance().toTable(map));
		luaState.setGlobal("map");
		luaState.load("map.x = 1", "=testToTable");
		luaState.call(0, 0);
		assertEquals(Double.valueOf(1.0), map.get("x"));

		// List
		List<Object> list = new ArrayList<Object>();
		luaState.pushJavaObject(JavaModule.getInstance().toTable(list));
		luaState.setGlobal("list");
		luaState.load("list[1] = 1", "=testToList");
		luaState.call(0, 0);
		assertEquals(Double.valueOf(1.0), list.get(0));
	}
	
	/**
	 * Tests the Java module from Lua.
	 */
	@Test
	public void testJavaModule() throws Exception {
		runTest("org/terasology/jnlua/test/JavaModule.lua", "JavaModule");
	}
}
