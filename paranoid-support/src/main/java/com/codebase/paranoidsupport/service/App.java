package com.codebase.paranoidsupport.service;

import android.app.Application;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;

import java.util.Map;

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(
                this,
                initializationStatus -> {
                    Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                    for (String adapterClass : statusMap.keySet()) {
                        AdapterStatus status = statusMap.get(adapterClass);
                        if (status != null) {
                            Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d",
                                    adapterClass, status.getDescription(), status.getLatency()));
                        }
                    }
                });

        AppStorage.init(this);
        AudienceNetworkAds.initialize(this);
        AppOpenManager.init(this);
        MultiDex.install(this);
    }
}
