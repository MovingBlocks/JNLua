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
 * Represents a Lua type.
 */
public enum LuaType {
	// -- Values
	/**
	 * Nil.
	 */
	NIL,

	/**
	 * Boolean.
	 */
	BOOLEAN,

	/**
	 * Light user data (pointer).
	 */
	LIGHTUSERDATA,

	/**
	 * Number.
	 */
	NUMBER,

	/**
	 * String.
	 */
	STRING,

	/**
	 * Table.
	 */
	TABLE,

	/**
	 * Function.
	 */
	FUNCTION,

	/**
	 * User data.
	 */
	USERDATA,

	/**
	 * Thread.
	 */
	THREAD;

	// -- Properties
	/**
	 * Returns the display text of this Lua type. The display text is the type
	 * name in lower case.
	 * 
	 * @return the display text
	 */
	public String displayText() {
		return toString().toLowerCase();
	}
}
