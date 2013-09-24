package com.taiwan.imageload;

import java.util.ArrayList;

import com.kosbrother.youtubefeeder.PlaylistVideosActivity;
import com.kosbrother.youtubefeeder.R;
import com.youtube.music.channels.entity.YoutubePlaylist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ListPlaylistAdapter extends BaseAdapter {

    private final Activity        activity;
    private final ArrayList<YoutubePlaylist> data;
    private static LayoutInflater inflater = null;
    public ImageLoader            imageLoader;

    public ListPlaylistAdapter(Activity a, ArrayList<YoutubePlaylist> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext(), 70);
        
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
        	vi = inflater.inflate(R.layout.item_playlist_item, null);
               
	        TextView textTitle = (TextView) vi.findViewById(R.id.text_playlist_title);
	        TextView textId = (TextView) vi.findViewById(R.id.text_playlist_id);
	        ImageView image = (ImageView) vi.findViewById(R.id.image_list);
	        
	        textTitle.setText(data.get(position).getTitle());
	        textId.setText(data.get(position).getListId());
	        
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
	            	
	            	Intent intent = new Intent(activity, PlaylistVideosActivity.class);
	 				Bundle bundle = new Bundle();
	 				bundle.putString("ListId", id); 
	 				bundle.putString("ListTitle", title);
	 				intent.putExtras(bundle);
	 				activity.startActivity(intent); 
	                
	            }
	        });
        
        
        return vi;
    }
}
