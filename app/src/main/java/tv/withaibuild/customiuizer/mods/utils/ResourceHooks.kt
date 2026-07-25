package tv.withaibuild.customiuizer.mods.utils

import android.app.MiuiThemeHelper
import android.content.Context
import android.content.res.Resources
import android.util.SparseIntArray
import io.github.libxposed.api.XposedInterface
import miui.content.res.ThemeValues
import java.util.concurrent.ConcurrentHashMap

class ResourceHooks {

    private class ResourceValue(val mType: ReplacementType, val mValue: Any?)

    class ThemeValue {
        var mValue: Any? = null
        var mNightValue: Any? = null
        var resId = -1
        var pkg: String? = null
        var name: String? = null
        var themeValueType: String? = null
        var resourceType: String? = null

        constructor(value: Any?) {
            mValue = value
            mNightValue = value
        }

        constructor(value: Any?, nightValue: Any?) {
            mValue = value
            mNightValue = nightValue
        }
    }

    enum class ReplacementType {
        ID, OBJECT
    }

    private val hookedTypes = HashSet<String>()
    private val fakes = SparseIntArray()
    private val themeValueReplacements = ConcurrentHashMap<String, ThemeValue>()
    private val resourceIdReplacements = ConcurrentHashMap<Int, ResourceValue>()

