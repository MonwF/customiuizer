package name.mikanoshi.customiuizer.mods;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.ShakeManager;

public class Launcher {

//	private static GestureDetector mDetector;
	private static GestureDetector mDetectorHorizontal;

	public static void HomescreenSwipesHook(final LoadPackageParam lpparam) {
//		// Detect vertical swipes
//		Helpers.findAndHookMethod("com.miui.home.launcher.ForceTouchLayer", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				Context helperContext = ((ViewGroup)param.thisObject).getContext();
//
//				if (helperContext == null) return;
//				if (mDetector == null) mDetector = new GestureDetector(helperContext, new SwipeListener(helperContext));
//
//				MotionEvent ev = (MotionEvent)param.args[0];
//				if (ev == null) return;
//				mDetector.onTouchEvent(ev);
//			}
//		});

//		if (MainModule.pref_swipedown != 1)
//		Helpers.findAndHookMethod("com.miui.launcher.utils.LauncherUtils", lpparam.classLoader, "expandStatusBar", Context.class, XC_MethodReplacement.DO_NOTHING);
//
//		if (MainModule.pref_swipeup != 1)
//		Helpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				param.setResult(false);
//			}
//		});

		Helpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onVerticalGesture", int.class, MotionEvent.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if ((boolean)XposedHelpers.callMethod(param.thisObject, "isInNormalEditingMode")) return;
				String key = null;
				Context helperContext = ((ViewGroup)param.thisObject).getContext();
				int numOfFingers = 1;
				if (param.args[1] != null) numOfFingers = ((MotionEvent)param.args[1]).getPointerCount();
				if ((int)param.args[0] == 11) {
					if (numOfFingers == 1)
						key = "pref_key_launcher_swipedown";
					else if (numOfFingers == 2)
						key = "pref_key_launcher_swipedown2";
					if (GlobalActions.handleAction(helperContext, key)) param.setResult(true);
				} else if ((int)param.args[0] == 10) {
					if (numOfFingers == 1)
						key = "pref_key_launcher_swipeup";
					else if (numOfFingers == 2)
						key = "pref_key_launcher_swipeup2";
					if (GlobalActions.handleAction(helperContext, key)) param.setResult(true);
				}
			}
		});

		if (!Helpers.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isGlobalSearchEnable", Context.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipeup_action", 1) > 1) param.setResult(false);
			}
		})) if (!Helpers.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipeup_action", 1) > 1) param.setResult(false);
			}
		})) if (lpparam.packageName.equals("com.miui.home")) Helpers.log("HomescreenSwipesHook", "Cannot disable swipe up search");
	}

//	// Listener for vertical swipe gestures
//	private static class SwipeListener extends GestureDetector.SimpleOnGestureListener {
//
//		private int SWIPE_MIN_DISTANCE = 300;
//		private int SWIPE_MAX_OFF_PATH = 250;
//		private int SWIPE_THRESHOLD_VELOCITY = 200;
//
//		final Context helperContext;
//
//		SwipeListener(Context context) {
//			helperContext = context;
//			float density = helperContext.getResources().getDisplayMetrics().density;
//			SWIPE_MIN_DISTANCE = Math.round(100 * density);
//			SWIPE_MAX_OFF_PATH = Math.round(85 * density);
//			SWIPE_THRESHOLD_VELOCITY = Math.round(65 * density);
//		}
//
//		@Override
//		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//			if (e1 == null || e2 == null) return false;
//			if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) return false;
//
//			if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//				//MainModule.pref.reload();
//				XposedBridge.log("Swiped DOWN! " + String.valueOf(MainModule.pref.getInt("pref_key_launcher_swipedown_action", 1)));
//				return GlobalActions.handleAction(MainModule.pref.getInt("pref_key_launcher_swipedown_action", 1), 1, MainModule.pref.getInt("pref_key_launcher_swipedown_toggle", 0), helperContext);
//			}
//
//			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//				//MainModule.pref.reload();
//				XposedBridge.log("Swiped UP! " + String.valueOf(MainModule.pref.getInt("pref_key_launcher_swipeup_action", 1)));
//				return GlobalActions.handleAction(MainModule.pref.getInt("pref_key_launcher_swipeup_action", 1), 2, MainModule.pref.getInt("pref_key_launcher_swipeup_toggle", 0), helperContext);
//			}
//
//			return false;
//		}
//	}

	public static void HotSeatSwipesHook(final LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.HotSeats", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev == null) return;

				ViewGroup hotSeat = (ViewGroup)param.thisObject;
				Context helperContext = hotSeat.getContext();
				if (helperContext == null) return;
				if (mDetectorHorizontal == null) mDetectorHorizontal = new GestureDetector(helperContext, new SwipeListenerHorizontal(hotSeat));
				mDetectorHorizontal.onTouchEvent(ev);
			}
		});

