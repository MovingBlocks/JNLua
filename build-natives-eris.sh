#!/bin/bash
mkdir native-build
mkdir native-debug
rm native-build/*.so native-build/*.dll
rm native-debug/*.so native-debug/*.dll
cd native

for lua_ver in 5.2 5.3; do
for arch_type in i686 amd64; do
for plat_type in windows linux; do

MY_GCC="gcc"
MY_STRIP="strip"
MY_SUFFIX="so"

if [ "$arch_type" == "i686" ]; then
	MINGW_GCC="i686-w64-mingw32-gcc"
	MINGW_STRIP="i686-w64-mingw32-strip"
	MY_CFLAGS="-fPIC -O2 -m32"
	MY_LDFLAGS="-m32"
else
	MINGW_GCC="x86_64-w64-mingw32-gcc"
	MINGW_STRIP="x86_64-w64-mingw32-strip"
	MY_CFLAGS="-fPIC -O2 -m64"
	MY_LDFLAGS="-m64"
fi

LUA_TYPE="posix"

if [ "$plat_type" = "windows" ]; then
	LUA_TYPE="mingw"
	MY_GCC="$MINGW_GCC"
	MY_STRIP="$MINGW_STRIP"
	MY_SUFFIX="dll"
fi

cd ../../eris

if [ "$lua_ver" == "5.2" ]; then
	git checkout master
else
	git checkout master-lua5.3
fi

make clean
make CC="$MY_GCC" CFLAGS="$MY_CFLAGS" LDFLAGS="$MY_LDFLAGS" $LUA_TYPE

cd ../JNLua/native
rm *.dll *.so build/*.o

CFLAGS="$MY_CFLAGS -DJNLUA_USE_ERIS -DLUA_USE_POSIX" LDFLAGS="$MY_LDFLAGS" ARCH=amd64 JNLUA_SUFFIX="" \
  LUA_LIB_NAME=lua LUA_INC_DIR=../../eris/src LUA_LIB_DIR=../../eris/src LIB_SUFFIX="$MY_SUFFIX" \
  CC="$MY_GCC" \
  make -f Makefile.linux libjnlua

cp libjnlua."$MY_SUFFIX" ../native-debug/libjnlua-"$lua_ver"-"$plat_type"-"$arch_type"."$MY_SUFFIX"
"$MY_STRIP" libjnlua."$MY_SUFFIX"
mv libjnlua."$MY_SUFFIX" ../native-build/libjnlua-"$lua_ver"-"$plat_type"-"$arch_type"."$MY_SUFFIX"

done
done
done
