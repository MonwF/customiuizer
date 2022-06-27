package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.preference.Preference;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Various_CallReminder extends SubFragment {

	String uriStr = "";
	String key_sound = "pref_key_various_callreminder_sound";
	String key_vibration = "pref_key_various_callreminder_vibration";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PreferenceEx pref = (PreferenceEx)findPreference(key_sound);
		uriStr = Helpers.prefs.getString(key_sound, "");
		pref.setCustomSummary(TextUtils.isEmpty(uriStr) ? getResources().getString(R.string.various_callreminder_nosound) : getRingtoneName(Uri.parse(uriStr)));
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uriStr));
				startActivityForResult(intent, 0);
				return true;
			}
		});

		findPreference(key_vibration + "_own").setEnabled("7".equals(Helpers.prefs.getString(key_vibration, "0")));
		ListPreferenceEx pref2 = (ListPreferenceEx)findPreference(key_vibration);
		pref2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference(key_vibration + "_own").setEnabled("7".equals(newValue));
				Helpers.performCustomVibration(getContext(), Integer.parseInt((String)newValue), "");
				return true;
			}
		});

		findPreference(key_vibration + "_own").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(preference.getTitle());

				final TextView msg = new TextView(getActivity());
				msg.setText(R.string.various_callreminder_vibration_own_msg);
				msg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				msg.setGravity(Gravity.CENTER_HORIZONTAL);
				msg.setPadding(0, 0, 0, Math.round(15 * getResources().getDisplayMetrics().density));

				final EditText input = new EditText(getActivity());
				input.setId(android.R.id.edit);
				input.setInputType(InputType.TYPE_CLASS_NUMBER);
				input.setKeyListener(DigitsKeyListener.getInstance("0123456789,"));
				input.setText(Helpers.prefs.getString(key_vibration + "_own", ""));

				LinearLayout layout = new LinearLayout(getActivity());
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.addView(msg);
				layout.addView(input);
				builder.setView(layout);

				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});

				builder.setNeutralButton(R.string.system_batteryindicator_test_title, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});

				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				final AlertDialog dialog = builder.create();
				dialog.show();

				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						EditText edit = dialog.findViewById(android.R.id.edit);
						String patternStr = edit.getText().toString();
						Helpers.prefs.edit().putString(key_vibration + "_own", patternStr).apply();
						dialog.dismiss();
					}
				});

				dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (Helpers.isNougat()) return;
						EditText edit = dialog.findViewById(android.R.id.edit);
						Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
						try {
							vibrator.vibrate(VibrationEffect.createWaveform(Helpers.getVibrationPattern(edit.getText().toString()), -1));
						} catch (Throwable t) {
							//noinspection deprecation
							vibrator.vibrate(200);
						}
					}
				});

				return true;
			}
		});
	}

	String getRingtoneName(Uri uri) {
		try {
			Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
			String name = ringtone.getTitle(getActivity());
			name = name.substring(0, name.lastIndexOf("."));
			return name;
		} catch (Throwable t) {
			return "";
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == 0) {
			Uri sound = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			String title;
			if (sound != null) {
				title = getRingtoneName(sound);
				uriStr = sound.toString();
				Helpers.prefs.edit().putString(key_sound, sound.toString()).apply();
			} else {
				title = getResources().getString(R.string.various_callreminder_nosound);
				uriStr = "";
				Helpers.prefs.edit().putString(key_sound, "").apply();
			}
			((PreferenceEx)findPreference(key_sound)).setCustomSummary(title);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
