package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.ReplyActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.activities.TweetDetailActivity;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    public static String TAG = "Adapter";
    public static int REQUEST_CODE = 42;

    // Fields
    Context context; // handle to environment
    List<Tweet> tweets; // list of tweet objects

    // Constructor
    public TweetsAdapter(Context context, List<Tweet> tweets){
        this.context = context;
        this.tweets = tweets;
    }

    // MANDATORY METHODS FROM RecyclerView.Adapter

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get tweet at specific position
        Tweet tweet = tweets.get(position);
        // (Try to) Bind the tweet with the view holder
        try {
            holder.bind(tweet);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    } // Size of the tweets array

    // OTHER METHODS

    // Removes all the tweets from the adapter
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add an entire array of tweets
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // Inner View Holder Class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Layout views as member variables
        CardView cvMedia;
        ImageView ivProfileImage, ivMedia;
        Button btnReply, btnFavorites, btnRetweet;
        TextView tvName, tvScreenName, tvBody, tvTimestamp, tvRtCount, tvFavCount;

        // Create an instance of a Twitter client with the current context (from outer class)
        TwitterClient client = new TwitterClient(context);

        // Constructor
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            // View assignments
            tvName = itemView.findViewById(R.id.tvName); // User info
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);

            tvBody = itemView.findViewById(R.id.tvBody); // Tweet info
            cvMedia = itemView.findViewById(R.id.cardImage);
            ivMedia = (ImageView) itemView.findViewById(R.id.ivMedia);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

            btnReply = itemView.findViewById(R.id.btnReply); // Metrics
            tvRtCount = itemView.findViewById(R.id.tvRtCount);
            btnRetweet = itemView.findViewById(R.id.btnRetweet);
            tvFavCount = itemView.findViewById(R.id.tvFavCount);
            btnFavorites = itemView.findViewById(R.id.btnFavorites);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, TweetDetailActivity.class);
                i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
//                i.putExtra("Position", position);
                //((TimelineActivity) context).startActivityForResult(i, REQUEST_CODE);
                context.startActivity(i);
            }
        }

        // Connect data with VH
        public void bind(Tweet tweet) throws JSONException {

            // User info
            tvName.setText(tweet.user.name);
            tvScreenName.setText(" @" + tweet.user.screenName);
            Glide.with(context) // Use glide to embed image
                    .load(tweet.user.profileImageUrl)
                    .circleCrop()
                    .into(ivProfileImage);

            // Tweet info
            tvBody.setText(tweet.body);
            tvTimestamp.setText(tweet.timestamp);
            if(tweet.mediaUrl != null) { // If there is media (photo, video, etc) show the CardView that contains de IV; if there isn't hide the whole CV
                Log.i(TAG, "Media found: " + tweet.mediaUrl);
                cvMedia.setVisibility(View.VISIBLE);
                Glide.with(context) // Embed image with glide
                        .load(tweet.mediaUrl)
                        .transform(new RoundedCornersTransformation(30,0))
                        .into(ivMedia);
            } else {
                Log.i(TAG, "No media found" );
                cvMedia.setVisibility(View.GONE);
            }

            // Metrics Interaction

            // If the tweet has been already liked or retweeted by the user, change the button icon
            if(tweet.favorited) { btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart); }
            if(tweet.retweeted) { btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet); }

            tvRtCount.setText(tweet.rtCount);
            tvFavCount.setText(tweet.favCount);

            // REPLY button listener
            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        Tweet tweet = tweets.get(position);
                        Intent i = new Intent(context, ReplyActivity.class);
                        i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                        context.startActivity(i);  // if button is clicked start the reply activity
                    }
                }
            });

            // RETWEET button listener
            btnRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Find tweet according to adapter position
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);

                    // If it has been retweeted by the user, call unretweet method from the client
                    if(tweet.retweeted) {
                        client.postUnretweet(tweet.id, new JsonHttpResponseHandler() { // destroys retweet
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "Successfully retweeted the tweet");
                                btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke); // change bg to regular stroke
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
                                btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
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
            btnFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Get tweet according to the position in adapter
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);

                    // If it has been favorited, when the user clicks the button it unlikes the tweet and changes the button background
                    if(tweet.favorited) { // if
                        client.postFavorites(tweet.id, "destroy", new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "Successfully unliked the tweet");
                                btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
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
                                btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
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


    }
}
