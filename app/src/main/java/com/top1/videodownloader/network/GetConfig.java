package com.top1.videodownloader.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
//    @GET("4_videodownload_catchvideodownloader.json")
//    @GET("3_downloadapp_videodownloadmaster.json")
@GET("update_com_top1_videodownloader.json")
    Call<JsonConfig> getConfig();
}