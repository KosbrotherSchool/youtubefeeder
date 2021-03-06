package com.kosbrother.youtubefeeder;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.VideoListResponse;
import com.youtube.music.channels.entity.YoutubePlaylist;

@SuppressLint("NewApi")
public class PlayerViewActivity extends YouTubeFailureRecoveryActivity implements
	View.OnClickListener,YouTubePlayer.OnFullscreenListener{

	private LinearLayout progressLayout;
	private LinearLayout layoutVideos;
	private LinearLayout layoutVideosList;
	private LinearLayout layoutActionButtons;
	private LinearLayout layoutVideoIntroduction;
	private String mChosenAccountName;

	private String videoId;
	private String videoTitle;
	private Bundle mBundle;
	private String videoDescription;

	GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();

	private TextView textTitle;
	private TextView textDescription;
	private Button buttonFavorite;
	private Button buttonPlayList;
	private Button buttonShare;
	private Button buttonYoutube;
	private CheckBox checkAuto;
	private CheckBox checkRepeat;
	private CheckBox checkRandom;

	private YouTubePlayer mPlayer;
	private MyPlayerStateChangeListener playerStateChangeListener;
	private MyPlaylistEventListener playlistEventListener;
	
	private Boolean isRepeat = true;
	private Boolean isAutoPlay = true;
	private Boolean isRandomPlay = false;

	public static final String Repeat_Key = "IS_REPEAT";
	public static final String AutoPlay_Key = "IS_AutoPlay";
	public static final String RandomPlay_Key = "IS_RandomPlay";
	
	public static final String Videos_Key = "Videos_Key";
	public static final String Videos_Title_Key = "Videos_Title_Key";
	
	private List<String> videosKey = new ArrayList<String>();
	private List<String> videosTitle = new ArrayList<String>();
	private int hilightTorch = 0;
	
	private Boolean isVideos = false;
//	private Boolean fullScreen = false;
	private Boolean isFullScreen;
	
	private YouTubePlayerView playerView;
	private LinearLayout otherViews;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playerview_demo);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mChosenAccountName = sp.getString(MainActivity.ACCOUNT_KEY, "");
		isRepeat = sp.getBoolean(Repeat_Key, true);
		isAutoPlay = sp.getBoolean(AutoPlay_Key, true);
		isRandomPlay = sp.getBoolean(RandomPlay_Key, true);

		mBundle = this.getIntent().getExtras();
		try{
			videoTitle = mBundle.getString("VideoTitle");
			videoId = mBundle.getString("VideoId");
			videosKey = mBundle.getStringArrayList(Videos_Key);
			videosTitle = mBundle.getStringArrayList(Videos_Title_Key);
		}catch(Exception e){
			
		}
		
		findViews();
		
		if (videoTitle !=null){
			isVideos = false;
			textTitle.setText(videoTitle);
		}else{
			isVideos = true;
			layoutVideos.setVisibility(View.VISIBLE);
			layoutActionButtons.setVisibility(View.GONE);
			layoutVideoIntroduction.setVisibility(View.GONE);
			textTitle.setVisibility(View.GONE);
		}		

		credential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(Auth.SCOPES));
		// set exponential backoff policy
		credential.setBackOff(new ExponentialBackOff());
		credential.setSelectedAccountName(mChosenAccountName);

		playerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

		
		playlistEventListener = new MyPlaylistEventListener();
		playerStateChangeListener = new MyPlayerStateChangeListener();

		new DownloadDescriptionTask().execute();
		
		int sdkVersion = android.os.Build.VERSION.SDK_INT; 
        if(sdkVersion > 10){
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
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

	private void findViews() {
		progressLayout = (LinearLayout) findViewById(R.id.layout_progress);
		layoutVideos = (LinearLayout) findViewById(R.id.layout_videos);
		layoutVideosList = (LinearLayout) findViewById(R.id.layout_video_list);
		layoutActionButtons = (LinearLayout) findViewById(R.id.layout_action_buttons);
		layoutVideoIntroduction = (LinearLayout) findViewById(R.id.layout_video_introduction);
		otherViews = (LinearLayout) findViewById(R.id.other_views);
		
		textTitle = (TextView) findViewById(R.id.youtube_text_title);
		textDescription = (TextView) findViewById(R.id.youtube_text_description);
		buttonFavorite = (Button) findViewById(R.id.button_favorite);
		buttonPlayList = (Button) findViewById(R.id.button_playlist);
		buttonShare = (Button) findViewById(R.id.button_share);
		buttonYoutube = (Button) findViewById(R.id.button_youtube);
		checkAuto = (CheckBox) findViewById(R.id.checkbox_auto);
		checkRepeat = (CheckBox) findViewById(R.id.checkbox_repeat);
		checkRandom = (CheckBox) findViewById(R.id.checkbox_random);
		if (isAutoPlay) {
			checkAuto.setChecked(true);
		} else {
			checkAuto.setChecked(false);
		}

		if (isRepeat) {
			checkRepeat.setChecked(true);
		} else {
			checkRepeat.setChecked(false);
		}
		
		if (isRandomPlay) {
			checkRandom.setChecked(true);
		} else {
			checkRandom.setChecked(false);
		}
		buttonFavorite.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	if (mChosenAccountName!=null && mChosenAccountName!=""){
            		new AddToFavoriteList().execute();
            	}else{
            		Toast.makeText(PlayerViewActivity.this, PlayerViewActivity.this.getResources().getString(R.string.login_first), Toast.LENGTH_SHORT).show();
            	}
            }         

        });
		buttonPlayList.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	if (mChosenAccountName!=null && mChosenAccountName!=""){
	            	final ArrayList<YoutubePlaylist> myList = MainActivity.getMyList();
	            	
	            	final String[] ListStr = new String[myList.size()];
	            	for (int i=0;i< myList.size();i++){
	            		ListStr[i] = myList.get(i).getTitle();
	            	}
	            	
	                AlertDialog.Builder builder = new AlertDialog.Builder(PlayerViewActivity.this);
	                builder.setTitle("Select List");
	                builder.setItems(ListStr, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int item) {                   
	                        	new AddToList().execute(myList.get(item));     
	                    }
	                });
	                AlertDialog alert = builder.create();
	                alert.show();
	            }else{
	            	Toast.makeText(PlayerViewActivity.this, PlayerViewActivity.this.getResources().getString(R.string.login_first), Toast.LENGTH_SHORT).show();
	            }
            }         

        });
		buttonShare.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, Constants.YOUTUBE_WATCH_URL_PREFIX+videoId);
                startActivity(Intent.createChooser(intent, "Share..."));          	
            }         

        });
		buttonYoutube.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {          	
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_WATCH_URL_PREFIX+videoId));
            	startActivity(browserIntent);

