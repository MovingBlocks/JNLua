HISTORY for JNLua

* Release 1.0.1

- Javadoc corrections.

- Corrected an issue where the native library would pass an invalid handle to
the ReleaseStringUTFChars function.


* Release 1.0.0 (2012-01-05)

- Adapted to Lua 5.2.

- Added the RIDX_MAINTHREAD and RIDX_GLOBALS constants to LuaState.

- Added a compare method to LuaState, and deprecated the equal and lessThan methods.

- Added a rawLen method to LuaState, and deprecated the length method.

- Added an absIndex method to LuaState.

- Added an arith method to LuaState.

- Added a copy method to LuaState.

- Added a len method to LuaState.

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

- Removed the GLOBALSINDEX and ENVIRONINDEX constants from LuaState. You can
use the getGlobal and setGlobal methods, or call rawget(REGISTRYINDEX,
RIDX_GLOBALS) to push the global environment onto the stack.

- Removed the checkBoolean methods from LuaState. You can use the toBoolean
method to evaluate any value in a boolean context. Also changed the toBoolean
method to accept non-valid stack indexes.

- Removed the setFEnv and getFEnv methods from LuaState.

- Changed the input stream based load method in LuaState to accept an
additional mode argument. Also, the source (aka chunkname) argument is
no longer auto-prefixed with an equals sign.

- Changed the behavior of the library opening methods. The openLib method
in LuaState and the open method in JavaModule now leave the loaded module on
the stack.

- Changed the behavior of the register method in LuaState to follow that of
luaL_requiref, no longer specifically supporting dots in module names. An
optional boolean argument now controls whether a global variable with the
module name is created.

- Changed the argument checking methods in LuaState to call the Lua standard
implementations, reducing the number of native transitions and taking advantage
of tonumberx and tointegerx improvements. Also changed the checkOption
methods to return an integer index instead of a string and added checkEnum
methods suitable for use with Java enums.

- Corrected an issue where the type method in LuaState would not return null
for non-valid stack indexes.

- Corrected an issue where the default converter would not properly handle
non-valid stack indexes.

- Corrected an issue where the setJavaReflector method in LuaState would allow
a null value to be set.


* Release 0.9.1 Beta (2010-04-05)

- Added NativeSupport for more explicit control over the native library
loading process.

- Migrated build system to Maven.
 

* Release 0.9.0 Beta (2008-10-27)
  
- Initial public release.
