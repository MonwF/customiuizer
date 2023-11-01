package name.monwf.customiuizer.mods;

import static name.monwf.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClassIfExists;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
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
import android.widget.GridView;
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
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.SystemServerLoadedParam;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.mods.utils.HookerClassHelper;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;

public class Various {

	public static PackageInfo mLastPackageInfo;
	public static Object mSupportFragment = null;
	public static void AppInfoHook(PackageLoadedParam lpparam) {
		Class<?> amaCls = XposedHelpers.findClassIfExists("com.miui.appmanager.AMAppInfomationActivity", lpparam.getClassLoader());
		if (amaCls == null) {
			XposedHelpers.log("AppInfoHook", "Cannot find activity class!");
			return;
		}

		if (findClassIfExists("androidx.fragment.app.Fragment", lpparam.getClassLoader()) != null) {
			ModuleHelper.findAndHookConstructor("androidx.fragment.app.Fragment", lpparam.getClassLoader(), new MethodHook() {
				@Override
				protected void before(final BeforeHookCallback param) throws Throwable {
					try {
						Field piField = XposedHelpers.findFirstFieldByExactType(param.getThisObject().getClass(), PackageInfo.class);
						if (piField != null) mSupportFragment = param.getThisObject();
					} catch (Throwable ignore) {}
				}
			});
		}

		ModuleHelper.findAndHookMethod(amaCls, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						final Activity act = (Activity)param.getThisObject();
						Object contentFrag = act.getFragmentManager().findFragmentById(android.R.id.content);
						Object frag = contentFrag != null ? contentFrag : mSupportFragment;
						if (frag == null) {
							XposedHelpers.log("AppInfoHook", "Unable to find fragment");
							return;
						}

						final Resources modRes;
						try {
							modRes = ModuleHelper.getModuleRes(act);
							Field piField = XposedHelpers.findFirstFieldByExactType(frag.getClass(), PackageInfo.class);
							mLastPackageInfo = (PackageInfo)piField.get(frag);
							Method[] addPref = XposedHelpers.findMethodsByExactParameters(frag.getClass(), void.class, String.class, String.class, String.class);
							if (mLastPackageInfo == null || addPref.length == 0) {
								XposedHelpers.log("AppInfoHook", "Unable to find field/class/method in SecurityCenter to hook");
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
										XposedHelpers.log(t);
									}
								}
							});
						} catch (Throwable t) {
							XposedHelpers.log(t);
							return;
						}

						ModuleHelper.hookAllMethods(frag.getClass(), "onPreferenceTreeClick", new MethodHook() {
							@Override
							protected void before(final BeforeHookCallback param) throws Throwable {
								String key = (String)XposedHelpers.callMethod(param.getArgs()[0], "getKey");
								String title = (String)XposedHelpers.callMethod(param.getArgs()[0], "getTitle");
								switch (key) {
									case "apk_filename":
										((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.sourceDir));
										Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
										param.returnAndSkip(true);
										break;
									case "data_path":
										((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.dataDir));
										Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
										param.returnAndSkip(true);
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
										param.returnAndSkip(true);
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
												XposedHelpers.log(t);
											}

											launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
											if (user != 0) try {
												XposedHelpers.callMethod(act, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle.class, user));
											} catch (Throwable t) {
												XposedHelpers.log(t);
											} else {
												act.startActivity(launchIntent);
											}
										}
										param.returnAndSkip(true);
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
			XposedHelpers.log("AppsDefaultSortHook", "Context is null!");
			return null;
		}
		if (bundle == null) bundle = new Bundle();
		int order = MainModule.mPrefs.getStringAsInt("various_appsort", 1);
		order = order - 1;
		bundle.putInt("current_sory_type", order); // Xiaomi noob typos :)
		bundle.putInt("current_sort_type", order); // Future proof, they may fix it someday :D
		return bundle;
	}

	public static void AppsDefaultSortHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.getArgs()[0] = checkBundle((Context)param.getThisObject(), (Bundle)param.getArgs()[0]);

				// Bruteforce class on MIUI 12.5
				String fragCls = null;
				Class<?> xfragCls = findClassIfExists("androidx.fragment.app.Fragment", lpparam.getClassLoader());
				Field[] fields = param.getThisObject().getClass().getDeclaredFields();
				for (Field field: fields)
					if (Fragment.class.isAssignableFrom(field.getType()) ||
					   (xfragCls != null && xfragCls.isAssignableFrom(field.getType()))) {
						fragCls = field.getType().getCanonicalName();
						break;
					}

				if (fragCls != null)
					ModuleHelper.hookAllMethods(fragCls, lpparam.getClassLoader(), "onActivityCreated", new MethodHook() {
						@Override
						protected void before(final BeforeHookCallback param) throws Throwable {
							try {
								param.getArgs()[0] = checkBundle((Context)XposedHelpers.callMethod(param.getThisObject(), "getContext"), (Bundle)param.getArgs()[0]);
							} catch (Throwable t) {
								XposedHelpers.log("AppsDefaultSortHook", t.getMessage());
							}
						}
					});
			}
		});

