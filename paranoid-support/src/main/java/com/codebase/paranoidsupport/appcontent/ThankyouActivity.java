package com.codebase.paranoidsupport.appcontent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.utils.DebouncedOnClickListener;
import com.codebase.paranoidsupport.extras.StartOneActivity;
import com.codebase.paranoidsupport.service.AdsUtility;
import com.codebase.paranoidsupport.service.BaseActivity;

public class ThankyouActivity extends BaseActivity {

    AppCompatTextView tvStay;
    AppCompatTextView tvQuit;
    AppCompatImageView aivQureka1;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView( R.layout.activity_thankyou);

        nativeAd();

        aivQureka1 = findViewById(R.id.aivQureka1);
        tvStay = findViewById(R.id.tvStay);
        tvQuit = findViewById(R.id.tvQuit);
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
                if (id == R.id.tvStay) {
                    if (AdsUtility.config.startScreenRepeatCount > AdsUtility.startScreenCount) {
                        AdsUtility.startScreenCount = 0;
                        AdsUtility.exitScreenCount = 0;
                        showInterstitial(new Intent(ThankyouActivity.this, StartOneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.DASHBOARD)) {
                        AdsUtility.startScreenCount = 0;
                        AdsUtility.exitScreenCount = 0;
                        showInterstitial(new Intent(ThankyouActivity.this, DashboardActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else {
                        AdsUtility.startScreenCount = 0;
                        AdsUtility.exitScreenCount = 0;
                        showInterstitial(new Intent(ThankyouActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                } else if (id == R.id.tvQuit) {
                    AdsUtility.startScreenCount = 0;
                    AdsUtility.exitScreenCount = 0;
                    finishAffinity();
                } else if (id == R.id.aivQureka1) {
                    openQureka();
                }
            }
        };

        tvQuit.setOnClickListener(debouncedOnClickListener);
        tvStay.setOnClickListener(debouncedOnClickListener);
        aivQureka1.setOnClickListener(debouncedOnClickListener);

    }

    public void onBackPressed() {
        AdsUtility.startScreenCount = 0;
        AdsUtility.exitScreenCount = 0;
        finishAffinity();
    }
}
