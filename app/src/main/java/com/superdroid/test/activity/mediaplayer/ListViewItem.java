package com.superdroid.test.activity.mediaplayer;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    private Drawable iconDrawable;
    private String musicTitle;

    public void setDrawable(Drawable d) {
        iconDrawable = d;
    }

    public void setMusicTitle(String title) {
        musicTitle = title;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public String getMusicTitle() {
        return musicTitle;
    }
}
