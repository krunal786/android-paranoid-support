package com.codebase.paranoidsupport.appcontent;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.extras.StartOneActivity;
import com.codebase.paranoidsupport.service.AdsUtility;
import com.codebase.paranoidsupport.service.BaseActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AdsUtility.refreshTokens(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    @Override
    protected void sdkInitialized() {
        super.sdkInitialized();
        callIntent();
    }

    @Override
    protected void sdkInitializationFailed() {
        super.sdkInitializationFailed();
        showSnack("Cannot connect to Server!");
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void showSnack(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.splashContainer), message, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction("Retry", view -> {
                    if (AdsUtility.isNetworkConnected(this)) {
                        showSnack("Cannot connect to Server!");
                    } else {
                        Toast.makeText(this, "Cannot connect to Internet!", Toast.LENGTH_SHORT).show();
                        showSnack("Please check your internet connection!");
                    }
                });
        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void callIntent() {
        if (AdsUtility.config.startScreenRepeatCount > AdsUtility.startScreenCount) {
            if (AdsUtility.showInitialAppOpen()) {
                startActivity(new Intent(this, StartOneActivity.class));
            } else {
                showInterstitial(new Intent(this, StartOneActivity.class));
            }
        } else if (AdsUtility.config.screenCount.contains(AdsUtility.ScreenType.DASHBOARD)) {
            if (AdsUtility.showInitialAppOpen()) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else {
                showInterstitial(new Intent(this, DashboardActivity.class));
            }
        } else {
            if (AdsUtility.showInitialAppOpen()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                showInterstitial(new Intent(SplashActivity.this, MainActivity.class));
            }
        }
    }
}
