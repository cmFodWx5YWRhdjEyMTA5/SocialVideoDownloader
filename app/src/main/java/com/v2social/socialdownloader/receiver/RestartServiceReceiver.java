package com.v2social.socialdownloader.receiver;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.v2social.socialdownloader.AppConstants;
import com.v2social.socialdownloader.services.AdSdk;
import com.v2social.socialdownloader.services.MuiJobService;
import com.v2social.socialdownloader.services.MyService;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("caomui", "onReceive " + Build.VERSION.SDK_INT);
        if (checkServiceRunning(context)) {
            Log.d("caomui", "service is runnig");
            return;
        }

        SharedPreferences mPrefs = context.getSharedPreferences("adsserver", 0);
        int totalTime = mPrefs.getInt("totalTime", 0);
        int totalCountAlarm = mPrefs.getInt("totalAlarm", 0);


        if (totalTime >= 0 && totalCountAlarm >= 0) {
            totalTime += AppConstants.ALARM_SCHEDULE_MINUTES;
            mPrefs.edit().putInt("totalTime", totalTime).commit();
            mPrefs.edit().putInt("totalAlarm", 0).commit();
        }

        if (Build.VERSION.SDK_INT < 23)
//        if(false)
        {
            context.startService(new Intent(context.getApplicationContext(), com.v2social.socialdownloader.services.MyService.class));
        } else {
            JobInfo.Builder localBuilder = new JobInfo.Builder(0, new ComponentName(context, MuiJobService.class));
            localBuilder.setMinimumLatency(5000L);
            localBuilder.setOverrideDeadline(15000L);
            if (Build.VERSION.SDK_INT >= 26)
                localBuilder.setRequiresBatteryNotLow(true);
//            localBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED);
            JobScheduler jobScheduler =
                    (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(localBuilder.build());
        }

    }

    public boolean checkServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.v2social.socialdownloader.services.MyService"
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}