//		ModuleHelper.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.getClassLoader(), "onSaveInstanceState", Bundle.class, new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				Bundle bundle = (Bundle)param.getArgs()[0];
//				if (bundle == null) bundle = new Bundle();
//				bundle.putInt("current_sory_type", 1); // Xiaomi noob typos :)
//				bundle.putInt("current_sort_type", 1); // Future proof, they may fix it someday :D
//				XposedHelpers.log("onSaveInstanceState: " + String.valueOf(bundle));
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
				Toast.makeText(act, ModuleHelper.getModuleRes(act).getString(R.string.disable_app_fail), Toast.LENGTH_LONG).show();
			}
			new Handler().postDelayed(act::invalidateOptionsMenu, 500);
		} catch (Throwable t) {
			XposedHelpers.log(t);
		}
	}
	static ArrayList<String> MIUI_CORE_APPS = new ArrayList<String>(Arrays.asList(
		"com.lbe.security.miui", "com.miui.securitycenter", "com.miui.packageinstaller"
	));
	public static void AppsDisableServiceHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.pm.PackageManagerServiceImpl", lpparam.getClassLoader(), "canBeDisabled", String.class, int.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				boolean canBeDisabled = (boolean) param.getResult();
				if (!canBeDisabled && !MIUI_CORE_APPS.contains(param.getArgs()[0])) {
					param.setResult(true);
				}
			}
		});
	}

	public static void AppsDisableHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.getClassLoader(), "onCreateOptionsMenu", Menu.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				Activity act = (Activity)param.getThisObject();
				Menu menu = (Menu)param.getArgs()[0];
				MenuItem dis = menu.add(0, 666, 1, act.getResources().getIdentifier("app_manager_disable_text", "string", lpparam.getPackageName()));
				dis.setIcon(act.getResources().getIdentifier("action_button_stop", "drawable", lpparam.getPackageName()));
				dis.setEnabled(true);
				dis.setShowAsAction(1);
				//XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mDisableButton", dis);

				PackageManager pm = act.getPackageManager();
				Field piField = XposedHelpers.findFirstFieldByExactType(act.getClass(), PackageInfo.class);
				PackageInfo mPackageInfo = (PackageInfo)piField.get(act);
				ApplicationInfo appInfo = pm.getApplicationInfo(mPackageInfo.packageName, PackageManager.GET_META_DATA);
				boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

				dis.setTitle(act.getResources().getIdentifier(appInfo.enabled ? "app_manager_disable_text" : "app_manager_enable_text", "string", lpparam.getPackageName()));

				if (!appInfo.enabled || (isSystem && !isUpdatedSystem)) {
					MenuItem item = menu.findItem(2);
					if (item != null) item.setVisible(false);
				}
			}
		});

		ModuleHelper.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.getClassLoader(), "onOptionsItemSelected", MenuItem.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				MenuItem item = (MenuItem)param.getArgs()[0];
				if (item == null || item.getItemId() != 666) return;

				Activity act = (Activity)param.getThisObject();
				Resources modRes = ModuleHelper.getModuleRes(act);
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

	public static void AppsRestrictHook(PackageLoadedParam lpparam) {
		Method[] mGetAppInfo = XposedHelpers.findMethodsByExactParameters(findClass("com.miui.appmanager.AppManageUtils", lpparam.getClassLoader()), ApplicationInfo.class, Object.class, PackageManager.class, String.class, int.class, int.class);
		if (mGetAppInfo.length == 0)
			XposedHelpers.log("AppsRestrictHook", "Cannot find getAppInfo method!");
		else
			ModuleHelper.hookMethod(mGetAppInfo[0], new MethodHook() {
				@Override
				protected void after(final AfterHookCallback param) throws Throwable {
					if ((int)param.getArgs()[3] == 128 && (int)param.getArgs()[4] == 0) {
						ApplicationInfo appInfo = (ApplicationInfo)param.getResult();
						appInfo.flags &= ~ApplicationInfo.FLAG_SYSTEM;
						param.setResult(appInfo);
					}
				}
			});

		ModuleHelper.findAndHookMethod("com.miui.networkassistant.ui.fragment.ShowAppDetailFragment", lpparam.getClassLoader(), "initFirewallData", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				Object mAppInfo = XposedHelpers.getObjectField(param.getThisObject(), "mAppInfo");
				if (mAppInfo != null) XposedHelpers.setBooleanField(mAppInfo, "isSystemApp", false);
			}
		});

		ModuleHelper.hookAllMethods("com.miui.networkassistant.service.FirewallService", lpparam.getClassLoader(), "setSystemAppWifiRuleAllow", HookerClassHelper.DO_NOTHING);
	}

	@SuppressWarnings("unchecked")
	public static void AppsRestrictPowerHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.powerkeeper.provider.PowerKeeperConfigureManager", lpparam.getClassLoader(), "pkgHasIcon", String.class, HookerClassHelper.returnConstant(true));
		//ModuleHelper.hookAllMethods("com.miui.powerkeeper.provider.PowerKeeperConfigureManager", lpparam.getClassLoader(), "isControlApp", HookerClassHelper.returnConstant(true));

		ModuleHelper.findAndHookMethod("com.miui.powerkeeper.provider.PreSetGroup", lpparam.getClassLoader(), "initGroup", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				HashMap<String, Integer> mGroupHeadUidMap = (HashMap<String, Integer>)XposedHelpers.getStaticObjectField(findClass("com.miui.powerkeeper.provider.PreSetGroup", lpparam.getClassLoader()), "mGroupHeadUidMap");
				mGroupHeadUidMap.clear();
			}
		});

		ModuleHelper.findAndHookMethod("com.miui.powerkeeper.provider.PreSetApp", lpparam.getClassLoader(), "isPreSetApp", String.class, HookerClassHelper.returnConstant(false));
		ModuleHelper.hookAllMethods("com.miui.powerkeeper.utils.Utils", lpparam.getClassLoader(), "pkgHasIcon", HookerClassHelper.returnConstant(true));
	}

	public static void PersistBatteryOptimizationHook(PackageLoadedParam lpparam) {
		ModuleHelper.hookAllMethods("com.miui.powerkeeper.utils.CommonAdapter", lpparam.getClassLoader(), "addPowerSaveWhitelistApps", HookerClassHelper.DO_NOTHING);
		ModuleHelper.hookAllMethods("com.miui.powerkeeper.millet.MilletPolicy", lpparam.getClassLoader(), "dealSleepModeWhiteList", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				boolean addWhiteList = (boolean) param.getArgs()[1];
				if (addWhiteList) {
					param.returnAndSkip(null);
				}
			}
		});
		ModuleHelper.findAndHookMethod("com.miui.powerkeeper.statemachine.ForceDozeController", lpparam.getClassLoader(), "restoreWhiteListAppsIfQuitForceIdle", HookerClassHelper.DO_NOTHING);
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

	public static void AddSideBarExpandReceiverHook(PackageLoadedParam lpparam) {
		final boolean[] isHooked = {false, false};
		boolean enableSideBar = MainModule.mPrefs.getBoolean("various_swipe_expand_sidebar");
		if (!enableSideBar) {
			MainModule.resHooks.setDensityReplacement("com.miui.securitycenter", "dimen", "sidebar_height_default", 8);
			MainModule.resHooks.setDensityReplacement("com.miui.securitycenter", "dimen", "sidebar_height_vertical", 8);
		}
		Class <?> RegionSamplingHelper = findClassIfExists("com.android.systemui.navigationbar.gestural.RegionSamplingHelper", lpparam.getClassLoader());
		if (RegionSamplingHelper == null) {
			XposedHelpers.log("AddSideBarExpandReceiverHook", "failed to find RegionSamplingHelper");
		}
        ModuleHelper.hookAllConstructors(RegionSamplingHelper, new MethodHook() {
			private int originDockLocation = -1;
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				if (!isHooked[0]) {
					isHooked[0] = true;
					View view = (View) param.getArgs()[0];
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
					XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "showReceiver", showReceiver);

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
									ModuleHelper.findAndHookMethod(mOnTouchListener.getClass(), "onTouch", View.class, MotionEvent.class, new MethodHook() {
										@Override
										protected void before(final BeforeHookCallback param) throws Throwable {
											MotionEvent me = (MotionEvent) param.getArgs()[1];
											if (me.getSource() != 9999) {
												param.returnAndSkip(false);
											}
										}
									});
								}
								Class <?> bgDrawable = view.getBackground().getClass();
								ModuleHelper.findAndHookMethod(bgDrawable, "draw", Canvas.class, new MethodHook() {
									@Override
									protected void before(final BeforeHookCallback param) throws Throwable {
										param.returnAndSkip(null);
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
		ModuleHelper.findAndHookMethod(RegionSamplingHelper, "onViewDetachedFromWindow", android.view.View.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				isHooked[0] = false;
				BroadcastReceiver showReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "showReceiver");
				if (showReceiver != null) {
					View view = (View) param.getArgs()[0];
					view.getContext().unregisterReceiver(showReceiver);
					XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "showReceiver");
				}
			}
		});
		Method[] methods = XposedHelpers.findMethodsByExactParameters(RegionSamplingHelper, void.class, Rect.class);
		if (methods.length == 0) {
			XposedHelpers.log("AddSideBarExpandReceiverHook", "Cannot find appropriate start method");
			return;
		}
		ModuleHelper.hookMethod(methods[0], new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.returnAndSkip(null);
			}
		});
	}

	public static void InterceptPermHook(PackageLoadedParam lpparam) {
		Class<?> InterceptBaseFragmentClass = XposedHelpers.findClass("com.miui.permcenter.privacymanager.InterceptBaseFragment", lpparam.getClassLoader());
		Class<?>[] innerClasses = InterceptBaseFragmentClass.getDeclaredClasses();
		Class<?> HandlerClass = null;
		for (Class<?> innerClass : innerClasses) {
			if (Handler.class.isAssignableFrom(innerClass)) {
				HandlerClass = innerClass;
				break;
			}
		}
		if (HandlerClass != null) {
            ModuleHelper.hookAllConstructors(HandlerClass, new MethodHook() {
				@Override
				protected void before(final BeforeHookCallback param) throws Throwable {
					if (param.getArgs().length == 2) {
						param.getArgs()[1] = 0;
					}
				}
			});
			Method[] methods = XposedHelpers.findMethodsByExactParameters(HandlerClass, void.class, int.class);
			if (methods.length > 0) {
                ModuleHelper.hookMethod(methods[0], new MethodHook() {
					@Override
					protected void before(final BeforeHookCallback param) throws Throwable {
						param.getArgs()[0] = 0;
					}
				});
			}
		}
	}

	public static void PrivacyAppsLayoutHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.privacyapps.ui.PrivacyAppsActivity", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(AfterHookCallback param) throws Throwable {
				Activity act = (Activity) param.getThisObject();
				int gridViewId = act.getResources().getIdentifier("privacy_apps_gridview", "id", "com.miui.securitycenter");
				GridView gridView = act.findViewById(gridViewId);
				gridView.setNumColumns(4);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) gridView.getLayoutParams();
				params.rightMargin = (int) Helpers.dp2px(16);
				params.leftMargin = params.rightMargin;
				gridView.setLayoutParams(params);
			}
		});
	}

	public static void NoLowBatteryWarningHook() {
		MethodHook settingHook = new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				String key = (String)param.getArgs()[1];
				if ("low_battery_dialog_disabled".equals(key)) param.returnAndSkip(1);
				else if ("low_battery_sound".equals(key)) param.returnAndSkip(null);
			}
		};
		ModuleHelper.hookAllMethods(Settings.System.class, "getInt", settingHook);
		ModuleHelper.hookAllMethods(Settings.Global.class, "getString", settingHook);
	}

	public static void OpenByDefaultHook(PackageLoadedParam lpparam) {
		final int[] defaultViewId = {-1};
		ModuleHelper.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.getClassLoader(), "initView", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				if (defaultViewId[0] == -1) {
					Activity act = (Activity) param.getThisObject();
					defaultViewId[0] = act.getResources().getIdentifier("am_detail_default", "id", "com.miui.securitycenter");
					MainModule.resHooks.setResReplacement("com.miui.securitycenter", "string", "app_manager_default_open_title", R.string.various_open_by_default_title);
				}
			}
		});

		ModuleHelper.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.getClassLoader(), "onClick", View.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				View view = (View) param.getArgs()[0];
				if (view.getId() == defaultViewId[0] && defaultViewId[0] != -1) {
					Activity act = (Activity) param.getThisObject();
					Intent intent = new Intent("android.settings.APP_OPEN_BY_DEFAULT_SETTINGS");
					String pkgName = act.getIntent().getStringExtra("package_name");
					intent.setData(Uri.parse("package:".concat(pkgName)));
					act.startActivity(intent);
					param.returnAndSkip(null);
				}
			}
		});
	}
	public static void SkipSecurityScanHook(PackageLoadedParam lpparam) {
		MethodHook skipScan = new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.returnAndSkip(new ArrayList<>());
			}
		};
		ModuleHelper.findAndHookMethod("com.miui.securityscan.model.ModelFactory", lpparam.getClassLoader(), "produceSystemGroupModel", Context.class, skipScan);
		ModuleHelper.findAndHookMethod("com.miui.securityscan.model.ModelFactory", lpparam.getClassLoader(), "produceManualGroupModel", Context.class, skipScan);
		ModuleHelper.findAndHookMethod("com.miui.common.customview.ScoreTextView", lpparam.getClassLoader(), "setScore", int.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.getArgs()[0] = 100;
			}
		});
		ModuleHelper.findAndHookMethod(ContentResolver.class, "call", Uri.class, String.class, String.class, Bundle.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				if ("callPreference".equals(param.getArgs()[1]) && "GET".equals(param.getArgs()[2])) {
					Bundle extras = (Bundle) param.getArgs()[3];
					if (extras != null && "latest_optimize_date".equals(extras.getString("key"))) {
						Bundle res = new Bundle();
						res.putLong("latest_optimize_date", java.lang.System.currentTimeMillis() - 10000);
						param.returnAndSkip(res);
					}
				}
			}
		});
		ModuleHelper.findAndHookMethod("com.miui.securityscan.ui.main.MainContentFrame", lpparam.getClassLoader(), "onClick", View.class, HookerClassHelper.DO_NOTHING);
