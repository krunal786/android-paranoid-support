package com.codebase.paranoidsupport.service;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.extras.LocalAdsActivity;
import com.codebase.paranoidsupport.service.AdsUtility.AdType;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greedygame.core.AppConfig;
import com.greedygame.core.GreedyGameAds;
import com.greedygame.core.models.general.AdErrors;
import com.greedygame.core.rewarded_ad.general.GGRewardedAd;
import com.greedygame.core.rewarded_ad.general.GGRewardedAdsEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.codebase.paranoidsupport.service.AdsUtility.config;
import static com.codebase.paranoidsupport.service.AdsUtility.rewardedVideoCount;
import static com.codebase.paranoidsupport.service.AdsUtility.rewardedVideoGGCount;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;

public class BaseActivity extends AppCompatActivity {
    public static final String PARAM_FOR_RESULT = "FOR_RESULT";
    public static final String PARAM_REQUEST_CODE = "REQUEST_CODE";

    private static Dialog progressDialog;
    BroadcastReceiver receiver;

    private final AppStorage storage = AppStorage.get();
    private GPSTracker gps;

    public static void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static void showDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void sdkInitialized() {

    }

    protected void sdkInitializationFailed() {

    }

    protected void bannerAd() {
        bannerAd(findViewById(R.id.adview));
    }

    public void bannerAd(ViewGroup viewGroup) {
        AdsUtility.requestBannerAd(this, viewGroup);
    }

    protected void nativeAd() {
        if (config.forcedNative == NativeAdsAdapter.NativeSize.SMALL) {
            AdsUtility.requestSmallNativeAd(this, findViewById(R.id.native_ad_container));
        } else {
            AdsUtility.requestNativeAd(this, findViewById(R.id.native_ad_container));
        }
    }

    public void nativeAd(ViewGroup viewGroup) {
        if (config.forcedNative == NativeAdsAdapter.NativeSize.SMALL) {
            AdsUtility.requestSmallNativeAd(this, viewGroup);
        } else {
            AdsUtility.requestNativeAd(this, viewGroup);
        }
    }

    protected void customNativeAd() {
        if (config.customAd) {
            customNativeAd(findViewById(R.id.native_ad_container));
        } else {
            nativeAd();
        }
    }

    public void customNativeAd(ViewGroup viewGroup) {
        if (config.customAd) {
            AdsUtility.requestCustomNative(this, viewGroup);
        } else {
            nativeAd();
        }
    }

    private void loadInterstitial() {
        AdsUtility.loadAdMobInterstitial(this);
        AdsUtility.loadGGInterstitial(this);
    }

