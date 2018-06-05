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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.console.LuaConsole;

/**
 * Contains tests for the Lua console.
 */
public class LuaConsoleTest {
	// -- Test cases
	/**
	 * Tests the Lua console.
	 */
	@Test
	public void testLuaConsole ()
	{
		LuaConsole luaConsole = new LuaConsole(new String[] { "a", "b" });
		LuaState luaState = luaConsole.getLuaState();
		assertNotNull(luaState);
		assertTrue(luaState.isOpen());
		luaState.getGlobal("argv");
		assertTrue(luaState.isTable(1));
		assertEquals(2, luaState.rawLen(1));
		luaState.rawGet(1, 1);
		assertTrue(luaState.isString(2));
		assertEquals("a", luaState.toString(2));
		luaState.pop(2);
	}
}
