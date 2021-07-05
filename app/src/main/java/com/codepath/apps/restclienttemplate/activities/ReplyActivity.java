package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityReplyBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyActivity extends AppCompatActivity {

    // Static variables
    public static String TAG = "ReplyActivity"; // Tag for log messages
    public static final int MAX_TWEET_LENGTH = 280; // Max number of tweets

    // Member variables
    Tweet tweet; // tweet received from intent
    TwitterClient client; // Create an instance of a Twitter client with the context of this activity

    // View binding implementation to reduce code
    ActivityReplyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View binding implementation
        binding = ActivityReplyBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Assign twitter client
        client = new TwitterClient(this);

        // Retrieve tweet from intent
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        // CONNECT TWEET INFO WITH VIEWS
        populateViews();
        setBtnListeners();
    }

    // Binds the tweet data into the views
    private void populateViews() {
        // User info
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText(" @" + tweet.user.screenName);
        Glide.with(this) // Use glide to embed image
                .load(tweet.user.profileImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage);

        // Tweet info
        binding.tvBody.setText(tweet.body);

        // Reply views
        binding.etReply.setText("@" + tweet.user.screenName); // Get the name of the user who wrote the tweet
        binding.ilReply.setCounterMaxLength(MAX_TWEET_LENGTH); // Set the max characters for the content
    }

    // Set interaction buttons listeners
    private void setBtnListeners(){
        // Reply button listener
        binding.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyContent = binding.etReply.getText().toString(); // Get reply text from EditText

                // Text can't be empty nor can it exceed the limit max length
                if(replyContent.isEmpty()){
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                } else if(replyContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                } else {
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
            }
        });
    }
}