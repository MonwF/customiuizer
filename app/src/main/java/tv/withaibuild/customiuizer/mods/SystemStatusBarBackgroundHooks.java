package tv.withaibuild.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static tv.withaibuild.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findMethodExactIfExists;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.MiuiNotification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import io.github.libxposed.api.XposedInterface;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.ResourceConstants;
import tv.withaibuild.customiuizer.mods.utils.ResourceHooks;
import tv.withaibuild.customiuizer.mods.utils.WeatherDataController;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.AudioVisualizer;
import tv.withaibuild.customiuizer.utils.Helpers;
import tv.withaibuild.customiuizer.utils.Helpers.MimeType;

public class SystemStatusBarBackgroundHooks {
    private static final int NOCOLOR = 0x01010101;
    private static int actionBarColor = NOCOLOR;

    private static int getActionBarColor(Window window, int oldColor) {
        if (actionBarColor != NOCOLOR) return actionBarColor;

        TypedValue outValue = new TypedValue();
        window.getContext().getTheme().resolveAttribute(android.R.attr.actionBarStyle, outValue, true);
        TypedArray abStyle = window.getContext().getTheme().obtainStyledAttributes(outValue.resourceId, new int[] { android.R.attr.background });
        Drawable bg = abStyle.getDrawable(0);
        abStyle.recycle();

        if (bg instanceof ColorDrawable)
            return ((ColorDrawable)bg).getColor();
        else
            return oldColor;
    }

    @SuppressWarnings("unchecked")
    private static void hookToolbar(Object thisObject, Drawable bg) {
        if (!(bg instanceof ColorDrawable)) return;
        actionBarColor = ((ColorDrawable)bg).getColor();
        Object mDecorToolbar = XposedHelpers.getObjectField(thisObject, "mDecorToolbar");
        ViewGroup mToolbar = (ViewGroup)XposedHelpers.getObjectField(mDecorToolbar, "mToolbar");
        Context mDecorContext = mToolbar.getRootView().getContext();
        if (mDecorContext != null) {
            WeakReference<Context> mActivityContext = (WeakReference<Context>)XposedHelpers.getObjectField(mDecorContext, "mActivityContext");
            Context mContext = mActivityContext.get();
            if (mContext != null)
                ((Activity)mContext).getWindow().setStatusBarColor(actionBarColor);
        }
    }

    private static void hookWindowDecor(Object thisObject, Drawable bg) {
        if (!(bg instanceof ColorDrawable)) return;
        actionBarColor = ((ColorDrawable)bg).getColor();
        Activity mActivity = (Activity)XposedHelpers.getObjectField(thisObject, "mActivity");
        if (mActivity != null)
            mActivity.getWindow().setStatusBarColor(actionBarColor);
    }

    public static void StatusBarBackgroundHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.getClassLoader(), "generateLayout", "com.android.internal.policy.DecorView", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result;
            	Throwable throwable = null;
            	try {
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	try {
            		Object thisObject = chain.getThisObject();

            		                Window wnd = (Window)thisObject;
            		                int mStatusBarColor = XposedHelpers.getIntField(thisObject, "mStatusBarColor");
            		                if (mStatusBarColor == -16777216) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int newColor = getActionBarColor(wnd, mStatusBarColor);
            		                if (newColor != mStatusBarColor)
            		                    XposedHelpers.callMethod(thisObject, "setStatusBarColor", newColor);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.getClassLoader(), "setStatusBarColor", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (actionBarColor != NOCOLOR) args[0] = actionBarColor;
            		                else if (Color.alpha((int)args[0]) < 255) args[0] = Color.TRANSPARENT;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.app.ToolbarActionBar", lpparam.getClassLoader(), "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                hookToolbar(thisObject, (Drawable)args[0]);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", lpparam.getClassLoader(), "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                hookWindowDecor(thisObject, (Drawable)args[0]);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void StatusBarBackgroundCompatHook(PackageReadyParam lpparam) {
        boolean androidx = false;

        // androidx
        Method sbdMethod = null;
        Class<?> tabCls = findClassIfExists("androidx.appcompat.app.ToolbarActionBar", lpparam.getClassLoader());
        if (tabCls != null) sbdMethod = findMethodExactIfExists(tabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object[] args = XposedHelpers.getArgsArray(chain);
                	Object thisObject = chain.getThisObject();
                	try {

                		                    hookToolbar(thisObject, (Drawable)args[0]);


                		result = chain.proceed(args);
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });

        sbdMethod = null;
        Class<?> wdabCls = findClassIfExists("androidx.appcompat.app.WindowDecorActionBar", lpparam.getClassLoader());
        if (wdabCls != null) sbdMethod = findMethodExactIfExists(wdabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object[] args = XposedHelpers.getArgsArray(chain);
                	Object thisObject = chain.getThisObject();
                	try {

                		                    hookWindowDecor(thisObject, (Drawable)args[0]);


                		result = chain.proceed(args);
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });

        // old appcompat lib
        if (!androidx) {
            sbdMethod = null;
            Class<?> tabv7Cls = findClassIfExists("android.support.v7.internal.app.ToolbarActionBar", lpparam.getClassLoader());
            if (tabv7Cls != null) sbdMethod = findMethodExactIfExists(tabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    	Object result = null;
                    	Throwable throwable = null;
                    	Object[] args = XposedHelpers.getArgsArray(chain);
                    	Object thisObject = chain.getThisObject();
                    	try {

                    		                        hookToolbar(thisObject, (Drawable)args[0]);


                    		result = chain.proceed(args);
                    	} catch (Throwable t) {
                    		throwable = t;
                    		result = null;
                    	}
                    	return XposedHelpers.throwOrReturn(throwable, result);
                    }
                });

            sbdMethod = null;
            Class<?> wdabv7Cls = findClassIfExists("android.support.v7.internal.app.WindowDecorActionBar", lpparam.getClassLoader());
            if (wdabv7Cls != null) sbdMethod = findMethodExactIfExists(wdabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    	Object result = null;
                    	Throwable throwable = null;
                    	Object[] args = XposedHelpers.getArgsArray(chain);
                    	Object thisObject = chain.getThisObject();
                    	try {

                    		                        hookWindowDecor(thisObject, (Drawable)args[0]);


                    		result = chain.proceed(args);
                    	} catch (Throwable t) {
                    		throwable = t;
                    		result = null;
                    	}
                    	return XposedHelpers.throwOrReturn(throwable, result);
                    }
                });
        }
    }
}
