package com.downloadapp.videodownloadmaster.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
@GET("update_downloadapp_videodownloadmaster.json")
    Call<JsonConfig> getConfig();
}