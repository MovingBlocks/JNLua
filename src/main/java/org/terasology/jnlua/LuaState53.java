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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LuaState53 extends LuaState {
    public LuaState53() {
        super();
    }

    public LuaState53(int memory) {
        super(memory);
    }

    @Override
    native int lua_integerwidth();

    @Override
    native int lua_registryindex();

    @Override
    native String lua_version();

    @Override
    native int lua_versionnum();

    @Override
    native void lua_newstate(int apiversion, long luaState);

    @Override
    native void lua_close(boolean ownState);

    @Override
    native int lua_gc(int what, int data);

    @Override
    native void lua_openlib(int lib);

    @Override
    native void lua_load(InputStream inputStream, String chunkname,
                           String mode) throws IOException;

    @Override
    native void lua_dump(OutputStream outputStream, boolean strip) throws IOException;

    @Override
    native void lua_pcall(int nargs, int nresults);

    @Override
    native void lua_getglobal(String name);

    @Override
    native void lua_setglobal(String name);

    @Override
    native void lua_pushboolean(boolean b);

    @Override
    native void lua_pushbytearray(byte[] b);

    @Override
    native void lua_pushinteger(long n);

    @Override
    native void lua_pushjavafunction(JavaFunction f);

    @Override
    native void lua_pushjavaobject(Object object);

    @Override
    native void lua_pushnil();

    @Override
    native void lua_pushnumber(double n);

    @Override
    native boolean lua_isboolean(int index);

    @Override
    native boolean lua_iscfunction(int index);

    @Override
    native boolean lua_isfunction(int index);

    @Override
    native boolean lua_isjavafunction(int index);

    @Override
    native boolean lua_isjavaobject(int index);

    @Override
    native boolean lua_isnil(int index);

    @Override
    native boolean lua_isnone(int index);

    @Override
    native boolean lua_isnoneornil(int index);

    @Override
    native boolean lua_isnumber(int index);

    @Override
    native boolean lua_isstring(int index);

    @Override
    native boolean lua_istable(int index);

    @Override
    native boolean lua_isthread(int index);

    @Override
    native int lua_compare(int index1, int index2, int operator);

    @Override
    native int lua_rawequal(int index1, int index2);

    @Override
    native int lua_rawlen(int index);

    @Override
    native boolean lua_toboolean(int index);

    @Override
    native byte[] lua_tobytearray(int index);

    @Override
    native long lua_tointeger(int index);

    @Override
    native Long lua_tointegerx(int index);

    @Override
    native JavaFunction lua_tojavafunction(int index);

    @Override
    native Object lua_tojavaobject(int index);

    @Override
    native double lua_tonumber(int index);

    @Override
    native Double lua_tonumberx(int index);

    @Override
    native long lua_topointer(int index);

    @Override
    native int lua_type(int index);

    @Override
    native int lua_absindex(int index);

    @Override
    native int lua_arith(int operator);

    @Override
    native void lua_concat(int n);

    @Override
    native int lua_copy(int fromIndex, int toIndex);

    @Override
    native int lua_gettop();

    @Override
    native void lua_len(int index);

    @Override
    native void lua_insert(int index);

    @Override
    native void lua_pop(int n);

    @Override
    native void lua_pushvalue(int index);

    @Override
    native void lua_remove(int index);

    @Override
    native void lua_replace(int index);

    @Override
    native void lua_settop(int index);

    @Override
    native void lua_createtable(int narr, int nrec);

    @Override
    native int lua_getsubtable(int idx, String fname);

    @Override
    native void lua_gettable(int index);

    @Override
    native void lua_getfield(int index, String k);

    @Override
    native void lua_newtable();

    @Override
    native int lua_next(int index);

    @Override
    native void lua_rawget(int index);

    @Override
    native void lua_rawgeti(int index, int n);

    @Override
    native void lua_rawset(int index);

    @Override
    native void lua_rawseti(int index, int n);

    @Override
    native void lua_settable(int index);

    @Override
    native void lua_setfield(int index, String k);

    @Override
    native int lua_getmetatable(int index);

    @Override
    native void lua_setmetatable(int index);

    @Override
    native int lua_getmetafield(int index, String k);

    @Override
    native void lua_newthread();

    @Override
    native int lua_resume(int index, int nargs);

    @Override
    native int lua_status(int index);

    @Override
    native int lua_ref(int index);

    @Override
    native void lua_unref(int index, int ref);

    @Override
    native LuaDebug lua_getstack(int level);

    @Override
    native int lua_getinfo(String what, LuaState.LuaDebug ar);

    @Override
    native int lua_tablesize(int index);

    @Override
    native void lua_tablemove(int index, int from, int to, int count);

    public static class LuaDebug extends LuaState.LuaDebug {
        LuaDebug(long luaDebug, boolean ownDebug) {
            super(luaDebug, ownDebug);
        }

        @Override
        native void lua_debugfree();

        @Override
        native String lua_debugname();

        @Override
        native String lua_debugnamewhat();
    }
}
