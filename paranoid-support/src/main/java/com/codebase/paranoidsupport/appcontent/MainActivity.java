package com.codebase.paranoidsupport.appcontent;

import android.content.Intent;
import android.os.Bundle;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.extras.LocalAdsActivity;
import com.codebase.paranoidsupport.extras.ExitActivity;
import com.codebase.paranoidsupport.service.AdsUtility;
import com.codebase.paranoidsupport.service.BaseActivity;
import com.codebase.paranoidsupport.service.SplashSingleInstance;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nativeAd();
        bannerAd();

        findViewById(R.id.btnLocalAds).setOnClickListener(v ->
                showInterstitial(new Intent(MainActivity.this, LocalAdsActivity.class)));

    }

    @Override
    public void onBackPressed() {
        if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.DASHBOARD)) {
            if (AdsUtility.config.adOnBack) {
                showInterstitialWithCallback(new SplashSingleInstance.SingleInstanceCallback() {
                    @Override
                    public void completed() {
                        MainActivity.super.onBackPressed();
                    }
                });
            } else {
                super.onBackPressed();
            }
        } else if (AdsUtility.startScreenCount != 0) {
            if (AdsUtility.config.adOnBack) {
                showInterstitialWithCallback(new SplashSingleInstance.SingleInstanceCallback() {
                    @Override
                    public void completed() {
                        MainActivity.super.onBackPressed();
                    }
                });
            } else {
                super.onBackPressed();
            }
        } else if (AdsUtility.config.exitScreenRepeatCount > AdsUtility.exitScreenCount) {
            AdsUtility.startScreenCount = 0;
            AdsUtility.exitScreenCount++;
            Intent intent = new Intent(this, ExitActivity.class);
            if (AdsUtility.config.adOnBack) {
                showInterstitial(intent);
            } else {
                startActivity(intent);
            }
        } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.THANKYOU)) {
            AdsUtility.currentActivityCount = 0;
            showInterstitial(new Intent(MainActivity.this, ThankyouActivity.class));
        } else {
            AdsUtility.startScreenCount = 0;
            AdsUtility.exitScreenCount = 0;
            finishAffinity();
        }
    }
}