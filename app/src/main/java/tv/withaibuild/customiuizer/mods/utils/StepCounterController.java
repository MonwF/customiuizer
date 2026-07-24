package tv.withaibuild.customiuizer.mods.utils;

import android.content.BroadcastReceiver;
import android.annotation.SuppressLint;
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
    private static BroadcastReceiver mTimeTickReceiver;
    private static Context mContext;

    public static void updateSteps(Context mContext) {
        if (stepViewList.size() == 0) return;
        Uri uri = Uri.parse("content://com.mi.health.provider.main/activity/steps/brief");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, new String[]{"steps","goal"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String stepCount = cursor.getString(0);
                String stepGoal = cursor.getString(1);
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
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public static void initContext(Context context) {
        if (mTimeTickReceiver != null && mContext != null) {
            try {
                mContext.unregisterReceiver(mTimeTickReceiver);
            } catch (Throwable ignore) {}
        }
        mContext = context;
        mTimeTickReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, Intent intent) {
                updateSteps(context);
            }
        };
        mContext.registerReceiver(mTimeTickReceiver, new IntentFilter("android.intent.action.TIME_TICK"), Context.RECEIVER_NOT_EXPORTED);
        mHandler = new Handler(mContext.getMainLooper());
        updateStepsRunnable = () -> updateSteps(mContext);
    }

    public static void removeStepViewByTag(String tag) {
        stepViewList.removeIf(tv -> tag.equals(tv.getTag()));
    }

    public static void addStepView(TextView sv) {
        stepViewList.add(sv);
        if (mHandler.hasCallbacks(updateStepsRunnable)) {
            mHandler.removeCallbacks(updateStepsRunnable);
        }
        mHandler.postDelayed(updateStepsRunnable, 3000L);
    }
}
