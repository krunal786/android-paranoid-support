package com.codebase.paranoidsupport.service.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoResModel {
    @SerializedName("status")
    @Expose
    public String status = "";
    @SerializedName("city")
    @Expose
    public String city = "";
    @SerializedName("country")
    @Expose
    public String country = "";
    @SerializedName("regionName")
    @Expose
    public String state = "";
}
