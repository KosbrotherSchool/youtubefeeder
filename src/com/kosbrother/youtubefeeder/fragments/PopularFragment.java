package com.kosbrother.youtubefeeder.fragments;

import java.util.ArrayList;

import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.taiwan.imageload.ListNothingAdapter;
import com.taiwan.imageload.ListVideoAdapter;
import com.taiwan.imageload.LoadMoreGridView;
import com.youtube.music.channels.entity.YoutubeVideo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public final class PopularFragment extends Fragment {
    
	private ArrayList<YoutubeVideo> videos = new ArrayList<YoutubeVideo>();
//	private static String myChannelName;
	private static int myPage;
	private static String mChannelId;
	private static String mChannelName;
	private ArrayList<YoutubeVideo> moreVideos;
	private ListVideoAdapter myListAdapter;
	private Boolean checkLoad = true;
	
	private LoadMoreGridView  myGrid;
	private LinearLayout progressLayout;
	private LinearLayout    loadmoreLayout;
	private LinearLayout 	nodataLayout;
	
    public static PopularFragment newInstance(String channelId, int page, String channelName) {     
   	 
  	  myPage = page;
//    mChannelId = channelId;
  	  PopularFragment fragment = new PopularFragment();
  	  
  	  Bundle bdl = new Bundle();
	  bdl.putString("channelId", channelId);
	  fragment.setArguments(bdl);
	  mChannelName = channelName;
  	  
      return fragment;
        
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getArguments().getString("channelId");      
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
    	progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
    	loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
    	nodataLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_no_data);
    	myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);
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
        
        if (myListAdapter != null) {
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
    
    private class DownloadChannelsTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            

        }

        @Override
        protected Object doInBackground(Object... params) {
            // TODO Auto-generated method stub

        	videos = ChannelApi.getChannelMostViewedVideo(mChannelId, myPage);

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progressLayout.setVisibility(View.GONE);
            loadmoreLayout.setVisibility(View.GONE);
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

        	moreVideos = ChannelApi.getChannelMostViewedVideo(mChannelId, myPage);  
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
}
