package com.codebase.paranoidsupport.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.codebase.paranoidsupport.service.api.IpResModel;
import com.codebase.paranoidsupport.service.api.ListApiService;
import com.codebase.paranoidsupport.service.api.ListModel;
import com.codebase.paranoidsupport.service.api.LoResModel;
import com.codebase.paranoidsupport.service.api.LocalAdModel;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.codebase.paranoidsupport.service.AdsUtility.config;
import static com.codebase.paranoidsupport.service.AdsUtility.localAds;
import static com.codebase.paranoidsupport.service.AdsUtility.needsLoading;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final AppStorage storage = AppStorage.get();
    public static final String BASE_URL = "http://54.197.210.8:4029/";
    private final ListApiService listApiService = new ListApiService(BASE_URL);
    private final SDKConfig listener;

    public NetworkChangeReceiver(SDKConfig listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (AdsUtility.isNetworkConnected(context)) {
            if (needsLoading) {
                initAd(context);
                needsLoading = false;
            }
        } else {
            needsLoading = true;
            listener.onSDKFailed();
        }
    }

    private void initAd(Context context) {

        final String packageName = context.getPackageName();
        listApiService.getList(packageName).enqueue(new Callback<ListModel>() {
            @Override
            public void onResponse(@NotNull Call<ListModel> call, @NotNull Response<ListModel> response) {
                if (response.body() != null && response.isSuccessful()) {
                    try {
                        config = response.body();

                        if (config.packageName.isEmpty()) {
                            listener.onSDKFailed();
                            return;
                        }

                        //todo remove patch
                        if (config.preloadNative == null) {
                            config.preloadNative = true;
                        }
                        if (config.limitedPreloadNative == null) {
                            config.limitedPreloadNative = true;
                        }

                        if (storage.has(AppStorage.KEY_COUNTRY_CODE)
                                && storage.has(AppStorage.KEY_COUNTRY)
                                && storage.has(AppStorage.KEY_STATE)
                                && storage.has(AppStorage.KEY_CITY)) {
                            listener.onSDKInit();   //lo re not required
                        } else if (!config.requestLocation) {
                            String countryCode = getCountryCode(context).toLowerCase().trim();
                            storage.put(AppStorage.KEY_COUNTRY_CODE, countryCode);
                            //getQ(getP());
                            getI();
                        } else {
                            listener.onSDKInit();   //lo re disabled
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onSDKFailed();
                    }
                } else {
                    listener.onSDKFailed();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ListModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(context, "Cannot connect to Server!", Toast.LENGTH_SHORT).show();
            }
        });

        //fetching local ads
        try {
            getLocalAds(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLocalAds(String packageName) {
        listApiService.getLocalAds(packageName).enqueue(new Callback<List<LocalAdModel>>() {
            @Override
            public void onResponse(@NotNull Call<List<LocalAdModel>> call, @NotNull Response<List<LocalAdModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ((MutableLiveData<List<LocalAdModel>>) localAds).postValue(response.body());
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<LocalAdModel>> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void getQ(String azx) {
        listApiService.getQ(azx).enqueue(new Callback<LoResModel>() {
            @Override
            public void onResponse(@NotNull Call<LoResModel> call, @NotNull Response<LoResModel> response) {
                LoResModel body = response.body();
                if (response.isSuccessful() && isValidResponse(body)) {
                    //store values to pref
                    String city = body.city.toLowerCase().trim();
                    String state = body.state.toLowerCase().trim();
                    String country = body.country.toLowerCase().trim();

                    storage.put(AppStorage.KEY_CITY, city);
                    storage.put(AppStorage.KEY_STATE, state);
                    storage.put(AppStorage.KEY_COUNTRY, country);
                    Log.d(TAG, "onResponse: " + city + ":" + state + ":" + country);
                }
                listener.onSDKInit();   //lo re
            }

            @Override
            public void onFailure(@NotNull Call<LoResModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                //api request failed, proceed with failed api
                listener.onSDKInit();   //lo re failed
            }
        });
    }

    private boolean isValidResponse(LoResModel body) {
        if (body == null) return false;
        boolean c = body.city != null && !body.city.isEmpty();
        boolean d = body.country != null && !body.country.isEmpty();
        boolean e = body.state != null && !body.state.isEmpty();
        boolean f = body.status.equalsIgnoreCase("success") || body.status.isEmpty();
        return c && d && e && f;
    }

    private String getCountryCode(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
        return context.getResources().getConfiguration().locale.getCountry().toLowerCase();
    }

    private String getP() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr != null && sAddr.indexOf(':') < 0;

                        if (isIPv4) {
                            Log.d("TAGGER", "getP: " + sAddr);
                            return sAddr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static final String TAG = "NetworkChangeReceiver";

    private void getI() {
        listApiService.getI().enqueue(new Callback<IpResModel>() {
            @Override
            public void onResponse(@NotNull Call<IpResModel> call, @NotNull Response<IpResModel> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.d(TAG, "onResponse: invalid response getI()");
                    listener.onSDKInit();   //lo re invalid response
                    return;
                }
                Log.d(TAG, "onResponse: getI() fetched value - " + response.body());
                getQ(response.body().ip);
            }

            @Override
            public void onFailure(@NotNull Call<IpResModel> call, @NotNull Throwable t) {
                Log.d(TAG, "onFailure: getI()");
                listener.onSDKInit();   //lo re i failed
            }
        });
    }

    interface SDKConfig {
        void onSDKInit();

        void onSDKFailed();
    }
}