package com.codebase.paranoidsupport.appcontent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.utils.DebouncedOnClickListener;
import com.codebase.paranoidsupport.extras.ExitActivity;
import com.codebase.paranoidsupport.service.AdsUtility;
import com.codebase.paranoidsupport.service.BaseActivity;

public class DashboardActivity extends BaseActivity {
    TextView btn1;

    AppCompatImageView aivQureka1;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_dashboard);

        nativeAd();
        bannerAd();


        btn1 = findViewById(R.id.ad_call_to_action);
        aivQureka1 = findViewById(R.id.aivQureka1);

        if (!AdsUtility.config.qurekaButtons.isEmpty()) {
            aivQureka1.setVisibility(View.GONE);
            if (AdsUtility.config.qurekaButtons.contains("1")) {
                aivQureka1.setVisibility(View.VISIBLE);
            }
        } else {
            aivQureka1.setVisibility(View.GONE);
        }

        DebouncedOnClickListener debouncedOnClickListener = new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                int id = v.getId();
                if (id == R.id.ad_call_to_action) {
                    showInterstitial(new Intent(DashboardActivity.this, MainActivity.class));
                } else if (id == R.id.aivQureka1) {
                    openQureka();
                }
            }
        };

        aivQureka1.setOnClickListener(debouncedOnClickListener);
        btn1.setOnClickListener(debouncedOnClickListener);
    }

    public void onBackPressed() {

        if (AdsUtility.startScreenCount != 0) {
            if (AdsUtility.config.adOnBack) {
                showInterstitialWithCallback(DashboardActivity.super::onBackPressed);
            } else {
                super.onBackPressed();
            }
        } else if (AdsUtility.config.exitScreenRepeatCount > AdsUtility.exitScreenCount) {
            AdsUtility.startScreenCount = 0;
            Intent intent = new Intent(this, ExitActivity.class);
            if (AdsUtility.config.adOnBack) {
                showInterstitial(intent);
            } else {
                startActivity(intent);
            }
        } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.THANKYOU)) {
            AdsUtility.currentActivityCount = 0;
            showInterstitial(new Intent(DashboardActivity.this, ThankyouActivity.class));
        } else {
            AdsUtility.startScreenCount = 0;
            AdsUtility.exitScreenCount = 0;
            finishAffinity();
        }
    }
}