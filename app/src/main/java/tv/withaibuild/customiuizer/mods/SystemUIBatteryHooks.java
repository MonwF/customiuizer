package tv.withaibuild.customiuizer.mods;

import static android.content.Context.RECEIVER_NOT_EXPORTED;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static tv.withaibuild.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
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
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
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
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import io.github.libxposed.api.XposedInterface;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import miui.os.Build;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.ResourceHooks;
import tv.withaibuild.customiuizer.mods.utils.StepCounterController;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.BatteryIndicator;
import tv.withaibuild.customiuizer.utils.Helpers;
import tv.withaibuild.customiuizer.utils.PrefMap;

public class SystemUIBatteryHooks {
    private final static String StatusBarCls = "com.android.systemui.statusbar.phone.CentralSurfacesImpl";

    public static void BatteryIndicatorHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "start", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Object sbWindowController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarWindowController");
                ViewGroup mStatusBarWindow = (ViewGroup) XposedHelpers.getObjectField(sbWindowController, "mStatusBarWindowView");

                BatteryIndicator indicator = new BatteryIndicator(mContext);
                mStatusBarWindow.addView(indicator);
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

        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updatePanelExpanded", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean mPanelExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mPanelExpanded");
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.getThisObject(), "isKeyguardShowing");
                Object mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mCentralSurfaces");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(mStatusBar, "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && mPanelExpanded);
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

    public static void StatusBarStyleBatteryIconHook(PackageReadyParam lpparam) {
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

}
