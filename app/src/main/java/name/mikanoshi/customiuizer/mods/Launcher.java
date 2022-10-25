package name.mikanoshi.customiuizer.mods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
import android.os.UserHandle;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
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

	@SuppressWarnings("ResultOfMethodCallIgnored")
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
							if (key.contains("pref_key_launcher_swipedown"))
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

		Helpers.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.StatusBarSwipeController", lpparam.classLoader, "canInterceptTouch", MotionEvent.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) param.setResult(false);
			}
		});

		// content_center, global_search, notification_bar
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.classLoader, "getPullDownGesture", Context.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipedown_action", 1) > 1) param.setResult("no_action");
			}
		});

		// content_center, global_search
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.classLoader, "getSlideUpGesture", Context.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipeup_action", 1) > 1) param.setResult("no_action");
			}
		});

		if (Helpers.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isGlobalSearchEnable", Context.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipeup_action", 1) > 1) param.setResult(false);
			}
		})) {
			Helpers.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.classLoader, "isTopSearchEnable", new MethodHook() {
				@Override
				protected void before(final MethodHookParam param) throws Throwable {
					View view = (View)param.thisObject;
					if (Helpers.getSharedIntPref(view.getContext(), "pref_key_launcher_swipedown_action", 1) > 1) param.setResult(false);
				}
			});
			Helpers.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.classLoader, "isBottomGlobalSearchEnable", new MethodHook() {
				@Override
				protected void before(final MethodHookParam param) throws Throwable {
					View view = (View)param.thisObject;
					if (Helpers.getSharedIntPref(view.getContext(), "pref_key_launcher_swipeup_action", 1) > 1) param.setResult(false);
				}
			});
			Helpers.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isGlobalSearchBottomEffectEnable", Context.class, new MethodHook() {
				@Override
				protected void before(final MethodHookParam param) throws Throwable {
					if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipeup_action", 1) > 1) param.setResult(false);
				}
			});
		} else if (!Helpers.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context.class, new MethodHook() {
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
		Helpers.findAndHookMethod("com.miui.home.launcher.hotseats.HotSeats", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
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
	}

	// Listener for horizontal swipes on hotseats
	private static class SwipeListenerHorizontal extends GestureDetector.SimpleOnGestureListener {

		private final int SWIPE_MIN_DISTANCE_HORIZ;
		private final int SWIPE_THRESHOLD_VELOCITY;

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
	public static void FolderShadeHook(final LoadPackageParam lpparam) {
		wallpaperUtilsCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader);

		MethodHook hook = new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				View folder = (View)param.thisObject;
				new Thread() {
					@Override
					public void run() {
						try {
							Context context = folder.getContext();
							int opt = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_launcher_foldershade", "1"));
							int level = Helpers.getSharedIntPref(context, "pref_key_launcher_foldershade_level", 40);
							final Drawable bkg;
							if (opt == 2) {
								boolean isLight = false;
								if (wallpaperUtilsCls != null) try { isLight = (boolean)XposedHelpers.callStaticMethod(wallpaperUtilsCls, "hasAppliedLightWallpaper"); } catch (Throwable ignore) {}
								int bgcolor = (isLight ? 0x00ffffff : 0x00000000) | (Math.round(255 * level / 100f) * 0x1000000);
								bkg = new ColorDrawable(bgcolor);
							} else if (opt == 3) {
								PaintDrawable pd = new PaintDrawable();
								pd.setShape(new RectShape());
								pd.setShaderFactory(new ShapeDrawable.ShaderFactory() {
									@Override
									public Shader resize(int width, int height) {
										boolean isLight = false;
										if (wallpaperUtilsCls != null) try { isLight = (boolean)XposedHelpers.callStaticMethod(wallpaperUtilsCls, "hasAppliedLightWallpaper"); } catch (Throwable ignore) {}
										int bgcolor1 = (isLight ? 0x00ffffff : 0x00000000) | (Math.round(255 / 6f * level / 100f) * 0x1000000);
										int bgcolor2 = (isLight ? 0x00ffffff : 0x00000000) | (Math.round(255 * level / 100f) * 0x1000000);
										return new LinearGradient(0, 0, 0, height,
											new int[]{ bgcolor1, bgcolor2, bgcolor2, bgcolor1 },
											new float[]{ 0.0f, 0.25f, 0.65f, 1.0f },
											Shader.TileMode.CLAMP
										);
									}
								});
								bkg = pd;
							} else bkg = null;
							new Handler(context.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									MainModule.mPrefs.put("pref_key_launcher_foldershade", String.valueOf(opt));
									MainModule.mPrefs.put("pref_key_launcher_foldershade_level", level);
									folder.setBackground(bkg);
								}
							});
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				}.start();
			}
		};
		Helpers.hookAllConstructors("com.miui.home.launcher.FolderCling", lpparam.classLoader, hook);
		Helpers.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.classLoader, "onWallpaperColorChanged", hook);
		Helpers.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.classLoader, "updateLayout", boolean.class, hook);

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
		UserHandle user = (UserHandle)XposedHelpers.getObjectField(thisObject, "user");
		String newTitle = MainModule.mPrefs.getString("launcher_renameapps_list:" + pkgName + "|" + actName + "|" + user.hashCode(), "");
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
								UserHandle user = (UserHandle)XposedHelpers.getObjectField(shortcut, "user");
								if (("pref_key_launcher_renameapps_list:" + pkgName + "|" + actName + "|" + user.hashCode()).equals(key)) {
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
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "loadToggleInfo", Context.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLabelOrig", XposedHelpers.getObjectField(param.thisObject, "mLabel"));
				modifyTitle(param.thisObject);
			}
		});

		Helpers.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "setLabelAndUpdateDB", CharSequence.class, Context.class, new MethodHook() {
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

		Helpers.hookAllMethodsSilently("com.miui.home.launcher.BaseAppInfo", lpparam.classLoader, "resetTitle", new MethodHook() {
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
				if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) != 2) return;
				boolean mHasLaunchedAppFromFolder = XposedHelpers.getBooleanField(param.thisObject, "mHasLaunchedAppFromFolder");
				if (mHasLaunchedAppFromFolder) XposedHelpers.callMethod(param.thisObject, "closeFolder");
			}
		});

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.common.CloseFolderStateMachine", lpparam.classLoader, "onPause", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) == 3) param.setResult(null);
			}
		});
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void FSGesturesHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "usingFsGesture", XC_MethodReplacement.returnConstant(true));

		Helpers.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader, "createAndAddNavStubView", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader), "REAL_FORCE_FSG_NAV_BAR");
				if (!fsg) param.setResult(null);
			}
		});

		Helpers.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader, "updateFsgWindowState", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader), "REAL_FORCE_FSG_NAV_BAR");
				if (fsg) return;

				Object mNavStubView = XposedHelpers.getObjectField(param.thisObject, "mNavStubView");
				Object mWindowManager = XposedHelpers.getObjectField(param.thisObject, "mWindowManager");
				if (mWindowManager != null && mNavStubView != null) {
					XposedHelpers.callMethod(mWindowManager, "removeView", mNavStubView);
					XposedHelpers.setObjectField(param.thisObject, "mNavStubView", null);
				}
			}
		});

		Helpers.findAndHookMethodSilently("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.classLoader, "getGlobalBoolean", ContentResolver.class, String.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (!"force_fsg_nav_bar".equals(param.args[1])) return;

				for (StackTraceElement el: Thread.currentThread().getStackTrace())
				if ("com.miui.home.recents.BaseRecentsImpl".equals(el.getClassName())) {
					XposedHelpers.setAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader), "REAL_FORCE_FSG_NAV_BAR", param.getResult());
					param.setResult(true);
					return;
				}
			}
		});

		Helpers.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				MotionEvent event = (MotionEvent)param.args[0];
				if (event.getAction() != MotionEvent.ACTION_DOWN) return;
				View stub = (View)param.thisObject;
				String pkgName = Settings.Global.getString(stub.getContext().getContentResolver(), Helpers.modulePkg + ".foreground.package");
				if (MainModule.mPrefs.getStringSet("controls_fsg_horiz_apps").contains(pkgName)) param.setResult(false);
			}
		});
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

	@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
	public static class DoubleTapController {
		private final long MAX_DURATION = 500;
		private float mActionDownRawX;
		private float mActionDownRawY;
		private int mClickCount;
		public final Context mContext;
		private final String mActionKey;
		private float mFirstClickRawX;
		private float mFirstClickRawY;
		private long mLastClickTime;
		private int mTouchSlop;

		DoubleTapController(Context context, String actionKey) {
			this.mContext = context;
			this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
			this.mActionKey = actionKey;
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
			GlobalActions.handleAction(mContext, mActionKey);
		}
	}

	public static void LauncherDoubleTapHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.miui.home.launcher.Workspace", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Object mDoubleTapControllerEx = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mDoubleTapControllerEx");
				if (mDoubleTapControllerEx != null) return;
				mDoubleTapControllerEx = new DoubleTapController((Context)param.args[0], "pref_key_launcher_doubletap");
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
					if (seekBar != null) {
						seekBar.animate().alpha(0.0f).setDuration(600).withEndAction(new Runnable() {
							@Override
							public void run() {
								seekBar.setVisibility(View.GONE);
							}
						});
					}
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
		if (!isInEditingMode && MainModule.mPrefs.getBoolean("launcher_hideseekpoints_edit")) {
			mScreenSeekBar.setAlpha(0.0f);
			mScreenSeekBar.setVisibility(View.GONE);
			return;
		}
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
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x_max", 10);
		MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y_max", 10);
	}

	public static void UnlockGridsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethodsSilently("com.miui.home.launcher.compat.LauncherCellCountCompatDevice", lpparam.classLoader, "shouldUseDeviceValue", XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("com.miui.home.settings.MiuiHomeSettings", lpparam.classLoader, "onCreatePreferences", Bundle.class, String.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mScreenCellsConfig"), "setVisible", true);
			}
		});
		Class <?> DeviceConfigClass = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.classLoader);
		Helpers.findAndHookMethod(DeviceConfigClass, "loadCellsCountConfig", Context.class, boolean.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				int sCellCountY = (int) XposedHelpers.getStaticObjectField(DeviceConfigClass, "sCellCountY");
				if (sCellCountY > 6) {
					int cellHeight = (int) XposedHelpers.callStaticMethod(DeviceConfigClass, "getCellHeight");
					XposedHelpers.setStaticObjectField(DeviceConfigClass, "sFolderCellHeight", cellHeight);
				}
			}
		});
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

		Helpers.hookAllMethods("com.miui.home.launcher.Folder", lpparam.classLoader, "onLayout", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if (!MainModule.mPrefs.getBoolean("launcher_folderwidth")) return;
				GridView mContent = (GridView)XposedHelpers.getObjectField(param.thisObject, "mContent");
				ImageView mFakeIcon = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mFakeIcon");
				mFakeIcon.layout(mContent.getLeft(), mContent.getTop(), mContent.getRight(), mContent.getTop() + mContent.getWidth());
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

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.gadget.ClearButton", lpparam.classLoader, "onCreate", new MethodHook() {
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
				int opt = Math.round((MainModule.mPrefs.getInt("launcher_titletopmargin", 0) - 11) * mTitleContainer.getResources().getDisplayMetrics().density);
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
				if (GlobalActions.handleAction((Activity)param.thisObject, "pref_key_launcher_spread")) {
					param.setResult(null);
					return;
				}
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
		try {
			XposedHelpers.setStaticBooleanField(XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.classLoader), "IS_USE_GOOGLE_MINUS_SCREEN", true);
		} catch (Throwable ignore) {}
		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isUseGoogleMinusScreen", XC_MethodReplacement.returnConstant(true));
	}

	@SuppressWarnings("unchecked")
	public static void GoogleMinusScreenHook(final LoadPackageParam lpparam) {
		try {
			Class<?> configCls = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.classLoader);
			if (configCls == null) {
				Helpers.log("GoogleMinusScreenHook", "Cannot find config class");
				return;
			}

			HashSet<String> SELECT_MINUS_SCREEN_CLIENT_ID = (HashSet<String>)XposedHelpers.getStaticObjectField(configCls, "SELECT_MINUS_SCREEN_CLIENT_ID");
			SELECT_MINUS_SCREEN_CLIENT_ID.add("");

			//HashSet<String> USE_GOOGLE_MINUS_SCREEN_REGIONS = (HashSet<String>)XposedHelpers.getStaticObjectField(configCls, "USE_GOOGLE_MINUS_SCREEN_REGIONS");
			//String CURRENT_REGION = (String)XposedHelpers.getStaticObjectField(configCls, "CURRENT_REGION");
			//USE_GOOGLE_MINUS_SCREEN_REGIONS.add(CURRENT_REGION);

			XposedHelpers.setStaticBooleanField(configCls, "CAN_SWITCH_MINUS_SCREEN", true);
			XposedHelpers.setStaticBooleanField(configCls, "ONLY_USE_GOOGLE_MINUS_SCREEN", false);
			Class<?> appCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.Application", lpparam.classLoader);
			if (appCls != null) try {
				Application app = (Application)XposedHelpers.callStaticMethod(appCls, "getInstance");
				XposedHelpers.setStaticBooleanField(configCls, "IS_USE_GOOGLE_MINUS_SCREEN", "personal_assistant_google".equals(Settings.System.getString(app.getContentResolver(), "switch_personal_assistant")));
			} catch (Throwable ignore) {}
		} catch (Throwable t) {
			Helpers.log("GoogleMinusScreenHook", t);
		}
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
							if (key.contains("pref_key_launcher_folder"))
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

		Class<?> buCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.classLoader);
		if (buCls != null) {
			Helpers.findAndHookMethod("com.miui.home.launcher.common.BlurUtils", lpparam.classLoader, "isUserBlurWhenOpenFolder", new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});

			Method[] methods = buCls.getDeclaredMethods();
			Method fastBlur = null;
    		for (Method method: methods)
    		if ("fastBlur".equals(method.getName())) {
    			fastBlur = method;
    			if (method.getParameterTypes().length == 4) break;
    		}

			if (fastBlur == null) Helpers.log("FolderBlurHook", "Cannot find fastBlur util method!"); else
			Helpers.hookMethod(fastBlur, new MethodHook() {
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

			return;
		}

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

	public static void StatusBarHeightHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.miui.home.launcher.Workspace", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				ViewGroup workspace = (ViewGroup)param.thisObject;
				workspace.setPadding(
					workspace.getPaddingLeft(),
					workspace.getResources().getDimensionPixelSize(workspace.getResources().getIdentifier("status_bar_height", "dimen", "android")),
					workspace.getPaddingRight(),
					workspace.getPaddingBottom()
				);
			}
		});
	}

	private static float scaleStiffness(float val, float scale) {
		return (scale < 1.0f ? 2f / scale : 1.0f / scale) * val;
	}

	public static void FixAnimHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.home.launcher.animate.SpringAnimator", lpparam.classLoader, "getSpringForce", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				float scale = Helpers.getAnimationScale(2);
				if (scale == 1.0f) return;
				if (scale == 0) scale = 0.01f;
				param.args[2] = scaleStiffness((float)param.args[2], scale);
			}
		});

		MethodHook hook = new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				float scale = Helpers.getAnimationScale(2);
				if (scale == 1.0f) return;
				if (scale == 0) scale = 0.01f;
				XposedHelpers.setFloatField(param.thisObject, "mCenterXStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mCenterXStiffness"), scale));
				XposedHelpers.setFloatField(param.thisObject, "mCenterYStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mCenterYStiffness"), scale));
				XposedHelpers.setFloatField(param.thisObject, "mWidthStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mWidthStiffness"), scale));
				XposedHelpers.setFloatField(param.thisObject, "mRadiusStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mRadiusStiffness"), scale));
				XposedHelpers.setFloatField(param.thisObject, "mAlphaStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mAlphaStiffness"), scale));
				try {
					XposedHelpers.setFloatField(param.thisObject, "mRatioStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mRatioStiffness"), scale));
				} catch (Throwable t) {
					XposedHelpers.setFloatField(param.thisObject, "mRadioStiffness", scaleStiffness(XposedHelpers.getFloatField(param.thisObject, "mRadioStiffness"), scale));
				}
			}
		};

		if (!Helpers.hookAllMethodsSilently("com.miui.home.recents.util.RectFSpringAnim", lpparam.classLoader, "start", hook))
		Helpers.hookAllMethods("com.miui.home.recents.util.RectFSpringAnim", lpparam.classLoader, "initAllAnimations", hook);

