package name.mikanoshi.customiuizer.utils;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import name.mikanoshi.customiuizer.ActivityEx;
import name.mikanoshi.customiuizer.R;

public class MaterialColorPicker extends AlertDialog {
	Activity mActivity = null;
	SharedPreferences prefs = null;
	Object[] colorValues = {};
	Object[] colorKeys = {};
	String selectedTheme = null;
	String prefKey = null;
	String themePrefix = "MaterialThemeAccent";
	
	public MaterialColorPicker(Activity act, String key, String prefix) {
		super(act);
		mActivity = act;
		prefKey = key;
		themePrefix = prefix;
		prefs = mActivity.getSharedPreferences("customiuizer_prefs", Context.MODE_PRIVATE);
		selectedTheme = prefs.getString(prefKey, null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (themePrefix.equals("MaterialThemeAccent")) {
			colorValues = Helpers.colorValues.values().toArray();
			colorKeys = Helpers.colorValues.keySet().toArray();
		} else if (themePrefix.equals("MaterialThemeHeader")) {
			colorValues = Helpers.colorValuesHeader.values().toArray();
			colorKeys = Helpers.colorValuesHeader.keySet().toArray();
		}
		
		final ListView listView = new ListView(this.getContext());
		listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		listView.setId(android.R.id.list);
		listView.setAdapter(new ColorsAdapter(this.getContext()));
		listView.setDividerHeight(0);
		listView.setFooterDividersEnabled(false);
		if (selectedTheme != null)
		listView.setSelection(Math.round(Arrays.asList(colorKeys).indexOf(selectedTheme.replace(themePrefix + "_", "")) / 4) - 1);
		
		this.setButton(DialogInterface.BUTTON_POSITIVE, Helpers.l10n(this.getContext(), R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		this.setCancelable(true);
		this.setView(listView);
		super.onCreate(savedInstanceState);
	}
	
	private class ColorButton extends Button {
		LayerDrawable selCircle;
		float density = 3f;
				
		private int densify(int dimension) {
			return Math.round(density * dimension);
		}
		
		public void setColor(int color) {
			((ShapeDrawable)selCircle.getDrawable(0)).getPaint().setColor(color);
			invalidate();
		}
		
		public void setKey(String key) {
			setTag(key);
			setChecked(key);
		}
		
		private void setChecked(String key) {
			//Log.e(null, selectedKey + " | " + themePrefix + "_" + key);
			if (selectedTheme != null && selectedTheme.equals(themePrefix + "_" + key))
				selCircle.getDrawable(1).setAlpha(255);
			else
				selCircle.getDrawable(1).setAlpha(0);
		}
		
		public ColorButton(Context mContext, String key, int color) {
			super(mContext);
			density = mContext.getResources().getDisplayMetrics().density;
			
			ShapeDrawable circle = new ShapeDrawable(new OvalShape());
			circle.getPaint().setColor(color);
			circle.getPaint().setStyle(Style.FILL);
			circle.getPaint().setAntiAlias(true);
			
			Drawable selection = mContext.getResources().getDrawable(R.drawable.theme_selected).mutate();
			selCircle = new LayerDrawable(new Drawable[] { circle, selection });
			
			setKey(key);
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(densify(60), densify(60));
			lp.setMargins(densify(5), densify(5), densify(5), densify(10));
			setLayoutParams(lp);
			setBackground(selCircle);
			setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
						((LayerDrawable)v.getBackground()).setColorFilter(new LightingColorFilter(Color.rgb(127, 127, 127), 0));
					else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
						((LayerDrawable)v.getBackground()).setColorFilter(null);
					return false;
				}
			});
			setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					prefs.edit().putString(prefKey, themePrefix + "_" + (String)v.getTag()).apply();
					if (MaterialColorPicker.this.isShowing()) MaterialColorPicker.this.dismiss();
					if (mActivity != null & !mActivity.isFinishing()) ((ActivityEx)mActivity).updateTheme(0);
				}
			});
		}
	}
	
	private class ViewHolder {
		ColorButton btn1 = null;
		ColorButton btn2 = null;
		ColorButton btn3 = null;
		ColorButton btn4 = null;
		TextView header = null;
	}
	
	private class ColorsAdapter extends BaseAdapter {
		float density = 3f;
		Context mContext = null;
		
		public ColorsAdapter(Context context) {
			mContext = context;
			density = mContext.getResources().getDisplayMetrics().density;
		}
		
		private int densify(int dimension) {
			return Math.round(density * dimension);
		}
		
		public int getCount() {
			return Math.round(colorValues.length / 4);
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public Object getItem(int position) {
			return colorKeys[position * 4];
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout row;
			if (convertView != null) {
				row = (LinearLayout)convertView;
				
				ViewHolder colorButtons = ((ViewHolder)row.getTag());
				colorButtons.btn1.setKey((String)colorKeys[position * 4]);
				colorButtons.btn1.setColor((Integer)colorValues[position * 4]);
				colorButtons.btn2.setKey((String)colorKeys[position * 4 + 1]);
				colorButtons.btn2.setColor((Integer)colorValues[position * 4 + 1]);
				colorButtons.btn3.setKey((String)colorKeys[position * 4 + 2]);
				colorButtons.btn3.setColor((Integer)colorValues[position * 4 + 2]);
				colorButtons.btn4.setKey((String)colorKeys[position * 4 + 3]);
				colorButtons.btn4.setColor((Integer)colorValues[position * 4 + 3]);
				
				if (position == 0) {
					colorButtons.header.setText("Sense");
					colorButtons.header.setVisibility(View.VISIBLE);
				} else if (position == 7) {
					colorButtons.header.setText("Material");
					colorButtons.header.setVisibility(View.VISIBLE);
				} else {
					colorButtons.header.setVisibility(View.GONE);
				}
			} else {
				row = new LinearLayout(mContext);
				row.setOrientation(LinearLayout.VERTICAL);
				LinearLayout insideRow = new LinearLayout(mContext);
				insideRow.setOrientation(LinearLayout.HORIZONTAL);
				
				ViewHolder colorButtons = new ViewHolder();
				colorButtons.btn1 = new ColorButton(mContext, (String)colorKeys[position * 4], (Integer)colorValues[position * 4]);
				colorButtons.btn2 = new ColorButton(mContext, (String)colorKeys[position * 4 + 1], (Integer)colorValues[position * 4 + 1]);
				colorButtons.btn3 = new ColorButton(mContext, (String)colorKeys[position * 4 + 2], (Integer)colorValues[position * 4 + 2]);
				colorButtons.btn4 = new ColorButton(mContext, (String)colorKeys[position * 4 + 3], (Integer)colorValues[position * 4 + 3]);
				
				TextView header = new TextView(mContext);
				if (position == 0)
					header.setText("Sense");
				else if (position == 7)
					header.setText("Material");
				else
					header.setVisibility(View.GONE);
				
				colorButtons.header = header;
				row.addView(header);
				
				if (colorButtons.header != null) {
					colorButtons.header.setPadding(densify(9), densify(5), densify(10), densify(10));
					colorButtons.header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					colorButtons.header.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					colorButtons.header.setGravity(Gravity.LEFT);
				}
				
				insideRow.addView(colorButtons.btn1);
				insideRow.addView(colorButtons.btn2);
				insideRow.addView(colorButtons.btn3);
				insideRow.addView(colorButtons.btn4);
				
				row.addView(insideRow);
				row.setTag(colorButtons);
			}
			
			if (position == 6)
				row.setPadding(densify(15), densify(5), densify(10), densify(20));
			else
				row.setPadding(densify(15), densify(5), densify(10), densify(0));
			
			return row;
		}
	}
}