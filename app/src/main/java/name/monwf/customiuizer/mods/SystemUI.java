package name.monwf.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static name.monwf.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClassIfExists;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.WallpaperColors;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.MiuiMultiWindowUtils;
import android.util.Pair;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import miui.app.MiuiFreeFormManager;
import miui.os.Build;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import miui.telephony.TelephonyManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.mods.utils.HookerClassHelper;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.ResourceHooks;
import name.monwf.customiuizer.mods.utils.StepCounterController;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.BatteryIndicator;
import name.monwf.customiuizer.utils.Helpers;

public class SystemUI {
    private final static String StatusBarCls = "com.android.systemui.statusbar.phone.CentralSurfacesImpl";

    private static int statusbarTextIconLayoutResId = 0;
    private static int notifVolumeOnResId;
    private static int notifVolumeOffResId;

    public static boolean newStyle = false;
    private final static int textIconTagId = ResourceHooks.getFakeResId("text_icon_tag");
    private final static int viewInitedTag = ResourceHooks.getFakeResId("view_inited_tag");

    private static List<String> statusbarIconList;

    public static void setupStatusBar(Context mContext) {
        if (newStyle) {
            statusbarTextIconLayoutResId = MainModule.resHooks.addResource("statusbar_text_icon", R.layout.statusbar_text_icon_new);
        }
        else {
            statusbarTextIconLayoutResId = MainModule.resHooks.addResource("statusbar_text_icon", R.layout.statusbar_text_icon);
        }
        if (MainModule.mPrefs.getBoolean("system_statusbar_topmargin")) {
            int topMargin = MainModule.mPrefs.getInt("system_statusbar_topmargin_val", 1);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_padding_top", topMargin);
        }
        if (MainModule.mPrefs.getBoolean("system_statusbar_horizmargin")) {
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_padding_start", 0);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_padding_end", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_enable_style_switch")) {
            MainModule.resHooks.setObjectReplacement("com.android.systemui", "integer", "force_use_control_panel", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_qs_force_systemfonts")) {
            MainModule.resHooks.setObjectReplacement("com.android.systemui", "bool", "header_big_time_use_system_font", true);
        }
        if (MainModule.mPrefs.getBoolean("system_detailednetspeed_fakedualrow")) {
            MainModule.resHooks.setObjectReplacement("com.android.systemui", "string", "network_speed_suffix", "%1$s\n%2$s");
        }
        if (MainModule.mPrefs.getBoolean("system_compactnotif")) {
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_row_extra_padding", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_volumetimer")) {
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "array", "miui_volume_timer_segments", R.array.miui_volume_timer_segments);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_tile_roundedrect")) {
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_unavailable", R.drawable.ic_qs_tile_bg_disabled);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_disabled", R.drawable.ic_qs_tile_bg_disabled);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_warning", R.drawable.ic_qs_tile_bg_warning);
        }
        int iconSize = MainModule.mPrefs.getInt("system_statusbar_iconsize", 6);
        if (iconSize > 6) {
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_icon_size", iconSize);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_clock_size", iconSize + 0.4f);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size", iconSize);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size_dark", iconSize);
            float notifyPadding = 2.5f * iconSize / 13;
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_notification_icon_padding", notifyPadding);
            float iconHeight = 20.5f * iconSize / 13;
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_icon_height", iconHeight);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_show_stepcount")) {
            StepCounterController.initContext(mContext);
        }
        Settings.System.putLong(mContext.getContentResolver(), "systemui_restart_time", java.lang.System.currentTimeMillis());

        boolean swapWifiSignal = MainModule.mPrefs.getBoolean("system_statusbaricons_swap_wifi_mobile");
        boolean moveSignalLeft = MainModule.mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft");
        if (swapWifiSignal || moveSignalLeft) {
            int resIconsId = mContext.getResources().getIdentifier("config_statusBarIcons", "array", "com.android.systemui");
            statusbarIconList = Arrays.asList(mContext.getResources().getStringArray(resIconsId));
        }
    }

    private static String getSlotNameByType(int mIconType) {
        String slotName = "";
        if (mIconType == 91) {
            slotName = "battery_info";
        }
        else if (mIconType == 92) {
            slotName = "device_temp";
        }
        return slotName;
    }

    static class TextIcon {
        public boolean atRight;
        public int iconType;
        public TextIcon(boolean mAtRight, int mIconType) {
            atRight = mAtRight;
            iconType = mIconType;
        }
    }
    public static void MonitorDeviceInfoHook(PackageLoadedParam lpparam) {
        class TextIconInfo {
            public boolean iconShow;
            public int iconType;
            public String iconText;
        }
        boolean showBatteryDetail = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent");
        boolean showDeviceTemp = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature");
        Class <?> ChargeUtilsClass = null;
        if (showBatteryDetail) {
            ChargeUtilsClass = findClassIfExists("com.android.keyguard.charge.ChargeUtils", lpparam.getClassLoader());
        }
        Class<?> finalChargeUtilsClass = ChargeUtilsClass;
        Class <?> DarkIconDispatcherClass = findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.getClassLoader());
        Class <?> Dependency = findClass("com.android.systemui.Dependency", lpparam.getClassLoader());
        Class <?> StatusBarIconHolder = findClass("com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.getClassLoader());
        boolean batteryAtRight = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        boolean tempAtRight = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");
        ArrayList<TextIcon> textIcons = new ArrayList<>();
        if (showBatteryDetail) {
            textIcons.add(new TextIcon(batteryAtRight, 91));
        }
        if (showDeviceTemp) {
            textIcons.add(new TextIcon(tempAtRight, 92));
        }

        boolean hasRightIcon = false;
        boolean hasLeftIcon = false;
        for (TextIcon ti:textIcons) {
            if (ti.atRight) {
                hasRightIcon = true;
            }
            else {
                hasLeftIcon = true;
            }
        }
        if (hasRightIcon && !MainModule.mPrefs.getBoolean("system_statusbar_dualrows")) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Object iconController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarIconController");
                    for (TextIcon ti:textIcons) {
                        if (ti.atRight) {
                            int slotIndex = (int) XposedHelpers.callMethod(iconController, "getSlotIndex", getSlotNameByType(ti.iconType));
                            Object iconHolder = XposedHelpers.callMethod(iconController, "getIcon", slotIndex, 0);
                            if (iconHolder == null) {
                                iconHolder = XposedHelpers.newInstance(StatusBarIconHolder);
                                XposedHelpers.setObjectField(iconHolder, "mType", ti.iconType);
                                XposedHelpers.callMethod(iconController, "setIcon", slotIndex, iconHolder);
                            }
                        }
                    }
                }
            });

            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconController$IconManager", lpparam.getClassLoader(), "addHolder", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    if (param.getArgs().length != 4) return;
                    Object iconHolder = param.getArgs()[3];
                    int type = (int) XposedHelpers.callMethod(iconHolder, "getType");
                    if (type == 91 || type == 92) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) XposedHelpers.callMethod(param.getThisObject(), "onCreateLayoutParams");
                        TextIcon createIcon = null;
                        for (TextIcon ti:textIcons) {
                            if (ti.iconType == type) {
                                createIcon = ti;
                                break;
                            }
                        }
                        View iconView = createStatusbarTextIcon(mContext, lp, createIcon);
                        int i = (int) param.getArgs()[0];
                        ViewGroup mGroup = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mGroup");
                        mGroup.addView(iconView, i);
                        mStatusbarTextIcons.add(iconView);
                        param.returnAndSkip(iconView);
                    }
                }
            });
        }
        if (hasLeftIcon) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getContext");
                    Object DarkIconDispatcher = XposedHelpers.callStaticMethod(Dependency, "get", DarkIconDispatcherClass);
                    View baseAnchor;
                    if (newStyle) {
                        baseAnchor = (View)XposedHelpers.getObjectField(param.getThisObject(), "mClockView");
                    }
                    else {
                        baseAnchor = (View)XposedHelpers.getObjectField(param.getThisObject(), "mDripNetworkSpeedSplitter");
                    }
                    ViewGroup leftIconsContainer = (ViewGroup) baseAnchor.getParent();
                    int bvIndex = leftIconsContainer.indexOfChild(baseAnchor);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) baseAnchor.getLayoutParams();
                    for (TextIcon ti:textIcons) {
                        if (!ti.atRight) {
                            View iconView = createStatusbarTextIcon(mContext, lp, ti);
                            leftIconsContainer.addView(iconView, bvIndex + 1);
                            mStatusbarTextIcons.add(iconView);
                            XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", iconView);
                        }
                    }
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "showSystemIconArea", boolean.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    for (View iconView:mStatusbarTextIcons) {
                        Object tagData = iconView.getTag(textIconTagId);
                        if (tagData != null) {
                            TextIcon ti = (TextIcon) tagData;
                            if (!ti.atRight) {
                                XposedHelpers.callMethod(iconView, "setVisibilityByController", true);
                            }
                        }
                    }
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "hideSystemIconArea", boolean.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    for (View iconView:mStatusbarTextIcons) {
                        Object tagData = iconView.getTag(textIconTagId);
                        if (tagData != null) {
                            TextIcon ti = (TextIcon) tagData;
                            if (!ti.atRight) {
                                XposedHelpers.callMethod(iconView, "setVisibilityByController", false);
                            }
                        }
                    }
                }
            });
        }
        Class<?> NetworkSpeedViewClass = findClass("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(NetworkSpeedViewClass, "getSlot", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View nsView = (View) param.getThisObject();
                Object tagData = nsView.getTag(textIconTagId);
                if (tagData != null) {
                    TextIcon ti = (TextIcon) tagData;
                    param.returnAndSkip(getSlotNameByType(ti.iconType));
                }
            }
        });
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), new MethodHook() {
            Handler mBgHandler;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) param.getArgs()[0];
                final Handler mHandler = new Handler(Looper.getMainLooper()) {
                    public void handleMessage(Message message) {
                        if (message.what == 100021) {
                            TextIconInfo tii = (TextIconInfo) message.obj;
                            for (View tv : mStatusbarTextIcons) {
                                Object tagData = tv.getTag(textIconTagId);
                                if (tagData != null) {
                                    TextIcon ti = (TextIcon) tagData;
                                    if (tii.iconType == ti.iconType) {
                                        XposedHelpers.callMethod(tv, "setBlocked", !tii.iconShow);
                                        if (tii.iconShow) {
                                            if (newStyle) {
                                                XposedHelpers.callMethod(tv, "setNetworkSpeed", tii.iconText, "");
                                            }
                                            else {
                                                XposedHelpers.callMethod(tv, "setNetworkSpeed", tii.iconText);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                };
                mBgHandler = new Handler((Looper) param.getArgs()[1]) {
                    public void handleMessage(Message message) {
                        if (message.what == 200021) {
                            String batteryInfo = "";
                            String deviceInfo = "";
                            boolean showBatteryInfo = showBatteryDetail;
                            if (showBatteryInfo && MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_incharge") && finalChargeUtilsClass != null) {
                                Object batteryStatus = ModuleHelper.getStaticObjectFieldSilently(finalChargeUtilsClass, "sBatteryStatus");
                                if (ModuleHelper.NOT_EXIST_SYMBOL.equals(batteryStatus)) {
                                    showBatteryInfo = false;
                                } else {
                                    showBatteryInfo = (boolean) XposedHelpers.callMethod(batteryStatus, "isCharging");
                                }
                            }
                            PowerManager powerMgr = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                            boolean isScreenOn = powerMgr.isInteractive();
                            if (isScreenOn) {
                                Properties props = null;
                                String cpuProps = null;
                                FileInputStream fis = null;
                                RandomAccessFile cpuReader = null;
                                try {
                                    fis = new FileInputStream("/sys/class/power_supply/battery/uevent");
                                    props = new Properties();
                                    props.load(fis);
                                    if (showDeviceTemp) {
                                        cpuReader = new RandomAccessFile("/sys/devices/virtual/thermal/thermal_zone0/temp", "r");
                                        cpuProps = cpuReader.readLine();
                                    }
                                } catch (Throwable ign) {
                                } finally {
                                    try {
                                        if (fis != null) {
                                            fis.close();
                                        }
                                        if (cpuReader != null) {
                                            cpuReader.close();
                                        }
                                    } catch (Throwable ign) {
                                    }
                                }
                                if (showBatteryInfo && props != null) {
                                    int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1);
                                    String simpleTempVal = "";
                                    if (opt == 1 || opt == 4) {
                                        boolean decimal = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_temp_decimal");
                                        int tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
                                        if (decimal) {
                                            simpleTempVal = tempVal / 10f + "";
                                        }
                                        else {
                                            simpleTempVal = tempVal % 10 == 0 ? (tempVal / 10 + "") : (tempVal / 10f + "");
                                        }
                                    }
                                    String currVal = "";
                                    String preferred = "mA";
                                    float currentRatio = 1000f;
                                    if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_fixcurrentratio")) {
                                        currentRatio = 1f;
                                    }
                                    int rawCurr = -1 * Math.round(Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW")) / currentRatio);
                                    if (opt == 1 || opt == 3 || opt == 5) {
                                        if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_positive")) {
                                            rawCurr = Math.abs(rawCurr);
                                        }
                                        if (Math.abs(rawCurr) > 999) {
                                            currVal = String.format("%.2f", rawCurr / 1000f);
                                            preferred = "A";
                                        } else {
                                            currVal = "" + rawCurr;
                                        }
                                    }
                                    int hideUnit = MainModule.mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_hideunit", 0);
                                    String tempUnit = (hideUnit == 1 || hideUnit == 2) ? "" : "℃";
                                    String powerUnit = (hideUnit == 1 || hideUnit == 3) ? "" : "W";
                                    String currUnit = (hideUnit == 1 || hideUnit == 3) ? "" : preferred;
                                    if (opt == 1) {
                                        String splitChar = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow") ? " " : "\n";
                                        batteryInfo = simpleTempVal + tempUnit + splitChar + currVal + currUnit;
                                        if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            batteryInfo = currVal + currUnit + splitChar + simpleTempVal + tempUnit;
                                        }
                                    }
                                    else if (opt == 4) {
                                        float voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f;
                                        String simpleWatt = String.format(Locale.getDefault(), "%.2f", Math.abs(voltVal * rawCurr) / 1000);
                                        String splitChar = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow") ? " " : "\n";
                                        batteryInfo = simpleTempVal + tempUnit + splitChar + simpleWatt + powerUnit;
                                        if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            batteryInfo = simpleWatt + powerUnit + splitChar + simpleTempVal + tempUnit;
                                        }
                                    } else if (opt == 2) {
                                        float voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f;
                                        String simpleWatt = String.format(Locale.getDefault(), "%.2f", Math.abs(voltVal * rawCurr) / 1000);
                                        batteryInfo = simpleWatt + powerUnit;
                                    } else if (opt == 5) {
                                        float voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f;
                                        String simpleWatt = String.format(Locale.getDefault(), "%.2f", Math.abs(voltVal * rawCurr) / 1000);
                                        String splitChar = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow") ? " " : "\n";
                                        batteryInfo = currVal + currUnit + splitChar + simpleWatt + powerUnit;
                                        if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            batteryInfo = simpleWatt + powerUnit + splitChar + currVal + currUnit;
                                        }
                                    }
                                    else {
                                        batteryInfo = currVal + currUnit;
                                    }
                                }
                                if (showDeviceTemp && props != null && cpuProps != null) {
                                    int batteryTempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
                                    int cpuTempVal = Integer.parseInt(cpuProps);
                                    String simpleBatteryTemp = String.format(Locale.getDefault(), "%.1f", batteryTempVal / 10f);
                                    String simpleCpuTemp = String.format(Locale.getDefault(), "%.1f", cpuTempVal / 1000f);
                                    int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_showdevicetemperature_content", 1);
                                    boolean hideUnit = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_hideunit");
                                    String tempUnit = hideUnit ? "" : "℃";
                                    if (opt == 1) {
                                        String splitChar = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_singlerow")
                                            ? " " : "\n";
                                        deviceInfo = simpleBatteryTemp + tempUnit + splitChar + simpleCpuTemp + tempUnit;
                                        if (MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_reverseorder")) {
                                            deviceInfo = simpleCpuTemp + tempUnit + splitChar + simpleBatteryTemp + tempUnit;
                                        }
                                    } else if (opt == 2) {
                                        deviceInfo = simpleBatteryTemp + tempUnit;
                                    } else {
                                        deviceInfo = simpleCpuTemp + tempUnit;
                                    }
                                }
                                if (showBatteryDetail) {
                                    TextIconInfo tii = new TextIconInfo();
                                    tii.iconShow = showBatteryInfo;
                                    tii.iconText = batteryInfo;
                                    tii.iconType = 91;
                                    mHandler.obtainMessage(100021, tii).sendToTarget();
                                }
                                if (showDeviceTemp) {
                                    TextIconInfo tii = new TextIconInfo();
                                    tii.iconShow = true;
                                    tii.iconText = deviceInfo;
                                    tii.iconType = 92;
                                    mHandler.obtainMessage(100021, tii).sendToTarget();
                                }
                            }
                        }
                        mBgHandler.removeMessages(200021);
                        mBgHandler.sendEmptyMessageDelayed(200021, 2000);
                    }
                };
                mBgHandler.sendEmptyMessage(200021);
            }
        });
    }

    private static TextView getIconTextView(View iconView) {
        if (newStyle) {
            return (TextView) XposedHelpers.getObjectField(iconView, "mNetworkSpeedNumberText");
        }
        return (TextView) iconView;
    }

    private static void initStatusbarTextIcon(Context mContext, LinearLayout.LayoutParams lp, TextIcon ti, View iconView) {
        XposedHelpers.setObjectField(iconView, "mVisibleByController", true);
        XposedHelpers.setObjectField(iconView, "mShown", true);
        TextView iconTextView = getIconTextView(iconView);
        Resources res = mContext.getResources();
        int styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
        iconTextView.setTextAppearance(styleId);
        String subKey = "";
        if (ti.iconType == 91) {
            subKey = "batterytempandcurrent";
        }
        else if (ti.iconType == 92) {
            subKey = "showdevicetemperature";
        }
        float fontSize = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_fontsize", 16) * 0.5f;
        int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_" + subKey + "_content", 1);
        if ((opt == 1 || opt == 4 || opt == 5) && !MainModule.mPrefs.getBoolean("system_statusbar_" + subKey + "_singlerow")) {
            iconTextView.setMaxLines(2);
            iconTextView.setLineSpacing(0, fontSize > 8.5f ? 0.85f : 0.9f);
        }
        iconTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        if (MainModule.mPrefs.getBoolean("system_statusbar_" + subKey + "_bold")) {
            iconTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        int leftMargin = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_leftmargin", 8);
        leftMargin = (int)Helpers.dp2px(leftMargin * 0.5f);
        int rightMargin = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_rightmargin", 8);
        rightMargin = (int)Helpers.dp2px(rightMargin * 0.5f);
        int topMargin = 0;
        int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_verticaloffset", 8);
        if (verticalOffset != 8) {
            topMargin = (int)Helpers.dp2px((verticalOffset - 8) * 0.5f);
        }
        iconTextView.setPaddingRelative(leftMargin, topMargin, rightMargin, 0);
        int fixedWidth = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_fixedcontent_width", 10);
        if (fixedWidth > 10) {
            lp.width = (int)Helpers.dp2px(fixedWidth);
        }
        iconTextView.setLayoutParams(lp);

        int align = MainModule.mPrefs.getStringAsInt("system_statusbar_" + subKey + "_align", 1);
        if (align == 2) {
            iconTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        else if (align == 3) {
            iconTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else if (align == 4) {
            iconTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
    }

    private static View createStatusbarTextIcon(Context mContext, LinearLayout.LayoutParams lp, TextIcon ti) {
        View iconView = LayoutInflater.from(mContext).inflate(statusbarTextIconLayoutResId, null);
        iconView.setTag(textIconTagId, ti);
        if (!newStyle) {
            XposedHelpers.setObjectField(iconView, "mVisibilityByDisableInfo", 0);
        }
        else {
            View mNumber = iconView.findViewWithTag("network_speed_number");
            XposedHelpers.setObjectField(iconView, "mNetworkSpeedNumberText", mNumber);
            View mUnit = iconView.findViewWithTag("network_speed_unit");
            XposedHelpers.setObjectField(iconView, "mNetworkSpeedUnitText", mUnit);
        }
        initStatusbarTextIcon(mContext, lp, ti, iconView);
        return iconView;
    }
    static final ArrayList<View> mStatusbarTextIcons = new ArrayList<View>();

    public static void AddCustomTileHook(PackageLoadedParam lpparam) {
        final boolean enable5G = MainModule.mPrefs.getBoolean("system_fivegtile");
        final boolean enableFps = MainModule.mPrefs.getBoolean("system_cc_fpstile");
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            private boolean isListened = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!isListened) {
                    isListened = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext");
                    int stockTilesResId = mContext.getResources().getIdentifier("miui_quick_settings_tiles_stock", "string", lpparam.getPackageName());
                    String stockTiles = mContext.getString(stockTilesResId);
                    if (enable5G) {
                        stockTiles = stockTiles  + ",custom_5G";
                    }
                    if (enableFps) {
                        stockTiles = stockTiles + ",custom_FPS";
                    }
                    MainModule.resHooks.setObjectReplacement("com.android.systemui", "string", "miui_quick_settings_tiles_stock", stockTiles);
                    MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "string", "miui_quick_settings_tiles_stock", stockTiles);
                    MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "string", "quick_settings_tiles_stock", stockTiles);
                }
            }
        });
        String QSFactoryCls = "com.android.systemui.qs.tileimpl.MiuiQSFactory";
        Class<?> ResourceIconClass = findClass("com.android.systemui.qs.tileimpl.QSTileImpl$ResourceIcon", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(QSFactoryCls, lpparam.getClassLoader(), "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) param.getArgs()[0];
                if (tileName.startsWith("custom_")) {
                    String nfcField = "nfcTileProvider";
                    Object provider = XposedHelpers.getObjectField(param.getThisObject(), nfcField);
                    Object tile = XposedHelpers.callMethod(provider, "get");
                    XposedHelpers.setAdditionalInstanceField(tile, "customName", tileName);
                    param.returnAndSkip(tile);
                }
            }
        });
        String NfcTileCls = "com.android.systemui.qs.tiles.MiuiNfcTile";
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "isAvailable", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    if ("custom_5G".equals(tileName)) {
                        param.returnAndSkip(enable5G && TelephonyManager.getDefault().isFiveGCapable());
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        param.returnAndSkip(enableFps);
                    }
                    else {
                        param.returnAndSkip(false);
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "getTileLabel", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    Resources modRes = ModuleHelper.getModuleRes(mContext);
                    if ("custom_5G".equals(tileName)) {
                        param.returnAndSkip(modRes.getString(R.string.qs_toggle_5g));
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        param.returnAndSkip(modRes.getString(R.string.qs_toggle_fps));
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "handleSetListening", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    if ("custom_5G".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        boolean mListening = (boolean) param.getArgs()[0];
                        if (mListening) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean z) {
                                    XposedHelpers.callMethod(param.getThisObject(), "refreshState");
                                }
                            };
                            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, contentObserver);
                            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("dual_nr_enabled"), false, contentObserver);
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "tileListener", contentObserver);
                        }
                        else {
                            ContentObserver contentObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "tileListener");
                            mContext.getContentResolver().unregisterContentObserver(contentObserver);
                        }
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        boolean mListening = (boolean) param.getArgs()[0];
                        if (mListening) {
                            Class<?> ServiceManager = findClass("android.os.ServiceManager", lpparam.getClassLoader());
                            Object mSurfaceFlinger = XposedHelpers.callStaticMethod(ServiceManager, "getService", "SurfaceFlinger");
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger", mSurfaceFlinger);
                        }
                        else {
                            XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger");
                        }
                    }

                    param.returnAndSkip(null);
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "getLongClickIntent", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    if ("custom_5G".equals(tileName)) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.settings.MiuiFiveGNetworkSetting"));
                        param.returnAndSkip(intent);
                    }
                    else {
                        param.returnAndSkip(null);
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "handleClick", View.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    if ("custom_5G".equals(tileName)) {
                        TelephonyManager manager = TelephonyManager.getDefault();
                        manager.setUserFiveGEnabled(!manager.isUserFiveGEnabled());
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        IBinder mSurfaceFlinger = (IBinder) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger");
                        if (mSurfaceFlinger != null) {
                            Object mState = XposedHelpers.getObjectField(param.getThisObject(), "mState");
                            boolean enabled = XposedHelpers.getBooleanField(mState, "value");
                            Parcel obtain = Parcel.obtain();
                            obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                            obtain.writeInt(enabled ? 0 : 1);
                            mSurfaceFlinger.transact(1034, obtain, null, 0);
                            obtain.recycle();
                            XposedHelpers.callMethod(param.getThisObject(), "refreshState");
                        }
                    }
                    param.returnAndSkip(null);
                }
            }
        });

        ArrayMap<String, Integer> tileOnResMap =  new ArrayMap<String, Integer>();
        ArrayMap<String, Integer> tileOffResMap =  new ArrayMap<String, Integer>();
        if (enable5G) {
            tileOnResMap.put("custom_5G", MainModule.resHooks.addResource("ic_qs_m5g_on", R.drawable.ic_qs_5g_on));
            tileOffResMap.put("custom_5G", MainModule.resHooks.addResource("ic_qs_m5g_off", R.drawable.ic_qs_5g_off));
        }
        if (enableFps) {
            tileOnResMap.put("custom_FPS", MainModule.resHooks.addResource("ic_qs_mfps_on", R.drawable.ic_qs_fps_on));
            tileOffResMap.put("custom_FPS", MainModule.resHooks.addResource("ic_qs_mfps_off", R.drawable.ic_qs_fps_off));
        }
        ModuleHelper.hookAllMethods(NfcTileCls, lpparam.getClassLoader(), "handleUpdateState", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    boolean isEnable = false;
                    if ("custom_5G".equals(tileName)) {
                        TelephonyManager manager = TelephonyManager.getDefault();
                        isEnable = manager.isUserFiveGEnabled();
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        IBinder mSurfaceFlinger = (IBinder) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger");
                        if (mSurfaceFlinger == null) {
                            isEnable = false;
                        }
                        else {
                            Parcel obtain = Parcel.obtain();
                            Parcel obtain2 = Parcel.obtain();
                            obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                            obtain.writeInt(2);
                            mSurfaceFlinger.transact(1034, obtain, obtain2, 0);
                            isEnable = obtain2.readBoolean();
                            obtain2.recycle();
                            obtain.recycle();
                        }
                    }
                    if (tileName.startsWith("custom_")) {
                        Object booleanState = param.getArgs()[0];
                        XposedHelpers.setObjectField(booleanState, "value", isEnable);
                        XposedHelpers.setObjectField(booleanState, "state", isEnable ? 2 : 1);
                        String tileLabel = (String) XposedHelpers.callMethod(param.getThisObject(), "getTileLabel");
                        XposedHelpers.setObjectField(booleanState, "label", tileLabel);
                        XposedHelpers.setObjectField(booleanState, "contentDescription", tileLabel);
                        XposedHelpers.setObjectField(booleanState, "expandedAccessibilityClassName", Switch.class.getName());
                        Object mIcon = XposedHelpers.callStaticMethod(ResourceIconClass, "get", isEnable ? tileOnResMap.get(tileName) : tileOffResMap.get(tileName));
                        XposedHelpers.setObjectField(booleanState, "icon", mIcon);
                    }
                    param.returnAndSkip(null);
                }
            }
        });
    }

    public static void DualRowStatusbarHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int firstRowLeftPadding = 0;
                int firstRowRightPadding = 0;
                if (MainModule.mPrefs.getBoolean("system_statusbar_dualrows_firstrow_horizmargin")) {
                    firstRowLeftPadding = MainModule.mPrefs.getInt("system_statusbar_dualrows_firstrow_horizmargin_left", 0);
                    firstRowRightPadding = MainModule.mPrefs.getInt("system_statusbar_dualrows_firstrow_horizmargin_right", 0);
                }
                boolean clock2Rows = MainModule.mPrefs.getBoolean("system_statusbar_dualrows_clock_span2rows");
                FrameLayout sbView = (FrameLayout) param.getThisObject();
                Context mContext = sbView.getContext();
                LinearLayout leftContainer = (LinearLayout) XposedHelpers.getObjectField(sbView, "mStatusBarLeftContainer");
                LinearLayout statusBarcontents = (LinearLayout) leftContainer.getParent();
                LinearLayout leftLayout = new LinearLayout(mContext);
                LinearLayout rightLayout = new LinearLayout(mContext);
                statusBarcontents.addView(leftLayout, 0);
                statusBarcontents.addView(rightLayout);
                LinearLayout leftGroup;

                if (clock2Rows) {
                    TextView mMiuiClock = (TextView) XposedHelpers.getObjectField(sbView, "mMiuiClock");
                    leftContainer.removeView(mMiuiClock);
                    leftGroup = new LinearLayout(mContext);
                    leftLayout.addView(mMiuiClock);
                    leftLayout.addView(leftGroup);
                    leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                    LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(0, -1, 1);
                    leftGroup.setLayoutParams(groupLp);
                }
                else {
                    leftGroup = leftLayout;
                    if (firstRowLeftPadding > 0) {
                        leftContainer.setPaddingRelative(firstRowLeftPadding, 0, 0, 0);
                    }
                }
                statusBarcontents.removeView(leftContainer);
                leftGroup.addView(leftContainer);
                LinearLayout secondLeft = new LinearLayout(mContext);
                leftGroup.addView(secondLeft);
                leftLayout.setId(leftContainer.getId());
                leftContainer.setId(View.NO_ID);
                XposedHelpers.setObjectField(sbView, "mStatusBarLeftContainer", leftLayout);

                ViewGroup rightContainer = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea");
                View mFullscreenStatusBarNotificationIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mFullscreenStatusBarNotificationIconArea");
                rightContainer.removeView(mFullscreenStatusBarNotificationIconArea);
                secondLeft.addView(mFullscreenStatusBarNotificationIconArea);
                View mDripStatusBarNotificationIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mDripStatusBarNotificationIconArea");
                leftContainer.removeView(mDripStatusBarNotificationIconArea);
                secondLeft.addView(mDripStatusBarNotificationIconArea);

                leftGroup.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(-1, 0, 1);
                leftContainer.setLayoutParams(leftLp);
                secondLeft.setLayoutParams(leftLp);
                secondLeft.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                rightLayout.setId(rightContainer.getId());
                XposedHelpers.setObjectField(param.getThisObject(), "mSystemIconArea", rightLayout);
                LinearLayout firstRight = new LinearLayout(mContext);
                rightLayout.addView(firstRight);
                firstRight.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                if (firstRowRightPadding > 0) {
                    firstRight.setPaddingRelative(0, 0, firstRowRightPadding, 0);
                }
                LinearLayout secondRight = new LinearLayout(mContext);
                rightLayout.addView(secondRight);
                secondRight.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

                rightLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(-1, 0, 1);
                firstRight.setLayoutParams(rightLp);
                secondRight.setLayoutParams(rightLp);

                int resSystemIconsId = sbView.getResources().getIdentifier("system_icons", "id", lpparam.getPackageName());
                int rightChildCount = rightContainer.getChildCount();
                for (int i = rightChildCount - 1; i >= 0; i--) {
                    View child = rightContainer.getChildAt(i);
                    if (child.getId() != resSystemIconsId) {
                        rightContainer.removeView(child);
                        firstRight.addView(child, 0);
                    }
                }

                View mStatusBarStatusIcons = (View) XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarStatusIcons");
                ((ViewGroup) mStatusBarStatusIcons.getParent()).removeView(mStatusBarStatusIcons);
                firstRight.addView(mStatusBarStatusIcons, 0);
                firstRight.setId(resSystemIconsId);

                View mBattery = (View) XposedHelpers.getObjectField(param.getThisObject(), "mBattery");
                ((ViewGroup) mBattery.getParent()).removeView(mBattery);
                secondRight.addView(mBattery);

                boolean showBatteryDetail = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent");
                boolean showDeviceTemp = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature");
                boolean batteryAtRight = showBatteryDetail && MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
                boolean tempAtRight = showDeviceTemp && MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");
                ArrayList<TextIcon> textIcons = new ArrayList<>();
                if (batteryAtRight) {
                    textIcons.add(new TextIcon(true, 91));
                }
                if (tempAtRight) {
                    textIcons.add(new TextIcon(true, 92));
                }
                if (textIcons.size() > 0) {
                    Class <?> DarkIconDispatcherClass = findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.getClassLoader());
                    Class <?> Dependency = findClass("com.android.systemui.Dependency", lpparam.getClassLoader());
                    Object DarkIconDispatcher = XposedHelpers.callStaticMethod(Dependency, "get", DarkIconDispatcherClass);
                    for (TextIcon ti:textIcons) {
                        View iconView = createStatusbarTextIcon(mContext, new LinearLayout.LayoutParams(-2, -2), ti);
                        secondRight.addView(iconView, 0);
                        mStatusbarTextIcons.add(iconView);
                        XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", iconView);
                    }
                }

                if (MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow") && !newStyle) {
                    View mDripNetworkSpeedView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mDripNetworkSpeedView");
                    leftContainer.removeView(mDripNetworkSpeedView);
                    secondRight.addView(mDripNetworkSpeedView, 0);
                }

                statusBarcontents.removeView(rightContainer);

                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "leftLayout", leftLayout);
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "rightLayout", rightLayout);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "updateCutoutLocation", new MethodHook(-1000) {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.getThisObject(), "mCurrentStatusBarType");
                LinearLayout leftLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "leftLayout");
                LinearLayout rightLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "rightLayout");

                if (mCurrentStatusBarType == 0) {
                    LinearLayout.LayoutParams leftLayoutLp = new LinearLayout.LayoutParams(0, -1, 4);
                    leftLayout.setLayoutParams(leftLayoutLp);
                    LinearLayout.LayoutParams rightLayoutLp = new LinearLayout.LayoutParams(0, -1, 6);
                    rightLayout.setLayoutParams(rightLayoutLp);
                }
                else {
                    LinearLayout.LayoutParams leftLayoutLp = new LinearLayout.LayoutParams(0, -1, 1);
                    leftLayout.setLayoutParams(leftLayoutLp);
                    LinearLayout.LayoutParams rightLayoutLp = new LinearLayout.LayoutParams(0, -1, 1);
                    rightLayout.setLayoutParams(rightLayoutLp);
                }

            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "showSystemIconArea", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBar");
                View rightLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "rightLayout");
                View leftLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "leftLayout");
                leftLayout.setVisibility(LinearLayout.VISIBLE);
                rightLayout.setVisibility(LinearLayout.VISIBLE);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "hideSystemIconArea", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBar");
                View rightLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "rightLayout");
                View leftLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "leftLayout");
                leftLayout.setVisibility(LinearLayout.GONE);
                rightLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }

    public static void DualRowSignalHook(PackageLoadedParam lpparam) {
        boolean mobileTypeSingle = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single");
        if (!mobileTypeSingle) {
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_half_to_top_distance", 3);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_mobile_left_inout_over_strength", 0);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_middle_to_strength_start", -0.4f);
        }

        HashMap<String, Integer> dualSignalResMap = new HashMap<String, Integer>();
        String[] colorModeList = {"", "dark", "tint"};
