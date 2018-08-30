package com.v2social.socialdownloader.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.v2social.socialdownloader.services.MyService;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e("caomui", "onReceive");
        if (!checkServiceRunning(context))
            context.startService(new Intent(context.getApplicationContext(), com.v2social.socialdownloader.services.MyService.class));
//        Intent select = new Intent(context, MainActivity.class);
//        context.startActivity(select);
    }

    public boolean checkServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.v2social.socialdownloader.services.MyService"
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}