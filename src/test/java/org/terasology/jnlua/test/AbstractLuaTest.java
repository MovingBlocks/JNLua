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

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;

import org.terasology.jnlua.LuaState;

/**
 * Abstract base class for JNLua unit tests.
 */
public abstract class AbstractLuaTest {
	// -- State
	protected LuaState luaState;

	// -- Setup
	/**
	 * Performs setup.
	 */
	@Before
	public void setup() throws Exception {
		luaState = new LuaState();
	}

	/**
	 * Performs teardown.
	 */
	@After
	public void teardown() throws Throwable {
		if (luaState != null) {
			try {
				luaState.close();
			} catch (Throwable e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	// -- Protected method
	/**
	 * Runs a Lua-based test.
	 */
	protected void runTest(String source, String moduleName) throws Exception {
		// Open libraries
		luaState.openLibs();

		// Load
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(source);
		luaState.load(inputStream, "=" + moduleName, "t");
		luaState.call(0, 1);

		// Run all module functions beginning with "test"
		luaState.getGlobal(moduleName);
		luaState.pushNil();
		while (luaState.next(1)) {
			String key = luaState.toString(-2);
			if (key.startsWith("test") && luaState.isFunction(-1)) {
				luaState.call(0, 0);
			} else {
				luaState.pop(1);
			}
		}
	}
}
