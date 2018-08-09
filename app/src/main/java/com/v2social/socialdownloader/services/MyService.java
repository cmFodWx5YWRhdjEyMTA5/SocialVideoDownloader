package com.v2social.socialdownloader.services;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

import com.v2social.socialdownloader.AppConstants;
import com.v2social.socialdownloader.network.CheckAds;
import com.v2social.socialdownloader.network.GetConfig;
import com.v2social.socialdownloader.network.JsonConfig;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyService extends Service {
    public static boolean check = false;
    private ScheduledThreadPoolExecutor myTask;

    public MyService() {
//        MyBroadcast myBroadcast = new MyBroadcast();
//        IntentFilter filter = new IntentFilter("android.intent.action.USER_PRESENT");
//        registerReceiver(myBroadcast, filter);
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
                Log.d("caomui","------------------00000");
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(AppConstants.URL_CONFIG)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                GetConfig config = retrofit.create(GetConfig.class);
                Call<CheckAds> call = config.checkAds();
                call.enqueue(new Callback<CheckAds>() {
                    @Override
                    public void onResponse(Call<CheckAds> call, Response<CheckAds> response) {
                        Log.d("caomui",response.body().isShow + "=========");
                    }

                    @Override
                    public void onFailure(Call<CheckAds> call, Throwable t) {
                        Log.d("caomui","=========" +t.getMessage() +";"+t.getLocalizedMessage());
                    }
                });

//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    public void run() {
//
//                    }
//                });
            }
        }, 3L, 10, TimeUnit.SECONDS);

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

    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("caomui", "Unlock Screen");
//            check = true;
        }
    }
}
