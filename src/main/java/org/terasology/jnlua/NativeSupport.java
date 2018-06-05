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
 * Loads the JNLua native library.
 * 
 * The class provides and configures a default loader implementation that loads
 * the JNLua native library by means of the <code>System.loadLibrary</code>
 * method. In some situations, you may want to override this behavior. For
 * example, when using JNLua as an OSGi bundle, the native library is loaded by
 * the OSGi runtime. Therefore, the OSGi bundle activator replaces the loader by
 * a no-op implementaion. Note that the loader must be configured before
 * LuaState is accessed.
 */
public final class NativeSupport {
	// -- Static
	private static final NativeSupport INSTANCE = new NativeSupport();

	// -- State
	private Loader loader = new DefaultLoader();

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static NativeSupport getInstance() {
		return INSTANCE;
	}

	// -- Construction
	/**
	 * Private constructor to prevent external instantiation.
	 */
	private NativeSupport() {
	}

	// -- Properties
	/**
	 * Return the native library loader.
	 * 
	 * @return the loader
	 */
	public Loader getLoader() {
		return loader;
	}

	/**
	 * Sets the native library loader.
	 * 
	 * @param loader
	 *            the loader
	 */
	public void setLoader(Loader loader) {
		if (loader == null) {
			throw new NullPointerException("loader must not be null");
		}
		this.loader = loader;
	}

	// -- Member types
	/**
	 * Loads the library.
	 */
	public interface Loader {
		public void load(Class src);
	}

	private class DefaultLoader implements Loader {
		@Override
		public void load(Class src) {
			// Generate library name.
			StringBuilder builder = new StringBuilder("jnlua-");

			if (src == LuaState53.class) {
				builder.append("5.3-");
			} else {
				builder.append("5.2-");
			}

			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				// Windows
				builder.append("windows-");
			} else {
				// Assume Linux
				builder.append("linux-");
			}

			if (System.getProperty("os.arch").endsWith("64")) {
				// Assume x86_64
				builder.append("amd64");
			} else {
				// Assume x86_32
				builder.append("i686");
			}

			System.loadLibrary(builder.toString());
		}
	}
}
