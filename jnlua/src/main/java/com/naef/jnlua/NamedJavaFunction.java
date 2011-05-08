/*
 * $Id: NamedJavaFunction.java,v 1.1 2008/10/28 16:36:48 anaef Exp $
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua;

/**
 * Provides a named Java function.
 */
public interface NamedJavaFunction extends JavaFunction {
	/**
	 * Returns the name of this Java function.
	 * 
	 * @return the Java function name
	 */
	public String getName();
}
