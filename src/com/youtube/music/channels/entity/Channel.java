package com.youtube.music.channels.entity;

public class Channel {

	String id;
	String title;
    String thumbnail;
    int videoNums;

    public Channel() {
        this("", "", "", 0);
    }

    public Channel(String id, String title, String thumbnail, int videoNums) {
        this.id = id;
        this.title = title;
        this.thumbnail = thumbnail;
        this.videoNums = videoNums;
    }

    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }


    public String getThumbnail() {
        return thumbnail;
    }
    
    public int getVideoNums() {
        return videoNums;
    }

}
