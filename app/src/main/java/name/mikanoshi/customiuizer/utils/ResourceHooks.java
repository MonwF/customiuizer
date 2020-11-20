package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;

import java.util.HashMap;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

public class ResourceHooks {
	private final SparseIntArray fakes = new SparseIntArray();
	private final HashMap<String, Integer> idReplacements = new HashMap<String, Integer>();
	private final HashMap<String, Integer> densityReplacements = new HashMap<String, Integer>();
	private final HashMap<String, Object> objReplacements = new HashMap<String, Object>();

	private static int getFakeResId(String resourceName) {
		return 0x7e000000 | (resourceName.hashCode() & 0x00ffffff);
	}

	@SuppressWarnings("FieldCanBeLocal")
	private final MethodHook mReplaceHook = new MethodHook() {
		@Override
		protected void before(MethodHookParam param) {
			Context mContext = null;
			try {
				Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
				if (currentActivityThread != null) mContext = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
			if (mContext == null) return;

			String method = param.method.getName();
			Object value = getFakeResource(mContext, method, param.args);
			if (value == null) {
				value = getResourceReplacement(mContext, (Resources)param.thisObject, method, param.args);
				if (value == null) return;
				if ("getDimensionPixelOffset".equals(method) || "getDimensionPixelSize".equals(method))
				if (value instanceof Float) value = ((Float)value).intValue();
			}
			param.setResult(value);
		}
	};

	public ResourceHooks() {
		Helpers.findAndHookMethod(Resources.class, "getInteger", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getFraction", int.class, int.class, int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getBoolean", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDimension", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDimensionPixelOffset", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDimensionPixelSize", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getText", int.class, mReplaceHook);
		if (Helpers.isNougat())
		Helpers.findAndHookMethod(Resources.class, "getDrawable", int.class, Resources.Theme.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDrawableForDensity", int.class, int.class, Resources.Theme.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getIntArray", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getStringArray", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getTextArray", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getAnimation", int.class, mReplaceHook);
	}

	public int addResource(String resName, int resId) {
		try {
			int fakeResId = getFakeResId(resName);
			fakes.put(fakeResId, resId);
			return fakeResId;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return 0;
		}
	}

	private Object getFakeResource(Context context, String method, Object[] args) {
		try {
			if (context == null) return null;
			int modResId = fakes.get((int)args[0]);
			if (modResId == 0) return null;

			Object value;
			Resources modRes = Helpers.getModuleRes(context);
			if ("getDrawable".equals(method))
				value = XposedHelpers.callMethod(modRes, method, modResId, args[1]);
			else if ("getDrawableForDensity".equals(method) || "getFraction".equals(method))
				value = XposedHelpers.callMethod(modRes, method, modResId, args[1], args[2]);
			else
				value = XposedHelpers.callMethod(modRes, method, modResId);
			return value;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	public void setResReplacement(String pkg, String type, String name, int replacementResId) {
		try {
			synchronized (idReplacements) {
				idReplacements.put(pkg + ":" + type + "/" + name, replacementResId);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public void setDensityReplacement(String pkg, String type, String name, Integer replacementResValue) {
		try {
			synchronized (densityReplacements) {
				densityReplacements.put(pkg + ":" + type + "/" + name, replacementResValue);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public void setObjectReplacement(String pkg, String type, String name, Object replacementResValue) {
		try {
			synchronized (objReplacements) {
				objReplacements.put(pkg + ":" + type + "/" + name, replacementResValue);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private Object getResourceReplacement(Context context, Resources res, String method, Object[] args) {
		if (context == null) return null;

		String pkgName = null;
		String resType = null;
		String resName = null;
		try {
			pkgName = res.getResourcePackageName((int)args[0]);
			resType = res.getResourceTypeName((int)args[0]);
			resName = res.getResourceEntryName((int)args[0]);
		} catch (Throwable t) {}
		if (pkgName == null || resType == null || resName == null) return null;

		try {
			Object value;
			String resFullName = pkgName + ":" + resType + "/" + resName;
			String resAnyPkgName = "*:" + resType + "/" + resName;

			synchronized (objReplacements) {
				if (objReplacements.containsKey(resFullName)) return objReplacements.get(resFullName);
				else if (objReplacements.containsKey(resAnyPkgName)) return objReplacements.get(resAnyPkgName);
			}

			synchronized (densityReplacements) {
				if (densityReplacements.containsKey(resFullName))
					return densityReplacements.get(resFullName) * res.getDisplayMetrics().density;
				else if (densityReplacements.containsKey(resAnyPkgName))
					return densityReplacements.get(resAnyPkgName) * res.getDisplayMetrics().density;
			}

			Integer modResId = null;
			synchronized (idReplacements) {
				if (idReplacements.containsKey(resFullName))
					modResId = idReplacements.get(resFullName);
				else if (idReplacements.containsKey(resAnyPkgName))
					modResId = idReplacements.get(resAnyPkgName);
			}
			if (modResId == null) return null;

			Resources modRes = Helpers.getModuleRes(context);
			if ("getDrawable".equals(method))
				value = XposedHelpers.callMethod(modRes, method, modResId, args[1]);
			else if ("getDrawableForDensity".equals(method) || "getFraction".equals(method))
				value = XposedHelpers.callMethod(modRes, method, modResId, args[1], args[2]);
			else
				value = XposedHelpers.callMethod(modRes, method, modResId);
			return value;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

}