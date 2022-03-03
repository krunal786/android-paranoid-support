package com.codebase.paranoidsupport.service.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ListApi {

    @POST("/Api_manage.php")
    @FormUrlEncoded
    Call<ListModel> getList(@Field("pkg_name") String packageName);

    @POST("/Api_Sponser.php")
    @FormUrlEncoded
    Call<List<LocalAdModel>> getLocalAds(@Field("pkg_name") String packageName);

    @GET("http://ip-api.com/json/{zx}?fields=country,city,regionName")
    Call<LoResModel> getQ(@Path("zx") String zx);

    @GET("https://api64.ipify.org?format=json")
    Call<IpResModel> getI();
}
