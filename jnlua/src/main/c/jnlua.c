/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

#include <stdlib.h>
#include <string.h>
#include <setjmp.h>
#include <jni.h>
#include <lua.h>
#include <lauxlib.h>
#include <lualib.h>

/* Include uintptr_t */
#ifdef LUA_WIN
#include <stddef.h>
#endif
#ifdef LUA_USE_POSIX
#include <stdint.h>
#endif

/* ---- Definitions ---- */
#define JNLUA_WEAKREF 0
#define JNLUA_HARDREF 1
#define JNLUA_APIVERSION 3
#define JNLUA_MOBJECT "com.naef.jnlua.Object"
#define JNLUA_RENV "com.naef.jnlua.Env"
#define JNLUA_RJAVASTATE "com.naef.jnlua.JavaState"
#define JNLUA_JNIVERSION JNI_VERSION_1_6

/* ---- Types ---- */
/* Structure for reading and writing Java streams. */
typedef struct StreamStruct  {
	JNIEnv *env;
	jobject stream;
	jbyteArray byteArray;
	jbyte* bytes;
	jboolean isCopy;
} Stream;

/* ---- JNI helpers ---- */
static jclass referenceClass(JNIEnv *env, const char *className);
static jobject newGlobalRef(JNIEnv *env, lua_State *L, jobject obj, int type);
static jbyteArray newByteArray(JNIEnv *env, lua_State *L, jsize length);
static const char *getStringUtfChars(JNIEnv *env, lua_State *L, jstring string);

/* ---- Lua helpers ---- */
static void checkstack(lua_State *L, int space, const char *msg);

/* ---- Java state operations ---- */
static lua_State *getLuaState(JNIEnv *env, jobject obj);
static void setLuaState(JNIEnv *env, jobject obj, lua_State *L);
static lua_State *getLuaThread(JNIEnv *env, jobject obj);
static void setLuaThread(JNIEnv *env, jobject obj, lua_State *L);

/* ---- Lua state operations ---- */
static JNIEnv *getJniEnv(lua_State *L);
static void setJniEnv(lua_State* L, JNIEnv *env);
static jobject getJavaState(lua_State *L);
static void setJavaState(lua_State *L, jobject javaState);

/* ---- Checks ---- */
static int validindex(lua_State *L, int index);
static void checkindex(JNIEnv *env, lua_State *L, int index);
static void checkrealindex(JNIEnv *env, lua_State *L, int index);
static void checktype(JNIEnv *env, lua_State *L, int index, int type);
static void checknelems(JNIEnv *env, lua_State *L, int n);
static void checknotnull (JNIEnv *env, lua_State *L, void *object);
static void checkarg(JNIEnv *env, lua_State *L, int cond, const char *msg);
static void checkstate(JNIEnv *env, lua_State *L, int cond, const char *msg);
static void check(JNIEnv *env, lua_State *L, int cond, jthrowable throwableClass, const char *msg);
static void throw(JNIEnv *env, lua_State *L, jthrowable throwableClass, const char *msg);

/* ---- Java object helpers ---- */
static void pushJavaObject(JNIEnv *env, lua_State *L, jobject object);
static jobject getJavaObject(JNIEnv *env, lua_State *L, int index, jclass class);
static jstring toString(JNIEnv *env, lua_State *L, int index);

/* ---- Metamethods ---- */
static int gcJavaObject(lua_State *L);
static int callJavaFunction(lua_State *L);

/* ---- Errror handling ---- */
static int handleError(lua_State *L);
static int processActivationRecord(lua_Debug *ar);
static void throwException(JNIEnv *env, lua_State *L, int status);

/* ---- Stream adapters ---- */
static const char *readInputStream(lua_State *L, void *ud, size_t *size);
static int writeOutputStream(lua_State *L, const void *data, size_t size, void *ud);

/* ---- Variables ---- */
static jclass luaStateClass = NULL;
static jfieldID luaStateId = 0;
static jfieldID luaThreadId = 0;
static jfieldID yieldId = 0;
static jclass javaFunctionInterface = NULL;
static jmethodID invokeId = 0;
static jclass luaRuntimeExceptionClass = NULL;
static jmethodID luaRuntimeExceptionInitId = 0;
static jmethodID setLuaErrorId = 0;
static jclass luaSyntaxExceptionClass = NULL;
static jmethodID luaSyntaxExceptionInitId = 0;
static jclass luaMemoryAllocationExceptionClass = NULL;
static jmethodID luaMemoryAllocationExceptionInitId = 0;
static jclass luaGcMetamethodExceptionClass = NULL;
static jmethodID luaGcMetamethodExceptionInitId = 0;
static jclass luaMessageHandlerExceptionClass = NULL;
static jmethodID luaMessageHandlerExceptionInitId = 0;
static jclass luaStackTraceElementClass = NULL;
static jmethodID luaStackTraceElementInitId = 0;
static jclass luaErrorClass = NULL;
static jmethodID luaErrorInitId = 0;
static jmethodID setLuaStackTraceId = 0;
static jclass throwableClass = NULL;
static jmethodID getMessageId = 0;
static jclass nullPointerExceptionClass = NULL;
static jclass illegalArgumentExceptionClass = NULL;
static jclass illegalStateExceptionClass = NULL;
static jclass inputStreamClass = NULL;
static jmethodID readId = 0;
static jclass outputStreamClass = NULL;
static jmethodID writeId = 0;
static jclass ioExceptionClass = NULL;
static jclass enumClass = NULL;
static jmethodID nameId = 0;
static int initialized = 0;

/* ---- Error handling ---- */
/*
 * JNI does not allow uncontrolled transitions such as jongjmp between Java
 * code and native code, but Lua uses longjmp for error handling. The follwing
 * section replicates logic from luaD_rawrunprotected that is internal to
 * Lua. Contact me if you know of a more elegant solution ;)
 */

struct lua_longjmp {
	struct lua_longjmp *previous;
	jmp_buf b;
	volatile int status;
};

struct lua_State {
	void *next;
	unsigned char tt;
	unsigned char marked;
	unsigned char status;
	void *top;
	void *l_G;
	void *ci;
	void *oldpc;
	void *stack_last;
	void *stack;
	int stacksize;
	unsigned short nny;
	unsigned short nCcalls;  
	unsigned char hookmask;
	unsigned char allowhook;
	int basehookcount;
	int hookcount;
	lua_Hook hook;
	void *openupval;
	void *gclist;
	struct lua_longjmp *errorJmp;  
};

#define JNLUA_TRY {\
	unsigned short oldnCcalls = L->nCcalls;\
	struct lua_longjmp lj;\
	lj.status = LUA_OK;\
	lj.previous = L->errorJmp;\
	L->errorJmp = &lj;\
	if (setjmp(lj.b) == 0) {\
		checkstack(L, LUA_MINSTACK, NULL);\
		setJniEnv(L, env);
#define JNLUA_END }\
	L->errorJmp = lj.previous;\
	L->nCcalls = oldnCcalls;\
	if (lj.status != LUA_OK) {\
		throwException(env, L, lj.status);\
	}\
}
#define JNLUA_THROW(status) lj.status = status;\
	longjmp(lj.b, -1)

/* ---- Fields ---- */
/* lua_registryindex() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1registryindex(JNIEnv *env, jobject obj) {
	return (jint) LUA_REGISTRYINDEX;
}

/* lua_version() */
JNIEXPORT jstring JNICALL Java_com_naef_jnlua_LuaState_lua_1version(JNIEnv *env, jobject obj) {
	const char *luaVersion;
	
	luaVersion = LUA_VERSION;
	if (strncmp(luaVersion, "Lua ", 4) == 0) {
		luaVersion += 4;
	}
	return (*env)->NewStringUTF(env, luaVersion); 
}

