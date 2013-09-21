package com.taiwan.imageload;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kosbrother.youtubefeeder.PlayerViewActivity;
import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.database.VideoTable;

public class VideoCursorAdapter extends SimpleCursorAdapter {

    private Context mContext;
    private int layout;
//    private Cursor cr;
    private final LayoutInflater inflater;
    public ImageLoader imageLoader;

    @SuppressWarnings("deprecation")
	public VideoCursorAdapter(Context context,int layout, Cursor c,String[] from,int[] to) {
        super(context,layout,c,from,to);
        this.layout=layout;
        this.mContext = context;
        this.inflater=LayoutInflater.from(context);
//        this.cr=c;
        imageLoader = new ImageLoader(mContext, 70);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        super.bindView(view, context, cursor);	
    	
//    	cr = cursor;
    	
        TextView textTitle = (TextView) view.findViewById(R.id.text_news_list);
        ImageView image = (ImageView) view.findViewById(R.id.image_news_list);
        TextView textDate = (TextView) view.findViewById(R.id.text_list_date);
        TextView textViews = (TextView) view.findViewById(R.id.text_list_views);
        TextView textDuration = (TextView) view.findViewById(R.id.text_list_duration);
        TextView textLikes = (TextView) view.findViewById(R.id.text_list_like);
        TextView textId = (TextView) view.findViewById(R.id.text_id);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_video);
        
        
        
        /**
         * tttt,ttt
         * public static final String COLUMN_NAME_DATA1 = "video_title";
         * public static final String COLUMN_NAME_DATA2 = "video_link";
    	 * public static final String COLUMN_NAME_DATA3 = "video_thumbnail";
    	 * public static final String COLUMN_NAME_DATA4 = "video_uploadTime";
    	 * public static final String COLUMN_NAME_DATA5 = "video_viewCount";
    	 * public static final String COLUMN_NAME_DATA6 = "video_duration";
    	 * public static final String COLUMN_NAME_DATA7 = "video_likes";
    	 * public static final String COLUMN_NAME_DATA8 = "video_dislikes";
         */
        
        int title_index  = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA1);
        int link_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA2);
        int thumbnail_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA3);
        int uploadTime_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA4);
        int viewCount_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA5);
        int duration_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA6);
        int likes_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA7);
//      int dislikes_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA8);
        
        textId.setText(parseVideoLink(cursor.getString(link_index)));
        
        
        // set title text
        textTitle.setText(cursor.getString(title_index));
        
        // set views text
        int views = cursor.getInt(viewCount_index);
        textViews.setText(NumberFormat.getNumberInstance(Locale.US).format(views)+" "+mContext.getResources().getString(R.string.views));
        
        // set likes text
        textLikes.setText(Integer.toString(cursor.getInt(likes_index))+" "+mContext.getResources().getString(R.string.likes));
        
        // set date text
        textDate.setText(mContext.getResources().getString(R.string.launch)+": "+cursor.getInt(uploadTime_index));
        
        // set duration text
        int[] intTime = splitToComponentTimes(cursor.getInt(duration_index));
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
        imageLoader.DisplayImage(cursor.getString(thumbnail_index), image);
        
        
//        final String videoId = parseVideoLink(cursor.getString(link_index));
//        final String videoTitle = cursor.getString(title_index);
        
        // set view onClick Listener
    	view.setClickable(true);
    	view.setFocusable(true);
    	view.setBackgroundResource(android.R.drawable.menuitem_background);
    	view.setOnClickListener(new OnClickListener() {
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
	
	
    
}
