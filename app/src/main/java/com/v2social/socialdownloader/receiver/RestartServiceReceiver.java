package com.v2social.socialdownloader.receiver;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.v2social.socialdownloader.services.MuiJobService;
import com.v2social.socialdownloader.services.MyService;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("caomui", "onReceive "+intent.getAction());
        if(checkServiceRunning(context))
            return;

        if(Build.VERSION.SDK_INT < 26)
        {
            context.startService(new Intent(context.getApplicationContext(), com.v2social.socialdownloader.services.MyService.class));
        }
        else
        {
            JobInfo.Builder localBuilder = new JobInfo.Builder(0, new ComponentName(context, MuiJobService.class));
            localBuilder.setMinimumLatency(10000L);
            localBuilder.setOverrideDeadline(15000L);
            localBuilder.setRequiresBatteryNotLow(true);
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