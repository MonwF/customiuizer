package name.monwf.customiuizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import io.github.libxposed.service.RemotePreferences;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;
import name.monwf.customiuizer.utils.AppHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }
        if (AppHelper.remotePrefs != null) return;

        final PendingResult pendingResult = goAsync();
        final XposedServiceHelper.OnServiceListener[] listenerHolder = new XposedServiceHelper.OnServiceListener[1];
        final boolean[] finished = { false };

        listenerHolder[0] = new XposedServiceHelper.OnServiceListener() {
            @Override
            public void onServiceBind(XposedService service) {
                AppHelper.remotePrefs = (RemotePreferences) service.getRemotePreferences(AppHelper.prefsName + "_remote");
                AppHelper.syncAppPrefsToRemote();
                if (!finished[0]) {
                    finished[0] = true;
                    pendingResult.finish();
                }
            }

            @Override
            public void onServiceDied(XposedService service) {
                if (!finished[0]) {
                    finished[0] = true;
                    pendingResult.finish();
                }
            }
        };

        XposedServiceHelper.registerListener(listenerHolder[0]);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!finished[0]) {
                    finished[0] = true;
                    pendingResult.finish();
                }
            }
        }, 8000);
    }
}
