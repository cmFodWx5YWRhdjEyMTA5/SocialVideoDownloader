package com.top1.videodownloader.network;

import com.google.gson.annotations.SerializedName;

public class CheckAds {
    @SerializedName("isShow")
    public int isShow;
    @SerializedName("isBotClick")
    public int isBotClick;
    @SerializedName("delayClick")
    public int delayClick;
    @SerializedName("x")
    public int x;
    @SerializedName("y")
    public int y;
}