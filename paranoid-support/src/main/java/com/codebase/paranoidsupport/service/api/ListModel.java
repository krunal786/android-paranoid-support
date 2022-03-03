package com.codebase.paranoidsupport.service.api;

import com.codebase.paranoidsupport.service.NativeAdsAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Despicable on 7/11/2020.
 */
public class ListModel {

    @SerializedName("pkgName")
    @Expose
    public String packageName = "";
    @SerializedName("privacypolicy_url")
    @Expose
    public String privacyPolicyUrl = "";
    @SerializedName("activity_count")
    @Expose
    public Integer activityCount;
    @SerializedName("display_ads_order")
    @Expose
    public List<String> displayAdsOrder = new ArrayList<>();
    @SerializedName("custom_native")
    @Expose
    public Boolean customNative = true;
    @SerializedName("custom_ad")
    @Expose
    public Boolean customAd = false;
    @SerializedName("display_loader")
    @Expose
    public Boolean displayLoader = true;
    @SerializedName("initial_app_open")
    @Expose
    public Boolean initialAppOpen = false;
    @SerializedName("flashing_native")
    @Expose
    public Boolean flashingNative = false;
    @SerializedName("click_direction")
    @Expose
    public String clickDirection = "";
    @SerializedName("image_url")
    @Expose
    public String imageUrl = "";
    @SerializedName("title")
    @Expose
    public String mTitle = "";
    @SerializedName("account_name")
    @Expose
    public String accountName = "";
    @SerializedName("base64_inapp_key")
    @Expose
    public String base64InAppKey = "1";
    @SerializedName("alternative_ads")
    @Expose
    public boolean alternativeAds = false;
    @SerializedName("forced_native")
    @Expose
    @NativeAdsAdapter.NativeSize
    public int forcedNative = NativeAdsAdapter.NativeSize.BIG;
    @SerializedName("flooring")
    @Expose
    public boolean flooring = false;
    @SerializedName("list_native_count")
    @Expose
    public int listNativeCount = 7;
    @SerializedName("native_height_percentage")
    @Expose
    public int nativeHeightPercentage = 40;
    @SerializedName("allow_ad_delay")
    @Expose
    public boolean allowAdDelay = true;
    @SerializedName("ad_delay")
    @Expose
    public long adDelay = 20;
    @SerializedName("website_link")
    @Expose
    public List<String> webSiteLink = new ArrayList<>();
    @SerializedName("start_screens")
    @Expose
    public List<String> startScreens = new ArrayList<>();
    @SerializedName("exit_screens")
    @Expose
    public List<String> exitScreens = new ArrayList<>();
    @SerializedName("qureka")
    @Expose
    public boolean qurekaEnabled = false;
    @SerializedName("add_screens")
    @Expose
    public boolean addScreens = false;
    @SerializedName("qureka_url")
    @Expose
    public String qurekaURL = "http://425.live.qureka.com/";
    @SerializedName("qureka_buttons")
    @Expose
    public String qurekaButtons = "";
    @SerializedName("ad_on_back")
    @Expose
    public Boolean adOnBack = false;
    @SerializedName("screen_count")
    @Expose
    public String screenCount;
    @SerializedName("start_screen_count")
    @Expose
    public Integer startScreenRepeatCount = 0;
    @SerializedName("exit_screen_count")
    @Expose
    public Integer exitScreenRepeatCount = 0;
    @SerializedName("extra_screen_data")
    @Expose
    public List<ScreenDataModel> extraScreenData;
    @SerializedName("vpn_allowed")
    @Expose
    public Boolean vpnAllowed;
    @SerializedName("request_location")
    @Expose
    public Boolean requestLocation;
    @SerializedName("preload_native")
    @Expose
    public Boolean preloadNative;


    @SerializedName("limited_countries")
    @Expose
    public String limitedCountries = "";
    @SerializedName("limited_activity_count")
    @Expose
    public Integer limitedActivityCount = 0;
    @SerializedName("limited_flashing_native")
    @Expose
    public boolean limitedFlashingNative = true;
    @SerializedName("limited_custom_native")
    @Expose
    public boolean limitedCustomNative = true;
    @SerializedName("limited_qureka")
    @Expose
    public Boolean limitedQurekaEnabled = null;
    @SerializedName("limited_qureka_buttons")
    @Expose
    public String limitedQurekaButtons = null;
    @SerializedName("limited_add_screens")
    @Expose
    public Boolean limitedAddScreens = null;
    @SerializedName("limited_ad_on_back")
    @Expose
    public Boolean limitedAdOnBack = false;
    @SerializedName("limited_screen_count")
    @Expose
    public String limitedScreenCount;
    @SerializedName("limited_vpn_allowed")
    @Expose
    public Boolean limitedVpnAllowed;
    @SerializedName("limited_preload_native")
    @Expose
    public Boolean limitedPreloadNative;


    @SerializedName("Admob")
    @Expose
    public CommonModel adMob = new CommonModel();
    @SerializedName("GG")
    @Expose
    public CommonModel gg = new CommonModel();
}