//		if (XposedHelpers.findClassIfExists("com.android.systemui.shared.recents.system.RemoteAnimationAdapterCompat", lpparam.classLoader) != null)
//		Helpers.hookAllConstructors("com.android.systemui.shared.recents.system.RemoteAnimationAdapterCompat", lpparam.classLoader, new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
//				float scale = Helpers.getAnimationScale(2);
//				if (scale == 1.0f) return;
//				param.args[1] = (long)((long)param.args[1] * scale);
//				param.args[2] = (long)((long)param.args[2] * scale);
//			}
//		});
	}

	public static void BottomSpacingRes() {
		int opt = MainModule.mPrefs.getInt("launcher_bottommargin", 0);
		MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "hotseats_padding_bottom", opt);
	}

	public static void BottomSpacingHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "calcHotSeatsHeight", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context context = (Context)param.args[0];
				if (context == null) return;
				int height = (int)param.getResult();
				int opt = MainModule.mPrefs.getInt("launcher_bottommargin", 0);
				param.setResult(Math.round(height + opt * context.getResources().getDisplayMetrics().density));
			}
		});
	}

	public static void HorizontalWidgetSpacingHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "getMiuiWidgetSizeSpec", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (param.args.length < 4) return;
				Context context = Helpers.findContext();
				long spec = (long)param.getResult();
				long width = spec >> 32;
				long height = spec - ((spec >> 32) << 32);
				int opt = Math.round((MainModule.mPrefs.getInt("launcher_horizwidgetmargin", 0) - 21) * context.getResources().getDisplayMetrics().density) * 2;
				width -= opt;
				param.setResult((width << 32) | height);
			}
		});

		Helpers.hookAllMethods("com.miui.home.launcher.MIUIWidgetUtil", lpparam.classLoader, "getMiuiWidgetPadding", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				param.setResult(new Rect());
			}
		});
	}

	public static void FixAppInfoLaunchHook(LoadPackageParam lpparam) {
		if (lpparam.packageName.equals("com.mi.android.globallauncher"))
			Helpers.hookAllMethods("com.miui.home.launcher.util.Utilities", lpparam.classLoader, "startDetailsActivityForInfo", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					Object itemInfo = param.args[0];
					ComponentName component;
					try {
						component = (ComponentName)XposedHelpers.callMethod(itemInfo, "getComponentName");
					} catch (Throwable t1) {
						try {
							component = (ComponentName)XposedHelpers.callMethod(XposedHelpers.getObjectField(itemInfo, "intent"), "getComponent");
						} catch (Throwable t2) {
							try {
								component = (ComponentName)XposedHelpers.getObjectField(itemInfo, "providerName");
							} catch (Throwable t3) {
								component = (ComponentName)XposedHelpers.getObjectField(XposedHelpers.getObjectField(itemInfo, "providerInfo"), "provider");
							}
						}
					}
					if (component == null) return;
					Context context = (Context)param.args[1];
					if (context == null) return;
					UserHandle userHandle = (UserHandle)XposedHelpers.callMethod(param.args[0], "getUser");
					Helpers.openAppInfo(context, component.getPackageName(), userHandle != null ? userHandle.hashCode() : 0);
					param.setResult(true);
				}
			});
		else
			Helpers.hookAllMethods("com.miui.home.launcher.shortcuts.ShortcutMenuManager", lpparam.classLoader, "startAppDetailsActivity", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					ComponentName component = (ComponentName)XposedHelpers.callMethod(param.args[0], "getComponentName");
					if (component == null) return;
					View view = (View)param.args[1];
					if (view == null) return;
					UserHandle userHandle = (UserHandle)XposedHelpers.callMethod(param.args[0], "getUserHandle");
					Helpers.openAppInfo(view.getContext(), component.getPackageName(), userHandle != null ? userHandle.hashCode() : 0);
					param.setResult(null);
				}
			});
	}

	public static void NoWidgetOnlyHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.CellLayout", lpparam.classLoader, "setScreenType", int.class, new MethodHook() {
			@Override
			protected void before(XC_MethodHook.MethodHookParam param) throws Throwable {
				param.args[0] = 0;
			}
		});
	}

	public static void NoUnlockAnimationHook(LoadPackageParam lpparam) {
		if (!Helpers.findAndHookMethodSilently("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "notShowUserPresentAnimation", Context.class, XC_MethodReplacement.returnConstant(true)))
		Helpers.hookAllMethods("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.classLoader, "isSystemAnimationOpen", XC_MethodReplacement.returnConstant(false));
	}

	public static void NoZoomAnimationHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.classLoader, "startShortcutMenuLayerFadeOutAnim", XC_MethodReplacement.DO_NOTHING);
		Helpers.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.classLoader, "startShortcutMenuLayerFadeInAnim", XC_MethodReplacement.DO_NOTHING);
	}

	public static void UseOldLaunchAnimationHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.home.recents.QuickstepAppTransitionManagerImpl", lpparam.classLoader, "hasControlRemoteAppTransitionPermission", XC_MethodReplacement.returnConstant(false));
	}

	public static void ReverseLauncherPortraitHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			@SuppressLint("SourceLockedOrientationActivity")
			protected void after(XC_MethodHook.MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			}
		});
	}

	public static void MaxHotseatIconsCountHook(LoadPackageParam lpparam) {
		String methodName = lpparam.packageName.equals("com.mi.android.globallauncher") ? "getHotseatCount" : "getHotseatMaxCount";
		Helpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, methodName, XC_MethodReplacement.returnConstant(666));

