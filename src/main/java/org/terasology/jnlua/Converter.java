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
 * Converts between Lua values and Java objects.
 */
public interface Converter {
	/**
	 * Returns the type distance between a Lua value and a formal Java type.
	 * Distances are comparable for the same Lua value only. If a Lua value
	 * cannot be converted to the specified formal type, the method returns
	 * <code>Integer.MAX_VALUE</code>.
	 * 
	 * @param luaState
	 *            the Lua state
	 * @param index
	 *            the stack index containing the value
	 * @param formalType
	 *            the formal Java type
	 * @return the type distance, or <code>Integer.MAX_VALUE</code> if the
	 *         conversion is not supported
	 */
	public int getTypeDistance(LuaState luaState, int index, Class<?> formalType);

	/**
	 * Converts a Lua value to a Java object of the specified formal type.
	 * 
	 * <p>
	 * If the Lua value is <code>nil</code>, the method returns
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param luaState
	 *            the Lua state
	 * @param index
	 *            the stack index containing the value
	 * @return the Java object, or <code>null</code>
	 * @param formalType
	 *            the formal Java type
	 * @throws ClassCastException
	 *             if the conversion is not possible
	 */
	<T> T convertLuaValue(LuaState luaState, int index,
						  Class<T> formalType);

	/**
	 * Converts a Java object to a Lua value and pushes that value on the stack.
	 * 
	 * <p>
	 * If the object is <code>null</code>, the method pushes <code>nil</code>.
	 * </p>
	 * 
	 * @param luaState
	 *            the Lua state
	 * @param object
	 *            the Java object, or <code>null</code>
	 */
	void convertJavaObject(LuaState luaState, Object object);
}
