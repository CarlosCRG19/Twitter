package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Main class to define tweets
@Parcel
@Entity(foreignKeys =  @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId"))
public class Tweet {

    @ColumnInfo
    @NonNull
    @PrimaryKey
    public String id;

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String createdAt;

    @Ignore
    public User user;

    @ColumnInfo
    public Long userId;

    @ColumnInfo
    public String timestamp;

    @ColumnInfo
    public String date;

    @ColumnInfo
    public String mediaUrl;

    @ColumnInfo
    public String favCount, rtCount;

    @ColumnInfo
    public boolean favorited, retweeted;

    public Tweet(){}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.id = jsonObject.getString("id_str");
        tweet.body = jsonObject.getString("full_text");
        tweet.createdAt = jsonObject.getString("created_at");
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        tweet.timestamp = tweet.formatTimestamp(tweet.createdAt);
        tweet.date = tweet.formatDate(tweet.createdAt);
        tweet.favCount = tweet.formatCount(jsonObject.getInt("favorite_count"));
        tweet.rtCount = tweet.formatCount(jsonObject.getInt("retweet_count"));
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        JSONObject entities = jsonObject.getJSONObject("entities");
        if(entities.has("media")) {
            tweet.mediaUrl = entities.getJSONArray("media").getJSONObject(0).getString("media_url_https");
        }

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for(int i=0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    // Formatter method provided by CodePath
    private String formatTimestamp(String rawJsonDate) {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;

        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i("RelativeTime", "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";

    }

    // Method to format date to appear as HH:MM DD M. YYYY
    private String formatDate(String rawJsonDate) {
        return rawJsonDate.substring(11, 16) + " " + rawJsonDate.substring(8,10) + " " + rawJsonDate.substring(4,7) + ". " + rawJsonDate.substring(26);
    }

    // Format count
    private String formatCount(int count){
        if(count > 999999) {
            return String.valueOf(count / 1000000) + "M";
        } else if (count > 999) {
            return String.valueOf(count / 1000) + "K";
        }
        return String.valueOf(count);
    }

}
