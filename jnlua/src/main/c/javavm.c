/*
 * $Id$
 * Provides the Java VM module. See LICENSE.txt for license terms.
 */

#include "javavm.h"

/*
 * Java VM parameters.
 */
#define JAVAVM_METATABLE "javavm.metatable"
#define JAVAVM_VM "javavm.vm"
#define JAVAVM_MAXOPTIONS 128

#define JAVAVM_JNIVERSION JNI_VERSION_1_6

/*
 * VM record.
 */
typedef struct vm_rec {
	JavaVM *vm;
	jobject lua_state;
	JavaVMOption options[JAVAVM_MAXOPTIONS];
	int num_options;
} vm_rec;

/*
 * Releases a VM.
 */
static int release_vm (lua_State *L) {
	vm_rec *vm;
	jclass lua_state_class;
	jmethodID lua_state_close_id;
	JNIEnv *env;
	int res;
	
	vm = luaL_checkudata(L, 1, JAVAVM_METATABLE);
	
	/* Already released? */
	if (!vm->vm) {
		return 0;
	}
	
	/* Check thread */
	if ((*vm->vm)->GetEnv(vm->vm, (void **) &env, JAVAVM_JNIVERSION) != JNI_OK) {
		return luaL_error(L, "invalid thread");
	}
	
	/* Close the Lua state in the Java VM */
	if (vm->lua_state) {
		lua_state_class = (*env)->GetObjectClass(env, vm->lua_state);
		if (!(lua_state_close_id = (*env)->GetMethodID(env, lua_state_class, "close", "()V"))) {
			return luaL_error(L, "JNLua close method not found");
		}
		(*env)->CallVoidMethod(env, vm->lua_state, lua_state_close_id);
		(*env)->DeleteGlobalRef(env, vm->lua_state);
		vm->lua_state = NULL;
	}
	
	/* Destroy the Java VM */
	res = (*vm->vm)->DestroyJavaVM(vm->vm);
	if (res < 0) {
		return luaL_error(L, "error destroying Java VM: %d", res);
	}
	vm->vm = NULL;
	
	return 0;
}

/*
 * Returns a string representation of a VM.
 */
static int tostring_vm (lua_State *L) {
	vm_rec *vm;
	int i;
	
	vm = luaL_checkudata(L, 1, JAVAVM_METATABLE);
	lua_pushfstring(L, "Java VM (%p)", vm->vm);
	luaL_checkstack(L, vm->num_options, NULL);
	for (i = 0; i < vm->num_options; i++) {
		lua_pushfstring(L, "\n\t%s", vm->options[i].optionString);
	}
	lua_concat(L, vm->num_options + 1);
	return 1;
}

/*
 * Creates a VM.
 */
