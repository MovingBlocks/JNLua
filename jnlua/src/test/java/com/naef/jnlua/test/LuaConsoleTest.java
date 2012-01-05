/*
 * $Id: AbstractLuaTest.java 38 2012-01-04 22:44:15Z andre@naef.com $
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.console.LuaConsole;

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
