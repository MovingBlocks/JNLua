#!/bin/bash
mkdir native-build
mkdir native-debug
rm native-build/*.dylib
rm native-debug/*.dylib

if [ ! -d eris ]; then
	git clone https://github.com/fnuecke/eris
fi

cd native

for lua_ver in 5.2 5.3; do
for arch_type in amd64 aarch64; do
for plat_type in mac; do

MY_GCC="gcc"
MY_STRIP="strip"
MY_LIB_SUFFIX="dylib"

if [ "$arch_type" == "i686" ]; then
	MY_CFLAGS="-fPIC -O2 -m32"
	MY_LDFLAGS="-m32 -dynamic"
else
	MY_CFLAGS="-fPIC -O2 -m64"
	MY_LDFLAGS="-m64 -dynamic"
fi

LUA_TYPE="posix"

cd ../eris

if [ "$lua_ver" == "5.2" ]; then
	MY_JNLUA_SUFFIX="52"
	MY_LUA_CFLAGS=""
	git checkout master
else
	MY_JNLUA_SUFFIX="53"
	MY_LUA_CFLAGS="-DLUA_COMPAT_5_2"
	git checkout master-lua5.3
fi

make clean
make CC="$MY_GCC" CFLAGS="$MY_CFLAGS $MY_LUA_CFLAGS" LDFLAGS="$MY_LDFLAGS" $LUA_TYPE

cd ../native
rm *.dll *.so build/*.o

CFLAGS="$MY_CFLAGS -DJNLUA_USE_ERIS -DLUA_USE_POSIX" LDFLAGS="$MY_LDFLAGS" ARCH="$arch_type" JNLUA_SUFFIX="$MY_JNLUA_SUFFIX" \
  LUA_LIB_NAME=lua LUA_INC_DIR=../eris/src LUA_LIB_DIR=../eris/src LIB_SUFFIX="$MY_LIB_SUFFIX" \
  CC="$MY_GCC" LUA_VERSION="$lua_ver" \
  make -f Makefile.mac libjnlua

cp libjnlua"$MY_JNLUA_SUFFIX"."$MY_LIB_SUFFIX" ../native-debug/libjnlua-"$lua_ver"-"$plat_type"-"$arch_type"."$MY_LIB_SUFFIX"
"$MY_STRIP" libjnlua"$MY_JNLUA_SUFFIX"."$MY_LIB_SUFFIX"
mv libjnlua"$MY_JNLUA_SUFFIX"."$MY_LIB_SUFFIX" ../native-build/libjnlua-"$lua_ver"-"$plat_type"-"$arch_type"."$MY_LIB_SUFFIX"

done
done
done