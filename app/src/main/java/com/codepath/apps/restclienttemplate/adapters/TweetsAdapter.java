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
import com.codepath.apps.restclienttemplate.ui.ReplyActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.ui.TweetDetailActivity;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    Context context;
    List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets){
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with the view holder
        try {
            holder.bind(tweet);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // For each row, inflate the layout

    // Bind values based on the position of the element

    // define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public boolean favoritesAction = true;

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvName, tvScreenName;
        TextView tvTimestamp;
        ImageView ivMedia;
        Button btnReply, btnFavorites, btnRetweet;
        CardView cvMedia;

        TwitterClient client = new TwitterClient(context);

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            ivMedia = (ImageView) itemView.findViewById(R.id.ivMedia);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            cvMedia = itemView.findViewById(R.id.cardImage);

            itemView.setOnClickListener(this);

            btnReply = itemView.findViewById(R.id.btnReply);
            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        Tweet tweet = tweets.get(position);
                        Intent i = new Intent(context, ReplyActivity.class);
                        i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                        context.startActivity(i);
                    }
                }
            });


            btnFavorites = itemView.findViewById(R.id.btnFavorites);

            btnFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);
                    if(tweet.favorited) {
                        client.postFavorites(tweet.id, "destroy", new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i("Favorites", "successfully liked the tweet");
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.i("Favorites", "failure liking the tweet");
                            }
                        });
                        tweet.favorited = false;
                        btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
                    } else {
                        client.postFavorites(tweet.id, "create", new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i("Favorites", "successfully liked the tweet");
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.i("Favorites", "failure liking the tweet");
                            }
                        });
                        tweet.favorited = true;
                        btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
                    }
                }
            });

            btnRetweet = itemView.findViewById(R.id.btnRetweet);
            btnRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);
                    if(tweet.retweeted) {
                        client.postRetweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i("Retweet", "successfully retweeted the tweet");
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.i("Retweet", "failure retweeting", throwable);
                            }
                        });
                        tweet.retweeted = false;
                        btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
                    } else {
                        client.postUnretweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i("Unretweet", "successfully retweeted the tweet");
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.i("Unretweet", "failure retweeting", throwable);
                            }
                        });
                        tweet.retweeted = false;
                        btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                    }
                }
            });


        }

        public void bind(Tweet tweet) throws JSONException {

            if(tweet.media != null) {
                Log.i("Media", tweet.media.getMediaUrl());
                cvMedia.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.media.getMediaUrl())
                        .transform(new RoundedCornersTransformation(30,0))
                        .into(ivMedia);
            } else {
                Log.i("Media", "No media");
                cvMedia.setVisibility(View.GONE);
            }

            if(tweet.favorited) {
                btnFavorites.setBackgroundResource(R.drawable.ic_vector_heart);
            }
            if(tweet.retweeted) {
                btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
            }
            tvName.setText(tweet.user.name);
            tvBody.setText(tweet.body);
            tvScreenName.setText(" @" + tweet.user.screenName);
            tvTimestamp.setText(tweet.timestamp);
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .circleCrop()
                    .into(ivProfileImage);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, TweetDetailActivity.class);
                i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(i);
            }
        }
    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }
}
