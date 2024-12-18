package name.monwf.customiuizer.mods.utils;

import android.app.MiuiThemeHelper;
import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;

public class ResourceHooks {
	static class ResourceValue {
		public Object mValue;
		public ReplacementType mType;
		public ResourceValue(ReplacementType type, Object value) {
			this.mType = type;
			this.mValue = value;
		}
	}

	public static class ThemeValue {
		public Object mNightValue;
		public Object mValue;
		public int resId = -1;

		public ThemeValue(Object value) {
			mValue = value;
			mNightValue = value;
		}
		public ThemeValue(Object value, Object nightValue) {
			mValue = value;
			mNightValue = nightValue;
		}
	}
	final private HashSet<String> hookedTypes = new HashSet<String>();
	boolean valueUpdated = false;
	boolean themeResourcesHooked = false;

	public enum ReplacementType {
		ID,
		OBJECT
	}
//	boolean replaceResourcesHooked = false;

	private final SparseIntArray fakes = new SparseIntArray();
	private final ConcurrentHashMap<String, ThemeValue> themeValueReplacements = new ConcurrentHashMap<String, ThemeValue>();
//	private final ConcurrentHashMap<String, ResourceValue> resourceReplacements = new ConcurrentHashMap<String, ResourceValue>();
	private final ConcurrentHashMap<Integer, ResourceValue> resourceIdReplacements = new ConcurrentHashMap<Integer, ResourceValue>();

	public static int getFakeResId(String resourceName) {
		return 0x7e00f000 | (resourceName.hashCode() & 0x00ffffff);
	}

	private final MethodHook mReplaceHook = new MethodHook() {
		@Override
		protected void before(final BeforeHookCallback param) {
			Context mContext = ModuleHelper.findContext();
			if (mContext == null) return;
			String method = param.getMember().getName();
			Object value = getFakeResource(mContext, method, param.getArgs());
			if (value == null) {
				if ("getLayout".equals(method)) return;
				value = getResourceReplacement(mContext, method, param.getArgs());
				if (value == null) return;
			}
			param.returnAndSkip(value);
		}
	};

	public ResourceHooks() {}

	private void initThemeHook() {
		ModuleHelper.findAndHookMethod(miui.content.res.ThemeResources.class, "mergeThemeValues", String.class, miui.content.res.ThemeValues.class, new MethodHook() {
			@Override
			protected void after(AfterHookCallback param) throws Throwable {
				String mPackageName = (String) XposedHelpers.getObjectField(param.getThisObject(), "mPackageName");
				if (mPackageName == null || "miui".equals(mPackageName)) return;
				if ((mPackageName.equals(ModuleHelper.currentPackageName)
					|| "miui.systemui.plugin".equals(mPackageName)
				) && (
					ModuleHelper.currentPackageName.equals(param.getArgs()[0])
					|| "miui.systemui.plugin".equals(param.getArgs()[0])
					)
				) {
					HashMap<Integer, Integer> themeIntValues = new HashMap<>();
					HashMap<Integer, int[]> themeIntegerArrays = new HashMap<>();
					HashMap<Integer, String[]> themeStringArrays = new HashMap<>();
					Object mThemeResources = param.getThisObject();
					Resources mResources = (Resources) XposedHelpers.getObjectField(mThemeResources, "mResources");
					boolean nightMode = XposedHelpers.getBooleanField(mThemeResources, "mNightMode");
					Object mThemeValues = param.getArgs()[1];
					HashMap<Integer, Integer> mIntegers = (HashMap<Integer, Integer>) XposedHelpers.getObjectField(mThemeValues, "mIntegers");
					HashMap<Integer, int[]> mIntegerArrays = (HashMap<Integer, int[]>)XposedHelpers.getObjectField(mThemeValues, "mIntegerArrays");
					HashMap<Integer, String[]> mStringArrays = (HashMap<Integer, String[]>)XposedHelpers.getObjectField(mThemeValues, "mStringArrays");
					for (Map.Entry<String, ThemeValue> entry : themeValueReplacements.entrySet()) {
						ThemeValue tv = entry.getValue();
						String resFullName = entry.getKey();
						String[] resMetas = resFullName.split(":|\\/");
						String themeValueType = resMetas[1];
						if (tv.resId == -1) {
							String resourceType = ("string-array".equals(themeValueType) || "integer-array".equals(themeValueType)) ? "array" : themeValueType;
							if (resMetas[0].equals(mPackageName) || "android".equals(resMetas[0])) {
								int resId = mResources.getIdentifier(resMetas[2], resourceType, resMetas[0]);
								tv.resId = resId;
							}
						}
						if (tv.resId > 0) {
							if ("string-array".equals(themeValueType)) {
								themeStringArrays.put(tv.resId, (String[]) (nightMode ? tv.mNightValue : tv.mValue));
							}
							else if ("integer-array".equals(themeValueType)) {
								themeIntegerArrays.put(tv.resId, (int[]) (nightMode ? tv.mNightValue : tv.mValue));
							}
							else {
								themeIntValues.put(tv.resId, (Integer) (nightMode ? tv.mNightValue : tv.mValue));
							}
						}
					}
					mIntegers.putAll(themeIntValues);
					mIntegerArrays.putAll(themeIntegerArrays);
					mStringArrays.putAll(themeStringArrays);
				}
			}
		});
	}

