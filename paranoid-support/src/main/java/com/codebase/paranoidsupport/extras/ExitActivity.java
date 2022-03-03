package com.codebase.paranoidsupport.extras;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.utils.DebouncedOnClickListener;
import com.codebase.paranoidsupport.service.AdsUtility;
import com.codebase.paranoidsupport.service.BaseActivity;
import com.codebase.paranoidsupport.appcontent.ThankyouActivity;

public class ExitActivity extends BaseActivity {

    AppCompatTextView tvQuit;
    LinearLayout llQureka2, llQureka3;
    AppCompatImageView aivQureka1;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (AdsUtility.exitScreenCount % 2 == 0) {
            setContentView(R.layout.ad_activity_extra_exit_even);
            bannerAd();
        } else {
            setContentView(R.layout.ad_activity_extra_exit_odd);
        }
        AdsUtility.exitScreenCount++;
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

        tvQuit = findViewById(R.id.tvQuit);
        tvQuit.setText(AdsUtility.config.exitScreens.get(AdsUtility.exitScreenCount - 1));


        DebouncedOnClickListener debouncedOnClickListener = new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                int id = v.getId();
                if (id == R.id.tvQuit) {
                    if (AdsUtility.config.exitScreenRepeatCount > AdsUtility.exitScreenCount) {
                        AdsUtility.startScreenCount = 0;
                        Intent intent = new Intent(ExitActivity.this, ExitActivity.class);
                        if (AdsUtility.config.adOnBack) {
                            showInterstitial(intent);
                        } else {
                            startActivity(intent);
                        }
                    } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.THANKYOU)) {
                        AdsUtility.currentActivityCount = 0;
                        Intent intent = new Intent(ExitActivity.this, ThankyouActivity.class);
                        if (AdsUtility.config.adOnBack) {
                            showInterstitial(intent);
                        } else {
                            startActivity(intent);
                        }
                    } else {
                        AdsUtility.startScreenCount = 0;
                        AdsUtility.exitScreenCount = 0;
                        finishAffinity();
                    }
                } else if (id == R.id.aivQureka1 || id == R.id.llQureka2 || id == R.id.llQureka3) {
                    openQureka();
                }
            }
        };

        tvQuit.setOnClickListener(debouncedOnClickListener);
        aivQureka1.setOnClickListener(debouncedOnClickListener);
        llQureka2.setOnClickListener(debouncedOnClickListener);
        llQureka3.setOnClickListener(debouncedOnClickListener);
    }

    public void onBackPressed() {
        if (AdsUtility.config.exitScreenRepeatCount > AdsUtility.exitScreenCount) {
            Intent intent = new Intent(this, ExitActivity.class);
            if (AdsUtility.config.adOnBack) {
                showInterstitial(intent);
            } else {
                startActivity(intent);
            }
        } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.THANKYOU)) {
            AdsUtility.currentActivityCount = 0;
            Intent intent = new Intent(this, ThankyouActivity.class);
            if (AdsUtility.config.adOnBack) {
                showInterstitial(intent);
            } else {
                startActivity(intent);
            }
        } else {
            AdsUtility.startScreenCount = 0;
            AdsUtility.exitScreenCount = 0;
            finishAffinity();
        }
    }
}