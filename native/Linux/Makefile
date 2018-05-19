# Paths
JDK_DIR=/usr/lib/jvm/openjdk
LUA_LIB_DIR=/usr/lib
LUA_VERSION=5.2
LUA_INC_DIR=/usr/include/lua$(LUA_VERSION)
ARCH=amd64

# Tools
CC=gcc
LD=gcc

# Default arguments
CFLAGS=-c -fno-strict-aliasing -fPIC -O2 -Wall -DNDEBUG -D_REENTRANT -DLUA_USE_LINUX
LDFLAGS=-shared

# Description blocks
all: libjnlua$(LUA_VERSION).so javavm.so

libjnlua$(LUA_VERSION).so: jnlua.o
	$(LD) $(LDFLAGS) -Wl,-soname=libjnlua$(LUA_VERSION).so -olibjnlua$(LUA_VERSION).so -L$(LUA_LIB_DIR) jnlua.o -lc -llua$(LUA_VERSION)

jnlua.o: ../jnlua.c
	$(CC) $(CFLAGS) -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux -I$(LUA_INC_DIR) ../jnlua.c

javavm.so: javavm.o
	$(LD) $(LDFLAGS) -Wl,-soname=javavm.so -ojavavm.so -L$(LUA_LIB_DIR) -L$(JDK_DIR)/jre/lib/$(ARCH)/server javavm.o -llua$(LUA_VERSION) -ljvm

javavm.o: ../javavm.c ../javavm.h
	$(CC) $(CFLAGS) -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux -I$(LUA_INC_DIR) ../javavm.c

clean:
	-rm libjnlua$(LUA_VERSION).so jnlua.o
	-rm javavm.so javavm.o
