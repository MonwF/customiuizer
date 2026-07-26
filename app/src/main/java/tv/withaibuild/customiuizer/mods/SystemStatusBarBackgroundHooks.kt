package tv.withaibuild.customiuizer.mods

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.ViewGroup
import android.view.Window
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.lang.ref.WeakReference
import java.lang.reflect.Method

object SystemStatusBarBackgroundHooks {

    private const val NOCOLOR = 0x01010101
    private var actionBarColor = NOCOLOR

    private fun getActionBarColor(window: Window, oldColor: Int): Int {
        if (actionBarColor != NOCOLOR) return actionBarColor

        val outValue = TypedValue()
        window.context.theme.resolveAttribute(android.R.attr.actionBarStyle, outValue, true)
        val abStyle: TypedArray = window.context.theme.obtainStyledAttributes(outValue.resourceId, intArrayOf(android.R.attr.background))
        val bg = abStyle.getDrawable(0)
        abStyle.recycle()

        return if (bg is ColorDrawable) bg.color else oldColor
    }

    @Suppress("UNCHECKED_CAST")
    private fun hookToolbar(thisObject: Any, bg: Drawable?) {
        if (bg !is ColorDrawable) return
        actionBarColor = bg.color
        val mDecorToolbar = XposedHelpers.getObjectField(thisObject, "mDecorToolbar")
        val mToolbar = XposedHelpers.getObjectField(mDecorToolbar, "mToolbar") as ViewGroup
        val mDecorContext = mToolbar.rootView.context
        if (mDecorContext != null) {
            val mActivityContext = XposedHelpers.getObjectField(mDecorContext, "mActivityContext") as WeakReference<Context>
            val mContext = mActivityContext.get()
            if (mContext != null) {
                (mContext as Activity).window.setStatusBarColor(actionBarColor)
            }
        }
    }

    private fun hookWindowDecor(thisObject: Any, bg: Drawable?) {
        if (bg !is ColorDrawable) return
        actionBarColor = bg.color
        val mActivity = XposedHelpers.getObjectField(thisObject, "mActivity") as Activity?
        mActivity?.window?.setStatusBarColor(actionBarColor)
    }

    @JvmStatic
    fun StatusBarBackgroundHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader, "generateLayout", "com.android.internal.policy.DecorView", object : MethodHook() {
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

                    val wnd = thisObject as Window
                    val mStatusBarColor = XposedHelpers.getIntField(thisObject, "mStatusBarColor")
                    if (mStatusBarColor == -16777216) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val newColor = getActionBarColor(wnd, mStatusBarColor)
                    if (newColor != mStatusBarColor) {
                        XposedHelpers.callMethod(thisObject, "setStatusBarColor", newColor)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader, "setStatusBarColor", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (actionBarColor != NOCOLOR) args[0] = actionBarColor
                    else if (Color.alpha(args[0] as Int) < 255) args[0] = Color.TRANSPARENT

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.internal.app.ToolbarActionBar", lpparam.classLoader, "setBackgroundDrawable", Drawable::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    hookToolbar(thisObject, args[0] as Drawable?)

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", lpparam.classLoader, "setBackgroundDrawable", Drawable::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    hookWindowDecor(thisObject, args[0] as Drawable?)

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun StatusBarBackgroundCompatHook(lpparam: PackageReadyParam) {
        var androidx = false

        var sbdMethod: Method? = null
        val tabCls = XposedHelpers.findClassIfExists("androidx.appcompat.app.ToolbarActionBar", lpparam.classLoader)
        if (tabCls != null) sbdMethod = XposedHelpers.findMethodExactIfExists(tabCls, "setBackgroundDrawable", Drawable::class.java)
        if (sbdMethod != null) androidx = true
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    val args = XposedHelpers.getArgsArray(chain)
                    val thisObject = chain.thisObject
                    try {

                        hookToolbar(thisObject, args[0] as Drawable?)

                        result = chain.proceed(args)
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })

        sbdMethod = null
        val wdabCls = XposedHelpers.findClassIfExists("androidx.appcompat.app.WindowDecorActionBar", lpparam.classLoader)
        if (wdabCls != null) sbdMethod = XposedHelpers.findMethodExactIfExists(wdabCls, "setBackgroundDrawable", Drawable::class.java)
        if (sbdMethod != null) androidx = true
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    val args = XposedHelpers.getArgsArray(chain)
                    val thisObject = chain.thisObject
                    try {

                        hookWindowDecor(thisObject, args[0] as Drawable?)

                        result = chain.proceed(args)
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })

        if (!androidx) {
            sbdMethod = null
            val tabv7Cls = XposedHelpers.findClassIfExists("android.support.v7.internal.app.ToolbarActionBar", lpparam.classLoader)
            if (tabv7Cls != null) sbdMethod = XposedHelpers.findMethodExactIfExists(tabv7Cls, "setBackgroundDrawable", Drawable::class.java)
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, object : MethodHook() {
                    override fun intercept(chain: XposedInterface.Chain): Any? {
                        var result: Any? = null
                        var throwable: Throwable? = null
                        val args = XposedHelpers.getArgsArray(chain)
                        val thisObject = chain.thisObject
                        try {

                            hookToolbar(thisObject, args[0] as Drawable?)

                            result = chain.proceed(args)
                        } catch (t: Throwable) {
                            throwable = t
                            result = null
                        }
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                })

            sbdMethod = null
            val wdabv7Cls = XposedHelpers.findClassIfExists("android.support.v7.internal.app.WindowDecorActionBar", lpparam.classLoader)
            if (wdabv7Cls != null) sbdMethod = XposedHelpers.findMethodExactIfExists(wdabv7Cls, "setBackgroundDrawable", Drawable::class.java)
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, object : MethodHook() {
                    override fun intercept(chain: XposedInterface.Chain): Any? {
                        var result: Any? = null
                        var throwable: Throwable? = null
                        val args = XposedHelpers.getArgsArray(chain)
                        val thisObject = chain.thisObject
                        try {

                            hookWindowDecor(thisObject, args[0] as Drawable?)

                            result = chain.proceed(args)
                        } catch (t: Throwable) {
                            throwable = t
                            result = null
                        }
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                })
        }
    }
}
