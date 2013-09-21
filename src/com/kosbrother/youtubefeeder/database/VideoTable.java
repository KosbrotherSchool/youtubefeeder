package com.kosbrother.youtubefeeder.database;

import com.kosbrother.youtubefeeder.MainActivity;

import android.net.Uri;
import android.provider.BaseColumns;

public class VideoTable implements BaseColumns {

    // This class cannot be instantiated
    private VideoTable() {}

    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "video";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse("content://" + MainActivity.AUTHORITY + "/"+TABLE_NAME);

    /**
     * The content URI base for a single row of data. Callers must
     * append a numeric row id to this Uri to retrieve a row
     */
    public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse("content://" + MainActivity.AUTHORITY + "/"+TABLE_NAME+"/");

    /**
     * The MIME type of {@link #CONTENT_URI}.
     */
    public static final String CONTENT_TYPE
            = "vnd.android.cursor.dir/vnd.example.api-demos-throttle";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
     */
    public static final String CONTENT_ITEM_TYPE
            = "vnd.android.cursor.item/vnd.example.api-demos-throttle";
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = _ID+" COLLATE LOCALIZED DESC";

    /**
     * Column name for the single column holding our data.
     * <P>Type: TEXT</P>
     * String title;
     */
    
    public static final String COLUMN_NAME_DATA1 = "video_title";
    public static final String COLUMN_NAME_DATA2 = "video_link";
    public static final String COLUMN_NAME_DATA3 = "video_thumbnail";
    public static final String COLUMN_NAME_DATA4 = "video_uploadTime";
    public static final String COLUMN_NAME_DATA5 = "video_viewCount";
    public static final String COLUMN_NAME_DATA6 = "video_duration";
    public static final String COLUMN_NAME_DATA7 = "video_likes";
    public static final String COLUMN_NAME_DATA8 = "video_dislikes";
    
    
    
}
