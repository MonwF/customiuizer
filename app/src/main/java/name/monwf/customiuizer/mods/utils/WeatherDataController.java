package name.monwf.customiuizer.mods.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherDataController {
    public static String weatherInfo = "";
    private static WeakReference<Context> weakReferenceContext;
    private static Runnable weakRefrenceRunnable;
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private static final Object sLock = new Object();
    private static BroadcastReceiver sTimeTickReceiver;
    private static Context sContext;

    private static void queryWeather() {
        Context ctx;
        synchronized (sLock) {
            ctx = weakReferenceContext != null ? weakReferenceContext.get() : null;
        }
        if (ctx == null) return;
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(
                Uri.parse("content://weather/actualWeatherData/1"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
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
        } catch (Throwable ignore) {
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public static void refreshWeatherData(boolean forceRefresh) {
        sExecutor.execute(() -> {
            queryWeather();
            if (forceRefresh) {
                Runnable r;
                synchronized (sLock) {
                    r = weakRefrenceRunnable;
                    weakRefrenceRunnable = null;
                }
                if (r != null) {
                    sHandler.post(r);
                }
            }
        });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public static void initContext(Context context, Runnable updateTimeRunnable) {
        synchronized (sLock) {
            weakReferenceContext = new WeakReference<>(context);
            weakRefrenceRunnable = updateTimeRunnable;
        }
        if (sTimeTickReceiver != null && sContext != null) {
            try {
                sContext.unregisterReceiver(sTimeTickReceiver);
            } catch (Throwable ignore) {}
        }
        sContext = context;
        sTimeTickReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                refreshWeatherData(false);
            }
        };
        sContext.registerReceiver(sTimeTickReceiver,
            new IntentFilter("android.intent.action.TIME_TICK"),
            Context.RECEIVER_NOT_EXPORTED);
        sHandler.postDelayed(() -> refreshWeatherData(true), 1800);
    }
}
