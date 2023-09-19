package name.monwf.customiuizer.utils;

import android.view.View;
import android.widget.ListView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import com.miui.system.internal.R;

public class SortableListView extends ListView {
    private static final int ANIMATION_DURATION = 200;
    private static final float SCROLL_BOUND = 0.25f;
    private static final int SCROLL_SPEED_MAX = 16;
    private static final int SNAPSHOT_ALPHA = 153;
    private static final String TAG = "SortableListView";
    private int mDraggingFrom;
    private int mDraggingItemHeight;
    private int mDraggingItemWidth;
    private int mDraggingTo;
    private int mDraggingY;
    private boolean mInterceptTouchForSorting;
    private int mItemUpperBound;
    private int mOffsetYInDraggingItem;
    private OnOrderChangedListener mOnOrderChangedListener;
    private View.OnTouchListener mOnTouchListener;
    private int mScrollBound;
    private int mScrollLowerBound;
    private int mScrollUpperBound;
    private BitmapDrawable mSnapshot;
    private Drawable mSnapshotBackgroundForOverUpperBound;
    private Drawable mSnapshotShadow;
    private int mSnapshotShadowPaddingBottom;
    private int mSnapshotShadowPaddingTop;
    private int[] mTmpLocation;

    /* loaded from: classes.dex */
    public interface OnOrderChangedListener {
        void OnOrderChanged(int i, int i2);
    }

    public SortableListView(Context context) {
        this(context, null);
    }

