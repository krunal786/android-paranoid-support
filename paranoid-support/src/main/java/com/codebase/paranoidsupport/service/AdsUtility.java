package com.codebase.paranoidsupport.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.service.api.ListModel;
import com.codebase.paranoidsupport.service.api.LocalAdModel;
import com.codebase.paranoidsupport.service.api.ScreenDataModel;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.greedygame.core.adview.general.AdLoadCallback;
import com.greedygame.core.adview.general.GGAdview;
import com.greedygame.core.interstitial.general.GGInterstitialAd;
import com.greedygame.core.interstitial.general.GGInterstitialEventsListener;
import com.greedygame.core.models.general.AdErrors;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.codebase.paranoidsupport.service.SplashSingleInstance.SingleInstanceCallback;

@SuppressLint("InflateParams")
public class
AdsUtility {

    private static final String TAG = "AdsUtility";

    public static ListModel config = new ListModel();
    public static LiveData<List<LocalAdModel>> localAds = new MutableLiveData<>();
    public static int currentActivityCount = 0;

    protected static String priorAdType = "";
    protected static boolean needsLoading = true;
    protected static int adTypeCount = 0;
    public static int startScreenCount = 0;
    public static int exitScreenCount = 0;

    protected static int adBannerCount = 0;
    protected static int adInterstitialCount = 0;
    protected static int adNativeCount = 0;
    protected static int appOpenCount = 0;
    protected static int rewardedVideoCount = 0;

    protected static int adBannerGGCount = 0;
    protected static int adInterstitialGGCount = 0;
    protected static int adNativeGGCount = 0;
    protected static int appOpenGGCount = 0;
    protected static int rewardedVideoGGCount = 0;

    private static InterstitialAd interstitialAd;
    private static GGInterstitialAd interstitialAdGG;

    private static NativeAd nativeAd;

    protected static void requestBannerAd(final BaseActivity activity, ViewGroup adView) {
        if (AdsUtility.priorAdType.contains(AdType.GOOGLE)) {
            AdsUtility.requestAdMobBanner(activity, adView);
        } else if (AdsUtility.priorAdType.contains(AdType.GG)) {
            AdsUtility.requestGGBanner(activity, adView);
        }
    }

    private static void requestAdMobBanner(BaseActivity activity, ViewGroup adViewLayout) {
        if (config.adMob.bannerAd.isEmpty()) {
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdUnitId(config.adMob.bannerAd.get(adBannerCount));
        //adView.setAdSize(AdSize.BANNER);
        adView.setAdSize(getAdSize(activity));
        AdRequest.Builder builder = new AdRequest.Builder();
        adView.loadAd(builder.build());
        adViewLayout.addView(adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NotNull LoadAdError i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                //Toast.makeText(activity, "admob banner: " + adBannerCount, Toast.LENGTH_SHORT).show();
                adBannerCount = (adBannerCount + 1) % config.adMob.bannerAd.size();
            }
        });
    }

    private static AdSize getAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    protected static void requestGGBanner(final BaseActivity activity, ViewGroup adView) {
        if (config.gg.bannerAd.isEmpty()) return;

        GGAdview ggAd = new GGAdview(activity);
        ggAd.setUnitId(config.gg.bannerAd.get(adBannerGGCount));
        ggAd.setAdsMaxHeight(px(activity, com.intuit.sdp.R.dimen._50sdp));
        ggAd.loadAd(new AdLoadCallback() {
            @Override
            public void onAdLoaded() {
                adBannerGGCount = (adBannerGGCount + 1) % config.gg.bannerAd.size();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {

            }

            @Override
            public void onUiiOpened() {

            }

            @Override
            public void onUiiClosed() {

            }

            @Override
            public void onReadyForRefresh() {

            }
        });
        adView.addView(ggAd);
    }

    protected static void requestSmallNativeAd(final BaseActivity activity, ViewGroup adView) {
        if (AdsUtility.priorAdType.contains(AdType.GOOGLE)) {
            AdsUtility.requestAdMobBigNativeSmall(activity, adView);
        } else if (AdsUtility.priorAdType.contains(AdType.GG)) {
            AdsUtility.requestGGSmallNative(activity, adView);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void populateAdMobNativeAdSmall(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {

        MediaView mediaView = unifiedNativeAdView.findViewById(R.id.media_view);
        unifiedNativeAdView.setMediaView(mediaView);
        unifiedNativeAdView.setHeadlineView(unifiedNativeAdView.findViewById(R.id.primary));
//        unifiedNativeAdView.setBodyView(unifiedNativeAdView.findViewById(R.id.body));
        unifiedNativeAdView.setCallToActionView(unifiedNativeAdView.findViewById(R.id.cta));
        unifiedNativeAdView.setIconView(unifiedNativeAdView.findViewById(R.id.icon));
        unifiedNativeAdView.setStarRatingView(unifiedNativeAdView.findViewById(R.id.rating_bar));
//        unifiedNativeAdView.setAdvertiserView(unifiedNativeAdView.findViewById(R.id.secondary));

        ((TextView) unifiedNativeAdView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
//        ((TextView) unifiedNativeAdView.getBodyView()).setText(unifiedNativeAd.getBody());
        ((AppCompatButton) unifiedNativeAdView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());
        if (unifiedNativeAd.getIcon() == null) {
            unifiedNativeAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) unifiedNativeAdView.getIconView()).setImageDrawable(unifiedNativeAd.getIcon().getDrawable());
            unifiedNativeAdView.getIconView().setVisibility(View.VISIBLE);
        }
        if (unifiedNativeAd.getStarRating() == null) {
            unifiedNativeAdView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) unifiedNativeAdView.getStarRatingView()).setRating(unifiedNativeAd.getStarRating().floatValue());
            unifiedNativeAdView.getStarRatingView().setVisibility(View.VISIBLE);
        }
//        if (unifiedNativeAd.getAdvertiser() == null) {
//            unifiedNativeAdView.getAdvertiserView().setVisibility(View.GONE);
//        } else {
//            ((TextView) unifiedNativeAdView.getAdvertiserView()).setText(unifiedNativeAd.getAdvertiser());
//            unifiedNativeAdView.getAdvertiserView().setVisibility(View.VISIBLE);
//        }
        unifiedNativeAdView.setNativeAd(unifiedNativeAd);
    }

    protected static void requestAdMobBigNativeSmall(final BaseActivity activity, final ViewGroup adViewLayout) {
        if (config.adMob.nativeAd.isEmpty()) return;

        if (!config.flashingNative) {
            adViewLayout.getLayoutParams().height = px(activity, com.intuit.sdp.R.dimen._80sdp);
        }
        AdLoader.Builder builder = new AdLoader.Builder(activity, config.adMob.nativeAd.get(adNativeCount));
        builder.forNativeAd(unifiedNativeAd -> {
            if (activity.isFinishing()) {
                unifiedNativeAd.destroy();
                return;
            }

            if (config.flashingNative) {
                adViewLayout.getLayoutParams().height = px(activity, com.intuit.sdp.R.dimen._80sdp);
            }
            NativeAdView unifiedNativeAdView;
            if (config.customNative) {
                unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_native_small_custom, adViewLayout, false);
            } else {
                unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_native_small_original, adViewLayout, false);
            }

            populateAdMobNativeAdSmall(unifiedNativeAd, unifiedNativeAdView);
            adViewLayout.removeAllViews();
            adViewLayout.addView(unifiedNativeAdView);
        });
        builder.withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build()).build());
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NotNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "onAdFailedToLoad: " + loadAdError.toString());
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adNativeCount = (adNativeCount + 1) % config.adMob.nativeAd.size();
            }
        }).build().loadAd(new AdRequest.Builder().build());
    }

    protected static void requestNativeAd(final BaseActivity activity, ViewGroup adView) {
        if (config.preloadNative != null && config.preloadNative) {
            //preloading natives
            if (nativeAd != null) {
                requestAdMobBigNativePreload(activity, adView);
                return;
            }
        }

        if (AdsUtility.priorAdType.contains(AdType.GOOGLE)) {
            AdsUtility.requestAdMobBigNative(activity, adView);
        } else if (AdsUtility.priorAdType.contains(AdType.GG)) {
            AdsUtility.requestGGBigNative(activity, adView);
        }
    }

    @SuppressWarnings("ConstantConditions")
    static void populateAdMobNative(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {
        MediaView mediaView = unifiedNativeAdView.findViewById(R.id.media_view);
        unifiedNativeAdView.setMediaView(mediaView);
        unifiedNativeAdView.setHeadlineView(unifiedNativeAdView.findViewById(R.id.primary));
        unifiedNativeAdView.setBodyView(unifiedNativeAdView.findViewById(R.id.body));
        unifiedNativeAdView.setCallToActionView(unifiedNativeAdView.findViewById(R.id.cta));
        unifiedNativeAdView.setIconView(unifiedNativeAdView.findViewById(R.id.icon));
        unifiedNativeAdView.setStarRatingView(unifiedNativeAdView.findViewById(R.id.rating_bar));
        unifiedNativeAdView.setAdvertiserView(unifiedNativeAdView.findViewById(R.id.secondary));

        ((TextView) unifiedNativeAdView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
        ((TextView) unifiedNativeAdView.getBodyView()).setText(unifiedNativeAd.getBody());
        ((AppCompatButton) unifiedNativeAdView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());
        if (unifiedNativeAd.getIcon() == null) {
            unifiedNativeAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) unifiedNativeAdView.getIconView()).setImageDrawable(unifiedNativeAd.getIcon().getDrawable());
            unifiedNativeAdView.getIconView().setVisibility(View.VISIBLE);
        }
        if (unifiedNativeAd.getStarRating() == null) {
            unifiedNativeAdView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) unifiedNativeAdView.getStarRatingView()).setRating(unifiedNativeAd.getStarRating().floatValue());
            unifiedNativeAdView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (unifiedNativeAd.getAdvertiser() == null) {
            unifiedNativeAdView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) unifiedNativeAdView.getAdvertiserView()).setText(unifiedNativeAd.getAdvertiser());
            unifiedNativeAdView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        unifiedNativeAdView.setNativeAd(unifiedNativeAd);
    }

    public static void loadAdMobBigNativePreload(final BaseActivity activity) {
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        AdLoader adLoader = new AdLoader.Builder(activity, config.adMob.nativeAd.get(adNativeCount))
                .forNativeAd(unifiedNativeAd -> {
                    nativeAd = unifiedNativeAd;
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NotNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e(TAG, "onAdFailedToLoad: " + loadAdError.toString());

                        adNativeCount = (adNativeCount + 1) % config.adMob.nativeAd.size();
                        loadAdMobBigNativePreload(activity);    //preload failed
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();


                    }
                }).withNativeAdOptions(adOptions).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private static void requestAdMobBigNativePreload(final BaseActivity activity, final ViewGroup nativeAdLayout) {
        NativeAdView unifiedNativeAdView;
        if (config.customNative) {
            unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_native_big_custom, nativeAdLayout, false);
        } else {
            unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_native_big_original, nativeAdLayout, false);
        }

        //flashing does not support in preloading natives
        long flashDelay = config.flashingNative ? 700L : 0L;
        Handler handler = new Handler(activity.getMainLooper());
        handler.postDelayed(() -> {
            double res = (getDeviceHeight(activity) / 100.0f) * config.nativeHeightPercentage;
            nativeAdLayout.getLayoutParams().height = (int) res;
            populateAdMobNative(nativeAd, unifiedNativeAdView);
            nativeAdLayout.removeAllViews();
            nativeAdLayout.addView(unifiedNativeAdView);

            loadAdMobBigNativePreload(activity);    //preload displayed
        }, flashDelay);
    }

    private static void requestAdMobBigNative(final BaseActivity activity, final ViewGroup nativeAdLayout) {
        if (config.adMob.nativeAd.isEmpty()) return;

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        double res = (getDeviceHeight(activity) / 100.0f) * config.nativeHeightPercentage;
        if (!config.flashingNative) {
            nativeAdLayout.getLayoutParams().height = (int) res;
        }
        AdLoader adLoader = new AdLoader.Builder(activity, config.adMob.nativeAd.get(adNativeCount))
                .forNativeAd(unifiedNativeAd -> {
                    if (config.flashingNative) {
                        nativeAdLayout.getLayoutParams().height = (int) res;
                    }
                    NativeAdView unifiedNativeAdView;

                    if (config.customNative) {
                        unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_native_big_custom, nativeAdLayout, false);
                    } else {
                        unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_native_big_original, nativeAdLayout, false);
                    }

                    populateAdMobNative(unifiedNativeAd, unifiedNativeAdView);
                    nativeAdLayout.removeAllViews();
                    nativeAdLayout.addView(unifiedNativeAdView);
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NotNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e(TAG, "onAdFailedToLoad: " + loadAdError.toString());
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adNativeCount = (adNativeCount + 1) % config.adMob.nativeAd.size();

                        if (config.preloadNative != null && config.preloadNative) {
                            loadAdMobBigNativePreload(activity);    //simple native loaded
                        }
                    }
                }).withNativeAdOptions(adOptions).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    protected static void requestGGBigNative(final BaseActivity activity, ViewGroup adView) {
        if (config.gg.nativeAd.isEmpty()) return;
        double res = (getDeviceHeight(activity) / 100.0f) * config.nativeHeightPercentage;
        adView.getLayoutParams().height = (int) res;
        GGAdview ggAd = new GGAdview(activity);
        ggAd.setUnitId(config.gg.nativeAd.get(adNativeGGCount));
        ggAd.setAdsMaxHeight((int) res);
        ggAd.loadAd(new AdLoadCallback() {
            @Override
            public void onAdLoaded() {
                adNativeGGCount = (adNativeGGCount + 1) % config.gg.nativeAd.size();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {

            }

            @Override
            public void onUiiOpened() {

            }

            @Override
            public void onUiiClosed() {

            }

            @Override
            public void onReadyForRefresh() {

            }
        });
        adView.addView(ggAd);
    }

    protected static void requestGGSmallNative(final BaseActivity activity, ViewGroup adView) {
        if (config.gg.nativeAd.isEmpty()) return;

        adView.getLayoutParams().height = px(activity, com.intuit.sdp.R.dimen._100sdp);
        GGAdview ggAd = new GGAdview(activity);
        ggAd.setUnitId(config.gg.nativeAd.get(adNativeGGCount));
        ggAd.setAdsMaxHeight(px(activity, com.intuit.sdp.R.dimen._100sdp));
        ggAd.loadAd(new AdLoadCallback() {
            @Override
            public void onAdLoaded() {
                adNativeGGCount = (adNativeGGCount + 1) % config.gg.nativeAd.size();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {

            }

            @Override
            public void onUiiOpened() {

            }

            @Override
            public void onUiiClosed() {

            }

            @Override
            public void onReadyForRefresh() {

            }
        });
        adView.addView(ggAd);
    }

    //this method will try to load custom api fetched native
    protected static void requestCustomNative(BaseActivity activity, ViewGroup viewGroup) {
        View view = activity.getLayoutInflater().inflate(R.layout.ad_layout_custom, viewGroup, false);
        viewGroup.addView(view);

        view.findViewById(R.id.cv_custom_ad).setOnClickListener((v) -> {
            try {
                if (!AdsUtility.config.clickDirection.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AdsUtility.config.clickDirection));
                    activity.startActivity(browserIntent);
                }
            } catch (Exception ignored) {
            }
        });
        Glide.with(activity).load(AdsUtility.config.imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewGroup.removeView(view);
                        requestNativeAd(activity, viewGroup);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        view.findViewById(R.id.progress).setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(((ImageView) view.findViewById(R.id.iv_custom_ad_image)));
        ((TextView) view.findViewById(R.id.tv_custom_ad_title)).setText(AdsUtility.config.mTitle);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    protected static void showAdMobInterstitial(BaseActivity activity, Intent intent) {
        if (config.adMob.interstitialAd.isEmpty()) {
            //BaseActivity.dismissDialog();
            startActivity(activity, intent);
            return;
        }

        BaseActivity.showDialog();
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, config.adMob.interstitialAd.get(adInterstitialCount), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                Toast.makeText(context, "loaded", Toast.LENGTH_SHORT).show();
//                AdsUtility.interstitialAd = interstitialAd;
                interstitialAd.show(activity);
                AppOpenManager.overrideAppOpenShow(true);
                adInterstitialCount = (adInterstitialCount + 1) % config.adMob.interstitialAd.size();
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
//                                interstitialAd = null;
                                startActivity(activity, intent);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                                startActivity(activity, intent);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                            }
                        });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i(TAG, loadAdError.getMessage());
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                startActivity(activity, intent);
            }
        });
    }

    protected static void showGGInterstitial(BaseActivity activity, Intent intent) {
        if (config.gg.interstitialAd.isEmpty()) {
            //BaseActivity.dismissDialog();
            startActivity(activity, intent);
            return;
        }

        BaseActivity.showDialog();
        GGInterstitialAd mAd = new GGInterstitialAd(activity, config.gg.interstitialAd.get(adInterstitialGGCount));
        mAd.setListener(new GGInterstitialEventsListener() {
            @Override
            public void onAdLoaded() {
                AppOpenManager.overrideAppOpenShow(true);
                mAd.show(activity);
                adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
            }

            @Override
            public void onAdClosed() {
                AppOpenManager.overrideAppOpenShow(false);
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                startActivity(activity, intent);
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdShowFailed() {
                Log.e(TAG, "GG onAdShowFailed: ");
                AppOpenManager.overrideAppOpenShow(false);
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                startActivity(activity, intent);
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors cause) {
                Log.e(TAG, "GG onAdLoadFailed: " + cause.name());
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                startActivity(activity, intent);
            }
        });
        mAd.loadAd();
    }

    private static void startActivity(BaseActivity activity, Intent intent) {
        activity.startActivity(intent);
    }

    protected static void loadAdMobInterstitial(Context context) {
        if (config.adMob.interstitialAd.isEmpty()) return;
        if (AdsUtility.interstitialAd != null) return;

        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        InterstitialAd.load(context, config.adMob.interstitialAd.get(adInterstitialCount), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                Toast.makeText(context, "loaded", Toast.LENGTH_SHORT).show();
                AdsUtility.interstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i(TAG, loadAdError.getMessage());
                interstitialAd = null;
            }
        });
    }

    protected static void loadAdMobInterstitialWithCallback(Context context, AdLoadListener listener) {
        if (config.adMob.interstitialAd.isEmpty() || AdsUtility.interstitialAd != null) {
            listener.onAdLoaded();
            return;
        }

        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        InterstitialAd.load(context, config.adMob.interstitialAd.get(adInterstitialCount), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                Toast.makeText(context, "loaded", Toast.LENGTH_SHORT).show();
                AdsUtility.interstitialAd = interstitialAd;
                listener.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i(TAG, loadAdError.getMessage());
                interstitialAd = null;
                listener.onAdLoaded();
            }
        });
    }

    protected static void loadGGInterstitial(Context context) {
        if (config.gg.interstitialAd.isEmpty()) return;
        if (interstitialAdGG != null && interstitialAdGG.isAdLoaded()) return;

        interstitialAdGG = new GGInterstitialAd(context, config.gg.interstitialAd.get(adInterstitialGGCount));
        interstitialAdGG.loadAd();
    }

    protected static void showAdMobInterstitialNoLoader(BaseActivity activity, Intent intent) {
        if (config.adMob.interstitialAd.isEmpty()) {
            startActivity(activity, intent);
            interstitialAd = null;
            return;
        }

        if (interstitialAd != null) {
            AppOpenManager.overrideAppOpenShow(true);
            interstitialAd.show(activity);
            adInterstitialCount = (adInterstitialCount + 1) % config.adMob.interstitialAd.size();
            interstitialAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            AppOpenManager.overrideAppOpenShow(false);
                            currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                            interstitialAd = null;
                            startActivity(activity, intent);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                            AppOpenManager.overrideAppOpenShow(false);
                            currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                            startActivity(activity, intent);
                            interstitialAd = null;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                        }
                    });
        } else {
            //currentActivityCount = (currentActivityCount + 1) % config.activityCount;
            startActivity(activity, intent);
        }
    }

    protected static void showGGInterstitialNoLoader(BaseActivity activity, Intent intent) {
        if (config.gg.interstitialAd.isEmpty()) {
            startActivity(activity, intent);
            return;
        }

        if (interstitialAdGG != null && interstitialAdGG.isAdLoaded()) {
            interstitialAdGG.setListener(new GGInterstitialEventsListener() {
                @Override
                public void onAdLoaded() {

                }

                @Override
                public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                    currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                    startActivity(activity, intent);
                }

                @Override
                public void onAdShowFailed() {
                    AppOpenManager.overrideAppOpenShow(false);
                    currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                    startActivity(activity, intent);
                }

                @Override
                public void onAdOpened() {

                }

                @Override
                public void onAdClosed() {
                    AppOpenManager.overrideAppOpenShow(false);
                    currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                    startActivity(activity, intent);
                }
            });
            AppOpenManager.overrideAppOpenShow(true);
            interstitialAdGG.show(activity);
            adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
        } else {
            startActivity(activity, intent);
        }
    }

    protected static void showAdMobInterstitialNoLoaderNoRedirect(BaseActivity activity) {
        if (config.adMob.interstitialAd.isEmpty()) {
            interstitialAd = null;
            return;
        }

        if (interstitialAd != null) {
            AppOpenManager.overrideAppOpenShow(true);
            interstitialAd.show(activity);
            adInterstitialCount = (adInterstitialCount + 1) % config.adMob.interstitialAd.size();
            interstitialAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            AppOpenManager.overrideAppOpenShow(false);
                            interstitialAd = null;
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                            AppOpenManager.overrideAppOpenShow(false);
                            interstitialAd = null;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                        }
                    });
        }
    }

    public static void requestSingleInstanceInterstitialGG(final BaseActivity context, SplashSingleInstance.SingleInstanceCallback callback) {
        if (config.gg.interstitialAd.isEmpty()) callback.completed();

        GGInterstitialAd mAd = new GGInterstitialAd(context, config.gg.interstitialAd.get(adInterstitialGGCount));
        mAd.setListener(new GGInterstitialEventsListener() {
            @Override
            public void onAdLoaded() {
                AppOpenManager.overrideAppOpenShow(true);
                mAd.show(context);
                adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
            }

            @Override
            public void onAdClosed() {
                AppOpenManager.overrideAppOpenShow(false);
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdShowFailed() {
                AppOpenManager.overrideAppOpenShow(false);
                Log.e(TAG, "GG onAdShowFailed: ");
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors cause) {
                Log.e(TAG, "GG onAdLoadFailed: " + cause.name());
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }
        });
        mAd.loadAd();
    }

    public static void requestSingleInstanceInterstitialAdmob(final BaseActivity context, SplashSingleInstance.SingleInstanceCallback callback) {
        if (config.adMob.interstitialAd.isEmpty()) callback.completed();

        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        InterstitialAd.load(context, config.adMob.interstitialAd.get(adInterstitialCount), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                Toast.makeText(context, "loaded", Toast.LENGTH_SHORT).show();
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        AppOpenManager.overrideAppOpenShow(false);
                        callback.completed();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        AppOpenManager.overrideAppOpenShow(false);
                        callback.completed();
                    }
                });
                AppOpenManager.overrideAppOpenShow(true);
                interstitialAd.show(context);
                adInterstitialCount = (adInterstitialCount + 1) % config.adMob.interstitialAd.size();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i(TAG, loadAdError.getMessage());
                callback.completed();
                interstitialAd = null;
            }
        });
    }

    public static void requestSingleInstanceInterstitial(final BaseActivity context, SplashSingleInstance.SingleInstanceCallback callback) {
        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        switch (adToDisplay) {
            case AdType.GOOGLE:
                requestSingleInstanceInterstitialAdmob(context, callback);
                break;
            case AdType.GG:
                requestSingleInstanceInterstitialGG(context, callback);
                break;
        }
    }

    public static boolean showInitialAppOpen() {
        if (config.displayAdsOrder.isEmpty()) return false;
        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        return AdsUtility.config.initialAppOpen && adToDisplay.equals(AdType.GOOGLE);
    }

    protected static void showAdMobInterstitialDelayedLoader(BaseActivity activity, Intent intent) {
        if (config.adMob.interstitialAd.isEmpty()) {
            startActivity(activity, intent);
            interstitialAd = null;
            return;
        }

        if (interstitialAd != null) {
            if (isNotEligibleForDelayedAd()) {
                startActivity(activity, intent);
                return;
            }

            BaseActivity.showDialog();
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> {
                AppOpenManager.overrideAppOpenShow(true);
                interstitialAd.show(activity);
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                interstitialAd = null;
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                startActivity(activity, intent);
                                delayedLoadRequest(activity.getApplicationContext());
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                                interstitialAd = null;
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                startActivity(activity, intent);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                            }
                        });
            }, 1000L);
        } else {
            //currentActivityCount = (currentActivityCount + 1) % config.activityCount;
            startActivity(activity, intent);
        }
    }

    protected static void showGGInterstitialDelayedLoader(BaseActivity activity, Intent intent) {
        if (config.gg.interstitialAd.isEmpty()) {
            startActivity(activity, intent);
            return;
        }

        if (interstitialAdGG != null && interstitialAdGG.isAdLoaded()) {
            if (isNotEligibleForDelayedAd()) {
                startActivity(activity, intent);
                return;
            }

            BaseActivity.showDialog();
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> {
                interstitialAdGG.setListener(new GGInterstitialEventsListener() {
                    @Override
                    public void onAdLoaded() {

                    }

                    @Override
                    public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                        BaseActivity.dismissDialog();
                        startActivity(activity, intent);
                    }

                    @Override
                    public void onAdShowFailed() {
                        AppOpenManager.overrideAppOpenShow(false);
                        BaseActivity.dismissDialog();
                        startActivity(activity, intent);
                    }

                    @Override
                    public void onAdOpened() {

                    }

                    @Override
                    public void onAdClosed() {
                        AppOpenManager.overrideAppOpenShow(false);
                        BaseActivity.dismissDialog();
                        startActivity(activity, intent);
                        delayedLoadRequest(activity.getApplicationContext());
                    }
                });
                AppOpenManager.overrideAppOpenShow(true);
                interstitialAdGG.show(activity);
                adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
            }, 1000L);
        } else {
            startActivity(activity, intent);
        }
    }

    private static long lastAdDismissTime = 0L;

    private static boolean isNotEligibleForDelayedAd() {
        return !config.allowAdDelay
                || config.adDelay <= 0L
                || lastAdDismissTime + (config.adDelay * 1000L) > System.currentTimeMillis();
    }

    private static void delayedLoadRequest(Context context) {
        lastAdDismissTime = System.currentTimeMillis();
        final long maxWaitTime = (config.adDelay * 1000L) - ((config.adDelay * 1000L) * 20 / 100);
        final Context appContext = context.getApplicationContext();
        new Handler(appContext.getMainLooper()).postDelayed(() -> {
            try {
                loadAdMobInterstitial(appContext);
                loadGGInterstitial(appContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, maxWaitTime);
    }

    protected static void showAdMobInterstitialWithCallback(BaseActivity activity, SingleInstanceCallback callback) {
        if (config.adMob.interstitialAd.isEmpty()) {
            //BaseActivity.dismissDialog();
            callback.completed();
            return;
        }

        BaseActivity.showDialog();
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, config.adMob.interstitialAd.get(adInterstitialCount), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                Toast.makeText(context, "loaded", Toast.LENGTH_SHORT).show();
//                AdsUtility.interstitialAd = interstitialAd;
                interstitialAd.show(activity);
                AppOpenManager.overrideAppOpenShow(true);
                adInterstitialCount = (adInterstitialCount + 1) % config.adMob.interstitialAd.size();
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
//                                interstitialAd = null;
                                callback.completed();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                                callback.completed();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                            }
                        });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i(TAG, loadAdError.getMessage());
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }
        });
    }

    protected static void showGGInterstitialWithCallback(BaseActivity activity, SingleInstanceCallback callback) {
        if (config.gg.interstitialAd.isEmpty()) {
            //BaseActivity.dismissDialog();
            callback.completed();
            return;
        }

        BaseActivity.showDialog();
        GGInterstitialAd mAd = new GGInterstitialAd(activity, config.gg.interstitialAd.get(adInterstitialGGCount));
        mAd.setListener(new GGInterstitialEventsListener() {
            @Override
            public void onAdLoaded() {
                AppOpenManager.overrideAppOpenShow(true);
                mAd.show(activity);
                adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
            }

            @Override
            public void onAdClosed() {
                AppOpenManager.overrideAppOpenShow(false);
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdShowFailed() {
                Log.e(TAG, "GG onAdShowFailed: ");
                AppOpenManager.overrideAppOpenShow(false);
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors cause) {
                Log.e(TAG, "GG onAdLoadFailed: " + cause.name());
                BaseActivity.dismissDialog();
                currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                callback.completed();
            }
        });
        mAd.loadAd();
    }

    protected static void showAdMobInterstitialDelayedLoaderWithCallback(BaseActivity activity, SingleInstanceCallback callback) {
        if (config.adMob.interstitialAd.isEmpty()) {
            callback.completed();
            interstitialAd = null;
            return;
        }

        if (interstitialAd != null) {
            if (isNotEligibleForDelayedAd()) {
                callback.completed();
                return;
            }

            BaseActivity.showDialog();
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> {
                AppOpenManager.overrideAppOpenShow(true);
                interstitialAd.show(activity);
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                interstitialAd = null;
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                callback.completed();
                                delayedLoadRequest(activity.getApplicationContext());
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                                interstitialAd = null;
                                AppOpenManager.overrideAppOpenShow(false);
                                BaseActivity.dismissDialog();
                                callback.completed();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                            }
                        });
            }, 1000L);
        } else {
            //currentActivityCount = (currentActivityCount + 1) % config.activityCount;
            callback.completed();
        }
    }

    protected static void showGGInterstitialDelayedLoaderWithCallback(BaseActivity activity, SingleInstanceCallback callback) {
        if (config.gg.interstitialAd.isEmpty()) {
            callback.completed();
            return;
        }

        if (interstitialAdGG != null && interstitialAdGG.isAdLoaded()) {
            if (isNotEligibleForDelayedAd()) {
                callback.completed();
                return;
            }

            BaseActivity.showDialog();
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> {
                interstitialAdGG.setListener(new GGInterstitialEventsListener() {
                    @Override
                    public void onAdLoaded() {

                    }

                    @Override
                    public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                        BaseActivity.dismissDialog();
                        callback.completed();
                    }

                    @Override
                    public void onAdShowFailed() {
                        AppOpenManager.overrideAppOpenShow(false);
                        BaseActivity.dismissDialog();
                        callback.completed();
                    }

                    @Override
                    public void onAdOpened() {

                    }

                    @Override
                    public void onAdClosed() {
                        AppOpenManager.overrideAppOpenShow(false);
                        BaseActivity.dismissDialog();
                        callback.completed();
                        delayedLoadRequest(activity.getApplicationContext());
                    }
                });
                AppOpenManager.overrideAppOpenShow(true);
                interstitialAdGG.show(activity);
                adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
            }, 1000L);
        } else {
            callback.completed();
        }
    }

    protected static void showAdMobInterstitialNoLoaderWithCallback(BaseActivity activity, SingleInstanceCallback callback) {
        if (config.adMob.interstitialAd.isEmpty()) {
            callback.completed();
            interstitialAd = null;
            return;
        }

        if (interstitialAd != null) {
            AppOpenManager.overrideAppOpenShow(true);
            interstitialAd.show(activity);
            adInterstitialCount = (adInterstitialCount + 1) % config.adMob.interstitialAd.size();
            interstitialAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            AppOpenManager.overrideAppOpenShow(false);
                            currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                            interstitialAd = null;
                            callback.completed();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NotNull com.google.android.gms.ads.AdError adError) {
                            AppOpenManager.overrideAppOpenShow(false);
                            currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                            callback.completed();
                            interstitialAd = null;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                        }
                    });
        } else {
            //currentActivityCount = (currentActivityCount + 1) % config.activityCount;
            callback.completed();
        }
    }

    protected static void showGGInterstitialNoLoaderWithCallback(BaseActivity activity, SingleInstanceCallback callback) {
        if (config.gg.interstitialAd.isEmpty()) {
            callback.completed();
            return;
        }

        if (interstitialAdGG != null && interstitialAdGG.isAdLoaded()) {
            interstitialAdGG.setListener(new GGInterstitialEventsListener() {
                @Override
                public void onAdLoaded() {

                }

                @Override
                public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                    currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                    callback.completed();
                }

                @Override
                public void onAdShowFailed() {
                    AppOpenManager.overrideAppOpenShow(false);
                    currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                    callback.completed();
                }

                @Override
                public void onAdOpened() {

                }

                @Override
                public void onAdClosed() {
                    AppOpenManager.overrideAppOpenShow(false);
                    currentActivityCount = (currentActivityCount + 1) % config.activityCount;
                    callback.completed();
                }
            });
            AppOpenManager.overrideAppOpenShow(true);
            interstitialAdGG.show(activity);
            adInterstitialGGCount = (adInterstitialGGCount + 1) % config.gg.interstitialAd.size();
        } else {
            callback.completed();
        }
    }

    //Rate
    public static void rateUs(BaseActivity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        } else {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        try {
            activity.startActivity(goToMarket);

        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));

        }
    }

    //More
    public static void moreApps(BaseActivity activity) {
        if (!config.accountName.isEmpty()) {
            Uri uri = Uri.parse("market://developer?id=" + config.accountName + "&hl=en");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            try {
                activity.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/developer?id=" + config.accountName + "&hl=en")));
            }
        } else {
            Uri uri = Uri.parse("market://developer?id=Motgo+Apps&hl=en");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            try {
                activity.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/developer?id=Motgo+Apps&hl=en")));
            }
        }
    }

    //Share
    public static void shareApp(BaseActivity activity) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));
            String shareMessage = "\nPlease try this application\n\n"
                    + "https://play.google.com/store/apps/details?id="
                    + activity.getApplicationContext().getPackageName() + "\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            activity.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception ignored) {
        }
    }

    //Policy
    public static void privacyPolicy(BaseActivity activity) {
        try {
            Intent browserIntent;
            if (!config.privacyPolicyUrl.isEmpty()) {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.privacyPolicyUrl));
            } else {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://visiontecprivacypolicy.blogspot.com/?m=1"));

            }
            activity.startActivity(browserIntent);
        } catch (Exception ignored) {
        }
    }

    public static void refreshTokens(BaseActivity activity) {
        startScreenCount = 0;
        exitScreenCount = 0;
        AdsUtility.needsLoading = true;
        AppOpenManager.refreshAppOpen(activity);
    }

    private static int px(BaseActivity activity, @DimenRes int dip) {
        return activity.getResources().getDimensionPixelSize(dip);
    }

    private static int getDeviceHeight(BaseActivity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    interface AdLoadListener {
        void onAdLoaded();
    }

    @Retention(RetentionPolicy.SOURCE)
    protected @interface AdType {
        String APPLOVIN = "Applovin";
        String FACEBOOK = "Facebook";
        String GOOGLE = "Admob";
        String GG = "GG";
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScreenType {
        String THANKYOU = "thankyou";
        String DASHBOARD = "dashboard";
    }

    public static void logFirebaseAdImpression(Context context) {
        if (context == null) return;
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.AD_PLATFORM, "AdMob");
        params.putString(FirebaseAnalytics.Param.AD_FORMAT, "Interstitial");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params);
    }

    public static void getStartScreenBundle(Intent intent) {
        try {
            if (AdsUtility.config.startScreenRepeatCount > AdsUtility.startScreenCount
                    && config.extraScreenData.size() > config.startScreenRepeatCount) {
                ScreenDataModel parcel = config.extraScreenData.get(config.startScreenRepeatCount);
                intent.putExtra("PARCEL", parcel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}