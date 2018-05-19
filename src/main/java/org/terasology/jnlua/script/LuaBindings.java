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

package org.terasology.jnlua.script;

import javax.script.Bindings;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.util.AbstractTableMap;

/**
 * Lua bindings implementation conforming to JSR 223: Scripting for the Java
 * Platform.
 */
class LuaBindings extends AbstractTableMap<String> implements Bindings {
	// -- State
	private LuaScriptEngine scriptEngine;

	// -- Construction
	public LuaBindings(LuaScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}

	// -- AbstractTableMap methods
	@Override
	protected void checkKey(Object key) {
		super.checkKey(key);
		if (!(key instanceof String)) {
			throw new IllegalArgumentException("key must be a string");
		}
		if (((String) key).length() == 0) {
			throw new IllegalArgumentException("key must not be empty");
		}
	}

	@Override
	protected boolean filterKeys() {
		return true;
	}

	@Override
	protected boolean acceptKey(int index) {
		return getLuaState().isString(index)
				&& getLuaState().toString(index).length() > 0;
	}

	@Override
	protected String convertKey(int index) {
		return getLuaState().toString(index);
	}

	// -- LuaProxy methods
	@Override
	public LuaState getLuaState() {
		return scriptEngine.getLuaState();
	}

	@Override
	public void pushValue() {
		getLuaState().rawGet(LuaState.REGISTRYINDEX, LuaState.RIDX_GLOBALS);
	}

	// -- Package-private methods
	/**
	 * Returns the script engine.
	 */
	LuaScriptEngine getScriptEngine() {
		return scriptEngine;
	}
}
