/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kosbrother.youtubefeeder.api;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.kosbrother.youtubefeeder.RecommendChannelsActivity;
import com.youtube.music.channels.entity.Channel;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Display personalized greeting. This class contains boilerplate code to consume the token but
 * isn't integral to getting the tokens.
 */
public  class GetSuggestChannelsTask extends AsyncTask<Void, Void, Void>{
    private static final String TAG = "TokenInfoTask";
    private static final String NAME_KEY = "given_name";
    protected RecommendChannelsActivity mActivity;

    protected String mScope;
    protected String mEmail;
//    protected int mRequestCode;

    public GetSuggestChannelsTask(RecommendChannelsActivity activity, String email, String scope) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = email;
//        this.mRequestCode = requestCode;
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        fetchNameFromProfileServer();
      } catch (IOException ex) {
        onError("Following Error occured, please try again. " + ex.getMessage(), ex);
      } catch (JSONException e) {
        onError("Bad response: " + e.getMessage(), e);
      }
      return null;
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
          Log.e(TAG, "Exception: ", e);
        }
//        mActivity.show(msg);  // will be run in UI thread
    }

    /**
     * Get a authentication token if one is not available. If the error is not recoverable then
     * it displays the error message on parent activity.
     */
//    protected abstract String fetchToken() throws IOException;

    /**
     * Contacts the user info server to get the profile of the user and extracts the first name
     * of the user from the profile. In order to authenticate with the user info server the method
     * first fetches an access token from Google Play services.
     * @throws IOException if communication with user info server failed.
     * @throws JSONException if the response from the server could not be parsed.
     */
    private void fetchNameFromProfileServer() throws IOException, JSONException {
//        String token = fetchToken();
    	
    	String token = null;
    	
    	 try {
    		 token= GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
         } catch (GooglePlayServicesAvailabilityException playEx) {
             // GooglePlayServices.apk is either old, disabled, or not present.
             mActivity.showErrorDialog(playEx.getConnectionStatusCode());
         } catch (UserRecoverableAuthException userRecoverableException) {
             // Unable to authenticate, but the user can fix this.
             // Forward the user to the appropriate activity.
             mActivity.startActivityForResult(userRecoverableException.getIntent(), RecommendChannelsActivity.REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
         } catch (GoogleAuthException fatalException) {
             onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
         }
    	
    	
        if (token == null) {
          // error has already been handled in fetchToken()
          return;
        }
//        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
//        URL url = new URL("https://gdata.youtube.com/feeds/api/users/default/suggestion?type=channel&inline=true&access_token=" 
//        		+ token  + "&key=AIzaSyC6zd4TsN6RR5mJMR_O9srbzXS9OM2R1wg" + "&v=2&max-results=10"+"fields=entry(title,media:thumbnail,yt:channelId)");
        URL url = new URL("https://gdata.youtube.com/feeds/api/users/default/suggestion?type=channel&inline=true&access_token=" 
        		+ token  + "&key=AIzaSyC6zd4TsN6RR5mJMR_O9srbzXS9OM2R1wg" + "&v=2" + "&fields=entry(content(entry(title)))" + "&alt=json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();       
        int sc = con.getResponseCode();
        Log.i(TAG, con.getResponseMessage());
        if (sc == 200) {
          InputStream is = con.getInputStream();
          String response = readResponse(is);
          ArrayList<Channel> channels = getChannels(response);
//          String name = getFirstName(response);
//          mActivity.show("Hello " + name + "!");
          is.close();
          return;
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(mActivity, token);
            onError("Server auth error, please try again.", null);
            Log.i(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
            return;
        } else {
          onError("Server returned the following error code: " + sc, null);
          return;
        }
    }

    

	/**
     * Reads the response from the input stream and returns it as a string.
     */
    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    /**
     * Parses the response and returns the first name of the user.
     * @throws JSONException if the response is not JSON or if first name does not exist in response
     */
//    private String getFirstName(String jsonResponse) throws JSONException {
//      JSONObject profile = new JSONObject(jsonResponse);
//      return profile.getString(NAME_KEY);
//    }
    
    private ArrayList<Channel> getChannels(String response) {
		// TODO Auto-generated method stub
    	ArrayList<Channel> channelLists = new ArrayList<Channel>();
    	
    	try {
			JSONObject object = new JSONObject(response);
			JSONObject feedObject = object.getJSONObject("feed");
			JSONArray channelArray = feedObject.getJSONArray("entry");
			for (int i=0; i< channelArray.length(); i++){
				String title =  channelArray.getJSONObject(i).getJSONObject("content").getJSONArray("entry").getJSONObject(0).getJSONObject("title").getString("$t");
				String pic = channelArray.getJSONObject(i).getJSONObject("content").getJSONArray("entry").getJSONObject(0).getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
				String id  = channelArray.getJSONObject(i).getJSONObject("content").getJSONArray("entry").getJSONObject(0).getJSONArray("yt$channelId").getJSONObject(0).getString("$t");
				String videoCount =  channelArray.getJSONObject(i).getJSONObject("content").getJSONArray("entry").getJSONObject(0).getJSONArray("yt$channelStatistics").getJSONObject(0).getString("videoCount");
				channelLists.add(new Channel(
						id,
						title,
						pic,
						Integer.parseInt(videoCount)));
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	
    	
		return channelLists;
	}
}
