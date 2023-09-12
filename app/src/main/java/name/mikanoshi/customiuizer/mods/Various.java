package name.mikanoshi.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static name.mikanoshi.customiuizer.mods.GlobalActions.ACTION_PREFIX;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

public class Various {

	public static PackageInfo mLastPackageInfo;
	public static Object mSupportFragment = null;
	public static void AppInfoHook(LoadPackageParam lpparam) {
		Class<?> amaCls = XposedHelpers.findClassIfExists("com.miui.appmanager.AMAppInfomationActivity", lpparam.classLoader);
		if (amaCls == null) {
			Helpers.log("AppInfoHook", "Cannot find activity class!");
			return;
		}

		if (findClassIfExists("androidx.fragment.app.Fragment", lpparam.classLoader) != null)
		Helpers.findAndHookConstructor("androidx.fragment.app.Fragment", lpparam.classLoader, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				try {
					Field piField = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), PackageInfo.class);
					if (piField != null) mSupportFragment = param.thisObject;
				} catch (Throwable ignore) {}
			}
		});

		Helpers.findAndHookMethod(amaCls, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						final Activity act = (Activity)param.thisObject;
						Object contentFrag = act.getFragmentManager().findFragmentById(android.R.id.content);
						Object frag = contentFrag != null ? contentFrag : mSupportFragment;
						if (frag == null) {
							Helpers.log("AppInfoHook", "Unable to find fragment");
							return;
						}

						final Resources modRes;
						try {
							modRes = Helpers.getModuleRes(act);
							Field piField = XposedHelpers.findFirstFieldByExactType(frag.getClass(), PackageInfo.class);
							mLastPackageInfo = (PackageInfo)piField.get(frag);
							Method[] addPref = XposedHelpers.findMethodsByExactParameters(frag.getClass(), void.class, String.class, String.class, String.class);
							if (mLastPackageInfo == null || addPref.length == 0) {
								Helpers.log("AppInfoHook", "Unable to find field/class/method in SecurityCenter to hook");
								return;
							} else {
								addPref[0].setAccessible(true);
							}
							addPref[0].invoke(frag, "apk_versioncode", modRes.getString(R.string.appdetails_apk_version_code), String.valueOf(mLastPackageInfo.versionCode));
							addPref[0].invoke(frag, "apk_filename", modRes.getString(R.string.appdetails_apk_file), mLastPackageInfo.applicationInfo.sourceDir);
							addPref[0].invoke(frag, "data_path", modRes.getString(R.string.appdetails_data_path), mLastPackageInfo.applicationInfo.dataDir);
							addPref[0].invoke(frag, "app_uid", modRes.getString(R.string.appdetails_app_uid), String.valueOf(mLastPackageInfo.applicationInfo.uid));
							addPref[0].invoke(frag, "target_sdk", modRes.getString(R.string.appdetails_sdk), String.valueOf(mLastPackageInfo.applicationInfo.targetSdkVersion));
							handler.post(new Runnable() {
								@Override
								public void run() {
									try {
										addPref[0].invoke(frag, "open_in_store", modRes.getString(R.string.appdetails_playstore), "");
										addPref[0].invoke(frag, "launch_app", modRes.getString(R.string.appdetails_launch), "");
									} catch (Throwable t) {
										XposedBridge.log(t);
									}
								}
							});
						} catch (Throwable t) {
							XposedBridge.log(t);
							return;
						}

						XposedBridge.hookAllMethods(frag.getClass(), "onPreferenceTreeClick", new MethodHook() {
							@Override
							protected void before(final MethodHookParam param) throws Throwable {
								String key = (String)XposedHelpers.callMethod(param.args[0], "getKey");
								String title = (String)XposedHelpers.callMethod(param.args[0], "getTitle");
								switch (key) {
									case "apk_filename":
										((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.sourceDir));
										Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
										param.setResult(true);
										break;
									case "data_path":
										((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.dataDir));
										Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
										param.setResult(true);
										break;
									case "open_in_store":
										try {
											Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mLastPackageInfo.packageName));
											launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
											act.startActivity(launchIntent);
										} catch (android.content.ActivityNotFoundException anfe) {
											Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + mLastPackageInfo.packageName));
											launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
											act.startActivity(launchIntent);
										}
										param.setResult(true);
										break;
									case "launch_app":
										Intent launchIntent = act.getPackageManager().getLaunchIntentForPackage(mLastPackageInfo.packageName);
										if (launchIntent == null) {
											Toast.makeText(act, modRes.getString(R.string.appdetails_nolaunch), Toast.LENGTH_SHORT).show();
										} else {
											int user = 0;
											try {
												int uid = act.getIntent().getIntExtra("am_app_uid", -1);
												user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
											} catch (Throwable t) {
												XposedBridge.log(t);
											}

											launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
											if (user != 0) try {
												XposedHelpers.callMethod(act, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle.class, user));
											} catch (Throwable t) {
												XposedBridge.log(t);
											} else {
												act.startActivity(launchIntent);
											}
										}
										param.setResult(true);
										break;
								}
							}
						});
					}
				});
			}
		});
	}

	public static Bundle checkBundle(Context context, Bundle bundle) {
		if (context == null) {
			Helpers.log("AppsDefaultSortHook", "Context is null!");
			return null;
		}
		if (bundle == null) bundle = new Bundle();
		int order = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_various_appsort", "1"));
		order = order - 1;
		bundle.putInt("current_sory_type", order); // Xiaomi noob typos :)
		bundle.putInt("current_sort_type", order); // Future proof, they may fix it someday :D
		return bundle;
	}

	public static void AppsDefaultSortHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				param.args[0] = checkBundle((Context)param.thisObject, (Bundle)param.args[0]);

				// Bruteforce class on MIUI 12.5
				String fragCls = null;
				Class<?> xfragCls = findClassIfExists("androidx.fragment.app.Fragment", lpparam.classLoader);
				Field[] fields = param.thisObject.getClass().getDeclaredFields();
				for (Field field: fields)
					if (Fragment.class.isAssignableFrom(field.getType()) ||
					   (xfragCls != null && xfragCls.isAssignableFrom(field.getType()))) {
						fragCls = field.getType().getCanonicalName();
						break;
					}

				if (fragCls != null)
					Helpers.hookAllMethods(fragCls, lpparam.classLoader, "onActivityCreated", new MethodHook() {
						@Override
						protected void before(final MethodHookParam param) throws Throwable {
							try {
								param.args[0] = checkBundle((Context)XposedHelpers.callMethod(param.thisObject, "getContext"), (Bundle)param.args[0]);
							} catch (Throwable t) {
								Helpers.log("AppsDefaultSortHook", t.getMessage());
							}
						}
					});
			}
		});

