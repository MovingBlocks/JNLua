/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaState.ArithOperator;
import com.naef.jnlua.LuaState.GcAction;
import com.naef.jnlua.LuaState.Library;
import com.naef.jnlua.LuaState.RelOperator;
import com.naef.jnlua.LuaValueProxy;
import com.naef.jnlua.NamedJavaFunction;

/**
 * Throws illegal arguments at the Lua state for error testing.
 */
public class LuaStateErrorTest extends AbstractLuaTest {
	// -- Properties tests
	/**
	 * setClassLodaer(ClassLoader) with null class loader.
	 */
	@Test(expected = NullPointerException.class)
	public void setNullClassLoader() {
		luaState.setClassLoader(null);
	}

	/**
	 * setJavaReflector(JavaReflector) with null Java reflector.
	 */
	@Test(expected = NullPointerException.class)
	public void setNullJavaReflector() {
		luaState.setJavaReflector(null);
	}

	/**
	 * getMetamethod(Object, Metamethod) with null metamethod.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullGetMetamethod() {
		luaState.getMetamethod(null, null);
	}

	/**
	 * setConverter(Converter) with null converter.
	 */
	@Test(expected = NullPointerException.class)
	public void setNullConverter() {
		luaState.setConverter(null);
	}

	// -- Life cycle tests
	/**
	 * Tests closing the Lua state while running.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalClose() {
		luaState.pushJavaFunction(new JavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				luaState.close();
				return 0;
			}
		});
		luaState.call(0, 0);
	}

	/**
	 * Tests invoking a method after the Lua state has been closed.
	 */
	@Test(expected = IllegalStateException.class)
	public void testPostClose() {
		luaState.close();
		luaState.pushInteger(1);
	}

