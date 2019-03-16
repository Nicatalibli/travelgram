package com.example.nicat.travelgram.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.nicat.travelgram.R;

public class welcomescreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomescreen);
        hideactionbar();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(welcomescreen.this,loginpage.class);
                startActivity(homeIntent);
                finish();
            }
        },SPLASH_TIME_OUT);

    }

    private void hideactionbar() {
        //hideactionbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
        else {
        }
        View decorView = getWindow().getDecorView();
        int uiOptions =View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


    }

}
