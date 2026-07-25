package tv.withaibuild.customiuizer.mods;

import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import android.app.Notification;
import android.app.WallpaperColors;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import java.util.List;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.Helpers;

public class SystemColorizeNotificationHooks {
    public static void ColorizeNotificationCardHook(PackageReadyParam lpparam) {
        Class<?> ColorScheme = findClassIfExists("com.android.systemui.monet.ColorScheme", lpparam.getClassLoader());
        Object contentStyle = null;
        Class<?> MonetStyle = findClassIfExists("com.android.systemui.monet.Style", lpparam.getClassLoader());
        Object[] styles = MonetStyle.getEnumConstants();
        for (Object o:styles) {
            if (o.toString().contains("CONTENT")) {
                contentStyle = o;
                break;
            }
        }
        Object finalContentStyle = contentStyle;

        ModuleHelper.findAndHookConstructor("android.app.Notification$Builder", lpparam.getClassLoader(), Context.class, Notification.class, new MethodHook() {
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

            		                if (args[1] != null) {
            		                    Notification mN = (Notification) args[1];
            		                    if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
            		                        Object builder = thisObject;
            		                        Object mParams = XposedHelpers.getObjectField(builder, "mParams");
            		                        XposedHelpers.callMethod(builder, "getColors", mParams);
            		                        Object mColors = XposedHelpers.getObjectField(builder, "mColors");
            		                        XposedHelpers.setObjectField(mColors, "mProtectionColor", XposedHelpers.getAdditionalInstanceField(mN, "mProtectionColor"));
            		                        XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
            		                        XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow", lpparam.getClassLoader(), "updateBlurBg", int.class, int.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                args[2] = false;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "onNotificationUpdated", new MethodHook() {
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

            		                Object mEntry = XposedHelpers.getObjectField(thisObject, "mEntry");
            		                if (mEntry != null) {
            		                    Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
            		                    Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
            		                    Object overflowColor = XposedHelpers.getAdditionalInstanceField(notify, "mSecondaryTextColor");
            		                    if (overflowColor != null) {
            		                        XposedHelpers.setObjectField(thisObject, "mNotificationColor", overflowColor);
            		                    }
            		                    Object mNotifyBackgroundColor = XposedHelpers.getAdditionalInstanceField(notify, "mNotifyBackgroundColor");
            		                    if (mNotifyBackgroundColor != null) {
            		                        int bgColor = (int) mNotifyBackgroundColor;
            		                        int mCurrentBackgroundTint = XposedHelpers.getIntField(thisObject, "mCurrentBackgroundTint");
            		                        if (mCurrentBackgroundTint != bgColor) {
            		                            bgColor = Color.argb(158, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
            		                            XposedHelpers.callMethod(thisObject, "setBackgroundTintColor", bgColor);
            		                        }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", lpparam.getClassLoader(), "setTint", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if ((int)args[0] == 0) {
            		                    { skipped = true; result = null; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.getClassLoader(), "getCustomBackgroundColor", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                { skipped = true; result = XposedHelpers.getObjectField(thisObject, "mBackgroundColor"); throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationContentView", lpparam.getClassLoader(), "updateAllSingleLineViews", new MethodHook() {
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

            		                Object mEntry = XposedHelpers.getObjectField(thisObject, "mNotificationEntry");
            		                Object singleLineView = XposedHelpers.getObjectField(thisObject, "mSingleLineView");
            		                if (mEntry != null && singleLineView != null) {
            		                    Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
            		                    Notification mN = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
            		                    if (XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor") != null) {
            		                        LinearLayout hybridNotificationView = (LinearLayout) singleLineView;
            		                        TextView mTitleView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTitleView");
            		                        TextView mTextView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTextView");
            		                        mTitleView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
            		                        mTextView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "handle3thThemeColor", new MethodHook() {
            private Object sAppIconManager = null;
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Notification.Builder builder = (Notification.Builder) args[0];
            		                Notification mN = (Notification) XposedHelpers.getObjectField(builder, "mN");
                                    if ((boolean)XposedHelpers.callMethod(mN, "isColorized")) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
                                    if ((boolean)XposedHelpers.callMethod(mN, "isMediaNotification")) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                ApplicationInfo applicationInfo = mN.extras.getParcelable("android.appInfo");
            		                if (applicationInfo == null) {
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                }
            		                Context mContext = (Context) args[1];
            		                String pkgName = applicationInfo.packageName;
            		                int opt = MainModule.mPrefs.getStringAsInt("system_colorizenotifs", 1);
            		                boolean isSelected = MainModule.mPrefs.getStringSet("system_colorizenotifs_apps").contains(pkgName);
            		                if (opt == 2 && !isSelected || opt == 3 && isSelected) {
            		                    XposedHelpers.callMethod(builder, "makeNotificationGroupHeader");
            		                    if (sAppIconManager == null) {
            		                        sAppIconManager = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.miui.systemui.graphics.AppIconsManager");
            		                    }
            		                    int userId = ModuleHelper.getUserId();
            		                    Bitmap notifyIcon = (Bitmap) XposedHelpers.callMethod(sAppIconManager, "getAppIconBitmap", userId, pkgName);
            		                    WallpaperColors wc = WallpaperColors.fromBitmap(notifyIcon);
            		                    int primaryColor = wc.getPrimaryColor().toArgb();
            		                    float lux = Color.luminance(primaryColor);
            		                    if (lux > 0.9) {
            		                        Color secColor = wc.getSecondaryColor();
            		                        if (secColor != null) {
            		                            primaryColor = secColor.toArgb();
            		                        }
            		                    }
            		                    Object cs;
            		                    boolean dark = mContext.getResources().getConfiguration().isNightModeActive();
            		                    cs = XposedHelpers.newInstance(ColorScheme, primaryColor, dark, finalContentStyle);
            		                    Object paletteAccent1 = XposedHelpers.getObjectField(cs, "accent1");
            		                    List<Integer> accent1 = (List<Integer>) XposedHelpers.getObjectField(paletteAccent1, "allShades");
            		                    Object paletteN1 = XposedHelpers.getObjectField(cs, "neutral1");
            		                    List<Integer> n1 = (List<Integer>) XposedHelpers.getObjectField(paletteN1, "allShades");
            		                    Object paletteN2 = XposedHelpers.getObjectField(cs, "neutral2");
            		                    List<Integer> n2 = (List<Integer>) XposedHelpers.getObjectField(paletteN2, "allShades");

            		                    int bgColor = accent1.get(dark ? 5 : 6);
            		                    Object mParams = XposedHelpers.getObjectField(builder, "mParams");
            		                    XposedHelpers.callMethod(mParams, "reset");
            		                    XposedHelpers.callMethod(builder, "getColors", mParams);
            		                    Object mColors = XposedHelpers.getObjectField(builder, "mColors");
            		                    int mProtectionColor = ColorUtils.blendARGB(n1.get(1), bgColor, 0.7f);
            		                    int mPrimaryTextColor = n1.get(dark ? 1 : 10);
            		                    int mSecondaryTextColor = n2.get(dark ? 3 : 8);
            		                    XposedHelpers.setObjectField(mColors, "mProtectionColor", mProtectionColor);
            		                    XposedHelpers.setAdditionalInstanceField(mN, "mProtectionColor", mProtectionColor);
            		                    XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", mPrimaryTextColor);
            		                    XposedHelpers.setAdditionalInstanceField(mN, "mPrimaryTextColor", mPrimaryTextColor);
            		                    XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", mSecondaryTextColor);
            		                    XposedHelpers.setAdditionalInstanceField(mN, "mSecondaryTextColor", mSecondaryTextColor);
            		                    XposedHelpers.setAdditionalInstanceField(mN, "mNotifyBackgroundColor", bgColor);
            		                    { skipped = true; result = null; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "createRemoteViews", new MethodHook() {
            private int titleResId = 0;
            private int subTextResId = 0;
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                Class<?> NotificationHelper = findClass("com.android.systemui.statusbar.notification.NotificationSettingsHelper", lpparam.getClassLoader());
            		                boolean miuiStyle = false;
            		                Notification.Builder builder = (Notification.Builder) args[1];
            		                Notification notification = builder.getNotification();
            		                if ((boolean)XposedHelpers.callMethod(notification, "isMediaNotification")) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                boolean isFoldEntrance = notification.extras.getBoolean("miui_unimportant", false);
            		                boolean showMiuiStyle = (boolean) XposedHelpers.callStaticMethod(NotificationHelper, "showMiuiStyle");
            		                if (showMiuiStyle || isFoldEntrance) {
            		                    Notification.Style style = builder.getStyle();
            		                    miuiStyle = style == null || (style instanceof Notification.BigPictureStyle) || (style instanceof Notification.BigTextStyle) || (style instanceof Notification.InboxStyle);
            		                }
            		                if (miuiStyle) {
            		                    Object inflationProgress = result;
            		                    Context mContext = (Context) args[args.length - 1];
            		                    if (titleResId == 0) {
            		                        titleResId = Helpers.getResId(mContext.getResources(), "title", "id", "com.android.systemui");
            		                        subTextResId = Helpers.getResId(mContext.getResources(), "text", "id", "com.android.systemui");
            		                    }
            		                    List<String> contents = List.of("newPublicView", "newContentView", "newExpandedView");
            		                    for (String contentType:contents) {
            		                        RemoteViews baseContent = (RemoteViews) XposedHelpers.getObjectField(inflationProgress, contentType);
            		                        if (baseContent != null && XposedHelpers.getAdditionalInstanceField(notification, "mPrimaryTextColor") != null) {
            		                            baseContent.setTextColor(titleResId, (int)XposedHelpers.getAdditionalInstanceField(notification, "mPrimaryTextColor"));
            		                            baseContent.setTextColor(subTextResId, (int)XposedHelpers.getAdditionalInstanceField(notification, "mSecondaryTextColor"));
            		                        }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
}
