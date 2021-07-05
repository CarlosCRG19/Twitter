package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import okhttp3.Headers;

// NOTE: Fragments are reusable portions of the app's UI. They cannot live on their own and are rather hosted by an activity (in this case, TimelineActivity)
// They have their own layout, lifecycle and input events.
public class ComposeFragment extends DialogFragment {

    // STATIC VARIABLES
    public static final String TAG="ComposeActivity"; // tag for log messages
    public static final int MAX_TWEET_LENGTH = 280; // Max number of chars in a tweet

    TwitterClient client; // Twitter client for requests

    ActivityComposeBinding binding; // View binding implementation

    // Interface to be used in the Activity that hosts the fragment
    public interface  ComposeFragmentListener {
        void onFinishCompose(Tweet tweet);
    }

    // Empty constructor required for DialogFragment
    public ComposeFragment() {}

    // Creates new fragment
    public static ComposeFragment newInstance() {
        return new ComposeFragment();
    }

    // Mandatory methods to create the fragment
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityComposeBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Assign a RestClient with the context of the host activity
        client = TwitterApp.getRestClient(getActivity());

        binding.inputLayout.setCounterMaxLength(MAX_TWEET_LENGTH); // set counter max number of characters allowed

        // Listener for the compose button
        binding.btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get content of the EditText
                String tweetContent = binding.etCompose.getText().toString();

                // Handle states of the content
                if(tweetContent.isEmpty()){
                    Toast.makeText(getActivity(), "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                } else if(tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getActivity(), "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                } else { // else represents that the content has an allowed length
                    Toast.makeText(getContext(), tweetContent, Toast.LENGTH_LONG).show();
                    // Make post request with client
                    client.postTweet(tweetContent, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to publish tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                ComposeFragmentListener listener = (ComposeFragmentListener) getActivity();
                                listener.onFinishCompose(tweet);
                                dismiss(); // finish the fragment action
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to publish tweet" + response, throwable);
                        }
                    });
                }
            }
        });
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

}
