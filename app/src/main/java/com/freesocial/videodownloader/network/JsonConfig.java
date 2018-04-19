package com.freesocial.videodownloader.network;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JsonConfig {

    @SerializedName("idBannerAdmob")
    @Expose
    private String idBannerAdmob;
    @SerializedName("idBannerFacebook")
    @Expose
    private String idBannerFacebook;
    @SerializedName("idFullAdmob")
    @Expose
    private String idFullAdmob;
    @SerializedName("idFullFacebook")
    @Expose
    private String idFullFacebook;
    @SerializedName("isAccept")
    @Expose
    private Integer isAccept;
    @SerializedName("newAppPackage")
    @Expose
    private String newAppPackage;
    @SerializedName("percentAds")
    @Expose
    private Integer percentAds;
    @SerializedName("priorityBanner")
    @Expose
    private String priorityBanner;
    @SerializedName("priorityFull")
    @Expose
    private String priorityFull;
    @SerializedName("urlAccept")
    @Expose
    private List<String> urlAccept = null;

    public String getIdBannerAdmob() {
        return idBannerAdmob;
    }

    public void setIdBannerAdmob(String idBannerAdmob) {
        this.idBannerAdmob = idBannerAdmob;
    }

    public String getIdBannerFacebook() {
        return idBannerFacebook;
    }

    public void setIdBannerFacebook(String idBannerFacebook) {
        this.idBannerFacebook = idBannerFacebook;
    }

    public String getIdFullAdmob() {
        return idFullAdmob;
    }

    public void setIdFullAdmob(String idFullAdmob) {
        this.idFullAdmob = idFullAdmob;
    }

    public String getIdFullFacebook() {
        return idFullFacebook;
    }

    public void setIdFullFacebook(String idFullFacebook) {
        this.idFullFacebook = idFullFacebook;
    }

    public Integer getIsAccept() {
        return isAccept;
    }

    public void setIsAccept(Integer isAccept) {
        this.isAccept = isAccept;
    }

    public String getNewAppPackage() {
        return newAppPackage;
    }

    public void setNewAppPackage(String newAppPackage) {
        this.newAppPackage = newAppPackage;
    }

    public Integer getPercentAds() {
        return percentAds;
    }

    public void setPercentAds(Integer percentAds) {
        this.percentAds = percentAds;
    }

    public String getPriorityBanner() {
        return priorityBanner;
    }

    public void setPriorityBanner(String priorityBanner) {
        this.priorityBanner = priorityBanner;
    }

    public String getPriorityFull() {
        return priorityFull;
    }

    public void setPriorityFull(String priorityFull) {
        this.priorityFull = priorityFull;
    }

    public List<String> getUrlAccept() {
        return urlAccept;
    }

    public void setUrlAccept(List<String> urlAccept) {
        this.urlAccept = urlAccept;
    }

}