package com.videodownload.masterfree.network;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by muicv on 6/18/2017.
 */

public interface GetConfig {
@GET("com_video_download_masterfree.json")
    Call<JsonConfig> getConfig();
}