//		Helpers.findAndHookMethod("com.htc.launcher.DragLayer", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				MotionEvent ev = (MotionEvent)param.args[0];
//				if (ev == null) return;
//
//				FrameLayout dragLayer = (FrameLayout)param.thisObject;
//				launcher = XposedHelpers.getObjectField(dragLayer, "m_launcher");
//				Context helperContext = dragLayer.getContext();
//				if (helperContext == null) return;
//				if (mDetectorVertical == null) mDetectorVertical = new GestureDetector(helperContext, new SwipeListenerVertical(helperContext));
//				if (mDetectorVertical.onTouchEvent(ev)) param.setResult(true);
//			}
//		});
	}

	// Listener for horizontal swipes on hotseats
	private static class SwipeListenerHorizontal extends GestureDetector.SimpleOnGestureListener {

		private int SWIPE_MIN_DISTANCE_HORIZ;
		private int SWIPE_THRESHOLD_VELOCITY;

		final Context helperContext;

		SwipeListenerHorizontal(Object cellLayout) {
			helperContext = ((ViewGroup)cellLayout).getContext();
			float density = helperContext.getResources().getDisplayMetrics().density;
			SWIPE_MIN_DISTANCE_HORIZ = Math.round(75 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(33 * density);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;

			if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			return GlobalActions.handleAction(helperContext, "pref_key_launcher_swiperight");

			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			return GlobalActions.handleAction(helperContext, "pref_key_launcher_swipeleft");

			return false;
		}
	}

	public static void ShakeHook(final LoadPackageParam lpparam) {
		final String shakeMgrKey = "MIUIZER_SHAKE_MGR";

		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onResume", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				ShakeManager shakeMgr = (ShakeManager)XposedHelpers.getAdditionalInstanceField(param.thisObject, shakeMgrKey);
				if (shakeMgr == null) {
					shakeMgr = new ShakeManager((Context)param.thisObject);
					XposedHelpers.setAdditionalInstanceField(param.thisObject, shakeMgrKey, shakeMgr);
				}
				Activity launcherActivity = (Activity)param.thisObject;
				SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
				shakeMgr.reset();
				sensorMgr.registerListener(shakeMgr, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onPause", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if (XposedHelpers.getAdditionalInstanceField(param.thisObject, shakeMgrKey) == null) return;
				Activity launcherActivity = (Activity)param.thisObject;
				SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
				sensorMgr.unregisterListener((ShakeManager)XposedHelpers.getAdditionalInstanceField(param.thisObject, shakeMgrKey));
			}
		});
	}

	private static Class<?> wallpaperUtilsCls = null;
	@SuppressWarnings("ConstantConditions")
	private static void applyFolderShade(View folder) {
		int opt = Integer.parseInt(Helpers.getSharedStringPref(folder.getContext(), "pref_key_launcher_foldershade", "1"));
		int level = Helpers.getSharedIntPref(folder.getContext(), "pref_key_launcher_foldershade_level", 40);
		MainModule.mPrefs.put("pref_key_launcher_foldershade", String.valueOf(opt));
		MainModule.mPrefs.put("pref_key_launcher_foldershade_level", level);
		if (opt == 2) {
			boolean isLight = false;
			if (wallpaperUtilsCls != null) try { isLight = (boolean)XposedHelpers.callStaticMethod(wallpaperUtilsCls, "hasAppliedLightWallpaper"); } catch (Throwable t) {}
			int bgcolor = (isLight ? 0x00ffffff : 0x00000000) | (Math.round(255 * level / 100f) * 0x1000000);
			folder.setBackground(new ColorDrawable(bgcolor));
		} else if (opt == 3) {
			PaintDrawable pd = new PaintDrawable();
			pd.setShape(new RectShape());
			pd.setShaderFactory(new ShapeDrawable.ShaderFactory() {
				@Override
				public Shader resize(int width, int height) {
					boolean isLight = false;
					if (wallpaperUtilsCls != null) try { isLight = (boolean)XposedHelpers.callStaticMethod(wallpaperUtilsCls, "hasAppliedLightWallpaper"); } catch (Throwable t) {}
					int bgcolor1 = (isLight ? 0x00ffffff : 0x00000000) | (Math.round(255 / 6f * level / 100f) * 0x1000000);
					int bgcolor2 = (isLight ? 0x00ffffff : 0x00000000) | (Math.round(255 * level / 100f) * 0x1000000);
					return new LinearGradient(0, 0, 0, height,
							new int[]{ bgcolor1, bgcolor2, bgcolor2, bgcolor1 },
							new float[]{ 0.0f, 0.25f, 0.65f, 1.0f },
							Shader.TileMode.CLAMP
					);
				}
			});
			folder.setBackground(pd);
		} else folder.setBackground(null);
	}

	public static void FolderShadeHook(final LoadPackageParam lpparam) {
		wallpaperUtilsCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader);

		Helpers.hookAllConstructors("com.miui.home.launcher.FolderCling", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				applyFolderShade((View)param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.classLoader, "onWallpaperColorChanged", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				applyFolderShade((View)param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.classLoader, "setBackgroundAlpha", float.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				int opt = MainModule.mPrefs.getStringAsInt("launcher_foldershade", 1);
				if (opt == 1) return;
				Object mLauncher = XposedHelpers.getObjectField(param.thisObject, "mLauncher");
				if (mLauncher == null) return;
				View folderCling = (View)XposedHelpers.callMethod(mLauncher, "getFolderCling");
				if (folderCling == null) return;
				Drawable bkg = folderCling.getBackground();
				if (bkg != null) bkg.setAlpha(Math.round((float)param.args[0] * 255));
			}
		});
	}

	public static void NoClockHideHook(final LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "isScreenHasClockGadget", long.class, XC_MethodReplacement.returnConstant(false));
	}

	private static void modifyTitle(Object thisObject) {
		boolean isApplicatoin = (boolean)XposedHelpers.callMethod(thisObject, "isApplicatoin");
		if (!isApplicatoin) return;
		String pkgName = (String)XposedHelpers.callMethod(thisObject, "getPackageName");
		String actName = (String)XposedHelpers.callMethod(thisObject, "getClassName");
		String newTitle = MainModule.mPrefs.getString("launcher_renameapps_list:" + pkgName + "|" + actName, "");
		if (!TextUtils.isEmpty(newTitle)) XposedHelpers.setObjectField(thisObject, "mLabel", newTitle);
	}

	public static void RenameShortcutsHook(final LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				Handler mHandler = (Handler)XposedHelpers.getObjectField(act, "mHandler");
				new Helpers.SharedPrefObserver(act, mHandler) {
					@Override
					public void onChange(Uri uri) {
						try {
							String type = uri.getPathSegments().get(1);
							if (!type.equals("string")) return;
							String key = uri.getPathSegments().get(2);
							if (!key.contains("pref_key_launcher_renameapps_list")) return;
							CharSequence newTitle = Helpers.getSharedStringPref(act, key, "");
							MainModule.mPrefs.put(key, newTitle);
							HashSet<?> mAllLoadedApps;
							if (XposedHelpers.findFieldIfExists(param.thisObject.getClass(), "mAllLoadedShortcut") != null)
								mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(param.thisObject, "mAllLoadedShortcut");
							else if (XposedHelpers.findFieldIfExists(param.thisObject.getClass(), "mAllLoadedApps") != null)
								mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(param.thisObject, "mAllLoadedApps");
							else
								mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(param.thisObject, "mLoadedAppsAndShortcut");
							if (mAllLoadedApps != null)
							for (Object shortcut: mAllLoadedApps) {
								boolean isApplicatoin = (boolean)XposedHelpers.callMethod(shortcut, "isApplicatoin");
								if (!isApplicatoin) continue;
								String pkgName = (String)XposedHelpers.callMethod(shortcut, "getPackageName");
								String actName = (String)XposedHelpers.callMethod(shortcut, "getClassName");
								if (("pref_key_launcher_renameapps_list:" + pkgName + "|" + actName).equals(key)) {
									CharSequence newStr = TextUtils.isEmpty(newTitle) ? (CharSequence)XposedHelpers.getAdditionalInstanceField(shortcut, "mLabelOrig") : newTitle;
									XposedHelpers.setObjectField(shortcut, "mLabel", newStr);
									if (lpparam.packageName.equals("com.miui.home")) {
										XposedHelpers.callMethod(shortcut, "updateBuddyIconView", act);
									} else {
										Object buddyIconView = XposedHelpers.callMethod(shortcut, "getBuddyIconView");
										if (buddyIconView != null) XposedHelpers.callMethod(buddyIconView, "updateInfo", param.thisObject, shortcut);
									}
									break;
								}
							}
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
			}
		});

		Helpers.hookAllConstructors("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLabelOrig", XposedHelpers.getObjectField(param.thisObject, "mLabel"));
				if (param.args != null && param.args.length > 0) modifyTitle(param.thisObject);
			}
		});

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "loadSettingsInfo", Context.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLabelOrig", XposedHelpers.getObjectField(param.thisObject, "mLabel"));
				modifyTitle(param.thisObject);
			}
		});

		if (!Helpers.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "setLabelAndUpdateDB", CharSequence.class, Context.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLabelOrig", param.args[0]);
				modifyTitle(param.thisObject);
			}
		})) if (!Helpers.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "setLabel", CharSequence.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLabelOrig", param.args[0]);
				modifyTitle(param.thisObject);
			}
		})) Helpers.findAndHookMethod("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "setLabel", CharSequence.class, Context.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLabelOrig", param.args[0]);
				modifyTitle(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "load", Context.class, Cursor.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				modifyTitle(param.thisObject);
			}
		});

		Helpers.hookAllMethodsSilently("com.miui.home.launcher.AppInfo", lpparam.classLoader, "resetTitle", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				modifyTitle(param.thisObject);
			}
		});
	}

	public static void CloseFolderOnLaunchHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "launch", "com.miui.home.launcher.ShortcutInfo", View.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean mHasLaunchedAppFromFolder = XposedHelpers.getBooleanField(param.thisObject, "mHasLaunchedAppFromFolder");
				if (mHasLaunchedAppFromFolder) XposedHelpers.callMethod(param.thisObject, "closeFolder");
			}
		});
	}

	public static void FSGesturesHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "usingFsGesture", XC_MethodReplacement.returnConstant(true));
	}

	public static void FixStatusBarModeHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "changeStatusBarMode", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				boolean mDuringMinusOneStartActivityForResult = XposedHelpers.getBooleanField(act, "mDuringMinusOneStartActivityForResult");
				boolean isMinusScreenShowing = (boolean)XposedHelpers.callMethod(act, "isMinusScreenShowing");
				if (!mDuringMinusOneStartActivityForResult) {
					if (isMinusScreenShowing) {
						XposedHelpers.setBooleanField(act, "mNeedChangeStatusBarMode", true);
					} else {
						XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.miui.launcher.utils.MiuiWindowManagerUtils", lpparam.classLoader),
							"changeStatusBarMode",
							act.getWindow(),
							XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader), "hasLightBgForStatusBar")
						);
						XposedHelpers.setBooleanField(act, "mNeedChangeStatusBarMode", false);
					}
				}
				param.setResult(null);
			}
		});
	}

	@SuppressWarnings("FieldCanBeLocal")
	public static class DoubleTapController {
		private final long MAX_DURATION = 500;
		private float mActionDownRawX;
		private float mActionDownRawY;
		private int mClickCount;
		private final Context mContext;
		private float mFirstClickRawX;
		private float mFirstClickRawY;
		private long mLastClickTime;
		private int mTouchSlop;

		DoubleTapController(Context context) {
			this.mContext = context;
			this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
		}

		boolean isDoubleTapEvent(MotionEvent motionEvent) {
			int action = motionEvent.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN) {
				this.mActionDownRawX = motionEvent.getRawX();
				this.mActionDownRawY = motionEvent.getRawY();
				return false;
			} else if (action != MotionEvent.ACTION_UP) {
				return false;
			} else {
				float rawX = motionEvent.getRawX();
				float rawY = motionEvent.getRawY();
				if (Math.abs(rawX - this.mActionDownRawX) <= ((float) this.mTouchSlop) && Math.abs(rawY - this.mActionDownRawY) <= ((float) this.mTouchSlop)) {
					if (SystemClock.elapsedRealtime() - this.mLastClickTime > MAX_DURATION || rawY - this.mFirstClickRawY > (float)this.mTouchSlop || rawX - this.mFirstClickRawX > (float)this.mTouchSlop) {
						this.mClickCount = 0;
					}
					this.mClickCount++;
					if (this.mClickCount == 1) {
						this.mFirstClickRawX = rawX;
						this.mFirstClickRawY = rawY;
						this.mLastClickTime = SystemClock.elapsedRealtime();
						return false;
					} else if (Math.abs(rawY - this.mFirstClickRawY) <= ((float) this.mTouchSlop) && Math.abs(rawX - this.mFirstClickRawX) <= ((float) this.mTouchSlop) && SystemClock.elapsedRealtime() - this.mLastClickTime <= MAX_DURATION) {
						this.mClickCount = 0;
						return true;
					}
				}
				this.mClickCount = 0;
				return false;
			}
		}

		void onDoubleTapEvent() {
			GlobalActions.handleAction(mContext, "pref_key_launcher_doubletap");
		}
	}

	public static void LauncherDoubleTapHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.miui.home.launcher.Workspace", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Object mDoubleTapControllerEx = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mDoubleTapControllerEx");
				if (mDoubleTapControllerEx != null) return;
				mDoubleTapControllerEx = new DoubleTapController((Context)param.args[0]);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mDoubleTapControllerEx", mDoubleTapControllerEx);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				DoubleTapController mDoubleTapControllerEx = (DoubleTapController)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mDoubleTapControllerEx");
				if (mDoubleTapControllerEx == null) return;
				if (!mDoubleTapControllerEx.isDoubleTapEvent((MotionEvent)param.args[0])) return;
				int mCurrentScreenIndex = XposedHelpers.getIntField(param.thisObject, lpparam.packageName.equals("com.miui.home") ? "mCurrentScreenIndex" : "mCurrentScreen");
				Object cellLayout = XposedHelpers.callMethod(param.thisObject, "getCellLayout", mCurrentScreenIndex);
				if ((boolean)XposedHelpers.callMethod(cellLayout, "lastDownOnOccupiedCell")) return;
				if ((boolean)XposedHelpers.callMethod(param.thisObject, "isInNormalEditingMode")) return;
				mDoubleTapControllerEx.onDoubleTapEvent();
			}
		});
	}

	public static void TitleShadowHook(LoadPackageParam lpparam) {
		if (lpparam.packageName.equals("com.miui.home"))
		Helpers.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader, "getIconTitleShadowColor", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				int color = (int)param.getResult();
				if (color == Color.TRANSPARENT) return;
				param.setResult(Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color)));
			}
		}); else
		Helpers.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader, "getTitleShadowColor", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				int color = (int)param.getResult();
				if (color == Color.TRANSPARENT) return;
				param.setResult(Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color)));
			}
		});
	}

	public static void HideNavBarHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "loadScreenSize", Context.class, Resources.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Settings.Global.putInt(((Context)param.args[0]).getContentResolver(), "force_immersive_nav_bar", 1);
			}
		});
	}

	private static void showSeekBar(View workspace) {
		if (!"Workspace".equals(workspace.getClass().getSimpleName())) return;
		boolean isInEditingMode = (boolean)XposedHelpers.callMethod(workspace, "isInNormalEditingMode");
		View mScreenSeekBar = (View)XposedHelpers.getObjectField(workspace, "mScreenSeekBar");
		if (mScreenSeekBar == null) {
			Helpers.log("HideSeekPointsHook", "Cannot find seekbar");
			return;
		}
		Context mContext = workspace.getContext();
		Handler mHandler = (Handler)XposedHelpers.getAdditionalInstanceField(workspace, "mHandlerEx");
		if (mHandler == null) {
			mHandler = new Handler(mContext.getMainLooper()) {
				@Override
				public void handleMessage(Message msg) {
					View seekBar = (View)msg.obj;
					if (seekBar != null)
					seekBar.animate().alpha(0.0f).setDuration(600).withEndAction(new Runnable() {
						@Override
						public void run() {
							seekBar.setVisibility(View.GONE);
						}
					});
				}
			};
			XposedHelpers.setAdditionalInstanceField(workspace, "mHandlerEx", mHandler);
		}
		if (mHandler == null) {
			Helpers.log("HideSeekPointsHook", "Cannot create handler");
			return;
		}
		if (mHandler.hasMessages(666)) mHandler.removeMessages(666);
		mScreenSeekBar.animate().cancel();
		mScreenSeekBar.setVisibility(View.VISIBLE);
		mScreenSeekBar.animate().alpha(1.0f).setDuration(300);
		if (!isInEditingMode) {
			Message msg = Message.obtain(mHandler, 666);
			msg.obj = mScreenSeekBar;
			mHandler.sendMessageDelayed(msg, 1500);
		}
	}

	public static void HideSeekPointsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "updateSeekPoints", int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				showSeekBar((View)param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "addView", View.class, int.class, ViewGroup.LayoutParams.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				showSeekBar((View)param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "removeScreen", int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				showSeekBar((View)param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "removeScreensInLayout", int.class, int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				showSeekBar((View)param.thisObject);
			}
		});
	}

	public static void InfiniteScrollHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "getSnapToScreenIndex", int.class, int.class, int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if (param.args[0] != param.getResult()) return;
				int screenCount = (int)XposedHelpers.callMethod(param.thisObject, "getScreenCount");
				if ((int)param.args[2] == -1 && (int)param.args[0] == 0)
					param.setResult(screenCount);
				else if ((int)param.args[2] == 1 && (int)param.args[0] == screenCount - 1)
					param.setResult(0);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "getSnapUnitIndex", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				int mCurrentScreenIndex = XposedHelpers.getIntField(param.thisObject, lpparam.packageName.equals("com.miui.home") ? "mCurrentScreenIndex" : "mCurrentScreen");
				if (mCurrentScreenIndex != (int)param.getResult()) return;
				int screenCount = (int)XposedHelpers.callMethod(param.thisObject, "getScreenCount");
				if ((int)param.getResult() == 0)
					param.setResult(screenCount);
				else if ((int)param.getResult() == screenCount - 1)
					param.setResult(0);
			}
		});
	}

	public static void UnlockGridsRes() {
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x", 3);
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y", 4);
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x_min", 3);
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y_min", 4);
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x_max", 8);
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y_max", 8);
	}

	public static void FolderColumnsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				int cols = MainModule.mPrefs.getInt("launcher_folder_cols", 1);

				GridView mContent = (GridView)XposedHelpers.getObjectField(param.thisObject, "mContent");
				mContent.setNumColumns(cols);

				if (MainModule.mPrefs.getBoolean("launcher_folderwidth")) {
					ViewGroup.LayoutParams lp = mContent.getLayoutParams();
					lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
					mContent.setLayoutParams(lp);
				}

				if (cols > 3 && MainModule.mPrefs.getBoolean("launcher_folderspace")) {
					ViewGroup mBackgroundView = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mBackgroundView");
					if (mBackgroundView != null)
					mBackgroundView.setPadding(
						mBackgroundView.getPaddingLeft() / 3,
						mBackgroundView.getPaddingTop(),
						mBackgroundView.getPaddingRight() / 3,
						mBackgroundView.getPaddingBottom()
					);
				}
			}
		});
	}

	public static void IconScaleHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.ShortcutIcon", lpparam.classLoader, "restoreToInitState", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mIconContainer");
				if (mIconContainer == null || mIconContainer.getChildAt(0) == null) return;
				float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
				mIconContainer.getChildAt(0).setScaleX(multx);
				mIconContainer.getChildAt(0).setScaleY(multx);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);

				ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mIconContainer");
				if (mIconContainer != null && mIconContainer.getChildAt(0) != null) {
					mIconContainer.getChildAt(0).setScaleX(multx);
					mIconContainer.getChildAt(0).setScaleY(multx);
					mIconContainer.setClipToPadding(false);
					mIconContainer.setClipChildren(false);
				}

				if (multx > 1) {
					final TextView mMessage = (TextView)XposedHelpers.getObjectField(param.thisObject, "mMessage");
					if (mMessage != null)
					mMessage.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {}

						@Override
						public void afterTextChanged(Editable s) {
							int maxWidth = mMessage.getResources().getDimensionPixelSize(mMessage.getResources().getIdentifier("icon_message_max_width", "dimen", lpparam.packageName));
							mMessage.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST));
							mMessage.setTranslationX(-mMessage.getMeasuredWidth() * (multx - 1) / 2f);
							mMessage.setTranslationY(mMessage.getMeasuredHeight() * (multx - 1) / 2f);
						}
					});
				}

				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mMessageAnimationOrig", XposedHelpers.getObjectField(param.thisObject, "mMessageAnimation"));
				XposedHelpers.setObjectField(param.thisObject, "mMessageAnimation", new Runnable() {
					@Override
					public void run() {
						try {
							Runnable mMessageAnimationOrig = (Runnable)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mMessageAnimationOrig");
							mMessageAnimationOrig.run();
							boolean mIsShowMessageAnimation = XposedHelpers.getBooleanField(param.thisObject, "mIsShowMessageAnimation");
							if (mIsShowMessageAnimation) {
								View mMessage = (View)XposedHelpers.getObjectField(param.thisObject, "mMessage");
								mMessage.animate().cancel();
								mMessage.animate().scaleX(multx).scaleY(multx).setStartDelay(0).start();
							}
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				});

//				if (mult <= 1) return;
//				TextView mMessage = (TextView)XposedHelpers.getObjectField(param.thisObject, "mMessage");
//				if (mMessage != null) {
//					int width = mMessage.getResources().getDimensionPixelSize(mMessage.getResources().getIdentifier("icon_message_max_width", "dimen", lpparam.packageName));
//					mMessage.setTranslationX(-width/2f * (1f - 1f / mult));
//					mMessage.setTranslationY(width/2f * (1f - 1f / mult));
//				}
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "getIconLocation", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
				Rect rect = (Rect)param.getResult();
				if (rect == null) return;
				rect.right = rect.left + Math.round(rect.width() * multx);
				rect.bottom = rect.top + Math.round(rect.height() * multx);
				param.setResult(rect);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.gadget.ClearButton", lpparam.classLoader, "onCreate", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mIconContainer");
				if (mIconContainer == null || mIconContainer.getChildAt(0) == null) return;
				float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
				mIconContainer.getChildAt(0).setScaleX(multx);
				mIconContainer.getChildAt(0).setScaleY(multx);
			}
		});