    public void showInterstitialNoLoaderNoRedirect() {
        if (config.displayAdsOrder.isEmpty()) {
            return;
        }

        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        switch (adToDisplay) {
            case AdType.GOOGLE:
//                        Toast.makeText(this, "google full: " + AdsUtility.adInterstitialCount, Toast.LENGTH_SHORT).show();
                AdsUtility.showAdMobInterstitialNoLoaderNoRedirect(this);
                AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                break;
            case AdType.GG:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        int newUiOptions = decorView.getSystemUiVisibility();
        newUiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        decorView.setSystemUiVisibility(newUiOptions);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getWindow().setStatusBarColor(Window.DECOR_CAPTION_SHADE_AUTO);
        }

        gps = new GPSTracker(BaseActivity.this);
        resolutionForResult = (this).registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                fetchLastLocation(); //system location enabled
            } else {
                if (gps.canGetLocation()) {
                    enableLocationSettings(); //system location rejected
                } else {
                    gps.showSettingsAlert(findViewById(R.id.splashContainer), v -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 99544);
                    });
                }
            }
        });

        receiver = new NetworkChangeReceiver(new NetworkChangeReceiver.SDKConfig() {
            @Override
            public void onSDKInit() {
                if (storage.has(AppStorage.KEY_COUNTRY_CODE)
                        && storage.has(AppStorage.KEY_COUNTRY)
                        && storage.has(AppStorage.KEY_STATE)
                        && storage.has(AppStorage.KEY_CITY)) {
                    List<String> limitedArea = new ArrayList<>();
                    limitedArea.add(storage.get(AppStorage.KEY_COUNTRY_CODE));
                    limitedArea.add(storage.get(AppStorage.KEY_COUNTRY));
                    limitedArea.add(storage.get(AppStorage.KEY_STATE));
                    limitedArea.add(storage.get(AppStorage.KEY_CITY));
                    setupAdMembers(limitedArea);   //fetched from prefs
                } else if (config.requestLocation) {
                    requestLocationPermission(); //initial request
                } else {
                    setupAdMembers(Collections.emptyList());
                }
            }

            @Override
            public void onSDKFailed() {
                sdkInitializationFailed();
            }

        });
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (!config.displayLoader) {
            loadInterstitial();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void showInterstitial(final Intent intent) {
        if (config.allowAdDelay && config.adDelay > 0) {
            showDelayedInterstitial(intent);
            return;
        }

        if (AdsUtility.currentActivityCount == 0) {
            if (config.displayAdsOrder.isEmpty()) {
                startActivity(intent);
                return;
            }

            String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
            if (config.displayLoader) {
                setupDialog();
                //progressDialog.show();
                switch (adToDisplay) {
                    case AdType.GOOGLE:
//                        Toast.makeText(this, "google full: " + AdsUtility.adInterstitialCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showAdMobInterstitial(this, intent);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    case AdType.GG:
//                        Toast.makeText(this, "gg full: " + AdsUtility.adInterstitialGGCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showGGInterstitial(this, intent);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    default:
                        dismissDialog();
                        AdsUtility.currentActivityCount = (AdsUtility.currentActivityCount + 1) % config.activityCount;
                        startActivity(intent);
                }
            } else {
                switch (adToDisplay) {
                    case AdType.GG:
//                        Toast.makeText(this, "gg full: " + AdsUtility.adInterstitialGGCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showGGInterstitialNoLoader(this, intent);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    case AdType.GOOGLE:
//                        Toast.makeText(this, "google full: " + AdsUtility.adInterstitialCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showAdMobInterstitialNoLoader(this, intent);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    default:
                        AdsUtility.currentActivityCount = (AdsUtility.currentActivityCount + 1) % config.activityCount;
                        startActivity(intent);
                }
            }
        } else {
            AdsUtility.currentActivityCount = (AdsUtility.currentActivityCount + 1) % config.activityCount;
            startActivity(intent);
        }
    }

    private void showDelayedInterstitial(final Intent intent) {
        if (config.displayAdsOrder.isEmpty()) {
            startActivity(intent);
            return;
        }

        setupDialog();
        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        switch (adToDisplay) {
            case AdType.GOOGLE:
                AdsUtility.showAdMobInterstitialDelayedLoader(this, intent);
                AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                break;
            case AdType.GG:
                AdsUtility.showGGInterstitialDelayedLoader(this, intent);
                AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                break;
            default:
                startActivity(intent);
        }
    }

    public void showInterstitialWithCallback(final SplashSingleInstance.SingleInstanceCallback callback) {
        if (config.allowAdDelay && config.adDelay > 0) {
            showDelayedInterstitialWithCallback(callback);
            return;
        }

        if (AdsUtility.currentActivityCount == 0) {
            if (config.displayAdsOrder.isEmpty()) {
                callback.completed();
                return;
            }

            String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
            if (config.displayLoader) {
                setupDialog();
                //progressDialog.show();
                switch (adToDisplay) {
                    case AdType.GOOGLE:
//                        Toast.makeText(this, "google full: " + AdsUtility.adInterstitialCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showAdMobInterstitialWithCallback(this, callback);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    case AdType.GG:
//                        Toast.makeText(this, "gg full: " + AdsUtility.adInterstitialGGCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showGGInterstitialWithCallback(this, callback);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    default:
                        dismissDialog();
                        AdsUtility.currentActivityCount = (AdsUtility.currentActivityCount + 1) % config.activityCount;
                        callback.completed();
                }
            } else {
                switch (adToDisplay) {
                    case AdType.GG:
//                        Toast.makeText(this, "gg full: " + AdsUtility.adInterstitialGGCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showGGInterstitialNoLoaderWithCallback(this, callback);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    case AdType.GOOGLE:
//                        Toast.makeText(this, "google full: " + AdsUtility.adInterstitialCount, Toast.LENGTH_SHORT).show();
                        AdsUtility.showAdMobInterstitialNoLoaderWithCallback(this, callback);
                        AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                        break;
                    default:
                        AdsUtility.currentActivityCount = (AdsUtility.currentActivityCount + 1) % config.activityCount;
                        callback.completed();
                }
            }
        } else {
            AdsUtility.currentActivityCount = (AdsUtility.currentActivityCount + 1) % config.activityCount;
            callback.completed();
        }
    }

    private void showDelayedInterstitialWithCallback(SplashSingleInstance.SingleInstanceCallback callback) {
        if (config.displayAdsOrder.isEmpty()) {
            callback.completed();
            return;
        }

        setupDialog();
        String adToDisplay = config.displayAdsOrder.get(config.alternativeAds ? AdsUtility.adTypeCount : 0);
        switch (adToDisplay) {
            case AdType.GOOGLE:
                AdsUtility.showAdMobInterstitialDelayedLoaderWithCallback(this, callback);
                AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                break;
            case AdType.GG:
                AdsUtility.showGGInterstitialDelayedLoaderWithCallback(this, callback);
                AdsUtility.adTypeCount = (AdsUtility.adTypeCount + 1) % config.displayAdsOrder.size();
                break;
            default:
                callback.completed();
        }
    }

    //TODO Needs Update
    private RewardItem rewardItem = null;

    public void showAdmobRewardedVideoAd(OnUserEarnedRewardListener callback) {
        if (config.adMob.videoAd.isEmpty()) return;

        setupDialog();
        progressDialog.show();


        final FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                if (rewardItem != null) {
                    callback.onUserEarnedReward(rewardItem);
                }
                rewardItem = null;
            }
        };

        OnUserEarnedRewardListener rewardCallback = reward -> rewardItem = reward;

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(
                this,
                config.adMob.videoAd.get(rewardedVideoCount),
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        dismissDialog();
                        Toast.makeText(BaseActivity.this, "Failed to fetch reward!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        rewardedVideoCount = (rewardedVideoCount + 1) % config.adMob.videoAd.size();
                        rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                        rewardedAd.show(BaseActivity.this, rewardCallback);
                        dismissDialog();
                    }
                });
    }

    private boolean hasRewardedGG = false;

    public void showGGRewardedVideoAd(OnUserEarnedRewardListener callback) {
        if (config.gg.videoAd.isEmpty()) return;

        setupDialog();
        progressDialog.show();

        GGRewardedAd mAd = new GGRewardedAd(this, config.gg.videoAd.get(rewardedVideoGGCount));
        mAd.setListener(new GGRewardedAdsEventListener() {
            @Override
            public void onReward() {
                hasRewardedGG = true;
            }

            @Override
            public void onAdLoaded() {
                rewardedVideoGGCount = (rewardedVideoGGCount + 1) % config.gg.videoAd.size();
                mAd.show();
                dismissDialog();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                dismissDialog();
                Toast.makeText(BaseActivity.this, "Failed to fetch reward!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdShowFailed() {
                dismissDialog();
                Toast.makeText(BaseActivity.this, "Failed to show reward!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClosed() {
                if (hasRewardedGG) {
                    callback.onUserEarnedReward(rewardItem);
                }
                hasRewardedGG = false;
                mAd.destroy();
            }
        });
        mAd.loadAd();
    }

    public void startActivity(Intent intent) {
        if (intent != null) {
            if (intent.getBooleanExtra(PARAM_FOR_RESULT, false)) {
                startActivityForResult(intent, intent.getIntExtra(PARAM_REQUEST_CODE, -1));
            } else {
                super.startActivity(intent);
            }
        }
    }

    private void setupDialog() {
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.ad_loading);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showLocalAds() {
        showInterstitial(new Intent(this, LocalAdsActivity.class));
    }

    public void openQureka() {
        if (!qurekaEnabled()) {
            Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, QurekaWebActivity.class);
        startActivity(intent);
    }

    public boolean qurekaEnabled() {
        return config.qurekaEnabled;
    }

    public void statusBarDark() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // use XOR here for remove LIGHT_STATUS_BAR from flags
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    public void statusBarLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;   // add LIGHT_STATUS_BAR to flag
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private String getCountryCode() {
        try {
            final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) {
                // SIM country code is available
                return simCountry.toLowerCase(Locale.getDefault());
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) {
                    // network country code is available
                    return networkCountry.toLowerCase(Locale.getDefault());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // nothing's working shoot the default locale ;)
        return getResources().getConfiguration().locale.getCountry().toLowerCase();
    }

    private boolean requestingPermissions = false;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (gps.canGetLocation()) {
                fetchLastLocation(); //coarse granted, system location available
            } else {
                enableLocationSettings(); //coarse granted, system location not available
            }
            return;
        }

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                requestingPermissions = false;
                if (gps.canGetLocation()) {
                    fetchLastLocation(); //coarse granted, system location available
                } else {
                    enableLocationSettings(); //system location not available
                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

            }
        };

        PermissionListener deniedDialogPermissionListener =
                SnackbarOnDeniedPermissionListener.Builder
                        .with(findViewById(R.id.splashContainer), "Location permission is needed for app functionality")
                        .withOpenSettingsButton("SETTINGS")
                        .withDuration(LENGTH_INDEFINITE)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar sb) {
                                super.onShown(sb);
                            }

                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                requestingPermissions = true;
                            }
                        })
                        .build();

        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new CompositePermissionListener(permissionListener, deniedDialogPermissionListener)).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99544) {
            requestLocationPermission(); //system location activity result
        }
    }

    private ActivityResultLauncher<IntentSenderRequest> resolutionForResult;

    private void enableLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10 * 1000)
                .setFastestInterval(2 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (LocationSettingsResponse response) -> fetchLastLocation()) //granted system location
                .addOnFailureListener(this, ex -> {
                    if (ex instanceof ResolvableApiException) {
                        try {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(((ResolvableApiException) ex).getResolution()).build();
                            resolutionForResult.launch(intentSenderRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void fetchLastLocation() {
        String countryCode = getCountryCode().toLowerCase().trim();
        storage.put(AppStorage.KEY_COUNTRY_CODE, countryCode);

        List<String> limitedArea = new ArrayList<>();
        limitedArea.add(countryCode);

        //if (gps.canGetLocation()) {
        gps.setupLocation(location -> {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Log.d("TAGGER", "fetchLastLocation: " + longitude + ", " + latitude);

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String stateName = address.getAdminArea();
                    String countryName = address.getCountryName();
                    String cityName = address.getSubAdminArea();
                    if (stateName != null && !stateName.isEmpty()) {
                        String state = stateName.toLowerCase().trim();
                        limitedArea.add(state);
                        storage.put(AppStorage.KEY_STATE, state);
                    }
                    if (countryName != null && !countryName.isEmpty()) {
                        String country = countryName.toLowerCase().trim();
                        limitedArea.add(country);
                        storage.put(AppStorage.KEY_COUNTRY, country);
                    }
                    if (cityName != null && !cityName.isEmpty()) {
                        String city = cityName.toLowerCase().trim();
                        limitedArea.add(city);
                        storage.put(AppStorage.KEY_CITY, city);
                    }
                }
                setupAdMembers(limitedArea);   //location fetched
            } catch (Exception e) {
                e.printStackTrace();
                setupAdMembers(limitedArea);   //failed to fetch location details
            }
            gps.stopUsingGPS();
        });
        //} else {
        //    gps.showSettingsAlert();
        //}
    }

    private void setupAdMembers(List<String> limitedArea) {
        //Toast.makeText(this, "" + limitedArea, Toast.LENGTH_SHORT).show();
        Log.d("TAGGER", "onResponse found: " + limitedArea);
        Log.d("TAGGER", "onResponse requested: " + config.limitedCountries);
        List<String> limitedCountries = Arrays.asList(config.limitedCountries.toLowerCase().split("-"));
        if (!Collections.disjoint(limitedArea, limitedCountries)) {
            config.activityCount = config.limitedActivityCount;
            config.flashingNative = config.limitedFlashingNative;
            config.customNative = config.limitedCustomNative;
            config.qurekaEnabled = config.limitedQurekaEnabled;
            config.adOnBack = config.limitedAdOnBack;
            config.screenCount = config.limitedScreenCount;
            config.vpnAllowed = config.limitedVpnAllowed;
            config.qurekaButtons = config.limitedQurekaButtons;
            config.preloadNative = config.limitedPreloadNative;
        }

        try {
            String[] splits = config.screenCount.split("-");
            if (!splits[0].equals("")) {
                config.startScreens = new Gson().fromJson("[\"" + splits[0].replace("*", "\",\"") + "\"]", new TypeToken<List<String>>() {
                }.getType());
                config.startScreenRepeatCount = config.startScreens.size();
            }

            if (!splits[1].equals("")) {
                config.exitScreens = new Gson().fromJson("[\"" + splits[1].replace("*", "\",\"") + "\"]", new TypeToken<List<String>>() {
                }.getType());
                config.exitScreenRepeatCount = config.exitScreens.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //screens setup & ad loading configurations
        Integer activityCount = config.activityCount;
        if (activityCount != null && activityCount > 0) {
            config.activityCount = activityCount + 1;
        } else {
            config.activityCount = 1;
        }

        if (!config.displayAdsOrder.isEmpty()) {
            config.displayAdsOrder.remove(AdsUtility.AdType.APPLOVIN);    //not integrated yet
            config.displayAdsOrder.remove(AdsUtility.AdType.FACEBOOK);    //deprecated

            int adMobRank = Integer.MAX_VALUE;
            if (config.displayAdsOrder.contains(AdsUtility.AdType.GOOGLE)) {
                adMobRank = config.displayAdsOrder.indexOf(AdsUtility.AdType.GOOGLE);
            }
            int ggRank = Integer.MAX_VALUE;
            if (config.displayAdsOrder.contains(AdsUtility.AdType.GG)) {
                ggRank = config.displayAdsOrder.indexOf(AdsUtility.AdType.GG);
            }
            AdsUtility.priorAdType = config.displayAdsOrder.get(Math.min(adMobRank, ggRank));
        }

        if (config.preloadNative != null && config.preloadNative) {
            //preloading natives
            AdsUtility.loadAdMobBigNativePreload(this); //initial request
        }

        if (!config.displayLoader || (config.allowAdDelay && config.adDelay > 0)) {
            AdsUtility.loadAdMobInterstitialWithCallback(this, this::postInit); //ad loaded
            AdsUtility.loadGGInterstitial(this);
        } else {
            //listener.onSDKInit();
            postInit(); //sdk initialization complete
        }
    }

    private void postInit() {
        if (config.displayAdsOrder.contains(AdType.GG)) {
            String appId = "";
            if (!config.gg.appIds.isEmpty()) {
                appId = config.gg.appIds.get(0);
            }
            AppConfig appConfig = new AppConfig.Builder(BaseActivity.this)
                    .withAppId(appId)
                    .build();
            GreedyGameAds.initWith(appConfig, null);
        }

        boolean admobValidation = config.displayAdsOrder.contains(AdType.GOOGLE) && !config.adMob.appOpenId.isEmpty();
        boolean ggValidation = config.displayAdsOrder.contains(AdType.GG) && !config.gg.appOpenId.isEmpty();
        if ((admobValidation || ggValidation) && !AdsUtility.showInitialAppOpen()) {
            AppOpenManager.self.loadAppOpen();
        }

        AdsUtility.currentActivityCount = 0;

        if (AdsUtility.showInitialAppOpen()) {
            AppOpenManager.self.requestInitialAppOpen(BaseActivity.this::sdkInitialized);
        } else {
            sdkInitialized();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (requestingPermissions) {
            requestingPermissions = false;
            requestLocationPermission(); //onResume
        }
    }
}
