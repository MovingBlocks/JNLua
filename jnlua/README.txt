HISTORY for JNLua

* Release 5.2.0

- Adapted to Lua 5.2. The version schema of JNLua now follows Lua versions.

- Added a compare method to LuaState, and deprecated the equal and lessThan methods.

- Added support for the bit library in both the openLib and openLibs methods of
LuaState.

- Added support for additional garbage collection actions, allowing to query
whether the collector is running and choosing the collector mode.

- Added the exception classes LuaGcMetamethodException and
LuaMessageHandlerException, indicating errors running the __gc metamethod
during garbage collection and errors running the message handler function in a
protected call.

- Removed the setFEnv and getFEnv methods from LuaState.

- Removed the checkBoolean methods from LuaState. (They should not have been
added in the first place.)

- Changed the input stream based load method in LuaState to accept an
additional mode argument. In addition, the source (aka chunkname) argument is
no longer auto-prefixed with an equal sign.

- Made the behavior of library open methods more consistent. The openLib method
of LuaState is now properly documented to leave the opened library on the
stack; the openLibs method of LuaState now removes libraries that are pushed
onto the stack; the open method of JavaModule now leaves the Java module on
the stack.

- Changed the behavior of the register method in LuaState to follow that of
luaL_requiref, no longer specifically supporting dots in module names.


TODO:

- __pairs, __ipairs support as metamethods, see Java Reflector and Java Module
- metamethods as free strings
- call check methods directly


* Release 0.9.1 Beta (2010-04-05)

- Added NativeSupport for more explicit control over the native library
loading process.

- Migrated build system to Maven.
 

* Release 0.9.0 Beta (2008-10-27)
  
- Initial public release.
