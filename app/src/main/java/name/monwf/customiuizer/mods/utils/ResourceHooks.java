package name.monwf.customiuizer.mods.utils;

import android.app.MiuiThemeHelper;
import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;
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
		public String pkg;
		public String name;
		public String themeValueType;
		public String resourceType;

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
	private final SparseIntArray fakes = new SparseIntArray();
	private final ConcurrentHashMap<String, ThemeValue> themeValueReplacements = new ConcurrentHashMap<String, ThemeValue>();
	private final ConcurrentHashMap<Integer, ResourceValue> resourceIdReplacements = new ConcurrentHashMap<Integer, ResourceValue>();

	public static int getFakeResId(String resourceName) {
		return 0x7e00f000 | (resourceName.hashCode() & 0x00ffffff);
	}

	private final MethodHook mReplaceHook = new MethodHook() {
		@Override
		public Object intercept(XposedInterface.Chain chain) throws Throwable {
			Object skipValue = null;
			boolean shouldSkip = false;
			boolean replacementHandled = false;
			try {
				List<Object> args = chain.getArgs();
				Object resIdObj = args.get(0);
				int resId = (Integer) resIdObj;
				String method = chain.getExecutable().getName();
				ResourceValue replacement = resourceIdReplacements.get(resIdObj);
				if (replacement != null) {
					replacementHandled = true;
					if (replacement.mType == ReplacementType.OBJECT) {
						skipValue = replacement.mValue;
						shouldSkip = true;
					} else if ("getLayout".equals(method)) {
						// proceed original, do not check fakes
					} else {
						Context mContext = ModuleHelper.findContext();
						if (mContext != null) {
							Resources modRes = ModuleHelper.getModuleRes(mContext);
							if (modRes != null) {
								Object value = getModuleResValue(modRes, method, (int) replacement.mValue, args);
								if (value != null) {
									skipValue = value;
									shouldSkip = true;
								}
							}
						}
					}
				}
				if (!shouldSkip && !replacementHandled) {
					int modResId = fakes.get(resId);
					if (modResId != 0) {
						Context mContext = ModuleHelper.findContext();
						if (mContext != null) {
							Resources modRes = ModuleHelper.getModuleRes(mContext);
							if (modRes != null) {
								Object value = getModuleResValue(modRes, method, modResId, args);
								if (value != null) {
									skipValue = value;
									shouldSkip = true;
								}
							}
						}
					}
				}
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
			if (shouldSkip) return skipValue;
			return chain.proceed();
		}
	};

	public ResourceHooks() {}

	private void initThemeHook() {
		ModuleHelper.findAndHookMethod(miui.content.res.ThemeResources.class, "mergeThemeValues", String.class, miui.content.res.ThemeValues.class, new MethodHook() {
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
					Object mThemeResources = chain.getThisObject();
					String mPackageName = (String) XposedHelpers.getObjectField(mThemeResources, "mPackageName");
					if (mPackageName != null && !"miui".equals(mPackageName) && (
						mPackageName.equals(ModuleHelper.currentPackageName)
						|| "miui.systemui.plugin".equals(mPackageName)
					)) {
						List<Object> args = chain.getArgs();
						if (args.size() > 1 && (
							ModuleHelper.currentPackageName.equals(args.get(0))
							|| "miui.systemui.plugin".equals(args.get(0))
						)) {
							HashMap<Integer, Integer> themeIntValues = new HashMap<>();
							HashMap<Integer, int[]> themeIntegerArrays = new HashMap<>();
							HashMap<Integer, String[]> themeStringArrays = new HashMap<>();
							Resources mResources = (Resources) XposedHelpers.getObjectField(mThemeResources, "mResources");
							boolean nightMode = XposedHelpers.getBooleanField(mThemeResources, "mNightMode");
							Object mThemeValues = args.get(1);
							HashMap<Integer, Integer> mIntegers = (HashMap<Integer, Integer>) XposedHelpers.getObjectField(mThemeValues, "mIntegers");
							HashMap<Integer, int[]> mIntegerArrays = (HashMap<Integer, int[]>) XposedHelpers.getObjectField(mThemeValues, "mIntegerArrays");
							HashMap<Integer, String[]> mStringArrays = (HashMap<Integer, String[]>) XposedHelpers.getObjectField(mThemeValues, "mStringArrays");
							for (Map.Entry<String, ThemeValue> entry : themeValueReplacements.entrySet()) {
								ThemeValue tv = entry.getValue();
								if (tv.resId == -1) {
									if (tv.pkg.equals(mPackageName) || "android".equals(tv.pkg)) {
										tv.resId = mResources.getIdentifier(tv.name, tv.resourceType, tv.pkg);
									}
								}
								if (tv.resId > 0) {
									if ("string-array".equals(tv.themeValueType)) {
										themeStringArrays.put(tv.resId, (String[]) (nightMode ? tv.mNightValue : tv.mValue));
									} else if ("integer-array".equals(tv.themeValueType)) {
										themeIntegerArrays.put(tv.resId, (int[]) (nightMode ? tv.mNightValue : tv.mValue));
									} else {
										themeIntValues.put(tv.resId, (Integer) (nightMode ? tv.mNightValue : tv.mValue));
									}
								}
							}
							mIntegers.putAll(themeIntValues);
							mIntegerArrays.putAll(themeIntegerArrays);
							mStringArrays.putAll(themeStringArrays);
						}
					}
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
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
		}
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
		ThemeValue tv = new ThemeValue(resValue, nightResValue);
		tv.pkg = pkg;
		tv.name = name;
		tv.themeValueType = type;
		tv.resourceType = ("string-array".equals(type) || "integer-array".equals(type)) ? "array" : type;
		themeValueReplacements.put(pkg + ":" + type + "/" + name, tv);
		valueUpdated = true;
		if (!themeResourcesHooked) {
			themeResourcesHooked = true;
			initThemeHook();
		}
	}

	private Object getModuleResValue(Resources modRes, String method, int modResId, List<Object> args) {
		switch (method) {
			case "getText":
				return modRes.getText(modResId);
			case "getString":
				return modRes.getString(modResId);
			case "getLayout":
				return modRes.getLayout(modResId);
			case "getDrawableForDensity":
				return modRes.getDrawableForDensity(modResId, (int) args.get(1), (Resources.Theme) args.get(2));
			default:
				return null;
		}
	}

	private Object getModuleResValue(Resources modRes, String method, int modResId, Object[] args) {
		return getModuleResValue(modRes, method, modResId, java.util.Arrays.asList(args));
	}

	private Object getFakeResource(Resources modRes, String method, Object[] args) {
		try {
			int modResId = fakes.get((int)args[0]);
			if (modResId == 0) return null;
			return getModuleResValue(modRes, method, modResId, args);
		} catch (Throwable t) {
			XposedHelpers.log(t);
			return null;
		}
	}

	private Object getResourceReplacement(Resources modRes, String method, Object[] args) {
		int resId = (int)args[0];
		ResourceValue replacement = resourceIdReplacements.get(resId);
		if (replacement == null) return null;
		if (replacement.mType == ReplacementType.OBJECT) {
			return replacement.mValue;
		}
		if (replacement.mType == ReplacementType.ID) {
			int modResId = (int)replacement.mValue;
			try {
				return getModuleResValue(modRes, method, modResId, args);
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
		}
		return null;
	}
}
