package com.codepath.apps.restclienttemplate.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    String TAG = "ReplyActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    Tweet tweet;
    TextView tvScreenName, tvBody;
    ImageView ivPPStatus, ivMedia;
    CardView cvMedia;
    TextView tvTimestamp;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        tvScreenName = findViewById(R.id.tvScreenName);
        tvBody = findViewById(R.id.tvBody);
        ivPPStatus = findViewById(R.id.ivProfileImage);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvTimestamp.setText(tweet.timestamp);

        tvScreenName.setText(tweet.user.screenName);
        tvBody.setText(tweet.body);
        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .circleCrop()
                .into(ivPPStatus);

        ivMedia = findViewById(R.id.ivMedia);
        cvMedia = findViewById(R.id.cardImage);

        if(tweet.media != null) {
            Log.i("Media", tweet.media.getMediaUrl());
            cvMedia.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tweet.media.getMediaUrl())
                    .transform(new RoundedCornersTransformation(30,0))
                    .into(ivMedia);
        } else {
            Log.i("Media", "No media");
            cvMedia.setVisibility(View.GONE);
        }
    }

}