    private val mReplaceHook = object : HookerClassHelper.MethodHook() {
        @Throws(Throwable::class)
        override fun intercept(chain: XposedInterface.Chain): Any? {
            var skipValue: Any? = null
            var shouldSkip = false
            var replacementHandled = false
            try {
                val args = chain.getArgs()
                val resIdObj = args[0]
                val resId = resIdObj as Int
                val method = chain.executable.name
                val replacement = resourceIdReplacements[resId]
                if (replacement != null) {
                    replacementHandled = true
                    if (replacement.mType == ReplacementType.OBJECT) {
                        skipValue = replacement.mValue
                        shouldSkip = true
                    } else if ("getLayout" == method) {
                        // proceed original, do not check fakes
                    } else {
                        val mContext = ModuleHelper.findContext()
                        if (mContext != null) {
                            val modRes = ModuleHelper.getModuleRes(mContext)
                            if (modRes != null) {
                                val value = getModuleResValue(modRes, method, replacement.mValue as Int, args)
                                if (value != null) {
                                    skipValue = value
                                    shouldSkip = true
                                }
                            }
                        }
                    }
                }
                if (!shouldSkip && !replacementHandled) {
                    val modResId = fakes[resId]
                    if (modResId != 0) {
                        val mContext = ModuleHelper.findContext()
                        if (mContext != null) {
                            val modRes = ModuleHelper.getModuleRes(mContext)
                            if (modRes != null) {
                                val value = getModuleResValue(modRes, method, modResId, args)
                                if (value != null) {
                                    skipValue = value
                                    shouldSkip = true
                                }
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
            return if (shouldSkip) skipValue else chain.proceed()
        }
    }

    private fun initThemeHook() {
        ModuleHelper.findAndHookMethod(
            miui.content.res.ThemeResources::class.java,
            "mergeThemeValues",
            String::class.java,
            ThemeValues::class.java,
            object : HookerClassHelper.MethodHook() {
                @Throws(Throwable::class)
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val mThemeResources = chain.thisObject
                        val mPackageName = XposedHelpers.getObjectField(mThemeResources, "mPackageName") as String?
                        if (mPackageName != null && mPackageName != "miui" && (
                            mPackageName == ModuleHelper.currentPackageName
                            || "miui.systemui.plugin" == mPackageName
                        )) {
                            val args = chain.getArgs()
                            if (args.size > 1 && (
                                args[0] == ModuleHelper.currentPackageName
                                || "miui.systemui.plugin" == args[0]
                            )) {
                                val themeIntValues = HashMap<Int, Int>()
                                val themeIntegerArrays = HashMap<Int, IntArray>()
                                val themeStringArrays = HashMap<Int, Array<String>>()
                                val mResources = XposedHelpers.getObjectField(mThemeResources, "mResources") as Resources
                                val nightMode = XposedHelpers.getBooleanField(mThemeResources, "mNightMode")
                                val mThemeValues = args[1]
                                @Suppress("UNCHECKED_CAST")
                                val mIntegers = XposedHelpers.getObjectField(mThemeValues, "mIntegers") as HashMap<Int, Int>
                                @Suppress("UNCHECKED_CAST")
                                val mIntegerArrays = XposedHelpers.getObjectField(mThemeValues, "mIntegerArrays") as HashMap<Int, IntArray>
                                @Suppress("UNCHECKED_CAST")
                                val mStringArrays = XposedHelpers.getObjectField(mThemeValues, "mStringArrays") as HashMap<Int, Array<String>>
                                for ((_, tv) in themeValueReplacements) {
                                    if (tv.resId == -1) {
                                        if (tv.pkg == mPackageName || "android" == tv.pkg) {
                                            tv.resId = mResources.getIdentifier(tv.name, tv.resourceType, tv.pkg)
                                        }
                                    }
                                    if (tv.resId > 0) {
                                        when (tv.themeValueType) {
                                            "string-array" -> themeStringArrays[tv.resId] = (if (nightMode) tv.mNightValue else tv.mValue) as Array<String>
                                            "integer-array" -> themeIntegerArrays[tv.resId] = (if (nightMode) tv.mNightValue else tv.mValue) as IntArray
                                            else -> themeIntValues[tv.resId] = (if (nightMode) tv.mNightValue else tv.mValue) as Int
                                        }
                                    }
                                }
                                mIntegers.putAll(themeIntValues)
                                mIntegerArrays.putAll(themeIntegerArrays)
                                mStringArrays.putAll(themeStringArrays)
                            }
                        }
                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            }
        )
    }

    private fun initResourceIdHook(pkg: String, type: String, name: String, resourceType: ReplacementType, replaceValue: Any?) {
        val mContext = ModuleHelper.findContext()
        val rv = ResourceValue(resourceType, replaceValue)
        if (mContext != null) {
            val resId = mContext.resources.getIdentifier(name, type, pkg)
            if (resId > 0) resourceIdReplacements[resId] = rv
            else {
                XposedHelpers.log("Resource not found: $pkg:$type/$name")
            }
        } else {
            XposedHelpers.log("Context not found: $pkg:$type/$name")
        }
    }

    private fun applyHooks(type: String) {
        if (hookedTypes.contains(type)) return
        hookedTypes.add(type)
        when (type) {
            "layout" -> ModuleHelper.findAndHookMethod(Resources::class.java, "getLayout", Int::class.javaPrimitiveType, mReplaceHook)
            "string" -> {
                ModuleHelper.findAndHookMethod(Resources::class.java, "getText", Int::class.javaPrimitiveType, mReplaceHook)
                ModuleHelper.findAndHookMethod(Resources::class.java, "getString", Int::class.javaPrimitiveType, mReplaceHook)
            }
            "drawable" -> ModuleHelper.findAndHookMethod(Resources::class.java, "getDrawableForDensity", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Resources.Theme::class.java, mReplaceHook)
        }
    }

    /**
     * Add fake resources which can be replaced by module resources. eg: drawable, string, layout
     *
     * @param resName resource name
     * @param resId   module resource id
     * @param type    resource type
     * @return fake resource id
     */
    fun addFakeResource(resName: String, resId: Int, type: String): Int {
        return try {
            val fakeResId = getFakeResId(resName)
            fakes.put(fakeResId, resId)
            applyHooks(type)
            fakeResId
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            0
        }
    }

    /**
     * Replace package resources with module resources
     *
     * @param pkg              package name. * for all packages
     * @param type             resource type
     * @param name             resource name
     * @param replacementResId module resource id
     */
    fun setResReplacement(pkg: String, type: String, name: String, replacementResId: Int) {
        try {
            initResourceIdHook(pkg, type, name, ReplacementType.ID, replacementResId)
            applyHooks(type)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    /**
     * Replace package resources with replacement value
     *
     * @param pkg                 package name. * for all packages
     * @param type                resource type
     * @param name                resource name
     * @param replacementResValue replacement value
     */
    fun setObjectReplacement(pkg: String, type: String, name: String, replacementResValue: Any?) {
        try {
            initResourceIdHook(pkg, type, name, ReplacementType.OBJECT, replacementResValue)
            applyHooks(type)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    fun setThemeValueReplacement(pkg: String, type: String, name: String, resValue: Any?) {
        setThemeValueReplacement(pkg, type, name, resValue, resValue)
    }

    fun setThemeValueReplacement(pkg: String, type: String, name: String, resValue: Any?, nightResValue: Any?) {
        var value: Any? = resValue
        var nightValue: Any? = nightResValue
        if ("bool" == type) {
            value = if (value as Boolean) 1 else 0
            nightValue = if (nightValue as Boolean) 1 else 0
        } else if ("dimen" == type) {
            val valInDimen = "${value}dp"
            value = MiuiThemeHelper.parseDimension(valInDimen)
            nightValue = value
        }
        val tv = ThemeValue(value, nightValue)
        tv.pkg = pkg
        tv.name = name
        tv.themeValueType = type
        tv.resourceType = if ("string-array" == type || "integer-array" == type) "array" else type
        themeValueReplacements["$pkg:$type/$name"] = tv
        if (!themeResourcesHooked) {
            themeResourcesHooked = true
            initThemeHook()
        }
    }

    private fun getModuleResValue(modRes: Resources, method: String, modResId: Int, args: List<Any?>): Any? {
        return when (method) {
            "getText" -> modRes.getText(modResId)
            "getString" -> modRes.getString(modResId)
            "getLayout" -> modRes.getLayout(modResId)
            "getDrawableForDensity" -> modRes.getDrawableForDensity(modResId, args[1] as Int, args[2] as Resources.Theme?)
            else -> null
        }
    }

    private fun getModuleResValue(modRes: Resources, method: String, modResId: Int, args: Array<Any?>): Any? {
        return getModuleResValue(modRes, method, modResId, args.asList())
    }

    private fun getFakeResource(modRes: Resources, method: String, args: Array<Any?>): Any? {
        return try {
            val modResId = fakes[(args[0] as Int)]
            if (modResId == 0) null else getModuleResValue(modRes, method, modResId, args)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            null
        }
    }

    private fun getResourceReplacement(modRes: Resources, method: String, args: Array<Any?>): Any? {
        val resId = args[0] as Int
        val replacement = resourceIdReplacements[resId]
        if (replacement == null) return null
        if (replacement.mType == ReplacementType.OBJECT) {
            return replacement.mValue
        }
        if (replacement.mType == ReplacementType.ID) {
            val modResId = replacement.mValue as Int
            try {
                return getModuleResValue(modRes, method, modResId, args)
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        fun getFakeResId(resourceName: String): Int {
            return 0x7e00f000 or (resourceName.hashCode() and 0x00ffffff)
        }
    }

    private var valueUpdated = false
    private var themeResourcesHooked = false
}
