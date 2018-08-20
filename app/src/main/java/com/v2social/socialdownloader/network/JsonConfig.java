package com.v2social.socialdownloader.network;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class JsonConfig {
    @SerializedName("idBannerAdmob")
    public String idBannerAdmob;

    @SerializedName("idBannerFacebook")
    public String idBannerFacebook;

    @SerializedName("idFullAdmob")
    public String idFullAdmob;

    @SerializedName("idFullFacebook")
    public String idFullFacebook;

    @SerializedName("isAccept")
    public Integer isAccept;

    @SerializedName("newAppPackage")
    public String newAppPackage;

    @SerializedName("percentAds")
    public Integer percentAds;

    @SerializedName("percentRate")
    public Integer percentRate;

    @SerializedName("priorityBanner")
    public String priorityBanner;

    @SerializedName("priorityFull")
    public String priorityFull;

    @SerializedName("urlAccept")
    public List<Site> urlAccept = null;

    @SerializedName("delayService")
    public Integer delayService;

    @SerializedName("intervalService")
    public Integer intervalService;

    @SerializedName("idFullService")
    public String idFullService;

    @SerializedName("delay_retention")
    public int delay_retention;

    @SerializedName("retention")
    public int retention;

    @SerializedName("delay_report")
    public int delay_report;
    @SerializedName("idFullFbService")
    public String idFullFbService;
}

