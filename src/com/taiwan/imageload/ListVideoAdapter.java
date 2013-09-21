package com.taiwan.imageload;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.kosbrother.youtubefeeder.ChannelTabs;
import com.kosbrother.youtubefeeder.MainActivity;
import com.kosbrother.youtubefeeder.PlayerViewActivity;
import com.kosbrother.youtubefeeder.R;
import com.youtube.music.channels.entity.YoutubeVideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class ListVideoAdapter extends BaseAdapter {

    private final Context   mContext;
    private final ArrayList<YoutubeVideo> data;
    private static LayoutInflater inflater = null;
    public ImageLoader            imageLoader;

    public ListVideoAdapter(Context context, ArrayList<YoutubeVideo> d) {
    	mContext = context;
        data = d;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(mContext.getApplicationContext(), 70);
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
					
	            	Intent intent = new Intent(mContext, PlayerViewActivity.class);
	            	intent.putExtra("VideoId", id);
	            	intent.putExtra("VideoTitle", title);
	            	mContext.startActivity(intent);
	            }
	
	        });
        	   
        
        TextView textTitle = (TextView) vi.findViewById(R.id.text_news_list);
        ImageView image = (ImageView) vi.findViewById(R.id.image_news_list);
        TextView textDate = (TextView) vi.findViewById(R.id.text_list_date);
        TextView textViews = (TextView) vi.findViewById(R.id.text_list_views);
        TextView textDuration = (TextView) vi.findViewById(R.id.text_list_duration);
        TextView textLikes = (TextView) vi.findViewById(R.id.text_list_like);
        TextView textId = (TextView) vi.findViewById(R.id.text_id);
        
        // set id
        textId.setText(parseVideoLink(data.get(position).getLink()));
        
        // set title text
        textTitle.setText(data.get(position).getTitle());
        
        // set views text
        int views = data.get(position).getViewCount();
        textViews.setText(NumberFormat.getNumberInstance(Locale.US).format(views)+" "+mContext.getResources().getString(R.string.views));
        
        // set likes text
        textLikes.setText(data.get(position).getLikes()+" "+mContext.getResources().getString(R.string.likes));
        
        // set date text
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
        final String dateString = formatter.format(data.get(position).getUploadDate()); 
        textDate.setText(mContext.getResources().getString(R.string.launch)+": "+dateString);
        
        // set duration text
        int[] intTime = splitToComponentTimes(data.get(position).getDuration());
        if(intTime[0]!=0){
        	textDuration.setText(mContext.getResources().getString(R.string.time)+":"+Integer.toString(intTime[0])+":"+Integer.toString(intTime[1])+":"+Integer.toString(intTime[2]));
        }else{
        	String timeSecond = "";
        	if(intTime[2]<10){
        		timeSecond = "0"+Integer.toString(intTime[2]);
        	}else{
        		timeSecond = Integer.toString(intTime[2]);
        	}
        	textDuration.setText(mContext.getResources().getString(R.string.time)+":"+Integer.toString(intTime[1])+":"+timeSecond);
        }
        
        // set image
        imageLoader.DisplayImage(data.get(position).getThumbnail(), image);
        
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
}