//		Helpers.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.classLoader, "onOpen", boolean.class, new MethodHook() {
//			@Override
//			protected void after(final MethodHookParam param) throws Throwable {
//				XposedHelpers.setFloatField(param.thisObject, "mItemIconToPreviewIconScale", -1.0f);
//			}
//		});
//
//		Helpers.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.classLoader, "changeItemsInFolderDuringOpenAndCloseAnimation", float.class, new MethodHook() {
//			@Override
//			protected void after(final MethodHookParam param) throws Throwable {
//				float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
//				ViewGroup mContent = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mContent");
//				for (int i = 0; i < mContent.getChildCount(); i++) {
//					String cls = mContent.getChildAt(i).getClass().getSimpleName();
//					if ("ItemIcon".equals(cls) || "ShortcutIcon".equals(cls) || "FolderIcon".equals(cls)) {
//						View iconContainer = (View)XposedHelpers.callMethod(mContent.getChildAt(i), "getIconContainer");
//						float mult = (float)param.args[0] * multx;
//						iconContainer.setScaleX(mult);
//						iconContainer.setScaleY(mult);
//					}
//				}
//			}
//		});
	}

	public static void TitleFontSizeHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				TextView mTitle = (TextView)XposedHelpers.getObjectField(param.thisObject, "mTitle");
				if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
			}
		});

		if (lpparam.packageName.equals("com.mi.android.globallauncher"))
		Helpers.hookAllMethods("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "setTitleColorMode", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				TextView mTitle = (TextView)XposedHelpers.getObjectField(param.thisObject, "mTitle");
				if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
			}
		});

		Helpers.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.classLoader, "fromXml", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Object buddyIcon = XposedHelpers.callMethod(param.args[3], "getBuddyIconView", param.args[2]);
				if (buddyIcon == null) return;
				TextView mTitle = (TextView)XposedHelpers.getObjectField(buddyIcon, "mTitle");
				if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
			}
		});

		if (lpparam.packageName.equals("com.miui.home")) {
			Helpers.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.classLoader, "createShortcutIcon", new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					Object buddyIcon = param.getResult();
					if (buddyIcon == null) return;
					TextView mTitle = (TextView)XposedHelpers.getObjectField(buddyIcon, "mTitle");
					if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
				}
			});

			Helpers.hookAllMethods("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "adaptTitleStyleToWallpaper", new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					TextView mTitle = (TextView)param.args[1];
					if (mTitle != null && mTitle.getId() == mTitle.getResources().getIdentifier("icon_title", "id", "com.miui.home"))
					mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
				}
			});
		}
	}

	public static void TitleTopMarginHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				ViewGroup mTitleContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mTitleContainer");
				if (mTitleContainer == null) return;
				ViewGroup.LayoutParams lp = mTitleContainer.getLayoutParams();
				int opt = Math.round(MainModule.mPrefs.getInt("launcher_titletopmargin", 0) * mTitleContainer.getResources().getDisplayMetrics().density);
				if (lp instanceof RelativeLayout.LayoutParams) {
					((RelativeLayout.LayoutParams)lp).topMargin = opt;
					mTitleContainer.setLayoutParams(lp);
				} else {
					mTitleContainer.setTranslationY(opt);
					mTitleContainer.setClipChildren(false);
					mTitleContainer.setClipToPadding(false);
					((ViewGroup)mTitleContainer.getParent()).setClipChildren(false);
					((ViewGroup)mTitleContainer.getParent()).setClipToPadding(false);
				}
			}
		});
	}

	public static void PrivacyFolderHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "startSecurityHide", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				boolean opt = Helpers.getSharedBoolPref((Activity)param.thisObject, "pref_key_launcher_privacyapps_gest", false);
				if (opt) param.setResult(null);
			}
		});
	}

	public static void HideTitlesHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				View mTitleContainer = (View)XposedHelpers.getObjectField(param.thisObject, "mTitleContainer");
				if (mTitleContainer != null) mTitleContainer.setVisibility(View.GONE);
			}
		});
	}

	public static void HorizontalSpacingRes() {
		int opt = MainModule.mPrefs.getInt("launcher_horizmargin", 0) - 21;
		MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "workspace_cell_padding_side", opt);
		MainModule.resHooks.setDensityReplacement("com.mi.android.globallauncher", "dimen", "workspace_cell_padding_side", opt);
	}

	public static void TopSpacingRes() {
		int opt = MainModule.mPrefs.getInt("launcher_topmargin", 0) - 21;
		MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "workspace_cell_padding_top", opt);
		MainModule.resHooks.setDensityReplacement("com.mi.android.globallauncher", "dimen", "workspace_cell_padding_top", opt);
	}

	public static void IndicatorHeightRes() {
		int opt = MainModule.mPrefs.getInt("launcher_indicatorheight", 9);
		MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "slide_bar_height", opt);
		MainModule.resHooks.setDensityReplacement("com.mi.android.globallauncher", "dimen", "slide_bar_height", opt);
	}

	public static void GoogleDiscoverHook(final LoadPackageParam lpparam) {
		XposedHelpers.setStaticBooleanField(XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.classLoader), "IS_USE_GOOGLE_MINUS_SCREEN", true);
	}

	public static void ShowHotseatTitlesRes() {
		MainModule.resHooks.setObjectReplacement("com.miui.home", "bool", "config_hide_hotseats_app_title", false);
		MainModule.resHooks.setObjectReplacement("com.mi.android.globallauncher", "bool", "config_hide_hotseats_app_title", false);
	}

	public static void ShowHotseatTitlesHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "calcHotSeatsHeight", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context context = (Context)param.args[0];
				if (context == null) return;
				int height = (int)param.getResult();
				boolean sIsImmersiveNavigationBar = XposedHelpers.getStaticBooleanField(XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.classLoader), "sIsImmersiveNavigationBar");
				if (sIsImmersiveNavigationBar) param.setResult(Math.round(height + 8 * context.getResources().getDisplayMetrics().density));
			}
		});
	}

	public static void FolderBlurHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				final Activity act = (Activity)param.thisObject;
				Handler mHandler = (Handler)XposedHelpers.getObjectField(act, "mHandler");
				new Helpers.SharedPrefObserver(act, mHandler) {
					@Override
					public void onChange(Uri uri) {
						try {
							String type = uri.getPathSegments().get(1);
							String key = uri.getPathSegments().get(2);
							if (!key.contains("pref_key_launcher_folder")) return;

							switch (type) {
								case "string":
									MainModule.mPrefs.put(key, Helpers.getSharedStringPref(act, key, ""));
									break;
								case "integer":
									MainModule.mPrefs.put(key, Helpers.getSharedIntPref(act, key, 1));
									break;
								case "boolean":
									MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(act, key, false));
									break;
							}
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.blur.BlockingBlurController", lpparam.classLoader, "setBlurEnabled", boolean.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getBoolean("launcher_folderblur_disable")) param.args[0] = false;
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.view.BlurFrameLayout", lpparam.classLoader, "setBlurAlpha", float.class, new MethodHook(10) {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				float ratio = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0) / 100f;
				if (ratio > 0) param.args[0] = ratio + (float)param.args[0] * (1.0f - ratio);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.view.BlurFrameLayout", lpparam.classLoader, "setBlurRadius", float.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				float ratio = 5 * MainModule.mPrefs.getInt("launcher_folderblur_radius", 0) / 100f;
				if (ratio > 0) param.args[0] = ratio;
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "fastBlur", float.class, Window.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getBoolean("launcher_folderwallblur_disable")) {
					param.args[0] = 0.0f;
					return;
				}
				float ratio = MainModule.mPrefs.getInt("launcher_folderwallblur_radius", 0) / 100f;
				if (ratio > 0) param.args[0] = (float)param.args[0] * ratio;
			}
		});
	}

