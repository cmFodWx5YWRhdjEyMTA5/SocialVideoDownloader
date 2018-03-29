package com.free.videodownloader.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
//    @GET("4_videodownload_catchvideodownloader.json")
//    @GET("3_downloadapp_videodownloadmaster.json")
@GET("8_com_free_videodownloader.json")
    Call<JsonConfig> getConfig();

@GET("/complete/search?client=youtube&ds=yt&client=firefox")
Call<String[]> getSuggestion( @Query("q") String q);
}