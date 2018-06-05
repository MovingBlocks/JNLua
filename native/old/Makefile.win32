# Paths
JDK_DIR=C:\Program Files\Java\jdk1.6.0_19
LUA_DIR=C:\Users\Public\Apps\Lua\5.2\lib64
LUA_VERSION=52
VERSION=52

# Tools
CC=cl
LD=link
MT=mt

# Default arguments
CFLAGS=/nologo /c /O2 /GL /W3 /DNDEBUG
LDFLAGS=/nologo /DLL /LTCG
MTFLAGS=-nologo

# Description blocks
all: jnlua$(VERSION).dll javavm.dll

jnlua$(VERSION).dll: jnlua.obj
	$(LD) $(LDFLAGS) /OUT:jnlua$(VERSION).dll /LIBPATH:"$(LUA_DIR)" jnlua.obj lua$(LUA_VERSION).lib

jnlua.obj: ..\jnlua.c
	$(CC) $(CFLAGS) /MD /DLUA_BUILD_AS_DLL /I"$(JDK_DIR)\include" /I"$(JDK_DIR)\include\win32" /I"$(LUA_DIR)\include" ..\jnlua.c

javavm.dll: javavm.obj
	$(LD) $(LDFLAGS) /OUT:javavm.dll /LIBPATH:"$(LUA_DIR)" /LIBPATH:"$(JDK_DIR)\lib" javavm.obj lua$(LUA_VERSION).lib jvm.lib

javavm.obj: ..\javavm.c ..\javavm.h
	$(CC) $(CFLAGS) /MD /DLUA_BUILD_AS_DLL /DLUA_LIB /I"$(JDK_DIR)\include" /I"$(JDK_DIR)\include\win32" /I"$(LUA_DIR)\include" ..\javavm.c

clean:
	-del jnlua$(VERSION).dll jnlua$(VERSION).dll.manifest jnlua$(VERSION).exp jnlua$(VERSION).lib jnlua.obj
	-del javavm.dll javavm.dll.manifest javavm.exp javavm.lib javavm.obj
