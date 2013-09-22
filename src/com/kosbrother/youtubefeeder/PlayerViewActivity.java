/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kosbrother.youtubefeeder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerViewActivity extends YouTubeFailureRecoveryActivity {

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

		YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);

		
		playlistEventListener = new MyPlaylistEventListener();
		playerStateChangeListener = new MyPlayerStateChangeListener();

		new DownloadDescriptionTask().execute();

	}

	private void findViews() {
		progressLayout = (LinearLayout) findViewById(R.id.layout_progress);
		layoutVideos = (LinearLayout) findViewById(R.id.layout_videos);
		layoutVideosList = (LinearLayout) findViewById(R.id.layout_video_list);
		layoutActionButtons = (LinearLayout) findViewById(R.id.layout_action_buttons);
		layoutVideoIntroduction = (LinearLayout) findViewById(R.id.layout_video_introduction);
		
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
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		mPlayer = player;
		mPlayer.setPlayerStateChangeListener(playerStateChangeListener);
		mPlayer.setPlaylistEventListener(playlistEventListener);
		if (!wasRestored) {
			if(isVideos){
				for(String item: videosTitle){
					TextView newTV = new TextView(this);
					newTV.setText(item);
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
//			mPlayer.cuePlaylist("PLMDSacPyadX3gD_k9awyJf_4pzLLdHDhg");
		}
	}

	@Override
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return (YouTubePlayerView) findViewById(R.id.youtube_view);
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
	
}
