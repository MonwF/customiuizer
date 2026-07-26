package tv.withaibuild.customiuizer.mods.utils

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Member
import java.lang.reflect.Method

/**
 * API 101 compatibility adapter for the module's existing before/after hooks.
 *
 * The adapter deliberately keeps the API 100 callback behavior used by the Android 14
 * codebase, including mutable arguments, early return/throw and after-hook exception recovery.
 */
class HookerClassHelper private constructor() {

    interface BeforeMethodCallback {
        fun beforeHook(callback: BeforeHookCallback)
    }

    interface AfterMethodCallback {
        fun afterHook(callback: AfterHookCallback)
    }

    class BeforeHookCallback internal constructor(private val chain: XposedInterface.Chain) {
        private val EMPTY_ARGS = emptyArray<Any?>()

        private var member: Member? = null
        private var thisObject: Any? = null
        private var args: Array<Any?>? = null
        private var thisObjectResolved = false

        internal var skipped = false
        internal var result: Any? = null
        internal var throwable: Throwable? = null

        fun getMember(): Member {
            if (member == null) member = chain.executable
            return member!!
        }

        fun getThisObject(): Any? {
            if (!thisObjectResolved) {
                thisObject = chain.thisObject
                thisObjectResolved = true
            }
            return thisObject
        }

        fun getArgs(): Array<Any?> {
            if (args == null) {
                val argList: List<Any?> = chain.getArgs()
                args = if (argList.isEmpty()) EMPTY_ARGS else argList.toTypedArray()
            }
            return args!!
        }

        internal fun hasMaterializedArgs(): Boolean = args != null

        fun returnAndSkip(returnValue: Any?) {
            skipped = true
            result = returnValue
            throwable = null
        }

        fun throwAndSkip(throwable: Throwable) {
            skipped = true
            result = null
            this.throwable = throwable
        }
    }

    class AfterHookCallback internal constructor(
        private val before: BeforeHookCallback,
        result: Any?,
        throwable: Throwable?
    ) {
        val isSkipped: Boolean = before.skipped

        private var _result: Any? = result
        private var _throwable: Throwable? = throwable

        fun getMember(): Member = before.getMember()
        fun getThisObject(): Any? = before.getThisObject()
        fun getArgs(): Array<Any?> = before.getArgs()

        fun getResult(): Any? = _result
        fun setResult(result: Any?) {
            _result = result
            _throwable = null
        }

        fun getThrowable(): Throwable? = _throwable
        fun setThrowable(throwable: Throwable) {
            _result = null
            _throwable = throwable
        }
    }

    open class MethodHook : BeforeMethodCallback, AfterMethodCallback, XposedInterface.Hooker {
        @JvmField
        val mPriority: Int

        @JvmField
        var mIsReturnConstant = false

        @JvmField
        var mReturnConstantValue: Any? = null

        @Volatile
        private var afterCallbackState: Byte = 0

        constructor() : this(XposedInterface.PRIORITY_DEFAULT)

        constructor(priority: Int) {
            mPriority = priority
        }

        /**
         * Detects an after callback by signature instead of its source name.
         *
         * R8 may rename `after(...)` in release builds. Looking it up with the literal
         * name "after" therefore disables every after hook after obfuscation, even though the
         * override is still present. The callback parameter type remains stable inside the same
         * optimized DEX and is safe to use for detection.
         */
        private fun declaresAfterCallback(hookClass: Class<*>): Boolean {
            var current: Class<*>? = hookClass
            while (current != null && current != MethodHook::class.java) {
                for (method in current.declaredMethods) {
                    val parameterTypes = method.parameterTypes
                    if (method.returnType == Void.TYPE
                        && parameterTypes.size == 1
                        && parameterTypes[0] == AfterHookCallback::class.java
                    ) {
                        return true
                    }
                }
                current = current.superclass
            }
            return false
        }

        private fun hasAfterCallback(): Boolean {
            val state = afterCallbackState
            if (state.toInt() == 0) {
                val newState = if (declaresAfterCallback(javaClass)) 2 else 1
                afterCallbackState = newState.toByte()
            }
            return afterCallbackState.toInt() == 2
        }

        @Throws(Throwable::class)
        open override fun intercept(chain: XposedInterface.Chain): Any? {
            if (mIsReturnConstant) {
                return mReturnConstantValue
            }

            val before = BeforeHookCallback(chain)
            beforeHook(before)

            var result: Any? = before.result
            var throwable: Throwable? = before.throwable
            if (!before.skipped) {
                try {
                    result = if (before.hasMaterializedArgs())
                        chain.proceed(before.getArgs())
                    else
                        chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                }
            }

            if (hasAfterCallback()) {
                val after = AfterHookCallback(before, result, throwable)
                afterHook(after)
                if (after.getThrowable() != null) {
                    throw after.getThrowable()!!
                }
                return after.getResult()
            }

            if (throwable != null) {
                throw throwable
            }
            return result
        }

        override fun beforeHook(callback: BeforeHookCallback) {
            try {
                before(callback)
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }

        override fun afterHook(callback: AfterHookCallback) {
            try {
                after(callback)
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }

        @Throws(Throwable::class)
        protected open fun before(callback: BeforeHookCallback) {
        }

        @Throws(Throwable::class)
        protected open fun after(callback: AfterHookCallback) {
        }
    }

    interface CustomMethodUnhooker {
        fun unhook()
    }

    companion object {
        /** Skips the hooked method and returns `null`.  */
        @JvmField
        val DO_NOTHING: MethodHook = object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            init {
                mIsReturnConstant = true
                mReturnConstantValue = null
            }
        }

        /** Creates a highest-priority callback which always returns the supplied value.  */
        @JvmStatic
        fun returnConstant(result: Any?): MethodHook = returnConstant(XposedInterface.PRIORITY_HIGHEST, result)

        /** Creates a callback which always returns the supplied value at the requested priority.  */
        @JvmStatic
        fun returnConstant(priority: Int, result: Any?): MethodHook {
            return object : MethodHook(priority) {
                init {
                    mIsReturnConstant = true
                    mReturnConstantValue = result
                }
            }
        }
    }
}
