package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;

import name.mikanoshi.customiuizer.SubFragment;

public class CommonActivity extends SubFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}
}