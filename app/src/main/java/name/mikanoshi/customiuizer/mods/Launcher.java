package name.mikanoshi.customiuizer.mods;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.ShakeManager;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class Launcher {

//	private static GestureDetector mDetector;
	private static GestureDetector mDetectorHorizontal;

	public static void HomescreenSwipesHook(final XC_LoadPackage.LoadPackageParam lpparam) {
//		// Detect vertical swipes
//		XposedHelpers.findAndHookMethod("com.miui.home.launcher.ForceTouchLayer", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
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
//		XposedHelpers.findAndHookMethod("com.miui.launcher.utils.LauncherUtils", lpparam.classLoader, "expandStatusBar", Context.class, XC_MethodReplacement.DO_NOTHING);
//
//		if (MainModule.pref_swipeup != 1)
//		XposedHelpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//				param.setResult(false);
//			}
//		});

		XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				XposedHelpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onVerticalGesture", int.class, MotionEvent.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
						int action = 1;
						int launch = 0;
						int toggle = 0;
						Context helperContext = ((ViewGroup)param.thisObject).getContext();
						int numOfFingers = 1;
						if (param.args[1] != null) numOfFingers = ((MotionEvent)param.args[1]).getPointerCount();
						if ((int)param.args[0] == 11) {
							if (numOfFingers == 1) {
								action = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipedown_action", 1);
								launch = 1;
								toggle = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipedown_toggle", 0);
							} else if (numOfFingers == 2) {
								action = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipedown2_action", 1);
								launch = 2;
								toggle = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipedown2_toggle", 0);
							}
							if (action <= 1) return;
							boolean res = GlobalActions.handleAction(action, launch, toggle, helperContext);
							if (res) param.setResult(true);
						} else if ((int)param.args[0] == 10) {
							if (numOfFingers == 1) {
								action = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipeup_action", 1);
								launch = 3;
								toggle = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipeup_toggle", 0);
							} else if (numOfFingers == 2) {
								action = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipeup2_action", 1);
								launch = 4;
								toggle = Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipeup2_toggle", 0);
							}
							if (action <= 1) return;
							boolean res = GlobalActions.handleAction(action, launch, toggle, helperContext);
							if (res) param.setResult(true);
						}
					}
				});
			}
		});

		try {
			XposedHelpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (Helpers.getSharedIntPref((Context)param.args[0], "pref_key_launcher_swipeup_action", 1) > 1) param.setResult(false);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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

	public static void HotSeatSwipesHook(final XC_LoadPackage.LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.miui.home.launcher.HotSeats", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev == null) return;

				ViewGroup hotSeat = (ViewGroup)param.thisObject;
				Context helperContext = hotSeat.getContext();
				if (helperContext == null) return;
				if (mDetectorHorizontal == null) mDetectorHorizontal = new GestureDetector(helperContext, new SwipeListenerHorizontal(hotSeat));
				mDetectorHorizontal.onTouchEvent(ev);
			}
		});

//		XposedHelpers.findAndHookMethod("com.htc.launcher.DragLayer", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
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

			if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				return GlobalActions.handleAction(
					Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swiperight_action", 1), 5,
					Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swiperight_toggle", 0), helperContext
				);
			}
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				return GlobalActions.handleAction(
					Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipeleft_action", 1), 6,
					Helpers.getSharedIntPref(helperContext, "pref_key_launcher_swipeleft_toggle", 0), helperContext
				);
			}

			return false;
		}
	}

	public static void ShakeHook(final XC_LoadPackage.LoadPackageParam lpparam) {
		final String shakeMgrKey = "MIUIZER_SHAKE_MGR";
		XposedHelpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
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

		XposedHelpers.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onPause", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				if (XposedHelpers.getAdditionalInstanceField(param.thisObject, shakeMgrKey) == null) return;
				Activity launcherActivity = (Activity)param.thisObject;
				SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
				sensorMgr.unregisterListener((ShakeManager)XposedHelpers.getAdditionalInstanceField(param.thisObject, shakeMgrKey));
			}
		});
	}

	public static void FolderShadeHook(final XC_LoadPackage.LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.miui.home.launcher.FolderCling", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				int opt = Integer.parseInt(Helpers.getSharedStringPref((Context)param.args[0], "pref_key_launcher_foldershade", "1"));
				if (opt == 2) {
					((View)param.thisObject).setBackground(new ColorDrawable(0x99000000));
				} else if (opt == 3) {
					PaintDrawable pd = new PaintDrawable();
					pd.setShape(new RectShape());
					pd.setShaderFactory(new ShapeDrawable.ShaderFactory() {
						@Override
						public Shader resize(int width, int height) {
							return new LinearGradient(0, 0, 0, height, new int[]{0x11000000, 0x99000000, 0x99000000, 0x11000000}, new float[]{0.0f, 0.25f, 0.65f, 1.0f}, Shader.TileMode.CLAMP);
						}
					});
					((View)param.thisObject).setBackground(pd);
				}
			}
		});
	}

}
