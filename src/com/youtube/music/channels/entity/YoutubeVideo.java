package com.youtube.music.channels.entity;

import java.util.Date;

public class YoutubeVideo {
    String title;
    String link;
    String thumbnail;
    Date   uploadTime;
    int    viewCount;
    int    duration;
    int    likes;
    int    dislikes;

    public YoutubeVideo() {
        this("", "", "", new Date(), 0, 0, 0, 0);
    }

    public YoutubeVideo(String title, String link, String thumbnail, Date uploadTime, int viewCount, int duration, int likes, int dislikes) {
        this.title = title;
        this.link = link;
        this.thumbnail = thumbnail;
        this.uploadTime = uploadTime;
        this.viewCount = viewCount;
        this.dislikes = dislikes;
        this.likes = likes;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getLink() {
        return link;
    }

    public int getViewCount() {
        return viewCount;
    }

    public Date getUploadDate() {
        return uploadTime;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public int getDuration() {
        return duration;
    }

}