//		Helpers.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.classLoader, "onSaveInstanceState", Bundle.class, new MethodHook() {
//			@Override
//			protected void after(final MethodHookParam param) throws Throwable {
//				Bundle bundle = (Bundle)param.args[0];
//				if (bundle == null) bundle = new Bundle();
//				bundle.putInt("current_sory_type", 1); // Xiaomi noob typos :)
//				bundle.putInt("current_sort_type", 1); // Future proof, they may fix it someday :D
//				Helpers.log("onSaveInstanceState: " + String.valueOf(bundle));
//			}
//		});
	}

	private static void setAppState(final Activity act, String pkgName, MenuItem item, boolean enable) {
		try {
			PackageManager pm = act.getPackageManager();
			pm.setApplicationEnabledSetting(pkgName, enable ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
			int state = pm.getApplicationEnabledSetting(pkgName);
			boolean isEnabledOrDefault = (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
			if ((enable && isEnabledOrDefault) || (!enable && !isEnabledOrDefault)) {
				item.setTitle(act.getResources().getIdentifier(enable ? "app_manager_disable_text" : "app_manager_enable_text", "string", "com.miui.securitycenter"));
				Toast.makeText(act, act.getResources().getIdentifier(enable ? "app_manager_enabled" : "app_manager_disabled", "string", "com.miui.securitycenter"), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(act, Helpers.getModuleRes(act).getString(R.string.disable_app_fail), Toast.LENGTH_LONG).show();
			}
			new Handler().postDelayed(act::invalidateOptionsMenu, 500);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	static ArrayList<String> MIUI_CORE_APPS = new ArrayList<String>(Arrays.asList(
		"com.lbe.security.miui", "com.miui.securitycenter", "com.miui.packageinstaller"
	));
	public static void AppsDisableServiceHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.pm.PackageManagerServiceImpl", lpparam.classLoader, "canBeDisabled", String.class, int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean canBeDisabled = (boolean) param.getResult();
				if (!canBeDisabled && !MIUI_CORE_APPS.contains(param.args[0])) {
					param.setResult(true);
				}
			}
		});
	}

	public static void AppsDisableHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onCreateOptionsMenu", Menu.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				Menu menu = (Menu)param.args[0];
				MenuItem dis = menu.add(0, 666, 1, act.getResources().getIdentifier("app_manager_disable_text", "string", lpparam.packageName));
				dis.setIcon(act.getResources().getIdentifier("action_button_stop", "drawable", lpparam.packageName));
				dis.setEnabled(true);
				dis.setShowAsAction(1);
				//XposedHelpers.setAdditionalInstanceField(param.thisObject, "mDisableButton", dis);

				PackageManager pm = act.getPackageManager();
				Field piField = XposedHelpers.findFirstFieldByExactType(act.getClass(), PackageInfo.class);
				PackageInfo mPackageInfo = (PackageInfo)piField.get(act);
				ApplicationInfo appInfo = pm.getApplicationInfo(mPackageInfo.packageName, PackageManager.GET_META_DATA);
				boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

				dis.setTitle(act.getResources().getIdentifier(appInfo.enabled ? "app_manager_disable_text" : "app_manager_enable_text", "string", lpparam.packageName));

				if (!appInfo.enabled || (isSystem && !isUpdatedSystem)) {
					MenuItem item = menu.findItem(2);
					if (item != null) item.setVisible(false);
				}
			}
		});

		Helpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onOptionsItemSelected", MenuItem.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				MenuItem item = (MenuItem)param.args[0];
				if (item == null || item.getItemId() != 666) return;

				Activity act = (Activity)param.thisObject;
				Resources modRes = Helpers.getModuleRes(act);
				Field piField = XposedHelpers.findFirstFieldByExactType(act.getClass(), PackageInfo.class);
				PackageInfo mPackageInfo = (PackageInfo)piField.get(act);
				if (MIUI_CORE_APPS.contains(mPackageInfo.packageName)) {
					Toast.makeText(act, modRes.getString(R.string.disable_app_settings), Toast.LENGTH_SHORT).show();
					return;
				}

				PackageManager pm = act.getPackageManager();
				ApplicationInfo appInfo = pm.getApplicationInfo(mPackageInfo.packageName, PackageManager.GET_META_DATA);
				boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				int state = pm.getApplicationEnabledSetting(mPackageInfo.packageName);
				boolean isEnabledOrDefault = (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
				if (isEnabledOrDefault) {
					if (isSystem) {
						String title = modRes.getString(R.string.disable_app_title);
						String text = modRes.getString(R.string.disable_app_text);
						new AlertDialog.Builder(act).setTitle(title).setMessage(text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								setAppState(act, mPackageInfo.packageName, item, false);
							}
						}).setNegativeButton(android.R.string.cancel, null).show();
					} else setAppState(act, mPackageInfo.packageName, item, false);
				} else setAppState(act, mPackageInfo.packageName, item, true);
				param.setResult(true);
			}
		});
	}

	public static void AppsRestrictHook(LoadPackageParam lpparam) {
		Method[] mGetAppInfo = XposedHelpers.findMethodsByExactParameters(findClass("com.miui.appmanager.AppManageUtils", lpparam.classLoader), ApplicationInfo.class, Object.class, PackageManager.class, String.class, int.class, int.class);
		if (mGetAppInfo.length == 0)
			Helpers.log("AppsRestrictHook", "Cannot find getAppInfo method!");
		else
			Helpers.hookMethod(mGetAppInfo[0], new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					if ((int)param.args[3] == 128 && (int)param.args[4] == 0) {
						ApplicationInfo appInfo = (ApplicationInfo)param.getResult();
						appInfo.flags &= ~ApplicationInfo.FLAG_SYSTEM;
						param.setResult(appInfo);
					}
				}
			});

		Helpers.findAndHookMethod("com.miui.networkassistant.ui.fragment.ShowAppDetailFragment", lpparam.classLoader, "initFirewallData", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Object mAppInfo = XposedHelpers.getObjectField(param.thisObject, "mAppInfo");
				if (mAppInfo != null) XposedHelpers.setBooleanField(mAppInfo, "isSystemApp", false);
			}
		});

		Helpers.hookAllMethods("com.miui.networkassistant.service.FirewallService", lpparam.classLoader, "setSystemAppWifiRuleAllow", XC_MethodReplacement.DO_NOTHING);
	}

	@SuppressWarnings("unchecked")
	public static void AppsRestrictPowerHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.powerkeeper.provider.PowerKeeperConfigureManager", lpparam.classLoader, "pkgHasIcon", String.class, XC_MethodReplacement.returnConstant(true));
		//Helpers.hookAllMethods("com.miui.powerkeeper.provider.PowerKeeperConfigureManager", lpparam.classLoader, "isControlApp", XC_MethodReplacement.returnConstant(true));

		Helpers.findAndHookMethod("com.miui.powerkeeper.provider.PreSetGroup", lpparam.classLoader, "initGroup", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				HashMap<String, Integer> mGroupHeadUidMap = (HashMap<String, Integer>)XposedHelpers.getStaticObjectField(findClass("com.miui.powerkeeper.provider.PreSetGroup", lpparam.classLoader), "mGroupHeadUidMap");
				mGroupHeadUidMap.clear();
			}
		});

		Helpers.findAndHookMethod("com.miui.powerkeeper.provider.PreSetApp", lpparam.classLoader, "isPreSetApp", String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.hookAllMethods("com.miui.powerkeeper.utils.Utils", lpparam.classLoader, "pkgHasIcon", XC_MethodReplacement.returnConstant(true));
	}

	public static void PersistBatteryOptimizationHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.powerkeeper.utils.CommonAdapter", lpparam.classLoader, "addPowerSaveWhitelistApps", XC_MethodReplacement.DO_NOTHING);
	}

	private static void showSideBar(View view, int dockLocation) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int y = location[1];
		long uptimeMillis = SystemClock.uptimeMillis();
		MotionEvent downEvent, moveEvent, upEvent;
		if (dockLocation == 0) { // left
			downEvent = MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN,  4, y + 15, 0);
			moveEvent = MotionEvent.obtain(uptimeMillis, uptimeMillis + 20, MotionEvent.ACTION_MOVE, 160, y + 15, 0);
			upEvent = MotionEvent.obtain(uptimeMillis, uptimeMillis + 21, MotionEvent.ACTION_UP, 160, y + 15, 0);
		}
		else {
			int x = location[0];
			downEvent = MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN, x - 4, y + 15, 0);
			moveEvent = MotionEvent.obtain(uptimeMillis, uptimeMillis + 20, MotionEvent.ACTION_MOVE, x - 160, y + 15, 0);
			upEvent = MotionEvent.obtain(uptimeMillis, uptimeMillis + 21, MotionEvent.ACTION_UP, x - 160, y + 15, 0);
		}
		downEvent.setSource(9999);
		moveEvent.setSource(9999);
		upEvent.setSource(9999);
		view.dispatchTouchEvent(downEvent);
		view.dispatchTouchEvent(moveEvent);
		view.dispatchTouchEvent(upEvent);
		downEvent.recycle();
		moveEvent.recycle();
		upEvent.recycle();
	}

	public static void AddSideBarExpandReceiverHook(LoadPackageParam lpparam) {
		final boolean[] isHooked = {false, false};
		boolean enableSideBar = MainModule.mPrefs.getBoolean("various_swipe_expand_sidebar");
		if (!enableSideBar) {
			MainModule.resHooks.setDensityReplacement("com.miui.securitycenter", "dimen", "sidebar_height_default", 8);
			MainModule.resHooks.setDensityReplacement("com.miui.securitycenter", "dimen", "sidebar_height_vertical", 8);
		}
		Class <?> RegionSamplingHelper = findClassIfExists("com.android.systemui.navigationbar.gestural.RegionSamplingHelper", lpparam.classLoader);
		if (RegionSamplingHelper == null) {
			Helpers.log("AddSideBarExpandReceiverHook", "failed to find RegionSamplingHelper");
		}
		Helpers.hookAllConstructors(RegionSamplingHelper, new MethodHook() {
			private int originDockLocation = -1;
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (!isHooked[0]) {
					isHooked[0] = true;
					View view = (View) param.args[0];
					if (originDockLocation == -1) {
						originDockLocation = view.getContext().getSharedPreferences("sp_video_box", 0).getInt("dock_line_location", 0);;
					}
					BroadcastReceiver showReceiver = new BroadcastReceiver() {
						@Override
						public void onReceive(Context context, Intent intent) {
							Bundle bundle = intent.getBundleExtra("actionInfo");
							int pos = originDockLocation;
							if (bundle != null) {
								pos = bundle.getInt("inDirection", 0);
								view.getContext().getSharedPreferences("sp_video_box", 0).edit().putInt("dock_line_location", pos).commit();
							}
							showSideBar(view, pos);
						}
					};
					view.getContext().registerReceiver(showReceiver, new IntentFilter(ACTION_PREFIX + "ShowSideBar"));
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "showReceiver", showReceiver);

					if (!isHooked[1]) {
						isHooked[1] = true;
						Handler myhandler = new Handler(Looper.myLooper());
						Runnable removeBg = new Runnable() {
							@Override
							public void run() {
								myhandler.removeCallbacks(this);
								if (!enableSideBar) {
									Object li = XposedHelpers.getObjectField(view, "mListenerInfo");
									Object mOnTouchListener = XposedHelpers.getObjectField(li, "mOnTouchListener");
									Helpers.findAndHookMethod(mOnTouchListener.getClass(), "onTouch", View.class, MotionEvent.class, new MethodHook() {
										@Override
										protected void before(MethodHookParam param) throws Throwable {
											MotionEvent me = (MotionEvent) param.args[1];
											if (me.getSource() != 9999) {
												param.setResult(false);
											}
										}
									});
								}
								Class <?> bgDrawable = view.getBackground().getClass();
								Helpers.findAndHookMethod(bgDrawable, "draw", Canvas.class, new MethodHook() {
									@Override
									protected void before(MethodHookParam param) throws Throwable {
										param.setResult(null);
									}
								});
								view.setBackground(null);
							}
						};
						myhandler.postDelayed(removeBg, 150);
					}
				}
			}
		});
		Helpers.findAndHookMethod(RegionSamplingHelper, "onViewDetachedFromWindow", android.view.View.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				isHooked[0] = false;
				BroadcastReceiver showReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(param.thisObject, "showReceiver");
				if (showReceiver != null) {
					View view = (View) param.args[0];
					view.getContext().unregisterReceiver(showReceiver);
					XposedHelpers.removeAdditionalInstanceField(param.thisObject, "showReceiver");
				}
			}
		});
		Method[] methods = XposedHelpers.findMethodsByExactParameters(RegionSamplingHelper, void.class, Rect.class);
		if (methods.length == 0) {
			Helpers.log("AddSideBarExpandReceiverHook", "Cannot find appropriate start method");
			return;
		}
		Helpers.hookMethod(methods[0], new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.setResult(null);
			}
		});
	}

	public static void InterceptPermHook(LoadPackageParam lpparam) {
		Class<?> InterceptBaseFragmentClass = XposedHelpers.findClass("com.miui.permcenter.privacymanager.InterceptBaseFragment", lpparam.classLoader);
		Class<?>[] innerClasses = InterceptBaseFragmentClass.getDeclaredClasses();
		Class<?> HandlerClass = null;
		for (Class<?> innerClass : innerClasses) {
			if (Handler.class.isAssignableFrom(innerClass)) {
				HandlerClass = innerClass;
				break;
			}
		}
		if (HandlerClass != null) {
			Helpers.hookAllConstructors(HandlerClass, new MethodHook() {
				@Override
				protected void before(final MethodHookParam param) throws Throwable {
					if (param.args.length == 2) {
						param.args[1] = 0;
					}
				}
			});
			Method[] methods = XposedHelpers.findMethodsByExactParameters(HandlerClass, void.class, int.class);
			if (methods.length > 0) {
				Helpers.hookMethod(methods[0], new MethodHook() {
					@Override
					protected void before(final MethodHookParam param) throws Throwable {
						param.args[0] = 0;
					}
				});
			}
		}
	}

	public static void OpenByDefaultHook(LoadPackageParam lpparam) {
		final int[] defaultViewId = {-1};
		Helpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "initView", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (defaultViewId[0] == -1) {
					Activity act = (Activity) param.thisObject;
					defaultViewId[0] = act.getResources().getIdentifier("am_detail_default", "id", "com.miui.securitycenter");
					MainModule.resHooks.setResReplacement("com.miui.securitycenter", "string", "app_manager_default_open_title", R.string.various_open_by_default_title);
				}
			}
		});

		Helpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onClick", View.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				View view = (View) param.args[0];
				if (view.getId() == defaultViewId[0] && defaultViewId[0] != -1) {
					Activity act = (Activity) param.thisObject;
					Intent intent = new Intent("android.settings.APP_OPEN_BY_DEFAULT_SETTINGS");
					String pkgName = act.getIntent().getStringExtra("package_name");
					intent.setData(Uri.parse("package:".concat(pkgName)));
					act.startActivity(intent);
					param.setResult(null);
				}
			}
		});
	}
	public static void SkipSecurityScanHook(LoadPackageParam lpparam) {
		MethodHook skipScan = new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.setResult(new ArrayList<>());
			}
		};
		Helpers.findAndHookMethod("com.miui.securityscan.model.ModelFactory", lpparam.classLoader, "produceSystemGroupModel", Context.class, skipScan);
		Helpers.findAndHookMethod("com.miui.securityscan.model.ModelFactory", lpparam.classLoader, "produceManualGroupModel", Context.class, skipScan);
		Helpers.findAndHookMethod("com.miui.common.customview.ScoreTextView", lpparam.classLoader, "setScore", int.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.args[0] = 100;
			}
		});
		Helpers.findAndHookMethod(ContentResolver.class, "call", Uri.class, String.class, String.class, Bundle.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if ("callPreference".equals(param.args[1]) && "GET".equals(param.args[2])) {
					Bundle extras = (Bundle) param.args[3];
					if (extras != null && "latest_optimize_date".equals(extras.getString("key"))) {
						Bundle res = new Bundle();
						res.putLong("latest_optimize_date", java.lang.System.currentTimeMillis() - 10000);
						param.setResult(res);
					}
				}
			}
		});
		Helpers.findAndHookMethod("com.miui.securityscan.ui.main.MainContentFrame", lpparam.classLoader, "onClick", View.class, XC_MethodReplacement.DO_NOTHING);
