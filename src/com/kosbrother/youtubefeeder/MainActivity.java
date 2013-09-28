package com.kosbrother.youtubefeeder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.ytdl.util.Utils;
import com.google.ytdl.util.VideoData;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.kosbrother.youtubefeeder.database.ChannelTable;
import com.kosbrother.youtubefeeder.database.VideoTable;
import com.taiwan.imageload.ImageLoader;
import com.taiwan.imageload.VideoCursorAdapter;
import com.youtube.music.channels.entity.Channel;
import com.youtube.music.channels.entity.YoutubePlaylist;
import com.youtube.music.channels.entity.YoutubeVideo;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.bartinger.list.item.EntryAdapter;
import at.bartinger.list.item.EntryItem;
import at.bartinger.list.item.Item;
import at.bartinger.list.item.SectionItem;

/**
 * 這是 Youtube 的訂閱器,方便收看訂閱的項目 功能: 1. 首頁, 訂閱 video 項目列表 2. 訂閱內頁, 顯示"最近上傳", "最受歡迎",
 * "播放清單" 三個 TabFragment 3. 影片內頁, 可以"加入最愛", "加入清單"(使用者原有清單), "前往Youtube" 4. 本
 * App 之重要功能是能夠選擇影片連續播放.
 * 
 * 2013/9/11 完成 youtube 訂閱資料的取得, 接下來應該實作畫面
 * 2013/9/13 做好了 drawer 功能, 接下來先取得一部分 channel video 並 show 在 main_list_view 上
 * 
 * 先寫 database, 將讀取到的 channel 跟 video 存下來
 * 2013/9/14 完成 video database 的部分! 
 * 2013/9/15 設好 channel database 的部分, 並記下 videoNums, 還沒調用
 * 2013/9/16 完成 channel database 的調用, 完成 channel 頁
 * 2013/9/17 完成 video 內頁, 接下來做 playList 頁
 * 2013/9/18 完成 playList 頁, PlayListFragment 有時會讀不出 data => fix
 * player 加入重復播放, 起始, 終了
 * 
 * 之後要把 video 的 insert 寫在 Service, 也要加個欄位用來 check 這個欄位已經讀過了 or not
 * 
 * 1. set player view functions
 * 2. add to playlist
 * 3. add to favorite
 * 
 * 1. add is_read column to video table
 * 2. write a service to run in background to download videos
 * a. for initiate 每個頻道讀2個影片
 * b. 定期檢查影片有新的就加入(Channel 要記下目前的影片數)
 * c. 該頻道 havn't read 留著, read 了的砍掉
 * 
 * 剩 Service!!
 * 
 * @author JasonKo
 */

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
		OnConnectionFailedListener,LoaderManager.LoaderCallbacks<Cursor> {

	public static final String AUTHORITY = "com.kosbrother.youtubefeeder.database";
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ListView mainListView;
	private EntryAdapter mDrawerAdapter;
	private ArrayList<Item> items = new ArrayList<Item>();
	private ArrayList<Channel> mSubscriptionChannels = new ArrayList<Channel>();
	
	private TextView textName;
	private ImageView viewAvatar;
	public ImageLoader imageLoader;
	private LinearLayout progressLayout;
	
	private static VideoCursorAdapter mVideoAdapter;
	private ArrayList<YoutubeVideo> mVideos = new ArrayList<YoutubeVideo>();
	
//	private ChannelCursorAdapter mChannelAdapter;
	private static ArrayList<YoutubePlaylist>  myPlayList =  new ArrayList<YoutubePlaylist>();
	
	private ActionBarHelper mActionBar;
	private ActionBarDrawerToggle mDrawerToggle;
	
	private static final int REQUEST_ACCOUNT_PICKER = 2;
	private static final int REQUEST_AUTHORIZATION = 3;

	public static final String ACCOUNT_KEY = "accountName";
	public static final String ACCOUNT_DISPLAY_NAME_KEY = "accountDisplayName";
	public static final String ACCOUNT_IMAGE_KEY = "accountImage";
	private String mChosenAccountName;
	private String mDisplayName;
	private String mAccountImage;
	
	public static String favoriteListId;
	
	public static final String Initialized_Key = "Initial_Action";
	private boolean isInitialized = false;
	private boolean isListSetted = false;
	
	private int sectionListPosition;
	
	private PlusClient mPlusClient;
	
	GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();

	private static final String TAG = "MainActivity";
	
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
					HashMap<String, String> map = mVideoAdapter.getMap();
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
		setContentView(R.layout.drawer_layout);
		mActivity = this;
		imageLoader = new ImageLoader(MainActivity.this, 70);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		isInitialized = sp.getBoolean(Initialized_Key, false);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_list_view);
		mainListView = (ListView) findViewById(R.id.main_list_view);
		textName = (TextView) findViewById(R.id.left_name);
		viewAvatar = (ImageView) findViewById(R.id.left_avatar);
		progressLayout = (LinearLayout) findViewById(R.id.layout_progress);
		
		mDrawerLayout.setDrawerListener(new DemoDrawerListener());
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mActionBar = createActionBarHelper();
		mActionBar.init();

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close);
		//(Context context,int layout, Cursor c,String[] from,int[] to)
		mVideoAdapter = new VideoCursorAdapter(this, R.layout.item_video_list, null, PROJECTION, null);
		mainListView.setAdapter(mVideoAdapter);
		
		// set drawer adapter
