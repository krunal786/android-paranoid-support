package com.codebase.paranoidsupport.service.api;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListApiService {

    private final ListApi listApi;

    public ListApiService(String baseUrl) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        listApi = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
                .create(ListApi.class);
    }

    public Call<ListModel> getList(String pkgName) {
        return listApi.getList(pkgName);
    }

    public Call<List<LocalAdModel>> getLocalAds(String pkgName) {
        return listApi.getLocalAds(pkgName);
    }

    public Call<LoResModel> getQ(String zx) {
        return listApi.getQ(zx);
    }

    public Call<IpResModel> getI() {
        return listApi.getI();
    }
}
