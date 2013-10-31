package com.kosbrother.youtubefeeder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.taiwan.imageload.ListChannelManageAdapter;
import com.taiwan.imageload.LoadMoreGridView;
import com.youtube.music.channels.entity.SubscribeChannel;

public class ManageChannelsActivity extends Activity {
	
    private String mEmail;
    
    private static final String TAG = "ManageChannelsActivity";
    
    GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();
    
	private static ArrayList<SubscribeChannel> mSubscriptionChannels = new ArrayList<SubscribeChannel>();
	private LinearLayout progressLayout;
    private LoadMoreGridView myGrid;
    private static ListChannelManageAdapter myListAdapter;
	
    private static Activity mActivity;
    private static final int MENU_ID_UNSUBSCRIBE = 333;
    
    private static YouTube youtube;
    private static ArrayList<String> unSubscribeIds = new ArrayList<String>();
    private static boolean isUnSubOK = true;
    
    private static ProgressDialog mProgressDialog;
    
    private static boolean mModeIsShowing = false;
	private static ActionMode mMode;
    private static ActionMode.Callback modeCallBack = new ActionMode.Callback() {
    	
    	/** Invoked whenever the action mode is shown. This is invoked immediately after onCreateActionMode */ 
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {		
			return false;
		}
		
		
		/** Called when user exits action mode */			
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			mModeIsShowing = false;
		}
		
		/** This is called when the action mode is created. This is called by startActionMode() */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(0, MENU_ID_UNSUBSCRIBE, 0, mActivity.getResources().getString(R.string.menu_unsubscribe)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}
		
		/** This is called when an item in the context menu is selected */
		@Override
		@SuppressWarnings("static-access")
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){
				case MENU_ID_UNSUBSCRIBE:
					unSubscribeIds.clear();
					HashMap<String, String> map2 = myListAdapter.getMap();
					if (map2.size()!=0){
						for (HashMap.Entry<String, String> entry : map2.entrySet()) {
							// unsubscribe
							unSubscribeIds.add(entry.getValue());							
						}
						MainActivity.isRefreshList = true;
						new unSubscribeTask().execute();
					}
					mode.finish();	
					break;
			}
			return false;
		}
	};
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_recommend_channels);
		mActivity = this;
		isUnSubOK = true;
		
		setTitle(getResources().getString(R.string.title_manage));
		
		mSubscriptionChannels.clear();
		
		progressLayout = (LinearLayout) findViewById (R.id.layout_progress);
		myGrid = (LoadMoreGridView) findViewById(R.id.news_list);
		
		//1. get all channels
		//2. set on the list view
		//3. add unScubscribe button
		
		mEmail = loadAccount();
		
		credential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(Auth.SCOPES));
		// set exponential backoff policy
		credential.setBackOff(new ExponentialBackOff());
		credential.setSelectedAccountName(mEmail);
		
		new GetSuggestChannelsTask().execute();
		
//		MainActivity.isRefreshList = true;
		
		int sdkVersion = android.os.Build.VERSION.SDK_INT; 
        if(sdkVersion > 10){
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
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
	

	
	private String loadAccount() {
		String mChosenAccountName = "";
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mChosenAccountName = sp.getString(MainActivity.ACCOUNT_KEY, null);
		return mChosenAccountName;
	}
	
	
	public static class unSubscribeTask extends AsyncTask{
		@Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
        	 super.onPreExecute();
        	 mProgressDialog = ProgressDialog.show(mActivity, null, "UnSubscribing...");
        	 mProgressDialog.setCancelable(true);
        }
		
		@Override
	    protected Void doInBackground(Object... params) {			
			
			try {
				for(String id : unSubscribeIds){
					youtube.subscriptions().delete(id).execute();
					isUnSubOK = true;
					for (int i=0; i< mSubscriptionChannels.size(); i++){
						if (mSubscriptionChannels.get(i).getSubId() == id){
							mSubscriptionChannels.remove(i);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isUnSubOK = false;
			}
			return null;
		}
		
		@Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            if (isUnSubOK){
            	Toast.makeText(mActivity, "UnSubscribe Success!", Toast.LENGTH_SHORT).show();
            	myListAdapter.notifyDataSetChanged();
            	myListAdapter.clearCheckMap();
            }else{
            	Toast.makeText(mActivity, "UnSubscribe Failed!", Toast.LENGTH_SHORT).show();
            }
          	
        }
		
		
	}
	
	
	public  class GetSuggestChannelsTask extends AsyncTask{

		@Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
        	 super.onPreExecute();
        	 progressLayout.setVisibility(View.VISIBLE);

        }
		
		
	    @Override
	    protected Void doInBackground(Object... params) {
	     
	    youtube = new YouTube.Builder(transport, jsonFactory,
					credential).setApplicationName(Constants.APP_NAME)
					.build();
	    
	    try {
			SubscriptionListResponse mSubscriptions = youtube
					.subscriptions().list("snippet,contentDetails").setMine(true).setMaxResults((long) 20).execute();
			
			List<Subscription> lists = mSubscriptions.getItems();
			
			// if channel size more than 20, keep loading
			boolean isKeepLoadChannel = false;
			String nextToken = "";
			if (lists.size()==20){
				isKeepLoadChannel = true;
				nextToken = mSubscriptions.getNextPageToken();
			}						
			while (isKeepLoadChannel){						
				SubscriptionListResponse moreSubscriptions = youtube
						.subscriptions().list("snippet,contentDetails").setMine(true).setMaxResults((long) 20).setPageToken(nextToken).execute();
				List<Subscription> moreList = moreSubscriptions.getItems();
				for(Subscription item: moreList){
					lists.add(item);
				}
				if (moreList.size()==20){
					isKeepLoadChannel = true;
					nextToken = moreSubscriptions.getNextPageToken();
				}else{
					isKeepLoadChannel = false;
				}
			}
			
			for (Subscription item : lists){
				
				String subId = item.getId();
				String channelId = item.getSnippet().getResourceId().getChannelId();
				String channelTitle = item.getSnippet().getTitle();
				String channelPicUrl = item.getSnippet().getThumbnails().getDefault().getUrl();
				int channelTotalNums = 0;
				try{
					channelTotalNums = item.getContentDetails().getTotalItemCount().intValue();
				}catch(Exception e){
					Log.e("MainActivity", channelTitle + "no total item count");
				}
				
				SubscribeChannel subChannel = new SubscribeChannel(channelId,
						channelTitle,
						channelPicUrl,
						channelTotalNums,
						subId
						);
				
				mSubscriptionChannels.add(subChannel);
											
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    	
	      return null;
	    }

	    
	    @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progressLayout.setVisibility(View.GONE);
            myListAdapter = new ListChannelManageAdapter(ManageChannelsActivity.this, mSubscriptionChannels);
            myGrid.setAdapter(myListAdapter);
          	
        }
	}
	
	public static void showActionMode() {
		if (!mModeIsShowing){
			mMode = mActivity.startActionMode(modeCallBack);
			mModeIsShowing = true;
		}
	}

}
