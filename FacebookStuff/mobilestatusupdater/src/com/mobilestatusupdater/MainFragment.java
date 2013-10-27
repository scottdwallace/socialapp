package com.mobilestatusupdater;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.android.AndroidTwitterLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainFragment extends Fragment {


/*
    TWITTER STUFF*//*
*/
    static String TWITTER_CONSUMER_KEY = "xg2scPwSMn6oufPmynPPPg";
    static String TWITTER_CONSUMER_SECRET = "A7nxrmlttZYiqsPt8mfDwWZ0YNhR7CoZkD2M1RyHcE";

   // static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "1965418128-VQthy6SEwT1qKZFvd5Yhs7nClBUXA9Q2o8FNq10";
    static final String PREF_KEY_OAUTH_SECRET = "pXU6jVCCKU40wFtkDtPsWMZW7j8Ah3sIScuZML5VnP1WI";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";

    static final String TWITTER_CALLBACK_URL = "oauth://my-CallBack";

    static final String URL_TWITTER_AUTH = "http://api.twitter.com/oauth/request_token";
    static final String URL_TWITTER_OAUTH_VERIFIER = "http://api.twitter.com/oauth/authorize";
    static final String URL_TWITTER_OAUTH_TOKEN = "http://api.twitter.com/oauth/access_token";

    // Login button
    Button btnLoginTwitter;

    // Twitter
    private static Twitter twitter;
    //private static RequestToken requestToken;

    // Shared Preferences
    SharedPreferences prefs;
    AndroidTwitterLogin atl;



    //FACEBOOK STUFF

		private static final String TAG = "MainFragment";

        private UiLifecycleHelper uiHelper;
        private Button shareButton;
        private EditText shareTxt;
        private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
        private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
        private boolean pendingPublishReauthorization = false;

        private Session.StatusCallback callback = new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state,
                                Exception exception) {
                        onSessionStateChange(session, state, exception);
                }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            if (android.os.Build.VERSION.SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
                prefs = getActivity().getSharedPreferences("myPrefs", 0);
                uiHelper = new UiLifecycleHelper(getActivity(), callback);
                uiHelper.onCreate(savedInstanceState);


        }

        private void onSessionStateChange(Session session, SessionState state,
                        Exception exception) {

            if (state.isOpened()) {
            	 Log.i(TAG, "Logged in...");
                shareButton.setVisibility(View.VISIBLE);
                if (pendingPublishReauthorization && 
                        state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                    pendingPublishReauthorization = false;
                    publishStory(MainFragment.this.shareTxt.getText().toString());
                }

            } else if (state.isClosed()) {
            	 Log.i(TAG, "Logged Out...");
                shareButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                        Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.activity_main, container, false);
                LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
                btnLoginTwitter = (Button) view.findViewById(R.id.btnLoginTwitter);
                authButton.setFragment(this);
                authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));
                shareButton = (Button) view.findViewById(R.id.shareButton);
                shareTxt = (EditText) view.findViewById(R.id.editText1);

                btnLoginTwitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Log.v("Twitter", "Please");
   //                     loginWithTwitter();
                    }
                });
                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("EditText", shareTxt.getText().toString());
                        publishStory(shareTxt.getText().toString());
                        updateTwitterStatus(shareTxt.getText().toString());
                        shareTxt.setText("");
                    }
                });
                if (savedInstanceState != null) {
                    pendingPublishReauthorization = 
                        savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
                }
                return view;
                
        }

    private void updateTwitterStatus(String tweet) {
            String userToken = prefs.getString("PREF_KEY_OAUTH_TOKEN", "NotSet");
            String userSecret = prefs.getString("PREF_KEY_OAUTH_SECRET", "NotSet");
     /*   if(userToken.equals("NotSet") || userSecret.equals("NotSet"))
        {
            loginWithTwitter(tweet);
        }else
        {*/
            OAuthSignpostClient oauthClient =  new OAuthSignpostClient(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET ,PREF_KEY_OAUTH_TOKEN, PREF_KEY_OAUTH_SECRET);
            Twitter twitter = new Twitter(null , oauthClient);
            twitter.setStatus(tweet);
       // }
    }

    private void loginWithTwitter(String status) {
        final String tweetThis = status;
        Log.v("Twitter", "Really?");
        atl = new AndroidTwitterLogin(getActivity(),
                TWITTER_CONSUMER_KEY,TWITTER_CONSUMER_SECRET,TWITTER_CALLBACK_URL) {

            protected void onSuccess(Twitter jtwitter, String[] tokens) {
                jtwitter.setStatus(tweetThis);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("PREF_KEY_OAUTH_TOKEN", tokens[0]);
                editor.putString("PREF_KEY_OAUTH_SECERT", tokens[1]);
                editor.commit();
            }
        };
        atl.run();
    }
    private void loginWithTwitter() {
        //Log.v("Twitter", "Before ATL is set");
        atl = new AndroidTwitterLogin(getActivity(),
                TWITTER_CONSUMER_KEY,TWITTER_CONSUMER_SECRET,TWITTER_CALLBACK_URL) {

            protected void onSuccess(Twitter jtwitter, String[] tokens) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("PREF_KEY_OAUTH_TOKEN", tokens[0]);
                editor.putString("PREF_KEY_OAUTH_SECERT", tokens[1]);
                editor.commit();
                //Log.v("Twitter", "After Preff Commit");
            }
        };
        atl.run();
    }

    @Override
        public void onResume() {
                super.onResume();
                
                Session session = Session.getActiveSession();
            if (session != null &&
                   (session.isOpened() || session.isClosed()) ) {
                onSessionStateChange(session, session.getState(), null);
            }

            
                uiHelper.onResume();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                uiHelper.onActivityResult(requestCode, resultCode, data);
                
        }

        @Override
        public void onPause() {
                super.onPause();
                uiHelper.onPause();
        }

        @Override
        public void onDestroy() {
                super.onDestroy();
                uiHelper.onDestroy();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
        	 super.onSaveInstanceState(outState);
        	    outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
        	    uiHelper.onSaveInstanceState(outState);
        }
        
        private void publishStory(String shareText) {
            Session session = Session.getActiveSession();

            if (session != null){
            	
            	  // Check for publish permissions    
                List<String> permissions = session.getPermissions();
                if (!isSubsetOf(PERMISSIONS, permissions)) {
                    pendingPublishReauthorization = true;
                    Session.NewPermissionsRequest newPermissionsRequest = new Session
                            .NewPermissionsRequest(this, PERMISSIONS);
                session.requestNewPublishPermissions(newPermissionsRequest);
                    return;
                }

                Bundle postParams = new Bundle();
                postParams.putString("message", shareText);
       
                Log.v("About to Do the Request", shareText);
                Request.Callback callback= new Request.Callback() {
                    public void onCompleted(Response response) {
                        JSONObject graphResponse = response
                                                   .getGraphObject()
                                                   .getInnerJSONObject();
                        String postId = null;
                        try {
                            postId = graphResponse.getString("id");
                        } catch (JSONException e) {
                            Log.i(TAG, "JSON error "+ e.getMessage());
                        }
                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            Toast.makeText(getActivity()
                                 .getApplicationContext(),
                                 error.getErrorMessage(),
                                 Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity()
                                     .getApplicationContext(), 
                                     postId, Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                Request request = new Request(session, "me/feed", postParams, 
                                      HttpMethod.POST, callback);

                RequestAsyncTask task = new RequestAsyncTask(request);
                task.execute();
            }

        }

        private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
            for (String string : subset) {
                if (!superset.contains(string)) {
                    return false;
                }
            }
            return true;
        }
}

