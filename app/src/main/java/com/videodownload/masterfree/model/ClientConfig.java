package com.videodownload.masterfree.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Muicv on 8/17/2018.
 */

public class ClientConfig {
    @SerializedName("max_ctr_bot")
    public int max_ctr_bot;
    @SerializedName("max_ads_perday")
    public int max_ads_perday;
    @SerializedName("min_click_delay")
    public int min_click_delay;
    @SerializedName("max_click_delay")
    public int max_click_delay;
    @SerializedName("max_percent_ads")
    public int max_percent_ads;
    @SerializedName("fb_percent_ads")
    public int fb_percent_ads;

    @SerializedName("idFullService")
    public String idFullService;
    @SerializedName("idFullFbService")
    public String idFullFbService;
}
