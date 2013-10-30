package com.kosbrother.youtubefeeder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.services.youtube.YouTubeScopes;
import com.taiwan.imageload.ListChannelAdapter;
import com.taiwan.imageload.LoadMoreGridView;
import com.taiwan.imageload.LoadMoreGridView.OnLoadMoreListener;
import com.youtube.music.channels.entity.Channel;


public class RecommendChannelsActivity extends Activity {
	
    private String mEmail;
    private static final String SCOPE = "oauth2:" + YouTubeScopes.YOUTUBE;
    
    private static final String TAG = "MainActivity";
    
    public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    
    private LinearLayout progressLayout;
    private LoadMoreGridView myGrid;
    private static ListChannelAdapter myListAdapter;
    private ArrayList<Channel> recommendChannels = new ArrayList<Channel>();
    
    private int myPage = 0;
    private Boolean isLoadingMore = false;
    private Boolean canLoadMore = true;
    
    private LinearLayout loadmoreLayout;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_recommend_channels);
		
		progressLayout = (LinearLayout) findViewById (R.id.layout_progress);
		myGrid = (LoadMoreGridView) findViewById(R.id.news_list);
		loadmoreLayout = (LinearLayout) findViewById(R.id.load_more_grid);
	
		mEmail = loadAccount();
		new GetSuggestChannelsTask().execute();
		MainActivity.isRefreshList = true;
		
		myGrid.setOnLoadMoreListener(new OnLoadMoreListener() {
			public void onLoadMore() {
				// Do the work to load more items at the end of list
				if(!isLoadingMore && canLoadMore){
					isLoadingMore = true;
					myPage = myPage +1;
					new GetSuggestChannelsTask().execute();
				}else{
					myGrid.onLoadMoreComplete();
				}
			}
		});
		
		int sdkVersion = android.os.Build.VERSION.SDK_INT; 
        if(sdkVersion > 10){
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

	    int itemId = item.getItemId();
	    switch (itemId) {
	    case android.R.id.home:
	        finish();
	        break;
	    }
	    return true;
	}
	
	
	public void showErrorDialog(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Dialog d = GooglePlayServicesUtil.getErrorDialog(
                  code,
                  RecommendChannelsActivity.this,
                  REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
              d.show();
            }
        });
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
//            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Retrying");
//            getTask(this, mEmail, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
//            show("User rejected authorization.");
            return;
        }
//        show("Unknown error, click the button again");
    }
	
	private String loadAccount() {
		String mChosenAccountName = "";
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mChosenAccountName = sp.getString(MainActivity.ACCOUNT_KEY, null);
		return mChosenAccountName;
	}
	
	
	public  class GetSuggestChannelsTask extends AsyncTask{
	    private static final String TAG = "TokenInfoTask";
	    
	    @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
        	 super.onPreExecute();
        	 if (!isLoadingMore){
        	   progressLayout.setVisibility(View.VISIBLE);
        	 }else{
        	    loadmoreLayout.setVisibility(View.VISIBLE);
        	 }
        }
	    
	    @Override
	    protected Void doInBackground(Object... params) {
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
//	        mActivity.show(msg);  // will be run in UI thread
	    }
	    
	    @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(isLoadingMore){
            	isLoadingMore = false;
            	loadmoreLayout.setVisibility(View.GONE);
            	myListAdapter.notifyDataSetChanged();
            	myGrid.onLoadMoreComplete();
            	if(!canLoadMore){
            		Toast.makeText(RecommendChannelsActivity.this, "No More Data!", Toast.LENGTH_SHORT).show();
            	}
            }else{
            	progressLayout.setVisibility(View.GONE);
            	myListAdapter = new ListChannelAdapter(RecommendChannelsActivity.this, recommendChannels);
                myGrid.setAdapter(myListAdapter);
            }           
            
          	
        }
	    
	    private void fetchNameFromProfileServer() throws IOException, JSONException {
//	        String token = fetchToken();
	    	
	    	String token = null;
	    	
	    	 try {
	    		 token= GoogleAuthUtil.getToken(RecommendChannelsActivity.this, mEmail, SCOPE);
//	    		 token = credential.getToken();
	         } catch (GooglePlayServicesAvailabilityException playEx) {
	             // GooglePlayServices.apk is either old, disabled, or not present.
	        	 RecommendChannelsActivity.this.showErrorDialog(playEx.getConnectionStatusCode());
	         } catch (UserRecoverableAuthException userRecoverableException) {
	             // Unable to authenticate, but the user can fix this.
	             // Forward the user to the appropriate activity.
	        	 RecommendChannelsActivity.this.startActivityForResult(userRecoverableException.getIntent(), RecommendChannelsActivity.REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
	         } catch (GoogleAuthException fatalException) {
	             onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
	         }
	    	
	    	
	        if (token == null) {
	          // error has already been handled in fetchToken()
	          return;
	        }

	        URL url = new URL("https://gdata.youtube.com/feeds/api/users/default/suggestion?type=channel&inline=true&access_token=" 
	        		+ token  + "&key=AIzaSyC6zd4TsN6RR5mJMR_O9srbzXS9OM2R1wg" + "&v=2" + "&fields=entry(content(entry(title)))" + "&alt=json&start-index=" + (myPage * 15 + 1) + "&max-results=15" );
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();       
	        int sc = con.getResponseCode();
	        Log.i(TAG, con.getResponseMessage());
	        if (sc == 200) {
	          InputStream is = con.getInputStream();
	          String response = readResponse(is);
	          ArrayList<Channel> moreChannels= getChannels(response);
	          if(moreChannels.size() == 0){
	        	  canLoadMore = false;
	          }else{
	        	  recommendChannels.addAll(moreChannels);
	          }
//	          String name = getFirstName(response);
//	          mActivity.show("Hello " + name + "!");
	          is.close();
	          return;
	        } else if (sc == 401) {
	        	canLoadMore = false;
	            GoogleAuthUtil.invalidateToken(RecommendChannelsActivity.this, token);
	            onError("Server auth error, please try again.", null);
	            Log.i(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
	            return;
	        } else {
	        	canLoadMore = false;
	        	onError("Server returned the following error code: " + sc, null);
	          return;
	        }
	    }

	    

		/**
	     * Reads the response from the input stream and returns it as a string.
	     */
	    private String readResponse(InputStream is) throws IOException {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] data = new byte[2048];
	        int len = 0;
	        while ((len = is.read(data, 0, data.length)) >= 0) {
	            bos.write(data, 0, len);
	        }
	        return new String(bos.toByteArray(), "UTF-8");
	    }
	    
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
	
}
