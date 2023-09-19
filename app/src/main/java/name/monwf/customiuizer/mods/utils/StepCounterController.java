package name.monwf.customiuizer.mods.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.ArrayList;

public class StepCounterController {
    private static ArrayList<TextView> stepViewList = new ArrayList<TextView>();
    private static Handler mHandler;
    private static Runnable updateStepsRunnable;
    private static String stepsWithGoal;

    public static void updateSteps(Context mContext) {
        if (stepViewList.size() == 0) return;
        Uri uri = Uri.parse("content://com.mi.health.provider.main/activity/steps/brief");
        try {
            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"steps","goal"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String stepCount = cursor.getString(0);
                String stepGoal = cursor.getString(1);
                cursor.close();
                String newText = stepCount + "/" + stepGoal;
                if (newText.equals(stepsWithGoal)) {
                    return;
                }
                stepsWithGoal = newText;
                for (TextView tv:stepViewList) {
                    tv.setText(newText);
                }
            }
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
    }

    public static void initContext(Context mContext) {
        BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, Intent intent) {
                updateSteps(context);
            }
        };
        mContext.registerReceiver(timeTickReceiver, new IntentFilter("android.intent.action.TIME_TICK"));
        mHandler = new Handler(Looper.myLooper());
        updateStepsRunnable = () -> {
            updateSteps(mContext);
        };
    }
    public static void removeStepViewByTag(String tag) {
        for (TextView tv:stepViewList) {
            if (tag.equals(tv.getTag())) {
                stepViewList.remove(tv);
                return;
            }
        }
    }
    public static void addStepView(TextView sv) {
        stepViewList.add(sv);
        if (mHandler.hasCallbacks(updateStepsRunnable)) {
            mHandler.removeCallbacks(updateStepsRunnable);
        }
        mHandler.postDelayed(updateStepsRunnable, 3000L);
    }
}
