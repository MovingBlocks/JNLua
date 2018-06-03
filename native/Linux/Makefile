# Paths
JDK_DIR=/usr/lib/jvm/openjdk
LUA_LIB_DIR=/usr/lib
LUA_VERSION=5.3
JNLUA_SUFFIX=53
LUA_INC_DIR=/usr/include/lua$(LUA_VERSION)
ARCH=amd64

# Tools
CC=gcc
LD=gcc

# Default arguments
CFLAGS=-c -fno-strict-aliasing -fPIC -O2 -Wall -DNDEBUG -D_REENTRANT -DLUA_USE_LINUX -DJNLUA_SUFFIX=$(JNLUA_SUFFIX)
LDFLAGS=-shared

# Description blocks
all: libjnlua$(JNLUA_SUFFIX).so javavm.so

libjnlua$(JNLUA_SUFFIX).so: jnlua.o
	$(LD) $(LDFLAGS) -Wl,-soname=libjnlua$(JNLUA_SUFFIX).so -olibjnlua$(JNLUA_SUFFIX).so -L$(LUA_LIB_DIR) jnlua.o -lc -llua$(LUA_VERSION)

jnlua.o: ../jnlua.c
	$(CC) $(CFLAGS) -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux -I$(LUA_INC_DIR) ../jnlua.c

javavm.so: javavm.o
	$(LD) $(LDFLAGS) -Wl,-soname=javavm.so -ojavavm.so -L$(LUA_LIB_DIR) -L$(JDK_DIR)/jre/lib/$(ARCH)/server javavm.o -llua$(LUA_VERSION) -ljvm

javavm.o: ../javavm.c ../javavm.h
	$(CC) $(CFLAGS) -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux -I$(LUA_INC_DIR) ../javavm.c

clean:
	-rm libjnlua$(JNLUA_SUFFIX).so jnlua.o
	-rm javavm.so javavm.o
