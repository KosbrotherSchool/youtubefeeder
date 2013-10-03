package com.kosbrother.youtubefeeder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
	
	private static boolean is_running = false;
	
    @Override
    public void onReceive(final Context context, final Intent intent) {
    	
    	Thread thread = new Thread()
    	{
    	    @Override
    	    public void run() {
    	        try {    	        	  
    	              sleep(20000);
    	              Log.i("NetworkChangeReceiver", "After sleep");
    	              int connection = NetworkUtil.getConnectivityStatus(context);
    	              Log.i("NetworkChangeReceiver", "Net type " + connection );
    	              
    	              if (connection == NetworkUtil.TYPE_WIFI){
    	            	Intent intentService = new Intent(context, UpdateVideosService.class);
    	            	context.startService(intentService);
    	              }
    	              is_running = false;
    	            
    	        } catch (InterruptedException e) {
    	            e.printStackTrace();
    	        }
    	    }
    	};
    	
    	Log.i("NetworkChangeReceiver", "get in the net receiver");
    	if (!is_running){
    		thread.start();
    		is_running =true;
    		Log.i("NetworkChangeReceiver", "is running = " + is_running );
    	}
    	
    }


}
