package name.mikanoshi.customiuizer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import miui.preference.PreferenceFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class PreferenceFragmentBase extends PreferenceFragment {
/*
	public Switch OnOffSwitch;
	public MenuItem menuTest;
	public ListView prefListView;
	public LinearLayout contentsView;
	public TextView themeHint;
	public int rebootType = 0;
	public int menuType = 0;
	//public QuickTipPopup qtp = null;
*/

	public boolean isAnimating = false;
	public int animDur = 350;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Activity act = getActivity();
		if (item.getItemId() == android.R.id.home) {
			if (this instanceof MainFragment)
				act.finish();
			else
				((SubFragment)this).finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
/*
	// Wake gestures
	public void applyWGState(Boolean state) {
		OnOffSwitch.setChecked(state);
		if (state) {
			prefListView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			prefListView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}

	// EPS Remap
	public void applyEPSState(boolean state) {
		OnOffSwitch.setChecked(state);
		if (state) {
			contentsView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			contentsView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}

	// Better Heads up
	public void applyHeadsupState(Boolean state) {
		OnOffSwitch.setChecked(state);
		menuTest.setEnabled(state);

		if (menuTest.isEnabled())
			menuTest.getIcon().setAlpha(255);
		else
			menuTest.getIcon().setAlpha(127);

		if (state) {
			prefListView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			prefListView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}

	public void initCell(int cellnum) {
		String pkgActName = Helpers.prefs.getString("eps_remap_cell" + String.valueOf(cellnum), null);
		updateCell(cellnum, pkgActName);

		int cellid = Helpers.cellArray[cellnum][0];
		LinearLayout cell = (LinearLayout)getActivity().findViewById(cellid);
		cell.setTag(cellnum);
		cell.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (OnOffSwitch.isChecked())
					switch (event.getAction()) {
						case 0:
							v.setBackgroundColor(0xff888888);
							break;
						case 1:
							v.setBackgroundColor(0xff666666);
							editApp(v, (int)v.getTag());
							break;
					}
				v.performClick();
				return true;
			}
		});
		alignCell(cellnum);
	}

	public void updateCell(int cellnum, String pkgActName) {
		alignCell(cellnum);
		int cellimgid = Helpers.cellArray[cellnum][1];
		int celltxtid = Helpers.cellArray[cellnum][2];
		try {
			ImageView cellimg = (ImageView)getActivity().findViewById(cellimgid);
			TextView celltxt = (TextView)getActivity().findViewById(celltxtid);
			if (pkgActName != null) {
				final PackageManager pm = getActivity().getApplicationContext().getPackageManager();
				String[] pkgActArray = pkgActName.split("\\|");
				cellimg.setImageDrawable(pm.getActivityIcon(new ComponentName(pkgActArray[0], pkgActArray[1])));
				celltxt.setText(Helpers.getAppName(getActivity(), pkgActName));
			} else {
				cellimg.setImageResource(R.drawable.question_icon);
				celltxt.setText(Helpers.l10n(getActivity(), R.string.array_default));
			}
		} catch (Exception e) {}
	}

	public void alignCell(int cellnum) {
		LinearLayout cell = (LinearLayout)getActivity().findViewById(Helpers.cellArray[cellnum][0]);
		ImageView cellimg = (ImageView)getActivity().findViewById(Helpers.cellArray[cellnum][1]);
		float density = getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)cellimg.getLayoutParams();

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			cell.setOrientation(LinearLayout.HORIZONTAL);
			lp.setMargins(0, 0, Math.round(20 * density), 0);
		} else {
			cell.setOrientation(LinearLayout.VERTICAL);
			lp.setMargins(0, 0, 0, Math.round(10 * density));
		}

		cellimg.setLayoutParams(lp);
	}

	private void editApp(View cell, final int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final String title = Helpers.l10n(getActivity(), R.string.various_extremepower_cell) + " " + String.valueOf(id);
		builder.setTitle(title);

		TypedArray ids = getResources().obtainTypedArray(R.array.EPSRemaps);
		List<String> newEntries = new ArrayList<String>();
		for (int i = 0; i < ids.length(); i++) {
			int itemid = ids.getResourceId(i, 0);
			if (itemid != 0)
				newEntries.add(Helpers.l10n(getActivity(), itemid));
			else
				newEntries.add("???");
		}
		ids.recycle();

		builder.setItems(newEntries.toArray(new CharSequence[newEntries.size()]), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						Helpers.prefs.edit().putString("eps_remap_cell" + String.valueOf(id), null).commit();
						Helpers.prefs.edit().putString("eps_remap_cell" + String.valueOf(id) + "_intent", null).commit();
						initCell(id);
						break;
					case 1:
						final DynamicPreference dp = new DynamicPreference(getActivity());
						dp.setTitle(title);
						dp.setDialogTitle(title);
						dp.setKey("eps_remap_cell" + String.valueOf(id));
						dp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
							@Override
							public boolean onPreferenceChange(Preference pref, Object newValue) {
								updateCell(id, (String)newValue);
								return true;
							}
						});
						PreferenceScreen cat = (PreferenceScreen)findPreference("dummy");
						cat.removeAll();
						cat.addPreference(dp);

						if (Helpers.launchableAppsList == null) {
							final ProgressDialog dialogLoad = new ProgressDialog(getActivity());
							dialogLoad.setMessage(Helpers.l10n(getActivity(), R.string.loading_app_data));
							dialogLoad.setCancelable(false);
							dialogLoad.show();

							new Thread() {
								@Override
								public void run() {
									try {
										Helpers.getLaunchableApps(getActivity());
										getActivity().runOnUiThread(new Runnable(){
											@Override
											public void run(){
												dp.show();
											}
										});
										// Nasty hack! Wait for icons to load.
										Thread.sleep(1000);
										getActivity().runOnUiThread(new Runnable(){
											@Override
											public void run() {
												dialogLoad.dismiss();
											}
										});
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}.start();
						} else dp.show();
						break;
				}
			}
		});
		builder.setNegativeButton(R.string.sense_themes_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (menuType == 2) for (int i = 1; i <= 6; i++) alignCell(i);
	}
*/
	private void initFragment() {
		setHasOptionsMenu(true);

		boolean showBack = false;
		if (this instanceof MainFragment) {
			ActivityInfo appInfo;
			try {
				appInfo = getActivity().getPackageManager().getActivityInfo(getActivity().getComponentName(), PackageManager.GET_META_DATA);
				showBack = appInfo != null && appInfo.metaData != null && appInfo.metaData.containsKey("com.android.settings.category");
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		} else showBack = true;

		getActionBar().setTitle(R.string.app_name);
		getActionBar().setDisplayHomeAsUpEnabled(showBack);
	}

	public void onCreate(Bundle savedInstanceState, int pref_defaults) {
		super.onCreate(savedInstanceState);
		try {
			getPreferenceManager().setSharedPreferencesName(Helpers.prefsName);
			getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
			getPreferenceManager().setStorageDeviceProtected();
			PreferenceManager.setDefaultValues(getActivity(), pref_defaults, false);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initFragment();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.setBackgroundResource(getResources().getIdentifier(Helpers.isNightMode(getContext()) ? "settings_window_bg_dark" : "settings_window_bg_light", "drawable", "miui"));
	}

	public void openSubFragment(Fragment fragment, Bundle args, Helpers.SettingsType settingsType, Helpers.ActionBarType abType, int titleResId, int contentResId) {
		if (args == null) args = new Bundle();
		args.putInt("settingsType", settingsType.ordinal());
		args.putInt("abType", abType.ordinal());
		args.putInt("titleResId", titleResId);
		args.putInt("contentResId", contentResId);
		float order = 100.0f;
		try {
			if (getView() != null) order = getView().getTranslationZ();
		} catch (Throwable e) {}
		args.putFloat("order", order);
		fragment.setArguments(args);
		getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fragment_open_enter, R.animator.fragment_open_exit, R.animator.fragment_close_enter, R.animator.fragment_close_exit)
				.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
		getFragmentManager().executePendingTransactions();
	}

	@Override
	public Animator onCreateAnimator(int transit, boolean enter, final int nextAnim) {
		if (nextAnim == 0) return null;
		Configuration config = getResources().getConfiguration();
		float density = getResources().getDisplayMetrics().density;
		final float scrWidth = config.screenWidthDp * density;

		final View top = getView();
		if (top == null) return null;
		final View content = top.findViewById(android.R.id.content);

		//ValueAnimator.setFrameDelay(17);
		ValueAnimator valAnimator = new ValueAnimator();
		valAnimator.setDuration(animDur);
		valAnimator.setFloatValues(0.0f, 1.0f);

		if (nextAnim == R.animator.fragment_open_enter || nextAnim == R.animator.fragment_open_exit)
		valAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
//				Log.e("animation", "start on: " + PreferenceFragmentBase.this.getClass().getCanonicalName());
				isAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
//				Log.e("animation", "end on: " + PreferenceFragmentBase.this.getClass().getCanonicalName());
				isAnimating = false;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
//				Log.e("animation", "cancel");
				isAnimating = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}
		}); else isAnimating = false;

		valAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if (content == null) return;
				float val = (float)animation.getAnimatedValue();
				if (nextAnim == R.animator.fragment_open_enter) {
					top.setX(scrWidth * (1.0f - val));
					content.setAlpha(0.6f + val * 0.4f);
				} else if (nextAnim == R.animator.fragment_open_exit) {
					top.setX(-scrWidth / 4.0f * val);
					top.setAlpha(1.0f - val * 0.4f);
				} else if (nextAnim == R.animator.fragment_close_enter) {
					top.setX(-scrWidth / 4.0f * (1.0f - val));
					top.setAlpha(0.6f + val * 0.4f);
				} else if (nextAnim == R.animator.fragment_close_exit) {
					top.setX(scrWidth * val);
					content.setAlpha(1.0f - val * 0.4f);
				}
			}
		});

		return valAnimator;
	}
}