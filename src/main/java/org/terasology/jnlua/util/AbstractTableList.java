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

package org.terasology.jnlua.util;

import java.util.AbstractList;
import java.util.RandomAccess;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;

/**
 * Abstract list implementation backed by a Lua table.
 */
public abstract class AbstractTableList extends AbstractList<Object> implements
		RandomAccess, LuaValueProxy {
	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public AbstractTableList() {
	}

	// -- List methods
	@Override
	public void add(int index, Object element) {
		LuaState luaState = getLuaState();
		synchronized (luaState) {
			int size = size();
			if (index < 0 || index > size) {
				throw new IndexOutOfBoundsException("index: " + index
						+ ", size: " + size);
			}
			pushValue();
			luaState.tableMove(-1, index + 1, index + 2, size - index);
			luaState.pushJavaObject(element);
			luaState.rawSet(-2, index + 1);
			luaState.pop(1);
		}
	}

	@Override
	public Object get(int index) {
		LuaState luaState = getLuaState();
		synchronized (luaState) {
			int size = size();
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("index: " + index
						+ ", size: " + size);
			}
			pushValue();
			luaState.rawGet(-1, index + 1);
			try {
				return luaState.toJavaObject(-1, Object.class);
			} finally {
				luaState.pop(2);
			}
		}
	}

	@Override
	public Object remove(int index) {
		LuaState luaState = getLuaState();
		synchronized (luaState) {
			int size = size();
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("index: " + index
						+ ", size: " + size);
			}
			Object oldValue = get(index);
			pushValue();
			luaState.tableMove(-1, index + 2, index + 1, size - index - 1);
			luaState.pushNil();
			luaState.rawSet(-2, size);
			luaState.pop(1);
			return oldValue;
		}
	}

	@Override
	public Object set(int index, Object element) {
		LuaState luaState = getLuaState();
		synchronized (luaState) {
			int size = size();
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("index: " + index
						+ ", size: " + size);
			}
			Object oldValue = get(index);
			pushValue();
			luaState.pushJavaObject(element);
			luaState.rawSet(-2, index + 1);
			luaState.pop(1);
			return oldValue;
		}
	}

	@Override
	public int size() {
		LuaState luaState = getLuaState();
		synchronized (luaState) {
			pushValue();
			try {
				return luaState.rawLen(-1);
			} finally {
				luaState.pop(1);
			}
		}
	}
}