//		Helpers.findAndHookMethod("com.miui.securityscan.ui.main.MainContentFrame", lpparam.classLoader, "setActionButtonText", java.lang.String.class, new MethodHook() {
//			int btnId = 0;
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				View mainFrame = (View) param.thisObject;
//				if (btnId == 0) {
//					btnId = mainFrame.getResources().getIdentifier("btn_action", "id", lpparam.packageName);
//				}
//				View btn = mainFrame.findViewById(btnId);
//				if (btn != null) {
//					btn.setVisibility(View.GONE);
//					btn.setOnClickListener(null);
//				}
//				XposedHelpers.callMethod(param.thisObject, "setContentMainClickable", false);
//			}
//		});
	}

	public static void SmartClipboardActionHook(LoadPackageParam lpparam) {
		int opt = MainModule.mPrefs.getStringAsInt("various_clipboard_defaultaction", 1);
		if (opt == 3) {
			Helpers.findAndHookMethod("com.lbe.security.ui.ClipboardTipDialog", lpparam.classLoader, "customReadClipboardDialog", Context.class, String.class, XC_MethodReplacement.returnConstant(false));
		}
		else {
			Helpers.findAndHookMethod("com.lbe.security.ui.ClipboardTipDialog", lpparam.classLoader, "customReadClipboardDialog", Context.class, String.class, XC_MethodReplacement.returnConstant(true));

			Class<?> SecurityPromptHandler = findClass("com.lbe.security.ui.SecurityPromptHandler", lpparam.classLoader);
			Helpers.hookAllMethods(SecurityPromptHandler, "handleNewRequest", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					Object permissionRequest = param.args[0];
					long permId = (long) XposedHelpers.callMethod(permissionRequest, "getPermission");
					if (permId == 274877906944L) {
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "currentStopped", XposedHelpers.getBooleanField(param.thisObject, "mStopped"));
					}
				}
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					Object permissionRequest = param.args[0];
					long permId = (long) XposedHelpers.callMethod(permissionRequest, "getPermission");
					if (permId == 274877906944L) {
						boolean mStopped = (boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "currentStopped");
						if (mStopped) {
							XposedHelpers.callMethod(param.thisObject, "gotChoice", 3, true, true);
						}
						XposedHelpers.removeAdditionalInstanceField(param.thisObject, "currentStopped");
					}
				}
			});
		}
	}

	public static void ShowTempInBatteryHook(LoadPackageParam lpparam) {
		Class<?> InterceptBaseFragmentClass = XposedHelpers.findClass("com.miui.powercenter.BatteryFragment", lpparam.classLoader);
		Class<?>[] innerClasses = InterceptBaseFragmentClass.getDeclaredClasses();
		Class<?> HandlerClass = null;
		for (Class<?> innerClass : innerClasses) {
			if (Handler.class.isAssignableFrom(innerClass)) {
				HandlerClass = innerClass;
				break;
			}
		}
		if (HandlerClass != null) {
			Field[] fields = HandlerClass.getDeclaredFields();
			String fieldName = null;
			for (Field field: fields) {
				if (WeakReference.class.isAssignableFrom(field.getType())) {
					fieldName = field.getName();
					break;
				}
			}
			if (fieldName == null) {
				return;
			}
			String finalFieldName = fieldName;
			XposedHelpers.findAndHookMethod(HandlerClass, "handleMessage", Message.class, new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					Message msg = (Message) param.args[0];
					int i = msg.what;
					if (i == 1) {
						Object wk = XposedHelpers.getObjectField(param.thisObject, finalFieldName);
						Object frag = XposedHelpers.callMethod(wk, "get");
						Activity batteryView = (Activity) XposedHelpers.callMethod(frag, "getActivity");
						int temp = batteryView.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("temperature", 0) / 10;
						int symbolResId = batteryView.getResources().getIdentifier("temp_symbol", "id", lpparam.packageName);
						int stateResId = batteryView.getResources().getIdentifier("current_temperature_state", "id", lpparam.packageName);
						TextView stateTv = batteryView.findViewById(stateResId);
						if (symbolResId > 0) {
							stateTv.setVisibility(View.GONE);
							TextView symbolTv = batteryView.findViewById(symbolResId);
							symbolTv.setVisibility(View.VISIBLE);
							int digitResId = batteryView.getResources().getIdentifier("current_temperature_value", "id", lpparam.packageName);
							TextView digitTv = batteryView.findViewById(digitResId);
							digitTv.setText(temp + "");
							digitTv.setVisibility(View.VISIBLE);
						}
						else {
							stateTv.setText(temp + "℃");
						}
					}
				}
			});
		}
	}
	public static void DisableDockSuggestHook(LoadPackageParam lpparam) {
		MethodHook clearHook = new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				ArrayList<String> blackList = new ArrayList<String>();
				blackList.add("xx.yy.zz");
				int topMethod = 10;
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				for (StackTraceElement el: stackTrace) {
					if (el != null && topMethod < 20
						&& (el.getClassName().contains("edit.DockAppEditActivity") || el.getClassName().contains("BubblesSettings"))
					) {
						return;
					}
					topMethod++;
				}
				param.setResult(blackList);
			}
		};
		Helpers.hookAllMethodsSilently("android.util.MiuiMultiWindowUtils", lpparam.classLoader, "getFreeformSuggestionList", clearHook);
	}
	public static void UnlockClipboardAndLocationHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.permcenter.settings.PrivacyLabActivity", lpparam.classLoader, "onCreateFragment", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Class<?> utilCls = findClassIfExists("com.miui.permcenter.utils.h", lpparam.classLoader);
				if (utilCls != null) {
					Object fm = Helpers.getStaticObjectFieldSilently(utilCls, "b");
					if (!Helpers.NOT_EXIST_SYMBOL.equals(fm)) {
						try {
							Map<String, Integer> featMap = (Map<String, Integer>) fm;
							featMap.put("mi_lab_ai_clipboard_enable", 0);
							featMap.put("mi_lab_blur_location_enable", 0);
						}
						catch (Throwable ignore) {
						}
					}
				}
			}
		});
	}

	public static void AlarmCompatHook() {
		Helpers.findAndHookMethod(Settings.System.class, "getStringForUser", ContentResolver.class, String.class, int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				String key = (String)param.args[1];
				if ("next_alarm_formatted".equals(key)) {
					param.args[1] = "next_alarm_clock_formatted";
				}
			}
		});
	}

	public static void AlarmCompatServiceHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.alarm.AlarmManagerService", lpparam.classLoader, "onBootPhase", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if ((int)param.args[0] != 500 /*PHASE_SYSTEM_SERVICES_READY*/) return;

				Context mContext = (Context)XposedHelpers.callMethod(param.thisObject, "getContext");
				if (mContext == null) {
					Helpers.log("AlarmCompatServiceHook", "Context is NULL");
					return;
				}
				ContentResolver resolver = mContext.getContentResolver();
				ContentObserver alarmObserver = new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						if (selfChange) return;
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mNextAlarmTime", Helpers.getNextMIUIAlarmTime(mContext));
					}
				};
				alarmObserver.onChange(false);
				resolver.registerContentObserver(Settings.System.getUriFor("next_alarm_clock_formatted"), false, alarmObserver);
			}
		});

		Helpers.findAndHookMethod("com.android.server.alarm.AlarmManagerService", lpparam.classLoader, "getNextAlarmClockImpl", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.callMethod(param.thisObject, "getContext");
				String pkgName = mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
				Object mNextAlarmTime = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mNextAlarmTime");
				if (mNextAlarmTime != null && MainModule.mPrefs.getStringSet("various_alarmcompat_apps").contains(pkgName))
					param.setResult((long)mNextAlarmTime == 0 ? null : new AlarmManager.AlarmClockInfo((long)mNextAlarmTime, null));
			}
		});
	}

	public static void AnswerCallInHeadUpHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.incallui.InCallPresenter", lpparam.classLoader, "answerIncomingCall", Context.class, String.class, int.class, boolean.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				boolean showUi = (boolean) param.args[3];
				if (showUi) {
					ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
					if (foregroundInfo != null) {
						String topPackage = foregroundInfo.mForegroundPackageName;
						if (!"com.miui.home".equals(topPackage)) {
							param.args[3] = false;
						}
					}
				}
			}
		});
	}

	public static void ShowCallUIHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.incallui.InCallPresenter", lpparam.classLoader, "startUi", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (!(boolean)param.getResult() || !"INCOMING".equals(param.args[0].toString())) return;
				try {
					boolean isCarMode = (boolean)XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.incallui.carmode.CarModeUtils", lpparam.classLoader), "isCarMode");
					if (isCarMode) return;
				} catch (Throwable t) {
					Helpers.log(t);
				}
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (MainModule.mPrefs.getStringAsInt("various_showcallui", 0) == 3) {
					String topPackage = Settings.Global.getString(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.package");
					if (topPackage != null && !topPackage.equals("com.miui.home")) {
						return;
					}
				}

				if (MainModule.mPrefs.getStringAsInt("various_showcallui", 0) == 1) {
					int fullScreen = Settings.Global.getInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", 0);
					if (fullScreen == 1) return;
				}

				XposedHelpers.callMethod(param.thisObject, "showInCall", false, false);
				Object mStatusBarNotifier = XposedHelpers.getObjectField(param.thisObject, "mStatusBarNotifier");
				if (mStatusBarNotifier != null) XposedHelpers.callMethod(mStatusBarNotifier, "cancelInCall");
				param.setResult(true);
			}
		});
	}

	public static void InCallBrightnessHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.incallui.InCallActivity", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;

				int opt = Integer.parseInt(Helpers.getSharedStringPref(act, "pref_key_various_calluibright_type", "0"));
				if (opt == 1 || opt == 2) {
					Object presenter = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.incallui.InCallPresenter", lpparam.classLoader), "getInstance");
					if (presenter == null) {
						Helpers.log("InCallBrightnessHook", "InCallPresenter is null");
						return;
					}

					String state = String.valueOf(XposedHelpers.callMethod(presenter, "getInCallState"));
					if (opt == 1 && !"INCOMING".equals(state)) return;
					else if (opt == 2 && !"OUTGOING".equals(state) && !"PENDING_OUTGOING".equals(state)) return;
				}

				String key = "pref_key_various_calluibright_night";
				boolean checkNight = Helpers.getSharedBoolPref(act, key, false);
				if (checkNight) {
					int start_hour = Helpers.getSharedIntPref(act, key + "_start_hour", 0);
					int start_minute = Helpers.getSharedIntPref(act, key + "_start_minute", 0);
					int end_hour = Helpers.getSharedIntPref(act, key + "_end_hour", 0);
					int end_minute = Helpers.getSharedIntPref(act, key + "_end_minute", 0);

					SimpleDateFormat formatter = new SimpleDateFormat("H:m", Locale.ENGLISH);
					formatter.setTimeZone(TimeZone.getDefault());
					Date start = formatter.parse(start_hour + ":" + start_minute);
					Date end = formatter.parse(end_hour + ":" + end_minute);
					Date now = formatter.parse(formatter.format(new Date()));
					if (start == null || end == null || now == null) return;

					boolean isNight = start.before(end) ? now.after(start) && now.before(end) : now.before(end) || now.after(start);
					if (isNight) return;
				}

				WindowManager.LayoutParams params = act.getWindow().getAttributes();
				int val = Helpers.getSharedIntPref(act, "pref_key_various_calluibright_val", 0);
				if (val == 0) return;
				params.screenBrightness = val / 100f;
				act.getWindow().setAttributes(params);
			}
		});
	}

	private static TextView createTitleTextView(Context context, ViewGroup.LayoutParams lp, int resId) {
		TextView tv = new TextView(context);
		tv.setMaxLines(1);
		tv.setSingleLine(true);
		tv.setGravity(Gravity.START);
		tv.setLayoutParams(lp);
		tv.setTextAppearance(resId != -1 ? resId : android.R.style.TextAppearance_DeviceDefault);
		return tv;
	}

	private static TextView createValueTextView(Context context, ViewGroup.LayoutParams lp, int resId, int gravity) {
		TextView tv = new TextView(context);
		tv.setMaxLines(1);
		tv.setSingleLine(true);
		tv.setGravity(gravity);
		tv.setEllipsize(TextUtils.TruncateAt.START);
		tv.setLayoutParams(lp);
		tv.setTextAppearance(resId != -1 ? resId : android.R.style.TextAppearance_DeviceDefault);
		return tv;
	}

	public static void AppInfoDuringInstallHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader, "startInstallConfirm", new MethodHook() {
			@Override
			@SuppressLint("SetTextI18n")
			protected void after(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				PackageInfo mPkgInfo = (PackageInfo)XposedHelpers.getObjectField(param.thisObject, "mPkgInfo");
				if (mPkgInfo == null) return;

				float density = act.getResources().getDisplayMetrics().density;

				TypedArray a = act.obtainStyledAttributes(new int[]{android.R.attr.textAppearance});
				int resId = a.getResourceId(0, -1);
				a.recycle();

				PackageInfo mAppInfo = null;
				try {
					PackageManager mPm = (PackageManager)XposedHelpers.getObjectField(param.thisObject, "mPm");
					mAppInfo = mPm.getPackageInfo(mPkgInfo.packageName, 0);
				} catch (Throwable ignore) {}

				Resources modRes = Helpers.getModuleRes(act);

				LinearLayout container = new LinearLayout(act);
				LinearLayout.LayoutParams lpCont = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lpCont.setMargins(Math.round(16.0f * density), Math.round(8.0f * density), Math.round(16.0f * density), Math.round(4.0f * density));
				container.setLayoutParams(lpCont);
				container.setOrientation(LinearLayout.VERTICAL);

				LinearLayout name = new LinearLayout(act);
				name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				name.setOrientation(LinearLayout.HORIZONTAL);

				LinearLayout.LayoutParams lpInfoTitle = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lpInfoTitle.setMargins(0, 0, Math.round(20.0f * density), 0);
				TextView infoTitle = createTitleTextView(act, lpInfoTitle, resId);
				infoTitle.setText(modRes.getString(R.string.various_installappinfo_package));
				LinearLayout.LayoutParams lpInfo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lpInfo.weight = 1;
				TextView info = createValueTextView(act, lpInfo, resId, Gravity.END);
				info.setText(mPkgInfo.applicationInfo.packageName);

				name.addView(infoTitle);
				name.addView(info);

				TableLayout table = new TableLayout(act);
				LinearLayout.LayoutParams lpTable = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				table.setLayoutParams(lpTable);
				table.setColumnStretchable(0, false);
				table.setColumnStretchable(1, true);
				table.setColumnStretchable(2, false);
				table.setColumnShrinkable(0, false);
				table.setColumnShrinkable(1, true);
				table.setColumnShrinkable(2, false);

				TableLayout.LayoutParams lpRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
				lpRow.gravity = Gravity.BOTTOM;
				TableRow row1 = new TableRow(act); row1.setLayoutParams(lpRow);
				TableRow row2 = new TableRow(act); row2.setLayoutParams(lpRow);
				TableRow row3 = new TableRow(act); row3.setLayoutParams(lpRow);

				TableRow.LayoutParams lp0 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				lp0.column = 0;
				lp0.setMargins(0, 0, Math.round(20.0f * density), 0);
				TableRow.LayoutParams lp1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
				lp1.column = 1;
				lp1.weight = 1;
				FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				lp2.gravity = Gravity.BOTTOM | Gravity.END;
				lp2.setMargins(0, 0, Math.round(20.0f * density), 0);
				TableRow.LayoutParams lp3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				lp3.column = 2;

				TextView infoTitle1 = createTitleTextView(act, lp0, resId);
				infoTitle1.setText(modRes.getString(R.string.various_installappinfo_vername));
				TextView infoTitle2 = createTitleTextView(act, lp0, resId);
				infoTitle2.setText(modRes.getString(R.string.various_installappinfo_vercode));
				TextView infoTitle3 = createTitleTextView(act, lp0, resId);
				infoTitle3.setText(modRes.getString(R.string.various_installappinfo_sdk));

				FrameLayout dummy1 = new FrameLayout(act); dummy1.setLayoutParams(lp1);
				FrameLayout dummy2 = new FrameLayout(act); dummy2.setLayoutParams(lp1);
				FrameLayout dummy3 = new FrameLayout(act); dummy3.setLayoutParams(lp1);

				String current = modRes.getString(R.string.various_installappinfo_current);

				TextView info1current = createValueTextView(act, lp2, resId, Gravity.START);
				if (mAppInfo != null) {
					SpannableString span = new SpannableString(current + " " + mAppInfo.versionName);
					span.setSpan(new RelativeSizeSpan(0.7f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					info1current.setText(span);
				} else info1current.setText(" ");

				TextView info2current = createValueTextView(act, lp2, resId, Gravity.START);
				if (mAppInfo != null) {
					SpannableString span = new SpannableString(current + " " + mAppInfo.versionCode);
					span.setSpan(new RelativeSizeSpan(0.7f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					info2current.setText(span);
				} else info2current.setText(" ");

				TextView info3current = createValueTextView(act, lp2, resId, Gravity.START);
				if (mAppInfo != null) {
					SpannableString span = new SpannableString(current + " " + mAppInfo.applicationInfo.minSdkVersion + "-" + mAppInfo.applicationInfo.targetSdkVersion);
					span.setSpan(new RelativeSizeSpan(0.7f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					info3current.setText(span);
				} else info3current.setText(" ");

				TextView info1 = createValueTextView(act, lp3, resId, Gravity.END);
				info1.setText(mPkgInfo.versionName);
				TextView info2 = createValueTextView(act, lp3, resId, Gravity.END);
				info2.setText(String.valueOf(mPkgInfo.versionCode));
				TextView info3 = createValueTextView(act, lp3, resId, Gravity.END);
				info3.setText(mPkgInfo.applicationInfo.minSdkVersion + "-" + mPkgInfo.applicationInfo.targetSdkVersion);

				row1.addView(infoTitle1);
				row2.addView(infoTitle2);
				row3.addView(infoTitle3);

				row1.addView(dummy1); dummy1.addView(info1current);
				row2.addView(dummy2); dummy2.addView(info2current);
				row3.addView(dummy3); dummy3.addView(info3current);

				row1.addView(info1);
				row2.addView(info2);
				row3.addView(info3);

				table.addView(row1);
				table.addView(row2);
				table.addView(row3);

				container.addView(name);
				container.addView(table);

				boolean isDialog = false;
				try { isDialog = "com.android.internal.app.AlertActivity".equals(act.getClass().getSuperclass().getCanonicalName()); } catch (Throwable ignore) {}
				ViewGroup parent = (ViewGroup)act.findViewById(act.getResources().getIdentifier("install_confirm_question", "id", "com.android.packageinstaller")).getParent();
				if (parent.getChildCount() == 1) parent = ((ViewGroup)parent.getParent());
				if (!isDialog) {
					parent.addView(container, parent.getChildCount() - 1);
				} else {
					ViewGroup fParent = parent;
					parent.post(new Runnable() {
						@Override
						public void run() {
							try {
								if (fParent.getMeasuredHeight() == 0)
								fParent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
								ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)container.getLayoutParams();
								lp.topMargin += fParent.getMeasuredHeight();
								lp.leftMargin = 0;
								lp.rightMargin = 0;
								container.setLayoutParams(lp);
								fParent.addView(container, fParent.getChildCount() - 1);
							} catch (Throwable ignore) {}
						}
					});
				}
			}
		});
	}

	public static void AppInfoDuringMiuiInstallHook(LoadPackageParam lpparam) {
		Class<?> AppInfoViewObjectClass = findClassIfExists("com.miui.packageInstaller.ui.listcomponets.AppInfoViewObject", lpparam.classLoader);
		if (AppInfoViewObjectClass != null) {
			Class<?> ViewHolderClass = findClassIfExists("com.miui.packageInstaller.ui.listcomponets.AppInfoViewObject$ViewHolder", lpparam.classLoader);
			Method[] methods = XposedHelpers.findMethodsByExactParameters(AppInfoViewObjectClass, void.class, ViewHolderClass);
			if (methods.length == 0) {
				Helpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate method");
				return;
			}
			Class<?> ApkInfoClass = findClassIfExists("com.miui.packageInstaller.model.ApkInfo", lpparam.classLoader);

			Field[] fields = AppInfoViewObjectClass.getDeclaredFields();
			String apkInfoFieldName = null;
			for (Field field: fields)
				if (ApkInfoClass.isAssignableFrom(field.getType())) {
					apkInfoFieldName = field.getName();
					break;
				}
			if (apkInfoFieldName == null) return;
			String finalApkInfoFieldName = apkInfoFieldName;
			Helpers.hookMethod(methods[0], new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					Object viewHolder = param.args[0];
					if (viewHolder == null) return;
					TextView tvAppVersion = (TextView) XposedHelpers.callMethod(viewHolder, "getTvDes");
					TextView tvAppSize = (TextView) XposedHelpers.callMethod(viewHolder, "getAppSize");
					TextView tvAppName = (TextView) XposedHelpers.callMethod(viewHolder, "getTvAppName");
					if (tvAppVersion == null) return;

					ViewGroup.MarginLayoutParams appNameLp = (ViewGroup.MarginLayoutParams) tvAppName.getLayoutParams();
					appNameLp.topMargin = 0;
					tvAppName.setLayoutParams(appNameLp);

					Object apkInfo = XposedHelpers.getObjectField(param.thisObject, finalApkInfoFieldName);
					ApplicationInfo mAppInfo = (ApplicationInfo) XposedHelpers.callMethod(apkInfo, "getInstalledPackageInfo");
					PackageInfo mPkgInfo = (PackageInfo) XposedHelpers.callMethod(apkInfo, "getPackageInfo");
					Resources modRes = Helpers.getModuleRes(tvAppVersion.getContext());
					SpannableStringBuilder builder = new SpannableStringBuilder();
					builder.append(modRes.getString(R.string.various_installappinfo_vername)).append(": ");
					if (mAppInfo != null) builder.append((String)XposedHelpers.callMethod(apkInfo, "getInstalledVersionName")).append(" ➟ ");
					builder.append(mPkgInfo.versionName).append("\n");
					builder.append(tvAppSize.getText()).append("\n");
					builder.append(modRes.getString(R.string.various_installappinfo_vercode)).append(": ");
					if (mAppInfo != null) builder.append(String.valueOf(XposedHelpers.callMethod(apkInfo, "getInstalledVersionCode"))).append(" ➟ ");
					builder.append(String.valueOf(mPkgInfo.getLongVersionCode())).append("\n");
					builder.append(modRes.getString(R.string.various_installappinfo_sdk)).append(": ");
					if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.minSdkVersion)).append("-").append(String.valueOf(mAppInfo.targetSdkVersion)).append(" ➟ ");
					builder.append(String.valueOf(mPkgInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mPkgInfo.applicationInfo.targetSdkVersion));

					tvAppVersion.setText(builder);
					tvAppVersion.setSingleLine(false);
					tvAppVersion.setMaxLines(10);
					LinearLayout layout = (LinearLayout) tvAppVersion.getParent();
					ViewGroup.MarginLayoutParams versionSizeLp = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
					versionSizeLp.topMargin = 0;
					layout.setLayoutParams(versionSizeLp);
					layout.removeAllViews();
					layout.addView(tvAppVersion);
				}
			});
		}
		else {
			Class<?> InstallActivity = findClassIfExists("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader);
			if (InstallActivity == null) {
				Helpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate activity");
				return;
			}
			Method[] methods = XposedHelpers.findMethodsByExactParameters(InstallActivity, void.class, String.class);
			if (methods.length == 0) {
				Helpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate method");
				return;
			}
			for (Method method: methods)
				Helpers.hookMethod(method, new MethodHook() {
					@Override
					protected void after(MethodHookParam param) throws Throwable {
						Activity act = (Activity)param.thisObject;
						TextView version = act.findViewById(act.getResources().getIdentifier("install_version", "id", lpparam.packageName));
						Field fPkgInfo = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), PackageInfo.class);
						PackageInfo mPkgInfo = (PackageInfo)fPkgInfo.get(param.thisObject);
						if (version == null || mPkgInfo == null) return;

						TextView source = act.findViewById(act.getResources().getIdentifier("install_source", "id", lpparam.packageName));
						source.setGravity(Gravity.CENTER_HORIZONTAL);
						source.setText(mPkgInfo.packageName);

						PackageInfo mAppInfo = null;
						try {
							mAppInfo = act.getPackageManager().getPackageInfo(mPkgInfo.packageName, 0);
						} catch (Throwable ignore) {}

						//String size = "";
						//String[] texts = version.getText().toString().split("\\|");
						//if (texts.length >= 2) size = texts[1].trim();

						Resources modRes = Helpers.getModuleRes(act);

						SpannableStringBuilder builder = new SpannableStringBuilder();
						//if (!TextUtils.isEmpty(size)) builder.append(size).append("\n");
						builder.append(modRes.getString(R.string.various_installappinfo_vername)).append(":\t\t");
						if (mAppInfo != null) builder.append(mAppInfo.versionName).append("  ➟  ");
						builder.append(mPkgInfo.versionName).append("\n");
						builder.append(modRes.getString(R.string.various_installappinfo_vercode)).append(":\t\t");
						if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.versionCode)).append("  ➟  ");
						builder.append(String.valueOf(mPkgInfo.versionCode)).append("\n");
						builder.append(modRes.getString(R.string.various_installappinfo_sdk)).append(":\t\t");
						if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mAppInfo.applicationInfo.targetSdkVersion)).append("  ➟  ");
						builder.append(String.valueOf(mPkgInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mPkgInfo.applicationInfo.targetSdkVersion));

						version.setGravity(Gravity.CENTER_HORIZONTAL);
						version.setSingleLine(false);
						version.setMaxLines(10);
						version.setText(builder);
						version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.09f);
					}
				});
		}
	}

	public static void MiuiPackageInstallerHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("android.os.SystemProperties", lpparam.classLoader, "getBoolean", String.class, boolean.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if ("persist.sys.allow_sys_app_update".equals(param.args[0])) {
					param.setResult(true);
				}
			}
		});
		Helpers.findAndHookMethodSilently("com.miui.packageInstaller.InstallStart", lpparam.classLoader, "getCallingPackage", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.setResult("com.android.fileexplorer");
			}
		});
	}

	public static void GboardPaddingHook() {
		Helpers.findAndHookMethod(findClass("android.os.SystemProperties", null), "get", String.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				String key = (String)param.args[0];
				if (key.equals("ro.com.google.ime.kb_pad_port_b")) {
					int opt = MainModule.mPrefs.getInt("various_gboardpadding_port", 0);
					if (opt > 0) param.setResult(String.valueOf(opt));
				} else if (key.equals("ro.com.google.ime.kb_pad_land_b")) {
					int opt = MainModule.mPrefs.getInt("various_gboardpadding_land", 0);
					if (opt > 0) param.setResult(String.valueOf(opt));
				}
			}
		});
	}

	public static void ScreenRecorderFramerateHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.screenrecorder.config.ScreenRecorderConfig", lpparam.classLoader, "setFramesValue", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.args[0] = 90;
			}
		});
	}

	public static void FixInputMethodBottomMarginHook(LoadPackageParam lpparam) {
		Class<?> InputMethodServiceInjectorClass = findClassIfExists("android.inputmethodservice.InputMethodServiceInjector", lpparam.classLoader);
		Helpers.hookAllMethods(InputMethodServiceInjectorClass, "addMiuiBottomView", new MethodHook() {
			private boolean isHooked = false;
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (isHooked) return;
				ClassLoader sClassLoader = (ClassLoader) XposedHelpers.getStaticObjectField(InputMethodServiceInjectorClass, "sClassLoader");
				if (sClassLoader != null) {
					isHooked = true;
					Class<?> InputMethodUtil = XposedHelpers.findClassIfExists("com.miui.inputmethod.InputMethodUtil", sClassLoader);
					XposedHelpers.setStaticBooleanField(InputMethodUtil, "sIsGestureLineEnable", false);
					Helpers.findAndHookMethod(InputMethodUtil, "updateGestureLineEnable", Context.class, new MethodHook() {
						@Override
						protected void before(MethodHookParam param) throws Throwable {
							XposedHelpers.setStaticBooleanField(InputMethodUtil, "sIsGestureLineEnable", false);
							param.setResult(null);
						}
					});
				}
			}
		});
	}

//	public static void LargeCallerPhotoHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.classLoader, "setCallCardImage", Drawable.class, boolean.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				param.args[1] = true;
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.classLoader, "showBigAvatar", boolean.class, Drawable.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				//Helpers.log("showBigAvatar: " + String.valueOf(param.args[0]) + " | " + String.valueOf(param.args[1]));
//				if (param.args[1] == null)
//					param.setResult(null);
//				else
//					param.args[0] = true;
//			}
//		});
//	}

}