package com.kosbrother.youtubefeeder;

import java.util.ArrayList;
import java.util.HashMap;

import com.kosbrother.youtubefeeder.fragments.PlaylistVideosFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PlaylistVideosActivity extends FragmentActivity {
	
    private static final int CONTENT_VIEW_ID = 10101010;
    
    private Bundle mBundle;
	private String listTitle;
	private String listId;
	 private static String channelTitle;
	private PlaylistVideosFragment newFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        mBundle = this.getIntent().getExtras();
        listTitle = mBundle.getString("ListTitle");
        listId = mBundle.getString("ListId");
        channelTitle = mBundle.getString("ChannelTitle");
        
        newFragment =  PlaylistVideosFragment.newInstance(listId, 0, PlaylistVideosActivity.this, channelTitle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(CONTENT_VIEW_ID, newFragment).commit();
        
        int sdkVersion = android.os.Build.VERSION.SDK_INT; 
        if(sdkVersion > 10){
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.play_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		   case android.R.id.home:
			   	finish();
	        break;
		   case R.id.play_all:
				playAll();
			break;
		   }
		return super.onOptionsItemSelected(item);
    }

	private void playAll() {
		
		ArrayList<String> videoKeys = new ArrayList<String>();
		ArrayList<String> videoValues = new ArrayList<String>();
		HashMap<String, String> map = newFragment.getAllVideos();
		for (HashMap.Entry<String, String> entry : map.entrySet()) {
		    // use "entry.getKey()" and "entry.getValue()"
			videoKeys.add(entry.getKey());
			videoValues.add(entry.getValue());						
		}
		Intent intent = new Intent(PlaylistVideosActivity.this, PlayerViewActivity.class);  
		intent.putStringArrayListExtra(PlayerViewActivity.Videos_Key, videoKeys);
		intent.putStringArrayListExtra(PlayerViewActivity.Videos_Title_Key, videoValues);
		startActivity(intent);  
	}
    
}
