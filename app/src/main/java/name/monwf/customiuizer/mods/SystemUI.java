package name.monwf.customiuizer.mods;

import static android.content.Context.RECEIVER_NOT_EXPORTED;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static name.monwf.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClassIfExists;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.WallpaperColors;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.libxposed.api.XposedInterface;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHookParam;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
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
import name.monwf.customiuizer.utils.PrefMap;

public class SystemUI {
    private final static String StatusBarCls = "com.android.systemui.statusbar.phone.CentralSurfacesImpl";

    private static int statusbarTextIconLayoutResId = 0;

    public static void setupStatusBar(Context mContext) {
        statusbarTextIconLayoutResId = MainModule.resHooks.addFakeResource("statusbar_text_icon", R.layout.statusbar_text_icon, "layout");
        if (MainModule.mPrefs.getBoolean("system_statusbar_topmargin")) {
            int topMargin = MainModule.mPrefs.getInt("system_statusbar_topmargin_val", 1);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_padding_top", topMargin);
        }
        if (MainModule.mPrefs.getBoolean("system_statusbar_horizmargin")) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_padding_start", 0);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_padding_end", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_enable_style_switch")) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "integer", "force_use_control_panel", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_volumetimer")) {
            int[] module_volume_timer_segments = {0, 1800, 3600, 7200, 10800, 14400, 18000, 21600, 28800, 36000, 43200};
            MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "integer-array", "miui_volume_timer_segments", module_volume_timer_segments);
        }
        int iconSize = MainModule.mPrefs.getInt("system_statusbar_iconsize", 6);
        if (iconSize > 6) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_size", iconSize);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_clock_size", iconSize + 0.4f);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size", iconSize);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size_dark", iconSize);
            float notifyPadding = 2.5f * iconSize / 13;
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_notification_icon_padding", notifyPadding);
            float iconHeight = 20.5f * iconSize / 13;
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_height", iconHeight);
        }
        if (!MainModule.mPrefs.getBoolean("system_drawer_hidedate")) {
            int drawerDateSize = MainModule.mPrefs.getInt("system_drawer_date_fontsize", 12);
            if (drawerDateSize > 12) {
                MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "shade_header_notification_date_text_size", drawerDateSize);
            }
        }
        if (MainModule.mPrefs.getBoolean("system_taptounlock")) {
            MainModule.resHooks.setResReplacement("com.android.systemui", "string", "default_lockscreen_unlock_hint_text", R.string.system_taptounlock_title);
        }
        int userActivityTimeout = MainModule.mPrefs.getInt("system_lstimeout", 3);
        if (userActivityTimeout > 3) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "integer", "config_lockScreenDisplayTimeout", userActivityTimeout * 1000);
        }
        Settings.System.putLong(mContext.getContentResolver(), "systemui_restart_time", java.lang.System.currentTimeMillis());
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

    private static int getTypeBySlotName(String slotName) {
        if ("battery_info".equals(slotName)) {
            return 91;
        }
        else if ("device_temp".equals(slotName)) {
            return 92;
        }
        return -1;
    }

    public static void MonitorDeviceInfoHook(PackageReadyParam lpparam, PrefMap<String, Object> mPrefs) {
        class TextIconInfo {
            public boolean iconShow;
            public int iconType;
            public String iconText;
        }
        boolean showBatteryDetail = mPrefs.getBoolean("system_statusbar_batterytempandcurrent");
        boolean showDeviceTemp = mPrefs.getBoolean("system_statusbar_showdevicetemperature");
        boolean dualRows = mPrefs.getBoolean("system_statusbar_dualrows");
        boolean batteryAtRight = showBatteryDetail && !dualRows && mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        boolean tempAtRight = showDeviceTemp && !dualRows && mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");
        boolean batteryAtLeft = showBatteryDetail && !mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        boolean tempAtLeft = showDeviceTemp && !mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");
        Class<?> ChargeUtilsClass = null;
        if (showBatteryDetail) {
            ChargeUtilsClass = findClassIfExists("com.miui.charge.ChargeUtils", lpparam.getClassLoader());
        }
        Class<?> finalChargeUtilsClass = ChargeUtilsClass;

        ArrayList<Integer> customIconTypes = new ArrayList<Integer>();
        if (batteryAtLeft || batteryAtRight) {
            customIconTypes.add(91);
        }
        if (tempAtLeft || tempAtRight) {
            customIconTypes.add(92);
        }
        if (!customIconTypes.isEmpty()) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), new MethodHook() {
                @Override
                protected void after(final MethodHookParam param) throws Throwable {
                    Object iconController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarIconController");
                    for (int iconType:customIconTypes) {
                        String slot = getSlotNameByType(iconType);
                        XposedHelpers.callMethod(iconController, "setIcon", (CharSequence)null, slot, 0);
                    }
                }
            });
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.ui.IconManager", lpparam.getClassLoader(), "addHolder", new MethodHook() {
                @Override
                protected void before(final MethodHookParam param) throws Throwable {
                    if (param.getArgs().length != 4) return;
                    Object iconHolder = param.getArgs()[3];
                    int type = XposedHelpers.getIntField(iconHolder, "type");
                    if (type == 91 || type == 92) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) XposedHelpers.callMethod(param.getThisObject(), "onCreateLayoutParams");
                        View iconView = createStatusbarTextIcon(mContext, lp, type, true);
                        int i = (int) param.getArgs()[0];
                        ViewGroup mGroup = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mGroup");
                        mGroup.addView(iconView, i);
                        mStatusbarTextIcons.add(iconView);
                        param.returnAndSkip(iconView);
                    }
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.ui.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIcon", String.class, "com.android.systemui.statusbar.phone.StatusBarIconHolder", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Object iconHolder = param.getArgs()[1];
                    String slotName = (String) param.getArgs()[0];
                    int iconType = getTypeBySlotName(slotName);
                    if (iconType > 0 && customIconTypes.contains(iconType)) {
                        XposedHelpers.setObjectField(iconHolder, "type", iconType);
                    }
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader(), "getSlot", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                View nsView = (View) param.getThisObject();
                Object tagData = ModuleHelper.getViewInfo(nsView, "text_icon_type");
                if (tagData != null) {
                    param.returnAndSkip(getSlotNameByType((int)tagData));
                }
            }
        });
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), new MethodHook() {
            Handler mBgHandler;
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context) param.getArgs()[0];
                final Handler mHandler = new Handler(Looper.getMainLooper()) {
                    public void handleMessage(Message message) {
                        if (message.what == 100021) {
                            TextIconInfo tii = (TextIconInfo) message.obj;
                            for (View tv : mStatusbarTextIcons) {
                                Object tagData = ModuleHelper.getViewInfo(tv, "text_icon_type");
                                if (tagData != null) {
                                    int iconType = (int)tagData;
                                    if (tii.iconType == iconType) {
                                        XposedHelpers.callMethod(tv, "setVisibilityByController", tii.iconShow);
                                        if (tii.iconShow) {
                                            XposedHelpers.callMethod(tv, "setNetworkSpeed", tii.iconText, "");
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
                            if (showBatteryInfo && mPrefs.getBoolean("system_statusbar_batterytempandcurrent_incharge") && finalChargeUtilsClass != null) {
                                Object batteryStatus = ModuleHelper.getStaticObjectFieldSilently(finalChargeUtilsClass, "sBatteryStatus");
                                if (ModuleHelper.NOT_EXIST_SYMBOL.equals(batteryStatus) || batteryStatus == null) {
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
                                        int thermalId = ModuleHelper.getCPUThermalId();
                                        if (thermalId != -1) {
                                            cpuReader = new RandomAccessFile("/sys/devices/virtual/thermal/thermal_zone" + thermalId + "/temp", "r");
                                            cpuProps = cpuReader.readLine();
                                        }
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
                                    int opt = mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1);
                                    String simpleTempVal = "";
                                    if (opt == 1 || opt == 4) {
                                        boolean decimal = mPrefs.getBoolean("system_statusbar_batterytempandcurrent_temp_decimal");
                                        int tempVal = 0;
                                        if (!TextUtils.isEmpty(props.getProperty("POWER_SUPPLY_TEMP"))) {
                                            tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
                                        }
                                        if (decimal) {
                                            simpleTempVal = String.valueOf(tempVal / 10f);
                                        }
                                        else {
                                            simpleTempVal = tempVal % 10 == 0 ? (String.valueOf(tempVal / 10)) : (String.valueOf(tempVal / 10f));
                                        }
                                    }
                                    String currVal = "";
                                    String preferred = "mA";
                                    float currentRatio = 1000f;
                                    if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_fixcurrentratio")) {
                                        currentRatio = 1f;
                                    }
                                    int curReadVal = 0;
                                    if (!TextUtils.isEmpty(props.getProperty("POWER_SUPPLY_CURRENT_NOW"))) {
                                        curReadVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW"));
                                    }
                                    int rawCurr = -1 * Math.round(curReadVal / currentRatio);
                                    if (opt == 1 || opt == 3 || opt == 5) {
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_positive")) {
                                            rawCurr = Math.abs(rawCurr);
                                        }
                                        if (Math.abs(rawCurr) > 999) {
                                            currVal = String.format("%.2f", rawCurr / 1000f);
                                            preferred = "A";
                                        } else {
                                            currVal = String.valueOf(rawCurr);
                                        }
                                    }
                                    int hideUnit = mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_hideunit", 0);
                                    String tempUnit = (hideUnit == 1 || hideUnit == 2) ? "" : "℃";
                                    String powerUnit = (hideUnit == 1 || hideUnit == 3) ? "" : "W";
                                    String currUnit = (hideUnit == 1 || hideUnit == 3) ? "" : preferred;
                                    String simpleWatt = "";
                                    if (opt == 2 || opt == 4 || opt == 5) {
                                        float voltVal = 0;
                                        if (!TextUtils.isEmpty(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW"))) {
                                            voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f;
                                        }
                                        simpleWatt = String.format(Locale.getDefault(), "%.2f", Math.abs(voltVal * rawCurr) / 1000);
                                    }
                                    if (opt == 1) {
                                        String splitChar = mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow") ? " " : "\n";
                                        batteryInfo = simpleTempVal + tempUnit + splitChar + currVal + currUnit;
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            batteryInfo = currVal + currUnit + splitChar + simpleTempVal + tempUnit;
                                        }
                                    }
                                    else if (opt == 4) {
                                        String splitChar = mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow") ? " " : "\n";
                                        batteryInfo = simpleTempVal + tempUnit + splitChar + simpleWatt + powerUnit;
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            batteryInfo = simpleWatt + powerUnit + splitChar + simpleTempVal + tempUnit;
                                        }
                                    } else if (opt == 2) {
                                        batteryInfo = simpleWatt + powerUnit;
                                    } else if (opt == 5) {
                                        String splitChar = mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow") ? " " : "\n";
                                        batteryInfo = currVal + currUnit + splitChar + simpleWatt + powerUnit;
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
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
                                    int opt = mPrefs.getStringAsInt("system_statusbar_showdevicetemperature_content", 1);
                                    boolean hideUnit = mPrefs.getBoolean("system_statusbar_showdevicetemperature_hideunit");
                                    String tempUnit = hideUnit ? "" : "℃";
                                    if (opt == 1) {
                                        String splitChar = mPrefs.getBoolean("system_statusbar_showdevicetemperature_singlerow")
                                            ? " " : "\n";
                                        deviceInfo = simpleBatteryTemp + tempUnit + splitChar + simpleCpuTemp + tempUnit;
                                        if (mPrefs.getBoolean("system_statusbar_showdevicetemperature_reverseorder")) {
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
        return (TextView) XposedHelpers.getObjectField(iconView, "mNetworkSpeedNumberText");
    }

    private static void initStatusbarTextIcon(Context mContext, int iconType, View iconView, boolean fromController) {
        if (!fromController) {
            XposedHelpers.callMethod(iconView, "setBlocked", false);
        }
        TextView iconTextView = getIconTextView(iconView);
        Resources res = mContext.getResources();
        int styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
        iconTextView.setTextAppearance(styleId);
        String subKey = "";
        if (iconType == 91) {
            subKey = "batterytempandcurrent";
        }
        else if (iconType == 92) {
            subKey = "showdevicetemperature";
        }
        float fontSize = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_fontsize", 16) * 0.5f;
        int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_" + subKey + "_content", 1);
        if ((opt == 1 || opt == 4 || opt == 5) && !MainModule.mPrefs.getBoolean("system_statusbar_" + subKey + "_singlerow")) {
            iconTextView.setMaxLines(2);
            float lineSpacing = fontSize > 8.5f ? 0.85f : 0.9f;
            int defaultMupliplier = 65;
            float lineSpacingMultiplier = MainModule.mPrefs.getInt("system_statusbar_line_spacing_multiplier", defaultMupliplier);
            if (lineSpacingMultiplier > defaultMupliplier) {
                lineSpacing = lineSpacingMultiplier / 100f;
            }
            iconTextView.setLineSpacing(0, lineSpacing);
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
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) iconTextView.getLayoutParams();
            lp.width = (int)Helpers.dp2px(fixedWidth);
            iconTextView.setLayoutParams(lp);
        }

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

    private static View createStatusbarTextIcon(Context mContext, LinearLayout.LayoutParams lp, int iconType, boolean fromController) {
        View iconView = LayoutInflater.from(mContext).inflate(statusbarTextIconLayoutResId, null);
        ModuleHelper.setViewInfo(iconView, "text_icon_type", iconType);
        iconView.setLayoutParams(lp);
        View mNumber = iconView.findViewWithTag("network_speed_number");
        XposedHelpers.setObjectField(iconView, "mNetworkSpeedNumberText", mNumber);
        View mUnit = iconView.findViewWithTag("network_speed_unit");
        XposedHelpers.setObjectField(iconView, "mNetworkSpeedUnitText", mUnit);
        initStatusbarTextIcon(mContext, iconType, iconView, fromController);
        return iconView;
    }
    static final ArrayList<View> mStatusbarTextIcons = new ArrayList<View>();

    public static void AddCustomTileHook(PackageReadyParam lpparam) {
        final boolean enable5G = MainModule.mPrefs.getBoolean("system_fivegtile");
        final boolean enableFps = MainModule.mPrefs.getBoolean("system_cc_fpstile");
        final boolean enableFloatingTime = MainModule.mPrefs.getBoolean("system_cc_floatingtimetile");
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            private boolean isListened = false;
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
                    if (enableFloatingTime) {
                        stockTiles = stockTiles + ",custom_floatingtime";
                    }
                    MainModule.resHooks.setObjectReplacement("com.android.systemui", "string", "miui_quick_settings_tiles_stock", stockTiles);
                }
            }
        });
        Class<?> ResourceIconClass = findClass("com.android.systemui.qs.tileimpl.QSTileImpl$ResourceIcon", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.MiuiQSFactory", lpparam.getClassLoader(), "createTile", String.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String tileName = (String) param.getArgs()[0];
                if (tileName.startsWith("custom_")) {
                    Object provider = XposedHelpers.getObjectField(param.getThisObject(), "nfcTileProvider");
                    Object tile = XposedHelpers.callMethod(provider, "get");
                    XposedHelpers.setAdditionalInstanceField(tile, "customName", tileName);
                    XposedHelpers.callMethod(tile, "handleInitialize");
                    XposedHelpers.callMethod(tile, "handleStale");
                    XposedHelpers.callMethod(tile, "setTileSpec", tileName);
                    param.returnAndSkip(tile);
                }
            }
        });
        String NfcTileCls = "com.android.systemui.qs.tiles.MiuiNfcTile";
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "isAvailable", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    if ("custom_5G".equals(tileName)) {
                        param.returnAndSkip(enable5G);
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        param.returnAndSkip(enableFps);
                    }
                    else if ("custom_floatingtime".equals(tileName)) {
                        param.returnAndSkip(enableFloatingTime);
                    }
                    else {
                        param.returnAndSkip(false);
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "getTileLabel", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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
                    else if ("custom_floatingtime".equals(tileName)) {
                        param.returnAndSkip(modRes.getString(R.string.qs_toggle_floatingtime));
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "handleSetListening", boolean.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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
                    else if ("custom_floatingtime".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        boolean mListening = (boolean) param.getArgs()[0];
                        if (mListening) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean z) {
                                    XposedHelpers.callMethod(param.getThisObject(), "refreshState");
                                }
                            };
                            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("miui_time_floating_window"), false, contentObserver);
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "tileListener", contentObserver);
                        }
                        else {
                            ContentObserver contentObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "tileListener");
                            mContext.getContentResolver().unregisterContentObserver(contentObserver);
                        }
                    }

                    param.returnAndSkip(null);
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "handleShowStateMessage", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    param.returnAndSkip(null);
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "getLongClickIntent", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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
        ModuleHelper.hookAllMethods(NfcTileCls, lpparam.getClassLoader(), "handleClick", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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
                    else if ("custom_floatingtime".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        boolean isEnable = ((int) XposedHelpers.callStaticMethod(Settings.System.class, "getIntForUser", mContext.getContentResolver(), "miui_time_floating_window", 0, -2)) != 0;
                        XposedHelpers.callStaticMethod(Settings.System.class, "putIntForUser", mContext.getContentResolver(), "miui_time_floating_window", isEnable ? 0 : 1, -2);
                    }
                    param.returnAndSkip(null);
                }
            }
        });

        ArrayMap<String, Integer> tileOnResMap =  new ArrayMap<String, Integer>();
        ArrayMap<String, Integer> tileOffResMap =  new ArrayMap<String, Integer>();
        if (enable5G) {
            tileOnResMap.put("custom_5G", MainModule.resHooks.addFakeResource("ic_qs_m5g_on", R.drawable.ic_qs_5g_on, "drawable"));
            tileOffResMap.put("custom_5G", MainModule.resHooks.addFakeResource("ic_qs_m5g_off", R.drawable.ic_qs_5g_off, "drawable"));
        }
        if (enableFps) {
            tileOnResMap.put("custom_FPS", MainModule.resHooks.addFakeResource("ic_qs_mfps_on", R.drawable.ic_qs_fps_on, "drawable"));
            tileOffResMap.put("custom_FPS", MainModule.resHooks.addFakeResource("ic_qs_mfps_off", R.drawable.ic_qs_fps_off, "drawable"));
        }
        if (enableFloatingTime) {
            tileOnResMap.put("custom_floatingtime", MainModule.resHooks.addFakeResource("ic_qs_mfloatingtime_on", R.drawable.ic_qs_second_off, "drawable"));
            tileOffResMap.put("custom_floatingtime", MainModule.resHooks.addFakeResource("ic_qs_mfloatingtime_off", R.drawable.ic_qs_second_on, "drawable"));
        }
        ModuleHelper.hookAllMethods(NfcTileCls, lpparam.getClassLoader(), "handleUpdateState", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
                    boolean isEnable = false;
                    if ("custom_5G".equals(tileName)) {
                        TelephonyManager manager = TelephonyManager.getDefault();
                        isEnable = manager.isUserFiveGEnabled();
                    }
                    else if ("custom_FPS".equals(tileName)) {
                        IBinder mSurfaceFlinger = (IBinder) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger");
                        if (mSurfaceFlinger != null) {
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
                    else if ("custom_floatingtime".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        isEnable = ((int) XposedHelpers.callStaticMethod(Settings.System.class, "getIntForUser", mContext.getContentResolver(), "miui_time_floating_window", 0, -2)) != 0;
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

    public static void DualRowsStatusbarHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
                leftContainer.setTag("mStatusBarLeftContainer");
                LinearLayout statusBarcontents = (LinearLayout) leftContainer.getParent();
                LinearLayout leftLayout = new LinearLayout(mContext);
                LinearLayout rightLayout = new LinearLayout(mContext);
                statusBarcontents.addView(leftLayout, 0);
                statusBarcontents.addView(rightLayout);
                LinearLayout leftGroup;

                if (clock2Rows) {
                    TextView mMiuiClock = (TextView) XposedHelpers.getObjectField(sbView, "mClock");
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

                int rightChildCount = rightContainer.getChildCount();
                for (int i = rightChildCount - 1; i >= 0; i--) {
                    View child = rightContainer.getChildAt(i);
                    rightContainer.removeView(child);
                    firstRight.addView(child, 0);
                }

                int resSystemIconsId = sbView.getResources().getIdentifier("system_icons", "id", lpparam.getPackageName());
                rightLayout.setId(resSystemIconsId);

                boolean showBatteryDetail = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent");
                boolean showDeviceTemp = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature");
                boolean batteryAtRight = showBatteryDetail && MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
                boolean tempAtRight = showDeviceTemp && MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");
                ArrayList<Integer> customIconTypes = new ArrayList<Integer>();
                if (batteryAtRight) {
                    customIconTypes.add(91);
                }
                if (tempAtRight) {
                    customIconTypes.add(92);
                }
                if (!customIconTypes.isEmpty()) {
                    Object DarkIconDispatcher = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.plugins.DarkIconDispatcher");
                    for (int iconType:customIconTypes) {
                        View iconView = createStatusbarTextIcon(mContext, new LinearLayout.LayoutParams(-2, -2), iconType, false);
                        secondRight.addView(iconView, 0);
                        mStatusbarTextIcons.add(iconView);
                        XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", iconView);
                    }
                }

                statusBarcontents.removeView(rightContainer);

                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "leftLayout", leftLayout);
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "rightLayout", rightLayout);

                if (MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow")) {
                    ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.ui.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setNetworkSpeedIcon", new MethodHook() {
                        View networkSpeedView = null;
                        @Override
                        protected void after(MethodHookParam param) throws Throwable {
                            Object networkSpeedState = param.getArgs()[0];
                            if (networkSpeedView == null) {
                                Context mContext = secondRight.getContext();
                                int layoutResId = mContext.getResources().getIdentifier("network_speed", "layout", "com.android.systemui");
                                networkSpeedView = LayoutInflater.from(mContext).inflate(layoutResId, (ViewGroup) null);
                                secondRight.addView(networkSpeedView, 0, new LinearLayout.LayoutParams(-2, -2));
                                Object DarkIconDispatcher = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.plugins.DarkIconDispatcher");
                                XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", networkSpeedView);
                            }
                            if (networkSpeedView != null) {
                                XposedHelpers.callMethod(networkSpeedView, "setBlocked", false);
                                XposedHelpers.callMethod(networkSpeedView, "setNetworkSpeed",
                                    XposedHelpers.getObjectField(networkSpeedState, "networkSpeedNumber"),
                                    XposedHelpers.getObjectField(networkSpeedState, "networkSpeedUnit")
                                );
                                XposedHelpers.callMethod(networkSpeedView, "setVisibilityByController",
                                    XposedHelpers.getObjectField(networkSpeedState, "visible")
                                );
                            }
                        }
                    });
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "updateCutoutLocation", new MethodHook(-1000) {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.getThisObject(), "mCurrentStatusBarType");
                LinearLayout leftLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "leftLayout");
                LinearLayout rightLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "rightLayout");

                if (mCurrentStatusBarType == 0) {
                    int leftWidth = MainModule.mPrefs.getInt("system_statusbar_dualrows_left_ratio", 4);
                    LinearLayout.LayoutParams leftLayoutLp = new LinearLayout.LayoutParams(0, -1, leftWidth);
                    leftLayout.setLayoutParams(leftLayoutLp);
                    LinearLayout.LayoutParams rightLayoutLp = new LinearLayout.LayoutParams(0, -1, 10 - leftWidth);
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
        ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.shade.ControlCenterHeaderExpandController", lpparam.getClassLoader(), "updateLocation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int[] realStatusBarLocation = (int[]) XposedHelpers.getObjectField(param.getThisObject(), "realStatusBarLocation");
                int[] normalControlStatusIconsLocation = (int[]) XposedHelpers.getObjectField(param.getThisObject(), "normalControlStatusIconsLocation");
                View realStatusIcons = (View) XposedHelpers.getObjectField(param.getThisObject(), "realStatusIcons");
                Object headerController = XposedHelpers.getObjectField(param.getThisObject(), "headerController");
                Object combinedHeaderController = XposedHelpers.callMethod(headerController, "get");
                View controlCenterStatusIcons = (View) XposedHelpers.getObjectField(combinedHeaderController, "controlCenterStatusIcons");
                if (realStatusIcons == null || controlCenterStatusIcons == null) {
                    return;
                }
                int normalControlStatusBarTranslationY = XposedHelpers.getIntField(param.getThisObject(), "normalControlStatusBarTranslationY");
                int fiexedNormalControlStatusBarTranslationY = (realStatusIcons.getHeight() / 2 + realStatusBarLocation[1]) - (controlCenterStatusIcons.getHeight() / 2 + normalControlStatusIconsLocation[1]);
                if (normalControlStatusBarTranslationY != fiexedNormalControlStatusBarTranslationY) {
                    XposedHelpers.setObjectField(param.getThisObject(), "normalControlStatusBarTranslationY", fiexedNormalControlStatusBarTranslationY);
                }
            }
        });
    }

    private static void initDigitalSignalView(Context mContext, TextView digitalTextView) {
        Resources res = mContext.getResources();
        int styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
        digitalTextView.setTextAppearance(styleId);
        String subKey = "mobile_digital_signal";
        float fontSize = MainModule.mPrefs.getInt("system_statusbar_" + subKey + "_fontsize", 26) * 0.5f;
        if (MainModule.mPrefs.getBoolean("system_statusbar_" + subKey + "_in2rows")) {
            digitalTextView.setMaxLines(2);
            digitalTextView.setLineSpacing(0, fontSize > 8.5f ? 0.85f : 0.9f);
        }
        digitalTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        if (MainModule.mPrefs.getBoolean("system_statusbar_" + subKey + "_bold")) {
            digitalTextView.setTypeface(Typeface.DEFAULT_BOLD);
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
        digitalTextView.setPaddingRelative(leftMargin, topMargin, rightMargin, 0);
        int align = MainModule.mPrefs.getStringAsInt("system_statusbar_" + subKey + "_align", 1);
        if (align == 2) {
            digitalTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        else if (align == 3) {
            digitalTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else if (align == 4) {
            digitalTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
    }

    public static void StatusBarDigitalSignalHook(PackageReadyParam lpparam) {
        SparseIntArray signalLevelMap = new SparseIntArray();
        Class<?> MobileStatusTrackerClass = findClass("com.android.systemui.statusbar.mobile.MobileStatusTracker", lpparam.getClassLoader());
        Field mCallback = XposedHelpers.findField(MobileStatusTrackerClass, "mCallback");
        ModuleHelper.findAndHookMethod(mCallback.getType(), "onMobileStatusChanged", boolean.class, "com.android.systemui.statusbar.mobile.MobileStatusTracker$MobileStatus", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object mobileStatus = param.getArgs()[1];
                Object mobileSignalController = XposedHelpers.getSurroundingThis(param.getThisObject());
                SubscriptionInfo subscriptionInfo = (SubscriptionInfo) XposedHelpers.getObjectField(mobileSignalController, "mSubscriptionInfo");
                int sid = subscriptionInfo.getSubscriptionId();
                Object signalStrength = XposedHelpers.getObjectField(mobileStatus, "signalStrength");
                if (signalStrength != null) {
                    int dbm = (int) XposedHelpers.callMethod(signalStrength, "getDbm");
                    signalLevelMap.put(sid, dbm);
                }
            }
        });
        MethodHook stateUpdateHook = new MethodHook() {
            boolean initAction = false;
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                if ("updateState".equals(param.getMember().getName())) {
                    return;
                }
                Object mState = XposedHelpers.getObjectField(param.getThisObject(), "mState");
                initAction = mState == null;
            }
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean updateStateMethod = "updateState".equals(param.getMember().getName());
                View mMobile = (View) XposedHelpers.getObjectField(param.getThisObject(), "mMobile");
                FrameLayout signalImageContainer = (FrameLayout) mMobile.getParent();
                if (initAction) {
                    TextView digitalView = new TextView(signalImageContainer.getContext());
                    initDigitalSignalView(signalImageContainer.getContext(), digitalView);
                    signalImageContainer.addView(digitalView);
                    digitalView.setTag("digitalSignalView");
                    mMobile.setVisibility(View.GONE);
                }
                if (updateStateMethod || initAction) {
                    Object mobileIconState = param.getArgs()[0];
                    boolean visible = XposedHelpers.getBooleanField(mobileIconState, "visible");
                    if (!visible) return;
                    boolean airplane = XposedHelpers.getBooleanField(mobileIconState, "airplane");
                    if (airplane) return;
                    boolean dualRows = MainModule.mPrefs.getBoolean("system_statusbar_mobile_digital_signal_in2rows");
                    int subId = (int) XposedHelpers.getObjectField(mobileIconState, "subId");
                    TextView digitalView = signalImageContainer.findViewWithTag("digitalSignalView");
                    boolean hideUnit = MainModule.mPrefs.getBoolean("system_statusbar_mobile_digital_signal_hideunit");
                    if (dualRows) {
                        int slotId = SubscriptionManager.getSlotIndex(subId);
                        if (slotId == 0) {
                            int subSubId = SubscriptionManager.getSubscriptionId(1);
                            digitalView.setText(signalLevelMap.get(subId) + (hideUnit ? "" : "dBm")
                                + "\n" + signalLevelMap.get(subSubId) + (hideUnit ? "" : "dBm")
                            );
                        }
                    }
                    else {
                        digitalView.setText(signalLevelMap.get(subId) + (hideUnit ? "" : "dBm"));
                    }
                }
                if (!updateStateMethod) {
                    initAction = false;
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "applyMobileState", stateUpdateHook);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "updateState", stateUpdateHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.getClassLoader(), "applyDarknessInternal", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle");
                TextView digitalView = ((LinearLayout) param.getThisObject()).findViewWithTag("digitalSignalView");
                if (digitalView != null) {
                    digitalView.setTextColor(mMobileTypeSingle.getCurrentTextColor());
                }
            }
        });
        boolean dualRows = MainModule.mPrefs.getBoolean("system_statusbar_mobile_digital_signal_in2rows");
        if (dualRows) {
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setMobileIcons", new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void before(final MethodHookParam param) throws Throwable {
                    if (!isHooked) {
                        isHooked = true;
                    }
                    List<?> iconStates = (List<?>) param.getArgs()[1];
                    if (iconStates.size() == 2) {
                        Object iconState0 = iconStates.get(0);
                        Object iconState1 = iconStates.get(1);
                        Object mainIconState, subIconState;
                        int subId = (int) XposedHelpers.getObjectField(iconState0, "subId");
                        int slotId = SubscriptionManager.getSlotIndex(subId);
                        if (slotId == 0) {
                            mainIconState = iconState0;
                            subIconState = iconState1;
                        }
                        else {
                            mainIconState = iconState1;
                            subIconState = iconState0;
                        }
                        XposedHelpers.setObjectField(subIconState, "visible", false);
                        boolean subDataConnected = (boolean) XposedHelpers.getObjectField(subIconState, "dataConnected");
                        if (subDataConnected) {
                            String[] syncFields = { "showName", "activityIn", "activityOut", "dataConnected" };
                            for (String field : syncFields) {
                                XposedHelpers.setObjectField(mainIconState, field, XposedHelpers.getObjectField(subIconState, field));
                            }
                        }
                        param.getArgs()[1] = iconStates;
                    }
                }
            });
        }
    }

    static void onMobileIconDarkChanged(PackageReadyParam lpparam) {
        Class<?> DarkIconDispatcher = findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.getClassLoader());
        Method isInAreas = null;
        Method getTint = null;
        try {
            isInAreas = DarkIconDispatcher.getMethod("isInAreas", Collection.class, View.class);
            getTint = DarkIconDispatcher.getMethod("getTint", Collection.class, View.class, int.class);
        }
        catch (Throwable e) {
            XposedHelpers.log("DarkIconDispatcher.isInArea not found");
        }
        if (isInAreas == null || getTint == null) return;
        final Method isInAreasFinal = isInAreas;
        final Method getTintFinal = getTint;
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.pipeline.shared.ui.view.ModernStatusBarView", lpparam.getClassLoader(), "onDarkChanged", ArrayList.class, float.class, int.class, int.class, int.class, boolean.class, new MethodHook() {
            int mobileTypeId = -1;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                String slot = (String) XposedHelpers.getObjectField(param.getThisObject(), "slot");
                if ("mobile".equals(slot)) {
                    float mDarkIntensity = (float) param.getArgs()[1];
                    int mLightColor = (int) param.getArgs()[3];
                    int mDarkColor = (int) param.getArgs()[4];
                    int mTint = (int) param.getArgs()[2];
                    boolean mUseTint = (boolean) param.getArgs()[5];
                    ViewGroup mobileView = (ViewGroup) param.getThisObject();
                    if (mobileTypeId < 1) {
                        mobileTypeId = mobileView.getResources().getIdentifier("mobile_type_single", "id", "com.android.systemui");
                    }
                    TextView mMobileTypeSingle = mobileView.findViewById(mobileTypeId);
                    if (mUseTint) {
                        mMobileTypeSingle.setTextColor((int) getTintFinal.invoke(null, param.getArgs()[0], mMobileTypeSingle, mTint));
                    }
                    else {
                        boolean inArea = (boolean) isInAreasFinal.invoke(null, param.getArgs()[0], mMobileTypeSingle);
                        float f = mDarkIntensity;
                        if (!inArea) {
                            f = 0.0f;
                        }
                        mMobileTypeSingle.setTextColor(f > 0.0f ? mDarkColor : mLightColor);
                    }
                }
            }
        });
    }

    public static void DualRowSignalHook(PackageReadyParam lpparam) {
        final int SUBMOBILE_ID = ResourceHooks.getFakeResId("sub_mobile_signal");
        boolean mobileTypeSingle = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single");
        if (!mobileTypeSingle) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_half_to_top_distance", 3);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_mobile_left_inout_over_strength", 0);
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_middle_to_strength_start", -0.4f);
        }

        HashMap<String, Integer> lightIconName2IdMap = new HashMap<String, Integer>();
        SparseIntArray signalResToLevelMap = new SparseIntArray();
        String selectedIconStyle = MainModule.mPrefs.getString("system_statusbar_dualsimin2rows_style", "");
        Class<?> IconsClass = findClass("com.android.systemui.statusbar.Icons", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext");
                Resources modRes = ModuleHelper.getModuleRes(mContext);
                String styleSuffix = !selectedIconStyle.isEmpty() ? ("_" + selectedIconStyle) : "";
                Map sTintIconMap = (Map) XposedHelpers.getStaticObjectField(IconsClass, "sTintIconMap");
                Map sDarkIconMap = (Map) XposedHelpers.getStaticObjectField(IconsClass, "sDarkIconMap");
                for (int slot = 1; slot <= 2; slot++) {
                    for (int lvl = 0; lvl <= 5; lvl++) {
                        if (!selectedIconStyle.equals("theme")) {
                            String dualIconResName = "statusbar_signal_" + slot + "_" + lvl + styleSuffix;
                            int iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg);
                            int lightIconResId = MainModule.resHooks.addFakeResource(dualIconResName, iconResId, "drawable");
                            lightIconName2IdMap.put(dualIconResName, lightIconResId);
                            dualIconResName = "statusbar_signal_" + slot + "_" + lvl + "_dark" + styleSuffix;
                            iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg);
                            int darkIconResId = MainModule.resHooks.addFakeResource(dualIconResName, iconResId, "drawable");
                            sDarkIconMap.put(Integer.valueOf(lightIconResId), Integer.valueOf(darkIconResId));
                            dualIconResName = "statusbar_signal_" + slot + "_" + lvl + "_tint" + styleSuffix;
                            iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg);
                            int tintIconResId = MainModule.resHooks.addFakeResource(dualIconResName, iconResId, "drawable");
                            sTintIconMap.put(Integer.valueOf(lightIconResId), Integer.valueOf(tintIconResId));
                        }
                        else {
                            String dualIconResName = "statusbar_signal_" + slot + "_" + lvl + styleSuffix;
                            int iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg);
                            int lightIconResId = MainModule.resHooks.addFakeResource(dualIconResName, iconResId, "drawable");
                            lightIconName2IdMap.put(dualIconResName, lightIconResId);
                            dualIconResName = "statusbar_signal_" + slot + "_" + lvl + "_dark" + styleSuffix;
                            iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg);
                            int darkIconResId = MainModule.resHooks.addFakeResource(dualIconResName, iconResId, "drawable");
                            sDarkIconMap.put(Integer.valueOf(lightIconResId), Integer.valueOf(darkIconResId));
                        }
                    }
                }
                Resources res = mContext.getResources();
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_0", "drawable", lpparam.getPackageName()), 0);
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_1", "drawable", lpparam.getPackageName()), 1);
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_2", "drawable", lpparam.getPackageName()), 2);
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_3", "drawable", lpparam.getPackageName()), 3);
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_4", "drawable", lpparam.getPackageName()), 4);
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_5", "drawable", lpparam.getPackageName()), 5);
                signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_null", "drawable", lpparam.getPackageName()), 0);
            }
        });

        final int[] subscriptionsData = {-1, 0, 0}; // data-subId, main-level, sub-level
        Class<?> MiuiMobileIconVMImplClass = findClassIfExists("com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.MiuiMobileIconVMImpl", lpparam.getClassLoader());
        final boolean newMobileIconVMImpl = MiuiMobileIconVMImplClass != null;
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.pipeline.mobile.ui.MobileUiAdapter", lpparam.getClassLoader(), "start", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                final SparseIntArray subIdLevels = new SparseIntArray();
                Object mobileIconsViewModel = XposedHelpers.getObjectField(param.getThisObject(), "mobileIconsViewModel");
                Map iconsVM = (Map) XposedHelpers.getObjectField(mobileIconsViewModel, "reuseCache");
                Object mStatusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                Object javaAdapter = XposedHelpers.getObjectField(mStatusBar, "mJavaAdapter");
                Object dataSubIdFlow = ModuleHelper.getObjectFieldByPath(mobileIconsViewModel, "miuiInt.mobileConnectionsRepo.activeMobileDataSubscriptionId");
                Consumer<Integer> setSignalStateConsumer = (s) -> {
                    if (subscriptionsData[0] != -1) {
                        Integer dataSubIdInteger = Integer.valueOf(subscriptionsData[0]);
                        Object obj = iconsVM.get(dataSubIdInteger);
                        if (obj != null) {
                            Object iconViewModel = XposedHelpers.callMethod(obj, "getThird");
                            if (newMobileIconVMImpl) {
                                iconViewModel = XposedHelpers.callMethod(iconViewModel, "getCellProvider");
                            }
                            Object signalIconId = XposedHelpers.getObjectField(iconViewModel, "signalIconId");
                            Object iconState = ModuleHelper.getMutableFlowOfReadonlyFlow(signalIconId);
                            XposedHelpers.callMethod(iconState, "setValue", Integer.valueOf(subscriptionsData[1] * 10 + subscriptionsData[2]));
                        }
                    }
                };
                XposedHelpers.callMethod(javaAdapter, "alwaysCollectFlow", dataSubIdFlow, new Consumer() {
                    @Override
                    public void accept(Object dataSubIdObject) {
                        if (dataSubIdObject != null) {
                            int dataSubId = ((Integer) dataSubIdObject).intValue();
                            subscriptionsData[0] = dataSubId;
                            Set keySet = iconsVM.keySet();
                            for (Object subIdInt : keySet) {
                                int tmpSubId = ((Integer) subIdInt).intValue();
                                if (tmpSubId != dataSubId) {
                                    subscriptionsData[2] = subIdLevels.get(tmpSubId);
                                    break;
                                }
                            }
                            subscriptionsData[1] = subIdLevels.get(dataSubId);
                            setSignalStateConsumer.accept(null);
                        }
                    }
                });
                Object subIdObject = XposedHelpers.callMethod(dataSubIdFlow, "getValue");
                if (subIdObject != null) {
                    subscriptionsData[0] = ((Integer) subIdObject).intValue();
                }
                ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.MiuiCellularIconVM", lpparam.getClassLoader(), new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) throws Throwable {
                        int subId = XposedHelpers.getIntField(param.getArgs()[0], "subscriptionId");
                        Object iconViewModel = param.getThisObject();
                        Object originIconIdFlow = XposedHelpers.getObjectField(iconViewModel, "signalIconId");
                        Object iconFlow = ModuleHelper.createReadonlyFlowWithInitValue(Integer.valueOf(0), lpparam.getClassLoader());
                        XposedHelpers.setObjectField(iconViewModel, "signalIconId", iconFlow);
                        XposedHelpers.callMethod(javaAdapter, "alwaysCollectFlow", originIconIdFlow, new Consumer() {
                            @Override
                            public void accept(Object iconIdObject) {
                                Object obj = iconsVM.get(Integer.valueOf(subId));
                                if (obj != null && iconIdObject != null) {
                                    int levelIconResId = ((Integer) iconIdObject).intValue();
                                    int level = signalResToLevelMap.get(levelIconResId);
                                    subIdLevels.put(subId, level);
                                    if (subscriptionsData[0] != -1) {
                                        if (subId == subscriptionsData[0]) {
                                            subscriptionsData[1] = level;
                                        }
                                        else {
                                            subscriptionsData[2] = level;
                                        }
                                        setSignalStateConsumer.accept(null);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        Class<?> MiuiStatusBarIconViewHelperClass = findClass("com.android.systemui.statusbar.MiuiStatusBarIconViewHelper", lpparam.getClassLoader());
        MethodHook updateMobileImageHook = new MethodHook() {
            int mobileSignalId = -1;
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                ImageView mMobile = (ImageView) param.getArgs()[0];
                if (mobileSignalId == -1) {
                    mobileSignalId = mMobile.getResources().getIdentifier("mobile_signal", "id", "com.android.systemui");
                }
                if (mMobile.getId() == mobileSignalId) {
                    Object subIdObject = ModuleHelper.getViewInfo(mMobile, "subId");
                    if (subIdObject == null || subscriptionsData[0] == -1) {
                        param.returnAndSkip(null);
                        return;
                    };
                    if ((int) subIdObject != subscriptionsData[0]) {
                        param.returnAndSkip(null);
                        return;
                    }
                    ViewGroup mobileView = (ViewGroup) mMobile.getParent();
                    ImageView subMobile = mobileView.findViewById(SUBMOBILE_ID);
                    boolean mUseTint;
                    boolean mLight;
                    if (selectedIconStyle.equals("theme")) {
                        mUseTint = false;
                    }
                    else {
                        mUseTint = ((Boolean) XposedHelpers.callMethod(param.getArgs()[2], "getFirst")).booleanValue();
                    }
                    mLight = ((Boolean) XposedHelpers.callMethod(param.getArgs()[2], "getSecond")).booleanValue();
                    int mainLevel = subscriptionsData[1];
                    int subLevel = subscriptionsData[2];
                    String styleSuffix = !selectedIconStyle.isEmpty() ? ("_" + selectedIconStyle) : "";
                    String sim1IconId = "statusbar_signal_1_" + mainLevel + styleSuffix;
                    String sim2IconId = "statusbar_signal_2_" + subLevel + styleSuffix;
                    int sim1LightResId = lightIconName2IdMap.get(sim1IconId);
                    mMobile.setTag(Integer.valueOf(sim1LightResId));
                    int sim1FinalResId = (int) XposedHelpers.callStaticMethod(MiuiStatusBarIconViewHelperClass, "transformResId", sim1LightResId, mUseTint, mLight);
                    mMobile.setImageResource(sim1FinalResId);
                    if (subMobile != null) {
                        int sim2LightResId = lightIconName2IdMap.get(sim2IconId);
                        int sim2FinalResId = (int) XposedHelpers.callStaticMethod(MiuiStatusBarIconViewHelperClass, "transformResId", sim2LightResId, mUseTint, mLight);
                        subMobile.setTag(Integer.valueOf(sim2LightResId));
                        subMobile.setImageResource(sim2FinalResId);
                    }
                    param.returnAndSkip(null);
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.pipeline.mobile.ui.binder.MiuiMobileIconBinder", lpparam.getClassLoader(), "access$setImageResWithTintLight", updateMobileImageHook);

        Class<?> MobileIconBind1 = findClass("com.android.systemui.statusbar.pipeline.mobile.ui.binder.MiuiMobileIconBinder$bind$1", lpparam.getClassLoader());
        ModuleHelper.hookAllConstructors(MobileIconBind1, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object viewModel = XposedHelpers.getObjectField(param.getThisObject(), "$viewModel");
                String mobileViewModelClassName = viewModel.getClass().getName();
                if (mobileViewModelClassName.contains("ShadeCarrierGroupMobileIconViewMode")) return;
                ImageView mMobile = (ImageView) XposedHelpers.getObjectField(param.getThisObject(), "$mobile");
                ViewGroup mobileView = (ViewGroup) mMobile.getParent();
                ImageView subMobile = mobileView.findViewById(SUBMOBILE_ID);
                if (subMobile != null) {
                    List tintViewList = (List) XposedHelpers.getObjectField(param.getThisObject(), "$tintViewList");
                    ArrayList tintViewListMore = new ArrayList(tintViewList);
                    tintViewListMore.add(subMobile);
                    XposedHelpers.setObjectField(param.getThisObject(), "$tintViewList", tintViewListMore);
                }
            }
        });

        Class<?> AlphaOptimizedImageView = findClass("com.android.systemui.statusbar.AlphaOptimizedImageView", lpparam.getClassLoader());
        MethodHook initHook = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                String mobileViewModelClassName = param.getArgs()[1].getClass().getName();
                if (mobileViewModelClassName.contains("ShadeCarrierGroupMobileIconViewMode")) return;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_rightmargin", 0);
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_leftmargin", 0);
                int iconScale = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_scale", 10);
                int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_verticaloffset", 8);
                ViewGroup mobileViewOuter = (ViewGroup) param.getArgs()[0];
                int mobileViewId = mobileViewOuter.getResources().getIdentifier("mobile_group", "id", "com.android.systemui");
                LinearLayout mobileView = mobileViewOuter.findViewById(mobileViewId);
                int rightSpacing = (int) Helpers.dp2px(rightMargin * 0.5f);
                int leftSpacing = (int) Helpers.dp2px(leftMargin * 0.5f);
                mobileView.setPadding(leftSpacing, 0, rightSpacing, 0);
                int mobileSignalId = mobileViewOuter.getResources().getIdentifier("mobile_signal", "id", "com.android.systemui");
                View mMobile = mobileView.findViewById(mobileSignalId);
                FrameLayout signalWrapper = (FrameLayout) mMobile.getParent();
                ImageView subMobile = (ImageView) XposedHelpers.newInstance(AlphaOptimizedImageView, mobileViewOuter.getContext());
                signalWrapper.addView(subMobile);
                subMobile.setAdjustViewBounds(true);
                subMobile.setId(SUBMOBILE_ID);
                if (verticalOffset != 8) {
                    float marginTop = Helpers.dp2px((verticalOffset - 8) * 0.5f);
                    signalWrapper.setTranslationY(marginTop);
                }
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMobile.getLayoutParams();
                layoutParams.width = -2;
                if (iconScale != 10) {
                    int mIconHeight = (int) Helpers.dp2px(2.0f * iconScale);
                    layoutParams.height = mIconHeight;
                    layoutParams.gravity = Gravity.CENTER;
                }
                else {
                    layoutParams.height = -1;
                }
                mMobile.setLayoutParams(layoutParams);
                subMobile.setLayoutParams(layoutParams);
                int subId = XposedHelpers.getIntField(mobileViewOuter, "subId");
                ModuleHelper.setViewInfo(mMobile, "subId", subId);
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.pipeline.mobile.ui.binder.MiuiMobileIconBinder", lpparam.getClassLoader(), "bind", initHook);
    }

    public static void StatusBarIconsPositionAdjustHook(PackageReadyParam lpparam, boolean moveLeft) {
        PrefMap<String, Object> mPrefs = MainModule.mPrefs;
        boolean dualRows = mPrefs.getBoolean("system_statusbar_dualrows");
        boolean swapWifiSignal = mPrefs.getBoolean("system_statusbaricons_swap_wifi_mobile");
        boolean moveSignalLeft = mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft");
        boolean netspeedAtRow2 = dualRows && mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow");
        boolean showBatteryDetail = mPrefs.getBoolean("system_statusbar_batterytempandcurrent");
        boolean showDeviceTemp = mPrefs.getBoolean("system_statusbar_showdevicetemperature");
        boolean batteryAtRight = showBatteryDetail && !dualRows && mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        boolean tempAtRight = showDeviceTemp && !dualRows && mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");
        boolean batteryAtLeft = showBatteryDetail && !mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        boolean tempAtLeft = showDeviceTemp && !mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright");

        HashSet<String> leftIcons = new HashSet<String>();
        if (!netspeedAtRow2 && mPrefs.getBoolean("system_statusbar_netspeed_atleft")) {
            leftIcons.add("network_speed");
        }
        if (mPrefs.getBoolean("system_statusbar_gps_atleft")) {
            leftIcons.add("location");
        }
        if (mPrefs.getBoolean("system_statusbar_alarm_atleft")) {
            leftIcons.add("alarm_clock");
        }
        if (mPrefs.getBoolean("system_statusbar_sound_atleft")) {
            leftIcons.add("volume");
        }
        if (mPrefs.getBoolean("system_statusbar_dnd_atleft")) {
            leftIcons.add("zen");
        }
        if (batteryAtLeft) {
            leftIcons.add("battery_info");
        }
        if (tempAtLeft) {
            leftIcons.add("device_temp");
        }

        List<String> signalRelatedIcons;
        if (!swapWifiSignal) {
            signalRelatedIcons = List.of("no_sim", "hd", "mobile", "demo_mobile", "airplane", "hotspot", "wifi", "demo_wifi");
        }
        else {
            signalRelatedIcons = List.of("hotspot", "wifi", "demo_wifi", "no_sim", "hd", "mobile", "demo_mobile", "airplane");
        }
        if (moveSignalLeft) {
            leftIcons.addAll(signalRelatedIcons);
        }
        ArrayList<String> leftBlockList = new ArrayList<String>();
        ArrayList<String> keyguardRightBlockList = new ArrayList<String>();
        ModuleHelper.findAndHookConstructor("com.android.systemui.statusbar.phone.ui.StatusBarIconList", lpparam.getClassLoader(), String[].class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                ArrayList<String> allStatusIcons = new ArrayList<String>(Arrays.asList((String[]) param.getArgs()[0]));
                Class<?> MiuiIconManagerUtils = findClass("com.android.systemui.statusbar.phone.MiuiIconManagerUtils", lpparam.getClassLoader());
                ArrayList<String> rightBlockList = (ArrayList<String>) ModuleHelper.getStaticObjectFieldSilently(MiuiIconManagerUtils, "RIGHT_BLOCK_LIST");
                ArrayList<String> customIcons = new ArrayList<String>();
                if (batteryAtLeft || batteryAtRight) {
                    customIcons.add("battery_info");
                }
                if (tempAtLeft || tempAtRight) {
                    customIcons.add("device_temp");
                }
                if (!customIcons.isEmpty()) {
                    int netspeedIndex = allStatusIcons.indexOf("network_speed") + 1;
                    allStatusIcons.addAll(netspeedIndex, customIcons);
                }
                if (netspeedAtRow2) {
                    rightBlockList.add("network_speed");
                }
                if (mPrefs.getBoolean("system_statusbar_alarm_atright")) {
                    rightBlockList.remove("alarm_clock");
                }
                if (mPrefs.getBoolean("system_statusbar_btbattery_atright")) {
                    rightBlockList.remove("bluetooth_handsfree_battery");
                }
                if (mPrefs.getBoolean("system_statusbar_nfc_atright")) {
                    rightBlockList.remove("nfc");
                }
                if (mPrefs.getBoolean("system_statusbar_headset_atright")) {
                    rightBlockList.remove("headset");
                }
                if (moveLeft) {
                    keyguardRightBlockList.addAll(rightBlockList);
                    for (String slotName : allStatusIcons) {
                        if (leftIcons.contains(slotName)) {
                            rightBlockList.add(slotName);
                        }
                        else {
                            leftBlockList.add(slotName);
                        }
                    }
                }
                XposedHelpers.setStaticObjectField(MiuiIconManagerUtils, "RIGHT_BLOCK_LIST", rightBlockList);
                if (swapWifiSignal) {
                    ArrayList<String> realSignalIcons = new ArrayList<String>();
                    for (String slotName : signalRelatedIcons) {
                        if (allStatusIcons.contains(slotName)) {
                            realSignalIcons.add(slotName);
                        }
                    }
                    allStatusIcons.removeAll(signalRelatedIcons);
                    allStatusIcons.addAll(realSignalIcons);
                }
                if (!customIcons.isEmpty() || swapWifiSignal) {
                    param.getArgs()[0] = allStatusIcons.toArray(new String[0]);
                }
            }
        });

        if (moveLeft) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
                @Override
                protected void after(final MethodHookParam param) throws Throwable {
                    FrameLayout mStatusBar = (FrameLayout) param.getThisObject();
                    LinearLayout leftContainer;
                    Class<?> IconsContainer = findClass("com.android.systemui.statusbar.views.MiuiStatusIconContainer", lpparam.getClassLoader());
                    LinearLayout iconContainer = (LinearLayout) XposedHelpers.newInstance(IconsContainer, mStatusBar.getContext());
                    iconContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                    iconContainer.setTag("leftIconsContainer");
                    if (dualRows) {
                        leftContainer = mStatusBar.findViewWithTag("mStatusBarLeftContainer");
                        leftContainer.addView(iconContainer);
                    }
                    else {
                        View leftNotifyContainer = (View) XposedHelpers.getObjectField(mStatusBar, "mDripStatusBarNotificationIconArea");
                        leftContainer = (LinearLayout) leftNotifyContainer.getParent();
                        leftContainer.addView(iconContainer, leftContainer.indexOfChild(leftNotifyContainer));
                    }
                    Object miuiIconManagerFactory = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.MiuiIconManagerFactory");

                    Class<?> DarkIconManager = findClass("com.android.systemui.statusbar.phone.ui.DarkIconManager", lpparam.getClassLoader());
                    Object mDarkIconManager = XposedHelpers.newInstance(DarkIconManager,
                        iconContainer,
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mWifiUiAdapter"),
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mMobileUiAdapter"),
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mMobileContextProvider"),
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mDarkIconDispatcher")
                    );

                    Object iconController = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.ui.StatusBarIconController");
                    XposedHelpers.callMethod(iconController, "addIconGroup", mDarkIconManager);
                    XposedHelpers.callMethod(iconContainer, "setIgnoredSlots", leftBlockList);
                }
            });

            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.getClassLoader(), "updateStatusBarVisibilities", boolean.class, new MethodHook() {
                private int lastShowLeftIcons = -1;
                @Override
                protected void after(final MethodHookParam param) throws Throwable {
                    boolean mLastIsFocusedNotifPromptViewShowing = XposedHelpers.getBooleanField(param.getThisObject(), "mLastIsFocusedNotifPromptViewShowing");
                    boolean mIsShowNotifPromptView = XposedHelpers.getBooleanField(param.getThisObject(), "mIsShowNotifPromptView");
                    Object mLastModifiedVisibility = XposedHelpers.getObjectField(param.getThisObject(), "mLastModifiedVisibility");
                    boolean showSystemInfo = XposedHelpers.getBooleanField(mLastModifiedVisibility, "showSystemInfo");
                    boolean showLeftIcons = showSystemInfo && (!mIsShowNotifPromptView || !mLastIsFocusedNotifPromptViewShowing);
                    int showFlag = showLeftIcons ? 1 : 0;
                    if (showFlag == lastShowLeftIcons) return;
                    lastShowLeftIcons = showFlag;
                    FrameLayout mStatusBar = (FrameLayout) XposedHelpers.getObjectField(param.getThisObject(), "mStatusBar");
                    View leftIconContainer = mStatusBar.findViewWithTag("leftIconsContainer");
                    if (leftIconContainer != null) {
                        leftIconContainer.setVisibility(showLeftIcons ? View.VISIBLE : View.INVISIBLE);
                    }
                }
            });

            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.getClassLoader(), "miuiOnAttachedToWindow", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object mTintedIconManager = XposedHelpers.getObjectField(param.getThisObject(), "mTintedIconManager");
                    ArrayList mBlockList = (ArrayList) XposedHelpers.getObjectField(mTintedIconManager, "mBlockList");
                    mBlockList.clear();
                    mBlockList.addAll(keyguardRightBlockList);
                    Object statusBarIconController = XposedHelpers.getObjectField(mTintedIconManager, "mController");
                    XposedHelpers.callMethod(statusBarIconController, "refreshIconGroup", mTintedIconManager);
                }
            });
        }
    }

    public static void StatusBarClockPositionHook(PackageReadyParam lpparam) {
        final int pos = MainModule.mPrefs.getStringAsInt("system_statusbar_clock_position", 1);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                FrameLayout sbView = (FrameLayout) param.getThisObject();
                Context mContext = sbView.getContext();
                TextView mClockView = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mClock");
                LinearLayout leftIconsContainer = (LinearLayout) mClockView.getParent();
                leftIconsContainer.removeView(mClockView);
                View spaceView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mCutoutSpace");
                LinearLayout mContentsContainer = (LinearLayout) spaceView.getParent();
                int spaceIndex = mContentsContainer.indexOfChild(spaceView);
                LinearLayout rightContainer = new LinearLayout(mContext);
                LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -1, 1.0f);
                View mSystemIconArea = (View) XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea");
                mContentsContainer.removeView(mSystemIconArea);
                mContentsContainer.addView(rightContainer, spaceIndex + 1, rightLp);
                rightContainer.addView(mSystemIconArea);

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
        MethodHook updateClockLayout = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
                        if (Math.abs(leftPadding - rightPadding) > 12) {
                            int topPadding = sbView.getPaddingTop();
                            int bottomPadding = sbView.getPaddingBottom();
                            mStatusBarLeftContainer.setPadding(leftPadding, 0, 0, 0);
                            rightContainer.setPadding(0, 0, rightPadding, 0);
                            sbView.setPadding(0, topPadding, 0, bottomPadding);
                            View focusedNotifView = sbView.findViewWithTag("focused_notif_view");
                            if (focusedNotifView == null) {
                                int focusedNotifViewResId = sbView.getResources().getIdentifier("focused_notif_view", "id", "com.android.systemui");
                                if (focusedNotifViewResId > 0) {
                                    focusedNotifView = sbView.findViewById(focusedNotifViewResId);
                                    focusedNotifView.setTag("focused_notif_view");
                                }
                            }
                            if (focusedNotifView != null) {
                                focusedNotifView.setPaddingRelative(leftPadding, focusedNotifView.getPaddingTop(), 0, 0);
                            }
                        }
                    }
                }
                else {
                    if (pos == 2) {
                        View mCutoutSpace = (View) XposedHelpers.getObjectField(param.getThisObject(), "mCutoutSpace");
                        mCutoutSpace.setVisibility(View.GONE);
                        mStatusBarLeftContainer.setPadding(0, 0, 0, 0);
                        LinearLayout rightContainer = (LinearLayout) mSystemIconArea.getParent();
                        rightContainer.setPadding(0, 0, 0, 0);
                    }
                }
            }
        };
        Object clockLayoutUnhooker = ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "updateSafeInsets", updateClockLayout);
        if (clockLayoutUnhooker == null) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.getClassLoader(), "updateLayoutForCutout", updateClockLayout);
        }
        if (pos == 2) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "updateNotificationIconAreaInnnerParent", new MethodHook() {
                private int originType = 0;
                @Override
                protected void before(final MethodHookParam param) throws Throwable {
                    int mCurrentStatusBarType = XposedHelpers.getIntField(param.getThisObject(), "mCurrentStatusBarType");
                    if (mCurrentStatusBarType == 0) {
                        XposedHelpers.setObjectField(param.getThisObject(), "mCurrentStatusBarType", 1);
                    }
                    originType = mCurrentStatusBarType;
                }
                @Override
                protected void after(final MethodHookParam param) throws Throwable {
                    XposedHelpers.setObjectField(param.getThisObject(), "mCurrentStatusBarType", originType);
                }
            });
        }
    }

    private static long measureTime = 0;
    private static long txBytesTotal = 0;
    private static long rxBytesTotal = 0;
    private static long txSpeed = 0;
    private static long rxSpeed = 0;

    private static Pair<Long, Long> getTrafficBytes() {
        long tx = -1L;
        long rx = -1L;

        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements();) {
                NetworkInterface iface = list.nextElement();
                if (iface.isUp() && !iface.isVirtual() && !iface.isLoopback() && !iface.isPointToPoint() && !"".equals(iface.getName())) {
                    tx += TrafficStats.getTxBytes(iface.getName());
                    rx += TrafficStats.getRxBytes(iface.getName());
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

    public static void DetailedNetSpeedHook(PackageReadyParam lpparam) {
        Class<?> NetworkSpeedController = findClassIfExists("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader());
        if (NetworkSpeedController == null) {
            XposedHelpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller");
            return;
        }

        Field mBgHandlerField = XposedHelpers.findField(NetworkSpeedController, "mBgHandler");
        ModuleHelper.findAndHookMethod(mBgHandlerField.getType(), "handleMessage", Message.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Message message = (Message) param.getArgs()[0];
                if (message.what == 200001) {
                    Object thisObect = XposedHelpers.getSurroundingThis(param.getThisObject());
                    boolean isConnected = false;
                    Context mContext = (Context)XposedHelpers.getObjectField(thisObect, "mContext");
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
                        if (newTime > 12000000000L || newTime == 0) newTime = Math.round(4 * Math.pow(10, 9));
                        Pair<Long, Long> bytes = getTrafficBytes();
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
            }
        });

        ModuleHelper.hookAllMethods(NetworkSpeedController, "updateText", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                boolean hideLow = MainModule.mPrefs.getBoolean("system_detailednetspeed_low");
                int lowLevel = MainModule.mPrefs.getInt("system_detailednetspeed_lowlevel", 1) * 1024;

                int speedStyle = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_style", 1);

                String txarrow = "";
                String rxarrow = "";
                if (speedStyle == 2) {
                    int icons = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_icon", 2);
                    if (icons == 2) {
                        txarrow = txSpeed < lowLevel ? "△" : "▲";
                        rxarrow = rxSpeed < lowLevel ? "▽" : "▼";
                    } else if (icons == 3) {
                        txarrow = txSpeed < lowLevel ? " ☖" : " ☗";
                        rxarrow = rxSpeed < lowLevel ? " ⛉" : " ⛊";
                    }
                }

                String[] strArr = new String[2];
                String rx = hideLow && rxSpeed < lowLevel ? "" : humanReadableByteCount(mContext, rxSpeed) + rxarrow;
                if (speedStyle == 2) {
                    String tx = hideLow && txSpeed < lowLevel ? "" : humanReadableByteCount(mContext, txSpeed) + txarrow;
                    strArr[0] = tx + "\n" + rx;
                }
                else {
                    strArr[0] = rx;
                }
                strArr[1] = "";
                param.getArgs()[0] = strArr;
            }
        });
    }

    private static void initNetSpeedStyle(LinearLayout speedView) {
        int speedStyle = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_style", 1);
        TextView numberView = getIconTextView(speedView);
        TextView unitView = (TextView)XposedHelpers.getObjectField(speedView, "mNetworkSpeedUnitText");

        float fontSize = MainModule.mPrefs.getInt("system_netspeed_fontsize", 13) * 0.5f;
        if (fontSize > 6.5) {
            numberView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
            if (speedStyle == 1) {
                unitView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
            }
        }

        boolean boldFont = MainModule.mPrefs.getBoolean("system_netspeed_boldfont");
        if (boldFont) {
            numberView.setTypeface(Typeface.DEFAULT_BOLD);
            if (speedStyle == 1) {
                unitView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        int fixedWidth = MainModule.mPrefs.getInt("system_netspeed_fixedcontent_width", 10);
        boolean singleOrDual = speedStyle == 2 || speedStyle == 3;
        if (singleOrDual) {
            numberView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            unitView.setVisibility(View.GONE);
        }
        if (fixedWidth > 10 || singleOrDual) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) numberView.getLayoutParams();
            if (fixedWidth > 10) {
                lp.width = (int) Helpers.dp2px(fixedWidth);
            }
            if (singleOrDual) {
                lp.topMargin = 0;
                lp.height = -1;
                lp.bottomMargin = 0;
            }
            numberView.setLayoutParams(lp);
            unitView.setLayoutParams(lp);
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
        speedView.setTranslationY(topMargin);
        speedView.setPaddingRelative(leftMargin, 0, rightMargin, 0);

        int align = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_align", 1);
        if (align > 1) {
            int alignVal = View.TEXT_ALIGNMENT_TEXT_START;
            if (align == 3) {
                alignVal = View.TEXT_ALIGNMENT_CENTER;
            }
            else if (align == 4) {
                alignVal = View.TEXT_ALIGNMENT_TEXT_END;
            }
            numberView.setTextAlignment(alignVal);
            unitView.setTextAlignment(alignVal);
        }

        if (speedStyle == 2) {
            numberView.setSingleLine(false);
            numberView.setMaxLines(2);
            float lineSpacing = fontSize > 8.5f ? 0.85f : 0.9f;
            int defaultMupliplier = 65;
            float lineSpacingMultiplier = MainModule.mPrefs.getInt("system_statusbar_line_spacing_multiplier", defaultMupliplier);
            if (lineSpacingMultiplier > defaultMupliplier) {
                lineSpacing = lineSpacingMultiplier / 100f;
            }
            numberView.setLineSpacing(0, lineSpacing);
        }
    }

    public static void NetSpeedStyleHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (param.getThisObject() == null) return;
                LinearLayout speedView = (LinearLayout) param.getThisObject();
                Object inited = ModuleHelper.getViewInfo(speedView, "view_inited");
                if (inited == null && !"slot_text_icon".equals(speedView.getTag())) {
                    ModuleHelper.setViewInfo(speedView, "view_inited", true);
                    speedView.postDelayed(() -> initNetSpeedStyle(speedView), 200);
                }
            }
        });

        boolean useClockStyle = MainModule.mPrefs.getBoolean("system_netspeed_use_clock_style");
        if (useClockStyle) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    LinearLayout speedView = (LinearLayout) param.getThisObject();
                    if (!"slot_text_icon".equals(speedView.getTag())) {
                        TextView numberView = getIconTextView(speedView);
                        TextView unitView = (TextView)XposedHelpers.getObjectField(speedView, "mNetworkSpeedUnitText");
                        int styleId = speedView.getResources().getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
                        numberView.setTextAppearance(styleId);
                        int speedStyle = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_style", 1);
                        if (speedStyle == 1) {
                            unitView.setTextAppearance(styleId);
                        }
                    }
                }
            });
        }
    }

    public static void NetSpeedIntervalHook(PackageReadyParam lpparam) {
        Class<?> NetworkSpeedController = findClass("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader());
        Field mBgHandlerField = XposedHelpers.findField(NetworkSpeedController, "mBgHandler");
        ModuleHelper.findAndHookMethod(mBgHandlerField.getType(), "handleMessage", Message.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Message message = (Message) param.getArgs()[0];
                if (message.what == 200001) {
                    Handler mBgHandler = (Handler) param.getThisObject();
                    mBgHandler.removeMessages(200001);
                    long newInterval = MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000L;
                    mBgHandler.sendEmptyMessageDelayed(200001, newInterval);
                }
            }
        });
    }

    public static void MobileNetworkTypeHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.pipeline.mobile.domain.interactor.MiuiMobileIconInteractorImpl", lpparam.getClassLoader(), "getMobileTypeName", int.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                String net = (String)param.getResult();
                if (MainModule.mPrefs.getBoolean("system_4gtolte")) {
                    if ("4G".equals(net)) param.setResult("LTE");
                    else if ("4G+".equals(net)) param.setResult("LTE+");
                }
                else {
                    String mobileType = MainModule.mPrefs.getString("system_statusbar_mobile_showname", "");
                    param.setResult(mobileType);
                }
            }
        });
    }
    public static void DisableFakeClockAnimHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "setMNCSwitching", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean mNCSwitching = (boolean)param.getArgs()[0];
                if (!mNCSwitching) {
                    Object mFakeClock = XposedHelpers.getObjectField(param.getThisObject(), "fakeStatusBarClockController");
                    XposedHelpers.setObjectField(mFakeClock, "ncSwitching", true);
                }
            }
        });
    }

    public static void HideSignalExtraIconsByFlowHook(PackageReadyParam lpparam) {
        boolean hideRoaming = MainModule.mPrefs.getBoolean("system_statusbaricons_roaming");
        boolean hideIndicator = MainModule.mPrefs.getBoolean("system_networkindicator_mobile");
        boolean hideMobileType = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_show_never");
        Class<?> MiuiCellularIconVM = findClass("com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.MiuiCellularIconVM", lpparam.getClassLoader());
        ModuleHelper.hookAllConstructors(MiuiCellularIconVM, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object activityFlow = ModuleHelper.createReadonlyFlowWithInitValue(Boolean.FALSE, lpparam.getClassLoader());
                if (hideRoaming) {
                    XposedHelpers.setObjectField(param.getThisObject(), "smallRoamVisible", activityFlow);
                    XposedHelpers.setObjectField(param.getThisObject(), "mobileRoamVisible", activityFlow);
                }
                if (hideIndicator) {
                    XposedHelpers.setObjectField(param.getThisObject(), "inOutVisible", activityFlow);
                }
                if (hideMobileType) {
                    XposedHelpers.setObjectField(param.getThisObject(), "mobileTypeVisible", activityFlow);
                }
            }
        });
    }

    public static void HideNetworkIndicatorOfWifi(PackageReadyParam lpparam) {
        Class<?> WifiRepositoryImpl = findClass("com.android.systemui.statusbar.pipeline.wifi.data.repository.prod.WifiRepositoryImpl", lpparam.getClassLoader());
        ModuleHelper.hookAllConstructors(WifiRepositoryImpl, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object noActivity = XposedHelpers.getStaticObjectField(WifiRepositoryImpl, "ACTIVITY_DEFAULT");
                Object activityFlow = ModuleHelper.createReadonlyFlowWithInitValue(noActivity, lpparam.getClassLoader());
                XposedHelpers.setObjectField(param.getThisObject(), "wifiActivity", activityFlow);
            }
        });
    }
    public static void DisplayWifiStandardHook(PackageReadyParam lpparam) {
        int opt = MainModule.mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1);
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.pipeline.wifi.ui.viewmodel.WifiViewModel", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (opt == 3) {
                    Object wifiStandardFlow = ModuleHelper.createReadonlyFlowWithInitValue(Integer.valueOf(0), lpparam.getClassLoader());
                    XposedHelpers.setObjectField(param.getThisObject(), "wifiStandard", wifiStandardFlow);
                }
                else if (opt == 2) {
                    Object originalFlow = XposedHelpers.getObjectField(param.getThisObject(), "wifiStandard");
                    Object currentValue = XposedHelpers.callMethod(originalFlow, "getValue");
                    Object wifiStandardFlow = ModuleHelper.createReadonlyFlowWithInitValue(currentValue, lpparam.getClassLoader());
                    XposedHelpers.setObjectField(param.getThisObject(), "wifiStandard", wifiStandardFlow);
                }
            }
        });
        if (opt == 2) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.pipeline.wifi.ui.viewmodel.WifiViewModel$wifiStandard$1", lpparam.getClassLoader(), "invokeSuspend", Object.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object wifiViewModel = XposedHelpers.getSurroundingThis(param.getThisObject());
                    Object wifiStandardFlow = XposedHelpers.getObjectField(wifiViewModel, "wifiStandard");
                    Object stateFlow = ModuleHelper.getMutableFlowOfReadonlyFlow(wifiStandardFlow);
                    XposedHelpers.callMethod(stateFlow, "setValue", param.getResult());
                }
            });
        }
    }

    public static void MobileTypeSingleHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.pipeline.mobile.ui.binder.MiuiMobileIconBinder", lpparam.getClassLoader(), "updateMobileTypeLayoutParams", HookerClassHelper.DO_NOTHING);
        MethodHook initHook = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                ViewGroup mobileView = (ViewGroup) param.getArgs()[0];
                String slot = (String) XposedHelpers.getObjectField(mobileView, "slot");
                if (!"mobile".equals(slot)) {
                    return;
                }
                int mobileTypeId = mobileView.getResources().getIdentifier("mobile_type_single", "id", "com.android.systemui");
                TextView mMobileTypeSingle = mobileView.findViewById(mobileTypeId);
                LinearLayout mMobileGroup = (LinearLayout) mMobileTypeSingle.getParent();
                if (!MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single_atleft")) {
                    mMobileGroup.removeView(mMobileTypeSingle);
                    mMobileGroup.addView(mMobileTypeSingle);
                }
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mMobileTypeSingle.getLayoutParams();
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_leftmargin", 4);
                mlp.leftMargin = (int) Helpers.dp2px(leftMargin * 0.5f);
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_rightmargin", 0);
                if (rightMargin > 0) {
                    mlp.rightMargin = (int) Helpers.dp2px(rightMargin * 0.5f);
                }
                int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_verticaloffset", 8);
                if (verticalOffset != 8) {
                    mlp.topMargin = (int) Helpers.dp2px((verticalOffset - 8) * 0.5f);
                }
                mMobileTypeSingle.setLayoutParams(mlp);
                int fontSize = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_fontsize", 27);
                mMobileTypeSingle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f);
                if (MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single_bold")) {
                    mMobileTypeSingle.setTypeface(Typeface.DEFAULT_BOLD);
                }
                int leftContainerId = mobileView.getResources().getIdentifier("mobile_container_left", "id", "com.android.systemui");
                ViewGroup mMobileContainerLeft = mobileView.findViewById(leftContainerId);
                mMobileGroup.removeView(mMobileContainerLeft);
                FrameLayout fakeGoneGroup = new FrameLayout(mobileView.getContext());
                mMobileGroup.addView(fakeGoneGroup);
                fakeGoneGroup.setVisibility(View.GONE);
                fakeGoneGroup.addView(mMobileContainerLeft);
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.pipeline.mobile.ui.binder.MiuiMobileIconBinder", lpparam.getClassLoader(), "bind", initHook);
        Class<?> MobileIconBind1 = findClass("com.android.systemui.statusbar.pipeline.mobile.ui.binder.MiuiMobileIconBinder$bind$1", lpparam.getClassLoader());
        Field tintLightColorFlow = XposedHelpers.findFieldIfExists(MobileIconBind1, "$tintLightColorFlow");
        if (tintLightColorFlow == null) {
            onMobileIconDarkChanged(lpparam);
        }
    }

    private static float blurCollapsed = 0.0f;
    private static float blurExpanded = 0.0f;

    public static void BlurVolumeDialogBackgroundHook(ClassLoader classLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "updateDialogWindowH", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Window mWindow = (Window) XposedHelpers.getObjectField(param.getThisObject(), "mWindow");
                mWindow.setDimAmount(0.0f);
                boolean mExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mExpanded");
                float blurRatio = blurCollapsed;
                boolean isVisible = (boolean) param.getArgs()[0];
                if (mExpanded && !isVisible) {
                    blurRatio = blurExpanded;
                }
                if (!mExpanded && blurCollapsed > 0.001f) {
                    mWindow.clearFlags(8);
                }
                if (mExpanded) {
                    XposedHelpers.callMethod(param.getThisObject(), "startBlurAnim", 0f, blurRatio, 0);
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "showH", int.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (blurCollapsed > 0.001f) {
                    Window mWindow = (Window) XposedHelpers.getObjectField(param.getThisObject(), "mWindow");
                    mWindow.clearFlags(8);
                    XposedHelpers.callMethod(param.getThisObject(), "startBlurAnim", 0f, blurCollapsed, 0);
                }
            }
        });
        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "initDialog", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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

    public static void initControlCenter(ClassLoader pluginLoader) {
        if (MainModule.mPrefs.getBoolean("system_nosilentvibrate")) {
            ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader, "vibrateH", HookerClassHelper.DO_NOTHING);
        }
        if (MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) > 0) {
            BlurVolumeDialogBackgroundHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_volumetimer")) {
            VolumeTimerValuesRes(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_volume_hide_ringermode")) {
            HideRingerModeLayoutHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_tile_roundedrect")) {
            CCTileCornerHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_volume_showpct")) {
            ShowVolumePctHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_showpct")) {
            CCSliderBrightnessPctHook(pluginLoader);
        }
        boolean customCCGrid = MainModule.mPrefs.getInt("system_ccgridcolumns", 4) > 4;
        if (customCCGrid) {
            SystemCCGridHookLoader(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_hide_edit")
            || MainModule.mPrefs.getBoolean("system_cc_hide_profile_monitoring")
        ) {
            CCHideEditButtonHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_btandtorch_ascard")) {
            CCBluetoothAsCardHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_tile_enabled_color")) {
            CCTileColorHook();
        }
        if (MainModule.mPrefs.getBoolean("system_cc_card_enabled_color")) {
            CCCardColorHook(pluginLoader);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_slider_color_enable")) {
            CCSliderColorHook();
        }
        if (MainModule.mPrefs.getBoolean("system_cc_collapse_after_clicked")) {
            CollpaseCCAfterStopSlideHook(pluginLoader);
        }
    }

    public static void CCCarrierAndStepsHook(PackageReadyParam lpparam) {
        Class<?> MiuiCarrierTextLayoutClass = findClassIfExists("com.android.systemui.controlcenter.shade.MiuiCarrierTextLayout", lpparam.getClassLoader());
        boolean newCarrierText = MiuiCarrierTextLayoutClass != null;
        Class<?> CarrierTextClass = findClass("com.android.keyguard.CarrierText", lpparam.getClassLoader());
        Field mCallback = XposedHelpers.findField(CarrierTextClass, "mCarrierTextCallback");
        boolean showSteps = MainModule.mPrefs.getBoolean("system_cc_show_stepcount");
        boolean hideOperator = MainModule.mPrefs.getBoolean("system_qs_hideoperator");
        boolean hideDelimiter = MainModule.mPrefs.getBoolean("system_cc_hideoperator_delimiter");
        if (!newCarrierText || (hideOperator || showSteps)) {
            ModuleHelper.hookAllMethods(mCallback.getType(), "onCarrierTextChanged", new MethodHook() {
                int ccCarrierViewId = 0;
                int ccSubCarrierViewId = 0;
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    if (newCarrierText && param.getArgs().length == 2) return;
                    int textIndex = newCarrierText ? 2 : 0;
                    String text = (String) param.getArgs()[textIndex];
                    TextView mCarrierText;
                    if (newCarrierText) {
                        mCarrierText = (TextView) XposedHelpers.getSurroundingThis(param.getThisObject());
                    }
                    else {
                        mCarrierText = (TextView) XposedHelpers.getOuterThis(param.getThisObject());
                    }
                    int viewId = mCarrierText.getId();
                    if (viewId < 0) return;
                    if (ccCarrierViewId == 0) {
                        ccCarrierViewId = mCarrierText.getResources().getIdentifier("normal_control_center_carrier_view", "id", "com.android.systemui");
                    }
                    if (!newCarrierText) {
                        if (ccCarrierViewId > 0 && ccCarrierViewId == viewId) {
                            boolean hideOperator = MainModule.mPrefs.getBoolean("system_qs_hideoperator");
                            boolean hideDelimiter = MainModule.mPrefs.getBoolean("system_cc_hideoperator_delimiter");
                            String carrierString = text;
                            if (hideOperator) {
                                carrierString = "";
                            }
                            else if (hideDelimiter) {
                                carrierString = text.replace(" | ", " ");
                            }
                            if (showSteps) {
                                ModuleHelper.setViewInfo(mCarrierText, "stepsTpl", "%s " + carrierString);
                                param.getArgs()[textIndex] = StepCounterController.getStepsShowValue(mCarrierText);
                            }
                            else {
                                param.getArgs()[textIndex] = carrierString;
                            }
                        }
                    }
                    else {
                        if (ccSubCarrierViewId == 0) {
                            ccSubCarrierViewId = mCarrierText.getResources().getIdentifier("normal_control_center_carrier_second_view", "id", "com.android.systemui");
                        }
                        if ((ccCarrierViewId > 0 && ccCarrierViewId == viewId)
                            || (ccSubCarrierViewId > 0 && ccSubCarrierViewId == viewId)
                        ) {
                            boolean hideOperator = MainModule.mPrefs.getBoolean("system_qs_hideoperator");
                            String carrierString = text;
                            if (hideOperator) {
                                carrierString = "";
                            }
                            if (showSteps) {
                                int slotNum = (int) param.getArgs()[0];
                                boolean prefixWithSteps = true;
                                if (slotNum == 2 && ccCarrierViewId == viewId) {
                                    prefixWithSteps = false;
                                }
                                if (prefixWithSteps) {
                                    ModuleHelper.setViewInfo(mCarrierText, "stepsTpl", "%s " + carrierString);
                                    param.getArgs()[textIndex] = StepCounterController.getStepsShowValue(mCarrierText);
                                }
                            }
                            else if (hideOperator) {
                                param.getArgs()[textIndex] = carrierString;
                            }
                        }
                    }
                }
            });
        }
        if (showSteps) {
            ModuleHelper.findAndHookMethod("com.android.keyguard.CarrierText", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
                int ccCarrierViewId = 0;
                int ccSubCarrierViewId = 0;
                boolean inited = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    TextView mCarrierText = (TextView) param.getThisObject();
                    int viewId = mCarrierText.getId();
                    if (viewId < 0) return;
                    if (!inited) {
                        inited = true;
                        StepCounterController.initContext(mCarrierText.getContext());
                    }
                    if (ccCarrierViewId == 0) {
                        ccCarrierViewId = mCarrierText.getResources().getIdentifier("normal_control_center_carrier_view", "id", "com.android.systemui");
                    }
                    if (ccSubCarrierViewId == 0 && newCarrierText) {
                        ccSubCarrierViewId = mCarrierText.getResources().getIdentifier("normal_control_center_carrier_second_view", "id", "com.android.systemui");
                    }
                    if ((ccCarrierViewId > 0 && ccCarrierViewId == viewId)
                        || (ccSubCarrierViewId > 0 && ccSubCarrierViewId == viewId)
                    ) {
                        StepCounterController.addStepView(mCarrierText);
                    }
                }
            });
        }
        if (newCarrierText) {
            if (hideOperator) {
                ModuleHelper.findAndHookMethod("com.android.keyguard.CarrierText", lpparam.getClassLoader(), "updateHDDrawable", int.class, new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) throws Throwable {
                        param.getArgs()[0] = 0;
                    }
                });
            }
            else if (hideDelimiter) {
                ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.shade.MiuiCarrierTextLayout", lpparam.getClassLoader(), "onConfigChanged", Configuration.class, new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) throws Throwable {
                        ImageView carrierSeparatorView = (ImageView) XposedHelpers.getObjectField(param.getThisObject(), "carrierSeparatorView");
                        if (carrierSeparatorView != null) {
                            carrierSeparatorView.setImageResource(0);
                            param.returnAndSkip(null);
                        }
                    }
                });
            }
        }
    }

    public static void ControlCenterPluginHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shared.plugins.PluginInstance$PluginFactory", lpparam.getClassLoader(), "createPlugin", new MethodHook() {
            boolean isHooked = false;
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(param.getThisObject(), "mComponentName");
                if ("miui.systemui.plugin".equals(componentName.getPackageName()) && !isHooked) {
                    isHooked = true;
                    Object mClassLoaderFactory = XposedHelpers.getObjectField(param.getThisObject(), "mClassLoaderFactory");
                    ClassLoader pluginLoader = (ClassLoader) XposedHelpers.callMethod(mClassLoaderFactory, "get");
                    initControlCenter(pluginLoader);
                }
            }
        });
    }

    private static float iconScaleRatio = 1f;
    public static void SystemCCGridHookLoader(ClassLoader pluginLoader) {
        int cols = MainModule.mPrefs.getInt("system_ccgridcolumns", 4);
        iconScaleRatio = 4f / cols;
        MethodHook updateIconSizeHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (!XposedHelpers.getBooleanField(param.getThisObject(), "card")) {
                    ImageView iconFrame = (ImageView) XposedHelpers.getObjectField(param.getThisObject(), "icon");
                    int iconSize = (int) Helpers.dp2px(68f * iconScaleRatio);
                    iconFrame.getLayoutParams().width = iconSize;
                    iconFrame.getLayoutParams().height = iconSize;
                    param.returnAndSkip(null);
                }
            }
        };
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "updateIconSize", updateIconSizeHook);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemView", pluginLoader, "updateContainerHeight", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int iconSize = (int) Helpers.dp2px(85f * iconScaleRatio + 1);
                XposedHelpers.setObjectField(param.getThisObject(), "containerHeight", iconSize);
            }
        });


        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelController", pluginLoader, "setUseSeparatedPanels", Boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.getArgs()[0] == null) {
                    param.returnAndSkip(null);
                    return;
                }
                Boolean bool = (Boolean) param.getArgs()[0];
                Object oldVal = XposedHelpers.getObjectField(param.getThisObject(), "useSeparatedPanels");
                if (bool.equals(oldVal)) {
                    param.returnAndSkip(null);
                    return;
                }
                XposedHelpers.setObjectField(param.getThisObject(), "useSeparatedPanels", bool);
                LinearLayout horizontalMainPanel = (LinearLayout) XposedHelpers.getObjectField(param.getThisObject(), "mainPanelContainer");
                ViewGroup leftMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel");
                horizontalMainPanel.removeView(leftMainPanel);
                if (!bool) {
                    horizontalMainPanel.addView(leftMainPanel);
                    ViewGroup.LayoutParams layoutParams = leftMainPanel.getLayoutParams();
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(0);
                    horizontalMainPanel.setOrientation(LinearLayout.VERTICAL);
                }
                else {
                    horizontalMainPanel.addView(leftMainPanel, 0);
                    int marginId = horizontalMainPanel.getResources().getIdentifier("control_center_horizontal_margin_center", "dimen", "miui.systemui.plugin");
                    int marginEnd = horizontalMainPanel.getResources().getDimensionPixelSize(marginId);
                    XposedHelpers.setObjectField(param.getThisObject(), "panelMargin", marginEnd);
                    ViewGroup.LayoutParams layoutParams = leftMainPanel.getLayoutParams();
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(marginEnd);
                    horizontalMainPanel.setOrientation(LinearLayout.HORIZONTAL);
                }
                param.returnAndSkip(null);
            }
        });

        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelContentDistributor", pluginLoader, "distributePanels", boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean horizontal = (boolean) param.getArgs()[0];
                if (!horizontal && XposedHelpers.getBooleanField(param.getThisObject(), "inited")) {
                    ArrayList<?> rightPanelContent = (ArrayList<?>) XposedHelpers.getObjectField(param.getThisObject(), "rightPanelContent");
                    ArrayList<Object> leftPanelContent = (ArrayList<Object>) XposedHelpers.getObjectField(param.getThisObject(), "leftPanelContent");
                    int size = rightPanelContent.size();
                    for (int i = size - 1;i >= 0;i--) {
                        Object controller = rightPanelContent.get(i);
                        String className = controller.getClass().getCanonicalName();
                        if (className.contains("EditButtonController")
                            || className.contains("SecurityFooterController")
                            || className.contains("QSListController")
                        ) {
                            rightPanelContent.remove(i);
                            leftPanelContent.add(controller);
                        }
                        else if (className.contains("FooterSpaceController")) {
                            rightPanelContent.remove(i);
                        }
                    }
                    leftPanelContent.sort(new Comparator<Object>() {
                        @Override
                        public int compare(Object lhs, Object rhs) {
                            int leftPriority = (int) XposedHelpers.callMethod(lhs, "getPriority");
                            int rightPriority = (int) XposedHelpers.callMethod(rhs, "getPriority");
                            return leftPriority - rightPriority;
                        }
                    });
                }
            }
        });

        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelController", pluginLoader, "updatePanelSize", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Boolean useSeparatedPanels = (Boolean) XposedHelpers.getObjectField(param.getThisObject(), "useSeparatedPanels");
                if (!useSeparatedPanels) {
                    ViewGroup leftMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel");
                    ViewGroup rightMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "rightMainPanel");
                    int panelWidth = XposedHelpers.getIntField(param.getThisObject(), "panelWidth");
                    ViewGroup.LayoutParams leftParams = leftMainPanel.getLayoutParams();
                    ViewGroup.LayoutParams rightParams = rightMainPanel.getLayoutParams();
                    if (leftParams != null) {
                        leftParams.width = panelWidth;
                        leftParams.height = -2;
                    }
                    if (rightParams != null) {
                        rightParams.width = panelWidth;
                        rightParams.height = -2;
                    }
                    param.returnAndSkip(null);
                }
            }
        });

        Class<?> MainPanelAdapter = findClass("miui.systemui.controlcenter.panel.main.recyclerview.MainPanelAdapter", pluginLoader);

        MethodHook spanSizeHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object adapter = XposedHelpers.getSurroundingThis(param.getThisObject());
                boolean leftAdapter = XposedHelpers.getAdditionalInstanceField(adapter, "leftAdapter") != null;
                if (leftAdapter) {
                    Object companion = XposedHelpers.getStaticObjectField(MainPanelAdapter, "Companion");
                    Object contentMap = XposedHelpers.getObjectField(adapter, "contentMap");
                    Object panelItem = XposedHelpers.callMethod(companion, "getItem", contentMap, param.getArgs()[0]);
                    if (panelItem == null) {
                        param.returnAndSkip(cols);
                    }
                    else {
                        param.returnAndSkip(XposedHelpers.callMethod(panelItem, "getSpanSize"));
                    }
                }
            }
        };

        ModuleHelper.hookAllConstructors("miui.systemui.controlcenter.panel.main.MainPanelContentDistributor", pluginLoader, new MethodHook() {
            boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object leftAdapter = XposedHelpers.getObjectField(param.getThisObject(), "leftAdapter");
                XposedHelpers.setAdditionalInstanceField(leftAdapter, "leftAdapter", true);
                Object layoutManager = XposedHelpers.getObjectField(leftAdapter, "layoutManager");
                XposedHelpers.callMethod(layoutManager, "setSpanCount", cols);
                Object spanSizeLookup = XposedHelpers.callMethod(layoutManager, "getSpanSizeLookup");
                if (!isHooked) {
                    isHooked = true;
                    ModuleHelper.findAndHookMethod(spanSizeLookup.getClass(), "getSpanSize", int.class, spanSizeHook);
                }
            }
        });

        MethodHook columnsReplaceHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.returnAndSkip(cols);
            }
        };
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.header.HeaderSpaceController", pluginLoader, "getSpanSize", columnsReplaceHook);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.security.SecurityFooterController", pluginLoader, "getSpanSize", columnsReplaceHook);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.qs.EditButtonController", pluginLoader, "getSpanSize", columnsReplaceHook);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.qs.QSListController$EditModeDividerTextItem", pluginLoader, "getSpanSize", columnsReplaceHook);

        // handle secondary panel show
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "updateVisibility", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Class<?> CommonUtils = findClass("miui.systemui.util.CommonUtils", pluginLoader);
                Object INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE");
                Object mContext = XposedHelpers.callMethod(param.getThisObject(), "getContext");
                boolean verticalMode = (boolean) XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", mContext);
                if (verticalMode) {
                    ViewGroup leftMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel");
                    leftMainPanel.setVisibility((int) param.getArgs()[0]);
                }
            }
        });
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "forceToShow", Object.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ViewGroup leftMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel");
                leftMainPanel.setAlpha(1.0f);
            }
        });
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "onAnimUpdate", boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Class<?> CommonUtils = findClass("miui.systemui.util.CommonUtils", pluginLoader);
                Object INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE");
                Object mContext = XposedHelpers.callMethod(param.getThisObject(), "getContext");
                boolean verticalMode = (boolean) XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", mContext);
                if (verticalMode) {
                    ViewGroup leftMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel");
                    ViewGroup rightMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "rightMainPanel");
                    float alpha = rightMainPanel.getAlpha();
                    leftMainPanel.setAlpha(alpha);
                }
            }
        });
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "onConfigurationChanged", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int i = (int) param.getArgs()[0];
                if ((i & 128) != 0) {
                    Class<?> CommonUtils = findClass("miui.systemui.util.CommonUtils", pluginLoader);
                    Object INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE");
                    Object mContext = XposedHelpers.callMethod(param.getThisObject(), "getContext");
                    boolean verticalMode = (boolean) XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", mContext);
                    ViewGroup leftMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel");
                    ViewGroup rightMainPanel = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "rightMainPanel");
                    if (verticalMode) {
                        leftMainPanel.setAlpha(rightMainPanel.getAlpha());
                        leftMainPanel.setVisibility(rightMainPanel.getVisibility());
                    }
                    else {
                        leftMainPanel.setAlpha(1.0f);
                        leftMainPanel.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public static void CCHideEditButtonHook(ClassLoader pluginLoader) {
        ModuleHelper.hookAllConstructors("miui.systemui.controlcenter.panel.main.MainPanelContentDistributor", pluginLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean hideEdit = MainModule.mPrefs.getBoolean("system_cc_hide_edit");
                boolean hideSecurity = MainModule.mPrefs.getBoolean("system_cc_hide_profile_monitoring");
                ArrayList<Object> childControllers = (ArrayList<Object>) XposedHelpers.getObjectField(param.getThisObject(), "childControllers");
                int size = childControllers.size();
                for (int i = size - 1;i >= 0;i--) {
                    Object controller = childControllers.get(i);
                    String className = controller.getClass().getCanonicalName();
                    if (
                        (hideEdit && className.contains("EditButtonController"))
                        || (hideSecurity && className.contains("SecurityFooterController"))
                    ) {
                        childControllers.remove(i);
                    }
                }
            }
        });
    }
    public static void CCBluetoothAsCardHook(ClassLoader pluginLoader) {
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.QSController", pluginLoader, "getCardStyleTileSpecs", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.returnAndSkip(List.of("wifi", "cell", "bt", "flashlight"));
            }
        });
    }
    public static void CCTileColorHook() {
        int customColor = MainModule.mPrefs.getInt("system_cc_tile_enabled_color_custom", 0xff277af7);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_enabled_color", customColor);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_warning_color", customColor);

        int iconColor = MainModule.mPrefs.getInt("system_cc_tile_enabled_iconcolor_custom", 0xffffffff);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_icon_enabled_color", iconColor);
    }
    public static void CCCardColorHook(ClassLoader pluginLoader) {
        int customColor = MainModule.mPrefs.getInt("system_cc_card_enabled_color_custom", 0xff3482ff);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_cellular_color", customColor);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_enabled_color", customColor);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_flashlight_color", customColor);

        int primaryColor = MainModule.mPrefs.getInt("system_cc_card_enabled_primary_textcolor", 0xffffffff);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_primary_text_enabled_color", primaryColor);
        int secondaryColor = MainModule.mPrefs.getInt("system_cc_card_enabled_secondary_textcolor", 0x80ffffff);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_secondary_text_enabled_color", secondaryColor);

        int iconColor = MainModule.mPrefs.getInt("system_cc_card_enabled_iconcolor_custom", 0xffffffff);
        if (iconColor != 0xffffffff) {
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSCardItemIconView", pluginLoader, "updateResources", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    XposedHelpers.setObjectField(param.getThisObject(), "iconColor", iconColor);
                }
            });
        }
    }
    public static void CCSliderColorHook() {
        int customColor = MainModule.mPrefs.getInt("system_cc_slider_progress_color", 0xffffffff);
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "toggle_slider_progress_color", customColor);
        int[] blendColors = {customColor, 3};
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "integer-array", "toggle_slider_progress_blend_colors", blendColors);

        int iconColor = MainModule.mPrefs.getInt("system_cc_slider_icon_color", 0xff959595);
        if (iconColor != 0xff959595) {
            MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "toggle_slider_icon_color", iconColor);
            int[] iconBlendColors = {iconColor, 3};
            MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "integer-array", "toggle_slider_icon_blend_colors", iconBlendColors);
        }
    }

    public static void CollpaseCCAfterStopSlideHook(ClassLoader pluginLoader) {
        Class<?> ControlCenterPluginInstance = findClass("miui.systemui.controlcenter.dagger.ControlCenterPluginInstance", pluginLoader);
        Method provideQSHost = ModuleHelper.findFirstMethodByName(ControlCenterPluginInstance, "provideQSHost");
        Class<?> SliderClass = findClass("miui.systemui.controlcenter.panel.main.brightness.BrightnessSliderController", pluginLoader);
        Field seekBarListener = XposedHelpers.findField(SliderClass, "seekBarListener");
        MethodHook collpaseHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mHost = provideQSHost.invoke(null, new Object[] {null});
                XposedHelpers.callMethod(mHost, "collapsePanels");
            }
        };
        ModuleHelper.hookAllMethods(seekBarListener.getType(), "onStopTrackingTouch", collpaseHook);
        SliderClass = findClass("miui.systemui.controlcenter.panel.main.volume.VolumeSliderController", pluginLoader);
        seekBarListener = XposedHelpers.findField(SliderClass, "seekBarListener");
        ModuleHelper.hookAllMethods(seekBarListener.getType(), "onStopTrackingTouch", collpaseHook);
    }

    public static void VolumeTimerValuesRes(ClassLoader pluginLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "initTimerString", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
            protected void before(final MethodHookParam param) throws Throwable {
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
            protected void before(final MethodHookParam param) throws Throwable {
                prevSeg = XposedHelpers.getIntField(param.getThisObject(), "mCurrentSegment");
                if (prevSeg < 3 || (prevSeg == 3 && XposedHelpers.getIntField(param.getThisObject(), "mDeterminedSegment") == 3)) {
                    XposedHelpers.setIntField(param.getThisObject(), "mCurrentSegment", 0);
                }
            }
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                XposedHelpers.setIntField(param.getThisObject(), "mCurrentSegment", prevSeg);
            }
        };

        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "updateDrawables", segHook);
    }

    public static void HideRingerModeLayoutHook(ClassLoader pluginLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogView", pluginLoader, "updateFooterVisibility", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean show = (boolean) param.getArgs()[0];
                if (show) {
                    boolean isExpanded = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isExpanded");
                    if (isExpanded) {
                        return;
                    }
                    View ringerModeLayout = (View) XposedHelpers.getObjectField(param.getThisObject(), "mRingerModeLayout");
                    ringerModeLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void CCTileCornerHook(ClassLoader pluginLoader) {
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "getCornerRadius", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                float radius = 20 * iconScaleRatio;
                param.returnAndSkip(Helpers.dp2px(radius));
            }
        });
        MethodHook radiusHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object drawable = param.getResult();
                GradientDrawable gradientDrawable = drawable instanceof GradientDrawable ? (GradientDrawable) drawable : null;
                if (gradientDrawable != null) {
                    float radius = 20 * iconScaleRatio;
                    gradientDrawable.setCornerRadius(Helpers.dp2px(radius));
                }
            }
        };
        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "getDisabledBackgroundDrawable", radiusHook);
        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "getActiveBackgroundDrawable", radiusHook);
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
    private static float nextBrightNess = -999;

    public static void StatusBarGesturesHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "setExpandedHeightInternal", float.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                float mExpandedFraction = XposedHelpers.getFloatField(param.getThisObject(), "mExpandedFraction");
                if (mExpandedFraction > 0.33f) {
                    currentTouchTime = 0;
                    currentTouchX = 0;
                    currentDownTime = 0;
                    currentDownX = 0;
                }
            }
        });

        MethodHook hook = new MethodHook() {
            Object mBrightnessController;
            private int sbHeight = -1;
            @Override
            @SuppressLint("SetTextI18n")
            protected void before(final MethodHookParam param) throws Throwable {
                String clsName = param.getThisObject().getClass().getSimpleName();
                boolean isInControlCenter = "ControlCenterWindowViewImpl".equals(clsName);
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
                Context mContext = ((View)param.getThisObject()).getContext();
                Resources res = mContext.getResources();
                if (sbHeight == -1) {
                    sbHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height_default", "dimen", "android"));
                }
                MotionEvent event = (MotionEvent)param.getArgs()[0];
                Object mDisplayManager;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        tapStartX = event.getX();
                        tapStartY = event.getY();
                        isSlidingStart = !isInControlCenter || tapStartY <= sbHeight;
                        tapStartPointers = 1;
                        if (mBrightnessController == null) {
                            if (isInControlCenter) {
                                Class<?> ControlCenterPluginInstance = findClass("miui.systemui.controlcenter.dagger.ControlCenterPluginInstance", param.getThisObject().getClass().getClassLoader());
                                mBrightnessController = XposedHelpers.callStaticMethod(ControlCenterPluginInstance, "provideBrightnessControllerBase");
                            }
                            else {
                                Object mControlCenterController = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.controlcenter.policy.ControlCenterControllerImpl");
                                Object lazyPluginDependenciesManager = ModuleHelper.getObjectFieldByPath(mControlCenterController, "controlCenter.pluginDependenciesManager");
                                Object mControlCenterPluginDependenciesManager = XposedHelpers.callMethod(lazyPluginDependenciesManager, "get");
                                mBrightnessController = XposedHelpers.getObjectField(mControlCenterPluginDependenciesManager, "brightnessController");
                            }
                        }
                        mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
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
                        nextBrightNess = -999;
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
                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 80F) {
                            currentTouchTime = 0L;
                            currentTouchX = 0F;
                            int screenWidth = res.getDisplayMetrics().widthPixels;
                            String actionKey = "system_statusbarcontrols_dt";
                            if (mTouchX * 5 < screenWidth) {
                                actionKey = "system_statusbarcontrols_dt_left";
                            }
                            else if (mTouchX > screenWidth * 0.8) {
                                actionKey = "system_statusbarcontrols_dt_right";
                            }
                            GlobalActions.handleAction(mContext, actionKey);
                        }
                        else if ((mTouchTime - currentDownTime > 600 && mTouchTime - currentDownTime < 4000)
                            && Math.abs(mTouchX - currentDownX) < 80F) {
                            if (MainModule.mPrefs.getBoolean("system_statusbarcontrols_longpress_vibrate")) {
                                boolean ignoreOff = MainModule.mPrefs.getBoolean("system_statusbarcontrols_longpress_vibrate_ignoreoff");
                                Helpers.performStrongVibration(mContext, ignoreOff);
                            }
                            GlobalActions.handleAction(mContext, "system_statusbarcontrols_longpress");
                        }
                        if (nextBrightNess > -10) {
                            mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
                            int displayId = XposedHelpers.getIntField(mBrightnessController, "mDisplayId");
                            XposedHelpers.callMethod(mDisplayManager, "setBrightness", displayId, nextBrightNess);
                            nextBrightNess = -999;
                        }
                        currentDownTime = 0L;
                        currentDownX = 0;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isSlidingStart = false;
                        isSliding = false;
                        nextBrightNess = -999;
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
                            mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
                            int displayId = XposedHelpers.getIntField(mBrightnessController, "mDisplayId");
                            XposedHelpers.callMethod(mDisplayManager, "setTemporaryBrightness", displayId, nextLevel);
                            nextBrightNess = nextLevel;
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
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.getClassLoader(), "onInterceptTouchEvent", MotionEvent.class, hook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.getClassLoader(), "onTouchEvent", MotionEvent.class, hook);
        ModuleHelper.findAndHookMethod("com.android.systemui.shared.plugins.PluginInstance$PluginFactory", lpparam.getClassLoader(), "createPlugin", new MethodHook() {
            boolean isHooked = false;
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(param.getThisObject(), "mComponentName");
                if ("miui.systemui.plugin".equals(componentName.getPackageName()) && !isHooked) {
                    isHooked = true;
                    Object mClassLoaderFactory = XposedHelpers.getObjectField(param.getThisObject(), "mClassLoaderFactory");
                    ClassLoader pluginLoader = (ClassLoader) XposedHelpers.callMethod(mClassLoaderFactory, "get");
                    ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.ControlCenterWindowViewImpl", pluginLoader, "handleMotionEvent", MotionEvent.class, boolean.class, hook);
                }
            }
        });
    }

    public static void HorizMarginHook(PackageReadyParam lpparam) {
        MethodHook horizHook = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_left", 16);
                float marginLeft = Helpers.dp2px(leftMargin);
                leftMargin = (int) marginLeft;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_right", 16);
                float marginRight = Helpers.dp2px(rightMargin);
                rightMargin = (int) marginRight;
                Insets rawInsets = (Insets) param.getResult();
                param.setResult(Insets.of(leftMargin, rawInsets.top, rightMargin, rawInsets.bottom));
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarContentInsetsProvider", lpparam.getClassLoader(), "getStatusBarContentInsetsForCurrentRotation", horizHook);
    }

    public static void HideSignalExtraIconsByOperatorHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.miui.interfaces.IOperatorCustomizedPolicy$OperatorConfig", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_volte")) {
                    XposedHelpers.setBooleanField(param.getThisObject(), "hideVolte", true);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single")) {
                    XposedHelpers.setBooleanField(param.getThisObject(), "showMobileDataTypeSingle", true);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_vowifi")) {
                    XposedHelpers.setBooleanField(param.getThisObject(), "hideVowifi", true);
                }
            }
        });
    }

    public static void HideWifiIconsHook(PackageReadyParam lpparam) {
        Class<?> HiddenWifiIconClass = findClass("com.android.systemui.statusbar.pipeline.wifi.ui.model.WifiIcon$Hidden", lpparam.getClassLoader());
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.pipeline.wifi.ui.viewmodel.WifiViewModel", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object hiddenState = XposedHelpers.getStaticObjectField(HiddenWifiIconClass, "INSTANCE");
                Object wifiIconFlow = ModuleHelper.createReadonlyFlowWithInitValue(hiddenState, lpparam.getClassLoader());
                XposedHelpers.setObjectField(param.getThisObject(), "wifiIcon", wifiIconFlow);
            }
        });
    }

    public static void HideSignalIconsHook (PackageReadyParam lpparam) {
        Class<?> MiuiCellularIconVM = findClass("com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.MiuiCellularIconVM", lpparam.getClassLoader());
        ModuleHelper.hookAllConstructors(MiuiCellularIconVM, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_signal") && !MainModule.mPrefs.getBoolean("system_statusbaricons_signal_wificonnected")) {
                    Object finalVisibleFlow = ModuleHelper.createReadonlyFlowWithInitValue(Boolean.FALSE, lpparam.getClassLoader());
                    XposedHelpers.setObjectField(param.getThisObject(), "isVisible", finalVisibleFlow);
                    return;
                }

                boolean dualSignal = MainModule.mPrefs.getBoolean("system_statusbar_dualsimin2rows");
                boolean hideNoDataSimPref = MainModule.mPrefs.getBoolean("system_statusbaricons_sim_nodata");
                boolean hideNoDataSim = hideNoDataSimPref || dualSignal;
                boolean hideWithWifi = MainModule.mPrefs.getBoolean("system_statusbaricons_signal") && MainModule.mPrefs.getBoolean("system_statusbaricons_signal_wificonnected");
                if (!hideWithWifi && !hideNoDataSim) return;
                int subId = XposedHelpers.getIntField(param.getArgs()[2], "subId");
                Object isVisibleFlow = XposedHelpers.getObjectField(param.getThisObject(), "isVisible");
                final boolean[] visibleStates = {true, false};
                final int[] subIds = {-1};
                visibleStates[0] = ((Boolean) XposedHelpers.callMethod(isVisibleFlow, "getValue")).booleanValue();
                Object operatorPolicy = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.miui.interfaces.IOperatorCustomizedPolicy");
                Object dataSubIdFlow = ModuleHelper.getObjectFieldByPath(operatorPolicy, "mobileIcons.activeMobileDataSubscriptionId");
                Object subIdObject = XposedHelpers.callMethod(dataSubIdFlow, "getValue");
                if (subIdObject != null) {
                    subIds[0] = ((Integer) subIdObject).intValue();
                }
                Object wifiAvailableFlow = XposedHelpers.getObjectField(param.getArgs()[2], "wifiAvailable");
                visibleStates[1] = ((Boolean)XposedHelpers.callMethod(wifiAvailableFlow, "getValue")).booleanValue();
                Object mStatusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                Object javaAdapter = XposedHelpers.getObjectField(mStatusBar, "mJavaAdapter");
                Function<Object, Boolean> getFinalVisibleState = (x) -> {
                    boolean currentVisible = visibleStates[0];
                    if (currentVisible && hideWithWifi && visibleStates[1]) {
                        currentVisible = false;
                    }
                    else if (currentVisible && hideNoDataSim && subId != subIds[0]) {
                        currentVisible = false;
                    }
                    return Boolean.valueOf(currentVisible);
                };
                boolean finalVisible = getFinalVisibleState.apply(null).booleanValue();
                Object finalVisibleFlow = ModuleHelper.createReadonlyFlowWithInitValue(finalVisible ? Boolean.TRUE : Boolean.FALSE, lpparam.getClassLoader());
                Object stateFlow = ModuleHelper.getMutableFlowOfReadonlyFlow(finalVisibleFlow);
                XposedHelpers.setObjectField(param.getThisObject(), "isVisible", finalVisibleFlow);
                XposedHelpers.callMethod(javaAdapter, "alwaysCollectFlow", isVisibleFlow, new Consumer() {
                    @Override
                    public void accept(Object obj) {
                        visibleStates[0] = ((Boolean) obj).booleanValue();
                        boolean finalVisible = getFinalVisibleState.apply(null).booleanValue();
                        XposedHelpers.callMethod(stateFlow, "setValue", finalVisible ? Boolean.TRUE : Boolean.FALSE);
                    }
                });
                if (hideNoDataSim) {
                    XposedHelpers.callMethod(javaAdapter, "alwaysCollectFlow", dataSubIdFlow, new Consumer() {
                        @Override
                        public void accept(Object obj) {
                            if (obj == null) return;
                            subIds[0] = ((Integer) obj).intValue();
                            boolean finalVisible = getFinalVisibleState.apply(null).booleanValue();
                            XposedHelpers.callMethod(stateFlow, "setValue", finalVisible ? Boolean.TRUE : Boolean.FALSE);
                        }
                    });
                }
                if (hideWithWifi) {
                    XposedHelpers.callMethod(javaAdapter, "alwaysCollectFlow", wifiAvailableFlow, new Consumer() {
                        @Override
                        public void accept(Object obj) {
                            visibleStates[1] = ((Boolean) obj).booleanValue();
                            boolean finalVisible = getFinalVisibleState.apply(null).booleanValue();
                            XposedHelpers.callMethod(stateFlow, "setValue", finalVisible ? Boolean.TRUE : Boolean.FALSE);
                        }
                    });
                }
            }
        });
    }

    private static boolean checkSlot(String slotName) {
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
            "hotspot".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_hotspot") ||
            "no_sim".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nosims") ||
            "bluetooth_handsfree_battery".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_btbattery") ||
            "ble_unlock_mode".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_ble_unlock") ||
            "bluetooth".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_bluetoothicn") ||
            "hd".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_volte");
    }

    public static void HideStatusBarIconsHook(PackageReadyParam lpparam) {
        MethodHook iconHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String iconType = (String)param.getArgs()[0];
                if (checkSlot(iconType)) {
                    param.getArgs()[1] = false;
                }
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.ui.StatusBarIconControllerImpl", lpparam.getClassLoader(), "setIconVisibility", String.class, boolean.class, iconHook);
    }


    public static void HideStatusBarIconsFromSystemManagerHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.CommandQueue", lpparam.getClassLoader(), "setIcon", String.class, "com.android.internal.statusbar.StatusBarIcon", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String slotName = (String)param.getArgs()[0];
                if (
                    ("stealth".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_privacy"))
                        || "mute".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_mute")
                        || "speakerphone".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_speaker")
                        || "call_record".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_record")
                        || "wireless_headset".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_wireless_headset")
                ){
                    XposedHelpers.setObjectField(param.getArgs()[1], "visible", false);
                }
            }
        });
    }

    public static void BatteryIndicatorHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "start", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Object sbWindowController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarWindowController");
                ViewGroup mStatusBarWindow = (ViewGroup) XposedHelpers.getObjectField(sbWindowController, "mStatusBarWindowView");

                BatteryIndicator indicator = new BatteryIndicator(mContext);
                mStatusBarWindow.addView(indicator);
                indicator.setAdjustViewBounds(false);
                indicator.init(param.getThisObject());
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator", indicator);
                Object mBatteryController = XposedHelpers.getObjectField(param.getThisObject(), "mBatteryController");
                XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator);
                XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged");
                XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged");
            }
        });

        Class<?> DozeServiceHostClass = findClass("com.android.systemui.statusbar.phone.DozeServiceHost", lpparam.getClassLoader());
        Class<?> NotificationPanelClass = findClass("com.android.systemui.shade.NotificationPanelViewController", lpparam.getClassLoader());
        if (NotificationPanelClass == null) return;
        Method isPanelExpandedMethod = ModuleHelper.findFirstMethodByName(NotificationPanelClass, "isPanelExpanded");
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updatePanelExpanded", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean mPanelExpanded = (boolean) isPanelExpandedMethod.invoke(param.getThisObject());
                boolean isKeyguardShowing = (boolean)ModuleHelper.getStaticObjectFieldSilently(DozeServiceHostClass, "mKeyguardShowing");
                Object mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mCentralSurfaces");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(mStatusBar, "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && mPanelExpanded);
            }
        });

        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "updateIsKeyguard", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)ModuleHelper.getStaticObjectFieldSilently(DozeServiceHostClass, "mKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onKeyguardStateChanged(isKeyguardShowing);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.DarkReceiverImpl", lpparam.getClassLoader(), "onDarkChanged", ArrayList.class, float.class, int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mStatusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(mStatusBar, "mBatteryIndicator");
                if (indicator != null) indicator.onDarkModeChanged((float)param.getArgs()[1], (int)param.getArgs()[2]);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl", lpparam.getClassLoader(), "fireBatteryLevelChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                int mLevel = XposedHelpers.getIntField(param.getThisObject(), "mLevel");
                boolean mCharging = XposedHelpers.getBooleanField(param.getThisObject(), "mCharging");
                boolean mCharged = XposedHelpers.getBooleanField(param.getThisObject(), "mCharged");
                if (indicator != null) indicator.onBatteryLevelChanged(mLevel, mCharging, mCharged);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.getClassLoader(), "firePowerSaveChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator");
                if (indicator != null) indicator.onPowerSaveChanged(XposedHelpers.getBooleanField(param.getThisObject(), "mPowerSave"));
            }
        });
    }
    public static void TempHideOverlaySystemUIHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.wm.shell.pip.PipTaskOrganizer", lpparam.getClassLoader(), "onTaskAppeared", new MethodHook() {
            private boolean isActListened = false;
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
                                boolean isPip = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isInPip");
                                if (isPip) {
                                    Object mSurfaceControlTransactionFactory = XposedHelpers.getObjectField(param.getThisObject(), "mSurfaceControlTransactionFactory");
                                    SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) XposedHelpers.callMethod(mSurfaceControlTransactionFactory, "getTransaction");
                                    SurfaceControl mLeash = (SurfaceControl) XposedHelpers.getObjectField(param.getThisObject(), "mLeash");
                                    transaction.setVisibility(mLeash, state);
                                    transaction.apply();
                                }
                            }
                        }
                    }, intentFilter, Context.RECEIVER_EXPORTED);
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

    public static void LockScreenAlbumArtHook(PackageReadyParam lpparam) {
        Class<?> MiuiThemeUtilsClass = findClassIfExists("com.miui.keyguard.utils.MiuiKeyguardUtils", lpparam.getClassLoader());

        ModuleHelper.hookAllConstructors("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme");
                if (isDefaultLockScreenTheme) {
                    Object notificationShadeDepthController = XposedHelpers.getObjectField(param.getThisObject(), "mDepthController");
                    ArrayList listeners = (ArrayList) XposedHelpers.getObjectField(notificationShadeDepthController, "listeners");
                    listeners.removeLast();
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
                                    XposedHelpers.callMethod(param.getThisObject(), "updateThemeBackgroundVisibility");
                                }
                                catch (Throwable e) {
                                }
                            }
                        }
                    }, intentFilter, RECEIVER_NOT_EXPORTED);
                }
            }
        });
        final boolean[] screenStates = {false}; // isAod
        MethodHook updateLockscreenHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme");
                if (!isDefaultLockScreenTheme) {
                    return ;
                }
                View view = (View) XposedHelpers.getObjectField(param.getThisObject(), "mThemeBackgroundView");
                boolean isOnShade = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isOnShade");
                if (isOnShade || screenStates[0]) {
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
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updateThemeBackgroundVisibility", updateLockscreenHook);
        boolean newAnimHook = ModuleHelper.hookAllMethodsSilently("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "linkageViewAnim$default",  new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean screenOn = (boolean) param.getArgs()[1];
                screenStates[0] = !screenOn;
                XposedHelpers.callMethod(param.getArgs()[0], "updateThemeBackgroundVisibility");
            }
        });
        if (!newAnimHook) {
            ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "linkageViewAnim", boolean.class, boolean.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    boolean screenOn = (boolean) param.getArgs()[0];
                    screenStates[0] = !screenOn;
                    XposedHelpers.callMethod(param.getThisObject(), "updateThemeBackgroundVisibility");
                }
            });
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.NotificationMediaManager", lpparam.getClassLoader(), "updateMediaMetaData", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme");
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
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.getClassLoader(), "dispatchUpdateMediaMetaData", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme");
                if (isDefaultLockScreenTheme) {
                    Object mMediaController = XposedHelpers.getObjectField(param.getThisObject(), "mMediaController");
                    if (mMediaController == null) {
                        XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null);
                        XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null);
                    }
                    Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    Intent updateAlbumWallpaper = new Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                    updateAlbumWallpaper.setPackage("com.android.systemui");
                    mContext.sendBroadcast(updateAlbumWallpaper);
                }
            }
        });
    }

    public static void SecureQSTilesHook(PackageReadyParam lpparam) {
        MethodHook clickHook = new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String tileName = (String) XposedHelpers.getObjectField(param.getThisObject(), "mTileSpec");
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
                if (MainModule.mPrefs.getBoolean("system_secureqs_custom")) {
                    secureTitles.add("intent");
                    secureTitles.add("custom");
                }
                if (secureTitles.contains(name)) {
                    Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    KeyguardManager kgMgr = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
                    if (!kgMgr.isKeyguardLocked() || !kgMgr.isKeyguardSecure()) return;
                    Object activityStater = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.plugins.ActivityStarter");
                    XposedHelpers.callMethod(activityStater, "postQSRunnableDismissingKeyguard", true, new Runnable() {
                        public void run() {
                            boolean keepOpened = MainModule.mPrefs.getBoolean("system_secureqs_keepopened");
                            if (keepOpened) {
                                Handler handler = new Handler(mContext.getMainLooper());
                                handler.postDelayed(() -> {
                                    Intent openCCIntent = new Intent(GlobalActions.ACTION_PREFIX + "ExpandSettings");
                                    openCCIntent.setPackage("com.android.systemui");
                                    mContext.sendBroadcast(openCCIntent);
                                }, 800);
                            }
                        }
                    });
                    Object mStatusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.CommandQueue");
                    XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels", 0, false);
                    param.returnAndSkip(null);
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "click", clickHook);
        ModuleHelper.hookAllMethods("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "longClick", clickHook);
        ModuleHelper.hookAllMethods("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "secondaryClick", clickHook);
    }

    public static void ExtendedPowerMenuHook(PackageReadyParam lpparam) {
        int fastbootTitleId = MainModule.resHooks.addFakeResource( "epm_fastboot_title", R.string.system_epm_action_fastboot_title, "string");
        int recoveryTitleId = MainModule.resHooks.addFakeResource( "epm_recovery_title", R.string.system_epm_action_recovery_title, "string");

        final int[] actionId = {-1};
        Class<?> DialogClass = findClass("com.android.systemui.globalactions.GlobalActionsDialogLite", lpparam.getClassLoader());
        ModuleHelper.findAndHookConstructor("com.android.systemui.globalactions.GlobalActionsDialogLite$SinglePressAction", lpparam.getClassLoader(), DialogClass, int.class, int.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                if (actionId[0] == 1) {
                    param.getArgs()[2] = fastbootTitleId;
                }
                else if (actionId[0] == 2) {
                    param.getArgs()[2] = recoveryTitleId;
                }
            }
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                actionId[0] = -1;
            }
        });
        Class<?> PowerAtionClass = findClass("com.android.systemui.globalactions.GlobalActionsDialogLite$PowerOptionsAction", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.systemui.globalactions.GlobalActionsDialogLite", lpparam.getClassLoader(), "createActionItems", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ArrayList mItems = (ArrayList) XposedHelpers.getObjectField(param.getThisObject(), "mItems");
                actionId[0] = 1;
                Object fastbootAction = XposedHelpers.newInstance(PowerAtionClass, param.getThisObject());
                actionId[0] = 2;
                Object recoveryAction = XposedHelpers.newInstance(PowerAtionClass, param.getThisObject());
                mItems.add(fastbootAction);
                mItems.add(recoveryAction);
            }
        });

        ModuleHelper.findAndHookMethod(PowerAtionClass, "onPress", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                int mMessageResId = XposedHelpers.getIntField(param.getThisObject(), "mMessageResId");
                if (mMessageResId == fastbootTitleId || mMessageResId == recoveryTitleId) {
                    Object actionsDialog = XposedHelpers.getSurroundingThis(param.getThisObject());
                    Context mContext = (Context)XposedHelpers.getObjectField(actionsDialog, "mContext");
                    Resources modRes = ModuleHelper.getModuleRes(mContext);
                    Class<?> SystemUIDialogClass = findClass("com.android.systemui.statusbar.phone.SystemUIDialog", lpparam.getClassLoader());
                    AlertDialog confirmDlg = (AlertDialog) XposedHelpers.newInstance(SystemUIDialogClass, mContext);
                    confirmDlg.setTitle(
                        modRes.getString(
                            mMessageResId == recoveryTitleId
                                ? R.string.system_epm_action_recovery_confirm_title
                                : R.string.system_epm_action_fastboot_confirm_title
                        )
                    );
                    confirmDlg.setButton(-1, Resources.getSystem().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                            Object mService = XposedHelpers.getObjectField(pm, "mService");
                            if (mMessageResId == recoveryTitleId) {
                                XposedHelpers.callMethod(mService, "reboot", false, "recovery", false);
                            } else {
                                XposedHelpers.callMethod(mService, "reboot", false, "bootloader", false);
                            }
                        }
                    });
                    confirmDlg.setButton(-2, Resources.getSystem().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {}
                    });
                    confirmDlg.show();
                    param.returnAndSkip(null);
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.plugins.PluginEnablerImpl", lpparam.getClassLoader(), "isEnabled", ComponentName.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                ComponentName componentName = (ComponentName) param.getArgs()[0];
                if (componentName.getClassName().contains("GlobalActions")) {
                    param.returnAndSkip(false);
                }
            }
        });
    }

    public static void HideDismissViewHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "getShowDismissView", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                param.returnAndSkip(false);
            }
        });
    }

    public static void HideNoficationAccessIconHook(PackageReadyParam lpparam) {
        MethodHook hideViewHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                View mShortCut = (View) XposedHelpers.getObjectField(param.getThisObject(), "mShortCut");
                if (mShortCut != null) {
                    mShortCut.setVisibility(View.GONE);
                    param.returnAndSkip(null);
                }
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.getClassLoader(), "updateShortCutVisibility", hideViewHook);
        Class<?> NotificationHeaderViewClass = findClass("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader());
        Method updateResourcesMethod = ModuleHelper.findFirstMethodByName(NotificationHeaderViewClass, "updateShortCutVisibility");
        if (updateResourcesMethod != null) {
            ModuleHelper.hookMethod(updateResourcesMethod, hideViewHook);
        }
//        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateLayout", new MethodHook() {
//            @Override
//            protected void after(final MethodHookParam param) throws Throwable {
//                View mShortCut = (View) XposedHelpers.getObjectField(param.getThisObject(), "mShortCut");
//                if (mShortCut != null) {
//                    mShortCut.setVisibility(View.GONE);
//                }
//            }
//        });
    }
    public static void HideNoNotificationsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout", lpparam.getClassLoader(), "updateEmptyShadeView", boolean.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mEmptyShadeView = (View)XposedHelpers.getObjectField(param.getThisObject(), "mEmptyShadeView");
                mEmptyShadeView.setOnClickListener(null);
                XposedHelpers.callMethod(mEmptyShadeView, "setVisible", false, false);
                param.returnAndSkip(null);
            }
        });
    }

    public static void ReplaceShortcutAppHook(PackageReadyParam lpparam) {
        MethodHook openAppHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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
                            Object mStatusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                            XposedHelpers.callMethod(mStatusBar, "collapsePanels");
                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    } else {
                        Object activiyStarter = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.plugins.ActivityStarter");
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
    public static void StatusBarStyleBatteryIconHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateAll$1", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
    public static void ForceClockUseSystemFontsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.clock.MiuiBaseClock", lpparam.getClassLoader(), "updateViewsTextSize", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                TextView mTimeText = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mTimeText");
                mTimeText.setTypeface(Typeface.DEFAULT);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.clock.MiuiLeftTopLargeClock", lpparam.getClassLoader(), "onLanguageChanged", String.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                TextView mTimeText = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mCurrentDateLarge");
                mTimeText.setTypeface(Typeface.DEFAULT);
            }
        });
    }
    public static void HideStatusBarWhenCaptureHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.getClassLoader(), "onViewCreated", View.class, Bundle.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
                view.getContext().registerReceiver(br, new IntentFilter("miui.intent.TAKE_SCREENSHOT"), Context.RECEIVER_EXPORTED);
            }
        });
    }
    public static void HideNavBarBeforeScreenshotHook(PackageReadyParam lpparam) {
        MethodHook hideNavHook = new MethodHook() {
            int visibleState = 0;
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                View view = (View) XposedHelpers.getObjectField(param.getThisObject(), "mView");
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
                view.getContext().registerReceiver(br, new IntentFilter("miui.intent.TAKE_SCREENSHOT"), Context.RECEIVER_EXPORTED);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBar", lpparam.getClassLoader(), "onInit", hideNavHook);
    }

    private static Bundle clickNotifyOptions;

    public static void OpenNotifyInFloatingWindowHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods(PendingIntent.class, "sendAndReturnResult", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.getArgs().length != 7) return;
                if (clickNotifyOptions != null) {
                    param.getArgs()[6] = clickNotifyOptions;
                    clickNotifyOptions = null;
                }
            }
        });
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter", lpparam.getClassLoader(), "onNotificationClicked", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object notificationEntry = param.getArgs()[0];
                Object mSbn = XposedHelpers.getObjectField(notificationEntry, "mSbn");
                Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
                PendingIntent pendingIntent = notify.contentIntent;
                if (pendingIntent == null) return;
                Object mKeyguardStateController = XposedHelpers.getObjectField(param.getThisObject(), "mKeyguardStateController");
                if (XposedHelpers.getBooleanField(mKeyguardStateController, "mShowing")) return;
                String pkgName;
                String opPkg = (String) XposedHelpers.callMethod(mSbn, "getOpPkg");
                String mPkgName = (String) XposedHelpers.callMethod(mSbn, "getPackageName");
                boolean isSubstituteNotification = !TextUtils.equals(mPkgName, opPkg);
                if (isSubstituteNotification) {
                    pkgName = mPkgName;
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
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                clickNotifyOptions = ModuleHelper.getFreeformOptions(mContext, pkgName, pendingIntent, true);
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private static TextView mPct = null;
    private static void initPct(ViewGroup container, int source) {
        Context context = container.getContext();
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

    static void CCSliderBrightnessPctHook(ClassLoader pluginLoader) {
        MethodHook startTrackHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                ClassLoader pluginClassLoader = param.getThisObject().getClass().getClassLoader();
                Class<?> PluginComponentFactoryClass = findClass("miui.systemui.dagger.PluginComponentFactory", pluginClassLoader);
                Object pluginComponentFactory = XposedHelpers.callStaticMethod(PluginComponentFactoryClass, "getInstance");
                Object pluginComponent = XposedHelpers.callMethod(pluginComponentFactory, "getPluginComponent");
                Object controlCenterWindowViewCreatorProvider = XposedHelpers.getObjectField(pluginComponent, "controlCenterWindowViewCreatorProvider");
                Object controlCenterWindowViewCreator = XposedHelpers.callMethod(controlCenterWindowViewCreatorProvider, "get");
                ViewGroup windowView = (ViewGroup) XposedHelpers.getObjectField(controlCenterWindowViewCreator, "windowView");
                initPct(windowView, 2);
                mPct.setVisibility(View.VISIBLE);
            }
        };
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.brightness.BrightnessSliderController$seekBarListener$1", pluginLoader, "onStartTrackingTouch", SeekBar.class, startTrackHook);
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.brightness.BrightnessPanelSliderController$seekBarListener$1", pluginLoader, "onStartTrackingTouch", SeekBar.class, startTrackHook);

    }

    public static void BrightnessPctHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.getClassLoader(), "onStop", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                removePct(mPct);
            }
        });

        final Class<?> BrightnessUtils = findClassIfExists("com.android.systemui.controlcenter.policy.BrightnessUtils", lpparam.getClassLoader());
        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.getClassLoader(), "onChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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
        Class<?> MiuiVolumeDialogImpl = findClassIfExists("com.android.systemui.miui.volume.VolumePanelViewController", pluginLoader);
        ModuleHelper.findAndHookMethod(MiuiVolumeDialogImpl, "showVolumePanelH", int.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                View mDialogView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mVolumeView");
                FrameLayout windowView = (FrameLayout) mDialogView.getParent();
                initPct(windowView, 3);
            }
        });

        ModuleHelper.findAndHookMethod(MiuiVolumeDialogImpl, "dismissH", int.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                removePct(mPct);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.VolumePanelViewController$VolumeSeekBarChangeListener", pluginLoader, "onProgressChanged", new MethodHook() {
            private int nowLevel = -233;
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
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

    public static void NotificationImportanceHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.MiuiNotificationListener", lpparam.getClassLoader(), "onSilentStatusBarIconsVisibilityChanged", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.getArgs()[0] = true;
            }
        });
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.notification.icon.domain.interactor.StatusBarNotificationIconsInteractor", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Class<?> StateFlowKtClass = findClass("kotlinx.coroutines.flow.StateFlowKt", lpparam.getClassLoader());
                Object stateFlow = XposedHelpers.callStaticMethod(StateFlowKtClass, "MutableStateFlow", Boolean.FALSE);
                XposedHelpers.setObjectField(param.getArgs()[2], "showSilentStatusIcons", stateFlow);
            }
        });
        XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.notification.icon.domain.interactor.NotificationIconsInteractor$filteredNotifSet$1$1", lpparam.getClassLoader(), "invoke", Object.class, new MethodHook() {
            Object notifCollection = null;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean showLowPriority = XposedHelpers.getBooleanField(param.getThisObject(), "$showLowPriority");
                if (showLowPriority) return;
                boolean result = ((Boolean)param.getResult()).booleanValue();
                if (!result) return;
                if (notifCollection == null) {
                    Object dismissHelper = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.policy.DismissNotificationHelper");
                    notifCollection = XposedHelpers.getObjectField(dismissHelper, "notifCollection");
                }
                String notifyKey = (String) XposedHelpers.getObjectField(param.getArgs()[0], "key");
                Object notifyEntry = XposedHelpers.callMethod(notifCollection, "getEntry", notifyKey);
                if (notifyEntry == null) {
                    return;
                }
                Object mRanking = XposedHelpers.getObjectField(notifyEntry, "mRanking");
                int importance = (int) XposedHelpers.callMethod(mRanking, "getImportance");
                if (importance > 1) {
                    return;
                }
                param.setResult(Boolean.FALSE);
            }
        });
    }

    public static void RemovePackageNotificationsLimitHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.collection.coordinator.CountLimitCoordinator", lpparam.getClassLoader(), "attach", HookerClassHelper.DO_NOTHING);
    }

    public static void DisableFoldNotificationsHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.collection.coordinator.FoldCoordinator", lpparam.getClassLoader(), "attach", HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.miui.systemui.notification.MiuiBaseNotifUtil", lpparam.getClassLoader(), "shouldSuppressFold", HookerClassHelper.returnConstant(true));
    }

    public static void DisableStrongToastHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.toast.MIUIStrongToastControl", lpparam.getClassLoader(), "showCustomStrongToast", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean blockToast = MainModule.mPrefs.getBoolean("system_notif_disable_strong_toast_always", true);
                if (!blockToast) {
                    boolean dnd = MainModule.mPrefs.getBoolean("system_notif_disable_strong_toast_dnd", false);
                    if (dnd) {
                        Object mStatusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                        Object zenModeController = ModuleHelper.getObjectFieldByPath(mStatusBar, "mIconPolicy.mZenController");
                        blockToast = (boolean)XposedHelpers.callMethod(zenModeController, "isZenModeOn");
                    }
                }
                if (blockToast) {
                    param.returnAndSkip(null);
                }
            }
        });
    }
    public static void TweakStrongToastHook(PackageReadyParam lpparam) {
        int toastWidth = MainModule.mPrefs.getInt("system_notif_strong_toast_width", 100);
        if (toastWidth < 100) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "strong_toast_width_window", Math.ceil(3.37 * toastWidth));
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "strong_toast_width", Math.ceil(3.2 * toastWidth));
            ModuleHelper.hookAllMethods("com.android.systemui.toast.MIUIStrongToast", lpparam.getClassLoader(), "showCustomStrongToast", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    View mStrongToastBottomView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mStrongToastBottomView");
                    mStrongToastBottomView.setVisibility(View.GONE);
                    RelativeLayout mRLLeft = (RelativeLayout) XposedHelpers.getObjectField(param.getThisObject(), "mRLLeft");
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mRLLeft.getLayoutParams();
                    layoutParams.leftMargin = 0;
                    mRLLeft.setLayoutParams(layoutParams);
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.toast.MIUIStrongToast", lpparam.getClassLoader(), "getWindowParam", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    WindowManager.LayoutParams lp = (WindowManager.LayoutParams) param.getResult();
                    lp.width = (int)Helpers.dp2px(3.2f * toastWidth);
                    param.setResult(lp);
                }
            });
        }
    }

    public static void HideSafeVolumeDlgHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.volume.VolumeUI", lpparam.getClassLoader(), "start", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object volumeDialogComponent = XposedHelpers.getObjectField(param.getThisObject(), "mVolumeComponent");
                Object volumeDialogControllerImpl = XposedHelpers.getObjectField(volumeDialogComponent, "mController");
                XposedHelpers.setObjectField(volumeDialogControllerImpl, "mShowSafetyWarning", false);
                Object audioManager = XposedHelpers.getObjectField(volumeDialogControllerImpl, "mAudio");
                XposedHelpers.callMethod(audioManager, "disableSafeMediaVolume");
            }
        });
    }
    public static void DisableHeadsUpWhenMuteHook(PackageReadyParam lpparam) {
        final boolean[] mMuteVisible = {false};
        MethodHook disableHeadsUpHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                if (mMuteVisible[0]) {
                    param.returnAndSkip(false);
                }
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl", lpparam.getClassLoader(), "canAlertAwakeCommon", disableHeadsUpHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), "updateVolumeZen", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                mMuteVisible[0] = XposedHelpers.getBooleanField(param.getThisObject(), "mMuteVisible");
            }
        });
    }

    public static void HideLockscreenZenModeHook(PackageReadyParam lpparam) {
        Class<?> NotificationHeaderViewClass = findClass("com.android.systemui.statusbar.notification.zen.ZenModeViewController", lpparam.getClassLoader());
        Method updateVisibilityMethod = ModuleHelper.findFirstMethodByName(NotificationHeaderViewClass, "updateVisibility");
        if (updateVisibilityMethod != null) {
            ModuleHelper.hookMethod(updateVisibilityMethod, new MethodHook() {
                boolean manuallyDismissed;

                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    manuallyDismissed = XposedHelpers.getBooleanField(param.getThisObject(), "manuallyDismissed");
                    XposedHelpers.setObjectField(param.getThisObject(), "manuallyDismissed", true);

                }

                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    XposedHelpers.setObjectField(param.getThisObject(), "manuallyDismissed", manuallyDismissed);
                }
            });
        }
    }

    public static void LongClickTileOpenInFreeFormHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "handleLongClick", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object longClickIntent = XposedHelpers.callMethod(param.getThisObject(), "getLongClickIntent");
                if (longClickIntent != null) {
                    Intent intent = (Intent) longClickIntent;
                    String action = intent.getAction();
                    boolean isSettings = action.startsWith("android.settings");
                    if (!isSettings && intent.getComponent() != null) {
                        ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
                        if (foregroundInfo != null) {
                            String topPackage = foregroundInfo.mForegroundPackageName;
                            if ("com.miui.home".equals(topPackage)) {
                                return;
                            }
                        }
                        Intent bIntent = new Intent(ACTION_PREFIX + "SetFreeFormPackage");
                        bIntent.putExtra("package", intent.getComponent().getPackageName());
                        bIntent.setPackage("android");
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        mContext.sendBroadcast(bIntent);
                    }
                }
            }
        });
    }

    public static void CollapseCCAfterClickHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "click", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object mState = XposedHelpers.callMethod(param.getThisObject(), "getState");
                int state = XposedHelpers.getIntField(mState, "state");
                if (state != 0) {
                    String tileSpec = (String) XposedHelpers.callMethod(param.getThisObject(), "getTileSpec");
                    if (!"edit".equals(tileSpec)) {
                        Object mHost = XposedHelpers.getObjectField(param.getThisObject(), "mHost");
                        XposedHelpers.callMethod(mHost, "collapsePanels");
                    }
                }
            }
        });
    }

    public static void SwitchCCAndNotificationHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "handleEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
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
            protected void before(final MethodHookParam param) throws Throwable {
                boolean added = XposedHelpers.getBooleanField(param.getThisObject(), "added");
                if (added) {
                    boolean useCC = XposedHelpers.getBooleanField(XposedHelpers.getObjectField(param.getThisObject(), "controlCenterController"), "useControlCenter");
                    if (useCC) {
                        MotionEvent motionEvent = (MotionEvent) param.getArgs()[0];
                        if (motionEvent.getActionMasked() == 0) {
                            XposedHelpers.setObjectField(param.getThisObject(), "mDownX", motionEvent.getRawX());
                        }
                        Object controlCenterWindowView = XposedHelpers.getObjectField(param.getThisObject(), "windowView");
                        if (controlCenterWindowView == null) {
                            param.returnAndSkip(false);
                        }
                        else {
                            float mDownX = XposedHelpers.getFloatField(param.getThisObject(), "downX");
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
    public static void NoLightUpOnChargeHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.charge.MiuiChargeController", lpparam.getClassLoader(), "shouldShowChargeAnim", HookerClassHelper.returnConstant(false));
    }
    public static void HidePrivacyIndicatorHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.privacy.MiuiPrivacyControllerImpl", lpparam.getClassLoader(), "setStatus", int.class, String.class, Bundle.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.returnAndSkip(null);
            }
        });
    }
    public static void ExpandHeadsUpHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "setHeadsUp", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean mOnKeyguard = XposedHelpers.getBooleanField(param.getThisObject(), "mOnKeyguard");
                boolean showHeadsUp = (boolean) param.getArgs()[0];
                if (!mOnKeyguard && showHeadsUp) {
                    View notifyRow = (View) param.getThisObject();
                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.getThisObject(), "getEntry"), "mSbn");
                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                    int opt = MainModule.mPrefs.getStringAsInt("system_expandheadups", 1);
                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandheadups_apps").contains(pkgName);
                    if (opt == 2 && !isSelected || opt == 3 && isSelected) {
                        Runnable expandNotify = new Runnable() {
                            @Override
                            public void run() {
                                View.OnClickListener mExpandClickListener = (View.OnClickListener) XposedHelpers.getObjectField(param.getThisObject(), "mExpandClickListener");
                                mExpandClickListener.onClick(notifyRow);
                            }
                        };
                        notifyRow.postDelayed(expandNotify, 60);
                    }
                }
            }
        });
    }
    public static void AutoDismissExpandedPopupsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BaseHeadsUpManager$HeadsUpEntry", lpparam.getClassLoader(), "cancelAutoRemovalCallbacks", String.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.getArgs()[0] != null) {
                    String reason = (String) param.getArgs()[0];
                    if (reason.contains("setExpanded(true)")) {
                        param.returnAndSkip(null);
                    }
                }
            }
        });
    }
}