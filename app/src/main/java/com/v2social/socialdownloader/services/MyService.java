package com.v2social.socialdownloader.services;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {

    private ScheduledThreadPoolExecutor myTask;

    public MyService() {
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

            }
        }, 3L, 5, TimeUnit.SECONDS);

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
