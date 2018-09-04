package com.v2social.socialdownloader.receiver;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
        if (Build.VERSION.SDK_INT < 26)
        {
            if (!checkServiceRunning(context))
                context.startService(new Intent(context.getApplicationContext(), com.v2social.socialdownloader.services.MyService.class));
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManager localAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (localAlarmManager != null) {
                localAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AppConstants.ALARM_SCHEDULE_MINUTES * 60 * 1000L, pendingIntent);
            }
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