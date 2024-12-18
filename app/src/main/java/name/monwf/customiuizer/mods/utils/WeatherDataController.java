package name.monwf.customiuizer.mods.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

public class WeatherDataController {
    public static String weatherInfo = "";
    private static WeakReference weakReferenceContext;
    private static Runnable weakRefrenceRunnable;

    public static void refreshWeatherData(boolean forceRefresh) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = ((Context) weakReferenceContext.get()).getContentResolver().query(Uri.parse("content://weather/actualWeatherData/1"), null, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToNext();
                        String newWeather = "";
                        int cursorColumnIndex = cursor.getColumnIndex("description");
                        if (cursorColumnIndex >= 0) {
                            newWeather = cursor.getString(cursorColumnIndex);
                        }
                        cursorColumnIndex = cursor.getColumnIndex("temperature");
                        if (cursorColumnIndex >= 0) {
                            newWeather += (" " + cursor.getString(cursorColumnIndex));
                        }
                        weatherInfo = newWeather;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (forceRefresh) {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(weakRefrenceRunnable);
                        weakRefrenceRunnable = null;
                    }
                } catch (Throwable ign) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }).start();
    }

    public static void initContext(Context mContext, Runnable updateTimeRunnable) {
        weakReferenceContext = new WeakReference(mContext);
        weakRefrenceRunnable = updateTimeRunnable;
        BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, Intent intent) {
                refreshWeatherData(false);
            }
        };
        mContext.registerReceiver(timeTickReceiver, new IntentFilter("android.intent.action.TIME_TICK"));
        Handler mHandler = new Handler(Looper.myLooper());
        mHandler.postDelayed(() -> {
            refreshWeatherData(true);
        }, 1800);
    }
}