// 				Youtube has no intent for www.youtube.com/channels 
//            	Intent intent = YouTubeIntents.createUserIntent(PlayerViewActivity.this, "NewMovie520");
//            	startActivity(intent);

            }         

        });
		
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		mPlayer = player;
		mPlayer.setPlayerStateChangeListener(playerStateChangeListener);
		mPlayer.setPlaylistEventListener(playlistEventListener);
		player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
		player.setOnFullscreenListener(this);
		player.setShowFullscreenButton(false);
//		mPlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
		mPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
//		mPlayer.setOnFullscreenListener(new OnFullscreenListener() {
//            @Override
//            public void onFullscreen(boolean _isFullScreen) {
//               
//            }
//        });
		if (!wasRestored) {
			if(isVideos){
				for(String item: videosTitle){
					TextView newTV = new TextView(this);
					newTV.setText(item);
					@SuppressWarnings("deprecation")
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				    params.setMargins(5, 5, 5, 5); //left, top, right, bottom
					layoutVideosList.addView(newTV,params);
				}
				layoutVideosList.getChildAt(hilightTorch).setBackgroundColor(getResources().getColor(R.color.light_blue));
				mPlayer.cueVideos(videosKey);
			}else{
				mPlayer.cueVideo(videoId);
			}
		}
	}