/* ---- Life cycle ---- */
/*
 * lua_newstate()
 * The function is not reentrant. Non-reentrant use is ensured on the Java side.
 */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1newstate (JNIEnv *env, jobject obj, int apiversion, jlong existing) {
	lua_State *L;
	int success = 0;
	
	/* Initialized? */
	if (!initialized) {
		return;
	}
	
	/* API version? */
	if (apiversion != JNLUA_APIVERSION) {
		return;
	}

	/* Create Lua state */
	L = existing == 0 ? luaL_newstate() : (lua_State *) (uintptr_t) existing;
	if (!L) {
		return;
	}

	/* Setup Lua state */
	JNLUA_TRY
		/* Set the Java state in the Lua state. */
		setJavaState(L, newGlobalRef(env, L, obj, JNLUA_WEAKREF));
		
		/*
		 * Create the meta table for Java objects and leave it on the stack. 
		 * Population will be finished on the Java side.
		 */
		luaL_newmetatable(L, JNLUA_MOBJECT);
		lua_pushboolean(L, 0);
		lua_setfield(L, -2, "__metatable");
		lua_pushcfunction(L, gcJavaObject);
		lua_setfield(L, -2, "__gc");
		success = 1;
	JNLUA_END
	if (!success) {
		lua_close(L);
		return;
	}
	
	/* Set the Lua state in the Java state. */
	setLuaThread(env, obj, L);
	setLuaState(env, obj, L);
}

/* lua_close() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1close (JNIEnv *env, jobject obj, jboolean ownState) {
	lua_State* L;
	lua_State* luaThread;
	lua_Debug ar;

	L = getLuaState(env, obj);
	if (ownState) {
		/* Can close? */
		luaThread = getLuaThread(env, obj);
		if (L != luaThread || lua_getstack(L, 0, &ar)) {
			return;
		}
	}

	/* Unset the Lua state in the Java state. */
	setLuaState(env, obj, NULL);
	setLuaThread(env, obj, NULL);

	if (ownState) {
		/* Prevent possible stack overflow when closing. */
		lua_settop(L, 0);
	}
	
	JNLUA_TRY
		/* Release the Java state */
		(*env)->DeleteWeakGlobalRef(env, getJavaState(L));
		
		/* Unset the Java state in the Lua state. */
		setJavaState(L, NULL);
		setJniEnv(L, NULL);
	JNLUA_END

	if (ownState) {
		/* Close Lua state */
		lua_close(L);
	}
}

/* lua_gc() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1gc (JNIEnv *env, jobject obj, jint what, jint data) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = lua_gc(L, what, data);
	JNLUA_END
	return (jint) result;
}

/* ---- Registration ---- */
/* lua_openlib() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1openlib (JNIEnv *env, jobject obj, jint lib) {
	lua_State* L;
	lua_CFunction openFunc;
	const char *libName;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		switch (lib) {
		case 0:
			openFunc = luaopen_base;
			libName = "_G";
			break;
		case 1:
			openFunc = luaopen_package;
			libName = LUA_LOADLIBNAME;
			break;
		case 2:
			openFunc = luaopen_coroutine;
			libName = LUA_COLIBNAME;
			break;
		case 3:
			openFunc = luaopen_table;
			libName = LUA_TABLIBNAME;
			break;
		case 4:
			openFunc = luaopen_io;
			libName = LUA_IOLIBNAME;
			break;
		case 5:
  			openFunc = luaopen_os;
  			libName = LUA_OSLIBNAME;
  			break;
  		case 6:
  			openFunc = luaopen_string;
  			libName = LUA_STRLIBNAME;
  			break;
		case 7:
			openFunc = luaopen_bit32;
			libName = LUA_BITLIBNAME;
			break;
  		case 8:
  			openFunc = luaopen_math;
  			libName = LUA_MATHLIBNAME;
  			break;
  		case 9:
  			openFunc = luaopen_debug;
  			libName = LUA_DBLIBNAME;
  			break;
  		default:
  			checkarg(env, L, 0, "illegal library");
			return;
  		}
		luaL_requiref(L, libName, openFunc, 1);
	JNLUA_END
}

/* ---- Load and dump ---- */
/* lua_load() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1load (JNIEnv *env, jobject obj, jobject inputStream, jstring chunkname, jstring mode) {
	lua_State *L;
	const char *chunknameUtf, *modeUtf;
	Stream stream;
	int status;

	chunknameUtf = NULL;
	modeUtf = NULL;
	stream.env = env;
	stream.byteArray = NULL;
	stream.bytes = NULL;
	stream.stream = inputStream;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		chunknameUtf = getStringUtfChars(env, L, chunkname);
		modeUtf = getStringUtfChars(env, L, mode);
		stream.byteArray = newByteArray(env, L, 1024);
		status = lua_load(L, readInputStream, &stream, chunknameUtf, modeUtf);
		if (status != LUA_OK) {
			JNLUA_THROW(status);
		}
	JNLUA_END
	if (stream.bytes) {
		(*env)->ReleaseByteArrayElements(env, stream.byteArray, stream.bytes, JNI_ABORT);
	}
	if (stream.byteArray) {
		(*env)->DeleteLocalRef(env, stream.byteArray);
	}
	if (chunknameUtf) {
		(*env)->ReleaseStringUTFChars(env, chunkname, chunknameUtf);
	}
	if (modeUtf) {
		(*env)->ReleaseStringUTFChars(env, mode, modeUtf);
	}
}

/* lua_dump() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1dump (JNIEnv *env, jobject obj, jobject outputStream) {
	Stream stream;
	lua_State *L;

	stream.env = env;
	stream.byteArray = NULL;
	stream.bytes = NULL;
	stream.stream = outputStream;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		stream.byteArray = newByteArray(env, L, 1024);
		checknelems(env, L, 1);
		lua_dump(L, writeOutputStream, &stream);
	JNLUA_END
	if (stream.bytes) {
		(*env)->ReleaseByteArrayElements(env, stream.byteArray, stream.bytes, JNI_ABORT);
	}
	if (stream.byteArray) {
		(*env)->DeleteLocalRef(env, stream.byteArray);
	}
}

/* ---- Call ---- */
/* lua_pcall() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pcall (JNIEnv *env, jobject obj, jint nargs, jint nresults) {
	lua_State* L;
	int index;
	int status;

	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkarg(env, L, nargs >= 0, "illegal argument count");
		checkarg(env, L, nresults >= 0 || nresults == LUA_MULTRET, "illegal return count");
		checknelems(env, L, nargs + 1);
		if (nresults != LUA_MULTRET) {
			checkstack(L, nresults - (nargs + 1), "call results");
		}
		index = lua_gettop(L) - nargs;
		lua_pushcfunction(L, handleError);
		lua_insert(L, index);
		status = lua_pcall(L, nargs, nresults, index);
		lua_remove(L, index);
		if (status != LUA_OK) {
			JNLUA_THROW(status);
		}
	JNLUA_END
}

/* ---- Global ---- */
/* lua_getglobal() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1getglobal (JNIEnv *env, jobject obj, jstring name) {
	const char* nameUtf;
	lua_State* L;

	nameUtf = NULL;	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		nameUtf = getStringUtfChars(env, L, name);
		lua_getglobal(L, nameUtf);
	JNLUA_END
	if (nameUtf) {
		(*env)->ReleaseStringUTFChars(env, name, nameUtf);
	}
}

/* lua_setglobal() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1setglobal (JNIEnv *env, jobject obj, jstring name) {
	const char* nameUtf;
	lua_State* L;

	nameUtf = NULL;	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		nameUtf = getStringUtfChars(env, L, name);
		checknelems(env, L, 1);
		lua_setglobal(L, nameUtf);
	JNLUA_END
	if (nameUtf) {
		(*env)->ReleaseStringUTFChars(env, name, nameUtf);
	}
}

/* ---- Stack push ---- */
/* lua_pushboolean() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushboolean (JNIEnv *env, jobject obj, jint b) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		lua_pushboolean(L, b);
	JNLUA_END
}

/* lua_pushinteger() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushinteger (JNIEnv *env, jobject obj, jint n) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		lua_pushinteger(L, n);
	JNLUA_END
}

/* lua_pushjavafunction() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushjavafunction (JNIEnv *env, jobject obj, jobject f) {
	lua_State* L;

	L = getLuaThread(env, obj);
	JNLUA_TRY
		pushJavaObject(env, L, f);
		lua_pushcclosure(L, callJavaFunction, 1);
	JNLUA_END
}

/* lua_pushjavaobject() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushjavaobject (JNIEnv *env, jobject obj, jobject object) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		pushJavaObject(env, L, object);
	JNLUA_END
}

/* lua_pushnil() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushnil (JNIEnv *env, jobject obj) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		lua_pushnil(L);
	JNLUA_END
}

/* lua_pushnumber() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushnumber (JNIEnv *env, jobject obj, jdouble n) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		lua_pushnumber(L, n);
	JNLUA_END
}

/* lua_pushstring() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushstring (JNIEnv *env, jobject obj, jstring s) {
	const char* sUtf;
	jsize sLength;
	lua_State* L;
	
	sUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		sUtf = getStringUtfChars(env, L, s);
		sLength = (*env)->GetStringUTFLength(env, s);
		lua_pushlstring(L, sUtf, sLength);
	JNLUA_END
	if (sUtf) {
		(*env)->ReleaseStringUTFChars(env, s, sUtf);
	}
}

/* ---- Stack type test ---- */
/* lua_isboolean() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isboolean (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_isboolean(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_iscfunction() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1iscfunction (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	lua_CFunction cFunction = NULL;
	
	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		cFunction = lua_tocfunction(L, index);
	JNLUA_END
	return (jint) (cFunction != NULL && cFunction != callJavaFunction);
}

/* lua_isfunction() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isfunction (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_isfunction(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_isjavafunction() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isjavafunction (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_tocfunction(L, index) == callJavaFunction;
	JNLUA_END
	return (jint) result;
}

/* lua_isjavaobject() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isjavaobject (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = getJavaObject(env, L, index, 0) != NULL;
	JNLUA_END
	return (jint) result;
}

/* lua_isnil() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isnil (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_isnil(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_isnone() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isnone (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;

	L = getLuaThread(env, obj);
	return (jint) !validindex(L, index);
}

/* lua_isnoneornil() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isnoneornil (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 1;
	}
	JNLUA_TRY
		result = lua_isnil(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_isnumber() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isnumber (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_isnumber(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_isstring() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isstring (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_isstring(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_istable() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1istable (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_istable(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_isthread() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1isthread (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;

	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_isthread(L, index);
	JNLUA_END
	return (jint) result;
}

/* ---- Stack query ---- */
/* lua_compare() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1compare (JNIEnv *env, jobject obj, jint index1, jint index2, jint operator) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = lua_compare(L, index1, index2, operator);
	JNLUA_END
	return (jint) result;
}

/* lua_rawequal() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1rawequal (JNIEnv *env, jobject obj, jint index1, jint index2) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index1);
		checkindex(env, L, index2);
		result = lua_rawequal(L, index1, index2);
	JNLUA_END
	return (jint) result;
}

/* lua_rawlen() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1rawlen (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	size_t result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		result = lua_rawlen(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_toboolean() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1toboolean (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return 0;
	}
	JNLUA_TRY
		result = lua_toboolean(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_tointeger() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1tointeger (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	lua_Integer	result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		result = lua_tointeger(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_tojavafunction() */
