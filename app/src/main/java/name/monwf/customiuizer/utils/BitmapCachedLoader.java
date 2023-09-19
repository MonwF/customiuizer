package name.monwf.customiuizer.utils;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

@SuppressLint("StaticFieldLeak")
public class BitmapCachedLoader extends AsyncTask<Void, Void, Bitmap> {
	private final WeakReference<Object> targetRef;
	private final WeakReference<Object> appInfo;
	private final Context ctx;
	private int theTag = -1;
	
	BitmapCachedLoader(Object target, Object info, Context context) {
		targetRef = new WeakReference<Object>(target);
		appInfo = new WeakReference<Object>(info);
		ctx = context.getApplicationContext();
		Object tag = ((ImageView)target).getTag();
		if (tag != null) theTag = (Integer)tag;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		Drawable icon = null;
		String cacheKey = null;

		AppData ad = ((AppData)appInfo.get());
		if (ad != null) try {
			if ((ad.pkgName == null || ad.pkgName.equals("")) && (ad.actName == null || ad.actName.equals(""))) return null;
			PackageManager pkgMgr = ctx.getPackageManager();
			if (ad.actName != null && !ad.actName.equals("-")) {
				ComponentName component = new ComponentName(ad.pkgName, ad.actName);
				if (pkgMgr.getActivityInfo(component, PackageManager.MATCH_ALL).icon != 0)
				icon = pkgMgr.getActivityIcon(component);
			}
			if (icon == null)
			if (pkgMgr.getApplicationInfo(ad.pkgName, PackageManager.MATCH_DISABLED_COMPONENTS).icon != 0)
			icon = pkgMgr.getApplicationIcon(ad.pkgName);

			if (ad.pkgName != null) cacheKey = ad.pkgName;
			if (ad.actName != null) cacheKey += "|" + ad.actName;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		if (icon == null) return null;

		int newIconSize = ctx.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
		Bitmap bmp = Bitmap.createBitmap(newIconSize, newIconSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		icon.setBounds(0, 0, newIconSize, newIconSize);
		icon.draw(canvas);

		//Log.e("mem_left", String.valueOf(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()));
		if (cacheKey != null)
		if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > 8 * 1024 * 1024)
			Helpers.memoryCache.put(cacheKey, bmp);
		else
			Runtime.getRuntime().gc();

		return bmp;
	}
	
	@Override
	protected void onPostExecute(Bitmap bmp) {
		if (targetRef != null && targetRef.get() != null && bmp != null) {
			Object tag = ((ImageView)targetRef.get()).getTag();
			if (tag != null && theTag == (Integer)tag) {
				ImageView itemIcon = ((ImageView)targetRef.get());
				if (itemIcon != null && itemIcon.getDrawable() instanceof TransitionDrawable) {
					TransitionDrawable crossfader = (TransitionDrawable)itemIcon.getDrawable();
					crossfader.addLayer(new BitmapDrawable(ctx.getResources(), bmp));
					crossfader.startTransition(200);
				}
			}
		}
		//Log.e("mem_used", String.valueOf(Helpers.memoryCache.size()) + " KB / " + String.valueOf(Runtime.getRuntime().totalMemory() / 1024) + " KB");
	}
}
