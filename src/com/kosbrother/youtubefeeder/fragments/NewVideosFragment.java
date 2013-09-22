package com.kosbrother.youtubefeeder.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.kosbrother.youtubefeeder.PlayerViewActivity;
import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.taiwan.imageload.ListNothingAdapter;
import com.taiwan.imageload.ListVideoAdapter;
import com.youtube.music.channels.entity.YoutubeVideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public final class NewVideosFragment extends Fragment {
    
	private ArrayList<YoutubeVideo> videos = new ArrayList<YoutubeVideo>();
//	private static String myChannelName;
	private static int myPage = 0;
//	private static ArrayList<MyYoutubeVideo> myVideos;
	private static String mChannelId;
	private LoadMoreListView  myList;
	private ArrayList<YoutubeVideo> moreVideos;
	private Boolean checkLoad = true;
	private LinearLayout progressLayout;
	private static ListVideoAdapter myListAdapter;
	private static Activity mActivity;
	
	private static boolean mModeIsShowing = false;
	private static ActionMode mMode;
	@SuppressLint("NewApi")
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
//			mode.setTitle("Demo");				
			mActivity.getMenuInflater().inflate(R.menu.context_menu, menu);
			return true;
		}
		
		/** This is called when an item in the context menu is selected */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){
				case R.id.action1:
					ArrayList<String> videoKeys = new ArrayList<String>();
					ArrayList<String> videoValues = new ArrayList<String>();
					HashMap<String, String> map = myListAdapter.getMap();
					for (HashMap.Entry<String, String> entry : map.entrySet()) {
					    // use "entry.getKey()" and "entry.getValue()"
						videoKeys.add(entry.getKey());
						videoValues.add(entry.getValue());						
					}
					Intent intent = new Intent(mActivity, PlayerViewActivity.class);  
		    		intent.putStringArrayListExtra(PlayerViewActivity.Videos_Key, videoKeys);
		    		intent.putStringArrayListExtra(PlayerViewActivity.Videos_Title_Key, videoValues);
		    		mActivity.startActivity(intent);  
					
//					Toast.makeText(mActivity.getBaseContext(), "Selected Action1 ", Toast.LENGTH_LONG).show();
					mode.finish();	// Automatically exists the action mode, when the user selects this action
					break;						
			}
			return false;
		}
	};
	
	
    public static NewVideosFragment newInstance(String channelId, int page, Context mContext) {     
   	 
  	  myPage = page;
//  	  mChannelId = channelId;
  	  NewVideosFragment fragment = new NewVideosFragment();
  	  Bundle bdl = new Bundle();
  	  bdl.putString("channelId", channelId);
  	  fragment.setArguments(bdl);
  	  mActivity = (Activity) mContext; 
      return fragment;
        
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getArguments().getString("channelId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	View myFragmentView = inflater.inflate(R.layout.loadmore, container, false);
    	progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
    	myList = (LoadMoreListView) myFragmentView.findViewById(R.id.news_list);
        myList.setOnLoadMoreListener(new OnLoadMoreListener() {
			public void onLoadMore() {
				// Do the work to load more items at the end of list
				
				if(checkLoad){
					myPage = myPage +1;
					new LoadMoreTask().execute();
				}else{
					myList.onLoadMoreComplete();
				}
			}
		});
        
        if (myListAdapter != null) {
            progressLayout.setVisibility(View.GONE);
            myList.setAdapter(myListAdapter);
        } else {
            new DownloadChannelsTask().execute();
        }
        
        return myFragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       
    }
    
    @SuppressLint("NewApi")
	public static void showActionMode() {
		if (!mModeIsShowing){
			mMode = mActivity.startActionMode(modeCallBack);
			mModeIsShowing = true;
		}
	}
    
    private class DownloadChannelsTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            

        }

        @Override
        protected Object doInBackground(Object... params) {
            // TODO Auto-generated method stub

      	  videos = ChannelApi.getChannelVideo(mChannelId, myPage, "");     

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progressLayout.setVisibility(View.GONE);
            
            if(videos !=null){
          	  try{
          		  myListAdapter = new ListVideoAdapter(getActivity(), videos);
  		          myList.setAdapter(myListAdapter);
          	  }catch(Exception e){
          		 
          	  }
            }else{
          	  ListNothingAdapter nothingAdapter = new ListNothingAdapter(getActivity());
          	  myList.setAdapter(nothingAdapter);
            }

        }
    }
    
    
    private class LoadMoreTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            

        }

        @Override
        protected Object doInBackground(Object... params) {
            // TODO Auto-generated method stub

        	moreVideos = ChannelApi.getChannelVideo(mChannelId, myPage, "");   
        	if(moreVideos!= null){
	        	for(int i=0; i<moreVideos.size();i++){
	            	videos.add(moreVideos.get(i));
	            }
        	}
        	
        	
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            
            if(moreVideos!= null){
            	myListAdapter.notifyDataSetChanged();	                
            }else{
                checkLoad= false;
                Toast.makeText(getActivity(), "no more data", Toast.LENGTH_SHORT).show();            	
            }       
          	myList.onLoadMoreComplete();
          	
          	
        }
    }
    
}
