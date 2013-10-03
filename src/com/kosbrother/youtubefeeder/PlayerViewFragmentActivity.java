package com.kosbrother.youtubefeeder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.kosbrother.youtubefeeder.fragments.PlayerViewFragment;

public class PlayerViewFragmentActivity extends YouTubeFailureRecoveryActivity {
	
	MyAdapter mAdapter;
    ViewPager mPager;
    private LinearLayout layoutVideoPlayer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_pager);
		layoutVideoPlayer = (LinearLayout) findViewById (R.id.layout_video_player);
		
//		mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        
        try {
			YouTubePlayerView youTubeView = YouTubePlayerView.class.newInstance();
			youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
			layoutVideoPlayer.addView(youTubeView, 0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
		
	}
	
	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		player.cueVideo("V8BTsiMxyaQ");
	}
	
	@Override
	protected Provider getYouTubePlayerProvider() {
		// TODO Auto-generated method stub
		return (YouTubePlayerView) findViewById(R.id.youtube_view);
	}
	
	class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	Fragment kk = new Fragment();
        	kk = PlayerViewFragment.newInstance("mTSuiGubCHE", "謝金燕官方HD「姐姐」跳針舞曲MV大首播 Jeannie Hsieh-SISTER.[Taiwan Singer]");
//        	if(position==0){
//            	kk = SingerAlbumFragment.newInstance(SingerId);
//        	}else if(position == 1){
//        		kk = SingerSongFragment.newInstance(SingerId);
//        	}else if(position == 2){
//        		kk = SingerNewsFragment.newInstance(SingerName);
//        	}else if(position == 3){
//        		kk = SingerVideoFragment.newInstance(SingerName);
//        	}
            return kk;
        }
       

//        @Override
//        public CharSequence getPageTitle(int position) {
//        	return CONTENT[position % CONTENT.length];
//        }

        @Override
        public int getCount() {
        	return 10;
        }
    }


	

}
