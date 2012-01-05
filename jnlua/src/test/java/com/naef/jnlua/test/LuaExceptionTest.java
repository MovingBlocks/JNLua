/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaGcMetamethodException;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaStackTraceElement;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaSyntaxException;
import com.naef.jnlua.LuaState.Library;

/**
 * Contains unit tests for Lua exceptions.
 */
public class LuaExceptionTest extends AbstractLuaTest {
	// -- Test cases
	/**
	 * Tests the call of a Lua function which invokes the Lua error function.
	 */
	@Test
	public void testLuaError() throws Exception {
		// Load program
		luaState.openLibs();
		StringBuffer sb = new StringBuffer();
		sb.append("function A ()\n");
		sb.append("    B()\n");
		sb.append("end\n");
		sb.append("\n");
		sb.append("function B ()\n");
		sb.append("    C()\n");
		sb.append("end\n");
		sb.append("\n");
		sb.append("function C ()\n");
		sb.append("    error(\"msg\")\n");
		sb.append("end\n");
		sb.append("\n");
		sb.append("A()\n");
		luaState.load(sb.toString(), "=testLuaError");

		// Run
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.call(0, 0);
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}
		assertTrue(luaRuntimeException.getMessage().endsWith("msg"));
		LuaStackTraceElement[] luaStackTrace = luaRuntimeException
				.getLuaStackTrace();
		assertEquals(5, luaStackTrace.length);
		assertEquals(new LuaStackTraceElement("error", null, -1),
				luaStackTrace[0]);
		assertEquals(new LuaStackTraceElement("C", "testLuaError", 10),
				luaStackTrace[1]);
		assertEquals(new LuaStackTraceElement("B", "testLuaError", 6), luaStackTrace[2]);
		assertEquals(new LuaStackTraceElement("A", "testLuaError", 2), luaStackTrace[3]);
		assertEquals(new LuaStackTraceElement(null, "testLuaError", 13),
				luaStackTrace[4]);
	}

	/**
	 * Tests the call of a Java function which throws a Java runtime exception.
	 */
	@Test
	public void testRuntimeException() throws Exception {
		// Push function
		luaState.pushJavaFunction(new RuntimeExceptionFunction());

		// Push arguments
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.call(0, 0);
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}
		assertNotNull(luaRuntimeException);
		Throwable cause = luaRuntimeException.getCause();
		assertNotNull(cause);
		assertTrue(cause instanceof ArithmeticException);
	}

	/**
	 * Tests the call of a Java function which throws a Lua runtime exception.
	 */
	@Test
	public void testLuaRuntimeException() throws Exception {
		// Push function
		luaState.pushJavaFunction(new LuaRuntimeExceptionFunction());

		// Push arguments
		LuaRuntimeException luaRuntimeException = null;
		try {
			luaState.call(0, 0);
		} catch (LuaRuntimeException e) {
			luaRuntimeException = e;
		}
		assertNotNull(luaRuntimeException);
		Throwable cause = luaRuntimeException.getCause();
		assertNotNull(cause);
		assertTrue(cause instanceof LuaRuntimeException);
	}

	/**
	 * Tests the generation of a Lua syntax exception on Lua code with invalid
	 * syntax.
	 */
	@Test
	public void testLuaSyntaxException() throws Exception {
		LuaSyntaxException luaSyntaxException = null;
		try {
			luaState.load("An invalid chunk of Lua.", "=testLuaSyntaxException");
		} catch (LuaSyntaxException e) {
			luaSyntaxException = e;
		}
		assertNotNull(luaSyntaxException);
	}

	/**
	 * Tests the generation of a Lua GC metamethod exception on a Lua value
	 * raising an error in its <code>__gc</code> metamethod.
	 */
	@Test
	public void testLuaGcMetamethodException() throws Exception {
		LuaGcMetamethodException luaGcMetamethodException = null;
		luaState.openLib(Library.BASE);
		luaState.pop(1);
		luaState.load(
				"setmetatable({}, { __gc = function() error(\"gc\") end })\n"
						+ "collectgarbage()", "=testLuaGcMetamethodException");
		try {
			luaState.call(0, 0);
		} catch (LuaGcMetamethodException e) {
			luaGcMetamethodException = e;
		}
		assertNotNull(luaGcMetamethodException);
	}

	// -- Private classes
	/**
	 * Provides a function throwing a Java runtime exception.
	 */
	private class RuntimeExceptionFunction implements JavaFunction {
		public int invoke(LuaState luaState) throws LuaRuntimeException {
			@SuppressWarnings("unused")
			int a = 0 / 0;
			return 0;
		}
	}
	
	/**
	 * Provides a function throwing a Lua runtime exception with a cause.
	 */
	private class LuaRuntimeExceptionFunction implements JavaFunction {
		public int invoke(LuaState luaState) throws LuaRuntimeException {
			try {
				@SuppressWarnings("unused")
				int a = 0 / 0;
			} catch (ArithmeticException e) {
				throw new LuaRuntimeException(e.getMessage(), e);
			}
			return 0;
		}
	}
}
