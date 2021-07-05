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
    public int POSITION;

    // Static variables
    public static String TAG = "ReplyActivity"; // Tag for log messages

    // Member variables
    Tweet tweet; // tweet received from intent
    TwitterClient client; // Create an instance of a Twitter client with the context of this activity

    // View binding implementation to reduce code
    ActivityTweetDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        POSITION = getIntent().getIntExtra("Position", 42);

        // Inflate via view binding
        binding = ActivityTweetDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Assign twitter client
        client = new TwitterClient(this);

        // Retrieve tweet from intent
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        // CONNECT TWEET INFO TO VIEWS
        populateViews();
        setBtnListeners();
    }

    private void populateViews() {

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
    }

    // Assigns a listener to each button
    private void setBtnListeners() {

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
                            tweet.subRt();
                            binding.tvRtCount.setText(tweet.rtCount);
                            tweet.retweeted = false;
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure uretweeting: " + response, throwable); // send message if there is a failure unretweeting
                        }
                    });
                }

                // If it has not been retweeted, use the client's retweet method
                else {
                    client.postRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successfully retweeted the tweet");
                            binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                            tweet.sumRt();
                            binding.tvRtCount.setText(tweet.rtCount);
                            tweet.retweeted = true;
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure retweeting: " + response, throwable);
                        }
                    });
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
                            tweet.subFav();
                            binding.tvFavCount.setText(tweet.favCount);
                            tweet.favorited = false;
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure unliking the tweet");
                        }
                    });
                }

                // If it has not been favorited, likes the tweet and changes the button background to a complete heart
                else {
                    client.postFavorites(tweet.id, "create", new JsonHttpResponseHandler() { // call client with the create action
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Successfully liked the tweet");
                            binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
                            tweet.sumFav();
                            binding.tvFavCount.setText(tweet.favCount);
                            tweet.favorited = true;
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failure liking the tweet");
                        }
                    });
                }
            }
        });
    }

    // When the user press to go back, the result of the activity is changed to OK and an intent passes the modified tweet info to TimelineActivity
    // so changes can persist in the main timeline
    @Override
    public void onBackPressed(){
        // Connect two activities through intents
        Intent i = new Intent(TweetDetailActivity.this, TimelineActivity.class);
        // Pass the tweet with Parcels
        i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
        // Pass the position of the tweet
        i.putExtra("Position", POSITION);
        // Set result
        setResult(RESULT_OK, i);
        finish();
    }


}