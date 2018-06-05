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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaState53;

/**
 * Lua script engine factory implementation conforming to JSR 223: Scripting for
 * the Java Platform.
 */
public class LuaScriptEngineFactory implements ScriptEngineFactory {
	// -- Static
	private static final String ENGINE_NAME = "JNLua";
	private static final String LANGUAGE_NAME = "Lua";
	private static final List<String> EXTENSIONS;
	private static final List<String> MIME_TYPES;
	private static final List<String> NAMES;
	static {
		// Extensions
		List<String> extensions = new ArrayList<String>();
		extensions.add("lua");
		EXTENSIONS = Collections.unmodifiableList(extensions);

		// MIME types
		List<String> mimeTypes = new ArrayList<String>();
		mimeTypes.add("application/x-lua");
		mimeTypes.add("text/x-lua");
		MIME_TYPES = Collections.unmodifiableList(mimeTypes);

		// Names
		List<String> names = new ArrayList<String>();
		names.add("lua");
		names.add("Lua");
		names.add("jnlua");
		names.add("JNLua");
		NAMES = Collections.unmodifiableList(names);
	}

	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public LuaScriptEngineFactory() {
	}

	// -- ScriptEngineFactory methods
	@Override
	public String getEngineName() {
		return ENGINE_NAME;
	}

	@Override
	public String getEngineVersion() {
		return LuaState.VERSION;
	}

	@Override
	public List<String> getExtensions() {
		return EXTENSIONS;
	}

	@Override
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}

	@Override
	public List<String> getNames() {
		return NAMES;
	}

	@Override
	public String getLanguageName() {
		return LANGUAGE_NAME;
	}

	@Override
	public String getLanguageVersion() {
		return "5.3"; /* TODO */
	}

	@Override
	public Object getParameter(String key) {
		if (key.equals(ScriptEngine.ENGINE)) {
			return getEngineName();
		}
		if (key.equals(ScriptEngine.ENGINE_VERSION)) {
			return getEngineVersion();
		}
		if (key.equals(ScriptEngine.NAME)) {
			return getNames().get(0);
		}
		if (key.equals(ScriptEngine.LANGUAGE)) {
			return getLanguageName();
		}
		if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return getLanguageVersion();
		}
		if (key.equals("THREADING")) {
			return "MULTITHREADED";
		}
		return null;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		StringBuffer sb = new StringBuffer();
		sb.append(obj);
		sb.append(':');
		sb.append(m);
		sb.append('(');
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(args[i]);
		}
		sb.append(')');
		return sb.toString();
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		StringBuffer sb = new StringBuffer();
		sb.append("print(");
		quoteString(sb, toDisplay);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public String getProgram(String... statements) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < statements.length; i++) {
			sb.append(statements[i]);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new LuaScriptEngine(this);
	}

	// --Private methods
	/**
	 * Quotes a string in double quotes.
	 */
	private void quoteString(StringBuffer sb, String s) {
		sb.append('"');
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
			case '\u0007':
				sb.append("\\a");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\u000b':
				sb.append("\\v");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '"':
				sb.append("\\\"");
				break;
			default:
				sb.append(s.charAt(i));
			}
		}
		sb.append('"');
	}
}
