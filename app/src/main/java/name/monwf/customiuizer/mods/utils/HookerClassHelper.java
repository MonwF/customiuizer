package name.monwf.customiuizer.mods.utils;

import java.lang.reflect.Member;

import io.github.libxposed.api.XposedInterface;

public class HookerClassHelper {
    public static class MethodHookParam {
        private enum Phase {
            BEFORE,
            AFTER
        }

        private final Member member;
        private final Object thisObject;
        private final Object[] args;
        private Object result;
        private boolean returnEarly;
        private Phase phase;

        MethodHookParam(Member member, Object thisObject, Object[] args) {
            this.member = member;
            this.thisObject = thisObject;
            this.args = args;
            this.phase = Phase.BEFORE;
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

        public int getArgsCount() {
            return args.length;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public void returnAndSkip(Object result) {
            if (phase != Phase.BEFORE) {
                throw new IllegalStateException("returnAndSkip can only be called from beforeHookedMethod");
            }
            this.result = result;
            this.returnEarly = true;
        }

        boolean isReturnEarly() {
            return returnEarly;
        }

        void enterAfterPhase(Object result) {
            this.result = result;
            this.phase = Phase.AFTER;
        }
    }

    public static class MethodHook {
        public int mPriority;

        public MethodHook() {
            this(XposedInterface.PRIORITY_DEFAULT);
        }
        public MethodHook(int priority) {
            mPriority = priority;
        }
        public final void beforeHook(MethodHookParam callback) throws Throwable {
            this.before(callback);
        }
        public final void afterHook(MethodHookParam callback) throws Throwable {
            this.after(callback);
        }
        protected void before(MethodHookParam callback) throws Throwable {

        }
        protected void after(MethodHookParam callback) throws Throwable {

        }
    }

    public static XposedInterface.Hooker newHooker(MethodHook hook) {
        return chain -> {
            Member member = chain.getExecutable();
            Object[] args = chain.getArgs().toArray(new Object[0]);

            MethodHookParam callback = new MethodHookParam(member, chain.getThisObject(), args);
            hook.beforeHook(callback);

            Object result;
            if (callback.isReturnEarly()) {
                result = callback.getResult();
            } else {
                result = chain.proceed(callback.args);
            }

            callback.enterAfterPhase(result);
            hook.afterHook(callback);

            return callback.getResult();
        };
    }

    /**
     * Predefined callback that skips the method without replacements.
     */
    public static final MethodHook DO_NOTHING = new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
        @Override
        protected void before(MethodHookParam param) {
            param.returnAndSkip(null);
        }
    };

    /**
     * Creates a callback which always returns a specific value.
     *
     * @param result The value that should be returned to callers of the hooked method.
     */
    public static MethodHook returnConstant(final Object result) {
        return returnConstant(XposedInterface.PRIORITY_HIGHEST, result);
    }

    /**
     * Creates a callback which always returns a specific value, but allows to specify a priority for the callback.
     *
     * @param priority See {@link XposedInterface#PRIORITY_DEFAULT}.
     * @param result   The value that should be returned to callers of the hooked method.
     */
    public static MethodHook returnConstant(int priority, final Object result) {
        return new MethodHook(priority) {
            @Override
            protected void before(MethodHookParam param) {
                param.returnAndSkip(result);
            }
        };
    }
}