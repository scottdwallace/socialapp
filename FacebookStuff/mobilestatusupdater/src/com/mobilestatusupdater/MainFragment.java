package com.mobilestatusupdater;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {

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
            	 Log.i(TAG, "Logged Put...");
                shareButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                        Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.activity_main, container, false);

                LoginButton authButton = (LoginButton) view
                                .findViewById(R.id.authButton);
                authButton.setFragment(this);
                //authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));
                shareButton = (Button) view.findViewById(R.id.shareButton);
                shareTxt = (EditText) view.findViewById(R.id.editText1);
                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	Log.v("EditText", shareTxt.getText().toString());
                        publishStory(shareTxt.getText().toString());        
                    }
                });
                if (savedInstanceState != null) {
                    pendingPublishReauthorization = 
                        savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
                }
                return view;
                
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
                //stParams.putString("name", "Status Updater");
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
                            Log.i(TAG,
                                "JSON error "+ e.getMessage());
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
                                     postId,
                                     Toast.LENGTH_LONG).show();
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