//		mChannelAdapter = new ChannelCursorAdapter(this, R.layout.list_item_entry, null, PROJECTION_CHANNEL, null);
//		mDrawerListView.setAdapter(mDrawerAdapter);
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(0, null, this);
				
		// get GoogleAccount OAuth
		credential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(Auth.SCOPES));
		// set exponential backoff policy
		credential.setBackOff(new ExponentialBackOff());
		if (savedInstanceState != null) {
			mChosenAccountName = savedInstanceState.getString(ACCOUNT_KEY);
		} else {
			loadAccount();
		}
		credential.setSelectedAccountName(mChosenAccountName);
		
		if (mDisplayName == null){
			mPlusClient = new PlusClient.Builder(MainActivity.this, this, this)
			.setScopes(Auth.SCOPES)
			.build();
			mPlusClient.connect();
		}else {
			textName.setText(mDisplayName);
			imageLoader.DisplayImage(mAccountImage, viewAvatar);
		}
		
		loadData();
		
	}
	
	public void setProfileInfo() {	
        if (!mPlusClient.isConnected() || mPlusClient.getCurrentPerson() == null) {            
        	textName.setText(R.string.not_signed_in);
        } else {
            Person currentPerson = mPlusClient.getCurrentPerson();
            if (currentPerson.hasImage()) {
            	imageLoader.DisplayImage(currentPerson.getImage().getUrl(), viewAvatar);
            }
            if (currentPerson.hasDisplayName()) {
            	textName.setText(currentPerson.getDisplayName());
            }
            SharedPreferences sp = PreferenceManager
    				.getDefaultSharedPreferences(this);
    		sp.edit().putString(ACCOUNT_DISPLAY_NAME_KEY, currentPerson.getDisplayName()).commit();
    		sp.edit().putString(ACCOUNT_IMAGE_KEY, currentPerson.getImage().getUrl()).commit();
        }
    }
	
	public static void showActionMode() {
		if (!mModeIsShowing){
			mMode = mActivity.startActionMode(modeCallBack);
			mModeIsShowing = true;
		}
	}
	
	public static ArrayList<YoutubePlaylist> getMyList() {
		 return myPlayList;
	}

	private class DemoDrawerListener implements DrawerLayout.DrawerListener {
		@Override
		public void onDrawerOpened(View drawerView) {
			mDrawerToggle.onDrawerOpened(drawerView);
			mActionBar.onDrawerOpened();
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			mDrawerToggle.onDrawerClosed(drawerView);
			mActionBar.onDrawerClosed();
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			mDrawerToggle.onDrawerStateChanged(newState);
		}
	}

	private ActionBarHelper createActionBarHelper() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new ActionBarHelperICS();
		} else {
			return new ActionBarHelper();
		}
	}

	private class ActionBarHelper {
		public void init() {}
		public void onDrawerClosed() {}
		public void onDrawerOpened() {}
		public void setTitle(CharSequence title) {}
	}
	
	private class ActionBarHelperICS extends ActionBarHelper {
		private final ActionBar mActionBar;
		private CharSequence mDrawerTitle;
		private CharSequence mTitle;

		ActionBarHelperICS() {
			mActionBar = getActionBar();
		}

		@Override
		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
			mTitle = mDrawerTitle = getTitle();
		}

		@Override
		public void onDrawerClosed() {
			super.onDrawerClosed();
			mActionBar.setTitle(mTitle);
		}

		@Override
		public void onDrawerOpened() {
			super.onDrawerOpened();
			mActionBar.setTitle(mDrawerTitle);
		}

		@Override
		public void setTitle(CharSequence title) {
			mTitle = title;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }else{
			switch (item.getItemId()) {
			case R.id.menu_refresh:
				progressLayout.setVisibility(View.VISIBLE);
				loadData();
				break;
			case R.id.menu_accounts:
				chooseAccount();
				return true;
			}
        }
		return super.onOptionsItemSelected(item);
	}

	private void loadAccount() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mChosenAccountName = sp.getString(ACCOUNT_KEY, null);
		mAccountImage = sp.getString(ACCOUNT_IMAGE_KEY, null);
		mDisplayName = sp.getString(ACCOUNT_DISPLAY_NAME_KEY, null);
		invalidateOptionsMenu();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ACCOUNT_KEY, mChosenAccountName);
	}

	private void loadData() {
		if (mChosenAccountName == null) {
			return;
		}
		loadVideos();
		
	}

	// Load Videos
	private void loadVideos() {
		if (mChosenAccountName == null) {
			return;
		}

		setProgressBarIndeterminateVisibility(true);
		new AsyncTask<Void, Void, List<VideoData>>() {
			@Override
	        protected void onPreExecute() {
	            // TODO Auto-generated method stub
	            super.onPreExecute();
	            

	        }
			
			@Override
			protected List<VideoData> doInBackground(Void... voids) {

				YouTube youtube = new YouTube.Builder(transport, jsonFactory,
						credential).setApplicationName(Constants.APP_NAME)
						.build();

				try {

					if (!isListSetted){
						items.add(new SectionItem("我的訂閱"));
						items.add(new EntryItem("全部項目","",""));
					}
					ContentResolver cr = getContentResolver();
					
					if (!isInitialized){
						SubscriptionListResponse mSubscriptions = youtube
								.subscriptions().list("snippet,contentDetails").setMine(true).setMaxResults((long) 20).execute();
						
						List<Subscription> lists = mSubscriptions.getItems();
						for (Subscription item : lists){
							
							Channel theChannel = new Channel(item.getSnippet().getResourceId().getChannelId(),
									item.getSnippet().getTitle(),
									item.getSnippet().getThumbnails().getDefault().getUrl(),
									item.getContentDetails().getTotalItemCount().intValue()
									); // 88x88, 240x240 if get high	
							mSubscriptionChannels.add(theChannel);
							
							items.add(new EntryItem(theChannel.getTitle(), theChannel.getId(), theChannel.getThumbnail()));
							
							ContentValues values = new ContentValues();
							values.put(ChannelTable.COLUMN_NAME_DATA1, theChannel.getId());
				        	values.put(ChannelTable.COLUMN_NAME_DATA2, theChannel.getTitle());
				        	values.put(ChannelTable.COLUMN_NAME_DATA3, theChannel.getThumbnail());
				        	values.put(ChannelTable.COLUMN_NAME_DATA4, theChannel.getVideoNums());
							cr.insert(ChannelTable.CONTENT_URI, values);
							
							updateVideos( cr, 2, theChannel.getId());
														
						}
						
						// set drawer list 					
						sectionListPosition = items.size();
						items.add(new SectionItem("播放清單"));
						
						// add favorite playlist
						try{													
							PlaylistSnippet playlistSnippet = new PlaylistSnippet();
						    playlistSnippet.setTitle("我的最愛 (YoutubeFeeder)");
							
						    PlaylistStatus playlistStatus = new PlaylistStatus();
						    playlistStatus.setPrivacyStatus("public");
						    
							Playlist youTubePlaylist = new Playlist();
						    youTubePlaylist.setSnippet(playlistSnippet);
						    youTubePlaylist.setStatus(playlistStatus);
						    
						    youtube.playlists().insert("snippet", youTubePlaylist).execute();
						   
						}catch(Exception e){
							
						}
						
						setPlayListData(youtube);
						isInitialized = true;
						isListSetted = true;
						
					}else{
						cr.delete(VideoTable.CONTENT_URI, VideoTable.COLUMN_NAME_DATA9+" = ?" , new String[] {"1"});
						
						SubscriptionListResponse mSubscriptions = youtube.subscriptions().list("snippet,contentDetails").setMine(true).setMaxResults((long) 20).execute();				
						List<Subscription> lists = mSubscriptions.getItems();
						
						for (Subscription item : lists){	
							Channel theChannel = new Channel(item.getSnippet().getResourceId().getChannelId(),
									item.getSnippet().getTitle(),
									item.getSnippet().getThumbnails().getDefault().getUrl(),
									item.getContentDetails().getTotalItemCount().intValue()
									);
							if (!isListSetted){
								mSubscriptionChannels.add(theChannel);
								items.add(new EntryItem(theChannel.getTitle(), theChannel.getId(), theChannel.getThumbnail()));
							}
							
				        	String channelId = item.getSnippet().getResourceId().getChannelId();			        	
				        	
				        	Cursor theChannelCursor = cr.query(ChannelTable.CONTENT_URI, MainActivity.PROJECTION_CHANNEL, 
				        			ChannelTable.COLUMN_NAME_DATA1+" = ?", new String[] {channelId}, null);
				        	theChannelCursor.moveToFirst();
				        	
				        	if (theChannelCursor.getCount() != 0){  
						        int videoNums_index = theChannelCursor.getColumnIndex(ChannelTable.COLUMN_NAME_DATA4);
						        int dataChannelNums = theChannelCursor.getInt(videoNums_index);
						        if(dataChannelNums< theChannel.getVideoNums()){
						        	int updateNums = theChannel.getVideoNums() - dataChannelNums;
						        	updateVideos(cr,updateNums,theChannel.getId());
						        	ContentValues values = new ContentValues();
						        	values.put(ChannelTable.COLUMN_NAME_DATA4, theChannel.getVideoNums());
						        	cr.update(ChannelTable.CONTENT_URI, values, ChannelTable.COLUMN_NAME_DATA1+" = ?" , new String[] {channelId});
						        }else if(dataChannelNums > theChannel.getVideoNums()) {
						        	ContentValues values = new ContentValues();
						        	values.put(ChannelTable.COLUMN_NAME_DATA4, theChannel.getVideoNums());
						        	cr.update(ChannelTable.CONTENT_URI, values, ChannelTable.COLUMN_NAME_DATA1+" = ?" , new String[] {channelId});
						        }
				        	} else {
				        		ContentValues values = new ContentValues();
								values.put(ChannelTable.COLUMN_NAME_DATA1, theChannel.getId());
					        	values.put(ChannelTable.COLUMN_NAME_DATA2, theChannel.getTitle());
					        	values.put(ChannelTable.COLUMN_NAME_DATA3, theChannel.getThumbnail());
					        	values.put(ChannelTable.COLUMN_NAME_DATA4, theChannel.getVideoNums());
					        	cr.insert(ChannelTable.CONTENT_URI, values);			        	
					        	updateVideos( cr, 2, theChannel.getId());				        	
				        	}
				        	
						}
						
				        
				        // set drawer list
						if (!isListSetted){
							sectionListPosition = items.size();
							items.add(new SectionItem("播放清單"));
							setPlayListData(youtube);
						}
						isListSetted = true;
					}
					
					return null;

				} catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
					// showGooglePlayServicesAvailabilityErrorDialog(availabilityException
					// .getConnectionStatusCode());
				} catch (UserRecoverableAuthIOException userRecoverableException) {
					startActivityForResult(
							userRecoverableException.getIntent(),
							REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					Utils.logAndShow(MainActivity.this, Constants.APP_NAME, e);
				}
				return null;
			}

			

			@Override
			protected void onPostExecute(List<VideoData> videos) {
				setProgressBarIndeterminateVisibility(false);
				progressLayout.setVisibility(View.GONE);
				
				mDrawerAdapter = new EntryAdapter(MainActivity.this, items);
				mDrawerListView.setAdapter(mDrawerAdapter);
				mDrawerListView.setOnItemClickListener((new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						if(!items.get(position).isSection()){
				    		if (position < sectionListPosition){
					    		EntryItem item = (EntryItem)items.get(position);			    		
	//				    		Toast.makeText(MainActivity.this, "id =  " + item.subtitle , Toast.LENGTH_SHORT).show();				    		
					    		Intent intent = new Intent(MainActivity.this, ChannelTabs.class);  
					    		intent.putExtra("ChannelTitle", item.title);  
					    		intent.putExtra("ChannelId", item.subtitle);  
					    		startActivity(intent);  
				    		}else{
				    			EntryItem item = (EntryItem)items.get(position);			    					    		
				    			Intent intent = new Intent(MainActivity.this, PlaylistVideosActivity.class);  
				    			intent.putExtra("ListTitle", item.title);  
				    			intent.putExtra("ListId", item.subtitle);  
				    			startActivity(intent);  
				    		}
				    		
				    	}
					}
				}));
				
				
			}

		}.execute((Void) null);
	}
	
	private void setPlayListData(YouTube youtube) {
		// TODO Auto-generated method stub
		PlaylistListResponse mLists;
		try {
			mLists = youtube.playlists().list("snippet").setMine(true).setMaxResults((long) 20).execute();
			List<Playlist> lists = mLists.getItems();
			for (Playlist item : lists){
				String title = item.getSnippet().getTitle();
				if(title.indexOf("(YoutubeFeeder)")!=-1){
					title = title.subSequence(0, title.indexOf("(YoutubeFeeder)")-1).toString();
					favoriteListId = item.getId();
				}else{
					YoutubePlaylist thePlayList = new YoutubePlaylist(item.getSnippet().getTitle(),item.getId(),"");
					myPlayList.add(thePlayList);
				}
				items.add(new EntryItem(title,item.getId(),""));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {

		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					mChosenAccountName = accountName;
					credential.setSelectedAccountName(accountName);
					saveAccount();
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode != Activity.RESULT_OK) {
				chooseAccount();
			}
			break;

		}
	}

	private void saveAccount() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.edit().putString(ACCOUNT_KEY, mChosenAccountName).commit();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		if (connectionResult.hasResolution()) {
			Toast.makeText(this, "connect fail", Toast.LENGTH_SHORT).show();

			Log.e(TAG,
					String.format(
							"Connection to Play Services Failed, error: %d, reason: %s",
							connectionResult.getErrorCode(),
							connectionResult.toString()));
			try {
				connectionResult.startResolutionForResult(this, 0);
			} catch (IntentSender.SendIntentException e) {
				Log.e(TAG, e.toString(), e);
			}
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
//		loadData();
		setProfileInfo();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

	public static final String[] PROJECTION = new String[] {
		VideoTable._ID,
		VideoTable.COLUMN_NAME_DATA1,
		VideoTable.COLUMN_NAME_DATA2,
		VideoTable.COLUMN_NAME_DATA3,
		VideoTable.COLUMN_NAME_DATA4,
		VideoTable.COLUMN_NAME_DATA5,
		VideoTable.COLUMN_NAME_DATA6,
		VideoTable.COLUMN_NAME_DATA7,
		VideoTable.COLUMN_NAME_DATA8,
		VideoTable.COLUMN_NAME_DATA9,
		VideoTable.COLUMN_NAME_DATA10
    };
	
	public static final String[] PROJECTION_CHANNEL = new String[] {
		ChannelTable._ID,
		ChannelTable.COLUMN_NAME_DATA1,
		ChannelTable.COLUMN_NAME_DATA2,
		ChannelTable.COLUMN_NAME_DATA3,
		ChannelTable.COLUMN_NAME_DATA4,
    };
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader cl = null;
		if (id == 0){
			 cl = new CursorLoader(this, VideoTable.CONTENT_URI,
                PROJECTION, null, null, null);
		}else if ( id == 1){
			 cl = new CursorLoader(this, ChannelTable.CONTENT_URI,
					 PROJECTION_CHANNEL, null, null, null);		
		}
        return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (loader.getId() == 0){
			mVideoAdapter.swapCursor(data);
			if(data.getCount() == 0){
				mainListView.setVisibility(View.GONE);
			}else{
				mainListView.setVisibility(View.VISIBLE);
			}
		}else if (loader.getId() == 1){
			
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == 0){
			mVideoAdapter.swapCursor(null);
		}else if (loader.getId() == 1){
			
		}
	}
	
	public static String parseVideoLink(String videoUrl) {
        String id = "";
        if(videoUrl.indexOf("&feature")!= -1){
     	   id = videoUrl.substring(videoUrl.indexOf("v=")+2, videoUrl.indexOf("&feature"));
        }else{
     	   id = videoUrl.substring(videoUrl.indexOf("videos/")+7, videoUrl.indexOf("?v=2"));
        }
 		return id;
 	}
	
	private void updateVideos(ContentResolver cr, int updateNums, String id) {
		// TODO Auto-generated method stub
		mVideos = ChannelApi.getChannelVideo(id, 0, "", updateNums);
		for (int i = 0; i< mVideos.size(); i++){
			YoutubeVideo video = mVideos.get(mVideos.size()-1-i);
			ContentValues videoValues = new ContentValues();
			videoValues.put(VideoTable.COLUMN_NAME_DATA1, video.getTitle());
			videoValues.put(VideoTable.COLUMN_NAME_DATA2, MainActivity.parseVideoLink(video.getLink()));
			videoValues.put(VideoTable.COLUMN_NAME_DATA3, video.getThumbnail());
        	// date to string 
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
            final String dateString = formatter.format(video.getUploadDate()); 
            videoValues.put(VideoTable.COLUMN_NAME_DATA4, dateString);
        	
            videoValues.put(VideoTable.COLUMN_NAME_DATA5, video.getViewCount());
            videoValues.put(VideoTable.COLUMN_NAME_DATA6, video.getDuration());
            videoValues.put(VideoTable.COLUMN_NAME_DATA7, video.getLikes());
            videoValues.put(VideoTable.COLUMN_NAME_DATA8, video.getDislikes());
            videoValues.put(VideoTable.COLUMN_NAME_DATA9, 0);
            videoValues.put(VideoTable.COLUMN_NAME_DATA10, id);
        	cr.insert(VideoTable.CONTENT_URI, videoValues);
		}	
	}

	@Override
	 protected void onStop() {
	  // TODO Auto-generated method stub
	  super.onStop();
	  SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		sp.edit().putBoolean(Initialized_Key, isInitialized).commit();
	 }
	
	 
	
}
