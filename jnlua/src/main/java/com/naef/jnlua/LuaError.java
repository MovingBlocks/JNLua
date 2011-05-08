/*
 * $Id: LuaError.java,v 1.1 2008/10/28 16:36:48 anaef Exp $
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua;

/**
 * Contains information about a Lua error condition. This object is created in
 * the native library.
 */
class LuaError {
	// -- State
	private String message;
	private LuaStackTraceElement[] luaStackTrace;
	private Throwable cause;

	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public LuaError(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
	}

	// -- Properties
	/**
	 * Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the Lua stack trace.
	 */
	public LuaStackTraceElement[] getLuaStackTrace() {
		return luaStackTrace;
	}

	/**
	 * Returns the cause.
	 */
	public Throwable getCause() {
		return cause;
	}

	// -- Object methods
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (message != null) {
			sb.append(message);
		}
		if (cause != null && cause.getMessage() != null) {
			sb.append(cause.getMessage());
		}
		return sb.toString();
	}

	// -- Package private methods
	/**
	 * Sets the Lua stack trace.
	 */
	void setLuaStackTrace(LuaStackTraceElement[] luaStackTrace) {
		this.luaStackTrace = luaStackTrace;
	}
}
