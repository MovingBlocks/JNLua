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

import java.io.ByteArrayInputStream;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Compiled script implementation conforming to JSR 223: Scripting for the Java
 * Platform.
 */
class CompiledLuaScript extends CompiledScript {
	// -- State
	private final LuaScriptEngine engine;
	private final byte[] script;

	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public CompiledLuaScript(LuaScriptEngine engine, byte[] script) {
		this.engine = engine;
		this.script = script;
	}

	// -- CompiledScript methods
	@Override
	public Object eval(ScriptContext context) throws ScriptException {
		synchronized (engine.getLuaState()) {
			engine.loadChunk(new ByteArrayInputStream(script), context, "b");
			return engine.callChunk(context);
		}
	}

	@Override
	public ScriptEngine getEngine() {
		return engine;
	}
}
