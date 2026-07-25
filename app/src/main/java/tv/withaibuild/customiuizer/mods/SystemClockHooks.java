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

public class SystemClockHooks {
    private static void initClockStyle(TextView mClock, String clockName) {
        Resources res = mClock.getResources();
        String subKey = "statusbar";
        boolean statusBarClock = clockName.equals("clock");
        if (!statusBarClock) {
            subKey = "cc";
        }
        boolean enableCustomFormat = !statusBarClock || MainModule.mPrefs.getBoolean("system_" + subKey + "_clock_customformat_enable");
        String customFormat = MainModule.mPrefs.getString("system_" + subKey + "_clock_customformat", "");
        boolean dualRows = enableCustomFormat && customFormat.contains("\n");
        if (statusBarClock) {
            float dimStep = 0.5f;
            int fontSize = MainModule.mPrefs.getInt("system_statusbar_clock_fontsize", 13);
            if (fontSize > 13) {
                mClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * dimStep);
            }
            if (dualRows) {
                mClock.setLineSpacing(0, 0.5 * fontSize > 8.5f ? 0.85f : 0.9f);
            }
            int align = MainModule.mPrefs.getStringAsInt("system_statusbar_clock_align", 1);
            if (align == 2) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
            else if (align == 3) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            else if (align == 4) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            }
            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_bold")) {
                mClock.setTypeface(Typeface.DEFAULT_BOLD);
            }
            int leftMargin = MainModule.mPrefs.getInt("system_statusbar_clock_leftmargin", 0);
            leftMargin = (int) Helpers.dp2px(leftMargin * dimStep);
            int rightMargin = MainModule.mPrefs.getInt("system_statusbar_clock_rightmargin", 0);
            rightMargin = (int) Helpers.dp2px(rightMargin * dimStep);
            int defaultVerticalOffset = 8;
            int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_clock_verticaloffset", defaultVerticalOffset);
            if (verticalOffset != defaultVerticalOffset) {
                float marginTop = Helpers.dp2px((verticalOffset - defaultVerticalOffset) * dimStep);
                mClock.setTranslationY(marginTop);
            }

            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_chip")) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mClock.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                if (leftMargin > 0) {
                    lp.leftMargin = leftMargin;
                }
                if (rightMargin > 0) {
                    lp.rightMargin = rightMargin;
                }
                mClock.setLayoutParams(lp);

                boolean useMonet = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_usemonet");
                boolean customTextColor = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor");

                int startColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_startcolor", 0x8F7C4DFF);
                int endColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_endcolor", 0x2FA7FFEB);
                if (useMonet) {
                    mClock.setTextColor(mClock.getResources().getColor(android.R.color.system_accent1_0, null));
                    startColor = mClock.getResources().getColor(android.R.color.system_accent1_600, null);
                    endColor = startColor;
                }
                else if (customTextColor) {
                    int textcolor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_textcolor", 0xFFFFFFFF);
                    mClock.setTextColor(textcolor);
                }
                GradientDrawable chipDrawable = new GradientDrawable();
                boolean verticalOrientation = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_orientation_vertical");
                chipDrawable.setOrientation(verticalOrientation ? GradientDrawable.Orientation.TOP_BOTTOM : GradientDrawable.Orientation.LEFT_RIGHT);
                chipDrawable.setColors(new int[]{startColor, endColor});
                chipDrawable.setShape(GradientDrawable.RECTANGLE);
                int horizPadding = MainModule.mPrefs.getInt("system_statusbar_clock_chip_horizpadding", 0);
                int vertPadding = MainModule.mPrefs.getInt("system_statusbar_clock_chip_verticalpadding", 0);
                if (horizPadding > 0) {
                    horizPadding = (int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        horizPadding,
                        res.getDisplayMetrics()
                    );
                }
                if (vertPadding > 0 || horizPadding > 0) {
                    chipDrawable.setPadding(horizPadding, vertPadding, horizPadding, vertPadding);
                }
                int radiusPx = MainModule.mPrefs.getInt("system_statusbar_clock_chip_radius", 0);
                if (radiusPx > 0) {
                    radiusPx = (int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        radiusPx,
                        res.getDisplayMetrics()
                    );
                    chipDrawable.setCornerRadius(radiusPx);
                }
                mClock.setBackground(chipDrawable);
            }
            else {
                if (leftMargin > 0 || rightMargin > 0) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mClock.getLayoutParams();
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    lp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    if (leftMargin > 0) {
                        lp.leftMargin = leftMargin;
                    }
                    if (rightMargin > 0) {
                        lp.rightMargin = rightMargin;
                    }
                    mClock.setLayoutParams(lp);
                }
            }
            int fixedWidth = MainModule.mPrefs.getInt("system_statusbar_clock_fixedcontent_width", 10);
            if (fixedWidth > 10) {
                ViewGroup.LayoutParams lp = mClock.getLayoutParams();
                lp.width = (int)(mClock.getResources().getDisplayMetrics().density * fixedWidth);
                mClock.setLayoutParams(lp);
            }
        }
        if (dualRows) {
            mClock.setSingleLine(false);
            mClock.setMaxLines(2);
        }
    }

    private static boolean getShowSeconds() {
        boolean sbShowSeconds = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_seconds");
        String customFormat = MainModule.mPrefs.getString("system_statusbar_clock_customformat", "");
        boolean enableCustomFormat = MainModule.mPrefs.getBoolean("system_statusbar_clock_customformat_enable");
        return (enableCustomFormat && customFormat.contains("ss")) || (!enableCustomFormat && sbShowSeconds);
    }

    private static boolean getCCShowSeconds() {
        String customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "");
        return customFormat.contains("ss");
    }

    private static final class SecondTicker implements Runnable {
        private final Object clockController;
        private final Context context;
        private final Handler handler;
        private boolean running;

        SecondTicker(Object clockController, Context context) {
            this.clockController = clockController;
            this.context = context;
            handler = new Handler(context.getMainLooper());
        }

        void start() {
            running = true;
            scheduleNextTick();
        }

        void stop() {
            running = false;
            handler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if (!running) return;
            try {
                Object calendar = XposedHelpers.getObjectField(clockController, "mCalendar");
                XposedHelpers.callMethod(calendar, "setTimeInMillis", java.lang.System.currentTimeMillis());
                XposedHelpers.setObjectField(
                    clockController,
                    "mIs24",
                    DateFormat.is24HourFormat(context)
                );
                ArrayList<Object> clockListeners =
                    (ArrayList<Object>) XposedHelpers.getObjectField(clockController, "mClockListeners");
                for (Object listener : clockListeners) {
                    View clock = (View) listener;
                    if (ModuleHelper.getViewInfo(clock, "showSeconds") != null) {
                        XposedHelpers.callMethod(clock, "updateTime");
                    }
                }
            } catch (Throwable t) {
                XposedHelpers.log("SecondTicker", t);
            }
            scheduleNextTick();
        }

        private void scheduleNextTick() {
            if (!running) return;
            long delay = 1000L - java.lang.System.currentTimeMillis() % 1000L;
            handler.postDelayed(this, delay);
        }
    }

    private static void initSecondTicker(Object clockController) {
        boolean ccShowSeconds = getCCShowSeconds();
        boolean finalSbShowSeconds = getShowSeconds();
        Context mContext = (Context) XposedHelpers.getObjectField(clockController, "mContext");
        SecondTicker previousTicker = (SecondTicker) XposedHelpers.getAdditionalInstanceField(
            clockController,
            "secondTicker"
        );
        if (previousTicker != null) {
            previousTicker.stop();
            XposedHelpers.removeAdditionalInstanceField(clockController, "secondTicker");
        }
        if (ccShowSeconds || finalSbShowSeconds) {
            SecondTicker ticker = new SecondTicker(clockController, mContext);
            XposedHelpers.setAdditionalInstanceField(clockController, "secondTicker", ticker);
            ticker.start();
        }
    }
    private static void initWeatherInfoHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.getClassLoader(), new MethodHook() {
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

            		                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
            		                Runnable mWeatherRunnable = new Runnable() {
            		                    @Override
            		                    public void run() {
            		                        XposedHelpers.callMethod(thisObject, "updateTime");
            		                    }
            		                };
            		                WeatherDataController.initContext(mContext, mWeatherRunnable);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

    }
    public static void StatusBarClockTweakHook(PackageReadyParam lpparam) {
        boolean enableWeatherParam = MainModule.mPrefs.getBoolean("system_statusbar_enable_weather_param");
        if (enableWeatherParam) {
            initWeatherInfoHook(lpparam);
        }
        boolean hideStatusbarClock = MainModule.mPrefs.getBoolean("system_statusbaricons_clock");
        boolean statusbarClockTweak = !hideStatusbarClock && MainModule.mPrefs.getBoolean("system_statusbar_clocktweak");
        boolean ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak");
        MethodHook ScheduleHook = new MethodHook() {
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

                                        initSecondTicker(thisObject);
            		                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
            		                if (getShowSeconds() || getCCShowSeconds()) {
            		                    BroadcastReceiver oldReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(thisObject, "customiuizer_timeSetReceiver");
            		                    if (oldReceiver != null) try { mContext.unregisterReceiver(oldReceiver); } catch (Throwable ignore) {}
            		                    BroadcastReceiver mUpdateTimeReceiver = new BroadcastReceiver() {
            		                        @Override
            		                        public void onReceive(Context context, Intent intent) {
                                                    initSecondTicker(thisObject);
            		                        }
            		                    };
            		                    XposedHelpers.setAdditionalInstanceField(thisObject, "customiuizer_timeSetReceiver", mUpdateTimeReceiver);
            		                    IntentFilter timeSetIntent = new IntentFilter();
            		                    timeSetIntent.addAction("android.intent.action.TIME_SET");
            		                    mContext.registerReceiver(mUpdateTimeReceiver, timeSetIntent, Context.RECEIVER_NOT_EXPORTED);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        if (ccClockTweak || statusbarClockTweak) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.getClassLoader(), ScheduleHook);
        }
        boolean hideDateView = MainModule.mPrefs.getBoolean("system_cc_hidedate");
        boolean hideDrawerDate = MainModule.mPrefs.getBoolean("system_drawer_hidedate");
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                final TextView clock = (TextView)thisObject;
            		                if (args.length != 3) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int clockId = Helpers.getResId(clock.getResources(), "clock", "id", "com.android.systemui");
            		                int bigClockId = Helpers.getResId(clock.getResources(), "big_time", "id", "com.android.systemui");
            		                int dateClockId = Helpers.getResId(clock.getResources(), "date_time", "id", "com.android.systemui");
            		                int horizDateClockId = Helpers.getResId(clock.getResources(), "horizontal_date_time", "id", "com.android.systemui");
            		                int thisClockId = clock.getId();
            		                if (clockId == thisClockId) {
            		                    ModuleHelper.setViewInfo(clock, "clockName", "clock");
            		                    if (statusbarClockTweak && getShowSeconds()) {
            		                        ModuleHelper.setViewInfo(clock, "showSeconds", true);
            		                    }
            		                }
            		                else if (bigClockId == thisClockId) {
            		                    ModuleHelper.setViewInfo(clock, "clockName", "ccClock");
            		                    if (ccClockTweak) {
            		                        if (getCCShowSeconds()) {
            		                            ModuleHelper.setViewInfo(clock, "showSeconds", true);
            		                        }
            		                        initClockStyle(clock, "ccClock");
            		                    }
            		                }
            		                else if (thisClockId == horizDateClockId) {
            		                    ModuleHelper.setViewInfo(clock, "clockName", "drawerDate");
            		                }
            		                else if (dateClockId == thisClockId) {
            		                    boolean ccDate = clock.getClass().getCanonicalName().contains("ControlCenterDateView");
            		                    if (ccDate) {
            		                        ModuleHelper.setViewInfo(clock, "clockName", "ccDate");
            		                    }
            		                    if (!ccDate) {
            		                        ModuleHelper.setViewInfo(clock, "clockName", "drawerDate");
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        final ThreadLocal<StringBuilder> clockFormatBuilder = new ThreadLocal<StringBuilder>() {
            @Override
            protected StringBuilder initialValue() { return new StringBuilder(32); }
        };
        final ThreadLocal<StringBuilder> clockTextBuilder = new ThreadLocal<StringBuilder>() {
            @Override
            protected StringBuilder initialValue() { return new StringBuilder(32); }
        };
        MethodHook updateTimeHook = new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                TextView clock = (TextView)thisObject;
            		                String clockName = (String) ModuleHelper.getViewInfo(clock, "clockName");
            		                Context mContext = clock.getContext();
            		                if (("ccDate".equals(clockName) && hideDateView)
            		                    || ("drawerDate".equals(clockName) && hideDrawerDate)
            		                    || ("clock".equals(clockName) && hideStatusbarClock)
            		                ) {
            		                    clock.setText("");
            		                    { skipped = true; result = null; throwable = null; }
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                }

            		                Object mMiuiStatusBarClockController = XposedHelpers.getObjectField(clock, "mMiuiStatusBarClockController");
            		                Object mCalendar = XposedHelpers.getObjectField(mMiuiStatusBarClockController, "mCalendar");
            		                String timeFmt = null;
            		                if ("ccClock".equals(clockName)) {
            		                    if (ccClockTweak) {
            		                        String customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "");
            		                        if (customFormat.length() > 0) {
            		                            timeFmt = customFormat;
            		                        }
            		                    }
            		                }
            		                else if ("ccDate".equals(clockName)) {
            		                    String ccDateFormat = MainModule.mPrefs.getString("system_cc_dateformat", "");
            		                    if (ccDateFormat.length() > 0) {
            		                        timeFmt = ccDateFormat;
            		                    }
            		                }
            		                else if ("drawerDate".equals(clockName)) {
            		                    String drawerDateFormat = MainModule.mPrefs.getString("system_drawer_dateformat", "");
            		                    if (drawerDateFormat.length() > 0) {
            		                        timeFmt = drawerDateFormat;
            		                    }
            		                }
            		                else if ("clock".equals(clockName) && statusbarClockTweak) {
            		                    String customFormat = MainModule.mPrefs.getString("system_statusbar_clock_customformat", "");
            		                    boolean enableCustomFormat = MainModule.mPrefs.getBoolean("system_statusbar_clock_customformat_enable");
            		                    enableCustomFormat = enableCustomFormat && (customFormat.length() > 0);
            		                    if (enableCustomFormat) {
            		                        timeFmt = customFormat;
            		                    }
            		                    else {
            		                        boolean showSeconds = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_seconds");
            		                        boolean is24 = MainModule.mPrefs.getBoolean("system_statusbar_clock_24hour_format");
            		                        boolean showAmpm = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_ampm");
            		                        boolean hourIn2d = MainModule.mPrefs.getBoolean("system_statusbar_clock_leadingzero");
            		                        String fmt;
            		                        if (showAmpm) {
            		                            fmt = "fmt_time_12hour_minute_pm";
            		                        }
            		                        else {
            		                            fmt = "fmt_time_12hour_minute";
            		                        }
            		                        int fmtResId = Helpers.getResId(mContext.getResources(), fmt, "string", "com.android.systemui");
            		                        timeFmt = mContext.getString(fmtResId);
            		                        if (showSeconds) {
            		                            int mmIdx = timeFmt.indexOf(":mm");
            		                            if (mmIdx >= 0) {
            		                                timeFmt = timeFmt.substring(0, mmIdx) + ":mm:ss" + timeFmt.substring(mmIdx + 3);
            		                            }
            		                        }
            		                        String hourStr = "h";
            		                        if (is24) {
            		                            hourStr = "H";
            		                        }
            		                        if (hourIn2d) {
            		                            hourStr = hourStr + hourStr;
            		                        }
            		                        int colonIdx = timeFmt.indexOf(':');
            		                        if (colonIdx > 0) {
            		                            timeFmt = hourStr + timeFmt.substring(colonIdx);
            		                        }
            		                    }
            		                }
            		                if (timeFmt != null) {
            		                    if (enableWeatherParam) {
            		                        String weatherInfo = WeatherDataController.weatherInfo;
            		                        if (weatherInfo != null) timeFmt = timeFmt.replace("tq", weatherInfo);
            		                    }
            		                    StringBuilder formatSb = clockFormatBuilder.get();
            		                    formatSb.setLength(0);
            		                    formatSb.append(timeFmt);
            		                    StringBuilder textSb = clockTextBuilder.get();
            		                    textSb.setLength(0);
            		                    XposedHelpers.callMethod(mCalendar, "format", mContext, textSb, formatSb);
            		                    clock.setText(textSb.toString());
            		                    { skipped = true; result = null; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "updateTime", updateTimeHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiStatusBarClock", lpparam.getClassLoader(), "updateTime", updateTimeHook);
        if (hideDateView || hideDrawerDate || hideStatusbarClock) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object thisObject = chain.getThisObject();
                	try {

                		                    TextView clock = (TextView)thisObject;
                		                    String clockName = (String) ModuleHelper.getViewInfo(clock, "clockName");
                		                    if (("ccDate".equals(clockName) && hideDateView)
                		                        || ("drawerDate".equals(clockName) && hideDrawerDate)
                		                        || ("clock".equals(clockName) && hideStatusbarClock)
                		                    ) {
                		                        XposedHelpers.setObjectField(thisObject, "mAttached", true);
                		                    }


                        result = chain.proceed();
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
        if (statusbarClockTweak) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
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

                		                    TextView clock = (TextView) XposedHelpers.getObjectField(thisObject, "mClock");
                		                    initClockStyle(clock, "clock");

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            boolean customTextColor = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor");
            boolean useMonet = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_usemonet");
            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_chip") && (customTextColor || useMonet)) {
                ModuleHelper.hookAllMethods("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "onDarkChanged", new MethodHook() {
                    @Override
                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    	boolean skipped = false;
                    	Object result = null;
                    	Throwable throwable = null;
                    	Object thisObject = chain.getThisObject();
                    	try {

                    		                        TextView clock = (TextView)thisObject;
                    		                        String clockName = (String) ModuleHelper.getViewInfo(clock, "clockName");
                    		                        if ("clock".equals(clockName)) {
                    		                            { skipped = true; result = null; throwable = null; }
                    		                        }

                    		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                            result = chain.proceed();
                    	} catch (Throwable t) {
                    		throwable = t;
                    		result = null;
                    	}
                    	return XposedHelpers.throwOrReturn(throwable, result);
                    }
                });
            }
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.FakeStatusBarClockController", lpparam.getClassLoader(), "initState", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	boolean skipped = false;
                	Object result = null;
                	Throwable throwable = null;
                	Object thisObject = chain.getThisObject();
                	try {

                		                    boolean useLeft = XposedHelpers.getBooleanField(thisObject, "useLeft");
                		                    if (!useLeft) {
                		                        Object mFakeClock = XposedHelpers.getObjectField(thisObject, "fakeStatusBarClock");
                		                        if (mFakeClock == null) { skipped = true; result = null; throwable = null; }
                		                    }

                		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                        result = chain.proceed();
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
    }

    public static void CCClockTweakHook(PackageReadyParam lpparam) {
        int ccClockSize = MainModule.mPrefs.getInt("system_cc_clock_fontsize", 9);
        if (ccClockSize > 9) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "qs_control_header_clock_size", ccClockSize);
        }
        MethodHook ccClockHook = new MethodHook() {
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

            		                TextView clock = (TextView)XposedHelpers.getObjectField(thisObject, "mBigTime");
            		                boolean ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak");
            		                boolean useSystemFonts = MainModule.mPrefs.getBoolean("system_qs_force_systemfonts");
            		                if (ccClockTweak) {
            		                    int defaultVerticalOffset = 10;
            		                    int verticalOffset = MainModule.mPrefs.getInt("system_cc_clock_verticaloffset", defaultVerticalOffset);
            		                    if (verticalOffset != defaultVerticalOffset) {
            		                        float marginTop = Helpers.dp2px(verticalOffset - defaultVerticalOffset);
            		                        clock.setTranslationY(marginTop);
            		                    }
            		                }
            		                if (useSystemFonts) {
            		                    clock.setTypeface(Typeface.DEFAULT);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateResources", ccClockHook);
    }
    public static void CCClockCenterAlignHook(PackageReadyParam lpparam) {
        boolean centerClock = MainModule.mPrefs.getBoolean("system_cc_clock_centeralign");
        boolean centerDate = !MainModule.mPrefs.getBoolean("system_drawer_hidedate") && MainModule.mPrefs.getBoolean("system_drawer_date_centeralign");
        MethodHook ccClockHook = new MethodHook() {
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

            		                TextView clock = (TextView)XposedHelpers.getObjectField(thisObject, "mBigTime");
            		                int mPolicyVisibility = XposedHelpers.getIntField(clock, "mPolicyVisibility");
            		                LinearLayout clockContainer = (LinearLayout) XposedHelpers.getObjectField(thisObject, "mNotificationHeaderClockContainer");
            		                if (mPolicyVisibility == 0 || mPolicyVisibility == 4) {
            		                    clockContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            		                }
            		                else {
            		                    clockContainer.setGravity(Gravity.START);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        if (centerClock) {
            ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateLayout", ccClockHook);
        }
        MethodHook clockMarginHook = new MethodHook() {
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

            		                if (centerClock) {
            		                    TextView clock = (TextView)XposedHelpers.getObjectField(thisObject, "mBigTime");
            		                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) clock.getLayoutParams();
            		                    lp.leftMargin = 0;
            		                    clock.setLayoutParams(lp);

            		                    Object mWeatherCity = ModuleHelper.getObjectFieldSilently(thisObject, "mWeatherCity");
            		                    if (!ModuleHelper.NOT_EXIST_SYMBOL.equals(mWeatherCity)) {
            		                        ViewGroup weatherContainer = (ViewGroup) ((View) mWeatherCity).getParent();
            		                        weatherContainer.setVisibility(View.GONE);
            		                    }
            		                }
            		                if (centerDate) {
            		                    TextView dateView = (TextView)XposedHelpers.getObjectField(thisObject, "mDateView");
            		                    LinearLayout dateContainer = (LinearLayout) dateView.getParent();
            		                    dateContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "onFinishInflate", clockMarginHook);
    }
}
