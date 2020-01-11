package name.mikanoshi.customiuizer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SnoozedFragment;

import static java.lang.System.currentTimeMillis;

public class SnoozedAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	public SnoozedAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return SnoozedFragment.snoozedList.size();
	}

	public SnoozeData getItem(int position) {
		return SnoozedFragment.snoozedList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean isEnabled(int position) {
		return !SnoozedFragment.snoozedList.get(position).header;
	}

	@SuppressLint("ClickableViewAccessibility")
	public View getView(final int position, View convertView, ViewGroup parent) {
		SnoozeData snoozed = getItem(position);

		View row;
		if (convertView != null && (boolean)convertView.getTag() == snoozed.header)
			row = convertView;
		else
			row = mInflater.inflate(Helpers.is11() ? (snoozed.header ? R.layout.pref_header11 : R.layout.pref_item11) : (snoozed.header ? R.layout.pref_header : R.layout.pref_item), parent, false);
		row.setTag(snoozed.header);

		TextView itemTitle = row.findViewById(android.R.id.title);

		if (snoozed.header) {
			itemTitle.setText(Helpers.getAppName(row.getContext(), snoozed.pkg, true));
			return row;
		}

		ImageView itemIcon = row.findViewById(android.R.id.icon);
		ImageView itemIcon2 = row.findViewById(R.id.drag_handle);
		TextView itemSummary = row.findViewById(android.R.id.summary);
		TextView itemText1 = row.findViewById(android.R.id.text1);

		Resources res = row.getResources();
		String text = "";
		if (snoozed.messages > 0)
		text += "\n" + res.getString(R.string.snooze_messages) + ": " + snoozed.messages;
		if (!"".equals(snoozed.channel))
		text += "\n" + res.getString(R.string.snooze_channel) + ": " + snoozed.channel;
		StringBuilder createdStr = new StringBuilder(DateUtils.getRelativeTimeSpanString(snoozed.created, currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
		createdStr.setCharAt(0, Character.toLowerCase(createdStr.charAt(0)));
		text += "\n" + res.getString(R.string.snooze_created) + " " + createdStr;
		if (snoozed.updated != snoozed.created) {
			StringBuilder updatedStr = new StringBuilder(DateUtils.getRelativeTimeSpanString(snoozed.updated, currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
			updatedStr.setCharAt(0, Character.toLowerCase(updatedStr.charAt(0)));
			text += "\n" + res.getString(R.string.snooze_updated) + " " + updatedStr;
		}
		if (snoozed.reposted > 0) {
			StringBuilder repostedStr = new StringBuilder(DateUtils.getRelativeTimeSpanString(snoozed.reposted, currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
			repostedStr.setCharAt(0, Character.toLowerCase(repostedStr.charAt(0)));
			text += "\n" + res.getString(R.string.snooze_repost) + " " + repostedStr;
		}

		itemTitle.setText(snoozed.title);
		if (snoozed.color != 0) itemTitle.setTextColor(snoozed.color);

		itemSummary.setSingleLine(true);
		itemSummary.setEllipsize(TextUtils.TruncateAt.END);
		itemSummary.setText(snoozed.text);

		itemText1.setVisibility(View.VISIBLE);
		itemText1.setText(text.trim());

		itemIcon.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)itemIcon.getLayoutParams();
		lp.gravity = Gravity.TOP;
		itemIcon.setLayoutParams(lp);
		try {
			itemIcon.setImageDrawable(snoozed.icon != null ? snoozed.icon.loadDrawable(row.getContext()) : row.getContext().getPackageManager().getApplicationIcon(snoozed.pkg));
		} catch (Throwable t) {
			t.printStackTrace();
		}

		itemIcon2.setVisibility(View.VISIBLE);
		if (snoozed.canceled)
			itemIcon2.setImageResource(R.drawable.am_card_item_disabled);
		else
			itemIcon2.setImageResource(0);

		row.setPadding(row.getPaddingLeft(), row.getPaddingTop(), 0, row.getPaddingBottom());
		return row;
	}

}