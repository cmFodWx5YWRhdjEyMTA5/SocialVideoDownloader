package com.mui.catchvideodownload.network;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
//    @GET("4_videodownload_catchvideodownloader.json")
//    @GET("3_downloadapp_videodownloadmaster.json")
@GET("com_v2social_socialdownloader.json")
//@GET("com_freesocial_videodownloader.json")
    Call<JsonConfig> getConfig();
}