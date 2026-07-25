package tv.withaibuild.customiuizer.mods;

import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import miui.telephony.TelephonyManager;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.PrefMap;


public class SystemUIMonitorAndTileHooks {
    public static void MonitorDeviceInfoHook(PackageReadyParam lpparam, PrefMap mPrefs) {
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
                protected void after(final AfterHookCallback param) throws Throwable {
                    Class<?> StatusBarIconHolder = findClass("com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.getClassLoader());
                    Object iconController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarIconController");
                    for (int iconType:customIconTypes) {
                        String slot = SystemUI.getSlotNameByType(iconType);
                        Object mStatusBarIconList = XposedHelpers.getObjectField(iconController, "mStatusBarIconList");
                        Object iconHolder = XposedHelpers.callMethod(mStatusBarIconList, "getIconHolder", 0, slot);
                        if (iconHolder == null) {
                            iconHolder = XposedHelpers.newInstance(StatusBarIconHolder);
                            XposedHelpers.setObjectField(iconHolder, "mType", iconType);
                            XposedHelpers.callMethod(iconController, "setIcon", slot, iconHolder);
                        }
                    }
                }
            });
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconController$IconManager", lpparam.getClassLoader(), "addHolder", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    if (param.getArgs().length != 4) return;
                    Object iconHolder = param.getArgs()[3];
                    int type = XposedHelpers.getIntField(iconHolder, "mType");
                    if (type == 91 || type == 92) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) XposedHelpers.callMethod(param.getThisObject(), "onCreateLayoutParams");
                        View iconView = SystemUI.createStatusbarTextIcon(mContext, lp, type, true);
                        int i = (int) param.getArgs()[0];
                        ViewGroup mGroup = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mGroup");
                        mGroup.addView(iconView, i);
                        SystemUI.mStatusbarTextIcons.add(iconView);
                        param.returnAndSkip(iconView);
                    }
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.getClassLoader(), "getSlot", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View nsView = (View) param.getThisObject();
                Object tagData = nsView.getTag(SystemUI.textIconTagId);
                if (tagData != null) {
                    param.returnAndSkip(SystemUI.getSlotNameByType((int)tagData));
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
                            for (View tv : SystemUI.mStatusbarTextIcons) {
                                Object tagData = tv.getTag(SystemUI.textIconTagId);
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
                                            currVal = String.format(Locale.ROOT, "%.2f", rawCurr / 1000f);
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
                                        simpleWatt = String.format(Locale.ROOT, "%.2f", Math.abs(voltVal * rawCurr) / 1000);
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
                                    String simpleBatteryTemp = String.format(Locale.ROOT, "%.1f", batteryTempVal / 10f);
                                    String simpleCpuTemp = String.format(Locale.ROOT, "%.1f", cpuTempVal / 1000f);
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
    @SuppressLint("DiscouragedApi")
    public static void AddCustomTileHook(PackageReadyParam lpparam) {
        final boolean enable5G = MainModule.mPrefs.getBoolean("system_fivegtile");
        final boolean enableFps = MainModule.mPrefs.getBoolean("system_cc_fpstile");
        final boolean enableFloatingTime = MainModule.mPrefs.getBoolean("system_cc_floatingtimetile");
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
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) param.getArgs()[0];
                if (tileName.startsWith("custom_")) {
                    String nfcField = "nfcTileProvider";
                    Object provider = XposedHelpers.getObjectField(param.getThisObject(), nfcField);
                    Object tile = XposedHelpers.callMethod(provider, "get");
                    XposedHelpers.setAdditionalInstanceField(tile, "customName", tileName);
                    XposedHelpers.callMethod(tile, "handleInitialize");
                    XposedHelpers.callMethod(tile, "handleStale");
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
                    else if ("custom_floatingtime".equals(tileName)) {
                        param.returnAndSkip(modRes.getString(R.string.qs_toggle_floatingtime));
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
                        ContentResolver resolver = mContext.getContentResolver();
                        ContentObserver oldObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "tileListener");
                        if (oldObserver != null) {
                            resolver.unregisterContentObserver(oldObserver);
                            XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "tileListener");
                        }
                        if (mListening) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean z) {
                                    XposedHelpers.callMethod(param.getThisObject(), "refreshState");
                                }
                            };
                            resolver.registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, contentObserver);
                            resolver.registerContentObserver(Settings.Global.getUriFor("dual_nr_enabled"), false, contentObserver);
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "tileListener", contentObserver);
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
                        ContentResolver resolver = mContext.getContentResolver();
                        ContentObserver oldObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "tileListener");
                        if (oldObserver != null) {
                            resolver.unregisterContentObserver(oldObserver);
                            XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "tileListener");
                        }
                        if (mListening) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean z) {
                                    XposedHelpers.callMethod(param.getThisObject(), "refreshState");
                                }
                            };
                            resolver.registerContentObserver(Settings.System.getUriFor("miui_time_floating_window"), false, contentObserver);
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "tileListener", contentObserver);
                        }
                    }

                    param.returnAndSkip(null);
                }
            }
        });
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.getClassLoader(), "handleShowStateMessage", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String tileName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName");
                if (tileName != null) {
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
}
