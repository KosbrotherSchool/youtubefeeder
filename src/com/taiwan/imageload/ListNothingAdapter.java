package com.taiwan.imageload;

import com.kosbrother.youtubefeeder.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListNothingAdapter extends BaseAdapter {
    
    private Activity activity;
    private static LayoutInflater inflater=null;

    
    public ListNothingAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        return 1;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.item_nothing, null);
	        TextView text=(TextView)vi.findViewById(R.id.text_no_data);
	        text.setText(activity.getResources().getString(R.string.no_data));
        return vi;
    }
}