	/**
	 * gc(GcAction, int) null action.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullGc() {
		luaState.gc(null, 0);
	}

	// -- Registration tests
	/**
	 * openLib(Library) with null library.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullOpenLib() {
		luaState.openLib(null);
	}

	/**
	 * register(JavaFunction[]) with null function.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullFunctionRegister() {
		luaState.register(null);
	}

	/**
	 * register(String, JavaFunction[]) with null string.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullNameRegister() {
		luaState.register(null, new NamedJavaFunction[0], true);
	}

	/**
	 * register(String, JavaFunction[]) with null functions.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullFunctionsRegister() {
		luaState.register("", null, true);
	}

	// -- Load and dump tests
	/**
	 * load(InputStream, String) with null input stream.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullStreamLoad() throws Exception {
		luaState.load((InputStream) null, "=testNullStreamLoad", "bt");
	}

	/**
	 * load(InputStream, String) with null string.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullChunkLoad1() throws Exception {
		luaState.load(new ByteArrayInputStream(new byte[0]), null, "bt");
	}

	/**
	 * load(String, String) with null string 1.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullStringLoad() throws Exception {
		luaState.load((String) null, "=testNullStringLoad");
	}

	/**
	 * load(String, String) with null string 2.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullChunkLoad2() throws Exception {
		luaState.load("", null);
	}

	/**
	 * load(InputStream, String) with input stream throwing IO exception.
	 */
	@Test(expected = IOException.class)
	public void testIoExceptionLoad() throws Exception {
		luaState.load(new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException();
			}
		}, "=testIoExceptionLoad", "bt");
	}

	/**
	 * dump(OutputStream) with null output stream.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullDump() throws Exception {
		luaState.load("return 0", "=testNullDump");
		luaState.dump(null);
	}

	/**
	 * dump(OutputStream) with an output stream throwing a IO exception.
	 */
	@Test(expected = IOException.class)
	public void testIoExceptionDump() throws Exception {
		luaState.load("return 0", "=testIoExceptionDump");
		luaState.dump(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new IOException();
			}
		});
	}

	/**
	 * dump(OutputStream) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowDump() throws Exception {
		luaState.dump(new ByteArrayOutputStream());
	}

	// -- Call tests
	/**
	 * Call(int, int) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowCall() {
		luaState.openLibs();
		luaState.getGlobal("print");
		luaState.call(1, 1);
	}

	/**
	 * Call(int, int) with an extremely high number of returns.
	 */
	@Test(expected = IllegalStateException.class)
	public void testOverflowCall() {
		luaState.openLibs();
		luaState.getGlobal("print");
		luaState.pushString("");
		luaState.call(1, Integer.MAX_VALUE);
	}

	/**
	 * Call(int, int) with an illegal number of arguments.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCall1() {
		luaState.openLibs();
		luaState.getGlobal("print");
		luaState.call(-1, 1);
	}

	/**
	 * Call(int, int) with an illegal number of returns.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCall2() {
		luaState.openLibs();
		luaState.getGlobal("print");
		luaState.pushString("");
		luaState.call(1, -2);
		assertEquals(0, luaState.getTop());
	}

	// -- Global tests
	/**
	 * getGlobal(String) with null.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullGetGlobal() {
		luaState.getGlobal(null);
	}

	/**
	 * setGlobal(String) with null.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullSetGlobal() {
		luaState.pushNumber(0.0);
		luaState.setGlobal(null);
	}

	/**
	 * setGlobal(String) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowSetGlobal() {
		luaState.setGlobal("global");
	}

	/**
	 * setGlobal(String) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testIllegalSetGlobal() {
		luaState.setGlobal("illegal");
	}

	// -- Stack push tests
	/**
	 * pushJavaFunction(JavaFunction) with null argument.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullPushJavaFunction() {
		luaState.pushJavaFunction(null);
	}

	/**
	 * pushJavaObjectRaw(Object) with null argument.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullPushJavaObjectRaw() {
		luaState.pushJavaObjectRaw(null);
	}

	/**
	 * pushString(String) with null argument.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullPushString() {
		luaState.pushString(null);
	}

	/**
	 * pushNumber(Double) until stack overflow.
	 */
	@Test(expected = IllegalStateException.class)
	public void testStackOverflow() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			luaState.pushNumber(0.0);
		}
	}

	// -- Stack query tests
	/**
	 * compare(int, int, RelOperator) with number and nil for less than.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCompareLt() {
		luaState.pushNumber(1);
		luaState.pushNil();
		luaState.compare(1, 2, RelOperator.LT);
	}

	/**
	 * compare(int, int, RelOperator) with number and nil for less or equal.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCompareLe() {
		luaState.pushNumber(1);
		luaState.pushNil();
		luaState.compare(1, 2, RelOperator.LE);
	}

	/**
	 * rawLen(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawLen() {
		luaState.rawLen(getIllegalIndex());
	}

	/**
	 * toInteger(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToInteger() {
		luaState.toInteger(getIllegalIndex());
	}

	/**
	 * toJavaFunction(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToIJavaFunction() {
		luaState.toJavaFunction(getIllegalIndex());
	}

	/**
	 * toJavaObject(int) with illegal index and LuaValueProxy type.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToIJavaObject() {
		luaState.toJavaObject(getIllegalIndex(), LuaValueProxy.class);
	}

	/**
	 * toJavaObjectRaw(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToIJavaObjectRaw() {
		luaState.toJavaObjectRaw(getIllegalIndex());
	}

	/**
	 * toNumber(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToNumber() {
		luaState.toNumber(getIllegalIndex());
	}

	/**
	 * toNumber(int) with maximum index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMaxToNumber() {
		luaState.toNumber(Integer.MAX_VALUE);
	}

	/**
	 * toNumber(int) with minimum index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMinToNumbern() {
		luaState.toNumber(Integer.MIN_VALUE);
	}

	/**
	 * toPointer(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToPointer() {
		luaState.toPointer(getIllegalIndex());
	}

	/**
	 * toString(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalToString() {
		luaState.toString(getIllegalIndex());
	}

	// -- Stack operation test
	/**
	 * arith(ArithOperator) with two missing arguments for addition.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowArith1() {
		luaState.arith(ArithOperator.ADD);
	}

	/**
	 * arith(ArithOperator) with one missing argument for addition.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowArith2() {
		luaState.pushNumber(1);
		luaState.arith(ArithOperator.ADD);
	}

	/**
	 * arith(ArithOperator) with one missing argument for mathematical negation.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowArith3() {
		luaState.arith(ArithOperator.UNM);
	}

	/**
	 * concat(int) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowConcat1() {
		luaState.concat(1);
	}

	/**
	 * concat(int) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowConcat2() {
		luaState.pushString("");
		luaState.pushString("");
		luaState.concat(3);
	}

	/**
	 * concat(int) with an illegal number of arguments.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConcat() {
		luaState.concat(-1);
	}

	/**
	 * copy(int, int) with two illegal indexes.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCopy1() {
		luaState.copy(getIllegalIndex(), getIllegalIndex());
	}

	/**
	 * copy(int, int) with one illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCopy2() {
		luaState.pushInteger(1);
		luaState.copy(1, getIllegalIndex());
	}

	/**
	 * len(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalLen() {
		luaState.len(getIllegalIndex());
	}

	/**
	 * insert(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalInsert() {
		luaState.insert(getIllegalIndex());
	}

	/**
	 * pop(int) with insufficient arguments.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testUnderflowPop() {
		luaState.pop(1);
	}

	/**
	 * pop(int) with an illegal number of arguments.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalPop() {
		luaState.pop(-1);
	}

	/**
	 * pushValue(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalPushValue() {
		luaState.pushValue(getIllegalIndex());
	}

	/**
	 * remove(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRemove() {
		luaState.remove(getIllegalIndex());
	}

	/**
	 * replace(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalReplace() {
		luaState.replace(getIllegalIndex());
	}

	/**
	 * setTop(int) with an illegal argument.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSetTop() {
		luaState.setTop(-1);
	}

	// -- Table tests
	/**
	 * getTable(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetTable1() {
		luaState.pushString("");
		luaState.getTable(getIllegalIndex());
	}

	/**
	 * getTable(int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetTable2() {
		luaState.pushNumber(0.0);
		luaState.pushString("");
		luaState.getTable(1);
	}

	/**
	 * getField(int, String) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetField1() {
		luaState.getField(getIllegalIndex(), "");
	}

	/**
	 * getField(int, String) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetField2() {
		luaState.pushNumber(0.0);
		luaState.getField(1, "");
	}

	/**
	 * newTable(int, int) with negative array count.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalNewTable1() {
		luaState.newTable(-1, 0);
	}

	/**
	 * newTable(int, int) with negative record count.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalNewTable2() {
		luaState.newTable(0, -1);
	}

	/**
	 * next(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalNext1() {
		luaState.pushNil();
		luaState.next(getIllegalIndex());
	}

	/**
	 * next(int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalNext2() {
		luaState.pushNumber(0.0);
		luaState.pushNil();
		luaState.next(1);
	}

	/**
	 * rawGet(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawGet1() {
		luaState.rawGet(getIllegalIndex());
	}

	/**
	 * rawGet(int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawGet2() {
		luaState.pushNumber(0.0);
		luaState.pushString("");
		luaState.rawGet(1);
	}

	/**
	 * rawGet(int, int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawGet3() {
		luaState.rawGet(getIllegalIndex(), 1);
	}

	/**
	 * rawGet(int, int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawGet4() {
		luaState.pushNumber(0.0);
		luaState.rawGet(1, 1);
	}

	/**
	 * rawSet(int) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowRawSet() {
		luaState.newTable();
		luaState.rawSet(1);
	}

	/**
	 * rawSet(int) with nil index.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testNilRawSet() {
		luaState.newTable();
		luaState.pushNil();
		luaState.pushString("value");
		luaState.rawSet(1);
	}

	/**
	 * rawSet(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawSet1() {
		luaState.pushString("key");
		luaState.pushString("value");
		luaState.rawSet(getIllegalIndex());
	}

	/**
	 * rawSet(int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawSet2() {
		luaState.pushNumber(0.0);
		luaState.pushString("key");
		luaState.pushString("value");
		luaState.rawSet(1);
	}

	/**
	 * rawSet(int, int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRawSet3() {
		luaState.pushNumber(0.0);
		luaState.pushString("value");
		luaState.rawSet(1, 1);
	}

	/**
	 * setTable(int) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowSetTable() {
		luaState.newTable();
		luaState.setTable(1);
	}

	/**
	 * setTable(int) with nil index.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testNilSetTable() {
		luaState.newTable();
		luaState.pushNil();
		luaState.pushString("");
		luaState.setTable(1);
	}

	/**
	 * setTable(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSetTable() {
		luaState.pushNil();
		luaState.pushString("");
		luaState.setTable(getIllegalIndex());
	}

	/**
	 * setField(int, String) with null key.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullSetField() {
		luaState.newTable();
		luaState.pushString("value");
		luaState.setField(1, null);
	}

	/**
	 * setField(int, String) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSetField1() {
		luaState.pushString("");
		luaState.setField(getIllegalIndex(), "key");
	}

	/**
	 * setField(int, String) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSetField2() {
		luaState.pushNumber(0.0);
		luaState.pushString("");
		luaState.setField(1, "key");
	}

	// -- Metatable tests
	/**
	 * setMetatable(int) with invalid table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSetMetatable() {
		luaState.newTable();
		luaState.pushNumber(0.0);
		luaState.setMetatable(1);
	}

	// -- Thread tests
	/**
	 * resume(int, int) with insufficient arguments.
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnderflowResume() {
		luaState.openLibs();
		luaState.getGlobal("print");
		luaState.newThread();
		luaState.resume(1, 1);
	}

	/**
	 * resume(int, int) with invalid thread.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalResume1() {
		luaState.pushNumber(0.0);
		luaState.resume(1, 0);
	}

	/**
	 * resume(int, int) with an illegal number of returns.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalResume2() {
		luaState.openLibs();
		luaState.getGlobal("print");
		luaState.newThread();
		luaState.resume(1, -1);
	}

	/**
	 * status(int) with illegal thread.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalStatus() {
		luaState.pushNumber(0.0);
		luaState.status(1);
	}

	/**
	 * yield(int) with no running thread.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalYield1() {
		luaState.register(new NamedJavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				return luaState.yield(0);
			}

			@Override
			public String getName() {
				return "yieldfunc";
			}
		});
		luaState.load("return yieldfunc()", "=testIllegalYield1");
		luaState.call(0, 0);
	}

	/**
	 * yield across C-call boundary.
	 */
	@Test(expected=LuaRuntimeException.class)
	public void testIllegalYield2() {
		luaState.openLib(Library.COROUTINE);
		luaState.pop(1);
		JavaFunction yieldFunction = new JavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				luaState.load("return coroutine.yield()", "=testIllegalYield2");
				luaState.call(0, 0);
				return 0;
			}
		};
		luaState.pushJavaFunction(yieldFunction);
		luaState.newThread();
		luaState.resume(1, 0);
	}

	/**
	 * yield(int) with insufficient arguments.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testUnderflowYield() {
		luaState.register(new NamedJavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				return luaState.yield(1);
			}

			@Override
			public String getName() {
				return "yieldfunc";
			}
		});
		luaState.load("yieldfunc()", "=testUnderflowYield");
		luaState.newThread();
		luaState.resume(1, 0);
	}

	// -- Reference tests
	/**
	 * ref(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRef1() {
		luaState.pushNumber(0.0);
		luaState.ref(getIllegalIndex());
	}

	/**
	 * ref(int) with illegal table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalRef2() {
		luaState.pushNumber(0.0);
		luaState.pushNumber(0.0);
		luaState.ref(1);
	}

	/**
	 * unref(int, int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalUnref1() {
		luaState.newTable();
		luaState.pushNumber(0.0);
		int reference = luaState.ref(1);
		luaState.unref(getIllegalIndex(), reference);
	}

	/**
	 * unref(int, int) with illegal table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalUnref2() {
		luaState.pushNumber(0.0);
		luaState.pushNumber(0.0);
		luaState.unref(1, 1);
	}

	// -- Optimization tests
	/**
	 * tableSize(int) with illegal table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalTableSize1() {
		luaState.pushNumber(0.0);
		luaState.tableSize(1);
	}

	/**
	 * tableSize(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalTableSize2() {
		luaState.tableSize(1);
	}

	/**
	 * tableMove(int, int, int, int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalTableMove1() {
		luaState.tableMove(getIllegalIndex(), 1, 1, 0);
	}

	/**
	 * tableMove(int, int, int, int) with illegal count.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalTableMove2() {
		luaState.newTable();
		luaState.tableMove(1, 1, 1, -1);
	}

	// -- Argument checking tests
	/**
	 * checkArg(int, boolean, String) with false condition.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckArg() {
		luaState.pushBoolean(false);
		luaState.checkArg(1, false, "");
	}

	/**
	 * checkEnum(int, T[]) with null values.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullCheckEnum1() {
		luaState.pushString("");
		luaState.checkEnum(1, (GcAction[]) null);
	}

	/**
	 * checkEnum(int, T[], T) with null values.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullCheckEnum2() {
		luaState.pushString("");
		luaState.checkEnum(1, null, GcAction.STOP);
	}

	/**
	 * checkEnum(int, T[]) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckEnum1() {
		luaState.pushBoolean(false);
		luaState.checkEnum(1, GcAction.values());
	}

	/**
	 * checkEnum(int, T[], T) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckEnum2() {
		luaState.pushBoolean(false);
		luaState.checkEnum(1, GcAction.values(), GcAction.STOP);
	}

	/**
	 * checkInteger(int) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckInteger1() {
		luaState.pushBoolean(false);
		luaState.checkInteger(1);
	}

	/**
	 * checkInteger(int, int) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckInteger2() {
		luaState.pushBoolean(false);
		luaState.checkInteger(1, 2);
	}

	/**
	 * checkJavaObject(int) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckJavaObject1() {
		luaState.pushBoolean(false);
		luaState.checkJavaObject(1, Integer.class);
	}

	/**
	 * checkJavaObject(int, int) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckJavaFunction2() {
		luaState.pushBoolean(false);
		luaState.checkJavaObject(1, Integer.class, Integer.valueOf(0));
	}

	/**
	 * checkNumber(int) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckNumber1() {
		luaState.pushBoolean(false);
		luaState.checkNumber(1);
	}

	/**
	 * checkNumber(int, double) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckNumber2() {
		luaState.pushBoolean(false);
		luaState.checkNumber(1, 2.0);
	}

	/**
	 * checkOption(int, String[]) with null values.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullCheckOption1() {
		luaState.pushInteger(1);
		luaState.checkOption(1, null);
	}

	/**
	 * checkOption(int, String[], String) with null values.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullCheckOption2() {
		luaState.pushInteger(1);
		luaState.checkOption(1, null, "");
	}

	/**
	 * checkOption(int, String[]) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckOption1() {
		luaState.pushInteger(1);
		luaState.checkOption(1, new String[] { "test" });
	}

	/**
	 * checkOption(int, String[], String) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckOption2() {
		luaState.pushInteger(1);
		luaState.checkOption(1, new String[] { "test" }, "test");
	}

	/**
	 * checkOption(int, String[], String) with illegal default option.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckOption3() {
		luaState.checkOption(1, new String[] { "test" }, "");
	}

	/**
	 * checkString(int) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckString1() {
		luaState.pushBoolean(false);
		luaState.checkString(1);
	}

	/**
	 * checkString(int, String) with illegal argument.
	 */
	@Test(expected = LuaRuntimeException.class)
	public void testIllegalCheckString2() {
		luaState.pushBoolean(false);
		luaState.checkString(1, "");
	}

	// -- Proxy tests
	/**
	 * getProxy(int, Class[]) with null interface.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullGetProxy() {
		luaState.newTable();
		luaState.getProxy(1, new Class<?>[] { null });
	}

	/**
	 * getProxy(int) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetProxy1() {
		luaState.getProxy(getIllegalIndex());
	}

	/**
	 * getProxy(int, Class<?>) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetProxy2() {
		luaState.getProxy(getIllegalIndex(), Runnable.class);
	}

	/**
	 * getProxy(int, Class<?>) with illegal table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetProxy3() {
		luaState.pushNumber(0.0);
		luaState.getProxy(1, Runnable.class);
	}

	/**
	 * getProxy(int, Class<?>[]) with illegal index.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetProxy4() {
		luaState.getProxy(getIllegalIndex(), new Class<?>[] { Runnable.class });
	}

	/**
	 * getProxy(int, Class<?>[]) with illegal table.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetProxy5() {
		luaState.pushNumber(0.0);
		luaState.getProxy(1, new Class<?>[] { Runnable.class });
	}

	// -- Private methods
	/**
	 * Returns an illegal index.
	 */
	private int getIllegalIndex() {
		return luaState.getTop() + 1;
	}
}
