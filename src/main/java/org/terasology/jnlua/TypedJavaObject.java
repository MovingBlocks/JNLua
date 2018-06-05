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
 * Represents a Java object with an explicit type.
 * 
 * <p>
 * The interface is implemented by objects needing to specify an explicit type
 * for a wrapped object. This typically occurs in casting situations. Such typed
 * Java object are considered <i>weak</i> since they have no representative
 * value of their own. Weak typed Java objects always convert to wrapped object.
 * </p>
 * 
 * <p>
 * The interface is also implemented by objects wrapping another object and
 * offering transparent conversion to the wrapped object if needed. This
 * situation for example occurs when an object implements the
 * {@link JavaReflector} interface to provide custom Java
 * reflection for a wrapped object and at the same time wants to ensure
 * transparent conversion to the wrapped object if needed. Such typed Java
 * objects are considered <i>strong</i> since they have a representative value
 * of their own. Strong typed Java objects convert to wrapped object only if
 * this is required to satisfy a type conversion.
 * </p>
 */
public interface TypedJavaObject {
	/**
	 * Returns the object.
	 * 
	 * @return the object
	 */
	Object getObject();

	/**
	 * Returns the type.
	 * 
	 * @return the type
	 */
	Class<?> getType();

	/**
	 * Returns whether this is a strong typed Java object.
	 * 
	 * @return <code>true</code> if this typed Java object is strong, and
	 *         <code>false</code> if it is weak
	 */
	boolean isStrong();
}
