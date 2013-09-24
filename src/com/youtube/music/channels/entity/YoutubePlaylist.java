package com.youtube.music.channels.entity;

public class YoutubePlaylist {
    String title;
    String listId;
    String thumbnail;

    public YoutubePlaylist() {
        this("", "", "");
    }

    public YoutubePlaylist(String title, String listId, String thumbnail) {
        this.title = title;
        this.listId = listId;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getListId() {
        return listId;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
