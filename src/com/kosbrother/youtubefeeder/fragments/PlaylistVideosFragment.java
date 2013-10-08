package com.kosbrother.youtubefeeder.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.kosbrother.youtubefeeder.PlayerViewActivity;
import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.taiwan.imageload.ListNothingAdapter;
import com.taiwan.imageload.ListVideoAdapter;
import com.taiwan.imageload.LoadMoreGridView;
import com.youtube.music.channels.entity.YoutubeVideo;

public class PlaylistVideosFragment extends Fragment {
    
	private static ArrayList<YoutubeVideo> videos = new ArrayList<YoutubeVideo>();
//	private static String myChannelName;
	private static int myPage = 0;
//	private static ArrayList<MyYoutubeVideo> myVideos;
	private static String mListId;
	private static String mChannelName;
	private ArrayList<YoutubeVideo> moreVideos;
	private Boolean checkLoad = true;
	private static ListVideoAdapter myListAdapter;
	private static Activity mActivity;
	private static Boolean isFirst = true;
	
	private LoadMoreGridView  myGrid;
	private LinearLayout progressLayout;
	private LinearLayout    loadmoreLayout;
	private LinearLayout 	nodataLayout;
	
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
					HashMap<String, String> map = myListAdapter.getMap();
					if (map.size()!=0){
						ArrayList<String> videoKeys = new ArrayList<String>();
						ArrayList<String> videoValues = new ArrayList<String>();
						
						for (HashMap.Entry<String, String> entry : map.entrySet()) {
						    // use "entry.getKey()" and "entry.getValue()"
							videoKeys.add(entry.getKey());
							videoValues.add(entry.getValue());						
						}
						Intent intent = new Intent(mActivity, PlayerViewActivity.class);  
			    		intent.putStringArrayListExtra(PlayerViewActivity.Videos_Key, videoKeys);
			    		intent.putStringArrayListExtra(PlayerViewActivity.Videos_Title_Key, videoValues);
			    		mActivity.startActivity(intent);  
					}
					mode.finish();
					break;						
			}
			return false;
		}
	};
	
	
    public static PlaylistVideosFragment newInstance(String listId, int page, Activity theActivity,String channelName) {     
   	 
  	  myPage = page;
//  	  mChannelId = channelId;
  	  PlaylistVideosFragment fragment = new PlaylistVideosFragment();
  	  Bundle bdl = new Bundle();
  	  bdl.putString("listId", listId);
  	  fragment.setArguments(bdl);
  	  mActivity = theActivity;
  	  isFirst = true;
  	  mChannelName = channelName;
  	  if (videos!=null){
  		  videos.clear();
  	  }
      return fragment;
        
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListId = getArguments().getString("listId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
    	progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
    	myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);
    	loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
    	nodataLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_no_data);
    	myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
			public void onLoadMore() {
				// Do the work to load more items at the end of list
				
				if(checkLoad){
					myPage = myPage +1;
					new LoadMoreTask().execute();
				}else{
					myGrid.onLoadMoreComplete();
				}
			}
		});
        
        if (myListAdapter != null && !isFirst) {
            progressLayout.setVisibility(View.GONE);
            loadmoreLayout.setVisibility(View.GONE);
            myGrid.setAdapter(myListAdapter);
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

      	  videos = ChannelApi.getPlaylistVideos(mListId, myPage);     

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progressLayout.setVisibility(View.GONE);
            loadmoreLayout.setVisibility(View.GONE);
            isFirst = false;
            
            if(videos !=null){
          	  try{
          		  myListAdapter = new ListVideoAdapter(getActivity(), videos, mChannelName);
          		  myGrid.setAdapter(myListAdapter);
          	  }catch(Exception e){
          		 
          	  }
            }else{
            	nodataLayout.setVisibility(View.VISIBLE);
          	  	myGrid.setVisibility(View.GONE);
            }

        }
    }
    
    
    private class LoadMoreTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            loadmoreLayout.setVisibility(View.VISIBLE);

        }

        @Override
        protected Object doInBackground(Object... params) {
            // TODO Auto-generated method stub

        	moreVideos = ChannelApi.getPlaylistVideos(mListId, myPage);       
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
            loadmoreLayout.setVisibility(View.GONE);
            
            if(moreVideos!= null){
            	myListAdapter.notifyDataSetChanged();	                
            }else{
                checkLoad= false;
                Toast.makeText(getActivity(), "no more data", Toast.LENGTH_SHORT).show();            	
            }       
            myGrid.onLoadMoreComplete();
        }

		
    }
    
    
    public static HashMap<String, String> getAllVideos() {
    	HashMap<String,String> videoMap = new HashMap<String, String>();
    	if (videos!=null){
	    	for(YoutubeVideo item : videos){
	    		videoMap.put(parseVideoLink(item.getLink()), item.getTitle());
	    	}
    	}
		return videoMap;
	}
    
    private static String parseVideoLink(String videoUrl) {
        String id = "";
        if(videoUrl.indexOf("&feature")!= -1){
     	   id = videoUrl.substring(videoUrl.indexOf("v=")+2, videoUrl.indexOf("&feature"));
        }else{
     	   id = videoUrl.substring(videoUrl.indexOf("videos/")+7, videoUrl.indexOf("?v=2"));
        }
 		return id;
 	}
    
}