//		Helpers.findAndHookMethod("com.miui.home.launcher.RecentsAndFSGestureUtils", lpparam.classLoader, "isUseFsGestureV2Device", new MethodHook() {
//			@Override
//			protected void after(XC_MethodHook.MethodHookParam param) throws Throwable {
//				XposedBridge.log("isUseFsGestureV2Device: " + param.getResult());
//			}
//		});
//
//		Helpers.findAndHookMethod("com.miui.home.launcher.RecentsAndFSGestureUtils", lpparam.classLoader, "isUseGestureVersion3", Context.class, new MethodHook() {
//			@Override
//			protected void after(XC_MethodHook.MethodHookParam param) throws Throwable {
//				XposedBridge.log("isUseGestureVersion3: " + param.getResult());
//			}
//		});
//
//		Helpers.findAndHookMethod("com.miui.home.launcher.RecentsAndFSGestureUtils", lpparam.classLoader, "isSupportRecentsAndFsGesture", new MethodHook() {
//			@Override
//			protected void after(XC_MethodHook.MethodHookParam param) throws Throwable {
//				XposedBridge.log("isSupportRecentsAndFsGesture: " + param.getResult());
//				//param.setResult(false);
//			}
//		});
	}

	public static void RecentsBlurRatioHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.miui.home.recents.views.RecentsView", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				try {
					XposedHelpers.setFloatField(param.thisObject, "mDefaultScrimAlpha", 0.15f);
					XposedHelpers.setObjectField(param.thisObject, "mBackgroundScrim", new ColorDrawable(Color.argb(38, 0, 0, 0)).mutate());
				} catch (Throwable ignore) {}
			}
		});

		Helpers.hookAllConstructors("com.miui.home.recents.RecentsViewStateController", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)param.args[0];
				Handler mHandler = new Handler(mContext.getMainLooper());

				new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_recents_blur", 100) {
					@Override
					public void onChange(String name, int defValue) {
						MainModule.mPrefs.put(name, Helpers.getSharedIntPref(mContext, name, 100));
					}
				};
			}
		});

		Class<?> utilsClass = XposedHelpers.findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.classLoader);
		if (utilsClass == null) utilsClass = XposedHelpers.findClassIfExists("com.miui.home.launcher.common.Utilities", lpparam.classLoader);
		if (utilsClass == null) {
			Helpers.log("RecentsBlurRatioHook", "Cannot find blur utility class");
			return;
		}

		Helpers.hookAllMethods(utilsClass, "fastBlur", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.args[0] = (float)param.args[0] * MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f;
			}
		});
	}

    public static void CloseFolderOrDrawerOnLaunchShortcutMenuHook(LoadPackageParam lpparam) {
    	Helpers.findAndHookMethod("com.miui.home.launcher.shortcuts.AppShortcutMenuItem", lpparam.classLoader, "getOnClickListener", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				final View.OnClickListener listener = (View.OnClickListener)param.getResult();
				param.setResult(new View.OnClickListener() {
					public final void onClick(View view) {
						listener.onClick(view);
						Class<?> appCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.Application", lpparam.classLoader);
						if (appCls == null) return;
						Object launcher = XposedHelpers.callStaticMethod(appCls, "getLauncher");
						if (launcher == null) return;
						if (MainModule.mPrefs.getBoolean("launcher_closedrawer")) XposedHelpers.callMethod(launcher, "hideAppView");
						if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) > 1) XposedHelpers.callMethod(launcher, "closeFolder");
					}
				});
			}
		});
	}

	public static void CloseDrawerOnLaunchHook(LoadPackageParam lpparam) {
		MethodHook hook = new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mLauncher"), "hideAppView");
			}
		};
		Helpers.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.AppsListFragment", lpparam.classLoader, "onClick", View.class, hook);
		Helpers.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.RecommendCategoryAppListFragment", lpparam.classLoader, "onClick", View.class, hook);
	}

	public static void LauncherPinchHook(LoadPackageParam lpparam) {
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
							if (key.contains("pref_key_launcher_pinch"))
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

		Helpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onPinching", float.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				float dampingScale = (float)XposedHelpers.callMethod(param.thisObject, "getDampingScale", param.args[0]);
				float screenScaleRatio = (float)XposedHelpers.callMethod(param.thisObject, "getScreenScaleRatio");
				if (dampingScale < screenScaleRatio)
				if (MainModule.mPrefs.getInt("launcher_pinch_action", 1) > 1) param.setResult(false);
			}
		});

		Helpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onPinchingEnd", float.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				float dampingScale = (float)XposedHelpers.callMethod(param.thisObject, "getDampingScale", param.args[0]);
				float screenScaleRatio = (float)XposedHelpers.callMethod(param.thisObject, "getScreenScaleRatio");
				if (dampingScale < screenScaleRatio)
				if (GlobalActions.handleAction(((View)param.thisObject).getContext(), "pref_key_launcher_pinch")) {
					XposedHelpers.callMethod(param.thisObject, "finishCurrentGesture");

					Class<?> pinchingStateEnum = XposedHelpers.findClass("com.miui.home.launcher.Workspace$PinchingState", lpparam.classLoader);
					Object stateFollow = XposedHelpers.getStaticObjectField(pinchingStateEnum, "FOLLOW");
					Object stateReadyToEdit = XposedHelpers.getStaticObjectField(pinchingStateEnum, "READY_TO_EDIT");

					Object mState = XposedHelpers.getObjectField(param.thisObject, "mState");
					XposedHelpers.setObjectField(param.thisObject, "mState", stateFollow);
					if (mState == stateReadyToEdit)
					XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mLauncher"), "changeEditingEntryViewToHotseats");
					XposedHelpers.callMethod(param.thisObject, "resetCellScreenScale", param.args[0]);

					param.setResult(null);
				}
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