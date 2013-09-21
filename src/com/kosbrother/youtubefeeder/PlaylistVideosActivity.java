package com.kosbrother.youtubefeeder;

import com.kosbrother.youtubefeeder.fragments.PlaylistVideosFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class PlaylistVideosActivity extends FragmentActivity {
	
    private static final int CONTENT_VIEW_ID = 10101010;
    
    private Bundle mBundle;
	private String listTitle;
	private String listId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        mBundle = this.getIntent().getExtras();
        listTitle = mBundle.getString("ListTitle");
        listId = mBundle.getString("ListId");
        
        
        PlaylistVideosFragment newFragment =  PlaylistVideosFragment.newInstance(listId, 0);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(CONTENT_VIEW_ID, newFragment).commit();
        
    }
    
}
