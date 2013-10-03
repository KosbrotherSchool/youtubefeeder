package com.kosbrother.youtubefeeder.fragments;


import java.util.ArrayList;
import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.taiwan.imageload.ListNothingAdapter;
import com.taiwan.imageload.ListPlaylistAdapter;
import com.youtube.music.channels.entity.YoutubePlaylist;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;



public class PlaylistFragment extends Fragment {

  private ArrayList<YoutubePlaylist> playlists = new ArrayList<YoutubePlaylist>();
  private static String mChannelId;
  private static String mChannelTitle;
  private static int myPage;
  private ArrayList<YoutubePlaylist> morePlaylist = new ArrayList<YoutubePlaylist>();
  private ListPlaylistAdapter myListAdapter;
  private Boolean checkLoad = true;
  private LoadMoreListView  myList;
  private LinearLayout progressLayout;
  
  
  
  
  public static PlaylistFragment newInstance(String channelId, int page, String channelTitle) {     
	 
//	  myChannelId = channelId;
	  myPage = page;
	  
	  PlaylistFragment fragment = new PlaylistFragment();
	  
	  Bundle bdl = new Bundle();
	  bdl.putString("channelId", channelId);
	  fragment.setArguments(bdl);
	  
	  mChannelTitle = channelTitle;
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
  
  
  private class DownloadChannelsTask extends AsyncTask {

      @Override
      protected void onPreExecute() {
          // TODO Auto-generated method stub
          super.onPreExecute();
          

      }

      @Override
      protected Object doInBackground(Object... params) {
          // TODO Auto-generated method stub

    	  playlists = ChannelApi.getChannelPlaylists(mChannelId, myPage); 
//    	  videos = ChannelApi.getPlaylistVideos(myChannelName, myPage);
    	  
          return null;
      }

      @Override
      protected void onPostExecute(Object result) {
          // TODO Auto-generated method stub
          super.onPostExecute(result);
          progressLayout.setVisibility(View.GONE);
          if(playlists != null){
        	  try{
		          myListAdapter = new ListPlaylistAdapter(getActivity(), playlists, mChannelTitle);
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

    	morePlaylist = ChannelApi.getChannelPlaylists(mChannelId, myPage);   
      	if(morePlaylist!= null){
	        	for(int i=0; i<morePlaylist.size();i++){
	        		playlists.add(morePlaylist.get(i));
	            }
      	}
      	
      	
          return null;
      }

      @Override
      protected void onPostExecute(Object result) {
          // TODO Auto-generated method stub
          super.onPostExecute(result);
          
          if(morePlaylist!= null){
          	myListAdapter.notifyDataSetChanged();	                
          }else{
              checkLoad= false;
              Toast.makeText(getActivity(), "no more data", Toast.LENGTH_SHORT).show();            	
          }       
          myList.onLoadMoreComplete();
        	
        	
      }
  }
  
} 
