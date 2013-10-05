package com.kosbrother.youtubefeeder;

import java.util.Arrays;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity implements ConnectionCallbacks,
OnConnectionFailedListener{

	private TextView textAccount;
	private Button buttonTimer;
	private RadioButton radioNotify;
	private RadioButton radioUnNotify;

	private String mChosenAccountName;
	private boolean isNotify;
	private int notifyTime; // hours

	public static final String NOTIFY_KEY = "IsNotify";
	public static final String NOTIFY_TIMER_KEY = "NotifyTimer";

	GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();
	private static final int REQUEST_ACCOUNT_PICKER = 2;
	
	private PlusClient mPlusClient;
	
	private static final String TAG = "SettingActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting);

		textAccount = (TextView) findViewById(R.id.text_current_account);
		buttonTimer = (Button) findViewById(R.id.button_notify_time);
		radioNotify = (RadioButton) findViewById(R.id.radio_notify);
		radioUnNotify = (RadioButton) findViewById(R.id.radio_unnotify);

		getDatas();
		setDatas();
	}

	private void setDatas() {
		textAccount.setText(mChosenAccountName);
		buttonTimer.setText("每"+notifyTime+"小時");
		
		textAccount.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseAccount();
			}
		});

		buttonTimer.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater inflater = SettingActivity.this.getLayoutInflater();
				final View dialogView = inflater.inflate(R.layout.layout_num_picker, null);
				NumberPicker mPicker = (NumberPicker) dialogView.findViewById(R.id.number_picker);
                mPicker.setMaxValue(24);
                mPicker.setMinValue(1);
                mPicker.setValue(notifyTime);
				
				new AlertDialog.Builder(SettingActivity.this).setTitle("選擇更新時間")
				.setView(dialogView)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    	NumberPicker mPicker = (NumberPicker) dialogView.findViewById(R.id.number_picker);
                    	notifyTime = mPicker.getValue();
                    	buttonTimer.setText("每"+notifyTime+"小時");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }

                }).show();
			}
		});

		if (isNotify) {
			radioNotify.setChecked(true);
			radioUnNotify.setChecked(false);
		} else {
			radioNotify.setChecked(false);
			radioUnNotify.setChecked(true);
		}
	}

	private void getDatas() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mChosenAccountName = sp.getString(MainActivity.ACCOUNT_KEY, null);
		isNotify = sp.getBoolean(NOTIFY_KEY, true);
		notifyTime = sp.getInt(NOTIFY_TIMER_KEY, 3);
		// get GoogleAccount OAuth
		credential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(Auth.SCOPES));
		// set exponential backoff policy
		credential.setBackOff(new ExponentialBackOff());
		if (mChosenAccountName !=null){
			credential.setSelectedAccountName(mChosenAccountName);
		}else{
			textAccount.setText("點擊設置帳號");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (radioNotify.isChecked()) {
			isNotify = true;
		} else {
			isNotify = false;
		}
		sp.edit().putBoolean(NOTIFY_KEY, isNotify).commit();
		sp.edit().putInt(NOTIFY_TIMER_KEY, notifyTime).commit();
	}

	@Override
	public void onResume() {
		super.onResume();
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
					
					textAccount.setText(mChosenAccountName);
					
					mPlusClient = new PlusClient.Builder(SettingActivity.this, this, this)
					.setScopes(Auth.SCOPES)
					.setAccountName(mChosenAccountName)
					.build();
					mPlusClient.connect();
					
				}
			}
			break;
		}
	}
	
	private void saveAccount() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.edit().putString(MainActivity.ACCOUNT_KEY, mChosenAccountName).commit();
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
	
	public void setProfileInfo() {	
        if (!mPlusClient.isConnected() || mPlusClient.getCurrentPerson() == null) {            
        	Toast.makeText(SettingActivity.this, "plus sign in failed", Toast.LENGTH_SHORT).show();
        } else {
        	Person currentPerson = mPlusClient.getCurrentPerson();
            SharedPreferences sp = PreferenceManager
    				.getDefaultSharedPreferences(this);
    		sp.edit().putString(MainActivity.ACCOUNT_DISPLAY_NAME_KEY, currentPerson.getDisplayName()).commit();
    		sp.edit().putString(MainActivity.ACCOUNT_IMAGE_KEY, currentPerson.getImage().getUrl()).commit();
        }
    }
	
}