	private void initResourceIdHook(String pkg, String type, String name, ReplacementType resourceType, Object replaceValue) {
		Context mContext = ModuleHelper.findContext();
		ResourceValue rv = new ResourceValue(resourceType, replaceValue);
		if (mContext != null) {
			int resId = mContext.getResources().getIdentifier(name, type, pkg);
			if (resId > 0) resourceIdReplacements.put(resId, rv);
			else {
				XposedHelpers.log("Resource not found: " + pkg + ":" + type + "/" + name);
			}
		}
		else {
			XposedHelpers.log("Context not found: " + pkg + ":" + type + "/" + name);
//			resourceReplacements.put(pkg + ":" + type + "/" + name, rv);
		}
//		if (!replaceResourcesHooked) {
//			replaceResourcesHooked = true;
//			ModuleHelper.findAndHookMethod(android.content.res.MiuiResources.class, "init", String.class, new MethodHook() {
//				@Override
//				protected void after(AfterHookCallback param) throws Throwable {
//
//				}
//			});
//		}
	}

	private void applyHooks(String type) {
		if (hookedTypes.contains(type)) return;
		hookedTypes.add(type);
		switch (type) {
			case "layout" -> {
				ModuleHelper.findAndHookMethod(Resources.class, "getLayout", int.class, mReplaceHook);
			}
			case "string" -> {
				ModuleHelper.findAndHookMethod(Resources.class, "getText", int.class, mReplaceHook);
				ModuleHelper.findAndHookMethod(Resources.class, "getString", int.class, mReplaceHook);
			}
			case "drawable" -> {
				ModuleHelper.findAndHookMethod(Resources.class, "getDrawableForDensity", int.class, int.class, Resources.Theme.class, mReplaceHook);
			}
		}
	}

	/**
	 * add fake resources which can be replaced by module resources. eg: drawable, string, layout
	 *
	 * @param resName resource name
	 * @param resId module resource id
	 * @param type resource type
	 * @return fake resource id
	 */
	public int addFakeResource(String resName, int resId, String type) {
		try {
			int fakeResId = getFakeResId(resName);
			fakes.put(fakeResId, resId);
			applyHooks(type);
			return fakeResId;
		} catch (Throwable t) {
			XposedHelpers.log(t);
			return 0;
		}
	}

	/**
	 * replace package resources with module resources
	 *
	 * @param pkg package name. * for all packages
	 * @param type resource type
	 * @param name resource name
	 * @param replacementResId module resource id
	 */
	public void setResReplacement(String pkg, String type, String name, int replacementResId) {
		try {
			initResourceIdHook(pkg, type, name, ReplacementType.ID, replacementResId);
			applyHooks(type);
		} catch (Throwable t) {
			XposedHelpers.log(t);
		}
	}

	/**
	 * replace package resources with replacement value
	 *
	 * @param pkg package name. * for all packages
	 * @param type resource type
	 * @param name resource name
	 * @param replacementResValue replacement value
	 */
	public void setObjectReplacement(String pkg, String type, String name, Object replacementResValue) {
		try {
			initResourceIdHook(pkg, type, name, ReplacementType.OBJECT, replacementResValue);
			applyHooks(type);
		} catch (Throwable t) {
			XposedHelpers.log(t);
		}
	}

	public void setThemeValueReplacement(String pkg, String type, String name, Object resValue) {
		setThemeValueReplacement(pkg, type, name, resValue, resValue);
	}

	public void setThemeValueReplacement(String pkg, String type, String name, Object resValue, Object nightResValue) {
		if ("bool".equals(type)) {
			resValue = ((boolean) resValue == true) ? 1 : 0;
			nightResValue = ((boolean) nightResValue == true) ? 1 : 0;
		}
		else if ("dimen".equals(type)) {
			String valInDimen = resValue + "dp";
			nightResValue = resValue = MiuiThemeHelper.parseDimension(valInDimen);
		}
		themeValueReplacements.put(pkg + ":" + type + "/" + name, new ThemeValue(resValue, nightResValue));
		valueUpdated = true;
		if (!themeResourcesHooked) {
			themeResourcesHooked = true;
			initThemeHook();
		}
	}

	private Object getModuleResValue(Resources modRes, String method, int modResId, Object[] args) {
		Object value;
		if ("getDrawableForDensity".equals(method))
			value = XposedHelpers.callMethod(modRes, method, modResId, args[1], args[2]);
		else
			value = XposedHelpers.callMethod(modRes, method, modResId);
		return value;
	}

	private Object getFakeResource(Context context, String method, Object[] args) {
		try {
			int modResId = fakes.get((int)args[0]);
			if (modResId == 0) return null;
			Resources modRes = ModuleHelper.getModuleRes(context);
			return getModuleResValue(modRes, method, modResId, args);
		} catch (Throwable t) {
			XposedHelpers.log(t);
			return null;
		}
	}

	private Object getResourceReplacement(Context context, String method, Object[] args) {
		int resId = (int)args[0];
		if (resourceIdReplacements.containsKey(resId)) {
			ResourceValue replacement = resourceIdReplacements.get(resId);
			if (replacement.mType == ReplacementType.OBJECT) {
				return replacement.mValue;
			}
			if (replacement.mType == ReplacementType.ID) {
				int modResId = (int)replacement.mValue;
				try {
					Resources modRes = ModuleHelper.getModuleRes(context);
					return getModuleResValue(modRes, method, modResId, args);
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
			}
		}
		return null;
	}
}