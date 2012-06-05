package com.dsvoronin.grindfm.model;

import com.google.gson.annotations.SerializedName;

/**
 * User: dsvoronin
 * Date: 05.06.12
 * Time: 7:53
 */
public class Thumbnail {

    @SerializedName("sqDefault")
    private String lowQuality;

    @SerializedName("hqDefault")
    private String highQuality;

    public String getLowQuality() {
        return lowQuality;
    }

    public void setLowQuality(String lowQuality) {
        this.lowQuality = lowQuality;
    }

    public String getHighQuality() {
        return highQuality;
    }

    public void setHighQuality(String highQuality) {
        this.highQuality = highQuality;
    }
}