    public SortableListView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842868);
    }

    public SortableListView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDraggingFrom = -1;
        this.mDraggingTo = -1;
        this.mItemUpperBound = -1;
        this.mTmpLocation = new int[2];
        Drawable drawable = context.getResources().getDrawable(R.drawable.sortable_list_dragging_item_shadow);
        this.mSnapshotShadow = drawable;
        drawable.setAlpha(SNAPSHOT_ALPHA);
        Rect rect = new Rect();
        this.mSnapshotShadow.getPadding(rect);
        this.mSnapshotShadowPaddingTop = rect.top;
        this.mSnapshotShadowPaddingBottom = rect.bottom;
        this.mOnTouchListener = new View.OnTouchListener() {
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int hittenItemPosition;
                if (SortableListView.this.mOnOrderChangedListener != null && (motionEvent.getAction() & 255) == 0 && (hittenItemPosition = SortableListView.this.getHittenItemPosition(motionEvent)) >= 0) {
                    SortableListView.this.mDraggingFrom = hittenItemPosition;
                    SortableListView.this.mDraggingTo = hittenItemPosition;
                    SortableListView.this.mInterceptTouchForSorting = true;
                    SortableListView sortableListView = SortableListView.this;
                    View childAt = sortableListView.getChildAt(hittenItemPosition - sortableListView.getFirstVisiblePosition());
                    SortableListView.this.mDraggingItemWidth = childAt.getWidth();
                    SortableListView.this.mDraggingItemHeight = childAt.getHeight();
                    SortableListView sortableListView2 = SortableListView.this;
                    sortableListView2.getLocationOnScreen(sortableListView2.mTmpLocation);
                    SortableListView.this.mDraggingY = ((int) motionEvent.getRawY()) - SortableListView.this.mTmpLocation[1];
                    SortableListView sortableListView3 = SortableListView.this;
                    sortableListView3.mOffsetYInDraggingItem = sortableListView3.mDraggingY - childAt.getTop();
                    Bitmap createBitmap = Bitmap.createBitmap(SortableListView.this.mDraggingItemWidth, SortableListView.this.mDraggingItemHeight, Bitmap.Config.ARGB_8888);
                    childAt.draw(new Canvas(createBitmap));
                    SortableListView.this.mSnapshot = new BitmapDrawable(SortableListView.this.getResources(), createBitmap);
                    SortableListView.this.mSnapshot.setAlpha(SortableListView.SNAPSHOT_ALPHA);
                    SortableListView.this.mSnapshot.setBounds(childAt.getLeft(), 0, childAt.getRight(), SortableListView.this.mDraggingItemHeight);
                    if (SortableListView.this.mSnapshotBackgroundForOverUpperBound != null) {
                        SortableListView.this.mSnapshotBackgroundForOverUpperBound.setAlpha(SortableListView.SNAPSHOT_ALPHA);
                        SortableListView.this.mSnapshotBackgroundForOverUpperBound.setBounds(childAt.getLeft(), 0, childAt.getRight(), SortableListView.this.mDraggingItemHeight);
                    }
                    SortableListView.this.mSnapshotShadow.setBounds(childAt.getLeft(), -SortableListView.this.mSnapshotShadowPaddingTop, childAt.getRight(), SortableListView.this.mDraggingItemHeight + SortableListView.this.mSnapshotShadowPaddingBottom);
                    SortableListView sortableListView4 = SortableListView.this;
                    childAt.startAnimation(sortableListView4.createAnimation(sortableListView4.mDraggingItemWidth, SortableListView.this.mDraggingItemWidth, 0, 0));
                }
                return SortableListView.this.mInterceptTouchForSorting;
            }
        };
    }

    public Animation createAnimation(int i, int i2, int i3, int i4) {
        TranslateAnimation translateAnimation = new TranslateAnimation(i, i2, i3, i4);
        translateAnimation.setDuration(200L);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    public int getHittenItemPosition(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int firstVisiblePosition = getFirstVisiblePosition();
        for (int lastVisiblePosition = getLastVisiblePosition(); lastVisiblePosition >= firstVisiblePosition; lastVisiblePosition--) {
            View childAt = getChildAt(lastVisiblePosition - firstVisiblePosition);
            if (childAt != null) {
                childAt.getLocationOnScreen(this.mTmpLocation);
                int[] iArr = this.mTmpLocation;
                if (iArr[0] <= rawX && iArr[0] + childAt.getWidth() >= rawX) {
                    int[] iArr2 = this.mTmpLocation;
                    if (iArr2[1] <= rawY && iArr2[1] + childAt.getHeight() >= rawY) {
                        return lastVisiblePosition;
                    }
                }
            }
        }
        return -1;
    }

    private void setViewAnimation(View view, Animation animation) {
        if (view == null) {
            return;
        }
        if (animation != null) {
            view.startAnimation(animation);
        } else {
            view.clearAnimation();
        }
    }

    private void setViewAnimationByPisition(int i, Animation animation) {
        setViewAnimation(getChildAt(i - getFirstVisiblePosition()), animation);
    }

    private void updateDraggingToPisition(int i) {
        if (i == this.mDraggingTo || i < 0) {
            return;
        }
        Log.d(TAG, "sort item from " + this.mDraggingFrom + " To " + i);
        if (this.mDraggingFrom < Math.max(this.mDraggingTo, i)) {
            while (true) {
                int i2 = this.mDraggingTo;
                if (i2 <= i || i2 <= this.mDraggingFrom) {
                    break;
                }
                Log.d(TAG, "item " + this.mDraggingTo + " set move down reverse animation");
                int i3 = this.mDraggingTo;
                this.mDraggingTo = i3 + (-1);
                setViewAnimationByPisition(i3, createAnimation(0, 0, -this.mDraggingItemHeight, 0));
            }
        }
        if (this.mDraggingFrom > Math.min(this.mDraggingTo, i)) {
            while (true) {
                int i4 = this.mDraggingTo;
                if (i4 >= i || i4 >= this.mDraggingFrom) {
                    break;
                }
                Log.d(TAG, "item " + this.mDraggingTo + " set move up reverse animation");
                int i5 = this.mDraggingTo;
                this.mDraggingTo = i5 + 1;
                setViewAnimationByPisition(i5, createAnimation(0, 0, this.mDraggingItemHeight, 0));
            }
        }
        if (this.mDraggingFrom < Math.max(this.mDraggingTo, i)) {
            while (true) {
                int i6 = this.mDraggingTo;
                if (i6 >= i) {
                    break;
                }
                int i7 = i6 + 1;
                this.mDraggingTo = i7;
                setViewAnimationByPisition(i7, createAnimation(0, 0, 0, -this.mDraggingItemHeight));
                Log.d(TAG, "item " + this.mDraggingTo + " set move up animation");
            }
        }
        if (this.mDraggingFrom <= Math.min(this.mDraggingTo, i)) {
            return;
        }
        while (true) {
            int i8 = this.mDraggingTo;
            if (i8 <= i) {
                return;
            }
            int i9 = i8 - 1;
            this.mDraggingTo = i9;
            setViewAnimationByPisition(i9, createAnimation(0, 0, 0, this.mDraggingItemHeight));
            Log.d(TAG, "item " + this.mDraggingTo + " set move down animation");
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mDraggingFrom >= 0) {
            int i = this.mDraggingY - this.mOffsetYInDraggingItem;
            int headerViewsCount = getHeaderViewsCount();
            if (headerViewsCount < getFirstVisiblePosition() || headerViewsCount > getLastVisiblePosition()) {
                headerViewsCount = getFirstVisiblePosition();
            }
            int max = Math.max(i, getChildAt(headerViewsCount - getFirstVisiblePosition()).getTop());
            int count = (getCount() - 1) - getFooterViewsCount();
            if (count < getFirstVisiblePosition() || count > getLastVisiblePosition()) {
                count = getLastVisiblePosition();
            }
            int min = Math.min(max, getChildAt(count - getFirstVisiblePosition()).getBottom() - this.mDraggingItemHeight);
            canvas.translate(0.0f, min);
            this.mSnapshotShadow.draw(canvas);
            this.mSnapshot.draw(canvas);
            Drawable drawable = this.mSnapshotBackgroundForOverUpperBound;
            if (drawable != null && this.mDraggingTo < this.mItemUpperBound) {
                drawable.draw(canvas);
            }
            canvas.translate(0.0f, -min);
        }
    }

    public View.OnTouchListener getListenerForStartingSort() {
        return this.mOnTouchListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mInterceptTouchForSorting) {
            requestDisallowInterceptTouchEvent(true);
            onTouchEvent(motionEvent);
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        int max = Math.max(1, (int) (i2 * SCROLL_BOUND));
        this.mScrollBound = max;
        this.mScrollUpperBound = max;
        this.mScrollLowerBound = i2 - max;
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int i;
        View childAt;
        if (this.mInterceptTouchForSorting) {
            switch (motionEvent.getAction() & 255) {
                case 1:
                case 3:
                case 5:
                    int i2 = this.mDraggingFrom;
                    if (i2 >= 0) {
                        OnOrderChangedListener onOrderChangedListener = this.mOnOrderChangedListener;
                        if (onOrderChangedListener == null || i2 == (i = this.mDraggingTo) || i < 0) {
                            setViewAnimationByPisition(i2, null);
                        } else {
                            onOrderChangedListener.OnOrderChanged(i2 - getHeaderViewsCount(), this.mDraggingTo - getHeaderViewsCount());
                        }
                    }
                    this.mInterceptTouchForSorting = false;
                    this.mDraggingFrom = -1;
                    this.mDraggingTo = -1;
                    invalidate();
                    return true;
                case 2:
                    int y = (int) motionEvent.getY();
                    if (this.mInterceptTouchForSorting || y != this.mDraggingY) {
                        int hittenItemPosition = getHittenItemPosition(motionEvent);
                        if (hittenItemPosition < getHeaderViewsCount() || hittenItemPosition > getCount() - getFooterViewsCount()) {
                            hittenItemPosition = this.mDraggingTo;
                        }
                        updateDraggingToPisition(hittenItemPosition);
                        this.mDraggingY = y;
                        invalidate();
                        int i3 = 0;
                        int i4 = this.mScrollLowerBound;
                        if (y > i4) {
                            i3 = ((i4 - y) * 16) / this.mScrollBound;
                        } else {
                            int i5 = this.mScrollUpperBound;
                            if (y < i5) {
                                i3 = ((i5 - y) * 16) / this.mScrollBound;
                            }
                        }
                        if (i3 == 0 || (childAt = getChildAt(hittenItemPosition - getFirstVisiblePosition())) == null) {
                            return true;
                        }
                        setSelectionFromTop(hittenItemPosition, childAt.getTop() + i3);
                        return true;
                    }
                    return true;
                case 4:
                default:
                    return true;
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public void setItemUpperBound(int i, Drawable drawable) {
        this.mItemUpperBound = i;
        this.mSnapshotBackgroundForOverUpperBound = drawable;
    }

    public void setOnOrderChangedListener(OnOrderChangedListener onOrderChangedListener) {
        this.mOnOrderChangedListener = onOrderChangedListener;
    }
}