//	@Override
//	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
//		return (YouTubePlayerView) findViewById(R.id.youtube_view);
//	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	}
	
	
	private final class MyPlaylistEventListener implements PlaylistEventListener {
	    @Override
	    public void onNext() {
//	      log("NEXT VIDEO");
	      layoutVideosList.getChildAt(hilightTorch).setBackgroundColor(getResources().getColor(R.color.white));
	      hilightTorch = hilightTorch +1;
	      layoutVideosList.getChildAt(hilightTorch).setBackgroundColor(getResources().getColor(R.color.light_blue));
	    }

	    @Override
	    public void onPrevious() {
//	      log("PREVIOUS VIDEO");
	      layoutVideosList.getChildAt(hilightTorch).setBackgroundColor(getResources().getColor(R.color.white));
		  hilightTorch = hilightTorch -  1;
		  layoutVideosList.getChildAt(hilightTorch).setBackgroundColor(getResources().getColor(R.color.light_blue));
	    }

	    @Override
	    public void onPlaylistEnded() {
//	      log("PLAYLIST ENDED");
	    }
	}
	
	private final class MyPlayerStateChangeListener implements
			PlayerStateChangeListener {
		@SuppressWarnings("unused")
		String playerState = "UNINITIALIZED";

		@Override
		public void onLoading() {
			playerState = "LOADING";
		}

		@Override
		public void onLoaded(String videoId) {
			playerState = String.format("LOADED %s", videoId);
			if (isAutoPlay) {
				mPlayer.play();
			}
		}

		@Override
		public void onAdStarted() {
			playerState = "AD_STARTED";
		}

		@Override
		public void onVideoStarted() {
			playerState = "VIDEO_STARTED";
		}

		@Override
		public void onVideoEnded() {
			playerState = "VIDEO_ENDED";
			isRepeat = checkRepeat.isChecked();
			if (isRepeat) {
				mPlayer.play();
			}
		}

		@Override
		public void onError(ErrorReason reason) {
			playerState = "ERROR (" + reason + ")";
			if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
				// When this error occurs the player is released and can no
				// longer be used.
				mPlayer = null;
			}
		}

	}

	private class DownloadDescriptionTask extends AsyncTask {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressLayout.setVisibility(View.VISIBLE);

		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			YouTube youtube = new YouTube.Builder(transport, jsonFactory,
					credential).setApplicationName(Constants.APP_NAME).build();
			try {

				VideoListResponse videosResponse = youtube.videos()
						.list("snippet").setId(videoId).execute();
				videoDescription = videosResponse.getItems().get(0)
						.getSnippet().getDescription();

			} catch (Exception e) {

			}

			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progressLayout.setVisibility(View.GONE);
			textDescription.setText(videoDescription);

		}
	}
	
	
	@Override
    public void onStop() {
        super.onStop();
        SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
        isRepeat = checkRepeat.isChecked();
        isAutoPlay = checkAuto.isChecked();
        isRandomPlay = checkRandom.isChecked();
		sp.edit().putBoolean(Repeat_Key, isRepeat).commit();
		sp.edit().putBoolean(AutoPlay_Key, isAutoPlay).commit();
		sp.edit().putBoolean(RandomPlay_Key, isRandomPlay).commit();
    }
	
	private class AddToFavoriteList extends AsyncTask<Void, Void, PlaylistItem> {

	      @Override
	      protected void onPreExecute() {
	          // TODO Auto-generated method stub
	          super.onPreExecute();          
	          Toast.makeText(PlayerViewActivity.this, "Adding to Favorite", Toast.LENGTH_SHORT).show();
	      }

	      @Override
	      protected PlaylistItem doInBackground(Void... voids) {
	          // TODO Auto-generated method stub
	    	  YouTube youtube = new YouTube.Builder(transport, jsonFactory,
						credential).setApplicationName(Constants.APP_NAME)
						.build();
	    	  
	    	  ResourceId resourceId = new ResourceId();
	    	  resourceId.setKind("youtube#video");
	    	  resourceId.setVideoId(videoId);
	    	  
	    	  PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
	    	  playlistItemSnippet.setPlaylistId(MainActivity.favoriteListId);
	    	  playlistItemSnippet.setResourceId(resourceId);
	    	  
	    	  PlaylistItem playlistItem = new PlaylistItem();
	    	  playlistItem.setSnippet(playlistItemSnippet);
	    	  
	    	  PlaylistItem returnedPlaylistItem = null;
	    	  
		      try {
					returnedPlaylistItem = youtube.playlistItems().insert("snippet,contentDetails", playlistItem).execute();
			  } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			  }
	    	  
	         return returnedPlaylistItem;
	      }

	      @Override
	      protected void onPostExecute(PlaylistItem result) {
	          if (result!=null){
	        	  Toast.makeText(PlayerViewActivity.this, "Added to Favorite", Toast.LENGTH_SHORT).show();
	          }else{
	        	  Toast.makeText(PlayerViewActivity.this, "Failed to Fovorite", Toast.LENGTH_SHORT).show();
	          }
	      }
	  }
	
	private class AddToList extends AsyncTask<YoutubePlaylist, Void, YoutubePlaylist> {

	      @Override
	      protected void onPreExecute() {
	          // TODO Auto-generated method stub
	          super.onPreExecute();          
	          Toast.makeText(PlayerViewActivity.this, "Adding to List", Toast.LENGTH_SHORT).show();
	      }

	      @Override
	      protected YoutubePlaylist doInBackground(YoutubePlaylist... playlists) {
	          // TODO Auto-generated method stub
	    	  YouTube youtube = new YouTube.Builder(transport, jsonFactory,
						credential).setApplicationName(Constants.APP_NAME)
						.build();
	    	  
	    	  ResourceId resourceId = new ResourceId();
	    	  resourceId.setKind("youtube#video");
	    	  resourceId.setVideoId(videoId);
	    	  
	    	  PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
	    	  playlistItemSnippet.setPlaylistId(playlists[0].getListId());
	    	  playlistItemSnippet.setResourceId(resourceId);
	    	  
	    	  PlaylistItem playlistItem = new PlaylistItem();
	    	  playlistItem.setSnippet(playlistItemSnippet);
	    	  
	    	  PlaylistItem returnedPlaylistItem = null;
	    	  
		      try {
					returnedPlaylistItem = youtube.playlistItems().insert("snippet,contentDetails", playlistItem).execute();
			  } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			  }
	    	  
		     if (returnedPlaylistItem!=null){
		    	 return playlists[0];
		     }else{
		    	 return null;
		     }
	      }

	      @Override
	      protected void onPostExecute(YoutubePlaylist result) {
	          if (result!=null){
	        	  Toast.makeText(PlayerViewActivity.this, "Added to " + result.getTitle(), Toast.LENGTH_SHORT).show();
	          }else{
	        	  Toast.makeText(PlayerViewActivity.this, "Failed to Add", Toast.LENGTH_SHORT).show();
	          }
	      }
	  }


	@Override
	  protected YouTubePlayer.Provider getYouTubePlayerProvider() {
	    return playerView;
	  }
	
	@Override
	public void onFullscreen(boolean _isFullscreen) {
		// TODO Auto-generated method stub
		isFullScreen = _isFullscreen;
		doLayout();
	}	

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		mPlayer.setFullscreen(!isFullScreen);
	}
	
	@Override
	  public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
	    	isFullScreen = true;
	    }else{
	    	isFullScreen = false;
	    }
//	    mPlayer.setFullscreen(!isFullScreen);
	    doLayout();
	}
	
	private void doLayout() {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams playerParams =
		        (LinearLayout.LayoutParams) playerView.getLayoutParams();
		if (isFullScreen) {
		    // When in fullscreen, the visibility of all other views than the player should be set to
		    // GONE and the player should be laid out across the whole screen.
		    playerParams.width = LayoutParams.MATCH_PARENT;
		    playerParams.height = LayoutParams.MATCH_PARENT;
		    otherViews.setVisibility(View.GONE);
		}else{
			
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		        playerParams.width  = 0;
		        playerParams.height = MATCH_PARENT;
		        otherViews.setVisibility(View.VISIBLE);
		        playerParams.weight = 1;
		        
		      }else{
		    	otherViews.setVisibility(View.VISIBLE);
		    	playerParams.width  = MATCH_PARENT;
		        playerParams.height = WRAP_CONTENT;
		        getActionBar().show();
		      }			
		} 
	}
	
}
