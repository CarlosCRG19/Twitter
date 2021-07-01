package com.codepath.apps.restclienttemplate.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    String TAG = "ReplyActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    Tweet tweet;
    ActivityTweetDetailBinding binding;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTweetDetailBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        setContentView(view);

        client = new TwitterClient(this);
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        binding.tvScreenName.setText(tweet.user.screenName);
        binding.tvBody.setText(tweet.body);
        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage);

        binding.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i = new Intent(TweetDetailActivity.this, ReplyActivity.class);
                    i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                    startActivity(i);
            }
        });

        binding.btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tweet.favorited) {
                    client.postFavorites(tweet.id, "destroy", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("Favorites", "successfully liked the tweet");
                            binding.tvFavCount.setText(String.valueOf(tweet.favCount));
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("Favorites", "failure liking the tweet");
                        }
                    });
                    tweet.favorited = false;
                    binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
                } else {
                    client.postFavorites(tweet.id, "create", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("Favorites", "successfully liked the tweet");
                            tweet.favCount += 1;
                            binding.tvFavCount.setText(String.valueOf(tweet.favCount));
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("Favorites", "failure liking the tweet");
                        }
                    });
                    tweet.favorited = true;
                    binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
                }
            }
        });

        binding.btnRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tweet.retweeted) {
                    client.postUnretweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("Retweet", "successfully retweeted the tweet");
                            binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
                            tweet.rtCount += 1;
                            binding.tvRtCount.setText(tweet.rtCount);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("Retweet", "failure retweeting", throwable);
                        }
                    });
                    tweet.retweeted = false;
                } else {
                    client.postRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("Unretweet", "successfully retweeted the tweet");
                            binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                            binding.tvRtCount.setText(tweet.rtCount);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("Unretweet", "failure retweeting", throwable);
                        }
                    });
                    tweet.retweeted = true;
                }
            }
        });

        if(tweet.mediaUrl != null) {
            Log.i("Media", tweet.mediaUrl);
            binding.cardImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tweet.mediaUrl)
                    .transform(new RoundedCornersTransformation(30,0))
                    .into(binding.ivMedia);
        } else {
            Log.i("Media", "No media");
            binding.cardImage.setVisibility(View.GONE);
        }

        if(tweet.favorited) {
            binding.btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
        }
        if(tweet.retweeted) {
            binding.btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
        }
        binding.tvName.setText(tweet.user.name);
        binding.tvBody.setText(tweet.body);
        binding.tvScreenName.setText(" @" + tweet.user.screenName);
        binding.tvDate.setText(tweet.date);
        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage);
        binding.tvRtCount.setText(tweet.rtCount);
        binding.tvFavCount.setText(tweet.favCount);
    }

}