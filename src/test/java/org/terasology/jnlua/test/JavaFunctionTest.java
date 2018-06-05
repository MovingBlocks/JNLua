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

import org.junit.Assert;
import org.junit.Test;

import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaState;

/**
 * Contains unit tests for Java functions.
 */
public class JavaFunctionTest extends AbstractLuaTest {
	// -- Test cases
	/**
	 * Tests the call of a Lua function implemented in Java.
	 */
	@Test
	public void testJavaFunction() throws Exception {
		// Push function
		luaState.pushJavaFunction(new Add());

		// Push arguments
		luaState.pushNumber(1);
		luaState.pushNumber(1);
		luaState.call(2, 1);

		// Test result
		Assert.assertEquals(2.0, luaState.toNumber(1), 0.0);
		luaState.pop(1);

		// Finish
		Assert.assertEquals(0, luaState.getTop());
	}

	// -- Private classes
	/**
	 * A simple Lua function.
	 */
	private static class Add implements JavaFunction {
		public int invoke(LuaState luaState) {
			double a = luaState.toNumber(1);
			double b = luaState.toNumber(2);
			luaState.setTop(0);
			luaState.pushNumber(a + b);
			return 1;
		}
	}
}