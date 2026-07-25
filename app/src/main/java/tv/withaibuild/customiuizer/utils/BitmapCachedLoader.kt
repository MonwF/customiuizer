package tv.withaibuild.customiuizer.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Process
import android.util.Log
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class BitmapCachedLoader(
    target: ImageView,
    info: AppData,
    context: Context
) {
    private val targetRef = WeakReference(target)
    private val appInfo = WeakReference(info)
    private val ctx = context.applicationContext
    private val theTag = (target.tag as? Int) ?: -1

    fun execute() {
        loaderScope.launch(loaderDispatcher) {
            val bitmap = loadBitmap()
            if (bitmap != null) {
                withContext(Dispatchers.Main) { applyBitmap(bitmap) }
            }
        }
    }

    private fun loadBitmap(): Bitmap? {
        var icon: android.graphics.drawable.Drawable? = null
        var cacheKey: String? = null

        val ad = appInfo.get() ?: return null
        try {
            if ((ad.pkgName == null || ad.pkgName.isEmpty()) && (ad.actName == null || ad.actName.isEmpty())) return null

            val pkgMgr = ctx.packageManager
            if (ad.pkgName != null && ad.actName != null && ad.actName != "-") {
                val component = ComponentName(ad.pkgName, ad.actName)
                try {
                    if (pkgMgr.getActivityInfo(component, PackageManager.MATCH_ALL).icon != 0) {
                        icon = pkgMgr.getActivityIcon(component)
                    }
                } catch (ignored: PackageManager.NameNotFoundException) {
                }
            }
            if (icon == null && ad.pkgName != null
                && pkgMgr.getApplicationInfo(ad.pkgName, PackageManager.MATCH_DISABLED_COMPONENTS).icon != 0
            ) {
                icon = pkgMgr.getApplicationIcon(ad.pkgName)
            }

            if (ad.pkgName != null) cacheKey = ad.pkgName
            if (cacheKey != null && ad.actName != null) cacheKey += "|" + ad.actName
        } catch (t: Throwable) {
            Log.w(TAG, "Unable to load app icon", t)
        }
        if (icon == null) return null

        val newIconSize = ctx.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        val bmp = Bitmap.createBitmap(newIconSize, newIconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        icon.setBounds(0, 0, newIconSize, newIconSize)
        icon.draw(canvas)

        if (cacheKey != null) Helpers.memoryCache.put(cacheKey, bmp)

        return bmp
    }

    private fun applyBitmap(bmp: Bitmap) {
        val itemIcon = targetRef.get() ?: return
        val tag = itemIcon.tag
        if (tag is Int && theTag == tag && itemIcon.drawable is TransitionDrawable) {
            val crossfader = itemIcon.drawable as TransitionDrawable
            crossfader.addLayer(BitmapDrawable(ctx.resources, bmp))
            crossfader.startTransition(200)
        }
    }

    companion object {
        private const val TAG = "Pengeek.IconLoader"
        private const val MAX_PENDING_TASKS = 128

        private val threadCount =
            Runtime.getRuntime().availableProcessors() / 2
                .coerceAtLeast(2)
                .coerceAtMost(4)
        private val threadNumber = AtomicInteger()

        private val executor = ThreadPoolExecutor(
            threadCount, threadCount, 15L, TimeUnit.SECONDS,
            LinkedBlockingQueue(MAX_PENDING_TASKS),
            { runnable ->
                Thread({
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                    runnable.run()
                }, "Pengeek-IconLoader-${threadNumber.incrementAndGet()}")
            },
            ThreadPoolExecutor.DiscardOldestPolicy()
        ).apply { allowCoreThreadTimeOut(true) }

        private val loaderDispatcher = executor.asCoroutineDispatcher()
        private val loaderScope = CoroutineScope(SupervisorJob())
    }
}
