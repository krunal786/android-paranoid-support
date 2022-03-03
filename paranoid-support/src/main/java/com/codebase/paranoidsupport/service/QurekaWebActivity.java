package com.codebase.paranoidsupport.service;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.codebase.paranoidsupport.R;

import static com.codebase.paranoidsupport.service.AdsUtility.config;

public class QurekaWebActivity extends BaseActivity {

    private WebView webViewClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ad_activity_qureka_web);

        webViewClient = findViewById(R.id.webViewClient);
        WebSettings settings = webViewClient.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //webViewClient.setPadding(0, 0, 0, 0);
        webViewClient.setInitialScale(1);
        webViewClient.loadUrl(config.qurekaURL);
        showProgress();
        webViewClient.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                dismissProgress();
            }
        });
    }

    private Dialog loadingDialog;

    private void showProgress() {
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.ad_loading);
        loadingDialog.setCancelable(false);
        TextView loadingText = loadingDialog.findViewById(R.id.loadingText);
        String msg = "Please Wait...";
        loadingText.setText(msg);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
    }

    public void dismissProgress() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        webViewClient.stopLoading();
        webViewClient.removeAllViews();
        webViewClient.destroy();
        finish();
    }
}