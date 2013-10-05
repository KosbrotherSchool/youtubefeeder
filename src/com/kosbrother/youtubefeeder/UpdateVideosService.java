package com.kosbrother.youtubefeeder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.kosbrother.youtubefeeder.api.ChannelApi;
import com.kosbrother.youtubefeeder.database.ChannelTable;
import com.kosbrother.youtubefeeder.database.VideoTable;
import com.youtube.music.channels.entity.Channel;
import com.youtube.music.channels.entity.YoutubeVideo;

public class UpdateVideosService extends Service {

	public static final String ACCOUNT_KEY = "accountName";
	private String mChosenAccountName;

	GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();

	private ArrayList<YoutubeVideo> mVideos = new ArrayList<YoutubeVideo>();

	enum State {
		Stopped, Running
	};

	static State mState = State.Stopped;

	private static ServiceThread serviceThread;

	Notification mNotification = null;
	final int NOTIFICATION_ID = 1;

	private int num_update_videos = 0;
	private boolean isNotify;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("UpdateVideosService", "StartService onStartCommand");
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		isNotify = sp.getBoolean(SettingActivity.NOTIFY_KEY, true);

		// get GoogleAccount OAuth
		credential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(Auth.SCOPES));
		// set exponential backoff policy
		credential.setBackOff(new ExponentialBackOff());
		loadAccount();
		credential.setSelectedAccountName(mChosenAccountName);

		if (mChosenAccountName != null) {
			checkChannelVideoNumsAndUpdate();
		}

		return START_NOT_STICKY;

	}

	private void checkChannelVideoNumsAndUpdate() {
		// TODO Auto-generated method stub
		mState = State.Running;
		serviceThread = new ServiceThread();
		new Thread(serviceThread).start();
	}

	private void stopUpdate() {
		// stop thread
		mState = State.Stopped;
		stopForeground(true);
	}

	@SuppressLint("SimpleDateFormat")
	public class ServiceThread implements Runnable {

		public ServiceThread() {
			super();
		}

		@Override
		public void run() {
			Log.i("UpdateVideosService", "StartService Video Update");

			YouTube youtube = new YouTube.Builder(transport, jsonFactory,
					credential).setApplicationName(Constants.APP_NAME).build();

			ContentResolver cr = getContentResolver();

			// delete viewd videos
			cr.delete(VideoTable.CONTENT_URI, VideoTable.COLUMN_NAME_DATA9
					+ " = ?", new String[] { "1" });

			SubscriptionListResponse mSubscriptions;
			try {
				mSubscriptions = youtube.subscriptions()
						.list("snippet,contentDetails").setMine(true)
						.setMaxResults((long) 20).execute();

				List<Subscription> lists = mSubscriptions.getItems();

				for (Subscription item : lists) {
					String channelId = item.getSnippet().getResourceId()
							.getChannelId();
					String channelTitle = item.getSnippet().getTitle();
					String channelPicUrl = item.getSnippet().getThumbnails()
							.getDefault().getUrl();
					int channelTotalNums = 0;
					try {
						channelTotalNums = item.getContentDetails()
								.getTotalItemCount().intValue();
					} catch (Exception e) {
						Log.e("MainActivity", channelTitle
								+ "no total item count");
					}

					Channel theChannel = new Channel(channelId, channelTitle,
							channelPicUrl, channelTotalNums);

					// String channelId =
					// item.getSnippet().getResourceId().getChannelId();

					Cursor theChannelCursor = cr.query(
							ChannelTable.CONTENT_URI,
							MainActivity.PROJECTION_CHANNEL,
							ChannelTable.COLUMN_NAME_DATA1 + " = ?",
							new String[] { channelId }, null);
					theChannelCursor.moveToFirst();

					if (theChannelCursor.getCount() != 0) {
						int videoNums_index = theChannelCursor
								.getColumnIndex(ChannelTable.COLUMN_NAME_DATA4);
						int dataChannelNums = theChannelCursor
								.getInt(videoNums_index);
						if (dataChannelNums < theChannel.getVideoNums()) {
							int updateNums = theChannel.getVideoNums()
									- dataChannelNums;
							num_update_videos = num_update_videos + updateNums;
							updateVideos(cr, updateNums, theChannel.getId(),
									theChannel.getTitle());
							ContentValues values = new ContentValues();
							values.put(ChannelTable.COLUMN_NAME_DATA4,
									theChannel.getVideoNums());
							cr.update(ChannelTable.CONTENT_URI, values,
									ChannelTable.COLUMN_NAME_DATA1 + " = ?",
									new String[] { channelId });
						} else if (dataChannelNums > theChannel.getVideoNums()) {
							ContentValues values = new ContentValues();
							values.put(ChannelTable.COLUMN_NAME_DATA4,
									theChannel.getVideoNums());
							cr.update(ChannelTable.CONTENT_URI, values,
									ChannelTable.COLUMN_NAME_DATA1 + " = ?",
									new String[] { channelId });
						}
					} else {
						ContentValues values = new ContentValues();
						values.put(ChannelTable.COLUMN_NAME_DATA1,
								theChannel.getId());
						values.put(ChannelTable.COLUMN_NAME_DATA2,
								theChannel.getTitle());
						values.put(ChannelTable.COLUMN_NAME_DATA3,
								theChannel.getThumbnail());
						values.put(ChannelTable.COLUMN_NAME_DATA4,
								theChannel.getVideoNums());
						cr.insert(ChannelTable.CONTENT_URI, values);
						updateVideos(cr, 2, theChannel.getId(),
								theChannel.getTitle());
						num_update_videos = num_update_videos + 2;
					}
				}
				if (isNotify){
					if (num_update_videos > 0) {
						setUpAsForeground("更新" + num_update_videos + "新影片");
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private void updateVideos(ContentResolver cr, int updateNums, String id,
			String channel_title) {
		// TODO Auto-generated method stub
		mVideos = ChannelApi.getChannelVideo(id, 0, "", updateNums);
		if (mVideos != null) {
			for (int i = 0; i < mVideos.size(); i++) {
				YoutubeVideo video = mVideos.get(mVideos.size() - 1 - i);
				ContentValues videoValues = new ContentValues();
				videoValues.put(VideoTable.COLUMN_NAME_DATA1, video.getTitle());
				videoValues.put(VideoTable.COLUMN_NAME_DATA2,
						MainActivity.parseVideoLink(video.getLink()));
				videoValues.put(VideoTable.COLUMN_NAME_DATA3,
						video.getThumbnail());
				// date to string
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
				final String dateString = formatter.format(video
						.getUploadDate());
				videoValues.put(VideoTable.COLUMN_NAME_DATA4, dateString);

				videoValues.put(VideoTable.COLUMN_NAME_DATA5,
						video.getViewCount());
				videoValues.put(VideoTable.COLUMN_NAME_DATA6,
						video.getDuration());
				videoValues.put(VideoTable.COLUMN_NAME_DATA7, video.getLikes());
				videoValues.put(VideoTable.COLUMN_NAME_DATA8,
						video.getDislikes());
				videoValues.put(VideoTable.COLUMN_NAME_DATA9, 0);
				videoValues.put(VideoTable.COLUMN_NAME_DATA10, id);
				videoValues.put(VideoTable.COLUMN_NAME_DATA11, channel_title);
				cr.insert(VideoTable.CONTENT_URI, videoValues);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	void setUpAsForeground(String text) {

		// 取得通知管理器
		NotificationManager noMgr = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// 當使用者按下通知欄中的通知時要開啟的 Activity
		Intent call = new Intent(this, MainActivity.class);
		// 建立待處理意圖
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, call, 0);
		// 指定通知欄位要顯示的圖示
		int icon = R.drawable.ic_launcher;
		// 指定通知出現時要顯示的文字,幾秒後會消失只剩圖示
		String ticket = "YoutubeFeeder更新";
		// 何時送出通知,傳入當前時間則立即發出通知
		long when = System.currentTimeMillis();
		// 建立通知物件
		Notification notification = new Notification(icon, ticket, when);

		// Cancel the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// 指定通知標題
		String title = "YoutubeFeeder";
		// 通知內容
		String desc = text;
		// 設定事件資訊
		notification.setLatestEventInfo(this, title, desc, pIntent);

		// 執行通知
		noMgr.notify(1, notification);
	}

	private void loadAccount() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mChosenAccountName = sp.getString(ACCOUNT_KEY, null);
	}

	public static void setUpdateService(Context theContext, int hours) {
		AlarmManager am = (AlarmManager) theContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(theContext, UpdateVideosService.class);
		PendingIntent mAlarmSender = PendingIntent.getService(theContext, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),1000 * 60 * 60 * hours, mAlarmSender);
//		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),1000*10, mAlarmSender); // repeat every 10 seconds
	}
	
	public static void cancelUpdateService(Context theContext) {
		AlarmManager alarmManager = (AlarmManager) theContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(theContext, UpdateVideosService.class);
		PendingIntent mAlarmSender = PendingIntent.getService(theContext, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(mAlarmSender);
	}
}