package tv.withaibuild.customiuizer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ListView
import kotlin.math.max
import kotlin.math.min

class SortableListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle
) : ListView(context, attrs, defStyleAttr) {

    interface OnOrderChangedListener {
        fun OnOrderChanged(oldPos: Int, newPos: Int)
    }

    companion object {
        private const val ANIMATION_DURATION = 200L
        private const val SCROLL_BOUND_RATIO = 0.25f
        private const val SCROLL_SPEED_MAX = 16
        private const val SNAPSHOT_ALPHA = 153
    }

    private var draggingFrom = -1
    private var draggingTo = -1
    private var draggingItemHeight = 0
    private var draggingItemWidth = 0
    private var draggingY = 0
    private var interceptTouchForSorting = false
    private var itemUpperBound = -1
    private var offsetYInDraggingItem = 0
    private var onOrderChangedListener: OnOrderChangedListener? = null
    private var onTouchListenerForSorting: View.OnTouchListener

    private val snapshotShadow: Drawable = ColorDrawable()
    private var snapshot: BitmapDrawable? = null
    private var snapshotBackgroundForOverUpperBound: Drawable? = null
    private val tmpLocation = IntArray(2)
    private val shadowPadding = Rect()
    private var scrollBound = 0
    private var scrollUpperBound = 0
    private var scrollLowerBound = 0

    init {
        snapshotShadow.alpha = SNAPSHOT_ALPHA
        snapshotShadow.getPadding(shadowPadding)

        onTouchListenerForSorting = View.OnTouchListener { _, event ->
            if (event.actionMasked != MotionEvent.ACTION_DOWN) return@OnTouchListener interceptTouchForSorting
            val hitten = getHittenItemPosition(event)
            if (hitten >= 0) {
                draggingFrom = hitten
                draggingTo = hitten
                interceptTouchForSorting = true
                val child = getChildAt(hitten - firstVisiblePosition) ?: return@OnTouchListener interceptTouchForSorting
                draggingItemWidth = child.width
                draggingItemHeight = child.height
                getLocationOnScreen(tmpLocation)
                draggingY = (event.rawY - tmpLocation[1]).toInt()
                offsetYInDraggingItem = draggingY - child.top

                val bitmap = Bitmap.createBitmap(draggingItemWidth, draggingItemHeight, Bitmap.Config.ARGB_8888)
                child.draw(Canvas(bitmap))
                snapshot = BitmapDrawable(resources, bitmap).apply {
                    alpha = SNAPSHOT_ALPHA
                    setBounds(child.left, 0, child.right, draggingItemHeight)
                }
                snapshotBackgroundForOverUpperBound?.apply {
                    alpha = SNAPSHOT_ALPHA
                    setBounds(child.left, 0, child.right, draggingItemHeight)
                }
                snapshotShadow.setBounds(
                    child.left,
                    -shadowPadding.top,
                    child.right,
                    draggingItemHeight + shadowPadding.bottom
                )
                child.startAnimation(createAnimation(0, 0, 0, 0))
            }
            interceptTouchForSorting
        }
    }

    fun createAnimation(fromX: Int, toX: Int, fromY: Int, toY: Int): Animation {
        return TranslateAnimation(fromX.toFloat(), toX.toFloat(), fromY.toFloat(), toY.toFloat()).apply {
            duration = ANIMATION_DURATION
            fillAfter = true
        }
    }

    fun getHittenItemPosition(event: MotionEvent): Int {
        val rawX = event.rawX
        val rawY = event.rawY
        val first = firstVisiblePosition
        for (i in lastVisiblePosition downTo first) {
            val child = getChildAt(i - first) ?: continue
            child.getLocationOnScreen(tmpLocation)
            if (tmpLocation[0] <= rawX && tmpLocation[0] + child.width >= rawX &&
                tmpLocation[1] <= rawY && tmpLocation[1] + child.height >= rawY) {
                return i
            }
        }
        return -1
    }

    private fun setViewAnimation(view: View?, animation: Animation?) {
        if (view == null) return
        if (animation != null) view.startAnimation(animation) else view.clearAnimation()
    }

    private fun setViewAnimationByPosition(position: Int, animation: Animation?) {
        setViewAnimation(getChildAt(position - firstVisiblePosition), animation)
    }

    private fun updateDraggingToPosition(target: Int) {
        if (target == draggingTo || target < 0) return

        if (draggingFrom < max(draggingTo, target)) {
            while (true) {
                val dt = draggingTo
                if (dt <= target || dt <= draggingFrom) break
                draggingTo = dt - 1
                setViewAnimationByPosition(dt, createAnimation(0, 0, -draggingItemHeight, 0))
            }
        }
        if (draggingFrom > min(draggingTo, target)) {
            while (true) {
                val dt = draggingTo
                if (dt >= target || dt >= draggingFrom) break
                draggingTo = dt + 1
                setViewAnimationByPosition(dt, createAnimation(0, 0, draggingItemHeight, 0))
            }
        }
        if (draggingFrom < max(draggingTo, target)) {
            while (true) {
                val dt = draggingTo
                if (dt >= target) break
                draggingTo = dt + 1
                setViewAnimationByPosition(draggingTo, createAnimation(0, 0, 0, -draggingItemHeight))
            }
        }
        if (draggingFrom <= min(draggingTo, target)) return
        while (true) {
            val dt = draggingTo
            if (dt <= target) return
            draggingTo = dt - 1
            setViewAnimationByPosition(draggingTo, createAnimation(0, 0, 0, draggingItemHeight))
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (draggingFrom < 0) return

        val top = max(
            draggingY - offsetYInDraggingItem,
            getChildAt(headerViewsCount - firstVisiblePosition)?.top ?: return
        )
        val footerPos = (count - 1) - footerViewsCount
        val bottom = min(
            top,
            (getChildAt(footerPos - firstVisiblePosition)?.bottom ?: return) - draggingItemHeight
        )

        canvas.translate(0f, bottom.toFloat())
        snapshotShadow.draw(canvas)
        snapshot?.draw(canvas)
        snapshotBackgroundForOverUpperBound?.let {
            if (draggingTo < itemUpperBound) it.draw(canvas)
        }
        canvas.translate(0f, -bottom.toFloat())
    }

    fun getListenerForStartingSort(): View.OnTouchListener = onTouchListenerForSorting

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (interceptTouchForSorting) {
            requestDisallowInterceptTouchEvent(true)
            onTouchEvent(event)
            return true
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scrollBound = max(1, (h * SCROLL_BOUND_RATIO).toInt())
        scrollUpperBound = scrollBound
        scrollLowerBound = h - scrollBound
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        if (interceptTouchForSorting) {
            when (action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> {
                    val from = draggingFrom
                    val to = draggingTo
                    if (from >= 0) {
                        val listener = onOrderChangedListener
                        if (listener == null || from == to || to < 0) {
                            setViewAnimationByPosition(from, null)
                        } else {
                            listener.OnOrderChanged(from - headerViewsCount, to - headerViewsCount)
                        }
                    }
                    interceptTouchForSorting = false
                    draggingFrom = -1
                    draggingTo = -1
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val y = event.y.toInt()
                    if (interceptTouchForSorting || y != draggingY) {
                        var hitten = getHittenItemPosition(event)
                        val header = headerViewsCount
                        val footer = count - footerViewsCount
                        if (hitten < header || hitten > footer) hitten = draggingTo
                        updateDraggingToPosition(hitten)
                        draggingY = y
                        invalidate()

                        var scroll = 0
                        if (y > scrollLowerBound) {
                            scroll = ((scrollLowerBound - y) * SCROLL_SPEED_MAX) / scrollBound
                        } else if (y < scrollUpperBound) {
                            scroll = ((scrollUpperBound - y) * SCROLL_SPEED_MAX) / scrollBound
                        }
                        if (scroll == 0) return true
                        val child = getChildAt(hitten - firstVisiblePosition) ?: return true
                        setSelectionFromTop(hitten, child.top + scroll)
                        return true
                    }
                    return true
                }
                else -> return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setItemUpperBound(upperBound: Int, drawable: Drawable?) {
        itemUpperBound = upperBound
        snapshotBackgroundForOverUpperBound = drawable
    }

    fun setOnOrderChangedListener(listener: OnOrderChangedListener?) {
        onOrderChangedListener = listener
    }
}
