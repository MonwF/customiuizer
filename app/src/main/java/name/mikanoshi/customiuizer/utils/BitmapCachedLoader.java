package name.mikanoshi.customiuizer.utils;

import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import name.mikanoshi.customiuizer.R;

public class BitmapCachedLoader extends AsyncTask<Void, Void, Bitmap> {
	private final WeakReference<Object> targetRef;
	private final WeakReference<Object> appInfo;
	private final Context ctx;
	private int theTag = -1;
	
	public BitmapCachedLoader(Object target, Object info, Context context) {
		targetRef = new WeakReference<Object>(target);
		appInfo = new WeakReference<Object>(info);
		ctx = context;
		Object tag = ((ImageView)target).getTag();
		if (tag != null) theTag = (Integer)tag;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		Bitmap bmp = null;
		Drawable icon = null;
		String cacheKey = null;
		int newIconSize = ctx.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);

		AppData ad = ((AppData)appInfo.get());
		if (ad != null) try {
			if (ad.actName != null)
			icon = ctx.getPackageManager().getActivityIcon(new ComponentName(ad.pkgName, ad.actName));
			if (icon == null) icon = ctx.getPackageManager().getApplicationIcon(ad.pkgName);

			if (ad.pkgName != null) cacheKey = ad.pkgName;
			if (ad.actName != null) cacheKey += "|" + ad.actName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (cacheKey != null && icon != null && BitmapDrawable.class.isInstance(icon)) {
			bmp = ((BitmapDrawable)icon).getBitmap();
			Matrix matrix = new Matrix();
			matrix.postScale(((float)newIconSize) / bmp.getWidth(), ((float)newIconSize) / bmp.getHeight());
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			
			//Log.e("mem_left", String.valueOf(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()));
			if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > 8 * 1024 * 1024)
				Helpers.memoryCache.put(cacheKey, bmp);
			else
				Runtime.getRuntime().gc();
		}
		
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
