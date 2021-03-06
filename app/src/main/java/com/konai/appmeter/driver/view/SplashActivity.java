package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.konai.appmeter.driver.IntroActivity;
import com.konai.appmeter.driver.R;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.sendEmptyMessageDelayed(0, 2000);

    }//onCreate..

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
            Intent intent = new Intent(SplashActivity.this, IntroActivity.class);
            startActivity(intent);

            finish();
        }
    };
}