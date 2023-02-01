package name.mikanoshi.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;
import static name.mikanoshi.customiuizer.mods.GlobalActions.ACTION_PREFIX;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AndroidAppHelper;
import android.app.KeyguardManager;
import android.app.MiuiNotification;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BadParcelableException;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Magnifier;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.callbacks.XCallback;
import miui.telephony.TelephonyManager;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.AudioVisualizer;
import name.mikanoshi.customiuizer.utils.BatteryIndicator;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.Helpers.MimeType;
import name.mikanoshi.customiuizer.utils.ResourceHooks;
import name.mikanoshi.customiuizer.utils.SoundData;

public class SystemUI {
    private final static String StatusBarCls = Helpers.isTPlus() ? "com.android.systemui.statusbar.phone.CentralSurfacesImpl" : "com.android.systemui.statusbar.phone.StatusBar";

    private static int statusbarTextIconLayoutResId = 0;
    private static int notifVolumeOnResId;
    private static int notifVolumeOffResId;
    public static void setupStatusBar(LoadPackageParam lpparam) {
        statusbarTextIconLayoutResId = MainModule.resHooks.addResource("statusbar_text_icon", R.layout.statusbar_text_icon);
        if (MainModule.mPrefs.getBoolean("system_statusbar_topmargin")) {
            int topMargin = MainModule.mPrefs.getInt("system_statusbar_topmargin_val", 1);
            MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "status_bar_padding_top", topMargin);
        }
        if (MainModule.mPrefs.getBoolean("system_statusbar_horizmargin")) {
            MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "status_bar_padding_start", 0);
            MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "status_bar_padding_end", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_cc_enable_style_switch")) {
            MainModule.resHooks.setObjectReplacement(lpparam.packageName, "integer", "force_use_control_panel", 0);
        }
        if (MainModule.mPrefs.getBoolean("system_qs_force_systemfonts")) {
            MainModule.resHooks.setObjectReplacement(lpparam.packageName, "bool", "header_big_time_use_system_font", true);
        }
        if (MainModule.mPrefs.getBoolean("system_separatevolume") && MainModule.mPrefs.getBoolean("system_separatevolume_slider")) {
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_content_width_expanded", R.dimen.miui_volume_content_width_expanded);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_ringer_layout_width_expanded", R.dimen.miui_volume_ringer_layout_width_expanded);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_width_expanded", R.dimen.miui_volume_column_width_expanded);
            MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_margin_horizontal_expanded", R.dimen.miui_volume_column_margin_horizontal_expanded);
            notifVolumeOnResId = MainModule.resHooks.addResource("ic_miui_volume_notification", R.drawable.ic_miui_volume_notification);
            notifVolumeOffResId = MainModule.resHooks.addResource("ic_miui_volume_notification_mute", R.drawable.ic_miui_volume_notification_mute);
        }
    }
    public static void DisplayBatteryDetailHook(LoadPackageParam lpparam) {
        Class <?> ChargeUtilsClass = findClass("com.android.keyguard.charge.ChargeUtils", lpparam.classLoader);
        Class <?> DarkIconDispatcherClass = XposedHelpers.findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.classLoader);
        Class <?> Dependency = findClass("com.android.systemui.Dependency", lpparam.classLoader);
        Class <?> StatusBarIconHolder = XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.classLoader);
        boolean atRight = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        if (atRight && !MainModule.mPrefs.getBoolean("system_statusbar_dualrows")) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object iconController = XposedHelpers.getObjectField(param.thisObject, "mStatusBarIconController");
                    int slotIndex = (int) XposedHelpers.callMethod(iconController, "getSlotIndex", "battery_detail");
                    Object iconHolder = XposedHelpers.callMethod(iconController, "getIcon", slotIndex, 0);
                    if (iconHolder == null) {
                        iconHolder = XposedHelpers.newInstance(StatusBarIconHolder);
                        XposedHelpers.setObjectField(iconHolder, "mType", 91);
                        XposedHelpers.callMethod(iconController, "setIcon", slotIndex, iconHolder);
                    }
                    if (!isHooked) {
                        isHooked = true;
                        updateTempAndCurrent(ChargeUtilsClass);
                    }
                }
            });

            Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconController$IconManager", lpparam.classLoader, "addHolder", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    if (param.args.length != 4) return;
                    Object iconHolder = param.args[3];
                    int type = (int) XposedHelpers.callMethod(iconHolder, "getType");
                    if (type == 91) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) XposedHelpers.callMethod(param.thisObject, "onCreateLayoutParams");
                        TextView batteryView = createBatteryDetailView(mContext, lp);
                        int i = (int) param.args[0];
                        ViewGroup mGroup = (ViewGroup) XposedHelpers.getObjectField(param.thisObject, "mGroup");
                        mGroup.addView(batteryView, i);
                        mBatteryDetailViews.add(batteryView);
                        param.setResult(batteryView);
                    }
                }
            });
            Class <?> NetworkSpeedViewClass = XposedHelpers.findClass("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader);
            Helpers.findAndHookMethod(NetworkSpeedViewClass, "getSlot", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Object customSlot = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomSlot");
                    if (customSlot != null) {
                        param.setResult(customSlot);
                    }
                }
            });
        }
        else if (!atRight) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getContext");
                    TextView mSplitter = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDripNetworkSpeedSplitter");
                    ViewGroup batteryViewContainer = (ViewGroup) mSplitter.getParent();
                    int bvIndex = batteryViewContainer.indexOfChild(mSplitter);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mSplitter.getLayoutParams();
                    TextView batteryView = createBatteryDetailView(mContext, lp);
                    batteryViewContainer.addView(batteryView, bvIndex + 1);
                    mBatteryDetailViews.add(batteryView);
                    Object DarkIconDispatcher = XposedHelpers.callStaticMethod(Dependency, "get", DarkIconDispatcherClass);
                    XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", batteryView);
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mBatteryView", batteryView);
                    if (!isHooked) {
                        isHooked = true;
                        updateTempAndCurrent(ChargeUtilsClass);
                    }
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "showClock", boolean.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object bv = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryView");
                    if (bv != null) {
                        XposedHelpers.callMethod(bv, "setVisibilityByController", true);
                    }
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "hideClockInternal", int.class, boolean.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object bv = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryView");
                    if (bv != null) {
                        XposedHelpers.callMethod(bv, "setVisibilityByController", false);
                    }
                }
            });
        }
    }

    private static TextView createBatteryDetailView(Context mContext, LinearLayout.LayoutParams lp) {
        TextView batteryView = (TextView) LayoutInflater.from(mContext).inflate(statusbarTextIconLayoutResId, null);
        XposedHelpers.setObjectField(batteryView, "mVisibilityByDisableInfo", 0);
        XposedHelpers.setObjectField(batteryView, "mVisibleByController", true);
        XposedHelpers.setObjectField(batteryView, "mShown", true);
        XposedHelpers.setAdditionalInstanceField(batteryView, "mCustomSlot", "battery_detail");
        Resources res = mContext.getResources();
        int styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
        batteryView.setTextAppearance(styleId);
        float fontSize = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_fontsize", 16) * 0.5f;
        int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1);
        if (opt == 1 && MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow")) {
            batteryView.setLineSpacing(0, fontSize > 8.5f ? 0.85f : 0.9f);
            batteryView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        batteryView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_bold")) {
            batteryView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        int leftMargin = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_leftmargin", 8);
        leftMargin = (int)TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            leftMargin * 0.5f,
            res.getDisplayMetrics()
        );
        int rightMargin = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_rightmargin", 8);
        rightMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            rightMargin * 0.5f,
            res.getDisplayMetrics()
        );
        batteryView.setPaddingRelative(leftMargin, 0, rightMargin, 0);

        int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_verticaloffset", 8);
        if (verticalOffset != 8) {
            float marginTop = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (verticalOffset - 8) * 0.5f,
                res.getDisplayMetrics()
            );
            lp.topMargin = (int) (marginTop);
        }
        batteryView.setLayoutParams(lp);
        return batteryView;
    }
    static final ArrayList<TextView> mBatteryDetailViews = new ArrayList<TextView>();

    private static void updateTempAndCurrent(Class<?> ChargeUtilsClass) {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable refreshRunner = new Runnable() {
            @Override
            public void run() {
                String batteryInfo = "";
                FileInputStream fis = null;
                Properties props = null;
                boolean showInfo = true;
                if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_incharge") && ChargeUtilsClass != null) {
                    Object batteryStatus = Helpers.getStaticObjectFieldSilently(ChargeUtilsClass, "sBatteryStatus");
                    if (batteryStatus == null) {
                        showInfo = false;
                    }
                    else {
                        showInfo = (boolean) XposedHelpers.callMethod(batteryStatus, "isCharging");
                    }
                }
                if (showInfo) {
                    try {
                        fis = new FileInputStream("/sys/class/power_supply/battery/uevent");
                        props = new Properties();
                        props.load(fis);
                    }
                    catch (Throwable ign) {}
                    finally {
                        try {
                            fis.close();
                        }
                        catch (Throwable ign) {}
                    }
                    if (props != null) {
                        int tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
                        int currVal = -1 * Math.round(Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW")) / 1000);
                        if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_positive")) {
                            currVal = Math.abs(currVal);
                        }
                        int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1);
                        String simpleTempVal = tempVal % 10 == 0 ? (tempVal / 10 + "") : (tempVal / 10f + "");
                        if (opt == 1) {
                            String splitChar = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow")
                                ? " " : "\n";
                            batteryInfo = simpleTempVal + "℃" + splitChar + currVal + "mA";
                            if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                batteryInfo = currVal + "mA" + splitChar + simpleTempVal + "℃";
                            }
                        }
                        else if (opt == 2) {
                            batteryInfo = simpleTempVal + "℃";
                        }
                        else {
                            batteryInfo = currVal + "mA";
                        }
                    }
                }
                for (TextView tv:mBatteryDetailViews) {
                    XposedHelpers.callMethod(tv, "setBlocked", !showInfo);
                    XposedHelpers.callMethod(tv, "setNetworkSpeed", batteryInfo);
                }
                handler.postDelayed(this, 1500);
            }
        };
        handler.post(refreshRunner);
    }

    public static void AddFiveGTileHook(LoadPackageParam lpparam) {

        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            private boolean isListened = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isListened) {
                    isListened = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    int stockTilesResId = mContext.getResources().getIdentifier("miui_quick_settings_tiles_stock", "string", lpparam.packageName);
                    String stockTiles = mContext.getString(stockTilesResId) + ",custom_5G";
                    MainModule.resHooks.setObjectReplacement(lpparam.packageName, "string", "miui_quick_settings_tiles_stock", stockTiles);
                    MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "string", "miui_quick_settings_tiles_stock", stockTiles);
                    MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "string", "quick_settings_tiles_stock", stockTiles);
                }
            }
        });
        String QSFactoryCls = Helpers.isTPlus() ? "com.android.systemui.qs.tileimpl.MiuiQSFactory" : "com.android.systemui.qs.tileimpl.QSFactoryImpl";
        Class<?> ResourceIconClass = findClass("com.android.systemui.qs.tileimpl.QSTileImpl$ResourceIcon", lpparam.classLoader);
        Helpers.findAndHookMethod(QSFactoryCls, lpparam.classLoader, "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String tileName = (String) param.args[0];
                if (tileName.startsWith("custom_")) {
                    String nfcField = Helpers.isTPlus() ? "nfcTileProvider" : "mNfcTileProvider";
                    Object provider = XposedHelpers.getObjectField(param.thisObject, nfcField);
                    Object tile = XposedHelpers.callMethod(provider, "get");
                    XposedHelpers.setAdditionalInstanceField(tile, "customName", tileName);
                    param.setResult(tile);
                }
            }
        });
        String NfcTileCls = Helpers.isTPlus() ? "com.android.systemui.qs.tiles.MiuiNfcTile" : "com.android.systemui.qs.tiles.NfcTile";
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "isAvailable", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        param.setResult(TelephonyManager.getDefault().isFiveGCapable());
                    }
                    else {
                        param.setResult(false);
                    }
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "getTileLabel", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        Resources modRes = Helpers.getModuleRes(mContext);
                        param.setResult(modRes.getString(R.string.qs_toggle_5g));
                    }
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleSetListening", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        boolean mListening = (boolean) param.args[0];
                        if (mListening) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean z) {
                                    XposedHelpers.callMethod(param.thisObject, "refreshState");
                                }
                            };
                            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, contentObserver);
                            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("dual_nr_enabled"), false, contentObserver);
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "tileListener", contentObserver);
                        }
                        else {
                            ContentObserver contentObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(param.thisObject, "tileListener");
                            mContext.getContentResolver().unregisterContentObserver(contentObserver);
                        }
                    }
                    param.setResult(null);
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "getLongClickIntent", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.settings.PreferredNetworkTypeListPreference"));
                        param.setResult(intent);
                    }
                    else {
                        param.setResult(null);
                    }
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleClick", View.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        TelephonyManager manager = TelephonyManager.getDefault();
                        manager.setUserFiveGEnabled(!manager.isUserFiveGEnabled());
                    }
                    param.setResult(null);
                }
            }
        });

        int fiveGIconResId = MainModule.resHooks.addResource("ic_qs_5g_on", R.drawable.ic_qs_5g_on);
        int fiveGIconOffResId = MainModule.resHooks.addResource("ic_qs_5g_off", R.drawable.ic_qs_5g_off);
        Helpers.hookAllMethods(NfcTileCls, lpparam.classLoader, "handleUpdateState", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Object booleanState = param.args[0];
                        TelephonyManager manager = TelephonyManager.getDefault();
                        boolean isEnable = manager.isUserFiveGEnabled();
                        XposedHelpers.setObjectField(booleanState, "value", isEnable);
                        XposedHelpers.setObjectField(booleanState, "state", isEnable ? 2 : 1);
                        String tileLabel = (String) XposedHelpers.callMethod(param.thisObject, "getTileLabel");
                        XposedHelpers.setObjectField(booleanState, "label", tileLabel);
                        XposedHelpers.setObjectField(booleanState, "contentDescription", tileLabel);
                        XposedHelpers.setObjectField(booleanState, "expandedAccessibilityClassName", Switch.class.getName());
                        Object mIcon = XposedHelpers.callStaticMethod(ResourceIconClass, "get", isEnable ? fiveGIconResId : fiveGIconOffResId);
                        XposedHelpers.setObjectField(booleanState, "icon", mIcon);
                    }
                    param.setResult(null);
                }
            }
        });
    }

    public static void DualRowStatusbarHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                FrameLayout sbView = (FrameLayout) param.thisObject;
                Context mContext = sbView.getContext();
                LinearLayout leftLayout = new LinearLayout(mContext);
                LinearLayout rightLayout = new LinearLayout(mContext);
                LinearLayout leftContainer = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, "mStatusBarLeftContainer");
                ViewGroup rightContainer = (ViewGroup) XposedHelpers.getObjectField(param.thisObject, "mSystemIconArea");
                View switchUserView = null;
                if (Helpers.isTPlus()) {
                    int userViewId = sbView.getResources().getIdentifier("user_switcher_container", "id", lpparam.packageName);
                    switchUserView = sbView.findViewById(userViewId);
                    ((ViewGroup) switchUserView.getParent()).removeView(switchUserView);
                }
                int firstRowLeftPadding = 0;
                int firstRowRightPadding = 0;
                if (MainModule.mPrefs.getBoolean("system_statusbar_dualrows_firstrow_horizmargin")) {
                    firstRowLeftPadding = MainModule.mPrefs.getInt("system_statusbar_dualrows_firstrow_horizmargin_left", 0);
                    firstRowRightPadding = MainModule.mPrefs.getInt("system_statusbar_dualrows_firstrow_horizmargin_right", 0);
                }
                LinearLayout statusBarcontents = (LinearLayout) rightContainer.getParent();
                statusBarcontents.removeView(leftContainer);
                statusBarcontents.removeView(rightContainer);
                statusBarcontents.addView(leftLayout, 0);
                statusBarcontents.addView(rightLayout);
                XposedHelpers.setObjectField(param.thisObject, "mSystemIconArea", rightLayout);
                leftLayout.addView(leftContainer);
                if (firstRowLeftPadding > 0) {
                    leftContainer.setPaddingRelative(firstRowLeftPadding, 0, 0, 0);
                }
                LinearLayout secondLeft = new LinearLayout(mContext);
                leftLayout.addView(secondLeft);
                LinearLayout firstRight = new LinearLayout(mContext);
                rightLayout.addView(firstRight);
                firstRight.setGravity(Gravity.END);
                if (firstRowRightPadding > 0) {
                    firstRight.setPaddingRelative(0, 0, firstRowRightPadding, 0);
                }
                LinearLayout secondRight = new LinearLayout(mContext);
                rightLayout.addView(secondRight);
                secondRight.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                View mBattery = (View) XposedHelpers.getObjectField(param.thisObject, "mBattery");
                ((ViewGroup) mBattery.getParent()).removeView(mBattery);
                if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright") && MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent")) {
                    TextView batteryView = createBatteryDetailView(mContext, new LinearLayout.LayoutParams(-2, -2));
                    secondRight.addView(batteryView);
                    mBatteryDetailViews.add(batteryView);
                    Class <?> ChargeUtilsClass = findClass("com.android.keyguard.charge.ChargeUtils", lpparam.classLoader);
                    Class <?> DarkIconDispatcherClass = XposedHelpers.findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.classLoader);
                    Class <?> Dependency = findClass("com.android.systemui.Dependency", lpparam.classLoader);
                    Object DarkIconDispatcher = XposedHelpers.callStaticMethod(Dependency, "get", DarkIconDispatcherClass);
                    XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", batteryView);
                    updateTempAndCurrent(ChargeUtilsClass);
                }
                secondRight.addView(mBattery);

                if (Helpers.isTPlus()) {
                    if (switchUserView != null) {
                        secondRight.addView(switchUserView);
                    }
                }

                XposedHelpers.setAdditionalInstanceField(param.thisObject, "leftLayout", leftLayout);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "rightLayout", rightLayout);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "firstRight", firstRight);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "secondLeft", secondLeft);

                View mFullscreenStatusBarNotificationIconArea = (View) XposedHelpers.getObjectField(param.thisObject, "mFullscreenStatusBarNotificationIconArea");
                ((ViewGroup) mFullscreenStatusBarNotificationIconArea.getParent()).removeView(mFullscreenStatusBarNotificationIconArea);
                secondLeft.addView(mFullscreenStatusBarNotificationIconArea);
                View mDripStatusBarNotificationIconArea = (View) XposedHelpers.getObjectField(param.thisObject, "mDripStatusBarNotificationIconArea");
                ((ViewGroup) mDripStatusBarNotificationIconArea.getParent()).removeView(mDripStatusBarNotificationIconArea);
                secondLeft.addView(mDripStatusBarNotificationIconArea);
                View mStatusBarStatusIcons = (View) XposedHelpers.getObjectField(param.thisObject, "mStatusBarStatusIcons");
                ((ViewGroup) mStatusBarStatusIcons.getParent()).removeView(mStatusBarStatusIcons);
                firstRight.addView(mStatusBarStatusIcons);
                int resSystemIconsId = sbView.getResources().getIdentifier("system_icons", "id", lpparam.packageName);
                firstRight.setId(resSystemIconsId);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateCutoutLocation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.thisObject, "mCurrentStatusBarType");
                LinearLayout leftContainer = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, "mStatusBarLeftContainer");
                LinearLayout leftLayout = (LinearLayout) leftContainer.getParent();
                LinearLayout rightLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.thisObject, "rightLayout");
                LinearLayout rightContainer = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.thisObject, "firstRight");
                LinearLayout secondLeft = (LinearLayout) XposedHelpers.getAdditionalInstanceField(param.thisObject, "secondLeft");
                LinearLayout secondRight = (LinearLayout) rightLayout.getChildAt(1);
                LinearLayout statusBarcontents = (LinearLayout) leftLayout.getParent();
                boolean moveSignalLeft = MainModule.mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft");

                statusBarcontents.setOrientation(mCurrentStatusBarType == 0 ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
                if (mCurrentStatusBarType == 0) {
                    if (leftLayout.getChildAt(1) != rightContainer) {
                        leftLayout.removeViewAt(1);
                        rightLayout.removeViewAt(0);
                        leftLayout.addView(rightContainer);
                        rightLayout.addView(secondLeft, 0);
                    }
                    leftLayout.setOrientation(LinearLayout.HORIZONTAL);
                    rightLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams leftLayoutLp = new LinearLayout.LayoutParams(-1, 0, 1);
                    leftLayout.setLayoutParams(leftLayoutLp);
                    LinearLayout.LayoutParams rightLayoutLp = new LinearLayout.LayoutParams(-1, 0, 1);
                    rightLayout.setLayoutParams(rightLayoutLp);

                    LinearLayout.LayoutParams firstRowlp = new LinearLayout.LayoutParams(-2, -1, 0);
                    leftContainer.setLayoutParams(firstRowlp);
                    firstRowlp = new LinearLayout.LayoutParams(0, -1, 1);
                    rightContainer.setLayoutParams(firstRowlp);

                    LinearLayout.LayoutParams secondRowLp = new LinearLayout.LayoutParams(0, -1, 1);
                    secondLeft.setLayoutParams(secondRowLp);
                    secondRight.setLayoutParams(secondRowLp);
                }
                else {
                    if (leftLayout.getChildAt(1) != secondLeft) {
                        leftLayout.removeViewAt(1);
                        rightLayout.removeViewAt(0);
                        leftLayout.addView(secondLeft);
                        rightLayout.addView(rightContainer, 0);
                    }
                    Object mMiuiEndIconManager = XposedHelpers.getObjectField(param.thisObject, "mMiuiEndIconManager");
                    XposedHelpers.callMethod(mMiuiEndIconManager, "setDripEnd", false);
                    Object mDripNetworkSpeedView = XposedHelpers.getObjectField(param.thisObject, "mDripNetworkSpeedView");
                    XposedHelpers.callMethod(mDripNetworkSpeedView, "setBlocked", true);
                    View mDripStatusBarLeftStatusIconArea = (View) XposedHelpers.getObjectField(param.thisObject, "mDripStatusBarLeftStatusIconArea");
                    mDripStatusBarLeftStatusIconArea.setVisibility(moveSignalLeft ? View.VISIBLE : View.GONE);
                    leftLayout.setOrientation(LinearLayout.VERTICAL);
                    rightLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams leftLayoutLp = new LinearLayout.LayoutParams(0, -1, 1);
                    leftLayout.setLayoutParams(leftLayoutLp);
                    LinearLayout.LayoutParams rightLayoutLp = new LinearLayout.LayoutParams(0, -1, 1);
                    rightLayout.setLayoutParams(rightLayoutLp);

                    LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(-1, 0, 1);
                    leftContainer.setLayoutParams(leftLp);
                    secondLeft.setLayoutParams(leftLp);

                    LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(-1, 0, 1);
                    rightContainer.setLayoutParams(rightLp);
                    secondRight.setLayoutParams(rightLp);
                }
                secondLeft.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "showSystemIconArea", boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mStatusBar = XposedHelpers.getObjectField(param.thisObject, "mStatusBar");
                View rightLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "rightLayout");
                View leftLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "leftLayout");
                leftLayout.setVisibility(LinearLayout.VISIBLE);
                rightLayout.setVisibility(LinearLayout.VISIBLE);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "hideSystemIconArea", boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mStatusBar = XposedHelpers.getObjectField(param.thisObject, "mStatusBar");
                View rightLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "rightLayout");
                View leftLayout = (View) XposedHelpers.getAdditionalInstanceField(mStatusBar, "leftLayout");
                leftLayout.setVisibility(LinearLayout.GONE);
                rightLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }

    public static void DualRowSignalHook(LoadPackageParam lpparam) {
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

        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    Resources modRes = Helpers.getModuleRes(mContext);
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
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone." + ControllerImplName, lpparam.classLoader, "setMobileIcons", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    Resources res = mContext.getResources();
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_0", "drawable", lpparam.packageName), 0);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_1", "drawable", lpparam.packageName), 1);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_2", "drawable", lpparam.packageName), 2);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_3", "drawable", lpparam.packageName), 3);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_4", "drawable", lpparam.packageName), 4);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_5", "drawable", lpparam.packageName), 5);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_null", "drawable", lpparam.packageName), 6);
                }
                List<?> iconStates = (List<?>) param.args[1];
                if (iconStates.size() == 2) {
                    Object mainIconState = iconStates.get(0);
                    Object subIconState = iconStates.get(1);
                    boolean mainDataConnected = (boolean) XposedHelpers.getObjectField(mainIconState, "dataConnected");
                    boolean subDataConnected = (boolean) XposedHelpers.getObjectField(subIconState, "dataConnected");
                    XposedHelpers.setObjectField(mainIconState, "dataConnected", mainDataConnected || subDataConnected);
                    XposedHelpers.setObjectField(subIconState, "visible", false);
                    int mainSignalResId = (int) XposedHelpers.getObjectField(mainIconState, "strengthId");
                    int subSignalResId = (int) XposedHelpers.getObjectField(subIconState, "strengthId");
                    int mainLevel = signalResToLevelMap.get(mainSignalResId);
                    int subLevel = signalResToLevelMap.get(subSignalResId);
                    int level;
                    if (subDataConnected) {
                        level = subLevel * 10 + mainLevel;
                        String showName = (String) XposedHelpers.getObjectField(subIconState, "showName");
                        XposedHelpers.setObjectField(mainIconState, "showName", showName);
                    }
                    else {
                        level = mainLevel * 10 + subLevel;
                    }
                    XposedHelpers.setObjectField(mainIconState, "strengthId", level);
                    param.args[1] = iconStates;
                }
            }
        });

        MethodHook beforeUpdate = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mobileIconState = param.args[0];
                boolean visible = (boolean) XposedHelpers.getObjectField(mobileIconState, "visible");
                boolean airplane = (boolean) XposedHelpers.getObjectField(mobileIconState, "airplane");
                int level = (int) XposedHelpers.getObjectField(mobileIconState, "strengthId");
                if (!visible || airplane || level == 0 || level > 100) {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "subStrengthId", -1);
                }
                else {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "subStrengthId", level % 10);
                    XposedHelpers.setObjectField(mobileIconState, "fiveGDrawableId", 0);
                }
            }
        };
        MethodHook afterUpdate = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                int subStrengthId = (int) XposedHelpers.getAdditionalInstanceField(param.thisObject, "subStrengthId");
                if (subStrengthId < 0) return;
                Object mSmallHd = XposedHelpers.getObjectField(param.thisObject, "mSmallHd");
                XposedHelpers.callMethod(mSmallHd, "setVisibility", 8);
                Object mSmallRoaming = XposedHelpers.getObjectField(param.thisObject, "mSmallRoaming");
                XposedHelpers.callMethod(mSmallRoaming, "setVisibility", 0);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", beforeUpdate);
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", afterUpdate);

        MethodHook resetImageDrawable = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                int subStrengthId = (int) XposedHelpers.getAdditionalInstanceField(param.thisObject, "subStrengthId");
                if (subStrengthId < 0) return;
                if (subStrengthId == 6) subStrengthId = 0;
                Object mobileIconState = XposedHelpers.getObjectField(param.thisObject, "mState");
                int level1 = (int) XposedHelpers.getObjectField(mobileIconState, "strengthId");
                level1 = level1 / 10;
                if (level1 == 6) level1 = 0;
                boolean mLight = (boolean) XposedHelpers.getObjectField(param.thisObject, "mLight");
                boolean mUseTint = (boolean) XposedHelpers.getObjectField(param.thisObject, "mUseTint");
                Object mSmallRoaming = XposedHelpers.getObjectField(param.thisObject, "mSmallRoaming");
                Object mMobile = XposedHelpers.getObjectField(param.thisObject, "mMobile");
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
        Helpers.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyDarknessInternal", resetImageDrawable);
        int rightMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_rightmargin", 0);
        int leftMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_leftmargin", 0);
        int iconScale = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_scale", 10);
        if (rightMargin > 0 || leftMargin > 0 || iconScale != 10) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "init", new MethodHook() {
                @Override
                protected void after(final MethodHookParam param) throws Throwable {
                    LinearLayout mobileView = (LinearLayout) param.thisObject;
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

                    if (iconScale != 10) {
                        View mMobile = (View) XposedHelpers.getObjectField(param.thisObject, "mMobile");
                        View mSmallRoaming = (View) XposedHelpers.getObjectField(param.thisObject, "mSmallRoaming");
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMobile.getLayoutParams();
                        int mIconHeight = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            20 * iconScale / 10f,
                            res.getDisplayMetrics()
                        );
                        Helpers.log("fad " + (layoutParams != null));
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

    public static void StatusBarIconsPositionAdjustHook(LoadPackageParam lpparam, boolean moveRight) {
        boolean swapWifiSignal = MainModule.mPrefs.getBoolean("system_statusbaricons_swap_wifi_mobile");
        boolean moveSignalLeft = MainModule.mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft");
        String[] signalIcons;
        if (!swapWifiSignal) {
            signalIcons = new String[]{"no_sim", "mobile", "demo_mobile", "airplane", "hotspot", "slave_wifi", "wifi", "demo_wifi"};
        }
        else {
            signalIcons = new String[]{"hotspot", "slave_wifi", "wifi", "demo_wifi", "no_sim", "mobile", "demo_mobile", "airplane"};
        }
        ArrayList<String> signalRelatedIcons = new ArrayList<String>(Arrays.asList(signalIcons));
        if (moveRight) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    String slot = (String) param.args[0];
                    if (("alarm_clock".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_alarm_atright"))
                        || ("volume".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_sound_atright"))
                        || ("zen".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_dnd_atright"))
                        || ("nfc".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_nfc_atright"))
                        || ("headset".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_headset_atright"))
                    ) {
                        param.args[1] = false;
                    }
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
                private boolean isHooked = false;

                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    if (!isHooked) {
                        isHooked = true;
                        Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                        Class<?> MiuiEndIconManager = findClass("com.android.systemui.statusbar.phone.MiuiEndIconManager", lpparam.classLoader);
                        Object blockList = Helpers.getStaticObjectFieldSilently(MiuiEndIconManager, "RIGHT_BLOCK_LIST");
                        ArrayList<String> rightBlockList;
                        Resources res = mContext.getResources();
                        if (blockList != null) {
                            rightBlockList = (ArrayList<String>) blockList;
                        }
                        else {
                            int blockResId = res.getIdentifier("config_drip_right_block_statusBarIcons", "array", lpparam.packageName);
                            rightBlockList = new ArrayList<String>(Arrays.asList(res.getStringArray(blockResId)));
                        }
                        if (MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atright")) {
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
                        if (blockList != null) {
                            XposedHelpers.setStaticObjectField(MiuiEndIconManager, "RIGHT_BLOCK_LIST", rightBlockList);
                        }
                        else {
                            MainModule.resHooks.setObjectReplacement(lpparam.packageName, "array", "config_drip_right_block_statusBarIcons", rightBlockList.toArray(new String[0]));
                        }
                    }
                }
            });
        }
        ArrayList<String> dripLeftIcons = new ArrayList<String>();
        if (swapWifiSignal || moveSignalLeft) {
            Helpers.findAndHookConstructor("com.android.systemui.statusbar.phone.StatusBarIconList", lpparam.classLoader, String[].class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    boolean isRightController = "StatusBarIconControllerImpl".equals(param.thisObject.getClass().getSimpleName());
                    ArrayList<String> allStatusIcons = new ArrayList<String>(Arrays.asList((String[]) param.args[0]));
                    if (isRightController) {
                        int startIndex = allStatusIcons.indexOf("no_sim");
                        int endIndex = allStatusIcons.indexOf("demo_wifi") + 1;
                        List<String> removedIcons = allStatusIcons.subList(startIndex, endIndex);
                        removedIcons.clear();
                        if (!moveSignalLeft) {
                            startIndex = allStatusIcons.indexOf("ethernet");
                            allStatusIcons.addAll(startIndex + 1, signalRelatedIcons);
                        }
                        param.args[0] = allStatusIcons.toArray(new String[0]);
                    }
                    else if (moveSignalLeft) {
                        dripLeftIcons.addAll(allStatusIcons);
                        allStatusIcons.addAll(0, signalRelatedIcons);
                        param.args[0] = allStatusIcons.toArray(new String[0]);
                    }
                }
            });
        }

        if (moveSignalLeft) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiStatusBarSignalPolicy", lpparam.classLoader, "initMiuiSlot", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Object dripLeftController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader));
                    XposedHelpers.setObjectField(param.thisObject, "mIconController", dripLeftController);
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object dripLeftController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader));
                    Object mDripIconManager = XposedHelpers.getObjectField(param.thisObject, "mDripLeftDarkIconManager");
                    ArrayList<String> blockList = new ArrayList<String>();
                    int mCurrentStatusBarType = (int) XposedHelpers.getAdditionalInstanceField(dripLeftController, "mCurrentStatusBarType");
                    if (mCurrentStatusBarType != 1) {
                        blockList.addAll(dripLeftIcons);
                    }
                    XposedHelpers.callMethod(mDripIconManager, "setBlockList", blockList);
                    XposedHelpers.callMethod(dripLeftController, "refreshIconGroup", mDripIconManager);
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "setStatusBarType", int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    int mCurrentStatusBarType = XposedHelpers.getIntField(param.thisObject, "mCurrentStatusBarType");
                    Object dripLeftController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader));
                    XposedHelpers.setAdditionalInstanceField(dripLeftController, "mCurrentStatusBarType", mCurrentStatusBarType);
                }
            });
        }

        boolean netspeedRight = MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atright");
        if (moveSignalLeft || netspeedRight) {
            Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateCutoutLocation", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.thisObject, "mCurrentStatusBarType");
                    if (mCurrentStatusBarType == 1) {
                        if (netspeedRight) {
                            Object mDripNetworkSpeedView = XposedHelpers.getObjectField(param.thisObject, "mDripNetworkSpeedView");
                            XposedHelpers.callMethod(mDripNetworkSpeedView, "setBlocked", true);
                        }
                    }
                    else {
                        boolean dualRows = MainModule.mPrefs.getBoolean("system_statusbar_dualrows");
                        if (moveSignalLeft && !dualRows) {
                            View mDripStatusBarLeftStatusIconArea = (View) XposedHelpers.getObjectField(param.thisObject, "mDripStatusBarLeftStatusIconArea");
                            mDripStatusBarLeftStatusIconArea.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        if (netspeedRight) {
            Helpers.hookAllMethods("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, "setDripNetworkSpeedView", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.args[0] = null;
                }
            });
        }
    }

    public static void StatusBarClockAtRightHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                FrameLayout sbView = (FrameLayout) param.thisObject;
                Context mContext = sbView.getContext();
                Resources res = mContext.getResources();
                TextView mClockView = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMiuiClock");
                ((ViewGroup)mClockView.getParent()).removeView(mClockView);
                int contentId = res.getIdentifier("status_bar_contents", "id", lpparam.packageName);
                LinearLayout mContentsContainer = sbView.findViewById(contentId);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                mContentsContainer.addView(mClockView, lp);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateCutoutLocation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.thisObject, "mCurrentStatusBarType");
                if (mCurrentStatusBarType == 0) {
                    View mSystemIconArea = (View) XposedHelpers.getObjectField(param.thisObject, "mSystemIconArea");
                    LinearLayout.LayoutParams mSystemIconAreaLp = (LinearLayout.LayoutParams) mSystemIconArea.getLayoutParams();
                    mSystemIconAreaLp.width = 0;
                    mSystemIconAreaLp.weight = 1.0f;
                }
            }
        });
    }

    public static void NoNetworkSpeedSeparatorHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedSplitter", lpparam.classLoader, "updateVisibility", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                XposedHelpers.setObjectField(param.thisObject, "mNetworkSpeedVisibility", View.GONE);
            }
        });
    }

    public static void HideNetworkSpeedUnitHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, "formatSpeed", Context.class, long.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                String speedText = (String) param.getResult();
                param.setResult(speedText.replaceFirst("B?[/']s", ""));
            }
        });
    }

    public static void HideLowNetworkSpeedHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, "formatSpeed", Context.class, long.class, new MethodHook(100) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int lowLevel = MainModule.mPrefs.getInt("system_detailednetspeed_lowlevel", 1) * 1024;
                long speedVal = (long) param.args[1];
                if (speedVal < lowLevel) {
                    param.setResult("");
                }
            }
        });
    }

    public static void NetSpeedStyleHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                TextView meter = (TextView)param.thisObject;
                if (meter == null) return;
                if (meter.getTag() == null || !"slot_text_icon".equals(meter.getTag())) {
                    int fontSize = MainModule.mPrefs.getInt("system_netspeed_fontsize", 13);
                    if (MainModule.mPrefs.getBoolean("system_detailednetspeed")) {
                        if (fontSize > 20) fontSize = 16;
                    }
                    else {
                        if (fontSize < 20) fontSize = 27;
                    }
                    if (fontSize != 13) {
                        meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f);
                    }
                    if (MainModule.mPrefs.getBoolean("system_netspeed_bold")) {
                        meter.setTypeface(Typeface.DEFAULT_BOLD);
                    }

                    int horizMargin = 0;
                    if (MainModule.mPrefs.getBoolean("system_fixmeter")) {
                        horizMargin = Math.round(meter.getResources().getDisplayMetrics().density * 4);
                    }
                    int topMargin = 0;
                    int verticalOffset = MainModule.mPrefs.getInt("system_netspeed_verticaloffset", 8);
                    if (verticalOffset != 8) {
                        float marginTop = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            (verticalOffset - 8) * 0.5f,
                            meter.getResources().getDisplayMetrics()
                        );
                        topMargin = (int) (marginTop);
                    }
                    meter.setPaddingRelative(horizMargin, topMargin, horizMargin, 0);

                    if (MainModule.mPrefs.getBoolean("system_detailednetspeed")) {
                        float spacing = 0.9f;
                        meter.setSingleLine(false);
                        meter.setLines(2);
                        meter.setMaxLines(2);
                        if (fontSize > 8.5f) {
                            spacing = 0.85f;
                        }
                        meter.setLineSpacing(0, spacing);
                    }
                }
            }
        });
    }
    public static void MobileTypeSingleHook(LoadPackageParam lpparam) {
        MethodHook showSingleMobileType = new MethodHook(MethodHook.PRIORITY_HIGHEST) {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mobileIconState = param.args[0];
                XposedHelpers.setObjectField(mobileIconState, "showMobileDataTypeSingle", true);
                XposedHelpers.setObjectField(mobileIconState, "fiveGDrawableId", 0);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", showSingleMobileType);

        MethodHook afterUpdate = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object mMobileLeftContainer = XposedHelpers.getObjectField(param.thisObject, "mMobileLeftContainer");
                XposedHelpers.callMethod(mMobileLeftContainer, "setVisibility", 8);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", afterUpdate);

        Helpers.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "init", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                Resources res = mContext.getResources();
                LinearLayout mMobileGroup = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, "mMobileGroup");
                TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMobileTypeSingle");
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
        Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "computeTimeoutH", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean mHovering = XposedHelpers.getBooleanField(param.thisObject, "mHovering");
                if (mHovering) {
                    param.setResult(16000);
                    return;
                }
                boolean mSafetyWarning;
                try {
                    mSafetyWarning = (boolean) XposedHelpers.getObjectField(param.thisObject, "mIsSafetyShowing");
                }
                catch (Throwable e) {
                    mSafetyWarning = (boolean) XposedHelpers.getObjectField(param.thisObject, "mSafetyWarning");
                }
                if (mSafetyWarning) {
                    int opt = MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0);
                    param.setResult(opt > 0 ? opt : 5000);
                    return;
                }
                boolean mExpanded = XposedHelpers.getBooleanField(param.thisObject, "mExpanded");
                int opt = MainModule.mPrefs.getInt(mExpanded ? "system_volumedialogdelay_expanded" : "system_volumedialogdelay_collapsed", 0);
                if (opt > 0) param.setResult(opt);
            }
        });
    }

    private static float blurCollapsed = 0.0f;
    private static float blurExpanded = 0.0f;

    public static void BlurVolumeDialogBackgroundHook(ClassLoader classLoader) {
        MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "fraction", "miui_volume_dim_behind_collapsed", 0f);
        MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "fraction", "miui_volume_dim_behind_expanded", 0f);

        Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "updateDialogWindowH", boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean mExpanded = XposedHelpers.getBooleanField(param.thisObject, "mExpanded");
                float blurRatio = blurCollapsed;
                boolean isVisible = (boolean) param.args[0];
                if (mExpanded && !isVisible) {
                    blurRatio = blurExpanded;
                }
                if (!mExpanded && blurCollapsed > 0.001f) {
                    Window mWindow = (Window) XposedHelpers.getObjectField(param.thisObject, "mWindow");
                    mWindow.clearFlags(8);
                }
                if (mExpanded) {
                    XposedHelpers.callMethod(param.thisObject, "startBlurAnim", 0f, blurRatio, 0);
                }
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "showH", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (blurCollapsed > 0.001f) {
                    Window mWindow = (Window) XposedHelpers.getObjectField(param.thisObject, "mWindow");
                    mWindow.clearFlags(8);
                    XposedHelpers.callMethod(param.thisObject, "startBlurAnim", 0f, blurCollapsed, 0);
                }
            }
        });
        Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "initDialog", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = new Handler(mContext.getMainLooper());

                blurCollapsed = MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) / 100f;
                blurExpanded = MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) / 100f;
                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if (key.equals("pref_key_system_volumeblur_collapsed")) blurCollapsed = Helpers.getSharedIntPref(mContext, key, 0) / 100f;
                            if (key.equals("pref_key_system_volumeblur_expanded")) blurExpanded = Helpers.getSharedIntPref(mContext, key, 0) / 100f;
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });
    }

    public static void BlurMTKVolumeBarHook(ClassLoader classLoader) {
        Helpers.findAndHookMethod("com.android.systemui.miui.volume.Util", classLoader, "isSupportBlurS", XC_MethodReplacement.returnConstant(true));
    }

    public static void MIUIVolumeDialogHook(LoadPackageParam lpparam) {
        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    Class<?> MiuiVolumeDialogImpl = XposedHelpers.findClassIfExists("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader);
                    if (MainModule.mPrefs.getBoolean("system_separatevolume") && MainModule.mPrefs.getBoolean("system_separatevolume_slider")) {
                        Helpers.hookAllMethods(MiuiVolumeDialogImpl, "addColumn", new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) throws Throwable {
                                if (param.args.length != 4) return;
                                int streamType = (int) param.args[0];
                                if (streamType == 4) {
                                    XposedHelpers.callMethod(param.thisObject, "addColumn", 5, notifVolumeOnResId, notifVolumeOffResId, true, false);
                                }
                            }
                        });
                    }
                    if (MainModule.mPrefs.getBoolean("system_nosilentvibrate")) {
                        Helpers.hookAllMethods(MiuiVolumeDialogImpl, "vibrateH", XC_MethodReplacement.DO_NOTHING);
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
                        Helpers.findAndHookMethod("miui.systemui.util.SystemUIResourcesHelperImpl", pluginLoader, "getBoolean", String.class, new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) throws Throwable {
                                String key = (String) param.args[0];
                                if (key.equals("header_big_time_use_system_font")) {
                                    param.setResult(Boolean.TRUE);
                                }
                            }
                        });
                    }
                    if (MainModule.mPrefs.getBoolean("system_qsnolabels")) {
                        HideCCLabelsHook(pluginLoader);
                    }
                }
            }
        });
    }

    private static float scaledTileWidthDim = -1f;
    public static void SystemCCGridHook(LoadPackageParam lpparam) {
        int cols = MainModule.mPrefs.getInt("system_ccgridcolumns", 4);
        int rows = MainModule.mPrefs.getInt("system_ccgridrows", 4);
        if (cols > 4) {
            MainModule.resHooks.setObjectReplacement(lpparam.packageName, "dimen", "qs_control_tiles_columns", cols);
        }

        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    Resources res = mContext.getResources();
                    float density = res.getDisplayMetrics().density;
                    int tileWidthResId = res.getIdentifier("qs_control_center_tile_width", "dimen", "com.android.systemui");
                    float tileWidthDim = res.getDimension(tileWidthResId);
                    if (cols > 4) {
                        tileWidthDim = tileWidthDim / density;
                        scaledTileWidthDim = tileWidthDim * 4 / cols;
                        MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "qs_control_center_tile_width", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_control_center_tile_width", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "qs_control_tile_icon_bg_size", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_control_tile_icon_bg_size", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_cell_height", 85f);
                    }
                }
            }
        });

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    if (cols > 4) {
                        Helpers.findAndHookConstructor("miui.systemui.controlcenter.qs.QSPager", pluginLoader, Context.class, AttributeSet.class, new MethodHook() {
                            @Override
                            protected void after(MethodHookParam param) throws Throwable {
                                XposedHelpers.setObjectField(param.thisObject, "columns", cols);
                            }
                        });
                        if (!MainModule.mPrefs.getBoolean("system_qsnolabels")) {
                            Helpers.hookAllMethods("miui.systemui.controlcenter.qs.tileview.StandardTileView", pluginLoader, "handleStateChanged", new MethodHook() {
                                @Override
                                protected void after(MethodHookParam param) throws Throwable {
                                    Object label = XposedHelpers.getObjectField(param.thisObject, "label");
                                    if (label != null) {
                                        TextView lb = (TextView) label;
                                        lb.setMaxLines(1);
                                        lb.setSingleLine(true);
                                        lb.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                        lb.setMarqueeRepeatLimit(0);
                                    }
                                }
                            });
                        }
                    }
                    if (rows != 4) {
                        Helpers.findAndHookMethod("miui.systemui.controlcenter.qs.QSPager", pluginLoader, "distributeTiles", new MethodHook() {
                            @Override
                            protected void after(MethodHookParam param) throws Throwable {
                                boolean collapse = (boolean) XposedHelpers.getObjectField(param.thisObject, "collapse");
                                if (collapse) {
                                    ArrayList<Object> pages = (ArrayList<Object>) XposedHelpers.getObjectField(param.thisObject, "pages");
                                    for (Object tileLayoutImpl : pages) {
                                        XposedHelpers.callMethod(tileLayoutImpl, "removeTiles");
                                    }
                                    ArrayList<Object> pageTiles = new ArrayList<Object>();
                                    int currentRow = 2;
                                    ArrayList<?> records = (ArrayList<?>) XposedHelpers.getObjectField(param.thisObject, "records");
                                    Iterator<?> it2 = records.iterator();
                                    int i3 = 0;
                                    int pageNow = 0;
                                    Object bigHeader = XposedHelpers.getObjectField(param.thisObject, "header");
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
                                            int totalRows = (int) XposedHelpers.getObjectField(param.thisObject, "rows");
                                            if (currentRow > totalRows) {
                                                XposedHelpers.setObjectField(param.thisObject, "rows", currentRow);
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
                                    Object pageIndicator = XposedHelpers.getObjectField(param.thisObject, "pageIndicator");
                                    if (pageIndicator != null) {
                                        XposedHelpers.callMethod(pageIndicator, "setNumPages", pages.size());
                                    }
                                    Object adapter = XposedHelpers.getObjectField(param.thisObject, "adapter");
                                    XposedHelpers.callMethod(param.thisObject, "setAdapter", adapter);
//                                    XposedHelpers.callMethod(param.thisObject, "notifyDataSetChanged");
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
        Class<?> QSController = XposedHelpers.findClassIfExists("miui.systemui.controlcenter.qs.tileview.StandardTileView", pluginLoader);
        Helpers.hookAllMethods(QSController, "init", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.args.length != 1) return;
                View mLabelContainer = (View)XposedHelpers.getObjectField(param.thisObject, "labelContainer");
                if (mLabelContainer != null) {
                    mLabelContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void QSGridLabelsHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.qs.MiuiTileLayout", lpparam.classLoader, "addTile", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                updateLabelsVisibility(param.args[0], XposedHelpers.getIntField(param.thisObject, "mRows"), ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.qs.MiuiPagedTileLayout", lpparam.classLoader, "addTile", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void before(MethodHookParam param) throws Throwable {
                ArrayList<Object> mPages = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mPages");
                if (mPages == null) return;
                int mRows = 0;
                if (mPages.size() > 0) mRows = XposedHelpers.getIntField(mPages.get(0), "mRows");
                updateLabelsVisibility(param.args[0], mRows, ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.qs.MiuiTileLayout", lpparam.classLoader, "updateResources", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getInt("system_qsgridrows", 1) != 2) return;
                if (!(boolean)param.getResult()) return;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) return;
                XposedHelpers.setIntField(param.thisObject, "mCellHeight", Math.round(XposedHelpers.getIntField(param.thisObject, "mCellHeight") / 1.5f));
                ((ViewGroup)param.thisObject).requestLayout();
            }
        });

        if (MainModule.mPrefs.getInt("system_qsgridrows", 1) == 4)
            Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.MiuiQSTileView", lpparam.classLoader, "createLabel", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    ViewGroup mLabelContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mLabelContainer");
                    if (mLabelContainer != null) mLabelContainer.setPadding(
                        mLabelContainer.getPaddingLeft(),
                        Math.round(mLabelContainer.getResources().getDisplayMetrics().density * 2),
                        mLabelContainer.getPaddingRight(),
                        mLabelContainer.getPaddingBottom()
                    );
                }
            });
    }

    public static void VolumeTimerValuesRes(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "array", "miui_volume_timer_segments", R.array.miui_volume_timer_segments);

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "initTimerString", new MethodHook() {
                        @Override
                        protected void after(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
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
                            XposedHelpers.setObjectField(param.thisObject, "mTimeSegmentTitle", mTimeSegmentTitle);
                        }
                    });
                    Helpers.findAndHookMethod("com.android.systemui.miui.volume.TimerItem", pluginLoader, "getTimePos", int.class, new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Object timer = XposedHelpers.getObjectField(param.thisObject, "mTimerTime");
                            float halfTimerWidth = ((int) XposedHelpers.callMethod(timer, "getWidth")) / 2.0f;
                            Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            float seekWidth = mContext.getResources().getDimension(mContext.getResources().getIdentifier("miui_volume_timer_seelbar_width", "dimen", "miui.systemui.plugin"));
                            int marginLeft = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("miui_volume_timer_seekbar_margin_left", "dimen", "miui.systemui.plugin"));
                            int seg = (int) XposedHelpers.getObjectField(param.thisObject, "mDeterminedSegment");
                            param.setResult(seekWidth / 10 * seg + marginLeft - halfTimerWidth);
                        }
                    });
                }
            }
        });
    }

    public static void CCTileCornerHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_unavailable", R.drawable.ic_qs_tile_bg_disabled);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_disabled", R.drawable.ic_qs_tile_bg_disabled);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_warning", R.drawable.ic_qs_tile_bg_warning);

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    Helpers.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.ExpandableIconView", pluginLoader, "setCornerRadius", float.class, new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getPluginContext");
                            float radius = 18;
                            if (scaledTileWidthDim > 0) {
                                radius *= scaledTileWidthDim / 65;
                            }
                            param.args[0] = mContext.getResources().getDisplayMetrics().density * radius;
                        }
                    });

                    Helpers.findAndHookMethod("miui.systemui.dagger.PluginComponentFactory", pluginLoader, "create", Context.class, new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) param.args[0];
                            int enabledTileBackgroundResId = mContext.getResources().getIdentifier("qs_background_enabled", "drawable", "miui.systemui.plugin");
                            int enabledTileColorResId = mContext.getResources().getIdentifier("qs_enabled_color", "color", "miui.systemui.plugin");
                            Helpers.findAndHookMethod("android.content.res.Resources", pluginLoader, "getDrawable", int.class, new MethodHook() {
                                @Override
                                protected void before(MethodHookParam param) throws Throwable {
                                    int resId = (int) param.args[0];
                                    if (resId == enabledTileBackgroundResId && resId != 0) {
                                        Resources modRes = Helpers.getModuleRes(mContext);
                                        Drawable enableTile = modRes.getDrawable(R.drawable.ic_qs_tile_bg_enabled, null);
                                        enableTile.setTint(mContext.getResources().getColor(enabledTileColorResId, null));
                                        param.setResult(enableTile);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private static boolean isSlidingStart = false;
    private static boolean isSliding = false;
    private static float tapStartX = 0;
    private static float tapStartY = 0;
    private static float tapStartPointers = 0;
    private static float tapStartBrightness = 0;
    private static float tapCurrentBrightness = 0;
    private static float topMinimumBacklight = 0.0f;
    private static float topMaximumBacklight = 1.0f;
    private static float currentTouchX = 0;
    private static long currentTouchTime = 0;

    public static void StatusBarGesturesHook(LoadPackageParam lpparam) {

        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if ("pref_key_system_statusbarcontrols_single".equals(key) || "pref_key_system_statusbarcontrols_dual".equals(key))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, "1"));
                            else if ("pref_key_system_statusbarcontrols_sens_bright".equals(key) || "pref_key_system_statusbarcontrols_sens_vol".equals(key))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, "2"));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "setExpandedHeightInternal", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                float mExpandedFraction = (float) XposedHelpers.callMethod(param.thisObject, "getExpandedFraction");
                if (mExpandedFraction > 0.33f) {
                    currentTouchTime = 0;
                    currentTouchX = 0;
                }
            }
        });

        MethodHook hook = new MethodHook() {
            Object mBrightnessController = null;
            private int sbHeight = 0;
            @Override
            @SuppressLint("SetTextI18n")
            protected void before(final MethodHookParam param) throws Throwable {
                String clsName = param.thisObject.getClass().getSimpleName();
                boolean isInControlCenter = "ControlPanelWindowView".equals(clsName) || "ControlCenterWindowViewImpl".equals(clsName);
                if (Helpers.isTPlus() && isInControlCenter) {
                    if (param.args.length == 2 && (boolean) param.args[1]) {
                        return ;
                    }
                    Object statusBarStateController = XposedHelpers.getObjectField(param.thisObject, "statusBarStateController");
                    int state = (int) XposedHelpers.callMethod(statusBarStateController, "getState");
                    if (state == 1 || state == 2) {
                        return;
                    }
                }
                Context mContext = isInControlCenter ? ((View)param.thisObject).getContext() : (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Resources res = mContext.getResources();
                if (sbHeight == 0) {
                    sbHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
                }
                MotionEvent event = (MotionEvent)param.args[0];
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        tapStartX = event.getX();
                        tapStartY = event.getY();
                        isSlidingStart = isInControlCenter ? tapStartY <= sbHeight : !XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
                        tapStartPointers = 1;
                        if (mBrightnessController == null) {
                            Object mControlCenterController;
                            if (Helpers.isTPlus() && isInControlCenter) {
                                mControlCenterController = XposedHelpers.getObjectField(param.thisObject, "controlCenterController");
                            }
                            else {
                                mControlCenterController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClassIfExists("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.classLoader));
                            }
                            mBrightnessController = XposedHelpers.callMethod(XposedHelpers.getObjectField(mControlCenterController, "brightnessController"), "get");
                        }
                        Object mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
                        int mDisplayId = mContext.getDisplay().getDisplayId();
                        topMinimumBacklight = (float) XposedHelpers.getObjectField(mBrightnessController, "mMinimumBacklight");
                        topMaximumBacklight = (float) XposedHelpers.getObjectField(mBrightnessController, "mMaximumBacklight");
                        tapStartBrightness = (float) XposedHelpers.callMethod(mDisplayManager, "getBrightness", mDisplayId);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        tapStartPointers = event.getPointerCount();
                        break;
                    case MotionEvent.ACTION_UP:
                        long lastTouchTime = currentTouchTime;
                        float lastTouchX = currentTouchX;
                        currentTouchTime = currentTimeMillis();
                        currentTouchX = event.getX();
                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 100F) {
                            currentTouchTime = 0L;
                            currentTouchX = 0F;
                            GlobalActions.handleAction(mContext, "pref_key_system_statusbarcontrols_dt");
                        }
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isSlidingStart = false;
                        isSliding = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isSlidingStart) return;
                        DisplayMetrics metrics = res.getDisplayMetrics();
                        if (event.getY() - tapStartY > sbHeight) return;
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
                            tapCurrentBrightness = nextLevel;
                        } else if (opt == 3) {
                            tapCurrentBrightness = -1.0f;
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
        String eventMethod = Helpers.isTPlus() ? "onTouchEvent" : "interceptTouchEvent";
        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, eventMethod, MotionEvent.class, hook);
        if (Helpers.isTPlus()) {
            String pluginLoaderClass = "com.android.systemui.shared.plugins.PluginInstance$Factory";
            Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                    if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                        isHooked = true;
                        if (pluginLoader == null) {
                            pluginLoader = (ClassLoader) param.getResult();
                        }
                        Helpers.findAndHookMethod("miui.systemui.controlcenter.windowview.ControlCenterWindowViewImpl", pluginLoader, "handleMotionEvent", MotionEvent.class, boolean.class, hook);
                    }
                }
            });
        }
        else {
            Helpers.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, hook);
        }
    }

    public static void HorizMarginHook(LoadPackageParam lpparam) {
        MethodHook horizHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context context;
                if (param.method.getName().equals("paddingNeededForCutoutAndRoundedCorner")) {
                    context = Helpers.findContext();
                }
                else {
                    context = (Context) XposedHelpers.getObjectField(param.thisObject, "context");
                }
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_left", 16);
                float marginLeft = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMargin,
                    context.getResources().getDisplayMetrics()
                );
                leftMargin = (int) marginLeft;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_right", 16);
                float marginRight = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    rightMargin,
                    context.getResources().getDisplayMetrics()
                );
                rightMargin = (int) marginRight;
                param.setResult(new Pair<Integer, Integer>(Integer.valueOf(leftMargin), Integer.valueOf(rightMargin)));
            }
        };
        String StatusBarWindowViewCls = Helpers.isTPlus() ? "com.android.systemui.statusbar.window.StatusBarWindowView" : "com.android.systemui.statusbar.phone.StatusBarWindowView";
        Helpers.hookAllMethods(StatusBarWindowViewCls, lpparam.classLoader, "paddingNeededForCutoutAndRoundedCorner", horizHook);
        if (Helpers.isTPlus()) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarContentInsetsProvider", lpparam.classLoader, "getStatusBarContentInsetsForCurrentRotation", horizHook);
        }
    }

    public static void LockScreenTopMarginHook(LoadPackageParam lpparam) {
        final int[] statusBarPaddingTop = new int[1];
        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                int dimenResId = mContext.getResources().getIdentifier("status_bar_padding_top", "dimen", lpparam.packageName);
                statusBarPaddingTop[0] = mContext.getResources().getDimensionPixelSize(dimenResId);
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.classLoader, "updateViewStatusBarPaddingTop", View.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View view = (View) param.args[0];
                if (view != null) {
                    view.setPadding(view.getPaddingLeft(), statusBarPaddingTop[0], view.getPaddingRight(), view.getPaddingBottom());
                    param.setResult(null);
                }
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(param.thisObject, "onDensityOrFontScaleChanged");
            }
        });
    }

    public static void HideIconsClockHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "showClock", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(param.thisObject, "hideClockInternal", 8, false);
                XposedHelpers.callMethod(param.thisObject, "hideNetworkSpeedSplitter", 8, false);
                param.setResult(null);
            }
        });
    }

    public static void HideIconsVoWiFiHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethodSilently("com.android.systemui.MiuiOperatorCustomizedPolicy$MiuiOperatorConfig", lpparam.classLoader, "getHideVowifi", XC_MethodReplacement.returnConstant(true));
    }

    public static void HideIconsSignalHook(LoadPackageParam lpparam) {
        MethodHook beforeUpdate = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mobileIconState = param.args[0];
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_signal")) {
                    XposedHelpers.setObjectField(mobileIconState, "visible", false);
                    return;
                }
                int subId = (int) XposedHelpers.getObjectField(mobileIconState, "subId");
                if ((MainModule.mPrefs.getBoolean("system_statusbaricons_sim1") && subId == 1)
                    || (MainModule.mPrefs.getBoolean("system_statusbaricons_sim2") && subId == 2)
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
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", beforeUpdate);
    }

    private static boolean checkSlot(String slotName) {
        try {
            return "headset".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_headset") ||
                "volume".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_sound") ||
                "zen".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_dnd") ||
                "volume".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_mute") ||
                "speakerphone".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_speaker") ||
                "call_record".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_record") ||
                "alarm_clock".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_alarm") ||
                "managed_profile".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_profile") ||
                "vpn".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_vpn") ||
                "nfc".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nfc") ||
                "location".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_gps") ||
                "wifi".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_wifi") ||
                "slave_wifi".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_dualwifi") ||
                "hotspot".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_hotspot") ||
                "no_sim".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nosims") ||
                "bluetooth_handsfree_battery".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_btbattery") ||
                "hd".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_volte");
        } catch (Throwable t) {
            XposedBridge.log(t);
            return false;
        }
    }

    public static void HideIconsHook(LoadPackageParam lpparam) {
        MethodHook iconHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String iconType = (String)param.args[0];
                if (checkSlot(iconType)) {
                    param.args[1] = false;
                }
            }
        };
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, iconHook);
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, iconHook);
    }

    public static void BatteryIndicatorHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "createAndAddWindows", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                ViewGroup mStatusBarWindow;
                if (Helpers.isTPlus()) {
                    Object sbWindowController = XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindowController");
                    mStatusBarWindow = (ViewGroup) XposedHelpers.getObjectField(sbWindowController, "mStatusBarWindowView");
                }
                else {
                    mStatusBarWindow = (ViewGroup) XposedHelpers.getObjectField(param.thisObject, "mPhoneStatusBarWindow");
                }

                BatteryIndicator indicator = new BatteryIndicator(mContext);
                View panel = mStatusBarWindow.findViewById(mContext.getResources().getIdentifier("notification_panel", "id", lpparam.packageName));
                mStatusBarWindow.addView(indicator, panel != null ? mStatusBarWindow.indexOfChild(panel) + 1 : Math.max(mStatusBarWindow.getChildCount() - 1, 2));
                indicator.setAdjustViewBounds(false);
                indicator.init(param.thisObject);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mBatteryIndicator", indicator);
                Object mNotificationIconAreaController = XposedHelpers.getObjectField(param.thisObject, "mNotificationIconAreaController");
                XposedHelpers.setAdditionalInstanceField(mNotificationIconAreaController, "mBatteryIndicator", indicator);
                Object mBatteryController = XposedHelpers.getObjectField(param.thisObject, "mBatteryController");
                XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator);
                XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged");
                XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged");
            }
        });

        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "setPanelExpanded", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && (boolean)param.args[0]);
            }
        });

        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "setQsExpanded", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
                if (!isKeyguardShowing) return;
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged((boolean)param.args[0]);
            }
        });

        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "updateIsKeyguard", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onKeyguardStateChanged(isKeyguardShowing);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "onDarkChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onDarkModeChanged((float)param.args[1], (int)param.args[2]);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl", lpparam.classLoader, "fireBatteryLevelChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                int mLevel = XposedHelpers.getIntField(param.thisObject, "mLevel");
                boolean mCharging = XposedHelpers.getBooleanField(param.thisObject, "mCharging");
                boolean mCharged = XposedHelpers.getBooleanField(param.thisObject, "mCharged");
                if (indicator != null) indicator.onBatteryLevelChanged(mLevel, mCharging, mCharged);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "firePowerSaveChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onPowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mPowerSave"));
            }
        });
    }
    public static void TempHideOverlaySystemUIHook(LoadPackageParam lpparam) {

        Helpers.hookAllMethods("com.android.wm.shell.pip.PipTaskOrganizer", lpparam.classLoader, "onTaskAppeared", new MethodHook() {
            private boolean isActListened = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
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
                                Object mState;
                                if (Helpers.isTPlus()) {
                                    mState = XposedHelpers.getObjectField(param.thisObject, "mPipTransitionState");
                                }
                                else {
                                    mState = XposedHelpers.getObjectField(param.thisObject, "mState");
                                }
                                boolean isPip = (boolean) XposedHelpers.callMethod(mState, "isInPip");
                                if (isPip) {
                                    Object mSurfaceControlTransactionFactory = XposedHelpers.getObjectField(param.thisObject, "mSurfaceControlTransactionFactory");
                                    SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) XposedHelpers.callMethod(mSurfaceControlTransactionFactory, "getTransaction");
                                    SurfaceControl mLeash = (SurfaceControl) XposedHelpers.getObjectField(param.thisObject, "mLeash");
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
            XposedBridge.log(t);
            tx = TrafficStats.getTotalTxBytes();
            rx = TrafficStats.getTotalRxBytes();
        }

        return new Pair<Long, Long>(tx, rx);
    }

    @SuppressLint("DefaultLocale")
    private static String humanReadableByteCount(Context ctx, long bytes) {
        try {
            Resources modRes = Helpers.getModuleRes(ctx);
            boolean hideSecUnit = MainModule.mPrefs.getBoolean("system_detailednetspeed_secunit");
            String unitSuffix = modRes.getString(R.string.Bs);
            if (hideSecUnit) {
                unitSuffix = "";
            }
            if (bytes < 1024) return bytes + " " + (hideSecUnit ? "B" : unitSuffix);
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = modRes.getString(R.string.speedunits).charAt(exp-1);
            DecimalFormat df = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            df.setMinimumFractionDigits(0);
            df.setMaximumFractionDigits(1);
            return df.format(bytes / Math.pow(1024, exp)) + " " + String.format("%s" + unitSuffix, pre);
        } catch (Throwable t) {
            XposedBridge.log(t);
            return "";
        }
    }

    public static void NetSpeedIntervalHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, "postUpdateNetworkSpeedDelay", long.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                long originInterval = (long) param.args[0];
                if (originInterval == 4000L) {
                    long newInterval = MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000L;
                    param.args[0] = newInterval;
                }
            }
        });
    }

    public static void DetailedNetSpeedHook(LoadPackageParam lpparam) {
        Class<?> nscCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader);
        if (nscCls == null) {
            Helpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller");
            return;
        }

        Helpers.findAndHookMethod(nscCls, "getTotalByte", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Pair<Long, Long> bytes = getTrafficBytes(param.thisObject);
                txBytesTotal = bytes.first;
                rxBytesTotal = bytes.second;
                measureTime = nanoTime();
            }
        });

        Helpers.findAndHookMethod(nscCls, "updateNetworkSpeed", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                boolean isConnected = false;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
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
                    Pair<Long, Long> bytes = getTrafficBytes(param.thisObject);
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

        Helpers.findAndHookMethod(nscCls, "updateText", String.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
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
                param.args[0] = tx + "\n" + rx;
            }
        });
    }

    private static Bitmap processAlbumArt(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null) return bitmap;
        int rescale = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_albumartonlock_scale", "1"));
        boolean grayscale = Helpers.getSharedBoolPref(context, "pref_key_system_albumartonlock_gray", false);
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

    public static void LockScreenAlbumArtHook(LoadPackageParam lpparam) {
        Class<?> MiuiThemeUtilsClass = XposedHelpers.findClassIfExists("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.classLoader);

        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (isDefaultLockScreenTheme) {
                    Object mBlurRatioChangedListener = XposedHelpers.getObjectField(param.thisObject, "mBlurRatioChangedListener");
                    Object notificationShadeDepthController = XposedHelpers.getObjectField(param.thisObject, "notificationShadeDepthController");
                    XposedHelpers.callMethod(notificationShadeDepthController, "removeListener", mBlurRatioChangedListener);
                    View view = (View) XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
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
                                    XposedHelpers.callMethod(param.thisObject, "updateThemeBackground");
                                }
                                catch (Throwable e) {
                                    XposedHelpers.callMethod(param.thisObject, "updateThemeBackgroundVisibility");
                                }
                            }
                        }
                    }, intentFilter);
                }
            }
        });
        MethodHook updateLockscreenHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (!isDefaultLockScreenTheme) {
                    return ;
                }
                View view = (View) XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
                boolean isOnShade = (boolean) XposedHelpers.callMethod(param.thisObject, "isOnShade");
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
                param.setResult(null);
            }
        };
        Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "updateThemeBackground", updateLockscreenHook);

        if (Helpers.isTPlus()) {
            Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "updateThemeBackgroundVisibility", updateLockscreenHook);
        }
        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (!isDefaultLockScreenTheme) {
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null);
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null);
                    return;
                }
                MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.thisObject, "mMediaMetadata");
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

                int blur = Helpers.getSharedIntPref(mContext, "pref_key_system_albumartonlock_blur", 0);
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
        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "clearCurrentMediaNotification", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
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

        final String key = "pref_key_system_lockscreenshortcuts_right";

        int action = Helpers.getSharedIntPref(mContext, key + "_action", 1);
        if (action <= 1) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        String str = Helpers.getActionName(mContext, key);
        if (str == null) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        Drawable icon = Helpers.getActionImage(mContext, key);
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
    public static void LockScreenShortcutHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$MiuiDefaultLeftButton", lpparam.classLoader, "getIcon", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object img = param.getResult();
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
                    Object thisObject = XposedHelpers.getSurroundingThis(param.thisObject);
                    Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
                    boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkStyle");
                    Drawable flashlightDrawable = Helpers.getModuleRes(mContext).getDrawable(
                        mDarkMode ? R.drawable.keyguard_bottom_flashlight_img_dark : R.drawable.keyguard_bottom_flashlight_img_light,
                        mContext.getTheme()
                    );
                    XposedHelpers.setObjectField(img, "drawable", flashlightDrawable);
                } else if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    XposedHelpers.setObjectField(img, "isVisible", false);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$MiuiDefaultRightButton", lpparam.classLoader, "getIcon", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object img = param.getResult();
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    XposedHelpers.setObjectField(img, "isVisible", false);
                    return;
                }

                boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
                if (!opt) return;
                Object thisObject = XposedHelpers.getSurroundingThis(param.thisObject);
                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
                boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkStyle");
                XposedHelpers.setObjectField(img, "drawable", Helpers.getModuleRes(mContext).getDrawable(
                    mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light,
                    mContext.getTheme()
                ));
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "initTipsView", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
                if (!opt) return;
                boolean isLeft = (boolean) param.args[0];
                if (!isLeft) {
                    TextView mRightAffordanceViewTips = (TextView) XposedHelpers.getObjectField(param.thisObject, "mRightAffordanceViewTips");
                    if (mRightAffordanceViewTips != null)
                        mRightAffordanceViewTips.setText(Helpers.getModuleRes(mRightAffordanceViewTips.getContext()).getString(R.string.system_lockscreenshortcuts_right_image_hint));
                }
            }
        });

        if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    View mLeftAffordanceView = (View) XposedHelpers.getObjectField(param.thisObject, "mLeftAffordanceView");
                    mLeftAffordanceView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Object flashlightController = XposedHelpers.getObjectField(param.thisObject, "mFlashlightController");
                            boolean z = !(boolean) XposedHelpers.callMethod(flashlightController, "isEnabled");
                            XposedHelpers.callMethod(flashlightController, "setFlashlight", z);
                            XposedHelpers.callMethod(param.thisObject, "updateLeftAffordanceIcon");
                            return true;
                        }
                    });
                }
            });

            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "updateLeftAffordanceIcon", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object mLeftAffordanceView = XposedHelpers.getObjectField(param.thisObject, "mLeftAffordanceView");
                    Object flashlightController = XposedHelpers.getObjectField(param.thisObject, "mFlashlightController");
                    boolean isOn = (boolean) XposedHelpers.callMethod(flashlightController, "isEnabled");
                    XposedHelpers.callMethod(mLeftAffordanceView, "setCircleRadiusWithoutAnimation", isOn ? 66f : 0f);
                }
            });

            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "onClick", View.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    View view = (View) param.args[0];
                    View mLeftAffordanceView = (View) XposedHelpers.getObjectField(param.thisObject, "mLeftAffordanceView");
                    if (view == mLeftAffordanceView) {
                        param.setResult(null);
                    }
                }
            });
        }

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "launchCamera", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (GlobalActions.handleAction(mContext, "pref_key_system_lockscreenshortcuts_right", true)) {
                    param.setResult(null);
                    Object PanelInjector = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.classLoader));
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

        Helpers.hookAllMethods("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "setDarkStyle", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image")) {
                    Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    boolean mDarkMode = XposedHelpers.getBooleanField(param.thisObject, "mDarkStyle");
                    XposedHelpers.callMethod(param.thisObject, "setPreviewImageDrawable", Helpers.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light, mContext.getTheme()));
                }
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "updatePreView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mPreViewContainer = (View) XposedHelpers.getObjectField(param.thisObject, "mPreViewContainer");
                if ("active".equals(mPreViewContainer.getTag())) {
                    XposedHelpers.setFloatField(param.thisObject, "mIconCircleAlpha", 0.0f);
                    ((View) param.thisObject).invalidate();
                }
            }
        });

        Helpers.hookAllMethods("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "setPreviewImageDrawable", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                boolean mDarkMode = XposedHelpers.getBooleanField(param.thisObject, "mDarkStyle");
                ImageView mIconView = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mIconView");
                if (mIconView != null)
                    if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image"))
                        mIconView.setImageDrawable(Helpers.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light, mContext.getTheme()));
                    else
                        mIconView.setImageDrawable(mContext.getDrawable(mDarkMode ? mContext.getResources().getIdentifier("keyguard_bottom_camera_img_dark", "drawable", lpparam.packageName) : mContext.getResources().getIdentifier("keyguard_bottom_camera_img", "drawable", lpparam.packageName)));

                View mPreView = (View) XposedHelpers.getObjectField(param.thisObject, "mPreView");
                View mPreViewContainer = (View) XposedHelpers.getObjectField(param.thisObject, "mPreViewContainer");
                View mBackgroundView = (View) XposedHelpers.getObjectField(param.thisObject, "mBackgroundView");
                Paint mIconCircleStrokePaint = (Paint) XposedHelpers.getObjectField(param.thisObject, "mIconCircleStrokePaint");
                ViewOutlineProvider mPreViewOutlineProvider = (ViewOutlineProvider) XposedHelpers.getObjectField(param.thisObject, "mPreViewOutlineProvider");
                boolean result = modifyCameraImage(mContext, mPreView, mDarkMode);
                if (result) param.setResult(null);
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

        Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "handleMoveDistanceChanged", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mIconView = (View) XposedHelpers.getObjectField(param.thisObject, "mIconView");
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    if (mIconView != null) mIconView.setVisibility(View.GONE);
                    param.setResult(null);
                } else if (mIconView != null) mIconView.setVisibility(View.VISIBLE);
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "startFullScreenAnim", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int action = MainModule.mPrefs.getInt("system_lockscreenshortcuts_right_action", 1);
                if (action <= 1) return;
                AnimatorSet mAnimatorSet = (AnimatorSet) XposedHelpers.getObjectField(param.thisObject, "mAnimatorSet");
                if (mAnimatorSet == null) return;
                param.setResult(null);
                mAnimatorSet.pause();
                mAnimatorSet.removeAllListeners();
                mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        GlobalActions.handleAction(mContext, "pref_key_system_lockscreenshortcuts_right", true);
                        Object mCallBack = XposedHelpers.getObjectField(param.thisObject, "mCallBack");
                        if (mCallBack != null)
                            XposedHelpers.callMethod(mCallBack, "onCompletedAnimationEnd");
                        XposedHelpers.setBooleanField(param.thisObject, "mIsPendingStartCamera", false);
                        XposedHelpers.callMethod(param.thisObject, "dismiss");
                        View mBackgroundView = (View) XposedHelpers.getObjectField(param.thisObject, "mBackgroundView");
                        if (mBackgroundView != null) mBackgroundView.setAlpha(1.0f);
                    }
                });
                mAnimatorSet.resume();
            }
        });

        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                notificationPanelView = param.thisObject;
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (!key.contains("pref_key_system_lockscreenshortcuts")) return;

                            switch (type) {
                                case "string":
                                    MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, ""));
                                    break;
                                case "integer":
                                    MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 1));
                                    break;
                                case "boolean":
                                    MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
                                    break;
                            }

                            if (key.contains("pref_key_system_lockscreenshortcuts_right"))
                                try {
                                    XposedHelpers.callMethod(notificationPanelView, "setCameraImage", false);
                                } catch (Throwable t1) {
                                    try {
                                        XposedHelpers.callMethod(notificationPanelView, "setCameraImage");
                                    } catch (Throwable t2) {
                                    }
                                }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.classLoader, "setTranslation", float.class, boolean.class, boolean.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
                if (mCurrentScreen != 1) return;
                if ((float) param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off"))
                    param.args[0] = 0.0f;
                else if ((float) param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    param.args[0] = 0.0f;
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.classLoader, "fling", float.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
                if (mCurrentScreen != 1) return;
                if ((float) param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off"))
                    param.setResult(null);
                else if ((float) param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    param.setResult(null);
            }
        });
    }

    public static void LockScreenSecureLaunchHook() {
        Helpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new MethodHook() {
            @SuppressWarnings("ConstantConditions")
            protected void after(MethodHookParam param) throws Throwable {
                Activity act = (Activity)param.thisObject;
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

    public static void SecureQSTilesHook(LoadPackageParam lpparam) {
        Class<?> tileHostCls = XposedHelpers.findClassIfExists("com.android.systemui.qs.QSTileHost", lpparam.classLoader);

        MethodHook hook = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
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
                            LinkedHashMap<String, ?> mTiles = (LinkedHashMap<String, ?>)XposedHelpers.getObjectField(param.thisObject, "mTiles");
                            Object tile = mTiles.get(tileName);
                            if (tile == null) {
                                if (usingCenter) {
                                    Object mController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClassIfExists("com.android.systemui.miui.statusbar.policy.ControlPanelController", lpparam.classLoader));
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
                            XposedBridge.log(t);
                        }
                    }
                };
                mContext.registerReceiver(mAfterUnlockReceiver, new IntentFilter(ACTION_PREFIX + "HandleQSTileClick"));
            }
        };

        Helpers.hookAllConstructors(tileHostCls, hook);

        String FactoryImpl = Helpers.isTPlus() ? "com.android.systemui.qs.tileimpl.MiuiQSFactory" : "com.android.systemui.qs.tileimpl.QSFactoryImpl";
        Helpers.findAndHookMethod(FactoryImpl, lpparam.classLoader, "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object tile = param.getResult();
                if (tile == null) return;
                String tileClass = tile.getClass().getCanonicalName();
                final String tileName = (String)param.args[0];
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
                    MethodHook hook = new MethodHook(10) {
                        @Override
                        protected void before(final MethodHookParam param) throws Throwable {
                            Boolean mCalledAfterUnlock = (Boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCalledAfterUnlock");
                            if (mCalledAfterUnlock != null && mCalledAfterUnlock) {
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCalledAfterUnlock", false);
                                return;
                            }
                            Boolean isScreenLockDisabled = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader), "isScreenLockDisabled");
                            isScreenLockDisabled = isScreenLockDisabled != null && isScreenLockDisabled;
                            if (isScreenLockDisabled) return;
                            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                            KeyguardManager kgMgr = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (!kgMgr.isKeyguardLocked() || !kgMgr.isKeyguardSecure()) return;
                            Handler mHandler = new Handler(mContext.getMainLooper());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Class<?> DependencyClass = findClass("com.android.systemui.Dependency", lpparam.classLoader);
                                        String StatusbarClsForDep = Helpers.isTPlus() ? "com.android.systemui.statusbar.phone.CentralSurfaces" : "com.android.systemui.statusbar.phone.StatusBar";
                                        Object mStatusBar = XposedHelpers.callStaticMethod(DependencyClass, "get", findClassIfExists(StatusbarClsForDep, lpparam.classLoader));
                                        boolean usingControlCenter;
                                        Object mController = XposedHelpers.callStaticMethod(DependencyClass, "get", findClassIfExists("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.classLoader));
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
                                        XposedBridge.log(t);
                                    }
                                }
                            });
                            param.setResult(null);
                        }
                    };
                    Helpers.findAndHookMethod(tileClass, lpparam.classLoader, "handleClick", View.class, hook);
                    Helpers.hookAllMethodsSilently(tileClass, lpparam.classLoader, "handleSecondaryClick", hook);
                    securedTiles.add(tileClass);
                }
            }
        });
    }

    public static void ExtendedPowerMenuHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            private boolean isListened = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isListened) {
                    isListened = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    File powermenu = new File(mContext.getCacheDir(), "extended_power_menu");
                    if (powermenu == null) {
                        Helpers.log("ExtendedPowerMenuHook", "No writable path found!");
                        return;
                    }
                    if (powermenu.exists()) powermenu.delete();

                    InputStream inputStream;
                    FileOutputStream outputStream;
                    byte[] fileBytes;
                    Resources resources = Helpers.getModuleRes(mContext);
                    inputStream = resources.openRawResource(resources.getIdentifier("extended_power_menu", "raw", Helpers.modulePkg));
                    fileBytes = new byte[inputStream.available()];
                    inputStream.read(fileBytes);
                    outputStream = new FileOutputStream(powermenu);
                    outputStream.write(fileBytes);
                    outputStream.close();
                    inputStream.close();

                    if (!powermenu.exists()) {
                        Helpers.log("ExtendedPowerMenuHook", "MAML file not found in cache");
                    }
                    else {
                        Helpers.findAndHookConstructor("com.miui.maml.util.ZipResourceLoader", lpparam.classLoader, String.class, new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) throws Throwable {
                                String res = (String) param.args[0];
                                if ("/system/media/theme/default/powermenu".equals(res)) {
                                    param.args[0] = powermenu.getPath();
                                }
                            }
                        });
                    }
                }
            }
        });
        Helpers.findAndHookMethod("com.miui.maml.ScreenElementRoot", lpparam.classLoader, "issueExternCommand", String.class, Double.class, String.class, new MethodHook() {
            @Override
            @SuppressLint("MissingPermission")
            protected void before(MethodHookParam param) throws Throwable {
                String cmd = (String)param.args[0];
                Object scrContext = XposedHelpers.getObjectField(param.thisObject, "mContext");
                Context mContext = (Context)XposedHelpers.getObjectField(scrContext, "mContext");
                PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                Object mService = XposedHelpers.getObjectField(pm, "mService");
                Object mSystemExternCommandListener = XposedHelpers.getObjectField(param.thisObject, "mSystemExternCommandListener");

                boolean custom = false;
                if ("recovery".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, "recovery", false);
                    custom = true;
                } else if ("bootloader".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, "bootloader", false);
                    custom = true;
                } else if ("softreboot".equals(cmd)) {
                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "FastReboot"));
                    custom = true;
                } else if ("killsysui".equals(cmd)) {
                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "RestartSystemUI"));
                    custom = true;
                } else if ("killlauncher".equals(cmd)) {
                    ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    ResolveInfo launcherInfo = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (launcherInfo != null) {
                        String pkgName = launcherInfo.activityInfo.packageName;
                        if (pkgName != null) XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
                    }
                    custom = true;
                }

                if (custom) {
                    if (mSystemExternCommandListener != null) XposedHelpers.callMethod(mSystemExternCommandListener, "onCommand", param.args[0], param.args[1], param.args[2]);
                    param.setResult(null);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.plugins.PluginEnablerImpl", lpparam.classLoader, "isEnabled", ComponentName.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                ComponentName componentName = (ComponentName) param.args[0];
                if (componentName.getClassName().contains("GlobalActions")) {
                    param.setResult(false);
                }
            }
        });
    }

    public static void HideDismissViewHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "updateDismissView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mDismissView = (View)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
                if (mDismissView != null) {
                    mDismissView.setVisibility(View.GONE);
                    param.setResult(null);
                }
            }
        });
    }

    public static void ReplaceShortcutAppHook(LoadPackageParam lpparam) {
        MethodHook openAppHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = AndroidAppHelper.currentApplication();
                int user = 0;
                String pkgAppName = "";
                if (param.method.getName().equals("startCalendarApp")) {
                    user = Helpers.getSharedIntPref(mContext, "pref_key_system_calendar_app_user", 0);
                    pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_calendar_app", "");
                }
                else if (param.method.getName().equals("startClockApp")) {
                    user = Helpers.getSharedIntPref(mContext, "pref_key_system_clock_app_user", 0);
                    pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_clock_app", "");
                }
                else if (param.method.getName().equals("startSettingsApp")) {
                    user = Helpers.getSharedIntPref(mContext, "pref_key_system_shortcut_app_user", 0);
                    pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_shortcut_app", "");
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
                            Class<?> Dependency = findClass("com.android.systemui.Dependency", lpparam.classLoader);
                            String StatusbarClsForDep = Helpers.isTPlus() ? "com.android.systemui.statusbar.phone.CentralSurfaces" : "com.android.systemui.statusbar.phone.StatusBar";
                            Object mStatusBar = XposedHelpers.callStaticMethod(Dependency, "get", findClass(StatusbarClsForDep, lpparam.classLoader));
                            XposedHelpers.callMethod(mStatusBar, "collapsePanels");
                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    } else {
                        Object activiyStarter = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", mContext.getClassLoader()), "get", findClass("com.android.systemui.plugins.ActivityStarter", mContext.getClassLoader()));
                        XposedHelpers.callMethod(activiyStarter, "startActivity", intent, true);
                    }
                    param.setResult(null);
                }
            }
        };
        if (!MainModule.mPrefs.getString("system_shortcut_app", "").equals("")) {
            Helpers.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startSettingsApp", openAppHook);
        }
        if (!MainModule.mPrefs.getString("system_calendar_app", "").equals("")) {
            Helpers.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startCalendarApp", Context.class, openAppHook);
        }
        if (!MainModule.mPrefs.getString("system_clock_app", "").equals("")) {
            Helpers.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startClockApp", openAppHook);
        }
    }
}