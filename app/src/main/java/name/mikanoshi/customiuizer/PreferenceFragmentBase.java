package name.mikanoshi.customiuizer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.utils.Helpers;

public class PreferenceFragmentBase extends PreferenceFragment {

	private Context actContext = null;
	public boolean isAnimating = false;
	public boolean supressMenu = false;
	public int animDur = 350;

	public static final int PICK_BACKFILE = 11;
	public boolean isCustomActionBar = false;

	protected ActionBar getActionBar() {
		AppCompatActivity act = (AppCompatActivity) getActivity();
		return act.getSupportActionBar();
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (supressMenu) {
			inflater.inflate(R.menu.menu_mods, menu);
		}
		if (isCustomActionBar) {
			MenuItem item = null;
			for (int i = 0; i < menu.size(); i++) {
				item = menu.getItem(i);
				item.setVisible(item.getItemId() == R.id.edit_confirm);
			}
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (isCustomActionBar) {
			MenuItem confirmMenu = menu.findItem(R.id.edit_confirm);
			confirmMenu.setTitle(null);
			int applyResId = getResources().getIdentifier(Helpers.isNightMode(getValidContext()) ? "action_mode_title_button_confirm_dark" : "action_mode_title_button_confirm_light", "drawable", "miui");
			if (applyResId == 0)
				applyResId = getResources().getIdentifier(Helpers.isNightMode(getValidContext()) ? "action_mode_immersion_done_dark" : "action_mode_immersion_done_light", "drawable", "miui");
			confirmMenu.setIcon(applyResId);
		}
		super.onPrepareOptionsMenu(menu);
	}

	public void confirmEdit() {}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Activity act = getActivity();
		switch (item.getItemId()) {
			case R.id.edit_confirm:
				confirmEdit();
				return true;
			case R.id.backuprestore:
				showBackupRestoreDialog();
				return true;
			case R.id.softreboot:
				if (!Helpers.miuizerModuleActive) {
					showXposedDialog(getActivity());
					return true;
				}

				AlertDialog.Builder alert = new AlertDialog.Builder(getValidContext());
				alert.setTitle(R.string.soft_reboot);
				alert.setMessage(R.string.soft_reboot_ask);
				alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						getValidContext().sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "FastReboot"));
					}
				});
				alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return true;
			case R.id.about:
				Bundle args = new Bundle();
				args.putInt("baseResId", R.layout.fragment_about);
				openSubFragment(new AboutFragment(), args, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.app_about, R.xml.prefs_about);
				return true;
		}
		return false;
	}

	public void showXposedDialog(Activity act) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle(R.string.warning);
			builder.setMessage(R.string.module_not_active);
			builder.setCancelable(true);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			AlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void showBackupRestoreDialog() {
		final Activity act = getActivity();

		AlertDialog.Builder alert = new AlertDialog.Builder(act);
		alert.setTitle(R.string.backup_restore);
		alert.setMessage(R.string.backup_restore_choose);
		alert.setPositiveButton(R.string.do_restore, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				restoreSettings(act);
			}
		});
		alert.setNegativeButton(R.string.do_backup, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				backupSettings(act);
			}
		});
		alert.show();
	}

	private void initFragment() {
		setHasOptionsMenu(supressMenu);

		boolean showBack = false;
		if (this instanceof MainFragment) {
			ActivityInfo appInfo;
			try {
				Activity act = getActivity();
				appInfo = act.getPackageManager().getActivityInfo(act.getComponentName(), PackageManager.GET_META_DATA);
				showBack = appInfo.metaData != null && appInfo.metaData.containsKey("from.settings");
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		} else showBack = !(this instanceof SnoozedFragment);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.app_name);
		actionBar.setDisplayHomeAsUpEnabled(showBack);
	}

	@SuppressLint("WorldReadableFiles")
	public void onCreate(Bundle savedInstanceState, int pref_defaults) {
		super.onCreate(savedInstanceState);
		try {
			getPreferenceManager().setSharedPreferencesName(Helpers.prefsName);
			getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
			getPreferenceManager().setStorageDeviceProtected();
			PreferenceManager.setDefaultValues(Helpers.getProtectedContext(getValidContext()), pref_defaults, false);
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
		setViewBackground(view);
	}

	public void setViewBackground(View view) {
		view.setBackgroundColor(Helpers.getSystemBackgroundColor(getValidContext()));
	}

	public void setActionModeStyle(View searchView) {
		boolean isNight = Helpers.isNightMode(getValidContext());
		if (searchView != null) try {
			searchView.setSaveFromParentEnabled(false);
			Drawable drawable = getResources().getDrawable(getResources().getIdentifier(isNight ? "search_mode_bg_dark" : "search_mode_bg_light", "drawable", "miui"), getValidContext().getTheme());
			try {
				int colorResId = getResources().getIdentifier(isNight ? "primary_color_dark" : "primary_color_light", "color", "miui");
				if (colorResId != 0 && drawable instanceof LayerDrawable) {
					drawable = ((LayerDrawable)drawable).getDrawable(0);
					if (drawable instanceof GradientDrawable)
					((GradientDrawable)drawable).setColor(getResources().getColor(colorResId, getValidContext().getTheme()));
				}
			} catch (Throwable ignore) {}
			searchView.setBackground(drawable);
			LinearLayout inputArea = searchView.findViewById(android.R.id.inputArea);
			inputArea.setBackgroundResource(getResources().getIdentifier(isNight ? "search_mode_edit_text_bg_dark" : "search_mode_edit_text_bg_light", "drawable", "miui"));
			if (Helpers.is11()) {
				ViewGroup.LayoutParams lp1 = searchView.getLayoutParams();
				int resId = getResources().getIdentifier("action_bar_default_height", "dimen", "miui");
				lp1.height = getResources().getDimensionPixelSize(resId == 0 ? R.dimen.secondary_text_size : resId);
				searchView.setLayoutParams(lp1);
				FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams)inputArea.getLayoutParams();
				resId = getResources().getIdentifier("searchbar_bg_height", "dimen", "miui");
				lp2.height = getResources().getDimensionPixelSize(resId == 0 ? R.dimen.searchbar_bg_height : resId);
				inputArea.setLayoutParams(lp2);
			}
			ImageView inputIcon = searchView.findViewById(R.id.inputIcon);
			inputIcon.setImageResource(getResources().getIdentifier(isNight ? "edit_text_search_dark" : "edit_text_search", "drawable", "miui"));
			TextView input = searchView.findViewById(android.R.id.input);
			int fontSize = getResources().getIdentifier(Helpers.is11() ? "edit_text_font_size" : "secondary_text_size", "dimen", "miui");
			input.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(fontSize == 0 ? R.dimen.secondary_text_size : fontSize));
			input.setHintTextColor(getResources().getColor(getResources().getIdentifier(isNight ? "edit_text_search_hint_color_dark" : "edit_text_search_hint_color_light", "color", "miui"), getValidContext().getTheme()));
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
		} catch (Throwable t) {}
		args.putFloat("order", order);
		if (fragment.getArguments() == null) {
			fragment.setArguments(args);
		} else {
			fragment.getArguments().clear();
			fragment.getArguments().putAll(args);
		}
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
		final View content = top.findViewById(android.R.id.list);

		//ValueAnimator.setFrameDelay(17);
		ValueAnimator valAnimator = new ValueAnimator();
		valAnimator.setDuration(animDur);
		valAnimator.setFloatValues(0.0f, 1.0f);
		valAnimator.setInterpolator(new DecelerateInterpolator(2.5f));

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
			public void onAnimationCancel(Animator animation) {}

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

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.actContext = context;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.actContext = null;
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		setupImmersiveMenu();
//	}

	public Context getValidContext() {
		if (actContext != null) return actContext;
		return getActivity() == null ? getContext() : getActivity().getApplicationContext();
	}

	public void backupSettings(Activity act) {
		String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Helpers.externalFolder;
		if (!Helpers.preparePathForBackup(act, backupPath)) return;
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(backupPath + Helpers.backupFile + new SimpleDateFormat("-MMddHHmmss").format(new java.util.Date())));
			output.writeObject(Helpers.prefs.getAll());

			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.do_backup);
			alert.setMessage(R.string.backup_ok);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		} catch (Throwable e) {
			e.printStackTrace();
			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.warning);
			alert.setMessage(getString(R.string.storage_cannot_backup) + "\n" + e.getMessage());
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
								 Intent resultData) {
		if (requestCode == MainFragment.PICK_BACKFILE
				&& resultCode == Activity.RESULT_OK) {
			Uri uri = null;
			if (resultData != null) {
				uri = resultData.getData();
				doRestoreSettings(uri);
			}
		}
	}

	public void restoreSettings(final Activity act) {
		if (!Helpers.checkStoragePerm(act, Helpers.REQUEST_PERMISSIONS_RESTORE)) return;
		if (!Helpers.checkStorageReadable(act)) return;
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/octet-stream");
		startActivityForResult(intent, PICK_BACKFILE);
	}

	public void doRestoreSettings(Uri uri) {
		ObjectInputStream input = null;
		final Activity act = getActivity();
		try {
			input = new ObjectInputStream(act.getContentResolver().openInputStream(uri));
			Map<String, ?> entries = (Map<String, ?>)input.readObject();
			if (entries == null || entries.isEmpty()) throw new RuntimeException("Cannot read entries");

			SharedPreferences.Editor prefEdit = Helpers.prefs.edit();
			prefEdit.clear();
			for (Map.Entry<String, ?> entry: entries.entrySet()) {
				Object val = entry.getValue();
				String key = entry.getKey();

				if (val instanceof Boolean)
					prefEdit.putBoolean(key, (Boolean)val);
				else if (val instanceof Float)
					prefEdit.putFloat(key, (Float)val);
				else if (val instanceof Integer)
					prefEdit.putInt(key, (Integer)val);
				else if (val instanceof Long)
					prefEdit.putLong(key, (Long)val);
				else if (val instanceof String)
					prefEdit.putString(key, ((String)val));
				else if (val instanceof Set<?>)
					prefEdit.putStringSet(key, ((Set<String>)val));
			}
			prefEdit.apply();

			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.do_restore);
			alert.setMessage(R.string.restore_ok);
			alert.setCancelable(false);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					act.finish();
					act.startActivity(act.getIntent());
				}
			});
			alert.show();
		} catch (Throwable t) {
			t.printStackTrace();
			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.warning);
			alert.setMessage(R.string.storage_cannot_restore);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		} finally {
			try {
				if (input != null) input.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}
}