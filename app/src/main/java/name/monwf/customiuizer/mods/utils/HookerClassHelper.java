package name.monwf.customiuizer.mods.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.annotations.AfterInvocation;
import io.github.libxposed.api.annotations.BeforeInvocation;
import io.github.libxposed.api.annotations.XposedHooker;

public class HookerClassHelper {
    interface BeforeMethodCallback {
        void beforeHook(XposedInterface.BeforeHookCallback callback);
    }

    interface AfterMethodCallback {
        void afterHook(XposedInterface.AfterHookCallback callback);
    }

    public static class MethodHook implements BeforeMethodCallback, AfterMethodCallback {
        public int mPriority;

        public MethodHook() {
            this(XposedInterface.PRIORITY_DEFAULT);
        }
        public MethodHook(int priority) {
            mPriority = priority;
        }
        public final void beforeHook(XposedInterface.BeforeHookCallback callback) {
            try {
                this.before(callback);
            } catch (Throwable t) {
                XposedHelpers.log(t);
            }
        }
        public final void afterHook(XposedInterface.AfterHookCallback callback) {
            try {
                this.after(callback);
            } catch (Throwable t) {
                XposedHelpers.log(t);
            }
        }
        protected void before(XposedInterface.BeforeHookCallback callback) throws Throwable {

        }
        protected void after(XposedInterface.AfterHookCallback callback) throws Throwable {

        }
    }

    static class BeforeHookerInfo {
        public String mHookerId;
        public BeforeMethodCallback mCallback;
        BeforeHookerInfo(String hkId, BeforeMethodCallback callback) {
            mHookerId = hkId;
            mCallback = callback;
        }
    }

    static class AfterHookerInfo {
        public String mHookerId;
        public AfterMethodCallback mCallback;
        AfterHookerInfo(String hkId, AfterMethodCallback callback) {
            mHookerId = hkId;
            mCallback = callback;
        }
    }

    public interface CustomMethodUnhooker {

        void unhook();
    }

    @XposedHooker
    public static class CustomHooker implements XposedInterface.Hooker {
        static ConcurrentHashMap<Member, ArrayList<BeforeHookerInfo>> beforeCallbacks = new ConcurrentHashMap<>();
        static ConcurrentHashMap<Member, ArrayList<AfterHookerInfo>> afterCallbacks = new ConcurrentHashMap<>();

        public static CustomMethodUnhooker addCallback(Member m, MethodHook hook) {
            String hookerId = RandomStringUtils.randomAlphanumeric(12);
            for (Method method : hook.getClass().getDeclaredMethods()) {
                if (method.getName().equals("before")) {
                    ArrayList<BeforeHookerInfo> hookers = beforeCallbacks.get(m);
                    boolean firstHook = hookers == null;
                    if (firstHook) hookers = new ArrayList<BeforeHookerInfo>();
                    hookers.add(new BeforeHookerInfo(hookerId, hook));
                    if (firstHook) beforeCallbacks.put(m, hookers);
                }
                else if (method.getName().equals("after")) {
                    ArrayList<AfterHookerInfo> hookers = afterCallbacks.get(m);
                    boolean firstHook = hookers == null;
                    if (firstHook) hookers = new ArrayList<AfterHookerInfo>();
                    hookers.add(new AfterHookerInfo(hookerId, hook));
                    if (firstHook) afterCallbacks.put(m, hookers);
                }
            }
            return new CustomMethodUnhooker() {
              public void unhook() {
                  ArrayList<BeforeHookerInfo> beforeHookers = beforeCallbacks.get(m);
                  if (beforeHookers != null) {
                    for (BeforeHookerInfo hookerInfo: beforeHookers) {
                        if (hookerInfo.mHookerId.equals(hookerId)) {
                            beforeHookers.remove(hookerInfo);
                            break;
                        }
                    }
                  }
                  ArrayList<AfterHookerInfo> afterHookers = afterCallbacks.get(m);
                  if (afterHookers != null) {
                      for (AfterHookerInfo hookerInfo: afterHookers) {
                          if (hookerInfo.mHookerId.equals(hookerId)) {
                              afterHookers.remove(hookerInfo);
                              break;
                          }
                      }
                  }
              }
            };
        }

        public static boolean memberIsRegistered(Member m) {
            return beforeCallbacks.get(m) != null || afterCallbacks.get(m) != null;
        }

        @BeforeInvocation
        public static void before(XposedInterface.BeforeHookCallback callback) {
            ArrayList<BeforeHookerInfo> hookers = beforeCallbacks.get(callback.getMember());
            if (hookers != null) {
                for (BeforeHookerInfo hookerInfo: hookers) {
                    hookerInfo.mCallback.beforeHook(callback);
                }
            }
        }
        @AfterInvocation
        public static void after(XposedInterface.AfterHookCallback callback) {
            ArrayList<AfterHookerInfo> hookers = afterCallbacks.get(callback.getMember());
            if (hookers != null) {
                for (AfterHookerInfo hookerInfo: hookers) {
                    hookerInfo.mCallback.afterHook(callback);
                }
            }
        }
    }

    @XposedHooker
    public static class HighestPriorityHooker extends CustomHooker {
        @BeforeInvocation
        public static void before(XposedInterface.BeforeHookCallback callback) {
            CustomHooker.before(callback);
        }
        @AfterInvocation
        public static void after(XposedInterface.AfterHookCallback callback) {
            CustomHooker.after(callback);
        }
    }

    @XposedHooker
    public static class LowestPriorityHooker extends CustomHooker {
        @BeforeInvocation
        public static void before(XposedInterface.BeforeHookCallback callback) {
            CustomHooker.before(callback);
        }
        @AfterInvocation
        public static void after(XposedInterface.AfterHookCallback callback) {
            CustomHooker.after(callback);
        }
    }

    /**
     * Predefined callback that skips the method without replacements.
     */
    public static final MethodHook DO_NOTHING = new MethodHook(XposedInterface.PRIORITY_HIGHEST * 2) {
        @Override
        protected void before(XposedInterface.BeforeHookCallback param) throws Throwable {
            param.returnAndSkip(null);
        }
    };

    /**
     * Creates a callback which always returns a specific value.
     *
     * @param result The value that should be returned to callers of the hooked method.
     */
    public static MethodHook returnConstant(final Object result) {
        return returnConstant(XposedInterface.PRIORITY_DEFAULT, result);
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
            protected void before(XposedInterface.BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(result);
            }
        };
    }
}
