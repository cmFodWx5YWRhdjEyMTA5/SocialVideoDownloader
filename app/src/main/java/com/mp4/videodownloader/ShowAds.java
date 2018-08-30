package com.mp4.videodownloader;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class ShowAds extends Activity {
    private static ShowAds instance;

    public static ShowAds getInstance() {
        return instance;
    }
    private  int countResume = 0;
    private ProgressDialog dialogLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fbinfo);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setTaskDescription(new ActivityManager.TaskDescription("", bitmap,
                        ContextCompat.getColor(getApplicationContext(), R.color.white)));

            dialogLoading = new ProgressDialog(this); // this = YourActivity
            dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialogLoading.setIndeterminate(true);
            dialogLoading.setCanceledOnTouchOutside(false);
            dialogLoading.show();
        }
        catch (Exception e)
        {

        }

        if (instance == null)
            instance = this;

    }

    @Override
    public void onResume(){
        super.onResume();
        countResume++;
        Log.d("cao","resume="+countResume);

        try {
            if(countResume >= 2)
            {
                if (Build.VERSION.SDK_INT < 21) {
                    finishAffinity();
                } else {
                    finishAndRemoveTask();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( dialogLoading!=null)
        {
            dialogLoading.cancel();
        }
    }

}
