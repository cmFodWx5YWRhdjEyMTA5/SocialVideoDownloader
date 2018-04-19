package com.all2.videodownloader.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
@GET("7_com_all2_videodownloader.json")
    Call<JsonConfig> getConfig();

@GET("/complete/search?client=youtube&ds=yt&client=firefox")
Call<String[]> getSuggestion( @Query("q") String q);
}