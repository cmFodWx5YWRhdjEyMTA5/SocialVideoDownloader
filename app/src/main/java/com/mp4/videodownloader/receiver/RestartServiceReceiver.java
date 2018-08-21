package com.mp4.videodownloader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mp4.videodownloader.MainActivity;
import com.mp4.videodownloader.services.MyService;

public class RestartServiceReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("caomui", "onReceive");
//        context.startService(new Intent(context.getApplicationContext(), MyService.class));
        Intent select = new Intent(context, MainActivity.class);
        context.startActivity(select);
    }

}