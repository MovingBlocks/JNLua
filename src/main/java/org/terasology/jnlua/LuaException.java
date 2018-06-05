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

/**
 * Abstract base class for Lua error conditions. Lua exceptions are unchecked
 * runtime exceptions.
 */
public abstract class LuaException extends RuntimeException {
	// -- Static
	private static final long serialVersionUID = 1L;

	// -- Construction
	/**
	 * Creates a new instance.
	 * 
	 * @param msg
	 *            the message
	 */
	public LuaException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param msg
	 *            the message
	 * @param cause
	 *            the cause of this exception
	 */
	public LuaException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param cause
	 *            the cause of this exception
	 */
	public LuaException(Throwable cause) {
		super(cause);
	}
}