JNIEXPORT jobject JNICALL Java_com_naef_jnlua_LuaState_lua_1tojavafunction (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	jobject functionObj = NULL;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		if (lua_tocfunction(L, index) == callJavaFunction) {
			if (lua_getupvalue(L, index, 1)) {
				functionObj = getJavaObject(env, L, -1, javaFunctionInterface);
				lua_pop(L, 1);
			}
		}
	JNLUA_END
	return functionObj;
}

/* lua_tojavaobject() */
JNIEXPORT jobject JNICALL Java_com_naef_jnlua_LuaState_lua_1tojavaobject (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	jobject result = NULL;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		result = getJavaObject(env, L, index, 0);
	JNLUA_END
	return result;
}

/* lua_tonumber() */
JNIEXPORT jdouble JNICALL Java_com_naef_jnlua_LuaState_lua_1tonumber (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	lua_Number result = 0.0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		result = lua_tonumber(L, index);
	JNLUA_END
	return (jdouble) result;
}

/* lua_topointer() */
JNIEXPORT jlong JNICALL Java_com_naef_jnlua_LuaState_lua_1topointer (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	const void *result = NULL;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		result = lua_topointer(L, index);
	JNLUA_END
	return (jlong) (uintptr_t) result;
}

/* lua_tostring() */
JNIEXPORT jstring JNICALL Java_com_naef_jnlua_LuaState_lua_1tostring (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	const char* string = NULL;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		string = lua_tostring(L, index);
	JNLUA_END
	return string != NULL ? (*env)->NewStringUTF(env, string) : NULL;
}

