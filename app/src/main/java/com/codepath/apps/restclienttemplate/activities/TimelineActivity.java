package com.codepath.apps.restclienttemplate.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.fragments.ComposeFragment;
import com.codepath.apps.restclienttemplate.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.ComposeFragmentListener {

    public static final String TAG = "TimelineActivity"; // TAG for log messages
    public final int REQUEST_CODE_DETAIL = 42; // Activity change identifier

    Long minId; // Member variable that defines the id from the oldest tweet

    TweetDao tweetDao; // Data Access Object

    List<Tweet> tweets; // Member variable to be accessed by inner classes

    TwitterClient client; // Twitter client that makes requests

    TweetsAdapter adapter; // binds the tweet data to a ViewHolder

    MenuItem miActionProgressItem; // Progress icon from menu

    ActivityTimelineBinding binding; // View binding implementation

    // Helpers
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    // MANDATORY METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create twitter client with the context of this activity
        client = TwitterApp.getRestClient(this);

        // Data access object
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        // View binding code
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Set new action bar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init the list of tweets
        tweets = new ArrayList<>();
        // Init adapter to bind data
        adapter = new TweetsAdapter(this, tweets);

        // RECYCLER VIEW SETUP
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); // rv manager (works like a linear layout)
        binding.rvTweets.setLayoutManager(linearLayoutManager);
        binding.rvTweets.setAdapter(adapter);
        binding.rvTweets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); // decoration at the bottom of each row

        // Query for existing tweets in DB
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from DB");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItem(); // CHECK THIS
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });

        // Populate main timeline (check method)
        populateHomeTimeline();

        // USER GENERAL INTERACTIONS (REFRESH AND LOAD MORE)

        // Refresh
        swipeContainer = (SwipeRefreshLayout) binding.swipeContainer; // Swipe container view that allows listening to swiping actions
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false); // ignore refresh icon
                minId = 0L; // if we set minId to 0, we'll populate with the latest tweets (refer to method on client)
                showProgressBar(); // change progress bar visibility
                populateHomeTimeline(); // fill home timeline again
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Load more
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                fetchHomeTimeline();
            }
        };

        binding.rvTweets.addOnScrollListener(scrollListener); // add listener to the recycler view

        // Tweet interactions
        binding.btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startComposeFragment();
            }
        });

    }

    // POPULATE METHODS

    // Method to fill timeline (but it refreshes it)
    private void populateHomeTimeline() {
        showProgressBar(); // as an async action is being run, the progressbar should be displayed
        client.getHomeTimeline(0, new JsonHttpResponseHandler(){ // client request
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    adapter.clear(); // Restart the adapter
                    tweets.clear();
                    tweets.addAll(tweetsFromNetwork);
                    adapter.notifyDataSetChanged();
                    // Async query to load tweets into the database
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            // insert users first
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            // insert tweets next
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                    hideProgressBar();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                    hideProgressBar();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure" + response, throwable);
            }
        });
    }

    // Method to add new tweets to the timeline
    private void fetchHomeTimeline() {
        showProgressBar(); // change progress bar visibility
        initializeMinId(); // set the minId values
        client.getHomeTimeline(minId, new JsonHttpResponseHandler(){ // overloaded method,

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    hideProgressBar(); // if it succeed on getting the tweets, hide the progress icon
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                    hideProgressBar();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure" + response, throwable);
            }
        });
    }

    // FRAGMENT METHODS

    // Runs the fragment on top of the activity
    private void startComposeFragment() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeFragment = ComposeFragment.newInstance();
        composeFragment.show(fm, "activity_compose");
    }

    @Override
    public void onFinishCompose(Tweet tweet) {
        // Update RV with the tweet
        tweets.add(0, tweet);
        // Update Adapter
        adapter.notifyItemInserted(0);
        binding.rvTweets.smoothScrollToPosition(0);
    }

    // MENU METHODS

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // inflate menu with layout
        miActionProgressItem = menu.findItem(R.id.miActionProgress); // Progress bar item
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnLogout) { // if the logout button is selected, run the logout method
            onLogoutButton();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Boolean methods to  change the visibility of the progress bar so it is not always being displayed

    public void showProgressBar() {
        // Show progress item
        if (miActionProgressItem != null){
            miActionProgressItem.setVisible(true);
        }
    }
    public void hideProgressBar() {
        // Hide progress item
        if (miActionProgressItem != null){
            miActionProgressItem.setVisible(false);
        }
    }

    // DETAIL METHODS

    // Catches the result of a detail activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {

        if(requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra(Tweet.class.getSimpleName())); // data represents the intent, which contains the tweet and its position in the RV
            int tweetPos = data.getExtras().getInt("Position");
            // change tweet in the model
            tweets.set(tweetPos, tweet);
            // notify changes in specific position
            adapter.notifyItemChanged(tweetPos);
            // return to modified tweet position
            binding.rvTweets.smoothScrollToPosition(tweetPos);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // OTHER METHODS

    // Clear the access token when clicked on logout button (which is part of the menu)
    public void onLogoutButton() {
        // forget who's logged in
        TwitterApp.getRestClient(this).clearAccessToken();

        // navigate backwards to Login screen
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // same as above
        startActivity(i);
    }
    // Assigns value to the minId, this is to define the oldest tweet on the timeline
    private void initializeMinId() {
        minId = Long.parseLong(tweets.get(0).id);
        for (int i=1; i < tweets.size(); i++) {
            minId = Math.min(minId, Long.parseLong(tweets.get(i).id));
        }
        minId -= 1;
    }

}