HISTORY for JNLua

* Release 1.0.0

- Adapted to Lua 5.2.

- Added a compare method to LuaState, and deprecated the equal and lessThan methods.

- Added rawLen and len methods to LuaState, and deprecated the length method.

- Added an absIndex method to LuaState.

- Added an arith method to LuaState.

- Added a copy method to LuaState.

- Added support for the bit32 library in both the openLib and openLibs methods
of LuaState.

- Added support for additional garbage collection actions, allowing to query
whether the collector is running and choosing the collector mode.

- Added the exception classes LuaGcMetamethodException and
LuaMessageHandlerException, indicating errors running the __gc metamethod
during garbage collection and errors running the message handler function in a
protected call.

- Added support for the __pairs and __ipairs metamethods on Java objects. The
behavior is equivalent to the pairs and ipairs functions provided by the Java
module.

- Removed the checkBoolean methods from LuaState.

- Removed the setFEnv and getFEnv methods from LuaState.

- Changed the input stream based load method in LuaState to accept an
additional mode argument. In addition, the source (aka chunkname) argument is
no longer auto-prefixed with an equals sign.

- Made the behavior of library open methods more consistent. The openLib method
of LuaState is now properly documented to leave the opened library on the
stack; the openLibs method of LuaState now removes libraries that are pushed
onto the stack; the open method of JavaModule now leaves the Java module on
the stack.

- Changed the behavior of the register method in LuaState to follow that of
luaL_requiref, no longer specifically supporting dots in module names. An
optional boolean argument now controls whether a global variable with the
module name is created.


* Release 0.9.1 Beta (2010-04-05)

- Added NativeSupport for more explicit control over the native library
loading process.

- Migrated build system to Maven.
 

* Release 0.9.0 Beta (2008-10-27)
  
- Initial public release.
