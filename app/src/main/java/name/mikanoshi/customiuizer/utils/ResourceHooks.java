package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;

import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ResourceHooks {
	private SparseIntArray fakes = new SparseIntArray();
	private final HashMap<String, Integer> idReplacements = new HashMap<String, Integer>();
	private final HashMap<String, String> strReplacements = new HashMap<String, String>();
	private final HashMap<String, Object> objReplacements = new HashMap<String, Object>();

	private static int getFakeResId(String resourceName) {
		return 0x7e000000 | (resourceName.hashCode() & 0x00ffffff);
	}

	@SuppressWarnings("FieldCanBeLocal")
	private XC_MethodHook mReplaceHook = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) {
			final int resId = (int)param.args[0];
			Context mContext = null;
			try {
				Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
				if (currentActivityThread != null) mContext = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
			if (mContext == null) return;

			try {
				String method = param.method.getName();
				Object value = getFakeResource(mContext, resId, method);
				if (value == null) {
					value = getResourceReplacement(mContext, (Resources)param.thisObject, resId, method);
					if (value == null) return;
				}
				param.setResult(value);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};

	public ResourceHooks() {
		Helpers.findAndHookMethod(Resources.class, "getInteger", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getBoolean", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDimension", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDimensionPixelOffset", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getDimensionPixelSize", int.class, mReplaceHook);
		Helpers.findAndHookMethod(Resources.class, "getText", int.class, mReplaceHook);
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

	private Object getFakeResource(Context context, int resId, String method) {
		try {
			if (context == null) return null;
			int modResId = fakes.get(resId);
			if (modResId == 0) return null;

			Object value;
			Resources modRes = Helpers.getModuleRes(context);
			if ("getDrawableForDensity".equals(method))
				value = XposedHelpers.callMethod(modRes, method, modResId, 0, context.getTheme());
			else
				value = XposedHelpers.callMethod(modRes, method, modResId);
			return value;
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		return null;
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

	public void setResReplacement(String pkg, String type, String name, String replacementResName) {
		try {
			synchronized (strReplacements) {
				strReplacements.put(pkg + ":" + type + "/" + name, replacementResName);
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

	private Object getResourceReplacement(Context context, Resources res, int resId, String method) {
		if (context == null) return null;

		String pkgName = null;
		String resType = null;
		String resName = null;
		try {
			pkgName = res.getResourcePackageName(resId);
			resType = res.getResourceTypeName(resId);
			resName = res.getResourceEntryName(resId);
		} catch (Throwable t) {}
		if (pkgName == null || resType == null || resName == null) return null;

		try {
			Object value = null;
			Integer modResId = null;
			String resFullName = pkgName + ":" + resType + "/" + resName;
			synchronized (objReplacements) {
				if (objReplacements.containsKey(resFullName))
				return objReplacements.get(resFullName);
			}
			synchronized (idReplacements) {
				if (idReplacements.containsKey(resFullName)) {
					modResId = idReplacements.get(resFullName);
					if (modResId == null || modResId == 0) return null;
				}
			}
			if (modResId == null)
			synchronized (strReplacements) {
				if (strReplacements.containsKey(resFullName))
				modResId = Helpers.getModuleRes(context).getIdentifier(strReplacements.get(resFullName), resType, Helpers.modulePkg);
			}
			if (modResId == null || modResId == 0) return null;

			Resources modRes = Helpers.getModuleRes(context);
			if ("getDrawableForDensity".equals(method))
				value = XposedHelpers.callMethod(modRes, method, modResId, context.getTheme());
			else
				value = XposedHelpers.callMethod(modRes, method, modResId);
			return value;
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		return null;
	}

}