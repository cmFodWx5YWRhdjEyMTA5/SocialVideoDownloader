package com.v2social.socialdownloader.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
    @GET("com_v2social_socialdownloader.json")
    Call<JsonConfig> getConfig();

    @GET("checkads")
    Call<CheckAds> checkAds();

    @FormUrlEncoded
    @POST("checkads")
    Call<String> checkAds(@Field("id") String id, @Field("isClick") String isClick);
}