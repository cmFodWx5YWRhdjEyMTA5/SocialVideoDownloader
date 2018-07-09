package com.mui.catchvideodownload.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Site {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("image")
    @Expose
    private String image;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}