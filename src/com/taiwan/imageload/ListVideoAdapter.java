package com.taiwan.imageload;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.kosbrother.youtubefeeder.PlayerViewActivity;
import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.SearchActivity;
import com.kosbrother.youtubefeeder.fragments.NewVideosFragment;
import com.kosbrother.youtubefeeder.fragments.PlaylistVideosFragment;
import com.youtube.music.channels.entity.YoutubeVideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class ListVideoAdapter extends BaseAdapter {

    private final Activity   mActivity;
    private final ArrayList<YoutubeVideo> data;
    private static LayoutInflater inflater = null;
    public ImageLoader            imageLoader;
    private static HashMap<String,String> checkMap = new HashMap<String, String>();
    private String channel_author;
    
    public ListVideoAdapter(Activity context, ArrayList<YoutubeVideo> d, String channel_title) {
    	mActivity = context;
        data = d;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(mActivity.getApplicationContext(), 70);
        channel_author = channel_title;
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

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
        	vi = inflater.inflate(R.layout.item_video_list, null);
        
        TextView textTitle = (TextView) vi.findViewById(R.id.text_news_list);
        ImageView image = (ImageView) vi.findViewById(R.id.image_news_list);
        TextView textDate = (TextView) vi.findViewById(R.id.text_list_date);
        TextView textViews = (TextView) vi.findViewById(R.id.text_list_views);
        TextView textDuration = (TextView) vi.findViewById(R.id.text_list_duration);
        TextView textLikes = (TextView) vi.findViewById(R.id.text_list_like);
        TextView textId = (TextView) vi.findViewById(R.id.text_id);
        CheckBox checkBox = (CheckBox) vi.findViewById(R.id.checkbox_video);
        TextView textAuthor = (TextView) vi.findViewById(R.id.text_author);
        
        // set id
        String mId = parseVideoLink(data.get(position).getLink());      
        textId.setText(mId);
        
        if(channel_author.equals("")){
        	textAuthor.setVisibility(View.GONE);
        }else{
        	textAuthor.setText("by "+ channel_author);
        }
        // set title text
        textTitle.setText(data.get(position).getTitle());
        
        // set views text
        int views = data.get(position).getViewCount();
        textViews.setText(NumberFormat.getNumberInstance(Locale.US).format(views)+" "+mActivity.getResources().getString(R.string.views));
        
        // set likes text
        textLikes.setText(data.get(position).getLikes()+" "+mActivity.getResources().getString(R.string.likes));
        
        // set date text
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
        final String dateString = formatter.format(data.get(position).getUploadDate()); 
        textDate.setText(mActivity.getResources().getString(R.string.launch_time)+": "+dateString);
        
        // set duration text
        int[] intTime = splitToComponentTimes(data.get(position).getDuration());
        if(intTime[0]!=0){
        	textDuration.setText(mActivity.getResources().getString(R.string.time)+":"+Integer.toString(intTime[0])+":"+Integer.toString(intTime[1])+":"+Integer.toString(intTime[2]));
        }else{
        	String timeSecond = "";
        	if(intTime[2]<10){
        		timeSecond = "0"+Integer.toString(intTime[2]);
        	}else{
        		timeSecond = Integer.toString(intTime[2]);
        	}
        	textDuration.setText(mActivity.getResources().getString(R.string.time)+":"+Integer.toString(intTime[1])+":"+timeSecond);
        }
        
        // set image
        imageLoader.DisplayImage(data.get(position).getThumbnail(), image);
        
        if (checkMap.get(mId) != null){
        	checkBox.setChecked(true);
        }else{
        	checkBox.setChecked(false);
        }
        
        checkBox.setOnClickListener((new OnClickListener(){  
            public void onClick(View v) {          	
				String id = parseVideoLink(data.get(position).getLink());			
				String title = data.get(position).getTitle();
				
            	CheckBox theCheckBox = (CheckBox) v.findViewById(R.id.checkbox_video);
            	if(theCheckBox.isChecked()){
            		checkMap.put(id, title);
            		String aTitle = mActivity.getClass().getName();           		
            		if (aTitle.equals("com.kosbrother.youtubefeeder.ChannelTabs")){
            			NewVideosFragment.showActionMode();
            		}else if(aTitle.equals("com.kosbrother.youtubefeeder.PlaylistVideosActivity") ) {
            			PlaylistVideosFragment.showActionMode();
            		}else if(aTitle.equals("com.kosbrother.youtubefeeder.SearchActivity")){
            			SearchActivity.showActionMode();
            		}
            	}else{
            		checkMap.remove(id);
            	}
            }  
        }));
        
        // set view onClick Listener
        vi.setClickable(true);
        vi.setFocusable(true);
        vi.setBackgroundResource(android.R.drawable.menuitem_background);
        vi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	TextView idView = (TextView) v.findViewById(R.id.text_id);
				String id = idView.getText().toString();			
				TextView titleView = (TextView) v.findViewById(R.id.text_news_list);
				String title = titleView.getText().toString();
				
            	Intent intent = new Intent(mActivity, PlayerViewActivity.class);
            	intent.putExtra("VideoId", id);
            	intent.putExtra("VideoTitle", title);
            	mActivity.startActivity(intent);
            }

        });
        
        return vi;
    }
    
    public static int[] splitToComponentTimes(int i)
    {
        long longVal = (long)i;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }
    
    private String parseVideoLink(String videoUrl) {
       String id = "";
       if(videoUrl.indexOf("&feature")!= -1){
    	   id = videoUrl.substring(videoUrl.indexOf("v=")+2, videoUrl.indexOf("&feature"));
       }else{
    	   id = videoUrl.substring(videoUrl.indexOf("videos/")+7, videoUrl.indexOf("?v=2"));
       }
		return id;
	}
    
    public static HashMap<String,String> getMap(){
		return checkMap;
	}
}