//        String[] iconStyles = {"", "thick", "theme"};
        String selectedIconStyle = MainModule.mPrefs.getString("system_statusbar_dualsimin2rows_style", "");

        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext");
                    Resources modRes = ModuleHelper.getModuleRes(mContext);
                    for (int slot = 1; slot <= 2; slot++) {
                        for (int lvl = 0; lvl <= 5; lvl++) {
                            for (String colorMode : colorModeList) {
                                if (!selectedIconStyle.equals("theme") || !colorMode.equals("tint") ) {
                                    String dualIconResName = "statusbar_signal_" + slot + "_" + lvl + (!colorMode.equals("") ? ("_" + colorMode) : "") + (!selectedIconStyle.equals("") ? ("_" + selectedIconStyle) : "");
                                    int iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg);
                                    dualSignalResMap.put(dualIconResName, MainModule.resHooks.addResource(dualIconResName, iconResId));
                                }
                            }
                        }
                    }
                }
            }
        });

        SparseIntArray signalResToLevelMap = new SparseIntArray();
        boolean moveSignalLeft = MainModule.mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft");
        String ControllerImplName = moveSignalLeft ? "MiuiDripLeftStatusBarIconControllerImpl" : "StatusBarIconControllerImpl";
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone." + ControllerImplName, lpparam.getClassLoader(), "setMobileIcons", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    Resources res = mContext.getResources();
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_0", "drawable", lpparam.getPackageName()), 0);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_1", "drawable", lpparam.getPackageName()), 1);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_2", "drawable", lpparam.getPackageName()), 2);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_3", "drawable", lpparam.getPackageName()), 3);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_4", "drawable", lpparam.getPackageName()), 4);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_5", "drawable", lpparam.getPackageName()), 5);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_null", "drawable", lpparam.getPackageName()), 6);
                }
                List<?> iconStates = (List<?>) param.getArgs()[1];
                if (iconStates.size() == 2) {
                    Object mainIconState = iconStates.get(0);
                    Object subIconState = iconStates.get(1);
                    boolean subDataConnected = (boolean) XposedHelpers.getObjectField(subIconState, "dataConnected");
                    XposedHelpers.setObjectField(subIconState, "visible", false);
                    int mainSignalResId = (int) XposedHelpers.getObjectField(mainIconState, "strengthId");
                    int subSignalResId = (int) XposedHelpers.getObjectField(subIconState, "strengthId");
                    int mainLevel = signalResToLevelMap.get(mainSignalResId);
                    int subLevel = signalResToLevelMap.get(subSignalResId);
                    int level;
                    if (subDataConnected) {
                        level = subLevel * 10 + mainLevel;
                        String[] syncFields = { "showName", "activityIn", "activityOut" };
                        for (String field : syncFields) {
                            XposedHelpers.setObjectField(mainIconState, field, XposedHelpers.getObjectField(subIconState, field));
                        }
                        XposedHelpers.setObjectField(mainIconState, "dataConnected", true);
                    }
                    else {
                        level = mainLevel * 10 + subLevel;
                    }
                    XposedHelpers.setObjectField(mainIconState, "strengthId", level);
                    param.getArgs()[1] = iconStates;
                }
            }
        });

        MethodHook stateUpdateHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mobileIconState = param.getArgs()[0];
                boolean visible = (boolean) XposedHelpers.getObjectField(mobileIconState, "visible");
                boolean airplane = (boolean) XposedHelpers.getObjectField(mobileIconState, "airplane");
                int level = (int) XposedHelpers.getObjectField(mobileIconState, "strengthId");
                if (!visible || airplane || level == 0 || level > 100) {
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "subStrengthId", -1);
                }
                else {
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "subStrengthId", level % 10);
                }
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int subStrengthId = (int) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "subStrengthId");
                if (subStrengthId < 0) return;
                Object mSmallHd = XposedHelpers.getObjectField(param.getThisObject(), "mSmallHd");
                XposedHelpers.callMethod(mSmallHd, "setVisibility", 8);
                Object mSmallRoaming = XposedHelpers.getObjectField(param.getThisObject(), "mSmallRoaming");
                XposedHelpers.callMethod(mSmallRoaming, "setVisibility", 0);
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "initViewState", stateUpdateHook);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "updateState", stateUpdateHook);

        MethodHook resetImageDrawable = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int subStrengthId = (int) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "subStrengthId");
                if (subStrengthId < 0) return;
                if (subStrengthId == 6) subStrengthId = 0;
                Object mobileIconState = XposedHelpers.getObjectField(param.getThisObject(), "mState");
                int level1 = (int) XposedHelpers.getObjectField(mobileIconState, "strengthId");
                level1 = level1 / 10;
                if (level1 == 6) level1 = 0;
                boolean mLight = (boolean) XposedHelpers.getObjectField(param.getThisObject(), "mLight");
                boolean mUseTint = (boolean) XposedHelpers.getObjectField(param.getThisObject(), "mUseTint");
                Object mSmallRoaming = XposedHelpers.getObjectField(param.getThisObject(), "mSmallRoaming");
                Object mMobile = XposedHelpers.getObjectField(param.getThisObject(), "mMobile");
                String colorMode = "";
                if (mUseTint && !selectedIconStyle.equals("theme")) {
                    colorMode = "_tint";
                }
                else if (!mLight) {
                    colorMode = "_dark";
                }
                String iconStyle = "";
                if (!selectedIconStyle.equals("")) {
                    iconStyle = "_" + selectedIconStyle;
                }
                String sim1IconId = "statusbar_signal_1_" + level1 + colorMode + iconStyle;
                String sim2IconId = "statusbar_signal_2_" + subStrengthId + colorMode + iconStyle;
                int sim1ResId = dualSignalResMap.get(sim1IconId);
                int sim2ResId = dualSignalResMap.get(sim2IconId);
                XposedHelpers.callMethod(mMobile, "setImageResource", sim1ResId);
                XposedHelpers.callMethod(mSmallRoaming, "setImageResource", sim2ResId);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "applyDarknessInternal", resetImageDrawable);
        int rightMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_rightmargin", 0);
        int leftMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_leftmargin", 0);
        int iconScale = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_scale", 10);
        int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_verticaloffset", 8);
        if (rightMargin > 0 || leftMargin > 0 || iconScale != 10 || verticalOffset != 8) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "init", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    LinearLayout mobileView = (LinearLayout) param.getThisObject();
                    Context mContext = mobileView.getContext();
                    Resources res = mContext.getResources();
                    int rightSpacing = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        rightMargin * 0.5f,
                        res.getDisplayMetrics()
                    );
                    int leftSpacing = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        leftMargin * 0.5f,
                        res.getDisplayMetrics()
                    );
                    mobileView.setPadding(leftSpacing, 0, rightSpacing, 0);
                    View mMobile = (View) XposedHelpers.getObjectField(param.getThisObject(), "mMobile");
                    if (verticalOffset != 8) {
                        float marginTop = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            (verticalOffset - 8) * 0.5f,
                            res.getDisplayMetrics()
                        );
                        FrameLayout mobileIcon = (FrameLayout) mMobile.getParent();
                        mobileIcon.setTranslationY(marginTop);
                    }
                    if (iconScale != 10) {
                        View mSmallRoaming = (View) XposedHelpers.getObjectField(param.getThisObject(), "mSmallRoaming");
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMobile.getLayoutParams();
                        int mIconHeight = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            20 * iconScale / 10f,
                            res.getDisplayMetrics()
                        );
                        if (layoutParams == null) {
                            layoutParams = new FrameLayout.LayoutParams(-2, mIconHeight);
                        } else {
                            layoutParams.height = mIconHeight;
                        }
                        layoutParams.gravity = Gravity.CENTER;
                        mMobile.setLayoutParams(layoutParams);
                        mSmallRoaming.setLayoutParams(layoutParams);
                    }
                }
            });
        }
    }

    public static void StatusBarIconsPositionAdjustHook(PackageLoadedParam lpparam, boolean moveRight, boolean moveLeft) {
        boolean dualRows = MainModule.mPrefs.getBoolean("system_statusbar_dualrows");
        boolean swapWifiSignal = MainModule.mPrefs.getBoolean("system_statusbaricons_swap_wifi_mobile");
        boolean moveSignalLeft = MainModule.mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft");
        boolean netspeedAtRow2 = dualRows && MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow");
        boolean netspeedRight = !netspeedAtRow2 && MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atright");
        boolean netspeedLeft = !netspeedAtRow2 && MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atleft");
        Class<?> DripLeftController = findClassIfExists("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.getClassLoader());

        ArrayList<String> rightOnly2LeftIcons = new ArrayList<String>();
        if (MainModule.mPrefs.getBoolean("system_statusbar_gps_atleft")) {
            rightOnly2LeftIcons.add("location");
        }

        List<String> signalRelatedIcons;
        if (!swapWifiSignal) {
            signalRelatedIcons = List.of("no_sim", "hd", "mobile", "demo_mobile", "airplane", "hotspot", "slave_wifi", "wifi", "demo_wifi");
        }
        else {
            signalRelatedIcons = List.of("hotspot", "slave_wifi", "wifi", "demo_wifi", "no_sim", "hd", "mobile", "demo_mobile", "airplane");
        }
        if (moveLeft) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIconVisibility", String.class, boolean.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    String slot = (String) param.getArgs()[0];
                    if (("alarm_clock".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_alarm_atleft"))
                        || ("volume".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_sound_atleft"))
                        || ("zen".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_dnd_atleft"))
                        || ("nfc".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_nfc_atleft"))
                        || ("headset".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_headset_atleft"))
                    ) {
                        param.getArgs()[1] = false;
                    }
                }
            });
        }
        if (moveRight) {
            ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.getClassLoader(), "setIconVisibility", String.class, boolean.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    String slot = (String) param.getArgs()[0];
                    if (("alarm_clock".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_alarm_atright"))
                        || ("volume".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_sound_atright"))
                        || ("zen".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_dnd_atright"))
                        || ("nfc".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_nfc_atright"))
                        || ("headset".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_headset_atright"))
                    ) {
                        param.getArgs()[1] = false;
                    }
                }
            });
        }
        if (moveRight || netspeedRight) {
            ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
                private boolean isHooked = false;

                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    if (!isHooked) {
                        isHooked = true;
                        Class<?> MiuiEndIconManager;
                        if (DripLeftController != null) {
                            MiuiEndIconManager = findClass("com.android.systemui.statusbar.phone.MiuiEndIconManager", lpparam.getClassLoader());
                        }
                        else {
                            MiuiEndIconManager = findClass("com.android.systemui.statusbar.phone.MiuiIconManagerUtils", lpparam.getClassLoader());
                        }
                        Object blockList = ModuleHelper.getStaticObjectFieldSilently(MiuiEndIconManager, "RIGHT_BLOCK_LIST");
                        ArrayList<String> rightBlockList = (ArrayList<String>) blockList;
                        if (netspeedRight) {
                            rightBlockList.remove("network_speed");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_alarm_atright")) {
                            rightBlockList.remove("alarm_clock");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_sound_atright")) {
                            rightBlockList.remove("volume");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_dnd_atright")) {
                            rightBlockList.remove("zen");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_btbattery_atright")) {
                            rightBlockList.remove("bluetooth_handsfree_battery");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_nfc_atright")) {
                            rightBlockList.remove("nfc");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_headset_atright")) {
                            rightBlockList.remove("headset");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_vpn_atright")) {
                            rightBlockList.remove("vpn");
                        }
                        XposedHelpers.setStaticObjectField(MiuiEndIconManager, "RIGHT_BLOCK_LIST", rightBlockList);
                    }
                }
            });
        }
        ArrayList<String> dripLeftIcons = new ArrayList<String>();
        if (swapWifiSignal || moveSignalLeft || moveLeft) {
            ModuleHelper.findAndHookConstructor("com.android.systemui.statusbar.phone.StatusBarIconList", lpparam.getClassLoader(), String[].class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    boolean isRightController = "StatusBarIconControllerImpl".equals(param.getThisObject().getClass().getSimpleName());
                    if (isRightController) {
                        if (swapWifiSignal || moveSignalLeft) {
                            ArrayList<String> allStatusIcons = new ArrayList<String>(Arrays.asList((String[]) param.getArgs()[0]));
                            allStatusIcons.removeAll(signalRelatedIcons);
                            if (swapWifiSignal) {
                                for (String slotName : signalRelatedIcons) {
                                    if (statusbarIconList.contains(slotName)) {
                                        allStatusIcons.add(slotName);
                                    }
                                }
                            }
                            param.getArgs()[0] = allStatusIcons.toArray(new String[0]);
                        }
                    }
                    else if (moveSignalLeft || moveLeft) {
                        ArrayList<String> allStatusIcons = new ArrayList<String>(Arrays.asList((String[]) param.getArgs()[0]));
                        allStatusIcons.addAll(rightOnly2LeftIcons);
                        dripLeftIcons.addAll(allStatusIcons);
                        if (moveSignalLeft) {
                            for (int i = signalRelatedIcons.size() - 1; i >= 0; i--) {
                                String slotName = signalRelatedIcons.get(i);
                                if (statusbarIconList.contains(slotName)) {
                                    allStatusIcons.add(0, slotName);
                                }
                            }
                        }
                        param.getArgs()[0] = allStatusIcons.toArray(new String[0]);
                    }
                }
            });
        }

        ArrayList<String> rightOnly2LeftWithSignal = new ArrayList<String>(rightOnly2LeftIcons);
        if (moveSignalLeft && DripLeftController != null) {
            rightOnly2LeftWithSignal.add("slave_wifi");
            rightOnly2LeftWithSignal.add("hotspot");
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.StatusBarSignalPolicy", lpparam.getClassLoader(), new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Object dripLeftController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", DripLeftController);
                    XposedHelpers.setObjectField(param.getThisObject(), "mIconController", dripLeftController);
                }
            });
        }
        if (!rightOnly2LeftWithSignal.isEmpty() && DripLeftController != null) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIcon", String.class, int.class, CharSequence.class, new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    String slot = (String) param.getArgs()[0];
                    if (rightOnly2LeftWithSignal.contains(slot)) {
                        Object dripLeftController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", DripLeftController);
                        XposedHelpers.callMethod(dripLeftController, "setIcon", param.getArgs()[0], param.getArgs()[1], param.getArgs()[2]);
                        param.returnAndSkip(null);
                    }
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIconVisibility", String.class, boolean.class, new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    String slot = (String) param.getArgs()[0];
                    if (rightOnly2LeftWithSignal.contains(slot)) {
                        Object dripLeftController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", DripLeftController);
                        XposedHelpers.callMethod(dripLeftController, "setIconVisibility", param.getArgs()[0], param.getArgs()[1]);
                        param.returnAndSkip(null);
                    }
                }
            });
        }
        if (DripLeftController != null && (moveSignalLeft || moveLeft)) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Object mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBar");
                    int mCurrentStatusBarType = XposedHelpers.getIntField(mStatusBar, "mCurrentStatusBarType");
                    if (mCurrentStatusBarType != 1) {
                        Object mDripIconManager = XposedHelpers.getObjectField(param.getThisObject(), "mDripLeftDarkIconManager");
                        ArrayList<String> blockList = new ArrayList<String>(dripLeftIcons);
                        if (MainModule.mPrefs.getBoolean("system_statusbar_alarm_atleft")) {
                            blockList.remove("alarm_clock");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_sound_atleft")) {
                            blockList.remove("volume");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_dnd_atleft")) {
                            blockList.remove("zen");
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_gps_atleft")) {
                            blockList.remove("location");
                        }
                        XposedHelpers.callMethod(mDripIconManager, "setBlockList", blockList);
                    }
                }
            });
        }
        if (DripLeftController != null) {
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "updateCutoutLocation", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.getThisObject(), "mCurrentStatusBarType");
                    if (mCurrentStatusBarType == 1) {
                        if (netspeedRight) {
                            Object mDripNetworkSpeedView = XposedHelpers.getObjectField(param.getThisObject(), "mDripNetworkSpeedView");
                            XposedHelpers.callMethod(mDripNetworkSpeedView, "setBlocked", true);
                        }
                    }
                    else {
                        if (moveSignalLeft || moveLeft) {
                            View mDripStatusBarLeftStatusIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mDripStatusBarLeftStatusIconArea");
                            mDripStatusBarLeftStatusIconArea.setVisibility(View.VISIBLE);
                        }
                        if (netspeedLeft || netspeedAtRow2) {
                            Object mDripNetworkSpeedView = XposedHelpers.getObjectField(param.getThisObject(), "mDripNetworkSpeedView");
                            XposedHelpers.callMethod(mDripNetworkSpeedView, "setBlocked", false);
                        }
                    }
                }
            });
        }

        if (netspeedRight && DripLeftController != null) {
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), "setDripNetworkSpeedView", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    param.getArgs()[0] = null;
                }
            });
        }
        if (netspeedLeft || netspeedAtRow2) {
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader(), "setVisibilityByController", new MethodHook() {
                int leftViewId = 0;
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    TextView meter = (TextView) param.getThisObject();
                    String slot = (String) XposedHelpers.callMethod(param.getThisObject(), "getSlot");
                    if (leftViewId == 0) {
                        leftViewId = meter.getResources().getIdentifier("drip_network_speed_view", "id", lpparam.getPackageName());
                    }
                    if (slot.equals("network_speed") && meter.getId() != leftViewId) {
                        param.getArgs()[0] = false;
                    }
                }
            });
        }
    }

    public static void StatusBarClockPositionHook(PackageLoadedParam lpparam) {
        final int pos = MainModule.mPrefs.getStringAsInt("system_statusbar_clock_position", 1);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                FrameLayout sbView = (FrameLayout) param.getThisObject();
                Context mContext = sbView.getContext();
                Resources res = mContext.getResources();
                TextView mClockView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMiuiClock");
                LinearLayout leftIconsContainer = (LinearLayout) mClockView.getParent();
                int clockIndex = leftIconsContainer.indexOfChild(mClockView);
                leftIconsContainer.removeView(mClockView);
                int contentId = res.getIdentifier("status_bar_contents", "id", lpparam.getPackageName());
                LinearLayout mContentsContainer = sbView.findViewById(contentId);
                View spaceView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mCutoutSpace");
                int spaceIndex = mContentsContainer.indexOfChild(spaceView);
                LinearLayout rightContainer = new LinearLayout(mContext);
                LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -1, 1.0f);
                View mSystemIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea");
                mContentsContainer.removeView(mSystemIconArea);
                mContentsContainer.addView(rightContainer, spaceIndex + 1, rightLp);
                rightContainer.addView(mSystemIconArea);
                View mDripStatusBarLeftStatusIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mDripStatusBarLeftStatusIconArea");
                leftIconsContainer.removeView(mDripStatusBarLeftStatusIconArea);
                leftIconsContainer.addView(mDripStatusBarLeftStatusIconArea, clockIndex);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                if (pos == 2) {
                    lp.gravity = Gravity.CENTER;
                    mContentsContainer.addView(mClockView, spaceIndex, lp);
                }
                else {
                    rightContainer.addView(mClockView, lp);
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.getClassLoader(), "updateLayoutForCutout", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.getThisObject(), "mCurrentStatusBarType");
                View mSystemIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea");
                View mStatusBarLeftContainer = (View) XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarLeftContainer");
                if (mCurrentStatusBarType == 0) {
                    LinearLayout.LayoutParams mSystemIconAreaLp = (LinearLayout.LayoutParams) mSystemIconArea.getLayoutParams();
                    mSystemIconAreaLp.width = 0;
                    mSystemIconAreaLp.weight = 1.0f;
                    if (pos == 2) {
                        LinearLayout rightContainer = (LinearLayout) mSystemIconArea.getParent();
                        View mDripStatusBarNotificationIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mDripStatusBarNotificationIconArea");
                        mDripStatusBarNotificationIconArea.setVisibility(View.VISIBLE);
                        LinearLayout.LayoutParams mStatusBarLeftContainerLp = (LinearLayout.LayoutParams) mStatusBarLeftContainer.getLayoutParams();
                        mStatusBarLeftContainerLp.width = 0;
                        mStatusBarLeftContainerLp.weight = 1.0f;
                        FrameLayout sbView = (FrameLayout) param.getThisObject();
                        int leftPadding = sbView.getPaddingStart();
                        int rightPadding = sbView.getPaddingEnd();
                        int topPadding = sbView.getPaddingTop();
                        int bottomPadding = sbView.getPaddingBottom();
                        mStatusBarLeftContainer.setPadding(leftPadding, 0, 0, 0);
                        rightContainer.setPadding(0, 0, rightPadding, 0);
                        sbView.setPadding(0, topPadding, 0, bottomPadding);
                    }
                }
                else {
                    View mCutoutSpace = (View) XposedHelpers.getObjectField(param.getThisObject(), "mCutoutSpace");
                    if (pos == 2) {
                        mCutoutSpace.setVisibility(View.GONE);
                        mStatusBarLeftContainer.setPadding(0, 0, 0, 0);
                        LinearLayout rightContainer = (LinearLayout) mSystemIconArea.getParent();
                        rightContainer.setPadding(0, 0, 0, 0);
                    }
                }
            }
        });
        if (pos == 2) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "updateNotificationIconAreaInnnerParent", new MethodHook() {
                private int originType = 0;
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    int mCurrentStatusBarType = XposedHelpers.getIntField(param.getThisObject(), "mCurrentStatusBarType");
                    if (mCurrentStatusBarType == 0) {
                        XposedHelpers.setObjectField(param.getThisObject(), "mCurrentStatusBarType", 1);
                    }
                    originType = mCurrentStatusBarType;
                }
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    XposedHelpers.setObjectField(param.getThisObject(), "mCurrentStatusBarType", originType);
                }
            });
        }
    }

    public static void NoNetworkSpeedSeparatorHook(PackageLoadedParam lpparam) {
        MethodHook hideSplitterHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                TextView tv = (TextView) param.getThisObject();
                tv.setVisibility(View.GONE);
                param.returnAndSkip(null);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedSplitter", lpparam.getClassLoader(), "onClockVisibilityChanged", int.class, hideSplitterHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedSplitter", lpparam.getClassLoader(), "onNetworkSpeedVisibilityChanged", int.class, hideSplitterHook);
    }

    public static void FormatNetworkSpeedHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), "formatSpeed", Context.class, long.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean hideLow = MainModule.mPrefs.getBoolean("system_detailednetspeed_low");
                if (hideLow) {
                    int lowLevel = MainModule.mPrefs.getInt("system_detailednetspeed_lowlevel", 1) * 1024;
                    long speedVal = (long) param.getArgs()[1];
                    if (speedVal < lowLevel) {
                        if (newStyle) {
                            String[] EMPTY = {"", ""};
                            param.returnAndSkip(EMPTY);
                        }
                        else {
                            param.returnAndSkip("");
                        }
                    }
                }
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean hideUnit = MainModule.mPrefs.getBoolean("system_detailednetspeed_secunit");
                if (hideUnit && !newStyle) {
                    String speedText = (String) param.getResult();
                    speedText = speedText.replaceFirst("B?[/']s", "");
                    param.setResult(speedText);
                }
            }
        });
    }

    private static void initNetSpeedStyle(View meter) {
        boolean dualRow = MainModule.mPrefs.getBoolean("system_detailednetspeed")
            || MainModule.mPrefs.getBoolean("system_detailednetspeed_fakedualrow");
        TextView iconTextView = getIconTextView(meter);
        int fontSize = MainModule.mPrefs.getInt("system_netspeed_fontsize", 13);
        if (dualRow) {
            if (newStyle) {
                View unitView = (View)XposedHelpers.getObjectField(meter, "mNetworkSpeedUnitText");
                unitView.setVisibility(View.GONE);
            }
            if (fontSize > 23 || fontSize == 13) fontSize = 16;
        }
        else {
            if (fontSize < 20 && fontSize != 13) fontSize = 27;
        }
        if (dualRow || fontSize != 13) {
            iconTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f);
        }
        if (MainModule.mPrefs.getBoolean("system_netspeed_bold")) {
            iconTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        int leftMargin = MainModule.mPrefs.getInt("system_netspeed_leftmargin", 0);
        leftMargin = (int)Helpers.dp2px(leftMargin * 0.5f);
        int rightMargin = MainModule.mPrefs.getInt("system_netspeed_rightmargin", 0);
        rightMargin = (int)Helpers.dp2px(rightMargin * 0.5f);
        int topMargin = 0;
        int verticalOffset = MainModule.mPrefs.getInt("system_netspeed_verticaloffset", 8);
        if (verticalOffset != 8) {
            topMargin = (int)Helpers.dp2px((verticalOffset - 8) * 0.5f);
        }
        iconTextView.setPaddingRelative(leftMargin, topMargin, rightMargin, 0);

        int align = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_align", 1);
        if (align == 2) {
            iconTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        else if (align == 3) {
            iconTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else if (align == 4) {
            iconTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }

        if (dualRow) {
            float spacing = 0.9f;
            iconTextView.setSingleLine(false);
            iconTextView.setMaxLines(2);
            if (0.5 * fontSize > 8.5f) {
                spacing = 0.85f;
            }
            iconTextView.setLineSpacing(0, spacing);
        }
    }

    public static void NetSpeedStyleHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View meter = (View) param.getThisObject();
                if (meter == null) return;
                Object inited = meter.getTag(viewInitedTag);
                if (inited == null && !"slot_text_icon".equals(meter.getTag())) {
                    meter.setTag(viewInitedTag, true);
                    int fixedWidth = MainModule.mPrefs.getInt("system_netspeed_fixedcontent_width", 10);
                    if (fixedWidth > 10) {
                        ViewGroup.LayoutParams lp = meter.getLayoutParams();
                        int viewWidth = (int)(meter.getResources().getDisplayMetrics().density * fixedWidth);
                        if (lp == null) {
                            lp = new ViewGroup.LayoutParams(viewWidth, -1);
                        }
                        else {
                            lp.width = viewWidth;
                        }
                        meter.setLayoutParams(lp);
                    }
                    meter.postDelayed(() -> {
                        initNetSpeedStyle(meter);
                    }, 200);
                }
            }
        });
    }
    public static void MobileTypeSingleHook(PackageLoadedParam lpparam) {
        MethodHook singleTypeHook = new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mobileIconState = param.getArgs()[0];
                XposedHelpers.setObjectField(mobileIconState, "showMobileDataTypeSingle", true);
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mMobileLeftContainer = XposedHelpers.getObjectField(param.getThisObject(), "mMobileLeftContainer");
                XposedHelpers.callMethod(mMobileLeftContainer, "setVisibility", 8);
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "initViewState", singleTypeHook);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "updateState", singleTypeHook);

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "init", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Resources res = mContext.getResources();
                LinearLayout mMobileGroup = (LinearLayout) XposedHelpers.getObjectField(param.getThisObject(), "mMobileGroup");
                TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle");
                if (!MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single_atleft")) {
                    mMobileGroup.removeView(mMobileTypeSingle);
                    mMobileGroup.addView(mMobileTypeSingle);
                }
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mMobileTypeSingle.getLayoutParams();
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_leftmargin", 4);
                float marginLeft = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMargin * 0.5f,
                    res.getDisplayMetrics()
                );
                mlp.leftMargin = (int) marginLeft;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_rightmargin", 0);
                if (rightMargin > 0) {
                    float marginRight = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        rightMargin * 0.5f,
                        res.getDisplayMetrics()
                    );
                    mlp.rightMargin = (int) marginRight;
                }
                int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_verticaloffset", 8);
                if (verticalOffset != 8) {
                    float marginTop = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (verticalOffset - 8) * 0.5f,
                        res.getDisplayMetrics()
                    );
                    mlp.topMargin = (int) marginTop;
                }
                mMobileTypeSingle.setLayoutParams(mlp);
                int fontSize = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_fontsize", 27);
                mMobileTypeSingle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f);
                if (MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single_bold")) {
                    mMobileTypeSingle.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
        });
    }

    private static ClassLoader pluginLoader = null;

    public static void VolumeDialogAutohideDelayHook(ClassLoader classLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "computeTimeoutH", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean mHovering = XposedHelpers.getBooleanField(param.getThisObject(), "mHovering");
                if (mHovering) {
                    param.returnAndSkip(16000);
                    return;
                }
                boolean mSafetyWarning;
                try {
                    mSafetyWarning = (boolean) XposedHelpers.getObjectField(param.getThisObject(), "mIsSafetyShowing");
                }
                catch (Throwable e) {
                    mSafetyWarning = (boolean) XposedHelpers.getObjectField(param.getThisObject(), "mSafetyWarning");
                }
                if (mSafetyWarning) {
                    int opt = MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0);
                    param.returnAndSkip(opt > 0 ? opt : 5000);
                    return;
                }
                boolean mExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mExpanded");
                int opt = MainModule.mPrefs.getInt(mExpanded ? "system_volumedialogdelay_expanded" : "system_volumedialogdelay_collapsed", 0);
                if (opt > 0) param.returnAndSkip(opt);
            }
        });
    }

    private static float blurCollapsed = 0.0f;
    private static float blurExpanded = 0.0f;

    public static void BlurVolumeDialogBackgroundHook(ClassLoader classLoader) {
        MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "fraction", "miui_volume_dim_behind_collapsed", 0f);
        MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "fraction", "miui_volume_dim_behind_expanded", 0f);

        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "updateDialogWindowH", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean mExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mExpanded");
                float blurRatio = blurCollapsed;
                boolean isVisible = (boolean) param.getArgs()[0];
                if (mExpanded && !isVisible) {
                    blurRatio = blurExpanded;
                }
                if (!mExpanded && blurCollapsed > 0.001f) {
                    Window mWindow = (Window) XposedHelpers.getObjectField(param.getThisObject(), "mWindow");
                    mWindow.clearFlags(8);
                }
                if (mExpanded) {
                    XposedHelpers.callMethod(param.getThisObject(), "startBlurAnim", 0f, blurRatio, 0);
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "showH", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (blurCollapsed > 0.001f) {
                    Window mWindow = (Window) XposedHelpers.getObjectField(param.getThisObject(), "mWindow");
                    mWindow.clearFlags(8);
                    XposedHelpers.callMethod(param.getThisObject(), "startBlurAnim", 0f, blurCollapsed, 0);
                }
            }
        });
        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "initDialog", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                blurCollapsed = MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) / 100f;
                blurExpanded = MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) / 100f;
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    @Override
                    public void onChange(String key) {
                        try {
                            if (key.equals("system_volumeblur_collapsed")) blurCollapsed = MainModule.mPrefs.getInt(key, 0) / 100f;
                            if (key.equals("system_volumeblur_expanded")) blurExpanded = MainModule.mPrefs.getInt(key, 0) / 100f;
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                });
            }
        });
    }

    public static void BlurMTKVolumeBarHook(ClassLoader classLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.Util", classLoader, "isSupportBlurS", HookerClassHelper.returnConstant(true));
    }

    public static void SingleNotificationSliderHook(ClassLoader classLoader) {
        boolean newSingleSlider = false;
        Class<?> UtilCls = findClassIfExists("com.android.systemui.miui.volume.Util", classLoader);
        if (UtilCls != null) {
            Object hasFeature = ModuleHelper.getStaticObjectFieldSilently(UtilCls, "sIsNotificationSingle");
            newSingleSlider = !ModuleHelper.NOT_EXIST_SYMBOL.equals(hasFeature);
        }
        if (newSingleSlider) {
            XposedHelpers.setStaticBooleanField(UtilCls, "sIsNotificationSingle", true);
            ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.Util", classLoader, "isNotificationSingle", Context.class, int.class, HookerClassHelper.returnConstant(true));
        }
        else {
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_content_width_expanded", R.dimen.miui_volume_content_width_expanded);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_ringer_layout_width_expanded", R.dimen.miui_volume_ringer_layout_width_expanded);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_width_expanded", R.dimen.miui_volume_column_width_expanded);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_margin_horizontal_expanded", R.dimen.miui_volume_column_margin_horizontal_expanded);
            notifVolumeOnResId = MainModule.resHooks.addResource("ic_miui_volume_notification", R.drawable.ic_miui_volume_notification);
            notifVolumeOffResId = MainModule.resHooks.addResource("ic_miui_volume_notification_mute", R.drawable.ic_miui_volume_notification_mute);
            ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "addColumn", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    if (param.getArgs().length != 4) return;
                    int streamType = (int) param.getArgs()[0];
                    if (streamType == 4) {
                        XposedHelpers.callMethod(param.getThisObject(), "addColumn", 5, notifVolumeOnResId, notifVolumeOffResId, true, false);
                    }
                }
            });
        }
    }

    public static void MIUIVolumeDialogHook(PackageLoadedParam lpparam) {
        String pluginLoaderClass = "com.android.systemui.shared.plugins.PluginInstance$Factory";
        ModuleHelper.hookAllMethods(pluginLoaderClass, lpparam.getClassLoader(), "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.getArgs()[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    if (MainModule.mPrefs.getBoolean("system_separatevolume") && MainModule.mPrefs.getBoolean("system_separatevolume_slider")) {
                        SingleNotificationSliderHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_nosilentvibrate")) {
                        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader, "vibrateH", HookerClassHelper.DO_NOTHING);
                    }
                    if (MainModule.mPrefs.getInt("system_volumedialogdelay_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0) > 0) {
                        VolumeDialogAutohideDelayHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) > 0) {
                        BlurVolumeDialogBackgroundHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_volumebar_blur_mtk")) {
                        BlurMTKVolumeBarHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_qs_force_systemfonts")) {
                        ModuleHelper.findAndHookMethod("miui.systemui.util.SystemUIResourcesHelperImpl", pluginLoader, "getBoolean", String.class, new MethodHook() {
                            @Override
                            protected void before(final BeforeHookCallback param) throws Throwable {
                                String key = (String) param.getArgs()[0];
                                if (key.equals("header_big_time_use_system_font")) {
                                    param.returnAndSkip(Boolean.TRUE);
                                }
                            }
                        });
                    }
                    if (MainModule.mPrefs.getBoolean("system_qsnolabels")) {
                        HideCCLabelsHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_volumetimer")) {
                        VolumeTimerValuesRes(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_cc_tile_roundedrect")) {
                        CCTileCornerHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_cc_volume_showpct")) {
                        ShowVolumePctHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_cc_hidedate")) {
                        HideCCDateView(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_cc_clocktweak")) {
                        initCCClockStyle(pluginLoader);
                    }
                    if (MainModule.mPrefs.getBoolean("system_cc_hide_shortcuticons")) {
                        hideCCSettingsTilesEdit(pluginLoader);
                    }
                    if (MainModule.mPrefs.getStringAsInt("system_cc_bluetooth_tile_style", 1) > 1) {
                        BluetoothTileStyleHook(pluginLoader);
                    }
                }
            }
        });
    }

    private static float scaledTileWidthDim = -1f;
    public static void SystemCCGridHook(PackageLoadedParam lpparam) {
        int cols = MainModule.mPrefs.getInt("system_ccgridcolumns", 4);
        int rows = MainModule.mPrefs.getInt("system_ccgridrows", 4);
        if (cols > 4) {
            MainModule.resHooks.setObjectReplacement(lpparam.getPackageName(), "dimen", "qs_control_tiles_columns", cols);
        }

        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext");
                    Resources res = mContext.getResources();
                    float density = res.getDisplayMetrics().density;
                    int tileWidthResId = res.getIdentifier("qs_control_center_tile_width", "dimen", "com.android.systemui");
                    float tileWidthDim = res.getDimension(tileWidthResId);
                    if (cols > 4) {
                        tileWidthDim = tileWidthDim / density;
                        scaledTileWidthDim = tileWidthDim * 4 / cols;
                        MainModule.resHooks.setDensityReplacement(lpparam.getPackageName(), "dimen", "qs_control_center_tile_width", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_control_center_tile_width", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement(lpparam.getPackageName(), "dimen", "qs_control_tile_icon_bg_size", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_control_tile_icon_bg_size", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_cell_height", 85f);
                    }
                }
            }
        });

        String pluginLoaderClass = "com.android.systemui.shared.plugins.PluginInstance$Factory";
        ModuleHelper.hookAllMethods(pluginLoaderClass, lpparam.getClassLoader(), "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.getArgs()[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    if (cols > 4) {
                        ModuleHelper.findAndHookConstructor("miui.systemui.controlcenter.qs.QSPager", pluginLoader, Context.class, AttributeSet.class, new MethodHook() {
                            @Override
                            protected void after(final AfterHookCallback param) throws Throwable {
                                XposedHelpers.setObjectField(param.getThisObject(), "columns", cols);
                            }
                        });
                        if (!MainModule.mPrefs.getBoolean("system_qsnolabels")) {
                            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.StandardTileView", pluginLoader, "createLabel", boolean.class, new MethodHook() {
                                @Override
                                protected void after(final AfterHookCallback param) throws Throwable {
                                    Object label = XposedHelpers.getObjectField(param.getThisObject(), "label");
                                    if (label != null) {
                                        TextView lb = (TextView) label;
                                        lb.setMaxLines(1);
                                        lb.setSingleLine(true);
                                        lb.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                        lb.setMarqueeRepeatLimit(0);

                                        View labelContainer = (View) XposedHelpers.getObjectField(param.getThisObject(), "labelContainer");
                                        labelContainer.setPadding(4, 0 , 4, 0);
                                    }
                                }
                            });
                        }
                    }
                    if (rows != 4) {
                        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.QSPager", pluginLoader, "distributeTiles", new MethodHook() {
                            @Override
                            protected void after(final AfterHookCallback param) throws Throwable {
                                boolean collapse = (boolean) XposedHelpers.getObjectField(param.getThisObject(), "collapse");
                                if (collapse) {
                                    ArrayList<Object> pages = (ArrayList<Object>) XposedHelpers.getObjectField(param.getThisObject(), "pages");
                                    for (Object tileLayoutImpl : pages) {
                                        XposedHelpers.callMethod(tileLayoutImpl, "removeTiles");
                                    }
                                    ArrayList<Object> pageTiles = new ArrayList<Object>();
                                    int currentRow = 2;
                                    ArrayList<?> records = (ArrayList<?>) XposedHelpers.getObjectField(param.getThisObject(), "records");
                                    Iterator<?> it2 = records.iterator();
                                    int i3 = 0;
                                    int pageNow = 0;
                                    Object bigHeader = XposedHelpers.getObjectField(param.getThisObject(), "header");
                                    while (it2.hasNext()) {
                                        Object tileRecord = it2.next();
                                        pageTiles.add(tileRecord);
                                        i3++;
                                        if (i3 >= cols) {
                                            currentRow++;
                                            i3 = 0;
                                        }
                                        if (currentRow >= rows || !it2.hasNext()) {
                                            XposedHelpers.callMethod(pages.get(pageNow), "setTiles", pageTiles, pageNow == 0 ? bigHeader : null);
                                            pageTiles.clear();
                                            int totalRows = (int) XposedHelpers.getObjectField(param.getThisObject(), "rows");
                                            if (currentRow > totalRows) {
                                                XposedHelpers.setObjectField(param.getThisObject(), "rows", currentRow);
                                            }
                                            if (it2.hasNext()) {
                                                pageNow++;
                                                currentRow = 0;
                                            }
                                        }
                                    }
                                    Iterator<Object> it3 = pages.iterator();
                                    while (it3.hasNext()) {
                                        Object next2 = it3.next();
                                        boolean isEmpty = (boolean) XposedHelpers.callMethod(next2, "isEmpty");
                                        if (isEmpty) {
                                            it3.remove();
                                        }
                                    }
                                    Object pageIndicator = XposedHelpers.getObjectField(param.getThisObject(), "pageIndicator");
                                    if (pageIndicator != null) {
                                        XposedHelpers.callMethod(pageIndicator, "setNumPages", pages.size());
                                    }
                                    Object adapter = XposedHelpers.getObjectField(param.getThisObject(), "adapter");
                                    XposedHelpers.callMethod(param.getThisObject(), "setAdapter", adapter);
//                                    XposedHelpers.callMethod(param.getThisObject(), "notifyDataSetChanged");
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public static void QQSGridRes() {
        int cols = MainModule.mPrefs.getInt("system_qqsgridcolumns", 2);
        int colsResId = R.integer.quick_quick_settings_num_rows_5;
        switch (cols) {
            case 3: colsResId = R.integer.quick_quick_settings_num_rows_3; break;
            case 4: colsResId = R.integer.quick_quick_settings_num_rows_4; break;
            case 5: colsResId = R.integer.quick_quick_settings_num_rows_5; break;
            case 6: colsResId = R.integer.quick_quick_settings_num_rows_6; break;
            case 7: colsResId = R.integer.quick_quick_settings_num_rows_7; break;
        }
        MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_qqs_count", colsResId);
    }

    public static void QSGridRes() {
        int cols = MainModule.mPrefs.getInt("system_qsgridcolumns", 2);
        int rows = MainModule.mPrefs.getInt("system_qsgridrows", 1);
        int colsRes = R.integer.quick_settings_num_columns_3;
        int rowsRes = R.integer.quick_settings_num_rows_4;

        switch (cols) {
            case 3: colsRes = R.integer.quick_settings_num_columns_3; break;
            case 4: colsRes = R.integer.quick_settings_num_columns_4; break;
            case 5: colsRes = R.integer.quick_settings_num_columns_5; break;
            case 6: colsRes = R.integer.quick_settings_num_columns_6; break;
            case 7: colsRes = R.integer.quick_settings_num_columns_7; break;
        }

        switch (rows) {
            case 2: rowsRes = R.integer.quick_settings_num_rows_2; break;
            case 3: rowsRes = R.integer.quick_settings_num_rows_3; break;
            case 4: rowsRes = R.integer.quick_settings_num_rows_4; break;
            case 5: rowsRes = R.integer.quick_settings_num_rows_5; break;
        }

        if (cols > 2) MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_num_columns", colsRes);
        if (rows > 1) MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_num_rows", rowsRes);
    }

    private static void updateLabelsVisibility(Object mRecord, int mRows, int orientation) {
        if (mRecord == null) return;
        Object tileView = XposedHelpers.getObjectField(mRecord, "tileView");
        if (tileView != null) {
            ViewGroup mLabelContainer = null;
            try {
                mLabelContainer = (ViewGroup)XposedHelpers.getObjectField(tileView, "mLabelContainer");
            }
            catch (Throwable ignore) {}

            if (mLabelContainer != null) {
                mLabelContainer.setVisibility(
                    MainModule.mPrefs.getBoolean("system_qsnolabels") ||
                        orientation == Configuration.ORIENTATION_PORTRAIT && mRows >= 5 ||
                        orientation == Configuration.ORIENTATION_LANDSCAPE && mRows >= 3 ? View.GONE : View.VISIBLE
                );
            }
        }
    }

    private static void HideCCLabelsHook(ClassLoader pluginLoader) {
        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_cell_height", 85f);
        Class<?> QSController = findClassIfExists("miui.systemui.controlcenter.qs.tileview.StandardTileView", pluginLoader);
        ModuleHelper.hookAllMethods(QSController, "init", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (param.getArgs().length != 1) return;
                View mLabelContainer = (View)XposedHelpers.getObjectField(param.getThisObject(), "labelContainer");
                if (mLabelContainer != null) {
                    mLabelContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void QSGridLabelsHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.qs.MiuiTileLayout", lpparam.getClassLoader(), "addTile", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                updateLabelsVisibility(param.getArgs()[0], XposedHelpers.getIntField(param.getThisObject(), "mRows"), ((ViewGroup)param.getThisObject()).getResources().getConfiguration().orientation);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.qs.MiuiPagedTileLayout", lpparam.getClassLoader(), "addTile", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void before(final BeforeHookCallback param) throws Throwable {
                ArrayList<Object> mPages = (ArrayList<Object>)XposedHelpers.getObjectField(param.getThisObject(), "mPages");
                if (mPages == null) return;
                int mRows = 0;
                if (mPages.size() > 0) mRows = XposedHelpers.getIntField(mPages.get(0), "mRows");
                updateLabelsVisibility(param.getArgs()[0], mRows, ((ViewGroup)param.getThisObject()).getResources().getConfiguration().orientation);
            }
        });

        int rows = MainModule.mPrefs.getInt("system_qsgridrows", 1);
        if (rows == 4) {
            ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.MiuiQSTileView", lpparam.getClassLoader(), "createLabel", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    ViewGroup mLabelContainer = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mLabelContainer");
                    if (mLabelContainer != null) mLabelContainer.setPadding(
                        mLabelContainer.getPaddingLeft(),
                        Math.round(mLabelContainer.getResources().getDisplayMetrics().density * 2),
                        mLabelContainer.getPaddingRight(),
                        mLabelContainer.getPaddingBottom()
                    );
                }
            });
        }
    }

    public static void VolumeTimerValuesRes(ClassLoader pluginLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "initTimerString", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                String[] mTimeSegmentTitle = new String[11];
                int timerOffId = mContext.getResources().getIdentifier("timer_off", "string", "miui.systemui.plugin");
                int minuteId = mContext.getResources().getIdentifier("timer_30_minutes", "string", "miui.systemui.plugin");
                int hourId = mContext.getResources().getIdentifier("timer_1_hour", "string", "miui.systemui.plugin");
                mTimeSegmentTitle[0] = mContext.getResources().getString(timerOffId);
                mTimeSegmentTitle[1] = mContext.getResources().getString(minuteId, 30);
                mTimeSegmentTitle[2] = mContext.getResources().getString(hourId, 1);
                mTimeSegmentTitle[3] = mContext.getResources().getString(hourId, 2);
                mTimeSegmentTitle[4] = mContext.getResources().getString(hourId, 3);
                mTimeSegmentTitle[5] = mContext.getResources().getString(hourId, 4);
                mTimeSegmentTitle[6] = mContext.getResources().getString(hourId, 5);
                mTimeSegmentTitle[7] = mContext.getResources().getString(hourId, 6);
                mTimeSegmentTitle[8] = mContext.getResources().getString(hourId, 8);
                mTimeSegmentTitle[9] = mContext.getResources().getString(hourId, 10);
                mTimeSegmentTitle[10] = mContext.getResources().getString(hourId, 12);
                XposedHelpers.setObjectField(param.getThisObject(), "mTimeSegmentTitle", mTimeSegmentTitle);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.TimerItem", pluginLoader, "getTimePos", int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object timer = XposedHelpers.getObjectField(param.getThisObject(), "mTimerTime");
                float halfTimerWidth = ((int) XposedHelpers.callMethod(timer, "getWidth")) / 2.0f;
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Object mTimerSeekbarWidth = ModuleHelper.getObjectFieldSilently(param.getThisObject(), "mTimerSeekbarWidth");
                int seekbarWidthResId;
                if (ModuleHelper.NOT_EXIST_SYMBOL.equals(mTimerSeekbarWidth)) {
                    seekbarWidthResId = mContext.getResources().getIdentifier("miui_volume_timer_seelbar_width", "dimen", "miui.systemui.plugin");
                }
                else {
                    seekbarWidthResId = (int) mTimerSeekbarWidth;
                }
                int mTimerSeekbarMarginLeft = mContext.getResources().getIdentifier("miui_volume_timer_seekbar_margin_left", "dimen", "miui.systemui.plugin");
                float seekWidth = mContext.getResources().getDimension(seekbarWidthResId);
                int marginLeft = mContext.getResources().getDimensionPixelSize(mTimerSeekbarMarginLeft);
                int seg = (int) XposedHelpers.getObjectField(param.getThisObject(), "mDeterminedSegment");
                param.returnAndSkip(seekWidth / 10 * seg + marginLeft - halfTimerWidth);
            }
        });

        MethodHook segHook = new MethodHook() {
            int prevSeg = 0;
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                prevSeg = XposedHelpers.getIntField(param.getThisObject(), "mCurrentSegment");
                if (prevSeg < 3 || (prevSeg == 3 && XposedHelpers.getIntField(param.getThisObject(), "mDeterminedSegment") == 3)) {
                    XposedHelpers.setIntField(param.getThisObject(), "mCurrentSegment", 0);
                }
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setIntField(param.getThisObject(), "mCurrentSegment", prevSeg);
            }
        };

        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "updateDrawables", segHook);
    }

    public static void CCTileCornerHook(ClassLoader pluginLoader) {
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.ExpandableIconView", pluginLoader, "setCornerRadius", float.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getPluginContext");
                float radius = 18;
                if (scaledTileWidthDim > 0) {
                    radius *= scaledTileWidthDim / 65;
                }
                param.getArgs()[0] = mContext.getResources().getDisplayMetrics().density * radius;
            }
        });

        ModuleHelper.findAndHookMethod("miui.systemui.dagger.PluginComponentFactory", pluginLoader, "create", Context.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context) param.getArgs()[0];
                int enabledTileBackgroundResId = mContext.getResources().getIdentifier("qs_background_enabled", "drawable", "miui.systemui.plugin");
                int enabledTileColorResId = mContext.getResources().getIdentifier("qs_enabled_color", "color", "miui.systemui.plugin");
                int tintColor = mContext.getResources().getColor(enabledTileColorResId, null);
                Resources modRes = ModuleHelper.getModuleRes(mContext);
                MethodHook imgHook = new MethodHook() {
                    @Override
                    protected void before(final BeforeHookCallback param) throws Throwable {
                        int resId = (int) param.getArgs()[0];
                        if (resId == enabledTileBackgroundResId && resId != 0) {
                            Drawable enableTile = modRes.getDrawable(R.drawable.ic_qs_tile_bg_enabled, null);
                            enableTile.setTint(tintColor);
                            param.returnAndSkip(enableTile);
                        }
                    }
                };
                ModuleHelper.findAndHookMethod("android.content.res.Resources", pluginLoader, "getDrawable", int.class, imgHook);
                ModuleHelper.findAndHookMethod("android.content.res.Resources.Theme", pluginLoader, "getDrawable", int.class, imgHook);
            }
        });
    }

    private static boolean isSlidingStart = false;
    private static boolean isSliding = false;
    private static float tapStartX = 0;
    private static float tapStartY = 0;
    private static float tapStartPointers = 0;
    private static float tapStartBrightness = 0;
    private static float topMinimumBacklight = 0.0f;
    private static float topMaximumBacklight = 1.0f;
    private static float currentTouchX = 0;
    private static long currentTouchTime = 0;
    private static long currentDownTime = 0;
    private static float currentDownX = 0;

    public static void StatusBarGesturesHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "setExpandedHeightInternal", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                float mExpandedFraction = (float) XposedHelpers.callMethod(param.getThisObject(), "getExpandedFraction");
                if (mExpandedFraction > 0.33f) {
                    currentTouchTime = 0;
                    currentTouchX = 0;
                    currentDownTime = 0;
                    currentDownX = 0;
                }
            }
        });

        MethodHook hook = new MethodHook() {
            Object mBrightnessController = null;
            private int sbHeight = -1;
            @Override
            @SuppressLint("SetTextI18n")
            protected void before(final BeforeHookCallback param) throws Throwable {
                String clsName = param.getThisObject().getClass().getSimpleName();
                boolean isInControlCenter = "ControlPanelWindowView".equals(clsName) || "ControlCenterWindowViewImpl".equals(clsName);
                if (isInControlCenter) {
                    if (param.getArgs().length == 2 && (boolean) param.getArgs()[1]) {
                        return ;
                    }
                    Object statusBarStateController = XposedHelpers.getObjectField(param.getThisObject(), "statusBarStateController");
                    int state = (int) XposedHelpers.callMethod(statusBarStateController, "getState");
                    if (state == 1 || state == 2) {
                        return;
                    }
                }
                Context mContext = isInControlCenter ? ((View)param.getThisObject()).getContext() : (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Resources res = mContext.getResources();
                if (sbHeight == -1) {
                    sbHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
                }
                MotionEvent event = (MotionEvent)param.getArgs()[0];
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        tapStartX = event.getX();
                        tapStartY = event.getY();
                        isSlidingStart = isInControlCenter ? tapStartY <= sbHeight : !XposedHelpers.getBooleanField(param.getThisObject(), "mPanelExpanded");
                        tapStartPointers = 1;
                        if (mBrightnessController == null) {
                            Object mControlCenterController;
                            if (isInControlCenter) {
                                mControlCenterController = XposedHelpers.getObjectField(param.getThisObject(), "controlCenterController");
                            }
                            else {
                                mControlCenterController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", findClassIfExists("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.getClassLoader()));
                            }
                            mBrightnessController = XposedHelpers.callMethod(XposedHelpers.getObjectField(mControlCenterController, "brightnessController"), "get");
                        }
                        Object mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
                        int mDisplayId = mContext.getDisplay().getDisplayId();
                        topMinimumBacklight = (float) XposedHelpers.getObjectField(mBrightnessController, "mMinimumBacklight");
                        topMaximumBacklight = (float) XposedHelpers.getObjectField(mBrightnessController, "mMaximumBacklight");
                        tapStartBrightness = (float) XposedHelpers.callMethod(mDisplayManager, "getBrightness", mDisplayId);
                        if (isSlidingStart) {
                            currentDownTime = currentTimeMillis();
                            currentDownX = tapStartX;
                        }
                        else {
                            currentDownTime = 0;
                            currentDownX = 0;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        tapStartPointers = event.getPointerCount();
                        break;
                    case MotionEvent.ACTION_UP:
                        long lastTouchTime = currentTouchTime;
                        float lastTouchX = currentTouchX;
                        currentTouchTime = currentTimeMillis();
                        currentTouchX = event.getX();
                        float mTouchX = currentTouchX;
                        long mTouchTime = currentTouchTime;
                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(mTouchX - lastTouchX) < 100F) {
                            currentTouchTime = 0L;
                            currentTouchX = 0F;
                            GlobalActions.handleAction(mContext, "system_statusbarcontrols_dt");
                        }
                        if ((mTouchTime - currentDownTime > 600 && mTouchTime - currentDownTime < 4000)
                            && Math.abs(mTouchX - currentDownX) < 100F) {
                            if (MainModule.mPrefs.getBoolean("system_statusbarcontrols_longpress_vibrate")) {
                                boolean ignoreOff = MainModule.mPrefs.getBoolean("system_statusbarcontrols_longpress_vibrate_ignoreoff");
                                Helpers.performStrongVibration(mContext, ignoreOff);
                            }
                            GlobalActions.handleAction(mContext, "system_statusbarcontrols_longpress");
                        }
                        currentDownTime = 0L;
                        currentDownX = 0;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isSlidingStart = false;
                        isSliding = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isSlidingStart) return;
                        if (event.getY() - tapStartY > sbHeight) {
                            currentDownTime = 0;
                            currentDownX = 0;
                            return;
                        }
                        DisplayMetrics metrics = res.getDisplayMetrics();
                        float delta = event.getX() - tapStartX;
                        if (delta == 0) return;
                        if (!isSliding && Math.abs(delta) > metrics.widthPixels / 10f) isSliding = true;
                        if (!isSliding) return;
                        int opt = MainModule.mPrefs.getStringAsInt(tapStartPointers == 2 ? "system_statusbarcontrols_dual" : "system_statusbarcontrols_single", 1);
                        if (opt == 2) {
                            int sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_bright", 2);
                            float ratio = delta / metrics.widthPixels;
                            ratio = (sens == 1 ? 0.66f : (sens == 3 ? 1.66f : 1.0f)) * ratio * 0.618f;
                            float nextLevel = Math.min(topMaximumBacklight, Math.max(topMinimumBacklight, tapStartBrightness + (topMaximumBacklight - topMinimumBacklight) * ratio));
                            XposedHelpers.callMethod(mBrightnessController, "setBrightness", nextLevel);
                        } else if (opt == 3) {
                            int sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_vol", 2);
                            if (Math.abs(delta) < metrics.widthPixels / ((sens == 1 ? 0.66f : (sens == 3 ? 1.66f : 1.0f)) * 20 * metrics.density)) return;
                            tapStartX = event.getX();
                            AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
                            audioManager.adjustVolume(delta > 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_VIBRATE);
                        }
                        break;
                }
            }
        };
        String eventMethod = "onTouchEvent";
        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), eventMethod, MotionEvent.class, hook);
        String pluginLoaderClass = "com.android.systemui.shared.plugins.PluginInstance$Factory";
        ModuleHelper.hookAllMethods(pluginLoaderClass, lpparam.getClassLoader(), "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.getArgs()[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.ControlCenterWindowViewImpl", pluginLoader, "handleMotionEvent", MotionEvent.class, boolean.class, hook);
                }
            }
        });
    }

    public static void HorizMarginHook(PackageLoadedParam lpparam) {
        MethodHook horizHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_left", 16);
                float marginLeft = Helpers.dp2px(leftMargin);
                leftMargin = (int) marginLeft;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_right", 16);
                float marginRight = Helpers.dp2px(rightMargin);
                rightMargin = (int) marginRight;
                param.returnAndSkip(new Pair<Integer, Integer>(Integer.valueOf(leftMargin), Integer.valueOf(rightMargin)));
            }
        };
        String StatusBarWindowViewCls = "com.android.systemui.statusbar.window.StatusBarWindowView";
        ModuleHelper.hookAllMethods(StatusBarWindowViewCls, lpparam.getClassLoader(), "paddingNeededForCutoutAndRoundedCorner", horizHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarContentInsetsProvider", lpparam.getClassLoader(), "getStatusBarContentInsetsForCurrentRotation", horizHook);
    }

    public static void LockScreenTopMarginHook(PackageLoadedParam lpparam) {
        final int[] statusBarPaddingTop = new int[1];
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext");
                int dimenResId = mContext.getResources().getIdentifier("status_bar_padding_top", "dimen", lpparam.getPackageName());
                statusBarPaddingTop[0] = mContext.getResources().getDimensionPixelSize(dimenResId);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.getClassLoader(), "updateViewStatusBarPaddingTop", View.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View view = (View) param.getArgs()[0];
                if (view != null) {
                    view.setPadding(view.getPaddingLeft(), statusBarPaddingTop[0], view.getPaddingRight(), view.getPaddingBottom());
                    param.returnAndSkip(null);
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.callMethod(param.getThisObject(), "onDensityOrFontScaleChanged");
            }
        });
    }

    public static void HideIconsClockHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "showClock", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.callMethod(param.getThisObject(), "hideClockInternal", 8, false);
                if (!newStyle) {
                    XposedHelpers.callMethod(param.getThisObject(), "hideNetworkSpeedSplitter", 8, false);
                }
                param.returnAndSkip(null);
            }
        });
    }

    public static void HideIconsVoWiFiHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethodSilently("com.android.systemui.MiuiOperatorCustomizedPolicy$MiuiOperatorConfig", lpparam.getClassLoader(), "getHideVowifi", HookerClassHelper.returnConstant(true));
    }

    public static void HideIconsSignalHook(PackageLoadedParam lpparam) {
        MethodHook beforeUpdate = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mobileIconState = param.getArgs()[0];
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_signal")) {
                    if (!MainModule.mPrefs.getBoolean("system_statusbaricons_signal_wificonnected") || XposedHelpers.getBooleanField(mobileIconState, "wifiAvailable")) {
                        XposedHelpers.setObjectField(mobileIconState, "visible", false);
                        return;
                    }
                }
                int subId = (int) XposedHelpers.getObjectField(mobileIconState, "subId");
                int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                int slotId = SubscriptionManager.getSlotIndex(subId);
                if ((MainModule.mPrefs.getBoolean("system_statusbaricons_sim1") && slotId == 0)
                    || (MainModule.mPrefs.getBoolean("system_statusbaricons_sim2") && slotId == 1)
                    || (MainModule.mPrefs.getBoolean("system_statusbaricons_sim_nodata") && subId != dataSubId)
                ) {
                    XposedHelpers.setObjectField(mobileIconState, "visible", false);
                    return;
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_roaming")) {
                    XposedHelpers.setObjectField(mobileIconState, "roaming", false);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_volte")) {
                    XposedHelpers.setObjectField(mobileIconState, "volte", false);
                    XposedHelpers.setObjectField(mobileIconState, "speechHd", false);
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "initViewState", beforeUpdate);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "updateState", beforeUpdate);
    }

    private static boolean checkSlot(String slotName) {
        try {
            return "headset".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_headset") ||
                "volume".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_sound") ||
                "zen".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_dnd") ||
                "alarm_clock".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_alarm") ||
                "managed_profile".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_profile") ||
                "vpn".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_vpn") ||
                "airplane".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_airplane") ||
                "nfc".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nfc") ||
                "second_space".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_secondspace") ||
                "location".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_gps") ||
                "wifi".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_wifi") ||
                "slave_wifi".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_dualwifi") ||
                "hotspot".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_hotspot") ||
                "no_sim".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nosims") ||
                "bluetooth_handsfree_battery".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_btbattery") ||
                "ble_unlock_mode".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_ble_unlock") ||
                "hd".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_volte");
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return false;
        }
    }

    public static void HideIconsHook(PackageLoadedParam lpparam) {
        MethodHook iconHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String iconType = (String)param.getArgs()[0];
                if (checkSlot(iconType)) {
                    param.getArgs()[1] = false;
                }
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIconVisibility", String.class, boolean.class, iconHook);
        if (!newStyle) {
            ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.getClassLoader(), "setIconVisibility", String.class, boolean.class, iconHook);
        }
    }


    public static void HideIconsFromSystemManager(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIcon", String.class, "com.android.internal.statusbar.StatusBarIcon", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String slotName = (String)param.getArgs()[0];
                if (
                    ("stealth".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_privacy"))
                        || "mute".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_mute")
                        || "speakerphone".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_speaker")
                        || "call_record".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_record")
                ){
                    XposedHelpers.setObjectField(param.getArgs()[1], "visible", false);
                }
            }
        });
    }

    public static void BatteryIndicatorHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods(StatusBarCls, lpparam.getClassLoader(), "createAndAddWindows", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Object sbWindowController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarWindowController");
                ViewGroup mStatusBarWindow = (ViewGroup) XposedHelpers.getObjectField(sbWindowController, "mStatusBarWindowView");

                BatteryIndicator indicator = new BatteryIndicator(mContext);
                View panel = mStatusBarWindow.findViewById(mContext.getResources().getIdentifier("notification_panel", "id", lpparam.getPackageName()));
                mStatusBarWindow.addView(indicator, panel != null ? mStatusBarWindow.indexOfChild(panel) + 1 : Math.max(mStatusBarWindow.getChildCount() - 1, 2));
                indicator.setAdjustViewBounds(false);
                indicator.init(param.getThisObject());
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator", indicator);
                Object mNotificationIconAreaController = XposedHelpers.getObjectField(param.getThisObject(), "mNotificationIconAreaController");
                XposedHelpers.setAdditionalInstanceField(mNotificationIconAreaController, "mBatteryIndicator", indicator);
                Object mBatteryController = XposedHelpers.getObjectField(param.getThisObject(), "mBatteryController");
                XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator);
                XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged");
                XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged");
            }
        });

        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "setPanelExpanded", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.getThisObject(), "isKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && (boolean)param.getArgs()[0]);
            }
        });

        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "setQsExpanded", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.getThisObject(), "isKeyguardShowing");
                if (!isKeyguardShowing) return;
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged((boolean)param.getArgs()[0]);
            }
        });

        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "updateIsKeyguard", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.getThisObject(), "isKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onKeyguardStateChanged(isKeyguardShowing);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.getClassLoader(), "onDarkChanged", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onDarkModeChanged((float)param.getArgs()[1], (int)param.getArgs()[2]);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl", lpparam.getClassLoader(), "fireBatteryLevelChanged", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                int mLevel = XposedHelpers.getIntField(param.getThisObject(), "mLevel");
                boolean mCharging = XposedHelpers.getBooleanField(param.getThisObject(), "mCharging");
                boolean mCharged = XposedHelpers.getBooleanField(param.getThisObject(), "mCharged");
                if (indicator != null) indicator.onBatteryLevelChanged(mLevel, mCharging, mCharged);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.getClassLoader(), "firePowerSaveChanged", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onPowerSaveChanged(XposedHelpers.getBooleanField(param.getThisObject(), "mPowerSave"));
            }
        });
    }
    public static void TempHideOverlaySystemUIHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.wm.shell.pip.PipTaskOrganizer", lpparam.getClassLoader(), "onTaskAppeared", new MethodHook() {
            private boolean isActListened = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                if (!isActListened) {
                    isActListened = true;
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("miui.intent.TAKE_SCREENSHOT");
                    mContext.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (action == null) return;
                            if (action.equals("miui.intent.TAKE_SCREENSHOT")) {
                                boolean state = intent.getBooleanExtra("IsFinished", true);
                                Object mState = XposedHelpers.getObjectField(param.getThisObject(), "mPipTransitionState");
                                boolean isPip = (boolean) XposedHelpers.callMethod(mState, "isInPip");
                                if (isPip) {
                                    Object mSurfaceControlTransactionFactory = XposedHelpers.getObjectField(param.getThisObject(), "mSurfaceControlTransactionFactory");
                                    SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) XposedHelpers.callMethod(mSurfaceControlTransactionFactory, "getTransaction");
                                    SurfaceControl mLeash = (SurfaceControl) XposedHelpers.getObjectField(param.getThisObject(), "mLeash");
                                    transaction.setVisibility(mLeash, state);
                                    transaction.apply();
                                }
                            }
                        }
                    }, intentFilter);
                }
            }
        });
    }

    private static long measureTime = 0;
    private static long txBytesTotal = 0;
    private static long rxBytesTotal = 0;
    private static long txSpeed = 0;
    private static long rxSpeed = 0;

    private static Pair<Long, Long> getTrafficBytes(Object thisObject) {
        long tx = -1L;
        long rx = -1L;

        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements();) {
                NetworkInterface iface = list.nextElement();
                if (iface.isUp() && !iface.isVirtual() && !iface.isLoopback() && !iface.isPointToPoint() && !"".equals(iface.getName())) {
                    tx += (long)XposedHelpers.callStaticMethod(TrafficStats.class, "getTxBytes", iface.getName());
                    rx += (long)XposedHelpers.callStaticMethod(TrafficStats.class, "getRxBytes", iface.getName());
                }
            }
        } catch (Throwable t) {
            XposedHelpers.log(t);
            tx = TrafficStats.getTotalTxBytes();
            rx = TrafficStats.getTotalRxBytes();
        }

        return new Pair<Long, Long>(tx, rx);
    }

    @SuppressLint("DefaultLocale")
    private static String humanReadableByteCount(Context ctx, long bytes) {
        try {
            Resources modRes = ModuleHelper.getModuleRes(ctx);
            boolean hideSecUnit = MainModule.mPrefs.getBoolean("system_detailednetspeed_secunit");
            String unitSuffix = modRes.getString(R.string.Bs);
            if (hideSecUnit) {
                unitSuffix = "";
            }
            float f = (bytes) / 1024.0f;
            int expIndex = 0;
            if (f > 999.0f) {
                expIndex = 1;
                f /= 1024.0f;
            }
            char pre = modRes.getString(R.string.speedunits).charAt(expIndex);
            return (f < 100.0f ? String.format("%.1f", f) : String.format("%.0f", f)) + String.format("%s" + unitSuffix, pre);
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return "";
        }
    }

    public static void NetSpeedIntervalHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), "postUpdateNetworkSpeedDelay", long.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                long originInterval = (long) param.getArgs()[0];
                if (originInterval == 4000L) {
                    long newInterval = MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000L;
                    param.getArgs()[0] = newInterval;
                }
            }
        });
    }

    public static void DetailedNetSpeedHook(PackageLoadedParam lpparam) {
        Class<?> nscCls = findClassIfExists("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader());
        if (nscCls == null) {
            XposedHelpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller");
            return;
        }

        ModuleHelper.findAndHookMethod(nscCls, "getTotalByte", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Pair<Long, Long> bytes = getTrafficBytes(param.getThisObject());
                txBytesTotal = bytes.first;
                rxBytesTotal = bytes.second;
                measureTime = nanoTime();
            }
        });

        ModuleHelper.findAndHookMethod(nscCls, "updateNetworkSpeed", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean isConnected = false;
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network nw = mConnectivityManager.getActiveNetwork();
                if (nw != null) {
                    NetworkCapabilities capabilities = mConnectivityManager.getNetworkCapabilities(nw);
                    if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                        isConnected = true;
                    }
                }
                if (isConnected) {
                    long nanoTime = nanoTime();
                    long newTime = nanoTime - measureTime;
                    measureTime = nanoTime;
                    if (newTime == 0) newTime = Math.round(4 * Math.pow(10, 9));
                    Pair<Long, Long> bytes = getTrafficBytes(param.getThisObject());
                    long newTxBytes = bytes.first;
                    long newRxBytes = bytes.second;
                    long newTxBytesFixed = newTxBytes - txBytesTotal;
                    long newRxBytesFixed = newRxBytes - rxBytesTotal;
                    if (newTxBytesFixed < 0 || txBytesTotal == 0) newTxBytesFixed = 0;
                    if (newRxBytesFixed < 0 || rxBytesTotal == 0) newRxBytesFixed = 0;
                    txSpeed = Math.round(newTxBytesFixed / (newTime / Math.pow(10, 9)));
                    rxSpeed = Math.round(newRxBytesFixed / (newTime / Math.pow(10, 9)));
                    txBytesTotal = newTxBytes;
                    rxBytesTotal = newRxBytes;
                } else {
                    txSpeed = 0;
                    rxSpeed = 0;
                }
            }
        });

        ModuleHelper.hookAllMethods(nscCls, "updateText", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                boolean hideLow = MainModule.mPrefs.getBoolean("system_detailednetspeed_low");
                int lowLevel = MainModule.mPrefs.getInt("system_detailednetspeed_lowlevel", 1) * 1024;
                int icons = Integer.parseInt(MainModule.mPrefs.getString("system_detailednetspeed_icon", "2"));

                String txarrow = "";
                String rxarrow = "";
                if (icons == 2) {
                    txarrow = txSpeed < lowLevel ? "△" : "▲";
                    rxarrow = rxSpeed < lowLevel ? "▽" : "▼";
                } else if (icons == 3) {
                    txarrow = txSpeed < lowLevel ? " ☖" : " ☗";
                    rxarrow = rxSpeed < lowLevel ? " ⛉" : " ⛊";
                }

                String tx = hideLow && txSpeed < lowLevel ? "" : humanReadableByteCount(mContext, txSpeed) + txarrow;
                String rx = hideLow && rxSpeed < lowLevel ? "" : humanReadableByteCount(mContext, rxSpeed) + rxarrow;
                if (newStyle) {
                    String[] strArr = new String[2];
                    strArr[0] = tx + "\n" + rx;
                    strArr[1] = "";
                    param.getArgs()[0] = strArr;
                }
                else {
                    param.getArgs()[0] = tx + "\n" + rx;
                }
            }
        });
    }

    private static Bitmap processAlbumArt(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null) return bitmap;
        int rescale = MainModule.mPrefs.getStringAsInt("system_albumartonlock_scale", 1);
        boolean grayscale = MainModule.mPrefs.getBoolean("system_albumartonlock_gray");
        if (rescale == 1 && !grayscale) return bitmap;

        Paint paint = new Paint();
        Matrix transformation = new Matrix();
        int width = 0;
        int height = 0;

        if (grayscale) {
            width = bitmap.getWidth();
            height = bitmap.getHeight();

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        }

        if (rescale != 1) {
            Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            width = point.x;
            height = point.y;

            float originalWidth = bitmap.getWidth();
            float originalHeight = bitmap.getHeight();
            float scale = rescale == 2 ? Math.min(width / originalWidth, height / originalHeight) : Math.max(width / originalWidth, height / originalHeight);
            float xTranslation = (width - originalWidth * scale) / 2.0f;
            float yTranslation = (height - originalHeight * scale) / 2.0f;

            transformation.postTranslate(xTranslation, yTranslation);
            transformation.preScale(scale, scale);

            paint.setFilterBitmap(true);
        }

        Bitmap processed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(processed);
        canvas.drawBitmap(bitmap, transformation, paint);
        return processed;
    }

    public static void LockScreenAlbumArtHook(PackageLoadedParam lpparam) {
        Class<?> MiuiThemeUtilsClass = findClassIfExists("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.getClassLoader());

        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (isDefaultLockScreenTheme) {
                    Object mBlurRatioChangedListener = XposedHelpers.getObjectField(param.getThisObject(), "mBlurRatioChangedListener");
                    Object notificationShadeDepthController = XposedHelpers.getObjectField(param.getThisObject(), "notificationShadeDepthController");
                    XposedHelpers.callMethod(notificationShadeDepthController, "removeListener", mBlurRatioChangedListener);
                    View view = (View) XposedHelpers.getObjectField(param.getThisObject(), "mThemeBackgroundView");
                    view.setAlpha(1.0f);

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                    view.getContext().registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (action == null) return;
                            if (action.equals(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART")) {
                                try {
                                    XposedHelpers.callMethod(param.getThisObject(), "updateThemeBackground");
                                }
                                catch (Throwable e) {
                                    XposedHelpers.callMethod(param.getThisObject(), "updateThemeBackgroundVisibility");
                                }
                            }
                        }
                    }, intentFilter);
                }
            }
        });
        MethodHook updateLockscreenHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (!isDefaultLockScreenTheme) {
                    return ;
                }
                View view = (View) XposedHelpers.getObjectField(param.getThisObject(), "mThemeBackgroundView");
                boolean isOnShade = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isOnShade");
                if (isOnShade) {
                    view.setVisibility(View.GONE);
                }
                else {
                    Object mAlbumArt = XposedHelpers.getAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt");
                    if (mAlbumArt != null) {
                        view.setBackground(new BitmapDrawable(view.getContext().getResources(), (Bitmap) mAlbumArt));
                    }
                    view.setVisibility(mAlbumArt != null ? View.VISIBLE : View.GONE);
                }
                param.returnAndSkip(null);
            }
        };
        ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updateThemeBackground", updateLockscreenHook);
        ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updateThemeBackgroundVisibility", updateLockscreenHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.getClassLoader(), "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (!isDefaultLockScreenTheme) {
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null);
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null);
                    return;
                }
                MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.getThisObject(), "mMediaMetadata");
                Bitmap art = null;
                if (mMediaMetadata != null) {
                    art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
                }
                Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource");
                try {
                    if (art == null && mAlbumArt == null) return;
                    if (art != null && art.sameAs(mAlbumArt)) return;
                } catch (Throwable ignore) {}
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", art);

                int blur = MainModule.mPrefs.getInt("system_albumartonlock_blur", 0);
                Bitmap blurArt = processAlbumArt(mContext, art != null && blur > 0 ? Helpers.fastBlur(art, blur + 1) : art);
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", blurArt);

                Intent updateAlbumWallpaper = new Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                updateAlbumWallpaper.setPackage("com.android.systemui");
                mContext.sendBroadcast(updateAlbumWallpaper);

                if (blurArt != null) {
                    Intent updateFakeWallpaper = new Intent("miui.intent.action.LOCK_WALLPAPER_CHANGED");
                    updateFakeWallpaper.setPackage("com.android.systemui");
                    WallpaperColors fromBitmap = WallpaperColors.fromBitmap(blurArt);
                    boolean isWallpaperColorLight = (fromBitmap.getColorHints() & 1) == 1;
                    updateFakeWallpaper.putExtra("is_wallpaper_color_light", isWallpaperColorLight);
                    mContext.sendBroadcast(updateFakeWallpaper);
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.getClassLoader(), "clearCurrentMediaNotification", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null);
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null);
                if (isDefaultLockScreenTheme) {
                    Intent updateAlbumWallpaper = new Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                    updateAlbumWallpaper.setPackage("com.android.systemui");
                    mContext.sendBroadcast(updateAlbumWallpaper);
                }
            }
        });
    }

    private static boolean modifyCameraImage(Context mContext, View mKeyguardRightView, boolean mDarkMode) {
        if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        final String key = "system_lockscreenshortcuts_right";
        int action = MainModule.mPrefs.getInt(key + "_action", 1);
        if (action <= 1) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        String str = ModuleHelper.getActionName(mContext, key);
        if (str == null) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        Drawable icon = ModuleHelper.getActionImage(mContext, key);
        mKeyguardRightView.setBackgroundColor(Color.TRANSPARENT);
        mKeyguardRightView.setForeground(icon);
        mKeyguardRightView.setForegroundGravity(Gravity.CENTER);

        float density = mContext.getResources().getDisplayMetrics().density;
        int size = Math.round(mContext.getResources().getConfiguration().smallestScreenWidthDp * density);

        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShadowLayer(2 * density, 0, 0, mDarkMode ? Color.argb(90, 255, 255, 255) : Color.argb(90, 0, 0, 0));
        paint.setTextSize(20 * density);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density);
        paint.setColor(mDarkMode ? Color.WHITE : Color.BLACK);
        paint.setAlpha(90);

        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        float x = size / 2f - bounds.width() / 2f;
        float y = size / 2f + bounds.height() / 2f + (icon == null ? 0 : icon.getIntrinsicHeight() / 2f + 30 * density);
        canvas.drawText(str, x, y, paint);
        paint.setStyle(Paint.Style.FILL);
