package com.freesocial.videodownloader.network;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
@GET("update_com_freesocial_videodownloader.json")
    Call<JsonConfig> getConfig();
}