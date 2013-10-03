package com.kosbrother.youtubefeeder;

import java.util.ArrayList;
import java.util.HashMap;

import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.taiwan.imageload.ListNothingAdapter;
import com.taiwan.imageload.ListVideoAdapter;
import com.youtube.music.channels.entity.YoutubeVideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SearchActivity extends Activity{
	
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private LinearLayout layoutProgress;
	private LoadMoreListView  myList;
	private EditText mEditText;
	private ImageView mImageButton;
	private ArrayList<YoutubeVideo> videos = new ArrayList<YoutubeVideo>();
	private ArrayList<YoutubeVideo> moreVideos = new ArrayList<YoutubeVideo>() ;
	private String mKeyword = "";
	private static ListVideoAdapter myListAdapter;
	private Boolean checkLoad = true;
	private int myPage =  0;
	
	private static Activity mActivity;
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
					mode.finish();	// Automatically exists the action mode, when the user selects this action
					break;						
			}
			return false;
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_search);
		mActivity = this;
		
		mProgressBar = (ProgressBar) findViewById (R.id.progress_progressbar);
		mTextView = (TextView) findViewById (R.id.progress_no_data);
		layoutProgress = (LinearLayout) findViewById (R.id.layout_progress);
		myList = (LoadMoreListView) findViewById(R.id.news_list);
		mEditText = (EditText) findViewById (R.id.edittext_search);
        mImageButton = (ImageView) findViewById (R.id.imageview_search);
		
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
		
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	try{
	                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO || actionId == 0) {
	                	if(mEditText.getText().toString().equals("") || mEditText.getText().toString().equals(0) ){
	                		Toast.makeText(SearchActivity.this, "請輸入搜索文字", Toast.LENGTH_SHORT).show();
	                	}else{
	                		layoutProgress.setVisibility(View.VISIBLE);
	                		mProgressBar.setVisibility(View.VISIBLE);
	                		mTextView.setVisibility(View.GONE);
	                		
	                		InputMethodManager imm =  (InputMethodManager)getSystemService(mActivity.INPUT_METHOD_SERVICE);
	                		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	                		// run search
	                		myPage = 0;
	                		mKeyword = mEditText.getText().toString();
	                		new DownloadChannelsTask().execute();
	                	}
	                    return true;
	                }
            	}catch(Exception e){
            		Toast.makeText(SearchActivity.this, "got problem", Toast.LENGTH_SHORT).show();
            	}
                return false;
            }
        });
		
		mImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // if edittext != "", go to SearchActivity
            	// else toast enter searh key
            	if(mEditText.getText().toString().equals("") || mEditText.getText().toString().equals(0) ){
            		Toast.makeText(SearchActivity.this, "請輸入搜索文字", Toast.LENGTH_SHORT).show();
            	}else{
            		layoutProgress.setVisibility(View.VISIBLE);
            		mProgressBar.setVisibility(View.VISIBLE);
            		mTextView.setVisibility(View.GONE);
            		
            		InputMethodManager imm =  (InputMethodManager)getSystemService(mActivity.INPUT_METHOD_SERVICE);
            		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            		// run search
            		myPage = 0;
            		mKeyword = mEditText.getText().toString();
            		new DownloadChannelsTask().execute();
            	}
            }
        });
		
		
		
	}
	
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

        	videos = ChannelApi.getYoutubeVideos(mKeyword, myPage);

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            
            
            if(videos !=null){
              layoutProgress.setVisibility(View.GONE);
          	  try{
          		  myListAdapter = new ListVideoAdapter(SearchActivity.this, videos, "");
  		          myList.setAdapter(myListAdapter);
          	  }catch(Exception e){
          		 
          	  }
            }else{
              layoutProgress.setVisibility(View.VISIBLE);
              mTextView.setVisibility(View.VISIBLE);
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

        	moreVideos = ChannelApi.getYoutubeVideos(mKeyword, myPage);
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
                Toast.makeText(SearchActivity.this, "no more data", Toast.LENGTH_SHORT).show();            	
            }       
          	myList.onLoadMoreComplete();
          	
          	
        }
    }
	
}
