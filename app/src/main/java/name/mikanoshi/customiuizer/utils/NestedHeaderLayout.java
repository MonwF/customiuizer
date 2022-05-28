package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import name.mikanoshi.customiuizer.R;

public class NestedHeaderLayout extends miui.widget.NestedScrollingLayout {

	private View mHeaderView;
	private float mRangeOffset;
	private View mScrollableSearchView;

	public NestedHeaderLayout(Context context) {
		this(context, null);
	}

	public NestedHeaderLayout(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public NestedHeaderLayout(Context context, AttributeSet attributeSet, int i) {
		super(context, attributeSet, i);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHeaderView = findViewById(R.id.searchView);
		mScrollableView = findViewById(android.R.id.list);
		mScrollableSearchView = findViewById(android.R.id.custom);
		mRangeOffset = 0.0f;
	}

	@Override
	protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
		super.onLayout(z, i, i2, i3, i4);
		if (mHeaderView != null) {
			setScrollingRange((int)((float)(-mHeaderView.getMeasuredHeight()) + mRangeOffset), 0);
		}
	}

	@Override
	protected void onScrollingProgressUpdated(int i) {
		super.onScrollingProgressUpdated(i);
		if (mHeaderView != null) {
			mHeaderView.offsetTopAndBottom(i - mHeaderView.getTop());
			mScrollableView.offsetTopAndBottom(mHeaderView.getMeasuredHeight() + i - mScrollableView.getTop());
			mScrollableSearchView.setTop(mHeaderView.getMeasuredHeight());
		}
	}
}
