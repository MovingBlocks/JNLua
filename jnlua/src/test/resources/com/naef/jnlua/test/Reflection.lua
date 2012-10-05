--[[
$Id$
See LICENSE.txt for license terms.
]]

module(..., package.seeall)

-- General reflection test
function testReflection ()
	-- Static field
	local TestObject = java.require("com.naef.jnlua.test.fixture.TestObject")
	assert(TestObject.TEST_FIELD == "test")
	TestObject.TEST_FIELD = ""
	assert(TestObject.TEST_FIELD == "")
	TestObject.TEST_FIELD = "test"
	
	-- Static method
	assert(TestObject:testStatic() == "test")
	
	-- Field
	local testObject = TestObject:new()
	assert(testObject.testField == "test")
	testObject.testField = ""
	assert(testObject.testField == "")
		
	-- Method
	assert(testObject:test() == "test")
	
	-- Property
	assert(testObject.foo == nil)
	testObject.foo = "bar"
	assert(testObject.foo == "bar")
end

-- Type test
function testTypes ()
	-- Create
	local TestObject = java.require("com.naef.jnlua.test.fixture.TestObject")
	local testObject = TestObject:new()
	
	-- Test
	testObject.booleanField = true
	assert(testObject.booleanField)
	testObject.byteField = 1
	assert(testObject.byteField == 1)
	testObject.byteArrayField = "test"
	assert(testObject.byteArrayField == "test")
	testObject.shortField = 1
	assert(testObject.shortField == 1)
	testObject.intField = 1
	assert(testObject.intField == 1)
	testObject.longField = 1
	assert(testObject.longField == 1)
	testObject.floatField = 1
	assert(testObject.floatField == 1)
	testObject.doubleField = 1
	assert(testObject.doubleField == 1)
	testObject.bigIntegerField = 1
	assert(testObject.bigIntegerField == 1)
	testObject.bigDecimalField = 1
	assert(testObject.bigDecimalField == 1)
	testObject.charField = 1
	assert(testObject.charField == 1)
	testObject.stringField = "test"
	assert(testObject.stringField == "test")
end

-- Meta test
function testMeta ()
	-- Create
	local TestObject = java.require("com.naef.jnlua.test.fixture.TestObject")
	local testObject1 = TestObject:new(1)
	local testObject2 = TestObject:new(2)
	
	-- __index
	local int = java.require("int")
	local intArray = java.new(int, 2)
	assert(intArray[1] == 0)
	assert(intArray[2] == 0)
		
	-- __newindex
	intArray[1] = 1
	assert(intArray[1] == 1)
	
	-- __len
	assert(#intArray == 2)
	
	-- __eq
	assert(testObject1 ~= testObject2)
	testObject2.value = 1
	assert(testObject1 == testObject2)
	testObject2.value = 2
	
	-- __lt, __le
	assert(testObject1 < testObject2)
	assert(testObject2 <= testObject2)
	testObject1.value = 2
	testObject2.value = 1
	assert(testObject1 > testObject2)
	assert(testObject2 >= testObject2)
	testObject1.value = 1
	testObject2.value = 2
	
	-- __tostring
	assert(tostring(testObject1) == "1")
	assert(tostring(testObject2) == "2")
	
	-- __pairs
	local HashMap = java.require("java.util.HashMap")
	local hashMap = HashMap:new()
	hashMap:put("k", "v")
	local cnt = 0
	for k, v in pairs(hashMap) do
		if k == "k" and v == "v" then cnt = cnt + 1 end
	end
	assert(cnt == 1)
	
	-- ipairs
	cnt = 0
	for i, j in ipairs(intArray) do
		cnt = cnt + 1
	end
	assert(cnt == 2)
end

-- Overloaded method dispatch test
function testMethodDispatch ()
	-- Subclass test
	local TestObject = java.require("com.naef.jnlua.test.fixture.TestObject")
	local testObject = TestObject:new()
	local Sub = java.require("com.naef.jnlua.test.fixture.TestObject$Sub")
	local sub = Sub:new()
	assert(testObject:overloadedSub(testObject) == "super")
	assert(testObject:overloadedSub(sub) == "sub")
	
	-- Subinterface test
	local AB = java.require("com.naef.jnlua.test.fixture.TestObject$AB")
	local ab = AB:new()
	local AC = java.require("com.naef.jnlua.test.fixture.TestObject$AC")
	local ac = AC:new()
	local BC = java.require("com.naef.jnlua.test.fixture.TestObject$BC")
	local bc = BC:new()
	assert(testObject:overloadedSibling(ab) == "b")
	assert(testObject:overloadedSibling(ac) == "c")
	local status, msg = pcall(testObject.overloadedSibling, testObject, bc)
	assert(not status)
	assert(string.find(tostring(msg), "ambivalent"))
	assert(testObject:overloadedParentChild(ab) == "b")
	assert(testObject:overloadedParentChild(ac) == "a")
	assert(testObject:overloadedParentChild(bc) == "b")
	assert(testObject:overloadedParentChild(bc) == "b")
end

-- VarArgs method test
function testVarargs ()
	local String = java.require("java.lang.String")
	assert(String:format("%s%.0f", "test", 1) == "test1")
	assert(true)
end