//		ModuleHelper.findAndHookMethod("com.miui.securityscan.ui.main.MainContentFrame", lpparam.getClassLoader(), "setActionButtonText", java.lang.String.class, new MethodHook() {
//			int btnId = 0;
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				View mainFrame = (View) param.getThisObject();
//				if (btnId == 0) {
//					btnId = mainFrame.getResources().getIdentifier("btn_action", "id", lpparam.getPackageName());
//				}
//				View btn = mainFrame.findViewById(btnId);
//				if (btn != null) {
//					btn.setVisibility(View.GONE);
//					btn.setOnClickListener(null);
//				}
//				XposedHelpers.callMethod(param.getThisObject(), "setContentMainClickable", false);
//			}
//		});
	}

	public static void SmartClipboardActionHook(PackageLoadedParam lpparam) {
		int opt = MainModule.mPrefs.getStringAsInt("various_clipboard_defaultaction", 1);
		if (opt == 3) {
			ModuleHelper.findAndHookMethod("com.lbe.security.ui.ClipboardTipDialog", lpparam.getClassLoader(), "customReadClipboardDialog", Context.class, String.class, HookerClassHelper.returnConstant(false));
		}
		else {
			ModuleHelper.findAndHookMethod("com.lbe.security.ui.ClipboardTipDialog", lpparam.getClassLoader(), "customReadClipboardDialog", Context.class, String.class, HookerClassHelper.returnConstant(true));

			Class<?> SecurityPromptHandler = findClass("com.lbe.security.ui.SecurityPromptHandler", lpparam.getClassLoader());
			ModuleHelper.hookAllMethods(SecurityPromptHandler, "handleNewRequest", new MethodHook() {
				@Override
				protected void before(final BeforeHookCallback param) throws Throwable {
					Object permissionRequest = param.getArgs()[0];
					long permId = (long) XposedHelpers.callMethod(permissionRequest, "getPermission");
					if (permId == 274877906944L) {
						XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "currentStopped", XposedHelpers.getBooleanField(param.getThisObject(), "mStopped"));
					}
				}
				@Override
				protected void after(final AfterHookCallback param) throws Throwable {
					Object permissionRequest = param.getArgs()[0];
					long permId = (long) XposedHelpers.callMethod(permissionRequest, "getPermission");
					if (permId == 274877906944L) {
						boolean mStopped = (boolean) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "currentStopped");
						if (mStopped) {
							XposedHelpers.callMethod(param.getThisObject(), "gotChoice", 3, true, true);
						}
						XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "currentStopped");
					}
				}
			});
		}
	}

	public static void ShowTempInBatteryHook(PackageLoadedParam lpparam) {
		Class<?> InterceptBaseFragmentClass = XposedHelpers.findClass("com.miui.powercenter.BatteryFragment", lpparam.getClassLoader());
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
			ModuleHelper.findAndHookMethod(HandlerClass, "handleMessage", Message.class, new MethodHook() {
				@Override
				protected void after(final AfterHookCallback param) throws Throwable {
					Message msg = (Message) param.getArgs()[0];
					int i = msg.what;
					if (i == 1) {
						Object wk = XposedHelpers.getObjectField(param.getThisObject(), finalFieldName);
						Object frag = XposedHelpers.callMethod(wk, "get");
						Activity batteryView = (Activity) XposedHelpers.callMethod(frag, "getActivity");
						int temp = batteryView.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("temperature", 0) / 10;
						int symbolResId = batteryView.getResources().getIdentifier("temp_symbol", "id", lpparam.getPackageName());
						int stateResId = batteryView.getResources().getIdentifier("current_temperature_state", "id", lpparam.getPackageName());
						TextView stateTv = batteryView.findViewById(stateResId);
						if (symbolResId > 0) {
							stateTv.setVisibility(View.GONE);
							TextView symbolTv = batteryView.findViewById(symbolResId);
							symbolTv.setVisibility(View.VISIBLE);
							int digitResId = batteryView.getResources().getIdentifier("current_temperature_value", "id", lpparam.getPackageName());
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
	public static void DisableDockSuggestHook(PackageLoadedParam lpparam) {
		MethodHook clearHook = new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
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
				param.returnAndSkip(blackList);
			}
		};
		ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowUtils", lpparam.getClassLoader(), "getFreeformSuggestionList", clearHook);
	}
	public static void UnlockClipboardAndLocationHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.permcenter.settings.PrivacyLabActivity", lpparam.getClassLoader(), "onCreateFragment", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				Class<?> utilCls = findClassIfExists("com.miui.permcenter.utils.h", lpparam.getClassLoader());
				if (utilCls != null) {
					Object fm = ModuleHelper.getStaticObjectFieldSilently(utilCls, "b");
					if (!ModuleHelper.NOT_EXIST_SYMBOL.equals(fm)) {
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
		ModuleHelper.findAndHookMethod(Settings.System.class, "getStringForUser", ContentResolver.class, String.class, int.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				String key = (String)param.getArgs()[1];
				if ("next_alarm_formatted".equals(key)) {
					param.getArgs()[1] = "next_alarm_clock_formatted";
				}
			}
		});
	}

	public static void AlarmCompatServiceHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.alarm.AlarmManagerService", lpparam.getClassLoader(), "onBootPhase", int.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				if ((int)param.getArgs()[0] != 500 /*PHASE_SYSTEM_SERVICES_READY*/) return;

				Context mContext = (Context)XposedHelpers.callMethod(param.getThisObject(), "getContext");
				if (mContext == null) {
					XposedHelpers.log("AlarmCompatServiceHook", "Context is NULL");
					return;
				}
				ContentResolver resolver = mContext.getContentResolver();
				ContentObserver alarmObserver = new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						if (selfChange) return;
						XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mNextAlarmTime", ModuleHelper.getNextMIUIAlarmTime(mContext));
					}
				};
				alarmObserver.onChange(false);
				resolver.registerContentObserver(Settings.System.getUriFor("next_alarm_clock_formatted"), false, alarmObserver);
			}
		});

		ModuleHelper.findAndHookMethod("com.android.server.alarm.AlarmManagerService", lpparam.getClassLoader(), "getNextAlarmClockImpl", int.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				Context mContext = (Context)XposedHelpers.callMethod(param.getThisObject(), "getContext");
				String pkgName = mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
				Object mNextAlarmTime = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mNextAlarmTime");
				if (mNextAlarmTime != null && MainModule.mPrefs.getStringSet("various_alarmcompat_apps").contains(pkgName))
					param.setResult((long)mNextAlarmTime == 0 ? null : new AlarmManager.AlarmClockInfo((long)mNextAlarmTime, null));
			}
		});
	}

	public static void AnswerCallInHeadUpHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.incallui.InCallPresenter", lpparam.getClassLoader(), "answerIncomingCall", Context.class, String.class, int.class, boolean.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				boolean showUi = (boolean) param.getArgs()[3];
				if (showUi) {
					ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
					if (foregroundInfo != null) {
						String topPackage = foregroundInfo.mForegroundPackageName;
						if (!"com.miui.home".equals(topPackage)) {
							param.getArgs()[3] = false;
						}
					}
				}
			}
		});
	}

	public static void ShowCallUIHook(PackageLoadedParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.incallui.InCallPresenter", lpparam.getClassLoader(), "startUi", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				if (!(boolean)param.getResult() || !"INCOMING".equals(param.getArgs()[0].toString())) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
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

				XposedHelpers.callMethod(param.getThisObject(), "showInCall", false, false);
				Object mStatusBarNotifier = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarNotifier");
				if (mStatusBarNotifier != null) XposedHelpers.callMethod(mStatusBarNotifier, "cancelInCall");
				param.setResult(true);
			}
		});
	}

	public static void InCallBrightnessHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.incallui.InCallActivity", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				Activity act = (Activity)param.getThisObject();

				int opt = MainModule.mPrefs.getStringAsInt("various_calluibright_type", 0);
				if (opt == 1 || opt == 2) {
					Object presenter = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.incallui.InCallPresenter", lpparam.getClassLoader()), "getInstance");
					if (presenter == null) {
						XposedHelpers.log("InCallBrightnessHook", "InCallPresenter is null");
						return;
					}

					String state = String.valueOf(XposedHelpers.callMethod(presenter, "getInCallState"));
					if (opt == 1 && !"INCOMING".equals(state)) return;
					else if (opt == 2 && !"OUTGOING".equals(state) && !"PENDING_OUTGOING".equals(state)) return;
				}

				String key = "various_calluibright_night";
				boolean checkNight = MainModule.mPrefs.getBoolean(key);
				if (checkNight) {
					int start_hour = MainModule.mPrefs.getInt(key + "_start_hour", 0);
					int start_minute = MainModule.mPrefs.getInt(key + "_start_minute", 0);
					int end_hour = MainModule.mPrefs.getInt(key + "_end_hour", 0);
					int end_minute = MainModule.mPrefs.getInt(key + "_end_minute", 0);

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
				int val = MainModule.mPrefs.getInt("various_calluibright_val", 0);
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

	public static void AppInfoDuringMiuiInstallHook(PackageLoadedParam lpparam) {
		Class<?> AppInfoViewObjectClass = findClassIfExists("com.miui.packageInstaller.ui.listcomponets.AppInfoViewObject", lpparam.getClassLoader());
		if (AppInfoViewObjectClass != null) {
			Class<?> ViewHolderClass = findClassIfExists("com.miui.packageInstaller.ui.listcomponets.AppInfoViewObject$ViewHolder", lpparam.getClassLoader());
			Method[] methods = XposedHelpers.findMethodsByExactParameters(AppInfoViewObjectClass, void.class, ViewHolderClass);
			if (methods.length == 0) {
				XposedHelpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate method");
				return;
			}
			Class<?> ApkInfoClass = findClassIfExists("com.miui.packageInstaller.model.ApkInfo", lpparam.getClassLoader());

			Field[] fields = AppInfoViewObjectClass.getDeclaredFields();
			String apkInfoFieldName = null;
			for (Field field: fields)
				if (ApkInfoClass.isAssignableFrom(field.getType())) {
					apkInfoFieldName = field.getName();
					break;
				}
			if (apkInfoFieldName == null) return;
			String finalApkInfoFieldName = apkInfoFieldName;
			ModuleHelper.hookMethod(methods[0], new MethodHook() {
				@Override
				protected void after(final AfterHookCallback param) throws Throwable {
					Object viewHolder = param.getArgs()[0];
					if (viewHolder == null) return;
					TextView tvAppVersion = (TextView) XposedHelpers.callMethod(viewHolder, "getTvDes");
					TextView tvAppSize = (TextView) XposedHelpers.callMethod(viewHolder, "getAppSize");
					TextView tvAppName = (TextView) XposedHelpers.callMethod(viewHolder, "getTvAppName");
					if (tvAppVersion == null) return;

					ViewGroup.MarginLayoutParams appNameLp = (ViewGroup.MarginLayoutParams) tvAppName.getLayoutParams();
					appNameLp.topMargin = 0;
					tvAppName.setLayoutParams(appNameLp);

					Object apkInfo = XposedHelpers.getObjectField(param.getThisObject(), finalApkInfoFieldName);
					ApplicationInfo mAppInfo = (ApplicationInfo) XposedHelpers.callMethod(apkInfo, "getInstalledPackageInfo");
					PackageInfo mPkgInfo = (PackageInfo) XposedHelpers.callMethod(apkInfo, "getPackageInfo");
					Resources modRes = ModuleHelper.getModuleRes(tvAppVersion.getContext());
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
			Class<?> InstallActivity = findClassIfExists("com.android.packageinstaller.PackageInstallerActivity", lpparam.getClassLoader());
			if (InstallActivity == null) {
				XposedHelpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate activity");
				return;
			}
			Method[] methods = XposedHelpers.findMethodsByExactParameters(InstallActivity, void.class, String.class);
			if (methods.length == 0) {
				XposedHelpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate method");
				return;
			}
			for (Method method: methods)
				ModuleHelper.hookMethod(method, new MethodHook() {
					@Override
					protected void after(final AfterHookCallback param) throws Throwable {
						Activity act = (Activity)param.getThisObject();
						TextView version = act.findViewById(act.getResources().getIdentifier("install_version", "id", lpparam.getPackageName()));
						Field fPkgInfo = XposedHelpers.findFirstFieldByExactType(param.getThisObject().getClass(), PackageInfo.class);
						PackageInfo mPkgInfo = (PackageInfo)fPkgInfo.get(param.getThisObject());
						if (version == null || mPkgInfo == null) return;

						TextView source = act.findViewById(act.getResources().getIdentifier("install_source", "id", lpparam.getPackageName()));
						source.setGravity(Gravity.CENTER_HORIZONTAL);
						source.setText(mPkgInfo.packageName);

						PackageInfo mAppInfo = null;
						try {
							mAppInfo = act.getPackageManager().getPackageInfo(mPkgInfo.packageName, 0);
						} catch (Throwable ignore) {}

						//String size = "";
						//String[] texts = version.getText().toString().split("\\|");
						//if (texts.length >= 2) size = texts[1].trim();

						Resources modRes = ModuleHelper.getModuleRes(act);

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

	public static void MiuiPackageInstallerHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("android.os.SystemProperties", lpparam.getClassLoader(), "getBoolean", String.class, boolean.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				if ("persist.sys.allow_sys_app_update".equals(param.getArgs()[0])) {
					param.returnAndSkip(true);
				}
			}
		});
		ModuleHelper.findAndHookMethodSilently("com.miui.packageInstaller.InstallStart", lpparam.getClassLoader(), "getCallingPackage", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.returnAndSkip("com.android.fileexplorer");
			}
		});
	}

	public static void GboardPaddingHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod(findClass("android.os.SystemProperties", lpparam.getClassLoader()), "get", String.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				String key = (String)param.getArgs()[0];
				if (key.equals("ro.com.google.ime.kb_pad_port_b")) {
					int opt = MainModule.mPrefs.getInt("various_gboardpadding_port", 0);
					if (opt > 0) param.returnAndSkip(String.valueOf(opt));
				} else if (key.equals("ro.com.google.ime.kb_pad_land_b")) {
					int opt = MainModule.mPrefs.getInt("various_gboardpadding_land", 0);
					if (opt > 0) param.returnAndSkip(String.valueOf(opt));
				}
			}
		});
	}

	public static void FixInputMethodBottomMarginHook(PackageLoadedParam lpparam) {
		Class<?> InputMethodServiceInjectorClass = findClassIfExists("android.inputmethodservice.InputMethodServiceInjector", lpparam.getClassLoader());
		ModuleHelper.hookAllMethods(InputMethodServiceInjectorClass, "addMiuiBottomView", new MethodHook() {
			private boolean isHooked = false;
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				if (isHooked) return;
				ClassLoader sClassLoader = (ClassLoader) XposedHelpers.getStaticObjectField(InputMethodServiceInjectorClass, "sClassLoader");
				if (sClassLoader != null) {
					isHooked = true;
					Class<?> InputMethodUtil = XposedHelpers.findClassIfExists("com.miui.inputmethod.InputMethodUtil", sClassLoader);
					XposedHelpers.setStaticBooleanField(InputMethodUtil, "sIsGestureLineEnable", false);
					ModuleHelper.findAndHookMethod(InputMethodUtil, "updateGestureLineEnable", Context.class, new MethodHook() {
						@Override
						protected void before(final BeforeHookCallback param) throws Throwable {
							XposedHelpers.setStaticBooleanField(InputMethodUtil, "sIsGestureLineEnable", false);
							param.returnAndSkip(null);
						}
					});
				}
			}
		});
	}

//	public static void LargeCallerPhotoHook(PackageLoadedParam lpparam) {
//		ModuleHelper.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.getClassLoader(), "setCallCardImage", Drawable.class, boolean.class, new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				param.getArgs()[1] = true;
//			}
//		});
//
//		ModuleHelper.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.getClassLoader(), "showBigAvatar", boolean.class, Drawable.class, new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				//XposedHelpers.log("showBigAvatar: " + String.valueOf(param.getArgs()[0]) + " | " + String.valueOf(param.getArgs()[1]));
//				if (param.getArgs()[1] == null)
//					param.returnAndSkip(null);
//				else
//					param.getArgs()[0] = true;
//			}
//		});
//	}

}