//	public static void NoInternationalBuildHook(LoadPackageParam lpparam) {
//		XposedHelpers.setStaticBooleanField(XposedHelpers.findClass("miui.os.Build", null), "IS_INTERNATIONAL_BUILD", false);
//	}

//	public static void ReplaceClockAppHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "startActivity", Context.class, String.class, View.class, new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
//				if (param.args[1] == null || !((String)param.args[1]).contains("DeskClockTabActivity")) return;
//
//				Context mContext = (Context)param.args[0];
//				String pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_clock_app", "");
//				if (pkgAppName == null || pkgAppName.equals("")) return;
//
//				String[] pkgAppArray = pkgAppName.split("\\|");
//				if (pkgAppArray.length < 2) return;
//
//				ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
//				Intent intent = new Intent(Intent.ACTION_MAIN);
//				intent.addCategory(Intent.CATEGORY_LAUNCHER);
//				intent.setComponent(name);
//
//				param.args[1] = intent.toUri(0);
//			}
//		});
//
//		Helpers.findAndHookMethod("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "getDeskClockTabActivityIntent", new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
//			}
//		});
//	}

//	public static void ReplaceCalendarAppHook(LoadPackageParam lpparam) {
//		Class<?> seCls = findClassIfExists("miui.maml.elements.ScreenElement", lpparam.classLoader);
//		if (seCls == null) {
//			Helpers.log("ReplaceCalendarAppHook", "Cannot find ScreenElement class");
//			return;
//		}
//		Helpers.findAndHookMethod("miui.maml.ActionCommand", lpparam.classLoader, "create", Element.class, seCls, new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
//				Element el = (Element)param.args[0];
//				if (el == null || !el.getNodeName().equals("IntentCommand")) return;
//				if (el.getAttribute("package").equals("com.android.calendar") && el.getAttribute("class").contains("AllInOneActivity")) try {
//					Context mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.callMethod(param.args[1], "getContext"), "mContext");
//					String pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_calendar_app", "");
//					if (pkgAppName == null || pkgAppName.equals("")) return;
//
//					String[] pkgAppArray = pkgAppName.split("\\|");
//					if (pkgAppArray.length < 2) return;
//
//					el.setAttribute("package", pkgAppArray[0]);
//					el.setAttribute("class", pkgAppArray[1]);
//				} catch (Throwable t) {
//					XposedBridge.log(t);
//				}
//			}
//		});
//	}
}
