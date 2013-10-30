package com.taiwan.imageload;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeIntents;
import com.kosbrother.youtubefeeder.ManageChannelsActivity;
import com.kosbrother.youtubefeeder.R;
import com.youtube.music.channels.entity.SubscribeChannel;

public class ListChannelManageAdapter extends BaseAdapter {

    private final Activity        activity;
    private final ArrayList<SubscribeChannel> data;
    private static LayoutInflater inflater = null;
    public ImageLoader            imageLoader;
//    private String mChannelTitle;
    private static HashMap<String,String> checkMap = new HashMap<String, String>();
    
    
    public ListChannelManageAdapter(Activity a, ArrayList<SubscribeChannel> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext(), 70);
        checkMap.clear();
//        mChannelTitle = channelTitle;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final View vi = inflater.inflate(R.layout.item_channel__list_manage, null);
//        if (convertView == null)
//        	vi = inflater.inflate(R.layout.item_channel__list_manage, null);
               
	        TextView textTitle = (TextView) vi.findViewById(R.id.text_playlist_title);
	        TextView textId = (TextView) vi.findViewById(R.id.text_playlist_id);
	        TextView textSubId = (TextView) vi.findViewById(R.id.text_sub_id);
	        ImageView image = (ImageView) vi.findViewById(R.id.image_list);
	        CheckBox checkBox = (CheckBox) vi.findViewById(R.id.checkbox_channel);
	        
	        textTitle.setText(data.get(position).getTitle());
	        textId.setText(data.get(position).getId());
	        textSubId.setText(data.get(position).getSubId());
	        
	        String mId = data.get(position).getId();
	        
	        imageLoader.DisplayImage(data.get(position).getThumbnail(), image);
	
	        vi.setClickable(true);
	        vi.setFocusable(true);
	        vi.setBackgroundResource(android.R.drawable.menuitem_background);
	        vi.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	
	            	TextView idView = (TextView) v.findViewById(R.id.text_playlist_id);
					String id = idView.getText().toString();			
					TextView titleView = (TextView) v.findViewById(R.id.text_playlist_title);
					String title = titleView.getText().toString();
//	            	
					Intent intent = YouTubeIntents.createUserIntent(activity, id);
					activity.startActivity(intent);
	                
	            }
	        });
	        
	        
	        if (checkMap.get(mId) != null){
	        	checkBox.setChecked(true);
	        }else{
	        	checkBox.setChecked(false);
	        }
	        
	        checkBox.setOnClickListener((new OnClickListener(){  
	            public void onClick(View v) {          	
	            	TextView idView = (TextView) vi.findViewById(R.id.text_playlist_id);
					String id = idView.getText().toString();			
					TextView subIdView = (TextView) vi.findViewById(R.id.text_sub_id);
					String subId = subIdView.getText().toString();
					
	            	CheckBox theCheckBox = (CheckBox) v.findViewById(R.id.checkbox_channel);
	            	if(theCheckBox.isChecked()){
	            		checkMap.put(id, subId);
	            		ManageChannelsActivity.showActionMode();
	            	}else{
	            		checkMap.remove(id);
	            	}
	            }  
	        }));
        
        return vi;
    }
    
    public static HashMap<String,String> getMap(){
		return checkMap;
	}
    
    public void clearCheckMap(){
    	checkMap.clear();
    }
}
