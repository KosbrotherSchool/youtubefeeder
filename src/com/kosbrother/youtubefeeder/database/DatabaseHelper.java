package com.kosbrother.youtubefeeder.database;

import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "youtubeFeederDB.db";
    private static final int DATABASE_VERSION = 2;

    DatabaseHelper(Context context) {

        // calls the super constructor, requesting the default cursor factory.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *  video column: 
     *  String title;
     *  String link;
     *  String thumbnail;
     *  Date   uploadTime;
     *  int    viewCount;
     *  int    duration;
     *  int    likes;
     *  int    dislikes;
     *  
     */
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + VideoTable.TABLE_NAME + " ("
                + VideoTable._ID + " INTEGER PRIMARY KEY,"
                + VideoTable.COLUMN_NAME_DATA1 + " TEXT,"
                + VideoTable.COLUMN_NAME_DATA2 + " TEXT,"
                + VideoTable.COLUMN_NAME_DATA3 + " TEXT,"
                + VideoTable.COLUMN_NAME_DATA4 + " TEXT,"
                + VideoTable.COLUMN_NAME_DATA5 + " INTEGER,"
                + VideoTable.COLUMN_NAME_DATA6 + " INTEGER,"
                + VideoTable.COLUMN_NAME_DATA7 + " INTEGER,"
                + VideoTable.COLUMN_NAME_DATA8 + " INTEGER,"
                + VideoTable.COLUMN_NAME_DATA9 + " INTEGER,"
                + VideoTable.COLUMN_NAME_DATA10 + " TEXT"
                + ");");
        
        db.execSQL("CREATE TABLE " + ChannelTable.TABLE_NAME + " ("
                + ChannelTable._ID + " INTEGER PRIMARY KEY,"
                + ChannelTable.COLUMN_NAME_DATA1 + " TEXT,"
                + ChannelTable.COLUMN_NAME_DATA2 + " TEXT,"
                + ChannelTable.COLUMN_NAME_DATA3 + " TEXT,"
                + ChannelTable.COLUMN_NAME_DATA4 + " INTEGER"
                + ");");
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Kills the table and existing data
        db.execSQL("DROP TABLE IF EXISTS "+ VideoTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ ChannelTable.TABLE_NAME);

        // Recreates the database with a new version
        onCreate(db);
    }
}
