/*
 * $Id: LuaException.java,v 1.1 2008/10/28 16:36:48 anaef Exp $
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua;

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
