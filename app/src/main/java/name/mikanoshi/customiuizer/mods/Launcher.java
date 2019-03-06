package name.mikanoshi.customiuizer.mods;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.utils.GlobalActions;
import name.mikanoshi.customiuizer.utils.ShakeManager;

public class Launcher {

	private static GestureDetector mDetector;
	private static GestureDetector mDetectorHorizontal;

	public static boolean handleAction(int action, int extraLaunch, int extraToggle, Context helperContext) {
		switch (action) {
			case 2: return GlobalActions.expandNotifications(helperContext);
			case 3: return GlobalActions.expandEQS(helperContext);
			case 4: return GlobalActions.lockDevice(helperContext);
			case 5: return GlobalActions.goToSleep(helperContext);
			case 6: return GlobalActions.takeScreenshot(helperContext);
			case 7: return GlobalActions.openRecents(helperContext);
			case 8: return GlobalActions.launchApp(helperContext, extraLaunch);
			case 9: return GlobalActions.launchShortcut(helperContext, extraLaunch);
			case 10: return GlobalActions.toggleThis(helperContext, extraToggle);
//			case 10: GlobalActions.toggleThis(helperContext, MainModule.pref.getInt("pref_key_launcher_shake_toggle", 0)); return;
			default: return false;
		}
	}

	public static void HomescreenSwipesHook(final XC_LoadPackage.LoadPackageParam lpparam) {
		// Detect vertical swipes
		XposedHelpers.findAndHookMethod("com.miui.home.launcher.ForceTouchLayer", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Context helperContext = ((ViewGroup)param.thisObject).getContext();

				if (helperContext == null) return;
				if (mDetector == null) mDetector = new GestureDetector(helperContext, new SwipeListener(helperContext));

				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev == null) return;
				mDetector.onTouchEvent(ev);
			}
		});

		if (MainModule.pref_swipedown != 1)
		XposedHelpers.findAndHookMethod("com.miui.launcher.utils.LauncherUtils", lpparam.classLoader, "expandStatusBar", Context.class, XC_MethodReplacement.DO_NOTHING);

		if (MainModule.pref_swipeup != 1)
		XposedHelpers.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});

//		XposedHelpers.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onVerticalGesture", int.class, MotionEvent.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//				Log.e("onVerticalGesture", String.valueOf(param.args[0]));
//			}
//		});
	}

	// Listener for vertical swipe gestures
	private static class SwipeListener extends GestureDetector.SimpleOnGestureListener {

		private int SWIPE_MIN_DISTANCE = 300;
		private int SWIPE_MAX_OFF_PATH = 250;
		private int SWIPE_THRESHOLD_VELOCITY = 200;

		final Context helperContext;

		SwipeListener(Context context) {
			helperContext = context;
			float density = helperContext.getResources().getDisplayMetrics().density;
			SWIPE_MIN_DISTANCE = Math.round(100 * density);
			SWIPE_MAX_OFF_PATH = Math.round(85 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(65 * density);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;
			if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) return false;

			if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				//MainModule.pref.reload();
				XposedBridge.log("Swiped DOWN! " + String.valueOf(MainModule.pref.getInt("pref_key_launcher_swipedown_action", 1)));
				return handleAction(MainModule.pref.getInt("pref_key_launcher_swipedown_action", 1), 1, MainModule.pref.getInt("pref_key_launcher_swipedown_toggle", 0), helperContext);
			}

			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				//MainModule.pref.reload();
				XposedBridge.log("Swiped UP! " + String.valueOf(MainModule.pref.getInt("pref_key_launcher_swipeup_action", 1)));
				return handleAction(MainModule.pref.getInt("pref_key_launcher_swipeup_action", 1), 2, MainModule.pref.getInt("pref_key_launcher_swipeup_toggle", 0), helperContext);
			}

			return false;
		}
	}

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

		private int SWIPE_MIN_DISTANCE_HORIZ = 230;
		private int SWIPE_THRESHOLD_VELOCITY = 100;

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
				//MainModule.pref.reload();
				XposedBridge.log("Swiped RIGHT! " + String.valueOf(MainModule.pref.getInt("pref_key_launcher_swiperight_action", 1)));
				return handleAction(MainModule.pref.getInt("pref_key_launcher_swiperight_action", 1), 3, MainModule.pref.getInt("pref_key_launcher_swiperight_toggle", 0), helperContext);
			}
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				//MainModule.pref.reload();
				XposedBridge.log("Swiped LEFT! " + String.valueOf(MainModule.pref.getInt("pref_key_launcher_swipeleft_action", 1)));
				return handleAction(MainModule.pref.getInt("pref_key_launcher_swipeleft_action", 1), 4, MainModule.pref.getInt("pref_key_launcher_swipeleft_toggle", 0), helperContext);
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

}
