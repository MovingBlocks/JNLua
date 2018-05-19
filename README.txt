README for JNlua

For information about JNLua, please visit http://code.google.com/p/jnlua/.


* Release 1.0.5

- Corrected an issue where an array access error message would fail to format.


* Release 1.0.4 (2013-07-28)

- Corrected an issue where method dispatch would enter an endless loop on
non-public superclasses.


* Release 1.0.3 (2012-10-09)

- Added transparent conversion between Lua strings and Java byte arrays.
This may break existing code that passes byte arrays between Java and Lua.
A compatibility property, com.naef.jnlua.rawByteArray=true, has been provided.

- Fixed an issue where method dispatch would incorrectly fail on public
methods found on non-public classes with public superclasses. Thanks
Ignazio Di Napoli for the analysis.

 
* Release 1.0.2 (2012-01-29)

- Added toIntegerX and toNumberX methods to LuaState.

- Added a 'new' method to interfaces in the default Java reflector. The method
accepts a table providing the methods of the interface and returns a proxy
that implements the interface.

- Changed the absIndex method in LuaState to accept non-valid indexes.

- Changed the rawEqual method to return false on non-valid indexes.

- Changed the setMetatable method in LuaState to no longer return a value,
adapting to the Lua 5.2 API.

- Improved the diagnostics in the javavm module. In case of JNI errors, the
Java exception string is now included in the error message.

- Refactored the error handling in the JNLua native library.


* Release 1.0.1 (2012-01-12)

- Added a javavm module, allowing to create a Java VM from Lua.

- Javadoc corrections.

- Corrected an issue where the native library would pass an invalid handle to
the ReleaseStringUTFChars function.

- Corrected an issue where the native library would not properly catch Lua
errors, leading to uncontrolled transitions between Java code and native code.

- Corrected an issue where the native library would exit incorrectly from the
lua_tojavafunction function.


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
