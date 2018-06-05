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

package org.terasology.jnlua;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Indicates a Lua runtime error.
 * 
 * <p>
 * This exception is thrown if a Lua runtime error occurs. The class provides
 * access to the Lua stack trace by means of the {@link #getLuaStackTrace()}
 * method.
 * </p>
 */
public class LuaRuntimeException extends LuaException {
	// -- Static
	private static final long serialVersionUID = 1L;
	private static final LuaStackTraceElement[] EMPTY_LUA_STACK_TRACE = new LuaStackTraceElement[0];

	// -- State
	private LuaStackTraceElement[] luaStackTrace;

	// -- Construction
	/**
	 * Creates a new instance. The instance is created with an empty Lua stack
	 * trace.
	 * 
	 * @param msg
	 *            the message
	 */
	public LuaRuntimeException(String msg) {
		super(msg);
		luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	/**
	 * Creates a new instance. The instance is created with an empty Lua stack
	 * trace.
	 * 
	 * @param msg
	 *            the message
	 * @param cause
	 *            the cause of this exception
	 */
	public LuaRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
		luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	/**
	 * Creates a new instance. The instance is created with an empty Lua stack
	 * trace.
	 * 
	 * @param cause
	 *            the cause of this exception
	 */
	public LuaRuntimeException(Throwable cause) {
		super(cause);
		luaStackTrace = EMPTY_LUA_STACK_TRACE;
	}

	// -- Properties
	/**
	 * Returns the Lua stack trace of this runtime exception.
	 */
	public LuaStackTraceElement[] getLuaStackTrace() {
		return luaStackTrace.clone();
	}

	// -- Operations
	/**
	 * Prints this exception and its Lua stack trace to the standard error
	 * stream.
	 */
	public void printLuaStackTrace() {
		printLuaStackTrace(System.err);
	}

	/**
	 * Prints this exception and its Lua stack trace to the specified print
	 * stream.
	 * 
	 * @param s
	 *            the print stream
	 */
	public void printLuaStackTrace(PrintStream s) {
		synchronized (s) {
			s.println(this);
			for (int i = 0; i < luaStackTrace.length; i++) {
				s.println("\tat " + luaStackTrace[i]);
			}
		}
	}

	/**
	 * Prints this exception and its Lua stack trace to the specified print
	 * writer.
	 * 
	 * @param s
	 *            the print writer
	 */
	public void printLuaStackTrace(PrintWriter s) {
		synchronized (s) {
			s.println(this);
			for (int i = 0; i < luaStackTrace.length; i++) {
				s.println("\tat " + luaStackTrace[i]);
			}
		}
	}

	// -- Package private methods
	/**
	 * Sets the Lua error in this exception. The method in invoked from the
	 * native library.
	 */
	void setLuaError(LuaError luaError) {
		initCause(luaError.getCause());
		luaStackTrace = luaError.getLuaStackTrace();
	}
}
