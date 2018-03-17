package com.mui.catchvideodownload.network;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JsonConfig {

    @SerializedName("idBannerAdmob")
    @Expose
    private String idBannerAdmob;
    @SerializedName("idFullAdmob")
    @Expose
    private String idFullAdmob;
    @SerializedName("idBannerFacebook")
    @Expose
    private String idBannerFacebook;
    @SerializedName("idFullFacebook")
    @Expose
    private String idFullFacebook;
    @SerializedName("isAccept")
    @Expose
    private Boolean isAccept;
    @SerializedName("newAppTitle")
    @Expose
    private String newAppTitle;
    @SerializedName("newAppPackage")
    @Expose
    private String newAppPackage;
    @SerializedName("percentUpdate")
    @Expose
    private Integer percentUpdate;
    @SerializedName("priorityBanner")
    @Expose
    private String priorityBanner;
    @SerializedName("priorityFull")
    @Expose
    private String priorityFull;
    @SerializedName("server")
    @Expose
    private String server;
    @SerializedName("sites")
    @Expose
    private List<Site> sites = null;
    @SerializedName("urlAccept")
    @Expose
    private List<String> urlAccept = null;

    public String getIdBannerAdmob() {
        return idBannerAdmob;
    }

    public void setIdBannerAdmob(String idBannerAdmob) {
        this.idBannerAdmob = idBannerAdmob;
    }

    public String getIdFullAdmob() {
        return idFullAdmob;
    }

    public void setIdFullAdmob(String idFullAdmob) {
        this.idFullAdmob = idFullAdmob;
    }

    public String getIdBannerFacebook() {
        return idBannerFacebook;
    }

    public void setIdBannerFacebook(String idBannerFacebook) {
        this.idBannerFacebook = idBannerFacebook;
    }

    public String getIdFullFacebook() {
        return idFullFacebook;
    }

    public void setIdFullFacebook(String idFullFacebook) {
        this.idFullFacebook = idFullFacebook;
    }

    public Boolean getIsAccept() {
        return isAccept;
    }

    public void setIsAccept(Boolean isAccept) {
        this.isAccept = isAccept;
    }

    public String getNewAppTitle() {
        return newAppTitle;
    }

    public void setNewAppTitle(String newAppTitle) {
        this.newAppTitle = newAppTitle;
    }

    public String getNewAppPackage() {
        return newAppPackage;
    }

    public void setNewAppPackage(String newAppPackage) {
        this.newAppPackage = newAppPackage;
    }

    public Integer getPercentUpdate() {
        return percentUpdate;
    }

    public void setPercentUpdate(Integer percentUpdate) {
        this.percentUpdate = percentUpdate;
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

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    public List<String> getUrlAccept() {
        return urlAccept;
    }

    public void setUrlAccept(List<String> urlAccept) {
        this.urlAccept = urlAccept;
    }

}