package com.youtube.music.channels.entity;

public class SubscribeChannel extends Channel {
	
	private String subscribtionId ="";
	
	public SubscribeChannel(String id, String title, String thumbnail, int videoNums, String subId) {
        this.id = id;
        this.title = title;
        this.thumbnail = thumbnail;
        this.videoNums = videoNums;
        this.subscribtionId = subId;
    }
	
	public String getSubId() {
        return subscribtionId;
    }
	
}
