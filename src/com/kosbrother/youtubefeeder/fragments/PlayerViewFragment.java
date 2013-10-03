package com.kosbrother.youtubefeeder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kosbrother.youtubefeeder.PlayerViewFragmentActivity;
import com.kosbrother.youtubefeeder.R;

public class PlayerViewFragment extends Fragment {
    	
	private String videoId;
	private String videoName;
	private static PlayerViewFragmentActivity mActivity;
	
    public static PlayerViewFragment newInstance(String videoId,String videoName) {     
   	 
  	  PlayerViewFragment fragment = new PlayerViewFragment();
  	  
  	  Bundle bdl = new Bundle();
	  bdl.putString("VideoId", videoId);
	  bdl.putString("VideoName", videoName);
	  fragment.setArguments(bdl);
	  
//	  mActivity = theActivity;
	  
      return fragment;
        
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoId = getArguments().getString("VideoId");  
        videoName = getArguments().getString("VideoName"); 
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	View myView = inflater.inflate(R.layout.playerview_video_fragment, container, false);
        
        return myView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       
    }
}