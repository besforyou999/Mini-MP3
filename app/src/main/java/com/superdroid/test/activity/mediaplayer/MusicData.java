package com.superdroid.test.activity.mediaplayer;

import android.graphics.drawable.Drawable;

public class MusicData {

    private String   title;
    private String   musicArtist;
    private Long     ID;
    private String   pathID;
    private Integer  Duration;
    private Drawable drawable;

    public MusicData(String title, String musicArtist, Long ID, String pathID, Integer Duration, Drawable d) {
        this.title       = title;
        this.musicArtist = musicArtist;
        this.ID          = ID;
        this.pathID      = pathID;
        this.Duration    = Duration;
        this.drawable    = d;
    }

    public String getTitle() { return title; }

    public String getMusicArtist() { return musicArtist; }

    public Long getID() { return ID; }

    public String getPathId() { return pathID; }

    public Integer getDuration() { return Duration; }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public void setPathID(String pathID) {
        this.pathID = pathID;
    }

    public void setDuration(Integer duration) {
        this.Duration = duration;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
