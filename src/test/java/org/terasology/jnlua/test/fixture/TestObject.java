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

package org.terasology.jnlua.test.fixture;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A test object for reflection testing.
 */
public class TestObject implements Comparable<TestObject> {
	// -- Static
	/**
	 * A public static field.
	 */
	public static String TEST_FIELD = "test";

	/*
	 * Public test fields for all types.
	 */
	public boolean booleanField;
	public byte byteField;
	public byte[] byteArrayField;
	public short shortField;
	public int intField;
	public long longField;
	public float floatField;
	public double doubleField;
	public BigInteger bigIntegerField;
	public BigDecimal bigDecimalField;
	public char charField;
	public String stringField;

	/**
	 * A private field.
	 */
	private String foo;

	/**
	 * The value of this object.
	 */
	private int value;

	// -- Static methods
	/**
	 * A public static method.
	 */
	public static String testStatic() {
		return "test";
	}

	// -- State
	/**
	 * A public field.
	 */
	public String testField = "test";

	// -- Construction
	/**
	 * Creates a new instance.
	 */
	public TestObject() {
	}

	/**
	 * Creates a new instance.
	 */
	public TestObject(int value) {
		this.value = value;
	}

	// -- Methods
	/**
	 * A public method.
	 */
	public String test() {
		return "test";
	}

	// -- Properties
	/**
	 * A property reader.
	 */
	public String getFoo() {
		return foo;
	}

	/**
	 * A property writer.
	 */
	public void setFoo(String foo) {
		this.foo = foo;
	}

	/**
	 * Returns the value of this object.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets the value of this object.
	 */
	public void setValue(int value) {
		this.value = value;
	}

	// -- Overloaded methods
	/**
	 * Overloaded method dispatch test method.
	 */
	public String overloadedSub(TestObject testObject) {
		return "super";
	}

	/**
	 * Overloaded method dispatch test method.
	 */
	public String overloadedSub(Sub sub) {
		return "sub";
	}

	/**
	 * Overloaded method dispatch test method.
	 */
	public String overloadedSibling(B b) {
		return "b";
	}

	/**
	 * Overloaded method dispatch test method.
	 */
	public String overloadedSibling(C c) {
		return "c";
	}

	/**
	 * Overloaded method dispatch test method.
	 */
	public String overloadedParentChild(A a) {
		return "a";
	}

	/**
	 * Overloaded method dispatch test method.
	 */
	public String overloadedParentChild(B c) {
		return "b";
	}

	// -- Object methods
	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TestObject)) {
			return false;
		}
		TestObject other = (TestObject) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	// -- Comparable methods
	@Override
	public int compareTo(TestObject o) {
		if (value < o.value) {
			return -1;
		}
		if (value > o.value) {
			return 1;
		}
		return 0;
	}

	// -- Nested classes
	/**
	 * Subclass for method dispatch testing.
	 */
	public static class Sub extends TestObject {
	}

	/**
	 * Class for method dispatch testing.
	 */
	public static class AB implements A, B {
	}

	/**
	 * Class for method dispatch testing.
	 */
	public static class AC implements A, C {
	}

	/**
	 * Class for method dispatch testing.
	 */
	public static class BC implements B, C {
	}

	// -- Nested interfaces
	/**
	 * Overloaded method dispatch test interface.
	 */
	public interface A {
	}

	/**
	 * Overloaded method dispatch test interface.
	 */
	public interface B extends A {
	}

	/**
	 * Overloaded method dispatch test interface.
	 */
	public interface C extends A {
	}
}
