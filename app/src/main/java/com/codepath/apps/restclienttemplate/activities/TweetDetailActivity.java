package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    // ADAPTER POSITION
    // public final int POSITION = Parcels.unwrap(getIntent().getParcelableExtra("Position"));

    // Static variables
    public static String TAG = "ReplyActivity"; // Tag for log messages
    public static final int MAX_TWEET_LENGTH = 280; // Max length to use in counter

    // Member variables
    Tweet tweet; // tweet received from intent
    TwitterClient client; // Create an instance of a Twitter client with the context of this activity

    // View binding implementation to reduce code
    ActivityTweetDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate via view binding
        binding = ActivityTweetDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Assign twitter client
        client = new TwitterClient(this);

        // Retrieve tweet from intent
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        // CONNECT TWEET INFO TO VIEWS

        // User info
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText("@" + tweet.user.screenName);
        Glide.with(this)// Use glide to embed image
                .load(tweet.user.profileImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage);

        // Tweet info
        binding.tvBody.setText(tweet.body);
        binding.tvDate.setText(tweet.date);
        if(tweet.mediaUrl != null) {  // If there is media (photo, video, etc) show the CardView that contains de IV; if there isn't hide the whole CV
            binding.cardImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tweet.mediaUrl)
                    .transform(new RoundedCornersTransformation(30,0))
                    .into(binding.ivMedia);
        } else {
            binding.cardImage.setVisibility(View.GONE); // if there is no media, remove the CV
        }

        // Metrics Interaction

        // If the tweet has been already liked or retweeted by the user, change the button icon
        if(tweet.favorited) { binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart); }
        if(tweet.retweeted) { binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet); }

        binding.tvRtCount.setText(tweet.rtCount);
        binding.tvFavCount.setText(tweet.favCount);

        // REPLY button listener
        binding.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i = new Intent(TweetDetailActivity.this, ReplyActivity.class);
                    i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                    startActivity(i);
            }
        });

        // RETWEET button listener
        binding.btnRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If it has been retweeted by the user, call unretweet method from the client
                if(tweet.retweeted) {
                    client.postUnretweet(tweet.id, new JsonHttpResponseHandler() { // destroys retweet
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successfully retweeted the tweet");
                            binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke); // change bg to regular stroke
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure uretweeting: " + response, throwable); // send message if there is a failure unretweeting
                        }
                    });
                    // TODO: manage retweeted (how to change the actual) tweet
                    tweet.retweeted = false;
                }

                // If it has not been retweeted, use the client's retweet method
                else {
                    client.postRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successfully retweeted the tweet");
                            binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure retweeting: " + response, throwable);
                        }
                    });
                    tweet.retweeted = true;
                }
            }
        });

        // FAVORITE button listener
        binding.btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If it has been favorited, when the user clicks the button it unlikes the tweet and changes the button background
                if(tweet.favorited) {
                    client.postFavorites(tweet.id, "destroy", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successfully unliked the tweet");
                            binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure unliking the tweet");
                        }
                    });
                    // TODO: manage favorited change
                    tweet.favorited = false;
                }

                // If it has not been favorited, likes the tweet and changes the button background to a complete heart
                else {
                    client.postFavorites(tweet.id, "create", new JsonHttpResponseHandler() { // call client with the create action
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successfully liked the tweet");
                            binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure liking the tweet");
                        }
                    });
                    // TODO: manage favorited change
                    tweet.favorited = true;
                }
            }
        });
    }

//    @Override
//    public void onBackPressed(){
//        Intent i = new Intent(TweetDetailActivity.this, TimelineActivity.class);
//        i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
//        i.putExtra("Position", POSITION);
//        setResult(RESULT_OK, i);
//        finish();
//    }
}