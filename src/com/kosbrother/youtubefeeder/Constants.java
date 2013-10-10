/*
 * Copyright (c) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kosbrother.youtubefeeder;

import com.kosbrother.youtubefeeder.database.ChannelTable;
import com.kosbrother.youtubefeeder.database.VideoTable;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         This class hold constants.
 */
public class Constants {

    public static final String APP_NAME = "Youtube Feeder";
    public static final String YOUTUBE_WATCH_URL_PREFIX = "http://www.youtube.com/watch?v=";
    
    public static final String[] PROJECTION_VIDEO = new String[] {
		VideoTable._ID,
		VideoTable.COLUMN_NAME_DATA1,
		VideoTable.COLUMN_NAME_DATA2,
		VideoTable.COLUMN_NAME_DATA3,
		VideoTable.COLUMN_NAME_DATA4,
		VideoTable.COLUMN_NAME_DATA5,
		VideoTable.COLUMN_NAME_DATA6,
		VideoTable.COLUMN_NAME_DATA7,
		VideoTable.COLUMN_NAME_DATA8,
		VideoTable.COLUMN_NAME_DATA9,
		VideoTable.COLUMN_NAME_DATA10,
		VideoTable.COLUMN_NAME_DATA11
    };
	
	public static final String[] PROJECTION_CHANNEL = new String[] {
		ChannelTable._ID,
		ChannelTable.COLUMN_NAME_DATA1,
		ChannelTable.COLUMN_NAME_DATA2,
		ChannelTable.COLUMN_NAME_DATA3,
		ChannelTable.COLUMN_NAME_DATA4,
    };
    
}