/* lua_type() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1type (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	if (!validindex(L, index)) {
		return LUA_TNONE;
	}
	JNLUA_TRY
		result = lua_type(L, index);
	JNLUA_END
	return (jint) result;
}

/* ---- Stack operations ---- */
/* lua_absindex() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1absindex (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = lua_absindex(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_arith() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1arith (JNIEnv *env, jobject obj, jint operator) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checknelems(env, L, operator != LUA_OPUNM ? 2 : 1);
		lua_arith(L, operator);
	JNLUA_END
}

/* lua_concat() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1concat (JNIEnv *env, jobject obj, jint n) {
	lua_State* L;

	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkarg(env, L, n >= 0, "illegal count");
		checknelems(env, L, n);
		lua_concat(L, n);
	JNLUA_END
}

/* lua_copy() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1copy (JNIEnv *env, jobject obj, jint fromIndex, jint toIndex) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, fromIndex);
		checkindex(env, L, toIndex);
		lua_copy(L, fromIndex, toIndex);
	JNLUA_END
}

/* lua_gettop() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1gettop (JNIEnv *env, jobject obj) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = lua_gettop(L);
	JNLUA_END
	return (jint) result;
}

/* lua_len() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1len (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		lua_len(L, index);
	JNLUA_END
}

/* lua_insert() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1insert (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkrealindex(env, L, index);
		lua_insert(L, index);
	JNLUA_END
}

/* lua_pop() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pop (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkarg(env, L, index >= 0 && index <= lua_gettop(L), "illegal count");
		lua_pop(L, index);
	JNLUA_END
}

/* lua_pushvalue() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1pushvalue (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		lua_pushvalue(L, index);
	JNLUA_END
}

/* lua_remove() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1remove (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;

	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkrealindex(env, L, index);
		lua_remove(L, index);
	JNLUA_END
}

/* lua_replace() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1replace (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		checknelems(env, L, 1);
		lua_replace(L, index);
	JNLUA_END
}

/* lua_settop() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1settop (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkarg(env, L, index > 0 || (index <= 0 && -index <= lua_gettop(L)), "illegal index");
		lua_settop(L, index);
	JNLUA_END
}

/* ---- Table ---- */
/* lua_createtable() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1createtable (JNIEnv *env, jobject obj, jint narr, jint nrec) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkarg(env, L, narr >= 0, "illegal array count");
		checkarg(env, L, nrec >= 0, "illegal record count");
		lua_createtable(L, narr, nrec);
	JNLUA_END
}

/* lua_getsubtable() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1getsubtable (JNIEnv *env, jobject obj, jint index, jstring fname) {
	const char* fnameUtf;
	lua_State *L;
	int result = 0;
	
	fnameUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		fnameUtf = getStringUtfChars(env, L, fname);
		result = luaL_getsubtable(L, index, fnameUtf);
	JNLUA_END
	if (fnameUtf) {
		(*env)->ReleaseStringUTFChars(env, fname, fnameUtf);
	}
	return (jint) result;
}

/* lua_getfield() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1getfield (JNIEnv *env, jobject obj, jint index, jstring k) {
	const char* kUtf;
	lua_State* L;
	
	kUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		kUtf = getStringUtfChars(env, L, k);
		lua_getfield(L, index, kUtf);
	JNLUA_END
	if (kUtf) {
		(*env)->ReleaseStringUTFChars(env, k, kUtf);
	}
}

/* lua_gettable() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1gettable (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		lua_gettable(L, index);
	JNLUA_END
}

/* lua_newtable() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1newtable (JNIEnv *env, jobject obj) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		lua_newtable(L);
	JNLUA_END
}

/* lua_next() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1next (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checknelems(env, L, 1);
		result = lua_next(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_rawget() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1rawget (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checknelems(env, L, 1);
		lua_rawget(L, index);
	JNLUA_END
}

/* lua_rawgeti() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1rawgeti (JNIEnv *env, jobject obj, jint index, jint n) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		lua_rawgeti(L, index, n);
	JNLUA_END
}

/* lua_rawset() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1rawset (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checknelems(env, L, 2);
		lua_rawset(L, index);
	JNLUA_END
}

/* lua_rawseti() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1rawseti (JNIEnv *env, jobject obj, jint index, jint n) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checknelems(env, L, 1);
		lua_rawseti(L, index, n);
	JNLUA_END
}

/* lua_settable() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1settable (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checknelems(env, L, 2);
		lua_settable(L, index);
	JNLUA_END
}

/* lua_setfield() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1setfield (JNIEnv *env, jobject obj, jint index, jstring k) {
	const char* kUtf;
	lua_State* L;
	
	kUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checknelems(env, L, 1);
		kUtf = getStringUtfChars(env, L, k);
		lua_setfield(L, index, kUtf);
	JNLUA_END
	if (kUtf) {
		(*env)->ReleaseStringUTFChars(env, k, kUtf);
	}
}

/* ---- Meta table ---- */
/* lua_getmetatable() */
JNIEXPORT int JNICALL Java_com_naef_jnlua_LuaState_lua_1getmetatable (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		result = lua_getmetatable(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_setmetatable() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1setmetatable (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		checknelems(env, L, 1);
		checkarg(env, L, lua_type(L, -1) == LUA_TTABLE || lua_type(L, -1) == LUA_TNIL, "illegal type");
		result = lua_setmetatable(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_getmetafield() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1getmetafield (JNIEnv *env, jobject obj, jint index, jstring k) {
	const char* kUtf;
	lua_State* L;
	int result = 0;
	
	kUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checkindex(env, L, index);
		kUtf = getStringUtfChars(env, L, k);
		result = luaL_getmetafield(L, index, kUtf);
	JNLUA_END
	if (kUtf) {
		(*env)->ReleaseStringUTFChars(env, k, kUtf);
	}
	return (jint) result;
}

/* ---- Thread ---- */
/* lua_newthread() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1newthread (JNIEnv *env, jobject obj) {
	lua_State *L;
	lua_State *luaThread;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, -1, LUA_TFUNCTION);
		luaThread = lua_newthread(L);
		lua_insert(L, -2);
		lua_xmove(L, luaThread, 1);
	JNLUA_END
}

/* lua_resume() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1resume (JNIEnv *env, jobject obj, jint index, jint nargs) {
	lua_State *L;
	lua_State *luaThread;
	int status;
	int nresults = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTHREAD);
		checkarg(env, L, nargs >= 0, "illegal argument count");
		checknelems(env, L, nargs + 1);
		luaThread = lua_tothread(L, index);
		checkstack(luaThread, nargs, "resume arguments");
		lua_xmove(L, luaThread, nargs);
		status = lua_resume(luaThread, L, nargs);
		switch (status) {
		case LUA_OK:
		case LUA_YIELD:
			nresults = lua_gettop(luaThread);
			checkstack(L, nresults, "yield arguments");
			lua_xmove(luaThread, L, nresults);
			break;
		default:
			JNLUA_THROW(status);
			nresults = 0;
		}
	JNLUA_END
	return (jint) nresults;
}

/* lua_status() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1status (JNIEnv *env, jobject obj, jint index) {
	lua_State *L;
	lua_State *luaThread;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTHREAD);
		luaThread = lua_tothread(L, index);
		result = lua_status(luaThread);
	JNLUA_END
	return (jint) result;	
}

/* ---- Reference ---- */
/* lua_ref() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1ref (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		result = luaL_ref(L, index);
	JNLUA_END
	return (jint) result;
}

/* lua_unref() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1unref (JNIEnv *env, jobject obj, jint index, jint ref) {
	lua_State* L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		luaL_unref(L, index, ref);
	JNLUA_END
}

/* ---- Optimization ---- */
/* lua_tablesize() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1tablesize (JNIEnv *env, jobject obj, jint index) {
	lua_State* L;
	int count = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		lua_pushvalue(L, index);
		lua_pushnil(L);
		count = 0;
		while (lua_next(L, -2)) {
			lua_pop(L, 1);
			count++;
		}
		lua_pop(L, 1);
	JNLUA_END
	return (jint) count;
}

/* lua_tablemove() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1tablemove (JNIEnv *env, jobject obj, jint index, jint from, jint to, jint count) {
	lua_State* L;
	int i;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		checktype(env, L, index, LUA_TTABLE);
		checkarg(env, L, count >= 0, "illegal count");
		lua_pushvalue(L, index);
		if (from < to) {
			for (i = count - 1; i >= 0; i--) {
				lua_rawgeti(L, -1, from + i);
				lua_rawseti(L, -2, to + i);
			}
		} else if (from > to) {
			for (i = 0; i < count; i++) { 
				lua_rawgeti(L, -1, from + i);
				lua_rawseti(L, -2, to + i);
			}
		}
		lua_pop(L, 1); 
	JNLUA_END
}

/* ---- Argument checking ---- */
/* lua_argcheck() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1argcheck (JNIEnv *env, jobject obj, jboolean cond, jint narg, jstring extraMsg) {
	lua_State *L;
	const char *extraMsgUtf;
	
	extraMsgUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		extraMsgUtf = getStringUtfChars(env, L, extraMsg);
		luaL_argcheck(L, cond, narg, extraMsgUtf);
	JNLUA_END
	if (extraMsgUtf) {
		(*env)->ReleaseStringUTFChars(env, extraMsg, extraMsgUtf);
	}
}

/* lua_checkenum() */
JNIEXPORT jobject JNICALL Java_com_naef_jnlua_LuaState_lua_1checkenum (JNIEnv *env, jobject obj, jint narg, jobject def, jobjectArray lst) {
	lua_State *L;
	jstring defString;
	const char *defUtf;
	jsize lstLength, i;
	jstring *lstString;
	const char **lstUtf;
	jobject result = NULL;

	defString = NULL;	
	defUtf = NULL;
	lstLength = 0;
	lstString = NULL;
	lstUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		if (def != NULL) {
			defString = (*env)->CallObjectMethod(env, def, nameId);
			defUtf = getStringUtfChars(env, L, defString);
		}
		checknotnull(env, L, lst);
		lstLength = (*env)->GetArrayLength(env, lst);
		lstString = (jstring *) calloc(lstLength + 1, sizeof(jstring));
		lstUtf = (const char **) calloc(lstLength + 1, sizeof(const char *));
		check(env, L, lstString != NULL && lstUtf != NULL, luaMemoryAllocationExceptionClass, "JNI error: calloc() failed");
		for (i = 0; i < lstLength; i++) {
			lstString[i] = (*env)->CallObjectMethod(env, (*env)->GetObjectArrayElement(env, lst, i), nameId);
			lstUtf[i] = getStringUtfChars(env, L, lstString[i]);
		}
		result = (*env)->GetObjectArrayElement(env, lst, luaL_checkoption(L, narg, defUtf, lstUtf));
	JNLUA_END
	if (lstUtf) {
		for (i = 0; i < lstLength; i++) {
			if (lstUtf[i]) {
				(*env)->ReleaseStringUTFChars(env, lstString[i], lstUtf[i]);
			}
		}
		free((void *) lstUtf);
	}
	if (lstString) {
		free((void *) lstString);
	}
	if (defUtf) {
		(*env)->ReleaseStringUTFChars(env, defString, defUtf);
	}
	return result;
}

/* lua_checkinteger() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1checkinteger (JNIEnv *env, jobject obj, jint narg) {
	lua_State *L;
	lua_Integer result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = luaL_checkinteger(L, narg);
	JNLUA_END
	return (jint) result;
}

/* lua_checknumber() */
JNIEXPORT jdouble JNICALL Java_com_naef_jnlua_LuaState_lua_1checknumber (JNIEnv *env, jobject obj, jint narg) {
	lua_State *L;
	lua_Number result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = luaL_checknumber(L, narg);
	JNLUA_END
	return (jdouble) result;
}

/* lua_checkoption() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1checkoption (JNIEnv *env, jobject obj, jint narg, jstring def, jobjectArray lst) {
	lua_State *L;
	const char *defUtf;
	jsize lstLength, i;
	jstring *lstString;
	const char **lstUtf;
	int result = 0;
	
	defUtf = NULL;
	lstLength = 0;
	lstString = NULL;
	lstUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		if (def != NULL) {
			defUtf = getStringUtfChars(env, L, def);
		}
		checknotnull(env, L, lst);
		lstLength = (*env)->GetArrayLength(env, lst);
		lstString = (jstring *) calloc(lstLength + 1, sizeof(jstring));
		lstUtf = (const char **) calloc(lstLength + 1, sizeof(const char *));
		check(env, L, lstString != NULL && lstUtf != NULL, luaMemoryAllocationExceptionClass, "JNI error: calloc() failed");
		for (i = 0; i < lstLength; i++) {
			lstString[i] = (*env)->GetObjectArrayElement(env, lst, i);
			lstUtf[i] = getStringUtfChars(env, L, lstString[i]);
		}
		result = luaL_checkoption(L, narg, defUtf, lstUtf);
	JNLUA_END
	if (lstUtf) {
		for (i = 0; i < lstLength; i++) {
			if (lstUtf[i]) {
				(*env)->ReleaseStringUTFChars(env, lstString[i], lstUtf[i]);
			}
		}
		free((void *) lstUtf);
	}
	if (lstString) {
		free((void *) lstString);
	}
	if (defUtf) {
		(*env)->ReleaseStringUTFChars(env, def, defUtf);
	}
	return (jint) result;
}

/* lua_checkstring() */
JNIEXPORT jstring JNICALL Java_com_naef_jnlua_LuaState_lua_1checkstring (JNIEnv *env, jobject obj, jint narg) {
	lua_State *L;
	const char *result = NULL;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = luaL_checkstring(L, narg);
	JNLUA_END
	return (*env)->NewStringUTF(env, result); 
}

/* lua_checktype() */
JNIEXPORT void JNICALL Java_com_naef_jnlua_LuaState_lua_1checktype (JNIEnv *env, jobject obj, jint narg, jint type) {
	lua_State *L;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		luaL_checktype(L, narg, type);
	JNLUA_END
}

/* lua_optinteger() */
JNIEXPORT jint JNICALL Java_com_naef_jnlua_LuaState_lua_1optinteger (JNIEnv *env, jobject obj, jint narg, jint d) {
	lua_State *L;
	lua_Integer result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = luaL_optinteger(L, narg, d);
	JNLUA_END
	return (jint) result;
}

/* lua_optnumber() */
JNIEXPORT jdouble JNICALL Java_com_naef_jnlua_LuaState_lua_1optnumber (JNIEnv *env, jobject obj, jint narg, jdouble d) {
	lua_State *L;
	lua_Number result = 0;
	
	L = getLuaThread(env, obj);
	JNLUA_TRY
		result = luaL_optnumber(L, narg, d);
	JNLUA_END
	return (jdouble) result;
}

/* lua_optstring() */
JNIEXPORT jstring JNICALL Java_com_naef_jnlua_LuaState_lua_1optstring (JNIEnv *env, jobject obj, jint narg, jstring d) {
	lua_State *L;
	const char *dUtf;
	const char *string;
	jstring result = NULL;
	
	dUtf = NULL;
	L = getLuaThread(env, obj);
	JNLUA_TRY
		dUtf = getStringUtfChars(env, L, d);
		string = luaL_optstring(L, narg, dUtf);
		result = string != dUtf ? (*env)->NewStringUTF(env, string) : d;
	JNLUA_END
	if (dUtf) {
		(*env)->ReleaseStringUTFChars(env, d, dUtf);
	}
	return result;
}

/* ---- JNI ---- */
/* Handles the loading of this library. */
JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM *vm, void *reserved) {
	JNIEnv *env;
	
	/* Get environment */
	if ((*vm)->GetEnv(vm, (void **) &env, JNLUA_JNIVERSION) != JNI_OK) {
		return JNLUA_JNIVERSION;
	}

	/* Lookup and pin classes, fields and methods */
	if (!(luaStateClass = referenceClass(env, "com/naef/jnlua/LuaState")) ||
			!(luaStateId = (*env)->GetFieldID(env, luaStateClass, "luaState", "J")) ||
			!(luaThreadId = (*env)->GetFieldID(env, luaStateClass, "luaThread", "J")) ||
			!(yieldId = (*env)->GetFieldID(env, luaStateClass, "yield", "Z"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(javaFunctionInterface = referenceClass(env, "com/naef/jnlua/JavaFunction")) ||
			!(invokeId = (*env)->GetMethodID(env, javaFunctionInterface, "invoke", "(Lcom/naef/jnlua/LuaState;)I"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaRuntimeExceptionClass = referenceClass(env, "com/naef/jnlua/LuaRuntimeException")) ||
			!(luaRuntimeExceptionInitId = (*env)->GetMethodID(env, luaRuntimeExceptionClass, "<init>", "(Ljava/lang/String;)V")) ||
			!(setLuaErrorId = (*env)->GetMethodID(env, luaRuntimeExceptionClass, "setLuaError", "(Lcom/naef/jnlua/LuaError;)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaSyntaxExceptionClass = referenceClass(env, "com/naef/jnlua/LuaSyntaxException")) ||
			!(luaSyntaxExceptionInitId = (*env)->GetMethodID(env, luaSyntaxExceptionClass, "<init>", "(Ljava/lang/String;)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaMemoryAllocationExceptionClass = referenceClass(env, "com/naef/jnlua/LuaMemoryAllocationException")) ||
			!(luaMemoryAllocationExceptionInitId = (*env)->GetMethodID(env, luaMemoryAllocationExceptionClass, "<init>", "(Ljava/lang/String;)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaGcMetamethodExceptionClass = referenceClass(env, "com/naef/jnlua/LuaGcMetamethodException")) ||
			!(luaGcMetamethodExceptionInitId = (*env)->GetMethodID(env, luaGcMetamethodExceptionClass, "<init>", "(Ljava/lang/String;)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaMessageHandlerExceptionClass = referenceClass(env, "com/naef/jnlua/LuaMessageHandlerException")) ||
			!(luaMessageHandlerExceptionInitId = (*env)->GetMethodID(env, luaMessageHandlerExceptionClass, "<init>", "(Ljava/lang/String;)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaStackTraceElementClass = referenceClass(env, "com/naef/jnlua/LuaStackTraceElement")) ||
			!(luaStackTraceElementInitId = (*env)->GetMethodID(env, luaStackTraceElementClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;I)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(luaErrorClass = referenceClass(env, "com/naef/jnlua/LuaError")) ||
			!(luaErrorInitId = (*env)->GetMethodID(env, luaErrorClass, "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V")) ||
			!(setLuaStackTraceId = (*env)->GetMethodID(env, luaErrorClass, "setLuaStackTrace", "([Lcom/naef/jnlua/LuaStackTraceElement;)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(throwableClass = referenceClass(env, "java/lang/Throwable")) ||
			!(getMessageId = (*env)->GetMethodID(env, throwableClass, "getMessage", "()Ljava/lang/String;"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(nullPointerExceptionClass = referenceClass(env, "java/lang/NullPointerException"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(illegalArgumentExceptionClass = referenceClass(env, "java/lang/IllegalArgumentException"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(illegalStateExceptionClass = referenceClass(env, "java/lang/IllegalStateException"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(inputStreamClass = referenceClass(env, "java/io/InputStream")) ||
			!(readId = (*env)->GetMethodID(env, inputStreamClass, "read", "([B)I"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(outputStreamClass = referenceClass(env, "java/io/OutputStream")) ||
			!(writeId = (*env)->GetMethodID(env, outputStreamClass, "write", "([BII)V"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(ioExceptionClass = referenceClass(env, "java/io/IOException"))) {
		return JNLUA_JNIVERSION;
	}
	if (!(enumClass = referenceClass(env, "java/lang/Enum")) ||
			!(nameId = (*env)->GetMethodID(env, enumClass, "name", "()Ljava/lang/String;"))) {
		return JNLUA_JNIVERSION;
	}

	/* OK */
	initialized = 1;
	return JNLUA_JNIVERSION;
}

/* Handles the unloading of this library. */
JNIEXPORT void JNICALL JNI_OnUnload (JavaVM *vm, void *reserved) {
	JNIEnv *env;
	
	/* Get environment */
	if ((*vm)->GetEnv(vm, (void **) &env, JNLUA_JNIVERSION) != JNI_OK) {
		return;
	}
	
	/* Free classes */
	if (luaStateClass) {
		(*env)->DeleteGlobalRef(env, luaStateClass);
	}
	if (javaFunctionInterface) {
		(*env)->DeleteGlobalRef(env, javaFunctionInterface);
	}
	if (luaRuntimeExceptionClass) {
		(*env)->DeleteGlobalRef(env, luaRuntimeExceptionClass);
	}
	if (luaSyntaxExceptionClass) {
		(*env)->DeleteGlobalRef(env, luaSyntaxExceptionClass);
	}
	if (luaMemoryAllocationExceptionClass) {
		(*env)->DeleteGlobalRef(env, luaMemoryAllocationExceptionClass);
	}
	if (luaGcMetamethodExceptionClass) {
		(*env)->DeleteGlobalRef(env, luaGcMetamethodExceptionClass);
	}
	if (luaMessageHandlerExceptionClass) {
		(*env)->DeleteGlobalRef(env, luaMessageHandlerExceptionClass);
	}
	if (luaStackTraceElementClass) {
		(*env)->DeleteGlobalRef(env, luaStackTraceElementClass);
	}
	if (luaErrorClass) {
		(*env)->DeleteGlobalRef(env, luaErrorClass);
	}
	if (throwableClass) {
		(*env)->DeleteGlobalRef(env, throwableClass);
	}
	if (nullPointerExceptionClass) {
		(*env)->DeleteGlobalRef(env, nullPointerExceptionClass);
	}
	if (illegalArgumentExceptionClass) {
		(*env)->DeleteGlobalRef(env, illegalArgumentExceptionClass);
	}
	if (illegalStateExceptionClass) {
		(*env)->DeleteGlobalRef(env, illegalStateExceptionClass);
	}
	if (inputStreamClass) {
		(*env)->DeleteGlobalRef(env, inputStreamClass);
	}
	if (outputStreamClass) {
		(*env)->DeleteGlobalRef(env, outputStreamClass);
	}
	if (ioExceptionClass) {
		(*env)->DeleteGlobalRef(env, ioExceptionClass);
	}
	if (enumClass) {
		(*env)->DeleteGlobalRef(env, enumClass);
	}
}

/* ---- JNI helpers ---- */
/* Finds a class and returns a new JNI global reference to it. */
static jclass referenceClass (JNIEnv *env, const char *className) {
	jclass clazz;
	
	clazz = (*env)->FindClass(env, className);
	if (!clazz) {
		return NULL;
	}
	return (*env)->NewGlobalRef(env, clazz);
}

/* Returns a new JNI global reference. */
static jobject newGlobalRef (JNIEnv *env, lua_State *L, jobject obj, int type) {
	jobject ref;

	checknotnull(env, L, obj);
	if (type == JNLUA_HARDREF) {
		ref = (*env)->NewGlobalRef(env, obj);
		check(env, L, ref != NULL, luaMemoryAllocationExceptionClass, "JNI error: NewGlobalRef() failed");
	} else {
		ref = (*env)->NewWeakGlobalRef(env, obj);
		check(env, L, ref != NULL, luaMemoryAllocationExceptionClass, "JNI error: NewWeakGlobalRef() failed");
	}
	return ref;
}

/* Return a new JNI byte array. */
static jbyteArray newByteArray (JNIEnv *env, lua_State *L, jsize length) {
	jbyteArray array;
	
	array = (*env)->NewByteArray(env, length);
	check(env, L, array != NULL, luaMemoryAllocationExceptionClass, "JNI error: NewByteArray() failed");
	return array;
}

/* Returns the JNI UTF chars of a string. */
static const char *getStringUtfChars (JNIEnv *env, lua_State *L, jstring string) {
	const char *utf;

	checknotnull(env, L, string);
	utf = (*env)->GetStringUTFChars(env, string, NULL);
	check(env, L, utf != NULL, luaMemoryAllocationExceptionClass, "JNI error: getStringUTFChars() failed");
	return utf;
}

/* ---- Lua helpers ---- */
/* Checks stack space. */
static void checkstack (lua_State *L, int space, const char *msg) {
	if (!lua_checkstack(L, space)) {
		if (msg) {
			luaL_error(L, "stack overflow (%s)", msg);
		} else {
			luaL_error(L, "stack overflow");
		}
	}
}

/* ---- Java state operations ---- */
/* Returns the Lua state from the Java state. */
static lua_State *getLuaState (JNIEnv *env, jobject obj) {
	return (lua_State *) (uintptr_t) (*env)->GetLongField(env, obj, luaStateId);
}

/* Sets the Lua state in the Java state. */
static void setLuaState (JNIEnv *env, jobject obj, lua_State *L) {
	(*env)->SetLongField(env, obj, luaStateId, (jlong) (uintptr_t) L);
}

/* Returns the Lua thread from the Java state. */
static lua_State *getLuaThread (JNIEnv *env, jobject obj) {
	return (lua_State *) (uintptr_t) (*env)->GetLongField(env, obj, luaThreadId);
}

/* Sets the Lua state in the Java state. */
static void setLuaThread (JNIEnv *env, jobject obj, lua_State *L) {
	(*env)->SetLongField(env, obj, luaThreadId, (jlong) (uintptr_t) L);
}

/* Returns the yield flag from the Java state */
static jboolean getYield (JNIEnv *env, jobject obj) {
	return (*env)->GetBooleanField(env, obj, yieldId);
}

/* Sets the yield flag in the Java state */
static void setYield (JNIEnv *env, jobject obj, jboolean yield) {
	(*env)->SetBooleanField(env, obj, yieldId, yield);
}

/* ---- Lua state operations ---- */
/* Returns the JNI environment from the Lua state. */
static JNIEnv *getJniEnv (lua_State *L) {
	JNIEnv* env;
	
	lua_getfield(L, LUA_REGISTRYINDEX, JNLUA_RENV);
	env = (JNIEnv *) lua_touserdata(L, -1);
	lua_pop(L, 1);
	return env;
}

/* Sets the JNI environment in the Lua state. */
static void setJniEnv (lua_State* L, JNIEnv *env) {
	lua_pushlightuserdata(L, (void *) env);
	lua_setfield(L, LUA_REGISTRYINDEX, JNLUA_RENV);
}

/* Returns the Java state from the Lua state. */
static jobject getJavaState (lua_State *L) {
	jobject obj;
	
	lua_getfield(L, LUA_REGISTRYINDEX, JNLUA_RJAVASTATE);
	obj = (jobject) lua_touserdata(L, -1);
	lua_pop(L, 1);
	return obj;
}

/* Sets the Java state in the Lua state. */
static void setJavaState (lua_State *L, jobject javaState) {
	lua_pushlightuserdata(L, javaState);
	lua_setfield(L, LUA_REGISTRYINDEX, JNLUA_RJAVASTATE);
}

/* ---- Checks ---- */
/* Returns whether an index is valid. */
static int validindex (lua_State *L, int index) {
	int top;
	
	top = lua_gettop(L);
	if (index <= 0) {
		if (index > LUA_REGISTRYINDEX) {
			index = top + index + 1;
		} else {
			switch (index) {
			case LUA_REGISTRYINDEX:
				return 1;
			default:
				return 0; /* C upvalue access not needed, don't even validate */
			}
		}
	}
	return index >= 1 && index <= top;
}

/* Checks if an index is valid. */
static void checkindex (JNIEnv *env, lua_State *L, int index) {
	checkarg(env, L, validindex(L, index), "illegal index");
}
	
/* Checks if an index is valid, ignoring pseudo indexes. */
static void checkrealindex (JNIEnv *env, lua_State *L, int index) {
	int top;
	
	top = lua_gettop(L);
	if (index <= 0) {
		index = top + index + 1;
	}
	checkarg(env, L, index >= 1 && index <= top, "illegal index");
}

/* Checks the type of a stack value. */
static void checktype (JNIEnv *env, lua_State *L, int index, int type) {
	checkindex(env, L, index);
	checkarg(env, L, lua_type(L, index) == type, "illegal type");
}
	
/* Checks that there are at least n values on the stack. */
static void checknelems (JNIEnv *env, lua_State *L, int n) {
	checkstate(env, L, lua_gettop(L) >= n, "stack underflow");
}

/* Checks an argument for not-null. */ 
static void checknotnull (JNIEnv *env, lua_State *L, void *object) {
	check(env, L, object != NULL, nullPointerExceptionClass, "null");
}

/* Checks an argument condition. */
static void checkarg (JNIEnv *env, lua_State *L, int cond, const char *msg) {
	check(env, L, cond, illegalArgumentExceptionClass, msg);
}

/* Checks a state condition. */
static void checkstate (JNIEnv *env, lua_State *L, int cond, const char *msg) {
	check(env, L, cond, illegalStateExceptionClass, msg);
}

/* Checks a condition. */
static void check (JNIEnv *env, lua_State *L, int cond, jthrowable throwableClass, const char *msg) {
	if (!cond) {
		throw(env, L, throwableClass, msg);
	}
}

/* Throws an exception */
static void throw (JNIEnv *env, lua_State *L, jthrowable throwableClass, const char *msg) {
	(*env)->ThrowNew(env, throwableClass, msg);
	longjmp(L->errorJmp->b, -1);
}

/* ---- Java object helpers ---- */
/* Pushes a Java object on the stack. */
static void pushJavaObject (JNIEnv *env, lua_State *L, jobject object) {
	jobject *userData;
	
	userData = (jobject *) lua_newuserdata(L, sizeof(jobject));
	luaL_getmetatable(L, JNLUA_MOBJECT);
	*userData = newGlobalRef(env, L, object, JNLUA_HARDREF);
	lua_setmetatable(L, -2);
}
	
/* Returns the Java object at the specified index, or NULL if such an object is unobtainable. */
static jobject getJavaObject (JNIEnv *env, lua_State *L, int index, jclass class) {
	int result;
	jobject object;

	if (!lua_isuserdata(L, index)) {
		return NULL;
	}
	if (!lua_getmetatable(L, index)) {
		return NULL;
	}
	luaL_getmetatable(L, JNLUA_MOBJECT);
	result = lua_rawequal(L, -1, -2);
	lua_pop(L, 2);
	if (!result) {
		return NULL;
	}
	object = *(jobject *) lua_touserdata(L, index);
	if (class) {
		if (!(*env)->IsInstanceOf(env, object, class)) {
			return NULL;
		}
	}
	return object;
}

/* Returns a Java string for a value on the stack. */
static jstring toString (JNIEnv *env, lua_State *L, int index) {
	jstring string;

	string = (*env)->NewStringUTF(env, luaL_tolstring(L, index, NULL));
	lua_pop(L, 1);
	return string;
}

/* ---- Metamethods ---- */
/* Finalizes Java objects. */
static int gcJavaObject (lua_State *L) {
	JNIEnv* env;
	jobject obj;
	
	env = getJniEnv(L);
	if (!env) {
		/* Java VM has been destroyed. Nothing to do. */
		return 0;
	}
	obj = *(jobject *) lua_touserdata(L, 1);
	(*env)->DeleteGlobalRef(env, obj);
	return 0;
}

/* Calls a Java function. If an exception is reported, store it as the cause for later use. */
static int callJavaFunction (lua_State *L) {
	JNIEnv* env;
	jobject obj;
	jobject javaFunctionObj;
	lua_State *javaLuaThread;
	int result;
	jthrowable throwable;
	jstring whereString;
	
	/* Get Java context. */
	env = getJniEnv(L);
	obj = getJavaState(L);
	if (!obj) {
		lua_pushliteral(L, "no Java VM");
		return lua_error(L);
	}
	
	/* Get Java function object. */
	lua_pushvalue(L, lua_upvalueindex(1));
	javaFunctionObj = getJavaObject(env, L, -1, javaFunctionInterface);
	lua_pop(L, 1);
	if (!javaFunctionObj) {
		/* Function was cleared from outside JNLua code. */
		lua_pushliteral(L, "no Java function");
		return lua_error(L);
	}
	
	/* Perform the call, handling coroutine situations. */
	setYield(env, obj, JNI_FALSE);
	javaLuaThread = getLuaThread(env, obj);
	if (javaLuaThread == L) {
		result = (*env)->CallIntMethod(env, javaFunctionObj, invokeId, obj);
	} else {
		setLuaThread(env, obj, L);
		result = (*env)->CallIntMethod(env, javaFunctionObj, invokeId, obj);
		setLuaThread(env, obj, javaLuaThread);
	}
	
	/* Handle exception */
	throwable = (*env)->ExceptionOccurred(env);
	if (throwable) {
		/* Push exception & clear */
		lua_settop(L, 0);
		luaL_where(L, 1);
		whereString = toString(env, L, -1);
		lua_pop(L, 1);
		pushJavaObject(env, L, (*env)->NewObject(env, luaErrorClass,
				luaErrorInitId, whereString, throwable));
		(*env)->ExceptionClear(env);
		
		/* Error out */
		return lua_error(L);
	}
	
	/* Handle yield */
	if (getYield(env, obj)) {
		if (result < 0 || result > lua_gettop(L)) {
			lua_pushliteral(L, "illegal return count");
			return lua_error(L);
		}
		return lua_yield(L, result);
	}
	
	return result;
}

/* ---- Error handling ---- */
/* Handles Lua errors. */
static int handleError (lua_State *L) {
	JNIEnv *env;
	jstring messageString;
	int level;
	int count;
	lua_Debug ar;
	jobjectArray luaStackTraceArray;
	jstring functionNameString;
	jstring sourceNameString;
	jobject luaStackTraceElementObj;
	jobject luaErrorObj;

	/* Get the JNI environment. */
	env = getJniEnv(L);

	/* Count relevant stack frames */
	level = 1;
	count = 0;
	while (lua_getstack(L, level, &ar)) {
		lua_getinfo(L, "nSl", &ar);
		if (processActivationRecord(&ar)) {
			count++;
		}
		level++;
	}
	
	/* Create Lua stack trace as a Java LuaStackTraceElement[] */
	luaStackTraceArray = (*env)->NewObjectArray(env, count, luaStackTraceElementClass, NULL);
	if (!luaStackTraceArray) {
		return 1;
	}
	level = 1;
	count = 0;
	while (lua_getstack(L, level, &ar)) {
		lua_getinfo(L, "nSl", &ar);
		if (processActivationRecord(&ar)) {
			if (ar.name) {
				functionNameString = (*env)->NewStringUTF(env, ar.name);
			} else {
				functionNameString = NULL;
			}
			if (ar.source) {
				sourceNameString = (*env)->NewStringUTF(env, ar.source);
			} else {
				sourceNameString = NULL;
			}
			luaStackTraceElementObj = (*env)->NewObject(env, luaStackTraceElementClass,
					luaStackTraceElementInitId, functionNameString, sourceNameString, ar.currentline);
			if (!luaStackTraceElementObj) {
				return 1;
			}
			(*env)->SetObjectArrayElement(env, luaStackTraceArray, count, luaStackTraceElementObj);
			if ((*env)->ExceptionCheck(env)) {
				return 1;
			}
			count++;
		}
		level++;
	}
	
	/* Get or create the error object  */
	luaErrorObj = getJavaObject(env, L, -1, luaErrorClass);
	if (!luaErrorObj) {
		messageString = toString(env, L, -1);
		if (!(luaErrorObj = (*env)->NewObject(env, luaErrorClass, luaErrorInitId, messageString, NULL))) {
			return 1;
		}
	}
	(*env)->CallVoidMethod(env, luaErrorObj, setLuaStackTraceId, luaStackTraceArray);
	
	/* Replace error */
	pushJavaObject(env, L, luaErrorObj);
	return 1;
}

/* Processes a Lua activation record and returns whether it is relevant. */
static int processActivationRecord (lua_Debug *ar) {
	if (ar->name && strlen(ar->name) == 0) {
		ar->name = NULL;
	}
	if (ar->what && strcmp(ar->what, "C") == 0) {
		ar->source = NULL;
	}
	if (ar->source) {
		if (*ar->source == '=' || *ar->source == '@') {
			ar->source++;
		}
	}
	return ar->name || ar->source;
}

/* Handles Lua errors by throwing a Java exception. */
static void throwException (JNIEnv* env, lua_State *L, int status) {
	jclass throwableClass;
	jmethodID throwableInitId;
	jthrowable throwable;
	jobject luaErrorObj;
	
	/* Determine the type of exception to throw. */
	switch (status) {
	case LUA_ERRSYNTAX:
		throwableClass = luaSyntaxExceptionClass;
		throwableInitId = luaSyntaxExceptionInitId;
		break;
	case LUA_ERRMEM:
		throwableClass = luaMemoryAllocationExceptionClass;
		throwableInitId = luaMemoryAllocationExceptionInitId;
		break;
	case LUA_ERRERR:
		throwableClass = luaMessageHandlerExceptionClass;
		throwableInitId = luaMessageHandlerExceptionInitId;
		break;
	case LUA_ERRGCMM:
		throwableClass = luaGcMetamethodExceptionClass;
		throwableInitId = luaGcMetamethodExceptionInitId;
		break;
	default:
		throwableClass = luaRuntimeExceptionClass;
		throwableInitId = luaRuntimeExceptionInitId;
	}

	/* Create exception */
	throwable = (*env)->NewObject(env, throwableClass, throwableInitId, toString(env, L, -1));
	if (!throwable) {
		return;
	}
	
	/* Set the Lua error, if any. */
	luaErrorObj = getJavaObject(env, L, -1, luaErrorClass);
	if (luaErrorObj && throwableClass == luaRuntimeExceptionClass) {
		(*env)->CallVoidMethod(env, throwable, setLuaErrorId, luaErrorObj);
	}
	
	/* Throw */
	if ((*env)->Throw(env, throwable) < 0) {
		return;
	}
	
	/* Pop error */
	lua_pop(L, 1);
}

/* ---- Stream adapters ---- */
/* Lua reader for Java input streams. */
static const char *readInputStream (lua_State *L, void *ud, size_t *size) {
	Stream *stream;
	int read;

	stream = (Stream *) ud;
	read = (*stream->env)->CallIntMethod(stream->env, stream->stream, readId, stream->byteArray);
	if ((*stream->env)->ExceptionCheck(stream->env)) {
		return NULL;
	}
	if (read == -1) {
		return NULL;
	}
	if (stream->bytes && stream->isCopy) {
		(*stream->env)->ReleaseByteArrayElements(stream->env, stream->byteArray, stream->bytes, JNI_ABORT);
		stream->bytes = NULL;
	}
	if (!stream->bytes) {
		stream->bytes = (*stream->env)->GetByteArrayElements(stream->env, stream->byteArray, &stream->isCopy);
		if (!stream->bytes) {
			(*stream->env)->ThrowNew(stream->env, ioExceptionClass, "error accessing IO buffer");
			return NULL;
		}
	}
	*size = read;
	return (const char *) stream->bytes;
}

/* Lua writer for Java output streams. */
static int writeOutputStream (lua_State *L, const void *data, size_t size, void *ud) {
	Stream *stream;

	stream = (Stream *) ud;
	if (!stream->bytes) {
		stream->bytes = (*stream->env)->GetByteArrayElements(stream->env, stream->byteArray, &stream->isCopy);
		if (!stream->bytes) {
			(*stream->env)->ThrowNew(stream->env, ioExceptionClass, "error accessing IO buffer");
			return 1;
		}
	}
	memcpy(stream->bytes, data, size);
	if (stream->isCopy) {
		(*stream->env)->ReleaseByteArrayElements(stream->env, stream->byteArray, stream->bytes, JNI_COMMIT);
	}
	(*stream->env)->CallVoidMethod(stream->env, stream->stream, writeId, stream->byteArray, 0, size);
	if ((*stream->env)->ExceptionCheck(stream->env)) {
		return 1;
	}
	return 0;
}
