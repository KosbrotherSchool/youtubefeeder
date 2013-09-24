package com.kosbrother.youtubefeeder.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.youtube.music.channels.entity.Channel;
import com.youtube.music.channels.entity.YoutubePlaylist;
import com.youtube.music.channels.entity.YoutubeVideo;

//new AsyncTask() {
//
//  @Override
//  protected Object doInBackground(Object... params) {
//      // ArrayList<Channel> news = ChannelApi.getPromotionNews(1);
//      ArrayList<YoutubeVideo> a = ChannelApi.getPlaylistVideos("PLqbiv4ZP2BvpY8JuQwOaLutpvWFZhA3me", 0);
//      // ChannelApi.getChannelPlaylists("himservice", 0);
//      // ChannelApi.getChannelMostViewedVideo("himservice", 0);
//      a = null;
//      // ChannelApi.getChannelVideo("himservice", 0);
//
//      return null;
//  }
//
//}.execute();

public class ChannelApi {

    final static String         HOST  = "http://106.187.103.131";
    public static final String  TAG   = "CHANNEL_API";
    public static final boolean DEBUG = true;

    public static ArrayList<YoutubeVideo> getPlaylistVideos(String listId, int page) {
        ArrayList<YoutubeVideo> videos = new ArrayList();
        String url = "http://gdata.youtube.com/feeds/api/playlists/" + listId + "?v=2&alt=json&start-index=" + (page * 50 + 1) + "&max-results=50";
        String message = getMessageFromServer("GET", null, null, url);
        if (message == null) {
            return null;
        } else {
            try {
                JSONObject object = new JSONObject(message);
                JSONObject feedObject = object.getJSONObject("feed");
                JSONArray videoArray = feedObject.getJSONArray("entry");
                for (int i = 0; i < videoArray.length(); i++) {
                	String title = "";
                	String link = "";
                	String thumbnail = "";
                	try{
	                    title = videoArray.getJSONObject(i).getJSONObject("title").getString("$t");
	                    link = videoArray.getJSONObject(i).getJSONArray("link").getJSONObject(0).getString("href");
	                    thumbnail = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0)
	                            .getString("url");
                	}catch(Exception e){
                		title = "private";
                	}
                    int duration = 0;
                    int viewCount = 0;
                    try{
                        duration = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONObject("yt$duration").getInt("seconds");
                        viewCount = videoArray.getJSONObject(i).getJSONObject("yt$statistics").getInt("viewCount");
                    }catch(Exception e){
                    	
                    }
                    int dislikes = 0;
                    int likes = 0;
                    try{
                    	dislikes= videoArray.getJSONObject(i).getJSONObject("yt$rating").getInt("numDislikes");
                    	likes= videoArray.getJSONObject(i).getJSONObject("yt$rating").getInt("numLikes");
                    }catch(Exception e){
                    	
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    Date uploadTime = null;
                    try {
                        uploadTime = sdf.parse(videoArray.getJSONObject(i).getJSONObject("published").getString("$t"));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    YoutubeVideo video = new YoutubeVideo(title, link, thumbnail, uploadTime, viewCount, duration, likes, dislikes);
                    if(!title.equals("private")){
                    	videos.add(video);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return videos;
    }

    public static ArrayList<YoutubePlaylist> getChannelPlaylists(String channelName, int page) {
        ArrayList<YoutubePlaylist> lists = new ArrayList();
        String url = "https://gdata.youtube.com/feeds/api/users/" + channelName + "/playlists?v=2&alt=json&start-index=" + (page * 10 + 1) + "&max-results=10";
        String message = getMessageFromServer("GET", null, null, url);

        if (message == null) {
            return null;
        } else {
            try {
                JSONObject object = new JSONObject(message);
                JSONObject feedObject = object.getJSONObject("feed");
                JSONArray videoArray = feedObject.getJSONArray("entry");
                for (int i = 0; i < videoArray.length(); i++) {
                	String title = "";
                	String listId = "";
                	String thumbnail = "";
                	try{
                		title = videoArray.getJSONObject(i).getJSONObject("title").getString("$t");
                		listId = videoArray.getJSONObject(i).getJSONObject("yt$playlistId").getString("$t");
                		thumbnail = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0)
                            .getString("url");
                	}catch(Exception e){
                		title = "nothing";
                	}
                    YoutubePlaylist list = new YoutubePlaylist(title, listId, thumbnail);
                    if(!title.equals("nothing")){
                    	lists.add(list);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return lists;
    }

    public static ArrayList<YoutubeVideo> getChannelRatingVideo(String channelName, int page) {
        return getChannelVideo(channelName, page, "&orderby=rating");
    }

    public static ArrayList<YoutubeVideo> getChannelMostViewedVideo(String channelName, int page) {
        return getChannelVideo(channelName, page, "&orderby=viewCount");
    }

    public static ArrayList<YoutubeVideo> getChannelVideo(String channelName, int page, String param) {
        ArrayList<YoutubeVideo> videos = new ArrayList();
        String url = "https://gdata.youtube.com/feeds/api/users/" + channelName + "/uploads?v=2&alt=json&start-index=" + (page * 10 + 1) + "&max-results=10"
                + param;
        String message = getMessageFromServer("GET", null, null, url);
        if (message == null) {
            return null;
        } else {
            try {
                JSONObject object = new JSONObject(message);
                JSONObject feedObject = object.getJSONObject("feed");
                JSONArray videoArray = feedObject.getJSONArray("entry");
                for (int i = 0; i < videoArray.length(); i++) {
                	String title = "";
                	String link = "";
                	String thumbnail = "";
                	try{
	                    title = videoArray.getJSONObject(i).getJSONObject("title").getString("$t");
	                    link = videoArray.getJSONObject(i).getJSONArray("link").getJSONObject(0).getString("href");
	                    thumbnail = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0)
	                            .getString("url");
                	}catch(Exception e){
                		title = "private";
                	}
                    int duration = 0;
                    int viewCount = 0;
                    try{
                        duration = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONObject("yt$duration").getInt("seconds");
                        viewCount = videoArray.getJSONObject(i).getJSONObject("yt$statistics").getInt("viewCount");
                    }catch(Exception e){
                    	
                    }
                    int dislikes = 0;
                    int likes = 0;
                    try{
                    	dislikes= videoArray.getJSONObject(i).getJSONObject("yt$rating").getInt("numDislikes");
                    	likes= videoArray.getJSONObject(i).getJSONObject("yt$rating").getInt("numLikes");
                    }catch(Exception e){
                    	
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    Date uploadTime = null;
                    try {
                        uploadTime = sdf.parse(videoArray.getJSONObject(i).getJSONObject("published").getString("$t"));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    YoutubeVideo video = new YoutubeVideo(title, link, thumbnail, uploadTime, viewCount, duration, likes, dislikes);
                    if(!title.equals("private")){
                    	videos.add(video);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return videos;

    }
    
    public static ArrayList<YoutubeVideo> getChannelVideo(String channelName, int page, String param, int page_num) {
        ArrayList<YoutubeVideo> videos = new ArrayList();
        String url = "https://gdata.youtube.com/feeds/api/users/" + channelName + "/uploads?v=2&alt=json&start-index=" + 
        		(page * page_num + 1) + "&max-results=" + page_num + param;
        String message = getMessageFromServer("GET", null, null, url);
        if (message == null) {
            return null;
        } else {
            try {
                JSONObject object = new JSONObject(message);
                JSONObject feedObject = object.getJSONObject("feed");
                JSONArray videoArray = feedObject.getJSONArray("entry");
                for (int i = 0; i < videoArray.length(); i++) {
                	String title = "";
                	String link = "";
                	String thumbnail = "";
                	try{
	                    title = videoArray.getJSONObject(i).getJSONObject("title").getString("$t");
	                    link = videoArray.getJSONObject(i).getJSONArray("link").getJSONObject(0).getString("href");
	                    thumbnail = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0)
	                            .getString("url");
                	}catch(Exception e){
                		title = "private";
                	}
                    int duration = 0;
                    int viewCount = 0;
                    try{
                        duration = videoArray.getJSONObject(i).getJSONObject("media$group").getJSONObject("yt$duration").getInt("seconds");
                        viewCount = videoArray.getJSONObject(i).getJSONObject("yt$statistics").getInt("viewCount");
                    }catch(Exception e){
                    	
                    }
                    int dislikes = 0;
                    int likes = 0;
                    try{
                    	dislikes= videoArray.getJSONObject(i).getJSONObject("yt$rating").getInt("numDislikes");
                    	likes= videoArray.getJSONObject(i).getJSONObject("yt$rating").getInt("numLikes");
                    }catch(Exception e){
                    	
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    Date uploadTime = null;
                    try {
                        uploadTime = sdf.parse(videoArray.getJSONObject(i).getJSONObject("published").getString("$t"));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    YoutubeVideo video = new YoutubeVideo(title, link, thumbnail, uploadTime, viewCount, duration, likes, dislikes);
                    if(!title.equals("private")){
                    	videos.add(video);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return videos;

    }

//    public static ArrayList<Channel> getChannels(int area) {
//        ArrayList<Channel> channels = new ArrayList();
//        String message = getMessageFromServer("GET", "/api/v1/channels.json?area_id=" + area, null, null);
//        if (message == null) {
//            return null;
//        } else {
//            try {
//                JSONArray newsArray;
//                newsArray = new JSONArray(message.toString());
//                for (int i = 0; i < newsArray.length(); i++) {
//                    String name = newsArray.getJSONObject(i).getString("name");
//                    String link = newsArray.getJSONObject(i).getString("link");
//                    int id = newsArray.getJSONObject(i).getInt("id");
//                    String thumbnail = newsArray.getJSONObject(i).getString("thumbnail");
//
//                    Channel n = new Channel(name, link, id, thumbnail);
//                    channels.add(n);
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return channels;
//    }

    public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json, String apiUrl) {
        URL url;
        try {
            if (apiUrl != null)
                url = new URL(apiUrl);
            else
                url = new URL(HOST + apiPath);

            if (DEBUG)
                Log.d(TAG, "URL: " + url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);

            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            if (requestMethod.equalsIgnoreCase("POST"))
                connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();

            if (requestMethod.equalsIgnoreCase("POST")) {
                OutputStream outputStream;

                outputStream = connection.getOutputStream();
                if (DEBUG)
                    Log.d("post message", json.toString());

                outputStream.write(json.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder lines = new StringBuilder();
            ;
            String tempStr;

            while ((tempStr = reader.readLine()) != null) {
                lines = lines.append(tempStr);
            }
            if (DEBUG)
                Log.d("MOVIE_API", lines.toString());

            reader.close();
            connection.disconnect();

            return lines.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
