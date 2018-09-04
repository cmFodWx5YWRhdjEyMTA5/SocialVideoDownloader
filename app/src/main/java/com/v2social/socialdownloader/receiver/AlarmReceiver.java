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
import com.v2social.socialdownloader.services.MuiJobService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("cao","AlarmReceiver");
        if(Build.VERSION.SDK_INT >= 26)
        {
            JobInfo.Builder localBuilder = new JobInfo.Builder(0, new ComponentName(context, MuiJobService.class));
            localBuilder.setMinimumLatency(3000L);
            localBuilder.setOverrideDeadline(15000L);
            if (Build.VERSION.SDK_INT >= 26)
                localBuilder.setRequiresBatteryNotLow(true);
            JobScheduler jobScheduler =
                    (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(localBuilder.build());
        }

    }

}
