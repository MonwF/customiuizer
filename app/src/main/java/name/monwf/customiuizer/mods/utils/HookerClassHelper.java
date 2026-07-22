package name.monwf.customiuizer.mods.utils;

import java.lang.reflect.Member;
import java.util.List;

import io.github.libxposed.api.XposedInterface;

/**
 * API 101 compatibility adapter for the module's existing before/after hooks.
 *
 * <p>The adapter deliberately keeps the API 100 callback behavior used by the Android 14
 * codebase, including mutable arguments, early return/throw and after-hook exception recovery.</p>
 */
public class HookerClassHelper {
    interface BeforeMethodCallback {
        void beforeHook(BeforeHookCallback callback);
    }

    interface AfterMethodCallback {
        void afterHook(AfterHookCallback callback);
    }

    public static class BeforeHookCallback {
        private static final Object[] EMPTY_ARGS = new Object[0];

        private final Member member;
        private final Object thisObject;
        private final Object[] args;
        private boolean skipped;
        private Object result;
        private Throwable throwable;

        BeforeHookCallback(XposedInterface.Chain chain) {
            member = chain.getExecutable();
            thisObject = chain.getThisObject();
            List<Object> argList = chain.getArgs();
            args = argList.isEmpty() ? EMPTY_ARGS : argList.toArray();
        }

        public Member getMember() {
            return member;
        }

        public Object getThisObject() {
            return thisObject;
        }

        public Object[] getArgs() {
            return args;
        }

        public void returnAndSkip(Object returnValue) {
            skipped = true;
            result = returnValue;
            throwable = null;
        }

        public void throwAndSkip(Throwable throwable) {
            skipped = true;
            result = null;
            this.throwable = throwable;
        }
    }

    public static class AfterHookCallback {
        private final Member member;
        private final Object thisObject;
        private final Object[] args;
        private final boolean skipped;
        private Object result;
        private Throwable throwable;

        AfterHookCallback(BeforeHookCallback before, Object result, Throwable throwable) {
            member = before.member;
            thisObject = before.thisObject;
            args = before.args;
            skipped = before.skipped;
            this.result = result;
            this.throwable = throwable;
        }

        public Member getMember() {
            return member;
        }

        public Object getThisObject() {
            return thisObject;
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getResult() {
            return result;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public void setResult(Object result) {
            this.result = result;
            throwable = null;
        }

        public void setThrowable(Throwable throwable) {
            result = null;
            this.throwable = throwable;
        }
    }

    public static class MethodHook implements BeforeMethodCallback, AfterMethodCallback, XposedInterface.Hooker {
        public final int mPriority;
        private final boolean mIsReturnConstant;
        private final Object mReturnConstantValue;
        private final boolean hasAfter;

        public MethodHook() {
            this(XposedInterface.PRIORITY_DEFAULT);
        }

        public MethodHook(int priority) {
            mPriority = priority;
            mIsReturnConstant = false;
            mReturnConstantValue = null;
            boolean ha = false;
            if (getClass() != MethodHook.class) {
                try {
                    getClass().getDeclaredMethod("after", AfterHookCallback.class);
                    ha = true;
                } catch (NoSuchMethodException ignored) {}
            }
            this.hasAfter = ha;
        }

        /** Creates a hook that always returns the supplied constant value. */
        public MethodHook(int priority, Object returnConstantValue) {
            mPriority = priority;
            mIsReturnConstant = true;
            mReturnConstantValue = returnConstantValue;
            this.hasAfter = false;
        }

        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            if (mIsReturnConstant) {
                return mReturnConstantValue;
            }

            BeforeHookCallback before = new BeforeHookCallback(chain);
            beforeHook(before);

            Object result = before.result;
            Throwable throwable = before.throwable;
            if (!before.skipped) {
                try {
                    result = chain.proceed(before.args);
                } catch (Throwable t) {
                    throwable = t;
                }
            }

            if (hasAfter) {
                AfterHookCallback after = new AfterHookCallback(before, result, throwable);
                afterHook(after);
                if (after.throwable != null) {
                    throw after.throwable;
                }
                return after.result;
            }

            if (throwable != null) {
                throw throwable;
            }
            return result;
        }

        public final void beforeHook(BeforeHookCallback callback) {
            try {
                before(callback);
            } catch (Throwable t) {
                XposedHelpers.log(t);
            }
        }

        public final void afterHook(AfterHookCallback callback) {
            try {
                after(callback);
            } catch (Throwable t) {
                XposedHelpers.log(t);
            }
        }

        protected void before(BeforeHookCallback callback) throws Throwable {
        }

        protected void after(AfterHookCallback callback) throws Throwable {
        }
    }

    public interface CustomMethodUnhooker {
        void unhook();
    }

    /** Skips the hooked method and returns {@code null}. */
    public static final MethodHook DO_NOTHING = new MethodHook(XposedInterface.PRIORITY_HIGHEST, null);

    /** Creates a highest-priority callback which always returns the supplied value. */
    public static MethodHook returnConstant(final Object result) {
        return returnConstant(XposedInterface.PRIORITY_HIGHEST, result);
    }

    /** Creates a callback which always returns the supplied value at the requested priority. */
    public static MethodHook returnConstant(int priority, final Object result) {
        return new MethodHook(priority, result);
    }
}
