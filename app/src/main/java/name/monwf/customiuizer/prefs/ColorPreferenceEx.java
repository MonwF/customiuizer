package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.preference.Preference;

import name.monwf.customiuizer.MainActivity;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.subs.ColorSelector;
import name.monwf.customiuizer.utils.AppHelper;

public class ColorPreferenceEx extends PreferenceEx {
    public ColorPreferenceEx(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Bundle args = new Bundle();
                args.putString("key", getKey());
                MainActivity act = (MainActivity) getContext();
                act.navToSubFragment(new ColorSelector(), args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.Edit, getTitle().toString(), R.layout.fragment_selectcolor);
                return true;
            }
        });
    }
}
