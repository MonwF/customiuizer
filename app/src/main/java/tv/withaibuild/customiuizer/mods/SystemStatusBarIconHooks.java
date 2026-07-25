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

public class SystemStatusBarIconHooks {
    public static void HideIconsBattery1Hook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateAll", new MethodHook() {
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

            		                ImageView mBatteryIconView = (ImageView)XposedHelpers.getObjectField(thisObject, "mBatteryIconView");
            		                mBatteryIconView.setVisibility(View.GONE);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideIconsBattery2Hook(PackageReadyParam lpparam) {
        boolean hideNormalPercentage = MainModule.mPrefs.getBoolean("system_statusbaricons_battery2");
        int batteryId = ResourceHooks.getFakeResId("batterview_in_statusbar");
        if (hideNormalPercentage) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

                		                    View mBatteryView = (View) XposedHelpers.getObjectField(thisObject, "mBattery");
                		                    mBatteryView.setTag(batteryId, true);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

                		                    ViewGroup mSystemIconsContainer = (ViewGroup) XposedHelpers.getObjectField(thisObject, "mSystemIconsContainer");
                		                    int batteryResId = Helpers.getResId(mSystemIconsContainer.getResources(), "battery", "id", "com.android.systemui");
                		                    View mBatteryView = mSystemIconsContainer.findViewById(batteryResId);
                		                    mBatteryView.setTag(batteryId, true);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateChargeAndText", new MethodHook() {
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

            		                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
            		                    TextView mBatteryPercentMarkView = (TextView)XposedHelpers.getObjectField(thisObject, "mBatteryPercentMarkView");
            		                    mBatteryPercentMarkView.setVisibility(View.GONE);
            		                }
            		                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery3")) {
            		                    ImageView mBatteryChargingView = (ImageView)XposedHelpers.getObjectField(thisObject, "mBatteryChargingView");
            		                    mBatteryChargingView.setVisibility(View.GONE);
            		                    try {
            		                        ImageView mBatteryChargingInView = (ImageView)XposedHelpers.getObjectField(thisObject, "mBatteryChargingInView");
            		                        mBatteryChargingInView.setVisibility(View.GONE);
            		                    } catch (Throwable ignore) {}
            		                }
            		                if (hideNormalPercentage) {
            		                    View mBatteryView = (View) thisObject;
            		                    if (mBatteryView.getTag(batteryId) != null) {
            		                        View percentView = (View)XposedHelpers.getObjectField(thisObject, "mBatteryPercentMarkView");
            		                        percentView.setVisibility(View.GONE);
            		                        percentView = (View)XposedHelpers.getObjectField(thisObject, "mBatteryPercentView");
            		                        percentView.setVisibility(View.GONE);
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static boolean lastState = false;
    private static long mNextAlarmTime = 0L;
    private static void updateAlarmVisibility(Object thisObject) {
        try {
            Object mIconController = XposedHelpers.getObjectField(thisObject, "mIconController");
            if (!lastState) {
                XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", false);
                return;
            }

            Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            long nowTime = java.lang.System.currentTimeMillis();
            long nextTime = mNextAlarmTime;
            if (nextTime == 0) {
                nextTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
            }
            if (nextTime == 0) nextTime = Helpers.getNextStockAlarmTime(mContext);

            long diffMSec = nextTime - nowTime;
            if (diffMSec < 0) diffMSec += 7 * 24 * 60 *60 * 1000;
            float diffHours = (diffMSec - 59 * 1000) / (1000f * 60f * 60f);
            boolean vis = diffHours <= MainModule.mPrefs.getInt("system_statusbaricons_alarmn", 0);
            XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", vis);
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
    }

    public static void HideIconsSelectiveAlarmHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), new MethodHook() {
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


            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                IntentFilter filter = new IntentFilter();
            		                filter.addAction("android.intent.action.TIME_TICK");
            		                filter.addAction("android.intent.action.TIME_SET");
            		                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            		                filter.addAction("android.intent.action.LOCALE_CHANGED");
            		                Object oldalarmTimeReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "alarmTimeReceiver");
            		                if (oldalarmTimeReceiver instanceof BroadcastReceiver) {
            		                    try { mContext.unregisterReceiver((BroadcastReceiver) oldalarmTimeReceiver); } catch (Throwable ignore) {}
            		                }
            		                BroadcastReceiver alarmTimeReceiver = new BroadcastReceiver() {
            		                    @Override
            		                    public void onReceive(Context context, Intent intent) {
            		                        updateAlarmVisibility(thisObject);
            		                    }
            		                };
            		                XposedHelpers.setAdditionalInstanceField(thisObject, "alarmTimeReceiver", alarmTimeReceiver);
            		                mContext.registerReceiver(alarmTimeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

            		                Object mNextAlarmCallback = XposedHelpers.getObjectField(thisObject, "mNextAlarmCallback");
            		                ModuleHelper.findAndHookMethod(mNextAlarmCallback.getClass(), "onAlarmChanged", boolean.class, new MethodHook() {
            		                    @Override
            		                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            		                    	boolean skipped = false;
            		                    	Object result = null;
            		                    	Throwable throwable = null;
            		                    	Object[] args = XposedHelpers.getArgsArray(chain);
            		                    	Object thisObject = chain.getThisObject();
            		                    	try {

            		                    		                        lastState = (boolean) args[0];
            		                    		                        mNextAlarmTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
            		                    		                        updateAlarmVisibility(thisObject);
            		                    		                        { skipped = true; result = null; throwable = null; }

            		                    		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    		result = chain.proceed(args);
            		                    	} catch (Throwable t) {
            		                    		throwable = t;
            		                    		result = null;
            		                    	}
            		                    	return XposedHelpers.throwOrReturn(throwable, result);
            		                    }
            		                });
            		                ModuleHelper.findAndHookMethod(mNextAlarmCallback.getClass(), "onNextAlarmChanged", AlarmManager.AlarmClockInfo.class, new MethodHook() {
            		                    @Override
            		                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            		                    	boolean skipped = false;
            		                    	Object result = null;
            		                    	Throwable throwable = null;
            		                    	Object[] args = XposedHelpers.getArgsArray(chain);
            		                    	Object thisObject = chain.getThisObject();
            		                    	try {

            		                    		                        if (args[0] == null) {
            		                    		                            lastState = false;
            		                    		                        }
            		                    		                        mNextAlarmTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
            		                    		                        updateAlarmVisibility(thisObject);
            		                    		                        { skipped = true; result = null; throwable = null; }

            		                    		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    		result = chain.proceed(args);
            		                    	} catch (Throwable t) {
            		                    		throwable = t;
            		                    		result = null;
            		                    	}
            		                    	return XposedHelpers.throwOrReturn(throwable, result);
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DisplayWifiStandardHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.getClassLoader(), "applyWifiState", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Object wifiState = args[0];
            		                if (wifiState != null) {
            		                    int opt = MainModule.mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1);
                                        if (opt == 1) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                    int wifiStandard = (int) XposedHelpers.getObjectField(wifiState, "wifiStandard");
            		                    XposedHelpers.setObjectField(wifiState, "showWifiStandard", opt == 2 && wifiStandard > 0);
            		                }


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
