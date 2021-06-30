package com.codepath.apps.restclienttemplate.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    public final int REQUEST_CODE = 20;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    Button btnCompose;

    Long max_id;
    private EndlessRecyclerViewScrollListener scrollListener;

    private SwipeRefreshLayout swipeContainer;
    MenuItem miActionProgressItem;

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        client = TwitterApp.getRestClient(this);

        // Find the rv
        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        // Init the list of tweets
        tweets = new ArrayList<Tweet>();
        adapter = new TweetsAdapter(this, tweets);
        // Recycler view setup: layout manager and the adapter

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);
        rvTweets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        populateHomeTimeline();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                max_id = 0L;
                populateHomeTimeline();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        btnCompose = findViewById(R.id.btnCompose);
        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                fetchHomeTimeline();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(0, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.clear();
                    tweets.clear();
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    hideProgressBar();
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure" + response, throwable);
            }
        });
    }

    private void fetchHomeTimeline() {
        initializeMaxId();
        client.getHomeTimeline(max_id, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    hideProgressBar();
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure" + response, throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnLogout) {
            onLogoutButton();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from intent (tweet object)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update RV with the tweet
            tweets.add(0, tweet);
            // Update Adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onLogoutButton() {
        client.clearAccessToken();
        finish();
    }

    private void initializeMaxId() {
        max_id = Long.parseLong(tweets.get(0).id);
        for (int i=1; i < tweets.size(); i++) {
            max_id = Math.min(max_id, Long.parseLong(tweets.get(i).id));
        }
        max_id -= 1;
    }
}