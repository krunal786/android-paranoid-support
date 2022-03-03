package com.codebase.paranoidsupport.extras;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.utils.DebouncedOnClickListener;
import com.codebase.paranoidsupport.service.AdsUtility;
import com.codebase.paranoidsupport.service.BaseActivity;
import com.codebase.paranoidsupport.appcontent.DashboardActivity;
import com.codebase.paranoidsupport.appcontent.MainActivity;
import com.codebase.paranoidsupport.appcontent.ThankyouActivity;

public class StartOneActivity extends BaseActivity {
    TextView btn1;
    LinearLayout llQureka2, llQureka3;
    AppCompatImageView aivQureka1;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (AdsUtility.startScreenCount % 2 == 0) {
            setContentView((int) R.layout.ad_activity_extra_start_even);
        } else {
            setContentView((int) R.layout.ad_activity_extra_start_odd);
            bannerAd();
        }
        AdsUtility.startScreenCount++;
        nativeAd();

        aivQureka1 = findViewById(R.id.aivQureka1);
        llQureka2 = findViewById(R.id.llQureka2);
        llQureka3 = findViewById(R.id.llQureka3);

        if (!AdsUtility.config.qurekaButtons.isEmpty()) {
            aivQureka1.setVisibility(View.GONE);
            llQureka2.setVisibility(View.GONE);
            llQureka3.setVisibility(View.GONE);
            if (AdsUtility.config.qurekaButtons.contains("3")) {
                llQureka3.setVisibility(View.VISIBLE);
            }
            if (AdsUtility.config.qurekaButtons.contains("2")) {
                llQureka2.setVisibility(View.VISIBLE);
            }
            if (AdsUtility.config.qurekaButtons.contains("1")) {
                aivQureka1.setVisibility(View.VISIBLE);
            }
        } else {
            aivQureka1.setVisibility(View.GONE);
            llQureka2.setVisibility(View.GONE);
            llQureka3.setVisibility(View.GONE);
        }

        btn1 = findViewById(R.id.ad_call_to_action);
        btn1.setText(AdsUtility.config.startScreens.get(AdsUtility.startScreenCount - 1));

        DebouncedOnClickListener debouncedOnClickListener = new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                int id = v.getId();
                if (id == R.id.ad_call_to_action) {
                    if (AdsUtility.config.startScreenRepeatCount > AdsUtility.startScreenCount) {
                        showInterstitial(new Intent(StartOneActivity.this, StartOneActivity.class));
                    } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.DASHBOARD)) {
                        showInterstitial(new Intent(StartOneActivity.this, DashboardActivity.class));
                    } else {
                        showInterstitial(new Intent(StartOneActivity.this, MainActivity.class));
                    }
                } else if (id == R.id.llQureka2 || id == R.id.llQureka3 || id == R.id.aivQureka1) {
                    openQureka();
                }
            }
        };

        btn1.setOnClickListener(debouncedOnClickListener);
        aivQureka1.setOnClickListener(debouncedOnClickListener);
        llQureka2.setOnClickListener(debouncedOnClickListener);
        llQureka3.setOnClickListener(debouncedOnClickListener);
    }

    public void onBackPressed() {
        if (AdsUtility.startScreenCount != 1) {
            AdsUtility.startScreenCount--;
            if (AdsUtility.config.adOnBack) {
                showInterstitialWithCallback(StartOneActivity.super::onBackPressed);
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
            showInterstitial(new Intent(StartOneActivity.this, ThankyouActivity.class));
        } else {
            AdsUtility.startScreenCount = 0;
            AdsUtility.exitScreenCount = 0;
            finishAffinity();
        }
    }
}