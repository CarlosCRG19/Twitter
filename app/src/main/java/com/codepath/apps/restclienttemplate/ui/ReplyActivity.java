package com.codepath.apps.restclienttemplate.ui;

import androidx.appcompat.app.AppCompatActivity;

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
import com.codepath.apps.restclienttemplate.databinding.ActivityReplyBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyActivity extends AppCompatActivity {

    String TAG = "ReplyActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    Tweet tweet;
    ActivityReplyBinding binding;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplyBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        setContentView(view);

        client = new TwitterClient(this);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        binding.tvName.setText(tweet.user.name);

        binding.tvScreenName.setText(" @" + tweet.user.screenName);
        binding.tvBody.setText(tweet.body);
        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage);

        binding.etReply.setText("@" + tweet.user.screenName);
        binding.ilReply.setCounterMaxLength(MAX_TWEET_LENGTH);

        binding.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyContent = binding.etReply.getText().toString();
                if(replyContent.isEmpty()){
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                } else if(replyContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(ReplyActivity.this, replyContent, Toast.LENGTH_LONG).show();
                client.postTweet(replyContent, tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        finish(); // closes the activity
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
            }
        });

    }
}