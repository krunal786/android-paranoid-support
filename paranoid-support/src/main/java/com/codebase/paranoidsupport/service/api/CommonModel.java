package com.codebase.paranoidsupport.service.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CommonModel {

    @SerializedName("app_id")
    @Expose
    public List<String> appIds = new ArrayList<>();
    @SerializedName("interstitial_ad")
    @Expose
    public List<String> interstitialAd = new ArrayList<>();
    @SerializedName("banner_ad")
    @Expose
    public List<String> bannerAd = new ArrayList<>();
    @SerializedName("video_ad")
    @Expose
    public List<String> videoAd = new ArrayList<>();
    @SerializedName("native_ad")
    @Expose
    public List<String> nativeAd = new ArrayList<>();
    @SerializedName("app_open_id")
    @Expose
    public List<String> appOpenId = new ArrayList<>();

}
