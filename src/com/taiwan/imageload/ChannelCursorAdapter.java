package com.taiwan.imageload;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kosbrother.youtubefeeder.R;
import com.kosbrother.youtubefeeder.database.ChannelTable;
import com.kosbrother.youtubefeeder.database.VideoTable;

public class ChannelCursorAdapter extends SimpleCursorAdapter {

    private Context mContext;
    private int layout;
    private Cursor cr;
    private final LayoutInflater inflater;
    public ImageLoader imageLoader;

    @SuppressWarnings("deprecation")
	public ChannelCursorAdapter(Context context,int layout, Cursor c,String[] from,int[] to) {
        super(context,layout,c,from,to);
        this.layout=layout;
        this.mContext = context;
        this.inflater=LayoutInflater.from(context);
        this.cr=c;
        imageLoader = new ImageLoader(mContext, 70);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        super.bindView(view, context, cursor);
        
        TextView textTitle = (TextView) view.findViewById(R.id.list_item_entry_title);
        ImageView image = (ImageView) view.findViewById(R.id.list_item_entry_drawable);
        TextView textId = (TextView) view.findViewById(R.id.list_item_entry_summary); 
        
        /**
         * public static final String COLUMN_NAME_DATA1 = "channel_id";
         * public static final String COLUMN_NAME_DATA2 = "channel_title";
         * public static final String COLUMN_NAME_DATA3 = "channel_thumbnail";
         * public static final String COLUMN_NAME_DATA4 = "channel_videoNums";
         */
        
        int id_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA1);
        int title_index  = cursor.getColumnIndex(ChannelTable.COLUMN_NAME_DATA2);
        int thumbnail_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA3);     
//        int videoNums_index = cursor.getColumnIndex(VideoTable.COLUMN_NAME_DATA4);
        
        // set title text
        textTitle.setText(cursor.getString(title_index));
        
        // set textId
        textId.setText(cursor.getString(id_index));

        
        // set image
        imageLoader.DisplayImage(cursor.getString(thumbnail_index), image);

    }
    
    
    
}
