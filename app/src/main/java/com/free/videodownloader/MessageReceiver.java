package com.free.videodownloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by What's That Lambda on 11/6/17.
 */

public class MessageReceiver extends FirebaseMessagingService {
    private static final int REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 788899;

    public MessageReceiver() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences mPrefs = getSharedPreferences("support_yt", 0);
        if(mPrefs.getInt("support_yt",0) <= 1 )
        {
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putInt("accept", 2).commit();

            final String title = remoteMessage.getNotification().getTitle();//getData().get("title");
            final String message = remoteMessage.getNotification().getBody();//getData().get("body");
            showNotifications(title, message);
        }
    }

    private void showNotifications(String title, String msg) {
        Intent i = new Intent(this, Main2Activity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this,
                    "default1")
                    .setContentText(msg)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setContentText(msg)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .build();
        }
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}