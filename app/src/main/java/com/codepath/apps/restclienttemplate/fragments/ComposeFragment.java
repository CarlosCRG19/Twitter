package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import okhttp3.Headers;

public class ComposeFragment extends DialogFragment {

    public static final String TAG="ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    EditText etCompose;
    TextInputLayout ilCharCount;
    Button btnCompose;

    TwitterClient client;

    public interface  ComposeFragmentListener {
        void onFinishCompose(Tweet tweet);
    }

    // Empty constructor required for DialogFragment
    public ComposeFragment() {}

    public static ComposeFragment newInstance() {
        ComposeFragment frag = new ComposeFragment();
        return frag;
    }

    // Mandatory methods to create the fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.activity_compose, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = TwitterApp.getRestClient(getActivity());

        etCompose = view.findViewById(R.id.etCompose);
        btnCompose = view.findViewById(R.id.btnCompose);

        ilCharCount = view.findViewById(R.id.inputLayout);
        ilCharCount.setCounterMaxLength(MAX_TWEET_LENGTH);

        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetContent = etCompose.getText().toString();
                if(tweetContent.isEmpty()){
                    Toast.makeText(getActivity(), "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                } else if(tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getActivity(), "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getContext(), tweetContent, Toast.LENGTH_LONG).show();
                client.postTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            ComposeFragmentListener listener = (ComposeFragmentListener) getActivity();
                            listener.onFinishCompose(tweet);
                            dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
            }
        });
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

}
