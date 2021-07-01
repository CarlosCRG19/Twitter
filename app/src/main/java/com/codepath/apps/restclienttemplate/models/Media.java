package com.codepath.apps.restclienttemplate.models;

import androidx.room.Entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Media {

    public Long id;
    public String mediaUrl;

    public static Media fromJson(JSONObject jsonObject) throws JSONException {
        Media media = new Media();
        media.id = jsonObject.getLong("id");
        media.mediaUrl = jsonObject.getString("media_url_https");
        return media;
    }

    public String getMediaUrl() {
        return mediaUrl + "?name=small";
    }
}