//		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.clearShadowLayer();
        paint.setColor(mDarkMode ? Color.BLACK : Color.WHITE);
        paint.setAlpha(mDarkMode ? 160 : 230);
        canvas.drawText(str, x, y, paint);

        BitmapDrawable bmpDrawable = new BitmapDrawable(mContext.getResources(), bmp);
        if (mKeyguardRightView instanceof ImageView) {
            ((ImageView)mKeyguardRightView).setScaleType(ImageView.ScaleType.CENTER);
            ((ImageView)mKeyguardRightView).setImageDrawable(bmpDrawable);
        } else {
            bmpDrawable.setGravity(Gravity.CENTER);
            mKeyguardRightView.setBackground(bmpDrawable);
        }

        return true;
    }

    private static void restoreCameraImage(View mKeyguardRightView) {
        mKeyguardRightView.setBackgroundColor(Color.BLACK);
        mKeyguardRightView.setForeground(null);
        if (mKeyguardRightView instanceof ImageView)
            ((ImageView)mKeyguardRightView).setScaleType(ImageView.ScaleType.FIT_END);
    }

    private static Object notificationPanelView = null;
    public static void LockScreenShortcutHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$MiuiDefaultLeftButton", lpparam.getClassLoader(), "getIcon", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object img = param.getResult();
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
                    Object thisObject = XposedHelpers.getSurroundingThis(param.getThisObject());
                    Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
                    boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkStyle");
                    Drawable flashlightDrawable = ModuleHelper.getModuleRes(mContext).getDrawable(
                        mDarkMode ? R.drawable.keyguard_bottom_flashlight_img_dark : R.drawable.keyguard_bottom_flashlight_img_light,
                        mContext.getTheme()
                    );
                    XposedHelpers.setObjectField(img, "drawable", flashlightDrawable);
                } else if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    XposedHelpers.setObjectField(img, "isVisible", false);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$MiuiDefaultRightButton", lpparam.getClassLoader(), "getIcon", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object img = param.getResult();
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    XposedHelpers.setObjectField(img, "isVisible", false);
                    return;
                }

                boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
                if (!opt) return;
                Object thisObject = XposedHelpers.getSurroundingThis(param.getThisObject());
                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
                boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkStyle");
                XposedHelpers.setObjectField(img, "drawable", ModuleHelper.getModuleRes(mContext).getDrawable(
                    mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light,
                    mContext.getTheme()
                ));
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.getClassLoader(), "initTipsView", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
                if (!opt) return;
                boolean isLeft = (boolean) param.getArgs()[0];
                if (!isLeft) {
                    TextView mRightAffordanceViewTips = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mRightAffordanceViewTips");
                    if (mRightAffordanceViewTips != null)
                        mRightAffordanceViewTips.setText(ModuleHelper.getModuleRes(mRightAffordanceViewTips.getContext()).getString(R.string.system_lockscreenshortcuts_right_image_hint));
                }
            }
        });

        if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    View mLeftAffordanceView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mLeftAffordanceView");
                    mLeftAffordanceView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Object flashlightController = XposedHelpers.getObjectField(param.getThisObject(), "mFlashlightController");
                            boolean z = !(boolean) XposedHelpers.callMethod(flashlightController, "isEnabled");
                            XposedHelpers.callMethod(flashlightController, "setFlashlight", z);
                            XposedHelpers.callMethod(param.getThisObject(), "updateLeftAffordanceIcon");
                            return true;
                        }
                    });
                }
            });

            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.getClassLoader(), "updateLeftAffordanceIcon", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Object mLeftAffordanceView = XposedHelpers.getObjectField(param.getThisObject(), "mLeftAffordanceView");
                    Object flashlightController = XposedHelpers.getObjectField(param.getThisObject(), "mFlashlightController");
                    boolean isOn = (boolean) XposedHelpers.callMethod(flashlightController, "isEnabled");
                    XposedHelpers.callMethod(mLeftAffordanceView, "setCircleRadiusWithoutAnimation", isOn ? 66f : 0f);
                }
            });

            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.getClassLoader(), "onClick", View.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    View view = (View) param.getArgs()[0];
                    View mLeftAffordanceView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mLeftAffordanceView");
                    if (view == mLeftAffordanceView) {
                        param.returnAndSkip(null);
                    }
                }
            });
        }

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.getClassLoader(), "launchCamera", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                if (GlobalActions.handleAction(mContext, "system_lockscreenshortcuts_right", true)) {
                    param.returnAndSkip(null);
                    Object PanelInjector = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", findClass("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.getClassLoader()));
                    Object panelController = XposedHelpers.getObjectField(PanelInjector, "mPanelViewController");
                    final View mNotificationPanelView = (View) XposedHelpers.getObjectField(PanelInjector, "mPanelView");
                    mNotificationPanelView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            XposedHelpers.callMethod(panelController, "resetViews", false);
                        }
                    }, 500);
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.keyguard.MiuiKeyguardCameraView", lpparam.getClassLoader(), "setDarkStyle", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image")) {
                    Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    boolean mDarkMode = XposedHelpers.getBooleanField(param.getThisObject(), "mDarkStyle");
                    XposedHelpers.callMethod(param.getThisObject(), "setPreviewImageDrawable", ModuleHelper.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light, mContext.getTheme()));
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.getClassLoader(), "updatePreView", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View mPreViewContainer = (View) XposedHelpers.getObjectField(param.getThisObject(), "mPreViewContainer");
                if ("active".equals(mPreViewContainer.getTag())) {
                    XposedHelpers.setFloatField(param.getThisObject(), "mIconCircleAlpha", 0.0f);
                    ((View) param.getThisObject()).invalidate();
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.keyguard.MiuiKeyguardCameraView", lpparam.getClassLoader(), "setPreviewImageDrawable", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");

                boolean mDarkMode = XposedHelpers.getBooleanField(param.getThisObject(), "mDarkStyle");
                ImageView mIconView = (ImageView) XposedHelpers.getObjectField(param.getThisObject(), "mIconView");
                if (mIconView != null)
                    if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image"))
                        mIconView.setImageDrawable(ModuleHelper.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light, mContext.getTheme()));
                    else
                        mIconView.setImageDrawable(mContext.getDrawable(mDarkMode ? mContext.getResources().getIdentifier("keyguard_bottom_camera_img_dark", "drawable", lpparam.getPackageName()) : mContext.getResources().getIdentifier("keyguard_bottom_camera_img", "drawable", lpparam.getPackageName())));

                View mPreView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mPreView");
                View mPreViewContainer = (View) XposedHelpers.getObjectField(param.getThisObject(), "mPreViewContainer");
                View mBackgroundView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mBackgroundView");
                Paint mIconCircleStrokePaint = (Paint) XposedHelpers.getObjectField(param.getThisObject(), "mIconCircleStrokePaint");
                ViewOutlineProvider mPreViewOutlineProvider = (ViewOutlineProvider) XposedHelpers.getObjectField(param.getThisObject(), "mPreViewOutlineProvider");
                boolean result = modifyCameraImage(mContext, mPreView, mDarkMode);
                if (result) param.returnAndSkip(null);
                if (mPreViewContainer != null) {
                    mPreViewContainer.setBackgroundColor(result ? Color.TRANSPARENT : Color.BLACK);
                    mPreViewContainer.setOutlineProvider(result ? null : mPreViewOutlineProvider);
                    mPreViewContainer.setTag(result ? "active" : "inactive");
                }
                if (mBackgroundView != null)
                    mBackgroundView.setBackgroundColor(result ? Color.TRANSPARENT : Color.BLACK);
                if (mIconCircleStrokePaint != null)
                    mIconCircleStrokePaint.setColor(result ? Color.TRANSPARENT : Color.WHITE);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.getClassLoader(), "handleMoveDistanceChanged", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View mIconView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mIconView");
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    if (mIconView != null) mIconView.setVisibility(View.GONE);
                    param.returnAndSkip(null);
                } else if (mIconView != null) mIconView.setVisibility(View.VISIBLE);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.getClassLoader(), "startFullScreenAnim", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int action = MainModule.mPrefs.getInt("system_lockscreenshortcuts_right_action", 1);
                if (action <= 1) return;
                AnimatorSet mAnimatorSet = (AnimatorSet) XposedHelpers.getObjectField(param.getThisObject(), "mAnimatorSet");
                if (mAnimatorSet == null) return;
                param.setResult(null);
                mAnimatorSet.pause();
                mAnimatorSet.removeAllListeners();
                mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        GlobalActions.handleAction(mContext, "system_lockscreenshortcuts_right", true);
                        Object mCallBack = XposedHelpers.getObjectField(param.getThisObject(), "mCallBack");
                        if (mCallBack != null)
                            XposedHelpers.callMethod(mCallBack, "onCompletedAnimationEnd");
                        XposedHelpers.setBooleanField(param.getThisObject(), "mIsPendingStartCamera", false);
                        XposedHelpers.callMethod(param.getThisObject(), "dismiss");
                        View mBackgroundView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mBackgroundView");
                        if (mBackgroundView != null) mBackgroundView.setAlpha(1.0f);
                    }
                });
                mAnimatorSet.resume();
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.getClassLoader(), "setTranslation", float.class, boolean.class, boolean.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int mCurrentScreen = XposedHelpers.getIntField(param.getThisObject(), "mCurrentScreen");
                if (mCurrentScreen != 1) return;
                if ((float) param.getArgs()[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off"))
                    param.getArgs()[0] = 0.0f;
                else if ((float) param.getArgs()[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    param.getArgs()[0] = 0.0f;
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.getClassLoader(), "fling", float.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int mCurrentScreen = XposedHelpers.getIntField(param.getThisObject(), "mCurrentScreen");
                if (mCurrentScreen != 1) return;
                if ((float) param.getArgs()[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off"))
                    param.returnAndSkip(null);
                else if ((float) param.getArgs()[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    param.returnAndSkip(null);
            }
        });
    }

    public static void LockScreenSecureLaunchHook() {
        ModuleHelper.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new MethodHook() {
            @SuppressWarnings("ConstantConditions")
            protected void after(final AfterHookCallback param) throws Throwable {
                Activity act = (Activity)param.getThisObject();
                if (act == null) return;
                Intent intent = act.getIntent();
                if (intent == null) return;
                boolean mFromSecureKeyguard = intent.getBooleanExtra("StartActivityWhenLocked", false);
                boolean mStartedFromLockScreen = false;
                try {
                    mStartedFromLockScreen = (boolean)XposedHelpers.getAdditionalInstanceField(act.getApplication(), "wasStartedFromLockScreen");
                } catch (Throwable ignore) {}
                if (mFromSecureKeyguard || mStartedFromLockScreen) {
                    XposedHelpers.setAdditionalInstanceField(act.getApplication(), "wasStartedFromLockScreen", true);
                    act.setShowWhenLocked(true);
                    act.setInheritShowWhenLocked(true);
                }
            }
        });
    }

    private static final List<String> securedTiles = new ArrayList<String>();

    public static void SecureQSTilesHook(PackageLoadedParam lpparam) {
        Class<?> tileHostCls = findClassIfExists("com.android.systemui.qs.QSTileHost", lpparam.getClassLoader());

        MethodHook hook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                BroadcastReceiver mAfterUnlockReceiver = new BroadcastReceiver() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void onReceive(Context context, Intent intent) {
                        try {
                            String tileName = intent.getStringExtra("tileName");
                            boolean expandAfter = intent.getBooleanExtra("expandAfter", false);
                            boolean usingCenter = intent.getBooleanExtra("usingCenter", false);
                            if ("edit".equals(tileName) || expandAfter) {
                                Intent expandIntent = new Intent(ACTION_PREFIX + "ExpandSettings");
                                expandIntent.putExtra("forceExpand", true);
                                context.sendBroadcast(expandIntent);
                            }
                            LinkedHashMap<String, ?> mTiles = (LinkedHashMap<String, ?>)XposedHelpers.getObjectField(param.getThisObject(), "mTiles");
                            Object tile = mTiles.get(tileName);
                            if (tile == null) {
                                if (usingCenter) {
                                    Object mController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", findClassIfExists("com.android.systemui.miui.statusbar.policy.ControlPanelController", lpparam.getClassLoader()));
                                    Object mControlCenter = XposedHelpers.getObjectField(mController, "mControlCenter");
                                    Object mControlPanelContentView = XposedHelpers.getObjectField(mControlCenter, "mControlPanelContentView");
                                    Object mControlCenterPanel = XposedHelpers.callMethod(mControlPanelContentView, "getControlCenterPanel");
                                    Object mBigTile = null;
                                    if ("bt".equals(tileName)) mBigTile = XposedHelpers.getObjectField(mControlCenterPanel, "mBigTile1");
                                    else if ("cell".equals(tileName)) mBigTile = XposedHelpers.getObjectField(mControlCenterPanel, "mBigTile2");
                                    else if ("wifi".equals(tileName)) mBigTile = XposedHelpers.getObjectField(mControlCenterPanel, "mBigTile3");
                                    if (mBigTile != null) tile = XposedHelpers.getObjectField(mBigTile, "mQSTile");
                                    if (tile == null) return;
                                }
                                return;
                            }
                            XposedHelpers.setAdditionalInstanceField(tile, "mCalledAfterUnlock", true);
                            Method clickHandler = XposedHelpers.findMethodExact(tile.getClass(), "handleClick", View.class);
                            clickHandler.invoke(tile, new Object[]{ null });
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                };
                mContext.registerReceiver(mAfterUnlockReceiver, new IntentFilter(ACTION_PREFIX + "HandleQSTileClick"));
            }
        };

        ModuleHelper.hookAllConstructors(tileHostCls, hook);

        String FactoryImpl = "com.android.systemui.qs.tileimpl.MiuiQSFactory";
        ModuleHelper.findAndHookMethod(FactoryImpl, lpparam.getClassLoader(), "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object tile = param.getResult();
                if (tile == null) return;
                String tileClass = tile.getClass().getCanonicalName();
                final String tileName = (String)param.getArgs()[0];
                String name = tileName;
                if (name.startsWith("intent(")) name = "intent";
                else if (name.startsWith("custom(")) name = "custom";
                final HashSet<String> secureTitles = new HashSet<String>();
                if (MainModule.mPrefs.getBoolean("system_secureqs_wifi")) secureTitles.add("wifi");
                if (MainModule.mPrefs.getBoolean("system_secureqs_bt")) secureTitles.add("bt");
                if (MainModule.mPrefs.getBoolean("system_secureqs_mobiledata")) secureTitles.add("cell");
                if (MainModule.mPrefs.getBoolean("system_secureqs_airplane")) secureTitles.add("airplane");
                if (MainModule.mPrefs.getBoolean("system_secureqs_location")) secureTitles.add("gps");
                if (MainModule.mPrefs.getBoolean("system_secureqs_hotspot")) secureTitles.add("hotspot");
                if (MainModule.mPrefs.getBoolean("system_secureqs_nfc")) secureTitles.add("nfc");
                if (MainModule.mPrefs.getBoolean("system_secureqs_sync")) secureTitles.add("sync");
                if (MainModule.mPrefs.getBoolean("system_secureqs_edit")) secureTitles.add("edit");
                if (MainModule.mPrefs.getBoolean("system_secureqs_custom")) {
                    secureTitles.add("intent");
                    secureTitles.add("custom");
                }
                if (secureTitles.contains(name) && !securedTiles.contains(tileClass)) {
                    MethodHook hook = new MethodHook() {
                        @Override
                        protected void before(final BeforeHookCallback param) throws Throwable {
                            Boolean mCalledAfterUnlock = (Boolean)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mCalledAfterUnlock");
                            if (mCalledAfterUnlock != null && mCalledAfterUnlock) {
                                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mCalledAfterUnlock", false);
                                return;
                            }
                            Boolean isScreenLockDisabled = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader()), "isScreenLockDisabled");
                            isScreenLockDisabled = isScreenLockDisabled != null && isScreenLockDisabled;
                            if (isScreenLockDisabled) return;
                            Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                            KeyguardManager kgMgr = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (!kgMgr.isKeyguardLocked() || !kgMgr.isKeyguardSecure()) return;
                            Handler mHandler = new Handler(mContext.getMainLooper());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Class<?> DependencyClass = findClass("com.android.systemui.Dependency", lpparam.getClassLoader());
                                        String StatusbarClsForDep = "com.android.systemui.statusbar.phone.CentralSurfaces";
                                        Object mStatusBar = XposedHelpers.callStaticMethod(DependencyClass, "get", findClassIfExists(StatusbarClsForDep, lpparam.getClassLoader()));
                                        boolean usingControlCenter;
                                        Object mController = XposedHelpers.callStaticMethod(DependencyClass, "get", findClassIfExists("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.getClassLoader()));
                                        usingControlCenter = (boolean)XposedHelpers.callMethod(mController, "isUseControlCenter");
                                        if (usingControlCenter) XposedHelpers.callMethod(mController, "collapseControlCenter", true);
                                        boolean keepOpened = MainModule.mPrefs.getBoolean("system_secureqs_keepopened");
                                        final boolean usingCenter = usingControlCenter;
                                        final boolean expandAfter = usingControlCenter && keepOpened;
//                                        if (!usingControlCenter && keepOpened)
//                                            XposedHelpers.callMethod(XposedHelpers.getObjectField(mStatusBar, "mStatusBarStateController"), "setLeaveOpenOnKeyguardHide", true);
                                        XposedHelpers.callMethod(mStatusBar, "postQSRunnableDismissingKeyguard", !keepOpened, new Runnable() {
                                            public void run() {
                                                Intent intent = new Intent(ACTION_PREFIX + "HandleQSTileClick");
                                                intent.putExtra("tileName", tileName);
                                                intent.putExtra("expandAfter", expandAfter);
                                                intent.putExtra("usingCenter", usingCenter);
                                                mContext.sendBroadcast(intent);
                                            }
                                        });
                                    } catch (Throwable t) {
                                        XposedHelpers.log(t);
                                    }
                                }
                            });
                            param.returnAndSkip(null);
                        }
                    };
                    ModuleHelper.findAndHookMethod(tileClass, lpparam.getClassLoader(), "handleClick", View.class, hook);
                    ModuleHelper.hookAllMethodsSilently(tileClass, lpparam.getClassLoader(), "handleSecondaryClick", hook);
                    securedTiles.add(tileClass);
                }
            }
        });
    }

    public static void ExtendedPowerMenuHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            private boolean isListened = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!isListened) {
                    isListened = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext");
                    File powermenu = new File(mContext.getCacheDir(), "extended_power_menu");
                    if (powermenu == null) {
                        XposedHelpers.log("ExtendedPowerMenuHook", "No writable path found!");
                        return;
                    }
                    if (powermenu.exists()) powermenu.delete();

                    InputStream inputStream;
                    FileOutputStream outputStream;
                    byte[] fileBytes;
                    Resources resources = ModuleHelper.getModuleRes(mContext);
                    inputStream = resources.openRawResource(resources.getIdentifier("extended_power_menu", "raw", Helpers.modulePkg));
                    fileBytes = new byte[inputStream.available()];
                    inputStream.read(fileBytes);
                    outputStream = new FileOutputStream(powermenu);
                    outputStream.write(fileBytes);
                    outputStream.close();
                    inputStream.close();

                    if (!powermenu.exists()) {
                        XposedHelpers.log("ExtendedPowerMenuHook", "MAML file not found in cache");
                    }
                    else {
                        ModuleHelper.findAndHookConstructor("com.miui.maml.util.ZipResourceLoader", lpparam.getClassLoader(), String.class, new MethodHook() {
                            @Override
                            protected void before(final BeforeHookCallback param) throws Throwable {
                                String res = (String) param.getArgs()[0];
                                if ("/system/media/theme/default/powermenu".equals(res)) {
                                    param.getArgs()[0] = powermenu.getPath();
                                }
                            }
                        });
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.maml.ScreenElementRoot", lpparam.getClassLoader(), "issueExternCommand", String.class, Double.class, String.class, new MethodHook() {
            @Override
            @SuppressLint("MissingPermission")
            protected void before(final BeforeHookCallback param) throws Throwable {
                String cmd = (String)param.getArgs()[0];
                Object scrContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Context mContext = (Context)XposedHelpers.getObjectField(scrContext, "mContext");
                PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                Object mService = XposedHelpers.getObjectField(pm, "mService");
                Object mSystemExternCommandListener = XposedHelpers.getObjectField(param.getThisObject(), "mSystemExternCommandListener");

                boolean custom = false;
                if ("recovery".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, "recovery", false);
                    custom = true;
                } else if ("bootloader".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, "bootloader", false);
                    custom = true;
                } else if ("softreboot".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, null, false);
                    custom = true;
                }

                if (custom) {
                    if (mSystemExternCommandListener != null) XposedHelpers.callMethod(mSystemExternCommandListener, "onCommand", param.getArgs()[0], param.getArgs()[1], param.getArgs()[2]);
                    param.returnAndSkip(null);
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.plugins.PluginEnablerImpl", lpparam.getClassLoader(), "isEnabled", ComponentName.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                ComponentName componentName = (ComponentName) param.getArgs()[0];
                if (componentName.getClassName().contains("GlobalActions")) {
                    param.returnAndSkip(false);
                }
            }
        });
    }

    public static void HideDismissViewHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updateDismissView", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View mDismissView = (View)XposedHelpers.getObjectField(param.getThisObject(), "mDismissView");
                if (mDismissView != null) {
                    mDismissView.setVisibility(View.GONE);
                    param.returnAndSkip(null);
                }
            }
        });
    }

    public static void HideNoficationAccessIconHook(PackageLoadedParam lpparam) {
        MethodHook hideViewHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View mShortCut = (View) XposedHelpers.getObjectField(param.getThisObject(), "mShortCut");
                if (mShortCut != null) {
                    mShortCut.setVisibility(View.GONE);
                    param.returnAndSkip(null);
                }
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.getClassLoader(), "updateShortCutVisibility", hideViewHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateShortCutVisibility", hideViewHook);
    }

    public static void ReplaceShortcutAppHook(PackageLoadedParam lpparam) {
        MethodHook openAppHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = ModuleHelper.findContext(lpparam);
                int user = 0;
                String pkgAppName = "";
                if (param.getMember().getName().equals("startCalendarApp")) {
                    user = MainModule.mPrefs.getInt("system_calendar_app_user", 0);
                    pkgAppName = MainModule.mPrefs.getString("system_calendar_app", "");
                }
                else if (param.getMember().getName().equals("startClockApp")) {
                    user = MainModule.mPrefs.getInt("system_clock_app_user", 0);
                    pkgAppName = MainModule.mPrefs.getString("system_clock_app", "");
                }
                else if (param.getMember().getName().equals("startSettingsApp")) {
                    user = MainModule.mPrefs.getInt("system_shortcut_app_user", 0);
                    pkgAppName = MainModule.mPrefs.getString("system_shortcut_app", "");
                }
                if (pkgAppName != null && !pkgAppName.equals("")) {
                    String[] pkgAppArray = pkgAppName.split("\\|");
                    if (pkgAppArray.length < 2) return;

                    ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.setComponent(name);
                    if (user != 0) {
                        try {
                            Class<?> Dependency = findClass("com.android.systemui.Dependency", lpparam.getClassLoader());
                            String StatusbarClsForDep = "com.android.systemui.statusbar.phone.CentralSurfaces";
                            Object mStatusBar = XposedHelpers.callStaticMethod(Dependency, "get", findClass(StatusbarClsForDep, lpparam.getClassLoader()));
                            XposedHelpers.callMethod(mStatusBar, "collapsePanels");
                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    } else {
                        Object activiyStarter = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", mContext.getClassLoader()), "get", findClass("com.android.systemui.plugins.ActivityStarter", mContext.getClassLoader()));
                        XposedHelpers.callMethod(activiyStarter, "startActivity", intent, true);
                    }
                    param.returnAndSkip(null);
                }
            }
        };
        if (!MainModule.mPrefs.getString("system_shortcut_app", "").equals("")) {
            ModuleHelper.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.getClassLoader(), "startSettingsApp", openAppHook);
        }
        if (!MainModule.mPrefs.getString("system_calendar_app", "").equals("")) {
            ModuleHelper.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.getClassLoader(), "startCalendarApp", Context.class, openAppHook);
        }
        if (!MainModule.mPrefs.getString("system_clock_app", "").equals("")) {
            ModuleHelper.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.getClassLoader(), "startClockApp", openAppHook);
        }
    }
    public static void StatusBarStyleBatteryIconHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateAll", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                LinearLayout batteryView = (LinearLayout) param.getThisObject();
                TextView mBatteryTextDigitView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mBatteryTextDigitView");
                TextView mBatteryPercentView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentView");
                TextView mBatteryPercentMarkView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentMarkView");
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_swap_batteryicon_percentage")) {
                    batteryView.removeView(mBatteryPercentView);
                    batteryView.removeView(mBatteryPercentMarkView);
                    batteryView.addView(mBatteryPercentMarkView, 0);
                    batteryView.addView(mBatteryPercentView, 0);
                }
                float fontSize = MainModule.mPrefs.getInt("system_statusbar_batterystyle_fontsize", 15) * 0.5f;
                if (fontSize > 7.5) {
                    mBatteryTextDigitView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
                    mBatteryPercentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
                }
                fontSize = MainModule.mPrefs.getInt("system_statusbar_batterystyle_mark_fontsize", 15) * 0.5f;
                if (fontSize > 7.5) {
                    mBatteryPercentMarkView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbar_batterystyle_bold")) {
                    mBatteryTextDigitView.setTypeface(Typeface.DEFAULT_BOLD);
                    mBatteryPercentView.setTypeface(Typeface.DEFAULT_BOLD);
                }
                Resources res = batteryView.getResources();
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_batterystyle_leftmargin", 0);
                leftMargin = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMargin * 0.5f,
                    res.getDisplayMetrics()
                );
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_batterystyle_rightmargin", 0);
                rightMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    rightMargin * 0.5f,
                    res.getDisplayMetrics()
                );
                int topMargin = 0;
                int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_batterystyle_verticaloffset", 8);
                if (verticalOffset != 8) {
                    float marginTop = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (verticalOffset - 8) * 0.5f,
                        res.getDisplayMetrics()
                    );
                    topMargin = (int) marginTop;
                }
                int digitRightMargin = 0;
                int markRightMargin = 0;
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
                    digitRightMargin = rightMargin;
                }
                else {
                    markRightMargin = rightMargin;
                }
                if (leftMargin > 0 || topMargin != 8 || digitRightMargin > 0) {
                    mBatteryPercentView.setPaddingRelative(leftMargin, topMargin, digitRightMargin, 0);
                }

                verticalOffset = MainModule.mPrefs.getInt("system_statusbar_batterystyle_mark_verticaloffset", 17);
                if (verticalOffset < 17) {
                    float marginTop = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (verticalOffset - 8) * 0.5f,
                        res.getDisplayMetrics()
                    );
                    topMargin = (int) marginTop;
                }
                if (verticalOffset < 17 || markRightMargin > 0) {
                    mBatteryPercentMarkView.setPaddingRelative(0, topMargin, markRightMargin, 0);
                }
            }
        });
    }
    public static void ForceClockUseSystemFontsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.clock.MiuiBaseClock", lpparam.getClassLoader(), "updateViewsTextSize", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                TextView mTimeText = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mTimeText");
                mTimeText.setTypeface(Typeface.DEFAULT);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.clock.MiuiLeftTopLargeClock", lpparam.getClassLoader(), "onLanguageChanged", String.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                TextView mTimeText = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mCurrentDateLarge");
                mTimeText.setTypeface(Typeface.DEFAULT);
            }
        });
    }
    public static void HideStatusBarBeforeScreenshotHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View view = (View) param.getArgs()[0];
                BroadcastReceiver br = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if ("miui.intent.TAKE_SCREENSHOT".equals(intent.getAction())) {
                            boolean finished = intent.getBooleanExtra("IsFinished", true);
                            view.setVisibility(finished ? View.VISIBLE : View.INVISIBLE);
                        }
                    }
                };
                view.getContext().registerReceiver(br, new IntentFilter("miui.intent.TAKE_SCREENSHOT"));
            }
        });
    }
    public static void HideNavBarBeforeScreenshotHook(PackageLoadedParam lpparam) {
        MethodHook hideNavHook = new MethodHook() {
            int visibleState = 0;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View view = (View) XposedHelpers.callMethod(param.getThisObject(), "getView");
                BroadcastReceiver br = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if ("miui.intent.TAKE_SCREENSHOT".equals(intent.getAction())) {
                            boolean finished = intent.getBooleanExtra("IsFinished", true);
                            if (!finished) {
                                visibleState = view.getVisibility();
                            }
                            view.setVisibility(finished ? visibleState : View.INVISIBLE);
                        }
                    }
                };
                view.getContext().registerReceiver(br, new IntentFilter("miui.intent.TAKE_SCREENSHOT"));
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBar", lpparam.getClassLoader(), "onInit", hideNavHook);
    }

    public static void OpenNotifyInFloatingWindowHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiStatusBarNotificationActivityStarter", lpparam.getClassLoader(), "startNotificationIntent", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                PendingIntent pendingIntent = (PendingIntent) param.getArgs()[0];
                Object mSbn = XposedHelpers.getObjectField(param.getArgs()[2], "mSbn");
                String pkgName;
                boolean isSubstituteNotification = (boolean) XposedHelpers.callMethod(mSbn, "isSubstituteNotification");
                if (isSubstituteNotification) {
                    pkgName = (String) XposedHelpers.getObjectField(mSbn, "mPkgName");
                }
                else {
                    pkgName = pendingIntent.getCreatorPackage();
                }
                ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
                if (foregroundInfo != null) {
                    String topPackage = foregroundInfo.mForegroundPackageName;
                    if (pkgName.equals(topPackage) || "com.miui.home".equals(topPackage)) {
                        return;
                    }
                }
                boolean whitelist = MainModule.mPrefs.getBoolean("system_notify_openinfw_in_whitelist");
                boolean appInList = MainModule.mPrefs.getStringSet("system_notify_openinfw_apps").contains(pkgName);
                if (whitelist ^ appInList) {
                    return;
                }
                Class<?> Dependency = findClass("com.android.systemui.Dependency", lpparam.getClassLoader());
                Object AppMiniWindowManager = XposedHelpers.callStaticMethod(Dependency, "get", findClassIfExists("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.getClassLoader()));
                XposedHelpers.callMethod(AppMiniWindowManager, "launchMiniWindowActivity", pkgName, pendingIntent);
                param.returnAndSkip(null);
            }
        });
    }

    public static void FixOpenNotifyInFreeFormHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.getClassLoader(), "launchMiniWindowActivity", String.class, PendingIntent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String pkgName = (String) param.getArgs()[0];
                PendingIntent pendingIntent = (PendingIntent) param.getArgs()[1];
                ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
                if (foregroundInfo != null) {
                    String topPackage = foregroundInfo.mForegroundPackageName;
                    if (pkgName.equals(topPackage)) {
                        return;
                    }
                }
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "context");
                List<MiuiFreeFormManager.MiuiFreeFormStackInfo> freeFormStackInfoList = MiuiFreeFormManager.getAllFreeFormStackInfosOnDisplay(mContext.getDisplay() != null ? mContext.getDisplay().getDisplayId() : 0);
                int freeFormCount = 0;
                if (freeFormStackInfoList != null) {
                    freeFormCount = freeFormStackInfoList.size();
                }
                if (freeFormCount == 2) return;
                for (MiuiFreeFormManager.MiuiFreeFormStackInfo rootTaskInfo : freeFormStackInfoList) {
                    if (pkgName.equals(rootTaskInfo.packageName)) return;
                }
                if (!pendingIntent.isActivity()) {
                    Intent bIntent = new Intent(ACTION_PREFIX + "SetFreeFormPackage");
                    bIntent.putExtra("package", pkgName);
                    bIntent.setPackage("android");
                    mContext.sendBroadcast(bIntent);
                }
                Intent intent = new Intent();
                if (!"com.tencent.tim".equals(pkgName)) {
                    XposedHelpers.callMethod(intent, "addFlags", 134217728);
                    XposedHelpers.callMethod(intent, "addFlags", 268435456);
                    XposedHelpers.callMethod(intent, "addMiuiFlags", 256);
                }
                ActivityOptions options = MiuiMultiWindowUtils.getActivityOptions(mContext, pkgName, true, false);
                pendingIntent.send(mContext, 0, intent, null, null, null, options != null ? options.toBundle() : null);
                param.returnAndSkip(null);
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private static TextView mPct = null;
    private static void initPct(ViewGroup container, int source, Context context) {
        Resources res = context.getResources();
        if (mPct == null) {
            mPct = new TextView(container.getContext());
            mPct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            mPct.setGravity(Gravity.CENTER);
            float density = res.getDisplayMetrics().density;
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Math.round(MainModule.mPrefs.getInt("system_showpct_top", 28) * density);
            lp.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
            mPct.setPadding(Math.round(20 * density), Math.round(10 * density), Math.round(18 * density), Math.round(12 * density));
            mPct.setLayoutParams(lp);
            try {
                Resources modRes = ModuleHelper.getModuleRes(context);
                mPct.setTextColor(modRes.getColor(R.color.color_on_surface_variant, context.getTheme()));
                mPct.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.input_background, context.getTheme()));
            }
            catch (Throwable err) {
                XposedHelpers.log(err);
            }
            container.addView(mPct);
        }
        mPct.setTag(source);
        mPct.setVisibility(View.GONE);
    }

    private static void removePct(TextView mPctText) {
        if (mPctText != null) {
            mPctText.setVisibility(View.GONE);
            ViewGroup p = (ViewGroup) mPctText.getParent();
            p.removeView(mPctText);
            mPct = null;
        }
    }

    public static void BrightnessPctHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.getClassLoader(), "showMirror", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup mStatusBarWindow = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarWindow");
                if (mStatusBarWindow == null) {
                    XposedHelpers.log("BrightnessPctHook", "mStatusBarWindow is null");
                    return;
                }
                initPct(mStatusBarWindow, 1, mStatusBarWindow.getContext());
                mPct.setVisibility(View.VISIBLE);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.getClassLoader(), "hideMirror", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                removePct(mPct);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.getClassLoader(), "onStart", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Object mMirror = XposedHelpers.getObjectField(param.getThisObject(), "mControl");
                Object controlCenterWindowViewController = XposedHelpers.getObjectField(mMirror, "controlCenterWindowViewController");
                String ClsName = controlCenterWindowViewController.getClass().getName();
                if (!ClsName.equals("ControlCenterWindowViewController")) {
                    controlCenterWindowViewController = XposedHelpers.callMethod(controlCenterWindowViewController, "get");
                }
                Object windowView = XposedHelpers.callMethod(controlCenterWindowViewController, "getView");
                if (windowView == null) {
                    XposedHelpers.log("BrightnessPctHook", "mControlPanelContentView is null");
                    return;
                }
                initPct((ViewGroup) windowView, 2, mContext);
                mPct.setVisibility(View.VISIBLE);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.getClassLoader(), "onStop", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                removePct(mPct);
            }
        });

        final Class<?> BrightnessUtils = findClassIfExists("com.android.systemui.controlcenter.policy.BrightnessUtils", lpparam.getClassLoader());
        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.getClassLoader(), "onChanged", new MethodHook() {
            @Override
            @SuppressLint("SetTextI18n")
            protected void after(final AfterHookCallback param) throws Throwable {
                int pctTag = 0;
                if (mPct != null && mPct.getTag() != null) {
                    pctTag = (int) mPct.getTag();
                }
                if (pctTag == 0 || mPct == null) return;
                int currentLevel = (int)param.getArgs()[3];
                if (BrightnessUtils != null) {
                    int maxLevel = (int) XposedHelpers.getStaticObjectField(BrightnessUtils, "GAMMA_SPACE_MAX");
                    mPct.setText(((currentLevel * 100) / maxLevel) + "%");
                }
            }
        });
    }
    public static void ShowVolumePctHook(ClassLoader pluginLoader) {
        Class<?> MiuiVolumeDialogImpl = findClassIfExists("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader);
        ModuleHelper.findAndHookMethod(MiuiVolumeDialogImpl, "showVolumeDialogH", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View mDialogView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mDialogView");
                FrameLayout windowView = (FrameLayout) mDialogView.getParent();
                initPct(windowView, 3, windowView.getContext());
            }
        });

        ModuleHelper.findAndHookMethod(MiuiVolumeDialogImpl, "dismissH", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                removePct(mPct);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl$VolumeSeekBarChangeListener", pluginLoader, "onProgressChanged", new MethodHook() {
            private int nowLevel = -233;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (nowLevel == (int)param.getArgs()[1]) return;
                int pctTag = 0;
                if (mPct != null && mPct.getTag() != null) {
                    pctTag = (int) mPct.getTag();
                }
                if (pctTag != 3 || mPct == null) return;
                Object mColumn = XposedHelpers.getObjectField(param.getThisObject(), "mColumn");
                Object ss = XposedHelpers.getObjectField(mColumn, "ss");
                if (ss == null) return;
                if (XposedHelpers.getIntField(mColumn, "stream") == 10) return;

                boolean fromUser = (boolean) param.getArgs()[2];
                int currentLevel;
                if (fromUser) {
                    currentLevel = (int)param.getArgs()[1];
                }
                else {
                    ObjectAnimator anim = (ObjectAnimator) XposedHelpers.getObjectField(mColumn, "anim");
                    if (anim == null || !anim.isRunning()) return;
                    currentLevel = XposedHelpers.getIntField(mColumn, "animTargetProgress");
                }
                nowLevel = currentLevel;
                mPct.setVisibility(View.VISIBLE);
                int levelMin = XposedHelpers.getIntField(ss, "levelMin");
                if (levelMin > 0 && currentLevel < levelMin * 1000) {
                    currentLevel = levelMin * 1000;
                }
                SeekBar seekBar = (SeekBar) param.getArgs()[0];
                int max = seekBar.getMax();
                int maxLevel = max / 1000;
                if (currentLevel != 0) {
                    int i3 = maxLevel - 1;
                    currentLevel = currentLevel == max ? maxLevel : (currentLevel * i3 / max) + 1;
                }
                mPct.setText(((currentLevel * 100) / maxLevel) + "%");
            }
        });
    }
    public static void HideCCDateView(ClassLoader pluginLoader) {
        MethodHook hideDateView = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                TextView dateView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "dateView");
                if (dateView != null) {
                    XposedHelpers.setObjectField(dateView, "mVisibility", 8);
                    dateView.setVisibility(View.GONE);
                }
            }
        };
        MethodHook fixClockView = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                TextView clockView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "clockView");
                Class<?> ConstraintSetClass = pluginLoader.loadClass("androidx.constraintlayout.widget.ConstraintSet");
                Object constraintSet = XposedHelpers.newInstance(ConstraintSetClass);
                Object headerView = XposedHelpers.getObjectField(param.getThisObject(), "view");
                XposedHelpers.callMethod(constraintSet, "clone", headerView);
                int clockId = clockView.getId();
                XposedHelpers.callMethod(constraintSet, "clear", clockId, 7);
                XposedHelpers.callMethod(constraintSet, "applyTo", headerView);
            }
        };
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.MainPanelHeaderController", pluginLoader, "updateVisibility", hideDateView);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.MainPanelHeaderController", pluginLoader, "updateConstraint", fixClockView);
    }
    public static void hideCCSettingsTilesEdit(ClassLoader pluginLoader) {
        MethodHook hideIcons = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup headerView = (ViewGroup) XposedHelpers.callMethod(param.getThisObject(), "getView");
                int iconId = headerView.getResources().getIdentifier("settings_shortcut", "id", "miui.systemui.plugin");
                ImageView iconView = headerView.findViewById(iconId);
                if (iconView != null) {
                    iconView.setVisibility(View.GONE);
                }
                iconId = headerView.getResources().getIdentifier("tiles_edit", "id", "miui.systemui.plugin");
                iconView = headerView.findViewById(iconId);
                if (iconView != null) {
                    iconView.setVisibility(View.GONE);
                }
            }
        };
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.MainPanelHeaderController", pluginLoader, "updateVisibility", hideIcons);

        if (MainModule.mPrefs.getBoolean("system_cc_custom_clock_action")) {
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.MainPanelHeaderController", pluginLoader, "addClockViews", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    TextView clockView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "clockView");
                    clockView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object activityStarter = XposedHelpers.getObjectField(param.getThisObject(), "activityStarter");
                            Intent addFlags = new Intent("android.settings.SETTINGS").addFlags(268435456);
                            XposedHelpers.callMethod(activityStarter, "postStartActivityDismissingKeyguard", addFlags, 350);
                        }
                    });
                    clockView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Object lazyQsCustomizer = XposedHelpers.getObjectField(param.getThisObject(), "qsCustomizer");
                            Object qsCustomizer = XposedHelpers.callMethod(lazyQsCustomizer, "get");
                            XposedHelpers.callMethod(qsCustomizer, "show");
                            Object hapticFeedback = XposedHelpers.getObjectField(param.getThisObject(), "hapticFeedback");
                            XposedHelpers.callMethod(hapticFeedback, "postLongClick");
                            return true;
                        }
                    });
                }
            });
        }
    }
    public static void initCCClockStyle(ClassLoader pluginLoader) {
        int defaultClockSize = 9;
        int ccClockFontSize = MainModule.mPrefs.getInt("system_cc_clock_fontsize", defaultClockSize);
        if (ccClockFontSize > defaultClockSize) {
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.MainPanelHeaderController", pluginLoader, "updateClocksAppearance", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    TextView clock = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "clockView");
                    clock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ccClockFontSize);
                    clock.setLineSpacing(0, 1);
                }
            });
        }
        int defaultVerticalOffset = 10;
        int verticalOffset = MainModule.mPrefs.getInt("system_cc_clock_verticaloffset", defaultVerticalOffset);
        if (verticalOffset != defaultVerticalOffset) {
            int topMargin = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                verticalOffset - defaultVerticalOffset,
                Resources.getSystem().getDisplayMetrics()
            );
            MethodHook verticalOffsetHook = new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    TextView clock = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "clockView");
                    Class<?> ConstraintSetClass = pluginLoader.loadClass("androidx.constraintlayout.widget.ConstraintSet");
                    Object constraintSet = XposedHelpers.newInstance(ConstraintSetClass);
                    Object headerView = XposedHelpers.getObjectField(param.getThisObject(), "view");
                    XposedHelpers.callMethod(constraintSet, "clone", headerView);
                    int clockId = clock.getId();
                    XposedHelpers.callMethod(constraintSet, "setMargin", clockId, 4, -topMargin);
                    XposedHelpers.callMethod(constraintSet, "applyTo", headerView);
                }
            };
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.MainPanelHeaderController", pluginLoader, "updateConstraint", verticalOffsetHook);
        }

    }
    public static void HideSafeVolumeDlgHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.volume.VolumeDialogControllerImpl", lpparam.getClassLoader(), "onShowSafetyWarningW", int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mAudio = XposedHelpers.getObjectField(param.getThisObject(), "mAudio");
                XposedHelpers.callMethod(mAudio, "disableSafeMediaVolume");
                param.returnAndSkip(null);
            }
        });
    }
    public static void DisableHeadsUpWhenMuteHook(PackageLoadedParam lpparam) {
        final boolean[] mMuteVisible = {false};
        MethodHook disableHeadsUpHook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs().length != 2) return;
                boolean canPopup = (boolean) param.getResult();
                if (canPopup && mMuteVisible[0]) {
                    param.setResult(false);
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.interruption.MiuiNotificationInterruptStateProviderImpl", lpparam.getClassLoader(), "shouldPeek", disableHeadsUpHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), "updateVolumeZen", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                mMuteVisible[0] = XposedHelpers.getBooleanField(param.getThisObject(), "mMuteVisible");
            }
        });
    }

    public static void HideLockscreenZenModeHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.zen.ZenModeViewController", lpparam.getClassLoader(), "shouldBeVisible", HookerClassHelper.returnConstant(false));
    }

    public static void SwitchCCAndNotificationHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "handleEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean useCC = (boolean) XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mPanelController"), "isExpandable");
                if (useCC) {
                    FrameLayout bar = (FrameLayout) param.getThisObject();
                    Object mControlPanelWindowManager = XposedHelpers.getObjectField(param.getThisObject(), "mControlPanelWindowManager");
                    boolean dispatchToControlPanel = (boolean) XposedHelpers.callMethod(mControlPanelWindowManager, "dispatchToControlPanel", param.getArgs()[0], bar.getWidth());
                    XposedHelpers.callMethod(mControlPanelWindowManager, "setTransToControlPanel", dispatchToControlPanel);
                    param.returnAndSkip(dispatchToControlPanel);
                    return;
                }
                param.returnAndSkip(false);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.getClassLoader(), "dispatchToControlPanel", MotionEvent.class, float.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean added = XposedHelpers.getBooleanField(param.getThisObject(), "added");
                if (added) {
                    boolean useCC = XposedHelpers.getBooleanField(XposedHelpers.getObjectField(param.getThisObject(), "mControlCenterController"), "useControlCenter");
                    if (useCC) {
                        MotionEvent motionEvent = (MotionEvent) param.getArgs()[0];
                        if (motionEvent.getActionMasked() == 0) {
                            XposedHelpers.setObjectField(param.getThisObject(), "mDownX", motionEvent.getRawX());
                        }
                        Object controlCenterWindowView = XposedHelpers.getObjectField(param.getThisObject(), "mControlPanel");
                        if (controlCenterWindowView == null) {
                            param.returnAndSkip(false);
                        }
                        else {
                            float mDownX = XposedHelpers.getFloatField(param.getThisObject(), "mDownX");
                            float width = (float) param.getArgs()[1];
                            if (mDownX < width / 2.0f) {
                                param.returnAndSkip(XposedHelpers.callMethod(controlCenterWindowView, "handleMotionEvent", motionEvent, true));
                            }
                            else {
                                param.returnAndSkip(false);
                            }
                        }
                        return;
                    }
                }
                param.returnAndSkip(false);
            }
        });
    }
    public static void ShowCCStepCountHook(PackageLoadedParam lpparam) {
        MethodHook updateStyleHook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View headView = (View) param.getThisObject();
                String carrierId;
                String tag;
                if (headView.getClass().getSimpleName().contains("ControlCenterStatusBar")) {
                    carrierId = "carrierText";
                    tag = "StepInControlCenter";
                }
                else {
                    carrierId = "mCarrierText";
                    tag = "StepInNotification";
                }
                TextView mCarrierText = (TextView) XposedHelpers.getObjectField(param.getThisObject(), carrierId);
                LinearLayout mSystemIconContainer = (LinearLayout) mCarrierText.getParent();
                TextView stepView = mSystemIconContainer.findViewWithTag(tag);
                if (stepView == null) {
                    StepCounterController.removeStepViewByTag(tag);
                    stepView = new TextView(headView.getContext());
                    Resources res = headView.getResources();
                    int styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
                    stepView.setTextAppearance(styleId);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    float horizMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        3,
                        res.getDisplayMetrics()
                    );
                    lp.rightMargin = (int) horizMargin;
                    lp.gravity = Gravity.CENTER_VERTICAL;
                    mSystemIconContainer.addView(stepView, mSystemIconContainer.indexOfChild(mCarrierText), lp);
                    stepView.setGravity(Gravity.CENTER_VERTICAL);
                    stepView.setTag(tag);
                    StepCounterController.addStepView(stepView);
                }
                stepView.setTextColor(mCarrierText.getTextColors());
            }
        };
        if (MainModule.mPrefs.getBoolean("system_drawer_show_stepcount")) {
            ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "themeChanged", updateStyleHook);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_show_stepcount")) {
            ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", lpparam.getClassLoader(), "updateHeaderColor", updateStyleHook);
        }
    }

    public static void BluetoothTileStyleHook(ClassLoader pluginLoader) {
        final int[] tileResIds = {0};
        ModuleHelper.findAndHookMethod("miui.systemui.dagger.PluginComponentFactory", pluginLoader, "create", Context.class, Context.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context pluginContext = (Context) param.getArgs()[1];
                tileResIds[0] = pluginContext.getResources().getIdentifier("big_tile", "layout", "miui.systemui.plugin");
            }
        });
        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.dagger.ControlCenterViewModule", pluginLoader, "createBigTileGroup", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup mView = (ViewGroup) param.getResult();
                LayoutInflater li = (LayoutInflater) XposedHelpers.callMethod(param.getArgs()[0], "injectable", param.getArgs()[1]);
                View btTileView = li.inflate(tileResIds[0], null);
                mView.addView(btTileView, 2);
                btTileView.setTag("big_tile_bt");
            }
        });
        int styleId = MainModule.mPrefs.getStringAsInt("system_cc_bluetooth_tile_style", 1);
        MethodHook updateStyleHook = new MethodHook() {
            boolean inited = false;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup mView = (ViewGroup) XposedHelpers.callMethod(param.getThisObject(), "getView");
                View bigTileB = (View) XposedHelpers.getObjectField(param.getThisObject(), "bigTileB");
                if (!inited) {
                    inited = true;
                    Object factory = XposedHelpers.getObjectField(param.getThisObject(), "tileViewFactory");
                    View btTileView = mView.findViewWithTag("big_tile_bt");
                    int btTileId = ResourceHooks.getFakeResId("bt_big_tile");
                    btTileView.setId(btTileId);
                    Object btController = XposedHelpers.callMethod(factory, "create", btTileView, "bt");
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "btTileView", btTileView);
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "btController", btController);

                    Class<?> ConstraintSetClass = pluginLoader.loadClass("androidx.constraintlayout.widget.ConstraintSet");
                    Object constraintSet = XposedHelpers.newInstance(ConstraintSetClass);
                    XposedHelpers.callMethod(constraintSet, "clone", mView);
                    View bigTileA = (View) XposedHelpers.getObjectField(param.getThisObject(), "bigTileA");
                    if (styleId == 2) {
                        XposedHelpers.callMethod(constraintSet, "connect", bigTileB.getId(), 7, btTileId, 6);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 6, bigTileB.getId(), 7);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 7, bigTileA.getId(), 7);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 3, bigTileB.getId(), 3);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 4, 0, 4);
                        XposedHelpers.callMethod(constraintSet, "setMargin", btTileId, 6, (int)Helpers.dp2px(10));
                        int labelResId = mView.getResources().getIdentifier("label_container", "id", "miui.systemui.plugin");
                        bigTileB.findViewById(labelResId).setVisibility(View.GONE);
                        btTileView.findViewById(labelResId).setVisibility(View.GONE);
                        int iconResId = mView.getResources().getIdentifier("status_icon", "id", "miui.systemui.plugin");
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) bigTileB.findViewById(iconResId).getLayoutParams();
                        layoutParams.leftMargin = (int)Helpers.dp2px(3);
                        layoutParams = (LinearLayout.LayoutParams) btTileView.findViewById(iconResId).getLayoutParams();
                        layoutParams.leftMargin = (int)Helpers.dp2px(3);
                    }
                    else {
                        XposedHelpers.callMethod(constraintSet, "connect", bigTileB.getId(), 4, btTileId, 3);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 6, bigTileA.getId(), 6);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 7, bigTileA.getId(), 7);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 3, bigTileB.getId(), 4);
                        XposedHelpers.callMethod(constraintSet, "connect", btTileId, 4, 0, 4);
                    }
                    XposedHelpers.callMethod(constraintSet, "constrainWidth", btTileId, 0);
                    XposedHelpers.callMethod(constraintSet, "constrainHeight", btTileId, 0);
                    XposedHelpers.callMethod(constraintSet, "applyTo", mView);
                }
                if (styleId == 3) {
                    ViewGroup.LayoutParams layoutParams = bigTileB.getLayoutParams();
                    int verticalMargin = (int)Helpers.dp2px(4);
                    ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = verticalMargin;
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = verticalMargin;
                }
            }
        };
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.BigTileGroupController", pluginLoader, "updateResources", updateStyleHook);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.BigTileGroupController", pluginLoader, "setListening", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object btController = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "btController");
                if (btController != null) {
                    XposedHelpers.callMethod(btController, "setListening", param.getArgs()[0]);
                }
            }
        });
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.BigTileGroupController", pluginLoader, "getRowViews", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int row = (int) param.getArgs()[0];
                Object btTileView;
                if (row == 1 && (btTileView = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "btTileView")) != null) {
                    ((ArrayList<Object>)param.getResult()).add(btTileView);
                }
            }
        });
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.BigTileGroupController", pluginLoader, "getChildControllers", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object btController = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "btController");
                if (btController != null) {
                    ((ArrayList<Object>)param.getResult()).add(btController);
                }
            }
        });
    }
    public static void HideMobileNetworkIndicatorHook(PackageLoadedParam lpparam) {
        boolean singleMobileType = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single");
        boolean showOnWifi = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_show_wificonnected");
        MethodHook hideMobileActivity = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int opt = MainModule.mPrefs.getStringAsInt("system_mobiletypeicon", 1);
                boolean hideIndicator = MainModule.mPrefs.getBoolean("system_networkindicator_mobile");
                View mMobileType = (View) XposedHelpers.getObjectField(param.getThisObject(), "mMobileType");
                boolean dataConnected = (boolean) XposedHelpers.getObjectField(param.getArgs()[0], "dataConnected");
                boolean wifiAvailable = (boolean) XposedHelpers.getObjectField(param.getArgs()[0], "wifiAvailable");
                if (opt == 3) {
                    if (singleMobileType) {
                        TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle");
                        mMobileTypeSingle.setVisibility(View.GONE);
                    }
                    else {
                        mMobileType.setVisibility(View.GONE);
                    }
                }
                else if (opt == 1) {
                    int viz = (dataConnected && (!wifiAvailable || showOnWifi)) ? View.VISIBLE : View.GONE;
                    if (singleMobileType) {
                        TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle");
                        mMobileTypeSingle.setVisibility(viz);
                    }
                    else {
                        mMobileType.setVisibility(viz);
                    }
                }
                else if (opt == 2) {
                    int viz = (!wifiAvailable || showOnWifi) ? View.VISIBLE : View.GONE;
                    if (singleMobileType) {
                        TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle");
                        mMobileTypeSingle.setVisibility(viz);
                    }
                    else {
                        mMobileType.setVisibility(viz);
                    }
                }
                View mLeftInOut = (View) XposedHelpers.getObjectField(param.getThisObject(), "mLeftInOut");
                if (hideIndicator) {
                    View mRightInOut = (View) XposedHelpers.getObjectField(param.getThisObject(), "mRightInOut");
                    mLeftInOut.setVisibility(View.GONE);
                    mRightInOut.setVisibility(View.GONE);
                }
                if (wifiAvailable && showOnWifi && !Build.IS_INTERNATIONAL_BUILD && (dataConnected || opt == 2)) {
                    View mSmallHd = (View) XposedHelpers.getObjectField(param.getThisObject(), "mSmallHd");
                    mSmallHd.setVisibility(View.GONE);
                }
                if (!singleMobileType) {
                    View mMobileLeftContainer = (View) XposedHelpers.getObjectField(param.getThisObject(), "mMobileLeftContainer");
                    mMobileLeftContainer.setVisibility((mMobileType.getVisibility() == View.GONE && mLeftInOut.getVisibility() == View.GONE) ? View.GONE : View.VISIBLE);
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "initViewState", hideMobileActivity);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "updateState", hideMobileActivity);
    }
}