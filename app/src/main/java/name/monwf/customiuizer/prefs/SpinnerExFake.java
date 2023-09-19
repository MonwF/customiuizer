package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Pair;

import java.util.ArrayList;

import name.monwf.customiuizer.utils.AppHelper;

public class SpinnerExFake extends SpinnerEx {

	private String value = null;
	private final ArrayList<Pair<String, String>> others = new ArrayList<Pair<String, String>>();

	public SpinnerExFake(Context context, AttributeSet attrs) {
		super(context, attrs);
		others.clear();
	}

	public void setValue(String val) {
		value = val;
	}

	public String getValue() {
		return value;
	}

	public void addValue(String key, String val) {
		if (val == null) val = AppHelper.getStringOfAppPrefs(key, null);
		if (val != null) others.add(new Pair<String, String>(key, val));
	}

	public void addValue(String key, Intent val) {
		String sVal;
		if (val == null)
			sVal = AppHelper.getStringOfAppPrefs(key, null);
		else
			sVal = val.toUri(0);
		if (sVal != null) others.add(new Pair<String, String>(key, sVal));
	}

	public void applyOthers() {
		if (others.size() == 0) return;
		SharedPreferences.Editor editor = AppHelper.appPrefs.edit();
		for (Pair<String, String> pref: others) {
			editor.putString(pref.first, pref.second);
		}
		editor.apply();
	}

}
