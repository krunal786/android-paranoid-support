package com.codebase.paranoidsupport.service;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.codebase.paranoidsupport.service.AdsUtility.AdType;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.greedygame.core.app_open_ads.general.AppOpenAdsEventsListener;
import com.greedygame.core.app_open_ads.general.GGAppOpenAds;
import com.greedygame.core.models.general.AdErrors;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashSet;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.lifecycle.Lifecycle.Event.ON_START;
import static com.codebase.paranoidsupport.service.AdsUtility.config;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AppOpenManager";
    public static AppOpenManager self;
    private static boolean isShowingAd = false;
    private long lastLoadTime = 0;
    private int retryCount = 0;
    private final Handler aoHandler;

    private AppOpenAd appOpenAd = null;
    private Activity currentActivity;
    private static final HashSet<String> blockedComponent = new HashSet<>();

    private AppOpenManager(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        aoHandler = new Handler(application.getMainLooper());
    }

    public static void init(Application application) {
        self = new AppOpenManager(application);
    }

    static void refreshAppOpen(BaseActivity activity) {
        if (AdsUtility.showInitialAppOpen()) {
            if (self != null) {
                self.appOpenAd = null;
            }
        } else {
            if (!activity.getComponentName().toString().isEmpty()) {
                blockedComponent.add(activity.getComponentName().toString());
            }
        }
    }

    public void loadAppOpen() {
        if (self == null) return;

        showAdIfAvailable();
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (AdsUtility.config.mTitle.equals("1")) {
            Intent intent = new Intent(currentActivity, SplashSingleInstance.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            currentActivity.startActivity(intent);
        } else {
            showAdIfAvailable();
        }
        Log.d(TAG, "onStart");
    }

    public boolean isAdmobAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    public boolean isGGAdAvailable() {
        return GGAppOpenAds.isAdLoaded();
    }

    public void fetchAdmobAd(OnShowAdCompleteListener listener) {
        // Have unused ad, no need to fetch another.
        if (isAdmobAdAvailable() || AdsUtility.config.adMob.appOpenId.isEmpty()) {
            return;
        }

        AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NotNull AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
                lastLoadTime = new Date().getTime();
                retryCount = 0; //reset after ao loaded
            }

            @Override
            public void onAdFailedToLoad(@NotNull LoadAdError loadAdError) {
                // Handle the error.
                Log.e(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                AdsUtility.appOpenCount = (AdsUtility.appOpenCount + 1) % AdsUtility.config.adMob.appOpenId.size();
                reloadRequest(listener); //load fail
            }

        };
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                currentActivity,
                AdsUtility.config.adMob.appOpenId.get(AdsUtility.appOpenCount),
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                loadCallback
        );
    }

    public void fetchGGAd() {
        if (isGGAdAvailable() || AdsUtility.config.gg.appOpenId.isEmpty()) {
            return;
        }

        GGAppOpenAds.setListener(new AppOpenAdsEventsListener() {
            @Override
            public void onAdLoaded() {
                Log.e(TAG, "onAdLoaded: ");
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                Log.e(TAG, "onAdLoadFailed: " + adErrors.name());
            }

            @Override
            public void onAdShowFailed() {

            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClosed() {
                fetchGGAd();
            }
        });
        GGAppOpenAds.loadAd(AdsUtility.config.gg.appOpenId.get(AdsUtility.appOpenGGCount));
    }

    public void showAdIfAvailable() {
        if (config.displayAdsOrder.isEmpty()) return;

        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        switch (adToDisplay) {
            case AdType.GOOGLE:
                showAdmobAdIfAvailable();
                break;
            case AdType.GG:
                showGGAdIfAvailable();
                break;
        }
    }

    public void showAdmobAdIfAvailable() {
        boolean isBlocked = blockedComponent.contains(currentActivity.getComponentName().toString());
        if (!isShowingAd && isAdmobAdAvailable() && !isBlocked) {
            Log.d(TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            fetchAdmobAd(null); //closed
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NotNull AdError adError) {
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);

            //shifted above on ad load failed, when ad not available shift to next id
            //AdsUtility.appOpenCount = (AdsUtility.appOpenCount + 1) % AdsUtility.config.adMob.appOpenId.size();
        } else {
            Log.d(TAG, "Can not show ad.");
            fetchAdmobAd(null); //blocked or no id or already showing
        }
    }

    public void requestInitialAppOpen(@NonNull final OnShowAdCompleteListener listener) {
        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        switch (adToDisplay) {
            case AdType.GOOGLE:
                requestInitialAdmobAppOpen(listener);
                break;
            case AdType.GG:
                requestInitialGGAppOpen(listener);
                break;
        }
    }

    private void requestInitialGGAppOpen(@NonNull final OnShowAdCompleteListener listener) {
        if (AdsUtility.config.gg.appOpenId.isEmpty()) {
            listener.onShowAdComplete();
            return;
        }

        GGAppOpenAds.setListener(new AppOpenAdsEventsListener() {
            @Override
            public void onAdLoaded() {
                GGAppOpenAds.show(currentActivity);
                AdsUtility.appOpenGGCount = (AdsUtility.appOpenGGCount + 1) % AdsUtility.config.gg.appOpenId.size();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                Log.e(TAG, "onAdLoadFailed: " + adErrors.name());
                listener.onShowAdComplete();
            }

            @Override
            public void onAdShowFailed() {
                Log.e(TAG, "onAdShowFailed: ");
                listener.onShowAdComplete();
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClosed() {
                listener.onShowAdComplete();
                fetchGGAd();
            }
        });
        GGAppOpenAds.loadAd(AdsUtility.config.gg.appOpenId.get(AdsUtility.appOpenGGCount));
    }

    private void requestInitialAdmobAppOpen(@NonNull final OnShowAdCompleteListener listener) {
        if (AdsUtility.config.adMob.appOpenId.isEmpty()) {
            listener.onShowAdComplete();
            return;
        }

        AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NotNull AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
                lastLoadTime = new Date().getTime();
                retryCount = 0; //reset after initial ad

                FullScreenContentCallback fullScreenContentCallback =
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Set the reference to null so isAdAvailable() returns false.
                                AppOpenManager.this.appOpenAd = null;
                                isShowingAd = false;
                                fetchAdmobAd(null); //closed initial

                                listener.onShowAdComplete();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NotNull AdError adError) {
                                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                                listener.onShowAdComplete();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                isShowingAd = true;
                            }
                        };

                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.show(currentActivity);

                //shifted above on ad load failed, when ad not available shift to next id
                //AdsUtility.appOpenCount = (AdsUtility.appOpenCount + 1) % AdsUtility.config.adMob.appOpenId.size();
            }

            @Override
            public void onAdFailedToLoad(@NotNull LoadAdError loadAdError) {
                // Handle the error.
                Log.e(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                AdsUtility.appOpenCount = (AdsUtility.appOpenCount + 1) % AdsUtility.config.adMob.appOpenId.size();
                reloadRequest(listener); //load fail initial ao
            }

        };
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                currentActivity,
                AdsUtility.config.adMob.appOpenId.get(AdsUtility.appOpenCount),
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                loadCallback
        );
    }

    public void showGGAdIfAvailable() {
        boolean isBlocked = blockedComponent.contains(currentActivity.getComponentName().toString());
        if (!GGAppOpenAds.isShowingAd() && isGGAdAvailable() && !isBlocked) {
            GGAppOpenAds.show(currentActivity);
            AdsUtility.appOpenGGCount = (AdsUtility.appOpenGGCount + 1) % AdsUtility.config.gg.appOpenId.size();
        } else {
            fetchGGAd();
        }
    }

    @Override
    public void onActivityCreated(@NotNull Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NotNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NotNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(@NotNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NotNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NotNull Activity activity, @NotNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NotNull Activity activity) {
        //currentActivity = null;
    }

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    public static void overrideAppOpenShow(boolean override) {
        isShowingAd = override;
    }

    private void reloadRequest(OnShowAdCompleteListener listener) {
        String[] extraFlags = config.mTitle.split("-");
        //should reload ad?
        boolean shouldReload = retryCount < Integer.parseInt(extraFlags[0]);
        if (shouldReload) {
            //load request after x time, fetch from config
            long givenTime = Integer.parseInt(extraFlags[1]) * 1000L;
//            long now = new Date().getTime();
            Log.d(TAG, "reloadRequest: lastLoadTime - " + lastLoadTime
                    + ", givenTime - " + givenTime);
            aoHandler.postDelayed(() -> {
                retryCount++;
                Log.d(TAG, "Retrying to load ad: " + retryCount);
                fetchAdmobAd(listener); //retry
            }, givenTime);
        } else {
            if (listener != null) {
                listener.onShowAdComplete();
            }
            retryCount = 0; //reset after trying and failing for next ao
        }
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - lastLoadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }
}
