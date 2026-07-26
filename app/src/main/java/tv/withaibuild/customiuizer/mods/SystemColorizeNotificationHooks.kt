package tv.withaibuild.customiuizer.mods

import android.app.Notification
import android.app.WallpaperColors
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.Helpers

object SystemColorizeNotificationHooks {

    @JvmStatic
    fun ColorizeNotificationCardHook(lpparam: PackageReadyParam) {
        val ColorScheme = XposedHelpers.findClassIfExists("com.android.systemui.monet.ColorScheme", lpparam.classLoader)
        var contentStyle: Any? = null
        val MonetStyle = XposedHelpers.findClassIfExists("com.android.systemui.monet.Style", lpparam.classLoader)
        val styles = MonetStyle?.enumConstants
        if (styles != null) {
            for (o in styles) {
                if (o.toString().contains("CONTENT")) {
                    contentStyle = o
                    break
                }
            }
        }
        val finalContentStyle = contentStyle

        ModuleHelper.findAndHookConstructor("android.app.Notification\$Builder", lpparam.classLoader, Context::class.java, Notification::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args[1] != null) {
                        val mN = args[1] as Notification
                        if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
                            val builder = thisObject
                            val mParams = XposedHelpers.getObjectField(builder, "mParams")
                            XposedHelpers.callMethod(builder, "getColors", mParams)
                            val mColors = XposedHelpers.getObjectField(builder, "mColors")
                            XposedHelpers.setObjectField(mColors, "mProtectionColor", XposedHelpers.getAdditionalInstanceField(mN, "mProtectionColor"))
                            XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"))
                            XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"))
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow", lpparam.classLoader, "updateBlurBg", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    args[2] = false

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, "onNotificationUpdated", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mEntry = XposedHelpers.getObjectField(thisObject, "mEntry")
                    if (mEntry != null) {
                        val mSbn = XposedHelpers.getObjectField(mEntry, "mSbn")
                        val notify = XposedHelpers.callMethod(mSbn, "getNotification") as Notification
                        val overflowColor = XposedHelpers.getAdditionalInstanceField(notify, "mSecondaryTextColor")
                        if (overflowColor != null) {
                            XposedHelpers.setObjectField(thisObject, "mNotificationColor", overflowColor)
                        }
                        val mNotifyBackgroundColor = XposedHelpers.getAdditionalInstanceField(notify, "mNotifyBackgroundColor")
                        if (mNotifyBackgroundColor != null) {
                            var bgColor = mNotifyBackgroundColor as Int
                            val mCurrentBackgroundTint = XposedHelpers.getIntField(thisObject, "mCurrentBackgroundTint")
                            if (mCurrentBackgroundTint != bgColor) {
                                bgColor = Color.argb(158, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor))
                                XposedHelpers.callMethod(thisObject, "setBackgroundTintColor", bgColor)
                            }
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", lpparam.classLoader, "setTint", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if ((args[0] as Int) == 0) {
                        skipped = true; result = null; throwable = null
                    }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.classLoader, "getCustomBackgroundColor", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    skipped = true; result = XposedHelpers.getObjectField(thisObject, "mBackgroundColor"); throwable = null

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationContentView", lpparam.classLoader, "updateAllSingleLineViews", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mEntry = XposedHelpers.getObjectField(thisObject, "mNotificationEntry")
                    val singleLineView = XposedHelpers.getObjectField(thisObject, "mSingleLineView")
                    if (mEntry != null && singleLineView != null) {
                        val mSbn = XposedHelpers.getObjectField(mEntry, "mSbn")
                        val mN = XposedHelpers.callMethod(mSbn, "getNotification") as Notification
                        if (XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor") != null) {
                            val hybridNotificationView = singleLineView as LinearLayout
                            val mTitleView = XposedHelpers.getObjectField(hybridNotificationView, "mTitleView") as TextView
                            val mTextView = XposedHelpers.getObjectField(hybridNotificationView, "mTextView") as TextView
                            mTitleView.setTextColor(XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") as Int)
                            mTextView.setTextColor(XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor") as Int)
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.classLoader, "handle3thThemeColor", object : MethodHook() {
            private var sAppIconManager: Any? = null
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val builder = args[0] as Notification.Builder
                    val mN = XposedHelpers.getObjectField(builder, "mN") as Notification
                    if (XposedHelpers.callMethod(mN, "isColorized") as Boolean) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (XposedHelpers.callMethod(mN, "isMediaNotification") as Boolean) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val applicationInfo = mN.extras.getParcelable("android.appInfo") as ApplicationInfo?
                    if (applicationInfo == null) {
                        return XposedHelpers.proceedOrThrow(chain, args, throwable)
                    }
                    val mContext = args[1] as Context
                    val pkgName = applicationInfo.packageName
                    val opt = MainModule.mPrefs.getStringAsInt("system_colorizenotifs", 1)
                    val isSelected = MainModule.mPrefs.getStringSet("system_colorizenotifs_apps").contains(pkgName)
                    if ((opt == 2 && !isSelected) || (opt == 3 && isSelected)) {
                        XposedHelpers.callMethod(builder, "makeNotificationGroupHeader")
                        if (sAppIconManager == null) {
                            sAppIconManager = ModuleHelper.getDepInstance(lpparam.classLoader, "com.miui.systemui.graphics.AppIconsManager")
                        }
                        val userId = ModuleHelper.getUserId()
                        val notifyIcon = XposedHelpers.callMethod(sAppIconManager, "getAppIconBitmap", userId, pkgName) as Bitmap
                        val wc = WallpaperColors.fromBitmap(notifyIcon)
                        var primaryColor = wc.primaryColor.toArgb()
                        val lux = Color.luminance(primaryColor)
                        if (lux > 0.9) {
                            val secColor = wc.secondaryColor
                            if (secColor != null) {
                                primaryColor = secColor.toArgb()
                            }
                        }
                        val dark = mContext.resources.configuration.isNightModeActive
                        val cs = XposedHelpers.newInstance(ColorScheme, primaryColor, dark, finalContentStyle)
                        val paletteAccent1 = XposedHelpers.getObjectField(cs, "accent1")
                        val accent1 = XposedHelpers.getObjectField(paletteAccent1, "allShades") as List<Int>
                        val paletteN1 = XposedHelpers.getObjectField(cs, "neutral1")
                        val n1 = XposedHelpers.getObjectField(paletteN1, "allShades") as List<Int>
                        val paletteN2 = XposedHelpers.getObjectField(cs, "neutral2")
                        val n2 = XposedHelpers.getObjectField(paletteN2, "allShades") as List<Int>

                        val bgColor = accent1[if (dark) 5 else 6]
                        val mParams = XposedHelpers.getObjectField(builder, "mParams")
                        XposedHelpers.callMethod(mParams, "reset")
                        XposedHelpers.callMethod(builder, "getColors", mParams)
                        val mColors = XposedHelpers.getObjectField(builder, "mColors")
                        val mProtectionColor = ColorUtils.blendARGB(n1[1], bgColor, 0.7f)
                        val mPrimaryTextColor = n1[if (dark) 1 else 10]
                        val mSecondaryTextColor = n2[if (dark) 3 else 8]
                        XposedHelpers.setObjectField(mColors, "mProtectionColor", mProtectionColor)
                        XposedHelpers.setAdditionalInstanceField(mN, "mProtectionColor", mProtectionColor)
                        XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", mPrimaryTextColor)
                        XposedHelpers.setAdditionalInstanceField(mN, "mPrimaryTextColor", mPrimaryTextColor)
                        XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", mSecondaryTextColor)
                        XposedHelpers.setAdditionalInstanceField(mN, "mSecondaryTextColor", mSecondaryTextColor)
                        XposedHelpers.setAdditionalInstanceField(mN, "mNotifyBackgroundColor", bgColor)
                        skipped = true; result = null; throwable = null
                    }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.classLoader, "createRemoteViews", object : MethodHook() {
            private var titleResId = 0
            private var subTextResId = 0
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val NotificationHelper = XposedHelpers.findClass("com.android.systemui.statusbar.notification.NotificationSettingsHelper", lpparam.classLoader)
                    var miuiStyle = false
                    val builder = args[1] as Notification.Builder
                    val notification = builder.notification
                    if (XposedHelpers.callMethod(notification, "isMediaNotification") as Boolean) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val isFoldEntrance = notification.extras.getBoolean("miui_unimportant", false)
                    val showMiuiStyle = XposedHelpers.callStaticMethod(NotificationHelper, "showMiuiStyle") as Boolean
                    if (showMiuiStyle || isFoldEntrance) {
                        val style = builder.style
                        miuiStyle = style == null || (style is Notification.BigPictureStyle) || (style is Notification.BigTextStyle) || (style is Notification.InboxStyle)
                    }
                    if (miuiStyle) {
                        val inflationProgress = result
                        val mContext = args[args.size - 1] as Context
                        if (titleResId == 0) {
                            titleResId = Helpers.getResId(mContext.resources, "title", "id", "com.android.systemui")
                            subTextResId = Helpers.getResId(mContext.resources, "text", "id", "com.android.systemui")
                        }
                        val contents = listOf("newPublicView", "newContentView", "newExpandedView")
                        for (contentType in contents) {
                            val baseContent = XposedHelpers.getObjectField(inflationProgress, contentType) as RemoteViews?
                            if (baseContent != null && XposedHelpers.getAdditionalInstanceField(notification, "mPrimaryTextColor") != null) {
                                baseContent.setTextColor(titleResId, XposedHelpers.getAdditionalInstanceField(notification, "mPrimaryTextColor") as Int)
                                baseContent.setTextColor(subTextResId, XposedHelpers.getAdditionalInstanceField(notification, "mSecondaryTextColor") as Int)
                            }
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }
}
