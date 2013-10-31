package com.taiwan.imageload;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeIntents;
import com.kosbrother.youtubefeeder.MainActivity;
import com.kosbrother.youtubefeeder.R;
import com.youtube.music.channels.entity.Channel;

public class ListChannelAdapter extends BaseAdapter {

    private final Activity        activity;
    private final ArrayList<Channel> data;
    private static LayoutInflater inflater = null;
    public ImageLoader            imageLoader;
//    private String mChannelTitle;

    public ListChannelAdapter(Activity a, ArrayList<Channel> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext(), 70);
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
        View vi = convertView;
        if (convertView == null)
        	vi = inflater.inflate(R.layout.item_channel_list, null);
               
	        TextView textTitle = (TextView) vi.findViewById(R.id.text_playlist_title);
	        TextView textId = (TextView) vi.findViewById(R.id.text_playlist_id);
	        ImageView image = (ImageView) vi.findViewById(R.id.image_list);
	        
	        textTitle.setText(data.get(position).getTitle());
	        textId.setText(data.get(position).getId());
	        
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
					
					// set MainActivity to refresh
					MainActivity.isRefreshList = true;
	                
	            }
	        });
        
        
        return vi;
    }
}