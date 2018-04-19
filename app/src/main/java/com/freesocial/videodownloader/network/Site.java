package com.freesocial.videodownloader.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Site {

@SerializedName("id")
@Expose
private Integer id;
@SerializedName("image")
@Expose
private String image;
@SerializedName("name")
@Expose
private String name;
@SerializedName("url")
@Expose
private String url;

public Integer getId() {
return id;
}

public void setId(Integer id) {
this.id = id;
}

public String getImage() {
return image;
}

public void setImage(String image) {
this.image = image;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public String getUrl() {
return url;
}

public void setUrl(String url) {
this.url = url;
}

}