package com.codebase.paranoidsupport.service;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.codebase.paranoidsupport.R;

public class SplashSingleInstance extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AdsUtility.refreshTokens(this);
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getWindow().setStatusBarColor(Window.DECOR_CAPTION_SHADE_AUTO);
        }
        setContentView(R.layout.activity_splash);

        AdsUtility.requestSingleInstanceInterstitial(this, this::finish);
    }

    @Override
    public void onBackPressed() {
    }

    public interface SingleInstanceCallback {
        void completed();
    }
}
