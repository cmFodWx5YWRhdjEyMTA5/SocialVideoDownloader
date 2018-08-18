package com.v2social.socialdownloader;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                SplashActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
//                        startActivity(i);
//                        finish();
//                    }
//                });
//
//            }
//        }, 1000);

        Button start = findViewById(R.id.testButton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
