package com.codebase.paranoidsupport.service.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocalAdModel {
    @SerializedName("apppkg")
    @Expose
    public String appPackage = "";
    @SerializedName("appname")
    @Expose
    public String appName = "";
    @SerializedName("applogo")
    @Expose
    public String appLogo = "";
}
