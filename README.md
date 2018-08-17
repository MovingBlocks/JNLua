# JNLua (Terasology version)

JNLua (Java Native Lua) is a bridge between the native Lua virtual machine and Java's, utilizing JNI to communicate between the C Lua code and JVM 
code.

## Features

From [the original website](https://code.google.com/p/jnlua/):

- **Full Lua support with full Java type-safety.** JNLua provides the full functionality of Lua C API including large parts of the Lua Auxiliary 
Library. All Lua Standard Libraries are supported, including the coroutine functions. At the same time, JNLua maintains the type-safety of the Java 
VM by performing rigorous checks in its native library.
- **Two-way integration.** With JNLua, you can access Java from Lua and Lua from Java. From Lua, JNLua provides full Java object access with 
intuitive syntax and the abilitiy to implement Java interfaces in Lua. From Java, JNLua provides full Lua access including the ability to implement 
Lua functions in Java. The integration works transparently in both directions and on each end conforms to the common principles of the respective 
platform.
- **Dual bootstrapping.** JNLua can be started from both the Java and the Lua side. If started from the Java side, a Lua state is attached to the 
calling Java virtual machine; if started from the Lua side, a Java virtual machine is attached to the calling Lua process.
- **Extensive language bindings.** The bindings between Lua and Java are abstracted into the domains of *Java reflection* and *conversion*. The 
*default Java reflector* supports field, method and property access on Java classes and objects. For overloaded methods, it provides a dispatch 
logic that mimics the behavior described in Java Language Specification. The *default converter* handles the bidirectional conversion of primitive 
types, such as numbers and strings. For complex types, it supports the bidirectional mapping of Lua tables to Java maps, lists and arrays. These 
mappings are generally implemented with proxy objects, that is, they work by *reference*. Both the Java reflector and converter can be specialized 
to fit custom needs.
- **Java module.** The JNLua Java module provides a small but comprehensive set of Lua functions providing Java language support for Lua.
- **Java VM module.** The Java VM module is a Lua module written in C that allows a Lua process to create a Java Virtual Machine and run Java code 
in that machine.
- **Transparent error handling.** Java does error handling by exceptions; Lua uses mechanics such as `error()` and `pcall()`. JNLua ensures a 
seamless translation of error conditions between the two domains. Lua errors are reported as exceptions to Java. Java exceptions generate errors on 
the Lua side.
- **JSR 223: Scripting for the Java Platform provider.** JNLua includes a provider that conforms to the [JSR 223: Scripting for the Java 
Platform](http://www.jcp.org/en/jsr/detail?id=223) specification. This allows the use of Lua as a scripting language for Java in a standardized 
way. The JSR 223 provider also supports the optional Compilable and Invocable interfaces.
- **JNLua Console.** A simple console implemented in Java for experimenting with JNLua. 

Additionally, the Terasology version provides the following:

- **Support for [Eris](https://github.com/fnuecke/eris)**, a modified version of the Lua virtual machine with support for state serialization.
- **Memory usage limiting** for additional sandboxing
- **Simultaneous Lua 5.2 and Lua 5.3 support**

## Building

To build the Java side of JNLua, a simple "./gradlew build" is sufficient.

Building the natives is more involved. Generally, JNLua expects natives to be present with the following filenames:

libjnlua-[5.2,5.3]-[windows,linux]-[i686,amd64].[dll,so]

To build the natives on Linux you need:

* a reasonably modern Linux distribution (tested with Debian Stretch),
* MinGW-w64 (apt-get install mingw-w64),
* 32-bit and 64-bit build environments (apt-get install build-essential gcc-multilib libc6-dev:i386)

Then simply run `./build-natives-eris.sh` and with a little luck you now have shiny new natives compiled for Linux (and Windows).

Note that during the script execution [Eris](https://github.com/fnuecke/eris) will be cloned into a subdir `eris`.

On Mac you need general development tools and `JAVA_HOME` set, to for instance `/Library/Java/JavaVirtualMachines/jdk-10.0.1.jdk/Contents/Home`

During initial work on the natives script on Mac that was the only edit needed. Unsure if any additional steps are needed, some were already done.

Run `./build-natives-eris_mac.sh` and the Mac OS specific natives should now be yours as well.

In both cases for the unit tests to work the resulting files must be copied into a `natives` subdir, which Gradle puts on the Java library path.

For instance on Mac make sure the directory exists then simply run `cp native-build/* natives` and a `./gradlew test` should pass all the tests.

Our official build publishes both the JNLua jar and natives to the [MovingBlocks Artifactory](http://artifactory.terasology.org/artifactory/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/org/terasology/jnlua) via `publishNatives.gradle` and some Jenkins magic.

Mac cloud builds are tricky so we're using a [special snowflake "build server" (namely Cervator's Macbook)](http://jenkins.terasology.org/computer/MacBuilder/) that'll be connected manually when needed.

## License

JNLua is licensed under the MIT license which at the time of this writing is the same license as the one of Lua. 
