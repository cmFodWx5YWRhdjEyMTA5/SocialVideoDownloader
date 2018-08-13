package com.mp4.videodownloader;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


public class ShowAds extends AppCompatActivity {
    private static ShowAds instance;

    public static ShowAds getInstance() {
        return instance;
    }
    private int countResume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Wellcome");
        try
        {

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.info);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setTaskDescription(new ActivityManager.TaskDescription("", bitmap,
                        ContextCompat.getColor(getApplicationContext(), R.color.white)));


            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        }
        catch (Exception e){}

        if (instance == null)
            instance = this;
    }

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        countResume++;
        if(countResume >= 2)
        {
            try {
                if (Build.VERSION.SDK_INT < 21) {
                    finishAffinity();
                } else {
                    finishAndRemoveTask();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
