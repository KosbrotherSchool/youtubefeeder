package com.youtube.music.channels.entity;

public class Playlist {
    String title;
    String listId;
    String thumbnail;

    public Playlist() {
        this("", "", "");
    }

    public Playlist(String title, String listId, String thumbnail) {
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
