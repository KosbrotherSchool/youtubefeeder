package com.kosbrother.youtubefeeder;

import java.util.ArrayList;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.ads.AdRequest.ErrorCode;
import com.kosbrother.youtubefeeder.fragments.NewVideosFragment;
import com.kosbrother.youtubefeeder.fragments.PlaylistFragment;
import com.kosbrother.youtubefeeder.fragments.PopularFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

@SuppressLint("NewApi")
public class ChannelTabs extends FragmentActivity {
    
	TabHost mTabHost;
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    private static String channelId;
    private static String channelTitle;
    private Bundle mBundle;
    
    private RelativeLayout adBannerLayout;
	private AdView adMobAdView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mBundle = this.getIntent().getExtras();
        channelTitle = mBundle.getString("ChannelTitle");
        channelId = mBundle.getString("ChannelId");
        
        setContentView(R.layout.fragment_tabs_pager);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mViewPager = (ViewPager)findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

        mTabsAdapter.addTab(mTabHost.newTabSpec("new").setIndicator(getResources().getString(R.string.tab_recent)),
                NewVideosFragment.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("popular").setIndicator(getResources().getString(R.string.tab_favorite)),
        		PopularFragment.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("playlist").setIndicator(getResources().getString(R.string.tab_list)),
        		PlaylistFragment.class, null);


        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        
        int sdkVersion = android.os.Build.VERSION.SDK_INT; 
        if(sdkVersion > 10){
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Call ads
     		adBannerLayout = (RelativeLayout) findViewById(R.id.adLayout);	
     		final AdRequest adReq = new AdRequest();
     		
     		adMobAdView = new AdView(this, AdSize.SMART_BANNER, DeveloperKey.MEDIATION_KEY);
     		adMobAdView.setAdListener(new AdListener() {
     			@Override
     			public void onDismissScreen(Ad arg0) {
     				Log.d("admob_banner", "onDismissScreen");
     			}

     			@Override
     			public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
     				Log.d("admob_banner", "onFailedToReceiveAd");
     			}

     			@Override
     			public void onLeaveApplication(Ad arg0) {
     				Log.d("admob_banner", "onLeaveApplication");
     			}

     			@Override
     			public void onPresentScreen(Ad arg0) {
     				Log.d("admob_banner", "onPresentScreen");
     			}

     			@Override
     			public void onReceiveAd(Ad ad) {
     				Log.d("admob_banner", "onReceiveAd ad:" + ad.getClass());
     			}

     		});
     		adMobAdView.loadAd(adReq);
     		adBannerLayout.addView(adMobAdView);
        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
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
    
    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
        	Fragment kk = new Fragment();     	
        	if(position==0){
        		kk = NewVideosFragment.newInstance(channelId, 0, mContext, channelTitle);
        	}else if(position == 1){
        		kk = PopularFragment.newInstance(channelId, 0, channelTitle);
        	}else if(position == 2){
        		kk = PlaylistFragment.newInstance(channelId, 0, channelTitle);
        	}
            return kk;
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}