package name.mikanoshi.customiuizer.utils;

import name.mikanoshi.customiuizer.R;

public class ModData {

	public enum ModCat {
		pref_key_system,
		pref_key_launcher,
		pref_key_controls,
		pref_key_various
	}

	public String title;
	public String breadcrumbs;
	public String key;
	public ModCat cat;
	public int order;
}
