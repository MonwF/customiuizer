package name.monwf.customiuizer.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BitmapCachedLoader {
	private static final String TAG = "Pengeek.IconLoader";
	private static final int MAX_PENDING_TASKS = 128;
	private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
	private static final ThreadPoolExecutor ICON_EXECUTOR = createExecutor();

	private final WeakReference<ImageView> targetRef;
	private final WeakReference<AppData> appInfo;
	private final Context ctx;
	private int theTag = -1;

	private static ThreadPoolExecutor createExecutor() {
		int threadCount = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
		AtomicInteger threadNumber = new AtomicInteger();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
			threadCount,
			threadCount,
			15,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(MAX_PENDING_TASKS),
			runnable -> new Thread(() -> {
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				runnable.run();
			}, "Pengeek-IconLoader-" + threadNumber.incrementAndGet()),
			new ThreadPoolExecutor.DiscardOldestPolicy()
		);
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}

	BitmapCachedLoader(ImageView target, AppData info, Context context) {
		targetRef = new WeakReference<>(target);
		appInfo = new WeakReference<>(info);
		ctx = context.getApplicationContext();
		Object tag = target.getTag();
		if (tag instanceof Integer) theTag = (Integer) tag;
	}

	void execute() {
		ICON_EXECUTOR.execute(() -> {
			Bitmap bitmap = loadBitmap();
			if (bitmap != null) MAIN_HANDLER.post(() -> applyBitmap(bitmap));
		});
	}

	private Bitmap loadBitmap() {
		Drawable icon = null;
		String cacheKey = null;

		AppData ad = appInfo.get();
		if (ad != null) try {
			if ((ad.pkgName == null || ad.pkgName.isEmpty()) && (ad.actName == null || ad.actName.isEmpty())) return null;
			PackageManager pkgMgr = ctx.getPackageManager();
			if (ad.pkgName != null && ad.actName != null && !ad.actName.equals("-")) {
				ComponentName component = new ComponentName(ad.pkgName, ad.actName);
				try {
					if (pkgMgr.getActivityInfo(component, PackageManager.MATCH_ALL).icon != 0) {
						icon = pkgMgr.getActivityIcon(component);
					}
				} catch (PackageManager.NameNotFoundException ignored) {}
			}
			if (icon == null && ad.pkgName != null
				&& pkgMgr.getApplicationInfo(ad.pkgName, PackageManager.MATCH_DISABLED_COMPONENTS).icon != 0) {
				icon = pkgMgr.getApplicationIcon(ad.pkgName);
			}

			if (ad.pkgName != null) cacheKey = ad.pkgName;
			if (cacheKey != null && ad.actName != null) cacheKey += "|" + ad.actName;
		} catch (Throwable t) {
			Log.w(TAG, "Unable to load app icon", t);
		}
		if (icon == null) return null;

		int newIconSize = ctx.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
		Bitmap bmp = Bitmap.createBitmap(newIconSize, newIconSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		icon.setBounds(0, 0, newIconSize, newIconSize);
		icon.draw(canvas);

		if (cacheKey != null) Helpers.memoryCache.put(cacheKey, bmp);

		return bmp;
	}

	private void applyBitmap(Bitmap bmp) {
		ImageView itemIcon = targetRef.get();
		if (itemIcon == null) return;
		Object tag = itemIcon.getTag();
		if (tag instanceof Integer && theTag == (Integer) tag
			&& itemIcon.getDrawable() instanceof TransitionDrawable) {
			TransitionDrawable crossfader = (TransitionDrawable) itemIcon.getDrawable();
			crossfader.addLayer(new BitmapDrawable(ctx.getResources(), bmp));
			crossfader.startTransition(200);
		}
	}
}