static int create_vm (lua_State *L) {
	vm_rec *vm;
	int i;
	const char *option;
	JavaVMInitArgs vm_args;
	int res;
	JNIEnv *env;
	jclass lua_state_class, library_class;
	jmethodID lua_state_init_id, lua_state_openlib_id;
	jfieldID library_java_id;
	jobject lua_state, java;

	/* Check for existing VM */
	lua_getfield(L, LUA_REGISTRYINDEX, JAVAVM_VM);
	if (!lua_isnil(L, -1)) {
		return luaL_error(L, "VM already created");
	}
	lua_pop(L, 1);
	
	/* Create VM */
	vm = lua_newuserdata(L, sizeof(vm_rec));
	memset(vm, 0, sizeof(vm_rec));
	luaL_getmetatable(L, JAVAVM_METATABLE);
	lua_setmetatable(L, -2);
	
	/* Process options */
	vm->num_options = lua_gettop(L) - 1;
	if (vm->num_options > JAVAVM_MAXOPTIONS) {
		return luaL_error(L, "%d options limit, got %d", JAVAVM_MAXOPTIONS, vm->num_options);
	}
	for (i = 1; i <= vm->num_options; i++) {
		option = luaL_checkstring(L, i);
		if (strcmp(option, "vfprintf") == 0
				|| strcmp(option, "exit") == 0
				|| strcmp(option, "abort") == 0) {
			luaL_error(L, "unsupported option: %s", option);
		}
		vm->options[i - 1].optionString = (char *) option;
	}
	
	/* Create Java VM */
	vm_args.version = JAVAVM_JNIVERSION;
	vm_args.options = vm->options;
	vm_args.nOptions = vm->num_options;
	vm_args.ignoreUnrecognized = JNI_TRUE;
	res = JNI_CreateJavaVM(&vm->vm, (void**) &env, &vm_args);
	if (res < 0) {
		return luaL_error(L, "error creating Java VM: %d", res);
	}
	
	/* Create a LuaState in the Java VM */
	if (!(lua_state_class = (*env)->FindClass(env, "com/naef/jnlua/LuaState"))
			|| !(lua_state_init_id = (*env)->GetMethodID(env, lua_state_class, "<init>", "(J)V"))) {
		return luaL_error(L, "JNLua not found");
	}
	lua_state = (*env)->NewObject(env, lua_state_class, lua_state_init_id, (jlong) (uintptr_t) L);
	if (lua_state == NULL) {
		return luaL_error(L, "error creating LuaState");
	}
	vm->lua_state = (*env)->NewGlobalRef(env, lua_state);
	if (vm->lua_state == NULL) {
		return luaL_error(L, "error referencing LuaState");
	}
	
	/* Load the Java module */
	if (!(library_class  = (*env)->FindClass(env, "com/naef/jnlua/LuaState$Library"))
			|| !(lua_state_openlib_id = (*env)->GetMethodID(env, lua_state_class, "openLib", "(Lcom/naef/jnlua/LuaState$Library;)V"))
			|| !(library_java_id = (*env)->GetStaticFieldID(env, library_class, "JAVA", "Lcom/naef/jnlua/LuaState$Library;"))
			|| !(java = (*env)->GetStaticObjectField(env, library_class, library_java_id))) {
		return luaL_error(L, "Java module not found");
	}
	(*env)->CallVoidMethod(env, lua_state, lua_state_openlib_id, java);
	if ((*env)->ExceptionOccurred(env)) {
		return luaL_error(L, "error loading Java module");
	}
	lua_pop(L, 1);
	
	/* Store VM */
	lua_pushvalue(L, -1);
	lua_setfield(L, LUA_REGISTRYINDEX, JAVAVM_VM);
	
	return 1;
}

/*
 * Destroys the VM.
 */
static int destroy_vm (lua_State *L) {
	/* Release VM, if any */
	lua_pushcfunction(L, release_vm);
	lua_getfield(L, LUA_REGISTRYINDEX, JAVAVM_VM);
	if (lua_isnil(L, -1)) {
		/* No VM to destroy */
		lua_pushboolean(L, 0);
		return 1;
	}
	lua_call(L, 1, 0);
	
	/* Clear VM */
	lua_pushnil(L);
	lua_setfield(L, LUA_REGISTRYINDEX, JAVAVM_VM);
	
	/* Success */
	lua_pushboolean(L, 1);
	return 1;
}

/*
 * Returns the VM, if any.
 */
static int get_vm (lua_State *L) {
	lua_getfield(L, LUA_REGISTRYINDEX, JAVAVM_VM);
	return 1;
}

/*
 * Java VM module functions.
 */
static const luaL_Reg functions[] = {
	{ "create", create_vm },
	{ "destroy", destroy_vm },
	{ "get", get_vm },
	{ NULL, NULL }
};

/*
 * Exported functions.
 */ 
 
LUALIB_API int luaopen_javavm (lua_State *L) {
	/* Create module */
	luaL_newlib(L, functions);
	
	/* Create metatable */
	luaL_newmetatable(L, JAVAVM_METATABLE);
	lua_pushcfunction(L, release_vm);
	lua_setfield(L, -2, "__gc");
	lua_pushcfunction(L, tostring_vm);
	lua_setfield(L, -2, "__tostring");
	lua_pop(L, 1);
	
	return 1;
}
